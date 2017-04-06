package casson.parser.symbols;

public enum Punctuation implements Terminal, Token {
    EOF;
    
    @Override
    public Terminal getTerminalType() {
        return this;
    }
}
