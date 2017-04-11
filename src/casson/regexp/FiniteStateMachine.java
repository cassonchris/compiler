package casson.regexp;

import java.util.Collection;
import java.util.Map;

/**
 * This class represents a finite state machine.
 * 
 * @author Chris Casson
 */
abstract class FiniteStateMachine {

    /**
     * Represents the states of the FSM.
     */
    static abstract class State {

        boolean acceptingState;

        State() { }

        abstract void addTransition(Character input, State nextState);
        abstract Map<Character, Collection<State>> getTransitionsInNonDeterministicForm();
        abstract Collection<State> getNextStateInNonDeterministicForm(Character input);
    }

    FiniteStateMachine() {}
    
    abstract Collection<State> getInitialStates();

    /**
     * 
     * @param input
     * @return true if this FSM accepts the input string, otherwise false
     */
    boolean accepts(String input) {
        // call accepts with the input and the initial state(s)
        return getInitialStates().stream().anyMatch(is -> accepts(input, is));
    }
    
    private boolean accepts(String input, State currentState) {
        if (currentState == null) {
            // the FSM went to a "dead" state
            return false;
        } else if (input.isEmpty()) {
            // the FSM finished processing the input
            return currentState.acceptingState;
        } else {
            // get the next character
            char nextChar = input.charAt(0);
            
            // remove the character from the input
            String subString = input.substring(1);
            
            // get the next states using the next character
            Collection<State> nextStates = currentState.getNextStateInNonDeterministicForm(nextChar);
            
            // if nextChar didn't go to a dead state, recursively call accepts with the substring and the next state
            return nextStates != null && nextStates.stream().anyMatch(s -> accepts(subString, s));
        }
    }
}
