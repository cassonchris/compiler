package casson.regexp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class NonDeterministicFSM extends FiniteStateMachine {
    
    static class NonDeterministicState extends State {
        
        boolean initialState;
        Map<Character, List<State>> transitions;

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
        
        void mergeTransitions(Map<Character, List<State>> newTransitions) {
            for (Map.Entry<Character, List<State>> entrySet : newTransitions.entrySet()) {
                Character character = entrySet.getKey();
                List<State> transitionStates = entrySet.getValue();
                
                for (State state : transitionStates) {
                    addTransition(character, state);
                }
            }
        }
    }
    
    Collection<NonDeterministicState> states;

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
        NonDeterministicState mergedInitialState = new NonDeterministicState();
        List<NonDeterministicState> initialStates = states.stream().filter(s -> s.initialState).collect(Collectors.toList());
        List<NonDeterministicState> acceptingStates = states.stream().filter(s -> s.acceptingState).collect(Collectors.toList());
        for (NonDeterministicState initialState : initialStates) {
            mergedInitialState.mergeTransitions(initialState.transitions);
            initialState.acceptingState = true;
        }
        
        for (NonDeterministicState acceptingState : acceptingStates) {
            acceptingState.mergeTransitions(mergedInitialState.transitions);
        }
        
        return this;
    }

    NonDeterministicFSM concat(NonDeterministicFSM fsmRight) {
        List<NonDeterministicState> leftAcceptingStates = this.states.stream().filter(s -> s.acceptingState).collect(Collectors.toList());
        NonDeterministicState rightInitialState = fsmRight.states.stream().filter(s -> s.initialState).findFirst().get();
        
        for (NonDeterministicState leftAcceptingState : leftAcceptingStates) {
            leftAcceptingState.mergeTransitions(rightInitialState.transitions);
            leftAcceptingState.acceptingState = rightInitialState.acceptingState;
        }
        
        List<NonDeterministicState> newStates = fsmRight.states.stream().filter(s -> !s.initialState).collect(Collectors.toList());
        
        this.states.addAll(newStates);
        
        return this;
    }

    NonDeterministicFSM union(NonDeterministicFSM fsmRight) {
        NonDeterministicState leftInitialState = this.states.stream().filter(s -> s.initialState).findFirst().get();
        NonDeterministicState rightInitialState = fsmRight.states.stream().filter(s -> s.initialState).findFirst().get();
        
        leftInitialState.mergeTransitions(rightInitialState.transitions);
        leftInitialState.acceptingState = leftInitialState.acceptingState || rightInitialState.acceptingState;
        
        List<NonDeterministicState> rightStates = fsmRight.states.stream().filter(s -> s.acceptingState).collect(Collectors.toList());
        this.states.addAll(rightStates);
        
        return this;
    }
}
