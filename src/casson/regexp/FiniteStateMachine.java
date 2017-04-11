package casson.regexp;

import java.util.Collection;
import java.util.Map;

abstract class FiniteStateMachine {

    static abstract class State {

        boolean acceptingState;

        State() { }

        abstract void addTransition(Character input, State nextState);
        abstract Map<Character, Collection<State>> getTransitionsInNonDeterministicForm();
        abstract Collection<State> getNextStateInNonDeterministicForm(Character input);
    }

    FiniteStateMachine() {}
    
    abstract Collection<State> getInitialStates();

    boolean accepts(String input) {
        return getInitialStates().stream().anyMatch(is -> accepts(input, is));
    }
    
    private boolean accepts(String input, State currentState) {
        if (currentState == null) {
            return false;
        } else if (input.isEmpty()) {
            return currentState.acceptingState;
        } else {
            char nextChar = input.charAt(0);
            String subString = input.substring(1);
            Collection<State> nextStates = currentState.getNextStateInNonDeterministicForm(nextChar);
            return nextStates != null && nextStates.stream().anyMatch(s -> accepts(subString, s));
        }
    }
}
