package casson.regexp;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a deterministic FSM.
 * No epsilon transitions and only a single transition for each input.
 * 
 * @author Chris Casson
 */
class DeterministicFSM extends FiniteStateMachine {

    /**
     * This class represents a state in a deterministic FSM
     */
    static class DeterministicState extends State {

        // map to hold the state's transitions
        Map<Character, State> transitions;

        DeterministicState() {
            transitions = new HashMap<>();
        }

        @Override
        void addTransition(Character input, State nextState) {
            transitions.put(input, nextState);
        }

        /**
         * Converts the next state to non-deterministic form (a collection) and returns it
         * 
         * @param input
         * @return a collection containing the next state
         */
        @Override
        Collection<State> getNextStateInNonDeterministicForm(Character input) {
            return Arrays.asList(transitions.get(input));
        }

        /**
         * Converts the transitions to non-deterministic form (collections) and returns them
         * 
         * @return map containing the transitions in non-deterministic form
         */
        @Override
        Map<Character, Collection<State>> getTransitionsInNonDeterministicForm() {
            // create a map to hold the non-deterministic form transitions
            Map<Character, Collection<State>> nonDeterministicTransitions = new HashMap<>();
            
            // for each entry set in the transitions map
            for (Map.Entry<Character, State> entrySet : transitions.entrySet()) {
                // add the entry set to the non-deterministic map
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
