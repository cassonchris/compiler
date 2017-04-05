package casson.parser.symbols;

public class OperatorToken implements Token {

    private Operator operator;

    public OperatorToken(Operator operator) {
        this.operator = operator;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }
    
    @Override
    public Terminal getTerminalType() {
        return operator;
    }
}
