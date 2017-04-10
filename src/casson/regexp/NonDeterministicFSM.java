package casson.regexp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class NonDeterministicFSM extends FiniteStateMachine {

    @Override
    boolean accepts(String input) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
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
        State nextState(Character input) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        Collection<NonDeterministicState> initialStates = states.stream().filter(s -> s.initialState).collect(Collectors.toList());
        Collection<NonDeterministicState> acceptingStates = states.stream().filter(s -> s.acceptingState).collect(Collectors.toList());
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
        Collection<NonDeterministicState> leftAcceptingStates = this.states.stream().filter(s -> s.acceptingState).collect(Collectors.toList());
        NonDeterministicState rightInitialState = fsmRight.states.stream().filter(s -> s.initialState).findFirst().get();
        
        for (NonDeterministicState leftAcceptingState : leftAcceptingStates) {
            leftAcceptingState.mergeTransitions(rightInitialState.transitions);
            leftAcceptingState.acceptingState = rightInitialState.acceptingState;
        }
        
        Collection<NonDeterministicState> newStates = fsmRight.states.stream().filter(s -> !s.initialState).collect(Collectors.toList());
        
        this.states.addAll(newStates);
        
        return this;
    }

    NonDeterministicFSM union(NonDeterministicFSM fsmRight) {
        NonDeterministicState leftInitialState = this.states.stream().filter(s -> s.initialState).findFirst().get();
        NonDeterministicState rightInitialState = fsmRight.states.stream().filter(s -> s.initialState).findFirst().get();
        
        leftInitialState.mergeTransitions(rightInitialState.transitions);
        leftInitialState.acceptingState = leftInitialState.acceptingState || rightInitialState.acceptingState;
        
        Collection<NonDeterministicState> rightStates = fsmRight.states.stream().filter(s -> s.acceptingState).collect(Collectors.toList());
        this.states.addAll(rightStates);
        
        return this;
    }
}
