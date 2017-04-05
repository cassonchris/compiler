package casson.parser.symbols;

public class PunctuationToken implements Token {

    private Punctuation punctuation;

    public PunctuationToken(Punctuation punctuation) {
        this.punctuation = punctuation;
    }

    public Punctuation getPunctuation() {
        return punctuation;
    }

    public void setPunctuation(Punctuation punctuation) {
        this.punctuation = punctuation;
    }

    @Override
    public Terminal getTerminalType() {
        return punctuation;
    }
}
