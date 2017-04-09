package casson.regexp;

abstract class FiniteStateMachine {

    abstract class State {

        boolean initialState;
        boolean acceptingState;

        State() { }

        abstract void addTransition(Character input, State nextState);
    }

    FiniteStateMachine() {}
}
