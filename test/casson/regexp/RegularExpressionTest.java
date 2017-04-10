package casson.regexp;

import org.junit.Test;
import static org.junit.Assert.*;

public class RegularExpressionTest {

    public RegularExpressionTest() {
    }

    @Test
    public void testConstructor() {
        RegularExpression re = new RegularExpression("a*b*");
        re.printSyntaxTree();
        assertTrue(re.accepts("ab"));
        assertTrue(re.accepts("aab"));
        assertTrue(re.accepts("abb"));
        assertTrue(re.accepts("aabb"));
        assertTrue(re.accepts("a"));
        assertTrue(re.accepts("b"));
        assertFalse(re.accepts("aba"));
        assertFalse(re.accepts("abc"));
        
        re = new RegularExpression("a|bc");
        assertTrue(re.accepts("a"));
        assertTrue(re.accepts("bc"));
        
        re = new RegularExpression("a*b*|c*d*");
        assertTrue(re.accepts("aaabbb"));
        assertTrue(re.accepts("cccddd"));
        assertFalse(re.accepts("abcd"));
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
