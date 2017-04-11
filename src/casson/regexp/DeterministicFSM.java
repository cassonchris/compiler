package casson.regexp;

import java.util.Arrays;
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
        Collection<State> getNextStateInNonDeterministicForm(Character input) {
            return Arrays.asList(transitions.get(input));
        }

        @Override
        Map<Character, Collection<State>> getTransitionsInNonDeterministicForm() {
            Map<Character, Collection<State>> nonDeterministicTransitions = new HashMap<>();
            for (Map.Entry<Character, State> entrySet : transitions.entrySet()) {
                Character character = entrySet.getKey();
                State nextState = entrySet.getValue();
                nonDeterministicTransitions.put(character, Arrays.asList(nextState));
            }
            return nonDeterministicTransitions;
        }
    }

    State initialState;
    Collection<DeterministicState> states;

    @Override
    Collection<State> getInitialStates() {
        return Arrays.asList(initialState);
    }
}
