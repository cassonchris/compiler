package casson.regexp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class NonDeterministicFSM extends FiniteStateMachine {
    
    private class NonDeterministicState extends State {
        
        private Map<Character, List<State>> transitions;

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
    }

    NonDeterministicFSM(Character character) {
        super();
        NonDeterministicState initialState = new NonDeterministicState();
        initialState.initialState = true;
        
        NonDeterministicState acceptingState = new NonDeterministicState();
        acceptingState.acceptingState = true;
        
        initialState.addTransition(character, acceptingState);
        
        states = new ArrayList<>(Arrays.asList(initialState, acceptingState));
    }

    NonDeterministicFSM closure() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    static NonDeterministicFSM concat(FiniteStateMachine fsmLeft, FiniteStateMachine fsmRight) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    static NonDeterministicFSM union(FiniteStateMachine fsmLeft, FiniteStateMachine fsmRight) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
