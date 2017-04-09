package casson.regexp;

abstract class FiniteStateMachine {

    abstract class State {

        boolean acceptingState;

        State() { }

        abstract void addTransition(Character input, State nextState);
    }

    FiniteStateMachine() {}
}
