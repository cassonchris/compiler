package casson.parser.symbols;

public class OperandToken implements Token {

    private Operand operand;
    private String value;

    public OperandToken(Operand operand, String value) {
        this.operand = operand;
        this.value = value;
    }

    public Operand getOperand() {
        return operand;
    }

    public void setOperand(Operand operand) {
        this.operand = operand;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public Terminal getTerminalType() {
        return operand;
    }
    
    @Override
    public String toString() {
        return "<" + operand + "," + value + ">";
    }
}
