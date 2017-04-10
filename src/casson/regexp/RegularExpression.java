package casson.regexp;

import casson.regexp.DeterministicFSM.DeterministicState;
import casson.regexp.FiniteStateMachine.State;
import casson.regexp.NonDeterministicFSM.NonDeterministicState;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RegularExpression {

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

        SyntaxTree previousCharacterTree = null;
        for (char character : expression.toCharArray()) {
            SyntaxTree currentTree = new SyntaxTree(character);
            if (character == '*') { // closure
                if (previousCharacterTree == null) {
                    throw new IllegalArgumentException(expression + " is not a valid regular expression.");
                }
                if (previousCharacterTree.parentTree != null) {
                    previousCharacterTree.parentTree.replace(previousCharacterTree, currentTree);
                }
                currentTree.setLeftTree(previousCharacterTree);
            } else if (character == '|') { // union
                if (previousCharacterTree == null) {
                    throw new IllegalArgumentException(expression + " is not a valid regular expression.");
                }
                while (previousCharacterTree.parentTree != null
                        && getPrecedence(previousCharacterTree.content) > getPrecedence(previousCharacterTree.parentTree.content)) {
                    previousCharacterTree = previousCharacterTree.parentTree;
                }
                if (previousCharacterTree.parentTree != null) {
                    previousCharacterTree.parentTree.replace(previousCharacterTree, currentTree);
                }
                currentTree.setLeftTree(previousCharacterTree);
            } else { // character literal
                if (previousCharacterTree != null) { // current is not the root
                    if (previousCharacterTree.content == '|') {
                        previousCharacterTree.setRightTree(currentTree);
                    } else {
                        SyntaxTree concatTree = new SyntaxTree('+');
                        if (previousCharacterTree.parentTree != null) { // previous is not the root
                            previousCharacterTree.parentTree.replace(previousCharacterTree, concatTree);
                        }

                        concatTree.setLeftTree(previousCharacterTree);
                        concatTree.setRightTree(currentTree);
                    }
                }
            }

            previousCharacterTree = currentTree;
        }

        SyntaxTree root = previousCharacterTree;
        while (root.parentTree != null) {
            root = root.parentTree;
        }
        return root;
    }

    public void printSyntaxTree() {
        printSyntaxTree(syntaxTree, 0);
    }

    private void printSyntaxTree(SyntaxTree tree, int tabsIndented) {
        if (tree.leftTree != null) {
            printSyntaxTree(tree.leftTree, tabsIndented + 1);
        }
        for (int i = 0; i < tabsIndented; i++) {
            System.out.print("\t");
        }
        System.out.println(tree.content);
        if (tree.rightTree != null) {
            printSyntaxTree(tree.rightTree, tabsIndented + 1);
        }
    }
    
    private NonDeterministicFSM generateNonDeterministicFSM(SyntaxTree tree) {
        NonDeterministicFSM fsmLeft = null;
        if (tree.leftTree != null) {
            fsmLeft = generateNonDeterministicFSM(tree.leftTree);
        }
        NonDeterministicFSM fsmRight = null;
        if (tree.rightTree != null) {
            fsmRight = generateNonDeterministicFSM(tree.rightTree);
        }
        
        NonDeterministicFSM fsm;
        if (tree.content == '*') {
            // the one to closure should be on the left
            fsm = fsmLeft.closure();
        } else if (tree.content == '+') {
            fsm = fsmLeft.concat(fsmRight);
        } else if (tree.content == '|') {
            fsm = fsmLeft.union(fsmRight);
        } else {
            fsm = new NonDeterministicFSM(tree.content);
        }
        return fsm;
    }
    
    private DeterministicFSM generateDeterministicFSM(SyntaxTree tree) {
        NonDeterministicFSM nonDeterministicFSM = generateNonDeterministicFSM(tree);
        List<NonDeterministicState> initialStates = nonDeterministicFSM.states.stream().filter(s -> s.initialState).collect(Collectors.toList());
        if (initialStates.size() != 1) {
            throw new IllegalArgumentException("Cannot construct DeterministicFSM from NonDeterministicFSM that doesn't have exactly one initial state.");
        }
        
        NonDeterministicState oldInitialState = initialStates.get(0);
        Map<String, DeterministicState> conversionMap = new HashMap<>();
        DeterministicFSM fsm = new DeterministicFSM();
        fsm.initialState = addConvertedState(conversionMap, String.valueOf(oldInitialState.hashCode()), oldInitialState);
        
        fsm.states = conversionMap.values();
        return fsm;
    }
    
    private DeterministicState addConvertedState(Map<String, DeterministicState> conversionMap, String key, NonDeterministicState state) {
        if (conversionMap.containsKey(key)) {
            return conversionMap.get(key);
        } else {
            DeterministicState newState = new DeterministicState();
            newState.acceptingState = state.acceptingState;
            conversionMap.put(key, newState);
            for (Map.Entry<Character, Collection<State>> transition : state.transitions.entrySet()) {
                Character character = transition.getKey();
                Collection<State> transitionStates = transition.getValue();
                
                String transitionKey = "";
                NonDeterministicState transitionState = new NonDeterministicState();
                for (State s : transitionStates) {
                    transitionKey += String.valueOf(s.hashCode()) + ",";
                    transitionState.mergeTransitions(s.getTransitionsInNonDeterministicForm());
                }
                int lastComma = transitionKey.lastIndexOf(",");
                if (lastComma != -1) {
                    transitionKey = transitionKey.substring(0, lastComma);
                }
                
                transitionState.acceptingState = transitionStates.stream().anyMatch(s -> s.acceptingState);
                
                DeterministicState deterministicTransitionState = addConvertedState(conversionMap, transitionKey, transitionState);
                newState.addTransition(character, deterministicTransitionState);
            }
            return newState;
        }
    }
    
    public boolean accepts(String input) {
        return finiteStateMachine.accepts(input);
    }
}
