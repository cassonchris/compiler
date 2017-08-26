package casson.parser.symbols;

import java.util.ArrayList;

/**
 *
 * @author Chris
 */
public class TokenList extends ArrayList<Token> {

    public TokenList addToken(Token token) {
        this.add(token);
        return this;
    }
    
    public TokenList addToken(Operand operand, String value) {
        this.add(new OperandToken(operand, value));
        return this;
    }
}
