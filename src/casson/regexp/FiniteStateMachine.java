package casson.regexp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FiniteStateMachine {

    private class State {

        private Map<Character, List<State>> transitions;
        private boolean initialState;
        private boolean acceptingState;

        private State() {
            transitions = new HashMap<>();
        }

        private void addTransition(Character input, State nextState) {
            if (!transitions.containsKey(input)) {
                transitions.put(input, new ArrayList<>());
            }
            transitions.get(input).add(nextState);
        }
    }

    List<State> states;

    FiniteStateMachine(Character character) {
        State initialState = new State();
        initialState.initialState = true;
        
        State acceptingState = new State();
        acceptingState.acceptingState = true;
        
        initialState.addTransition(character, acceptingState);
        
        states = new ArrayList<>(Arrays.asList(initialState, acceptingState));
    }

    FiniteStateMachine closure() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    static FiniteStateMachine concat(FiniteStateMachine fsmLeft, FiniteStateMachine fsmRight) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    static FiniteStateMachine union(FiniteStateMachine fsmLeft, FiniteStateMachine fsmRight) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
