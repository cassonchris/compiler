package casson.regexp;

import casson.regexp.DeterministicFSM.DeterministicState;
import casson.regexp.FiniteStateMachine.State;
import casson.regexp.NonDeterministicFSM.NonDeterministicState;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a regular expression that supports the following special characters:
 * 
 * () - parenthesis - used for grouping and explicit order of operations.
 * * - closure - 0 or more occurrences of the previous character or character group.
 * | - union - the character or character group on the left OR the character or character group on the right.
 * concatenation (no explicit character) - characters or character groups next to each other are concatenated together.
 * 
 * @author Chris Casson
 */
public class RegularExpression {

    /**
     * The SyntaxTree is used to represent the regular expression in tree form.
     */
    private class SyntaxTree {

        private SyntaxTree parentTree;
        private SyntaxTree leftTree;
        private SyntaxTree rightTree;
        private final char content;

        private SyntaxTree(char content) {
            this.content = content;
        }

        private void setLeftTree(SyntaxTree leftTree) {
            this.leftTree = leftTree;
            leftTree.parentTree = this;
        }

        private void setRightTree(SyntaxTree rightTree) {
            this.rightTree = rightTree;
            rightTree.parentTree = this;
        }

        private void replace(SyntaxTree oldTree, SyntaxTree newTree) {
            if (leftTree == oldTree) {
                setLeftTree(newTree);
            } else if (rightTree == oldTree) {
                setRightTree(newTree);
            }
        }
    }

    private final SyntaxTree syntaxTree;
    private final FiniteStateMachine finiteStateMachine;

    /**
     * Create a RegularExpression object for the given expression.
     * @param expression
     */
    public RegularExpression(String expression) {
        syntaxTree = generateSyntaxTree(expression);
        finiteStateMachine = generateDeterministicFSM(syntaxTree);
    }

    private int getPrecedence(char a) {
        switch (a) {
            case '*':
                return 1;
            case '+':
                return 2;
            case '|':
                return 3;
            default:
                return 4;
        }
    }

    private SyntaxTree generateSyntaxTree(String expression) {
        if (expression == null || expression.isEmpty()) {
            throw new IllegalArgumentException("Regular expression cannot be empty.");
        }

        // previousCharacterTree is used to keep track of where we need to add to
        SyntaxTree previousCharacterTree = null;
        for (char character : expression.toCharArray()) {
            
            // create a tree with the current character
            SyntaxTree currentTree = new SyntaxTree(character);
            
            if (character == '*') { // closure
                // the first character can't be *
                if (previousCharacterTree == null) {
                    throw new IllegalArgumentException(expression + " is not a valid regular expression.");
                }
                
                // if the previous character has a parent, 
                // the closure character needs to be inserted in between the previous character and its parent
                if (previousCharacterTree.parentTree != null) {
                    previousCharacterTree.parentTree.replace(previousCharacterTree, currentTree);
                }
                
                // make the previous character a child of the closure node
                currentTree.setLeftTree(previousCharacterTree);
            } else if (character == '|') { // union
                // the first character can't be |
                if (previousCharacterTree == null) {
                    throw new IllegalArgumentException(expression + " is not a valid regular expression.");
                }
                
                // walk up the tree to find the correct insertion point
                while (previousCharacterTree.parentTree != null
                        // | should go above characters with lower precendence (*, +, ...)
                        && getPrecedence(previousCharacterTree.content) > getPrecedence(previousCharacterTree.parentTree.content)
                        // stop walking the tree if the left paren is encountered
                        && previousCharacterTree.parentTree.content != '(') {
                    
                    previousCharacterTree = previousCharacterTree.parentTree;
                }
                
                // if the previous tree has a parent,
                // the union character needs to be inserted in between the previous tree and its parent
                if (previousCharacterTree.parentTree != null) {
                    previousCharacterTree.parentTree.replace(previousCharacterTree, currentTree);
                }
                
                // make the previous tree a child of the union node
                currentTree.setLeftTree(previousCharacterTree);
            } else if (character == ')') {
                // walk up the tree to find the node with the left paren
                SyntaxTree leftParen = previousCharacterTree;
                while (leftParen.parentTree != null
                        && leftParen.content != '(') {
                    leftParen = leftParen.parentTree;
                }
                
                // remove the left paren node
                if (leftParen.parentTree == null) {
                    leftParen.leftTree.parentTree = null;
                } else {
                    leftParen.parentTree.replace(leftParen, leftParen.leftTree);
                }
                
                // left paren's left tree is the point to continue building the tree from
                currentTree = leftParen.leftTree;
            } else { // character literal
                if (previousCharacterTree != null) { // current is not the root
                    
                    // if the current character is the right side of a union
                    if (previousCharacterTree.content == '|') {
                        // if the rightTree of the union is available, just set it
                        if (previousCharacterTree.rightTree == null) {
                            previousCharacterTree.setRightTree(currentTree);
                        } else { // this happens when the union was in parenthesis
                            
                            // insert a concat in between the union and its parent (if it has a parent)
                            SyntaxTree concatTree = new SyntaxTree('+');
                            if (previousCharacterTree.parentTree != null) { // previous is not the root
                                previousCharacterTree.parentTree.replace(previousCharacterTree, concatTree);
                            }

                            // concat the union (previousCharacterTree) and the currentTree
                            concatTree.setLeftTree(previousCharacterTree);
                            concatTree.setRightTree(currentTree);
                        }
                    } else if (previousCharacterTree.content == '(') {
                        previousCharacterTree.setLeftTree(currentTree);
                    } else { 
                        // insert a concat in between the previous and its parent (if it has a parent)
                        SyntaxTree concatTree = new SyntaxTree('+');
                        if (previousCharacterTree.parentTree != null) { // previous is not the root
                            previousCharacterTree.parentTree.replace(previousCharacterTree, concatTree);
                        }

                        // concat previous and current
                        concatTree.setLeftTree(previousCharacterTree);
                        concatTree.setRightTree(currentTree);
                    }
                }
            }

            // set previous to current for the next iteration of the loop
            previousCharacterTree = currentTree;
        }

        // find the root of the tree and return it
        SyntaxTree root = previousCharacterTree;
        while (root.parentTree != null) {
            root = root.parentTree;
        }
        return root;
    }

