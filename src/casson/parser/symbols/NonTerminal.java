package casson.parser.symbols;

public enum NonTerminal implements Symbol {
    GOAL,
    EXPRESSION,
    TERM,
    FACTOR;
    
    @Override
    public String toString() {
        return "<" + this.name() + ">";
    }
}
