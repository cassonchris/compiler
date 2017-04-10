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
    }

    State initialState;
    Collection<DeterministicState> states;

    DeterministicFSM() {
        super();
    }

}
