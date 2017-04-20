package casson.regexp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class represents a non-deterministic FSM.
 * Epsilon transitions are allowed and there can be multiple transitions for a character.
 * 
 * @author Chris Casson
 */
class NonDeterministicFSM extends FiniteStateMachine {
    
    /**
     * This class represents a state in a non-deterministic FSM
     */
    static class NonDeterministicState extends State {
        
        boolean initialState;
        Map<Character, Collection<State>> transitions;

        NonDeterministicState() {
            transitions = new HashMap<>();
        }

        @Override
        void addTransition(Character input, State nextState) {
            if (!transitions.containsKey(input)) {
                transitions.put(input, new ArrayList<>());
            }
            transitions.get(input).add(nextState);
        }
        
        @Override
        Map<Character, Collection<State>> getTransitionsInNonDeterministicForm() {
            return transitions;
        }
        
        /**
         * Merge the new transitions with the existing transitions.
         * 
         * @param newTransitions 
         */
        void mergeTransitions(Map<Character, Collection<State>> newTransitions) {
            for (Map.Entry<Character, Collection<State>> entrySet : newTransitions.entrySet()) {
                Character character = entrySet.getKey();
                Collection<State> transitionStates = entrySet.getValue();
                
                for (State state : transitionStates) {
                    addTransition(character, state);
                }
            }
        }

        @Override
        Collection<State> getNextStateInNonDeterministicForm(Character input) {
            return transitions.get(input);
        }
    }
    
    Collection<NonDeterministicState> states;

    /**
     * Creates a non-deterministic FSM that accepts the given character as input.
     * 
     * @param character 
     */
    NonDeterministicFSM(Character character) {
        super();
        
        // create an initial state
        NonDeterministicState initialState = new NonDeterministicState();
        initialState.initialState = true;
        
        // create an accepting state
        NonDeterministicState acceptingState = new NonDeterministicState();
        acceptingState.acceptingState = true;
        
        // add a transition from initial to accepting for input character
        initialState.addTransition(character, acceptingState);
        
        // assign initial and accepting to states
        states = new ArrayList<>(Arrays.asList(initialState, acceptingState));
    }

    /**
     * 
     * @return states with initialState set to true
     */
    @Override
    Collection<State> getInitialStates() {
        return states.stream().filter(s -> s.initialState).collect(Collectors.toList());
    }

    /**
     * Applies closure to the FSM.
     * 
     * @return the NonDeterministicFSM on which closure() was called
     */
    NonDeterministicFSM closure() {
        // create a state to hold all the transitions from the initial states
        // these transitions get added to all the accepting states
        NonDeterministicState mergedInitialState = new NonDeterministicState();
        
        // get the initial states and accepting states
        Collection<NonDeterministicState> initialStates = states.stream().filter(s -> s.initialState).collect(Collectors.toList());
        Collection<NonDeterministicState> acceptingStates = states.stream().filter(s -> s.acceptingState).collect(Collectors.toList());
        
        // for each initial state
        for (NonDeterministicState initialState : initialStates) {
            // add the transitions to the merged initial state
            mergedInitialState.mergeTransitions(initialState.transitions);
            // make the initial state an accepting state
            initialState.acceptingState = true;
        }
        
        // for each accepting state
        for (NonDeterministicState acceptingState : acceptingStates) {
            // add all the transitions from the initial states
            acceptingState.mergeTransitions(mergedInitialState.transitions);
        }
        
        return this;
    }

    /**
     * Concats the given FSM to this FSM.
     * 
     * @param fsmRight the FSM to concat to this
     * @return the NonDeterministicFSM on which concat() was called
     */
    NonDeterministicFSM concat(NonDeterministicFSM fsmRight) {
        // get the accepting states from the left
        Collection<NonDeterministicState> leftAcceptingStates = this.states.stream().filter(s -> s.acceptingState).collect(Collectors.toList());
        
        // get the first initial state on the right
        // there should only be one
        NonDeterministicState rightInitialState = fsmRight.states.stream().filter(s -> s.initialState).findFirst().get();
        
        // for each left accepting state
        for (NonDeterministicState leftAcceptingState : leftAcceptingStates) {
            // add the transitions from the right initial state
            leftAcceptingState.mergeTransitions(rightInitialState.transitions);
            // update accepting to right initial accepting
            leftAcceptingState.acceptingState = rightInitialState.acceptingState;
        }
        
        // get the non-initial states from the right and add them to the left
        Collection<NonDeterministicState> newStates = fsmRight.states.stream().filter(s -> !s.initialState).collect(Collectors.toList());
        this.states.addAll(newStates);
        
        return this;
    }

    /**
     * Unions this FSM with the given FSM
     * 
     * @param fsmRight the FSM to union
     * @return the NonDeterministicFSM on which union() was called
     */
    NonDeterministicFSM union(NonDeterministicFSM fsmRight) {
        // get the initial states from the left and the right
        NonDeterministicState leftInitialState = this.states.stream().filter(s -> s.initialState).findFirst().get();
        NonDeterministicState rightInitialState = fsmRight.states.stream().filter(s -> s.initialState).findFirst().get();
        
        // add the transitions from the right initial state to the left initial state
        leftInitialState.mergeTransitions(rightInitialState.transitions);
        
        // if either of the initial states are accepting states then the left initial state is accepting
        leftInitialState.acceptingState = leftInitialState.acceptingState || rightInitialState.acceptingState;
        
        // add states (other than initial) from the right to the left
        Collection<NonDeterministicState> rightStates = fsmRight.states.stream().filter(s -> !s.initialState).collect(Collectors.toList());
        this.states.addAll(rightStates);
        
        return this;
    }
}
