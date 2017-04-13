package casson.parser.symbols;

public enum Punctuation implements Terminal, Token {
    EOF,
    DOT,
    LEFTPAREN,
    RIGHTPAREN;
    
    @Override
    public Terminal getTerminalType() {
        return this;
    }
}
