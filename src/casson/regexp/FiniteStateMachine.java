package casson.regexp;

import java.util.Collection;
import java.util.Map;

abstract class FiniteStateMachine {

    abstract class State {

        boolean acceptingState;

        State() { }

        abstract void addTransition(Character input, State nextState);
        abstract State nextState(Character input);
        abstract Map<Character, Collection<State>> getTransitionsInNonDeterministicForm();
    }

    FiniteStateMachine() {}
    
    abstract boolean accepts(String input);
}
