package casson.regexp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class DeterministicFSM extends FiniteStateMachine {

    static class DeterministicState extends State {

        Map<Character, State> transitions;

        DeterministicState() {
            transitions = new HashMap<>();
        }

        @Override
        void addTransition(Character input, State nextState) {
            transitions.put(input, nextState);
        }
        
        @Override
        State nextState(Character input) {
            return transitions.get(input);
        }
    }

    State initialState;
    Collection<DeterministicState> states;

    DeterministicFSM() {
        super();
    }

    @Override
    boolean accepts(String input) {
        return accepts(input, initialState);
    }
    
    boolean accepts(String input, State currentState) {
        if (currentState == null) {
            return false;
        } else if (input.isEmpty()) {
            return currentState.acceptingState;
        } else {
            currentState = currentState.nextState(input.charAt(0));
            return accepts(input.substring(1), currentState);
        }
    }
}
