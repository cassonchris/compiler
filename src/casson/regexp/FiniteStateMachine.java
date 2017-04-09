package casson.regexp;

import java.util.List;

abstract class FiniteStateMachine {

    abstract class State {

        boolean initialState;
        boolean acceptingState;

        State() { }

        abstract void addTransition(Character input, State nextState);
    }

    List<State> states;

    FiniteStateMachine() {}
}
