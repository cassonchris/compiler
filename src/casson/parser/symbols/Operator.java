package casson.parser.symbols;

public enum Operator implements Terminal, Token {
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE;
    
    @Override
    public Terminal getTerminalType() {
        return this;
    }
}
