package casson.regexp;

import org.junit.Test;
import static org.junit.Assert.*;

public class RegularExpressionTest {

    public RegularExpressionTest() {
    }

    @Test
    public void testConstructor() {
        RegularExpression re = new RegularExpression("0*a|bc|3*f");
        re.printSyntaxTree();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmpty() {
        RegularExpression empty = new RegularExpression("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorInvalid() {
        RegularExpression invalid = new RegularExpression("*a");
    }
}
