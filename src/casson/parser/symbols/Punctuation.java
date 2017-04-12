package casson.parser.symbols;

public enum Punctuation implements Terminal, Token {
    EOF,
    DOT;
    
    @Override
    public Terminal getTerminalType() {
        return this;
    }
}
