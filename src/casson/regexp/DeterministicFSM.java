package casson.regexp;

import java.util.HashMap;
import java.util.Map;

class DeterministicFSM extends FiniteStateMachine {
    
    private class DeterministicState extends State {
        
        private Map<Character, State> transitions;

        DeterministicState() {
            transitions = new HashMap<>();
        }
                
        @Override
        void addTransition(Character input, State nextState) {
            transitions.put(input, nextState);
        }
    }

    DeterministicFSM(Character character) {
        super();
    }

}