    /**
     * Prints to standard out a visual representation of the tree with the top on the left and branching to the right.
     */
    public void printSyntaxTree() {
        printSyntaxTree(syntaxTree, 0);
    }

    private void printSyntaxTree(SyntaxTree tree, int tabsIndented) {
        // print the left tree with 1 extra tab
        if (tree.leftTree != null) {
            printSyntaxTree(tree.leftTree, tabsIndented + 1);
        }
        // print the tabs
        for (int i = 0; i < tabsIndented; i++) {
            System.out.print("\t");
        }
        // print the current node's content
        System.out.println(tree.content);
        // print the right tree with 1 extra tab
        if (tree.rightTree != null) {
            printSyntaxTree(tree.rightTree, tabsIndented + 1);
        }
    }
    
    private NonDeterministicFSM generateNonDeterministicFSM(SyntaxTree tree) {
        // generate the non-deterministic fsm for the left tree
        NonDeterministicFSM fsmLeft = null;
        if (tree.leftTree != null) {
            fsmLeft = generateNonDeterministicFSM(tree.leftTree);
        }
        
        // generate the non-deterministic fsm for the right tree
        NonDeterministicFSM fsmRight = null;
        if (tree.rightTree != null) {
            fsmRight = generateNonDeterministicFSM(tree.rightTree);
        }
        
        NonDeterministicFSM fsm;
        if (tree.content == '*') {
            // the one to closure should be on the left
            fsm = fsmLeft.closure();
        } else if (tree.content == '+') {
            // concatenate the left and right fsms
            fsm = fsmLeft.concat(fsmRight);
        } else if (tree.content == '|') {
            // union the left and right fsms
            fsm = fsmLeft.union(fsmRight);
        } else {
            // the tree content is a character literal
            // create a new non-deterministic fsm for the character
            fsm = new NonDeterministicFSM(tree.content);
        }
        return fsm;
    }
    
    private DeterministicFSM generateDeterministicFSM(SyntaxTree tree) {
        // first generate a non-deterministic fsm
        NonDeterministicFSM nonDeterministicFSM = generateNonDeterministicFSM(tree);
        
        // get the initial states from the non-deterministic fsm
        List<NonDeterministicState> initialStates = nonDeterministicFSM.states.stream()
                .filter(s -> s.initialState)
                .collect(Collectors.toList());
        
        // there has to be exactly 1 initial state in order to generate a deterministic fsm
        if (initialStates.size() != 1) {
            throw new IllegalArgumentException("Cannot construct DeterministicFSM from NonDeterministicFSM that doesn't have exactly one initial state.");
        }
        
        // get the initial state
        NonDeterministicState oldInitialState = initialStates.get(0);
        
        // create a conversion map to store the states that are converted to deterministic states
        Map<String, DeterministicState> conversionMap = new HashMap<>();
        
        // create a deterministic fsm
        DeterministicFSM fsm = new DeterministicFSM();
        
        // convert the initial state and set it as the fsm initial state
        // convertState will recursively call convertState for states referred to by oldInitialState
        fsm.initialState = convertState(conversionMap, String.valueOf(oldInitialState.hashCode()), oldInitialState);
        
        // set fsm.states to the converted states in the conversion map
        fsm.states = conversionMap.values();
        return fsm;
    }
    
    private DeterministicState convertState(Map<String, DeterministicState> conversionMap, String key, NonDeterministicState state) {
        // base case - the key already exists, just return the value
        if (conversionMap.containsKey(key)) {
            return conversionMap.get(key);
        } else {
            // create a new deterministic state and add to the conversionMap
            DeterministicState newState = new DeterministicState();
            newState.acceptingState = state.acceptingState;
            conversionMap.put(key, newState);
            
            // for each transition
            for (Map.Entry<Character, Collection<State>> transition : state.transitions.entrySet()) {
                // get the key (the char) and the value (next states)
                Character character = transition.getKey();
                Collection<State> transitionStates = transition.getValue();
                
                // build a key for the combined state (comma separated list of next state hash codes)
                // and build a non-deterministic state that contains the transitions for all states in transitionStates
                String combinedStateKey = "";
                NonDeterministicState combinedState = new NonDeterministicState();
                for (State s : transitionStates) {
                    combinedStateKey += String.valueOf(s.hashCode()) + ",";
                    combinedState.mergeTransitions(s.getTransitionsInNonDeterministicForm());
                }
                
                // remove the last comma from the transition key
                int lastComma = combinedStateKey.lastIndexOf(",");
                if (lastComma != -1) {
                    combinedStateKey = combinedStateKey.substring(0, lastComma);
                }
                
                // the combined state is accepting if any states in transitionStates are accepting
                combinedState.acceptingState = transitionStates.stream().anyMatch(s -> s.acceptingState);
                
                // convert the combined non-deterministic state to a deterministic state
                DeterministicState deterministicTransitionState = convertState(conversionMap, combinedStateKey, combinedState);
                
                // add the deterministic transition to newState
                newState.addTransition(character, deterministicTransitionState);
            }
            return newState;
        }
    }
    
    /**
     *
     * @param input
     * @return true if the input string matches the regular expression, false otherwise.
     */
    public boolean accepts(String input) {
        return finiteStateMachine.accepts(input);
    }
}
