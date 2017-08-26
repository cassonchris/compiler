package casson;

import casson.parser.symbols.NonTerminal;
import casson.parser.symbols.Operand;
import casson.parser.symbols.Operator;
import casson.parser.symbols.Punctuation;
import casson.parser.symbols.TokenList;
import org.junit.Test;
import static org.junit.Assert.*;

public class GrammarTest {

    public GrammarTest() {
    }

    @Test
    public void testK0Grammar() {
        GrammarBuilder gb = new GrammarBuilder();
        gb.addProduction(NonTerminal.GOAL,
                NonTerminal.EXPRESSION)
            .addProduction(NonTerminal.EXPRESSION,
                NonTerminal.EXPRESSION,
                Operator.PLUS,
                NonTerminal.TERM)
            .addProduction(NonTerminal.EXPRESSION,
                NonTerminal.TERM)
            .addProduction(NonTerminal.TERM,
                Operand.ID)
            .addProduction(NonTerminal.TERM,
                Punctuation.LEFTPAREN,
                NonTerminal.EXPRESSION,
                Punctuation.RIGHTPAREN);
        
        Grammar grammar = gb.toGrammar(0);

        TokenList tokens = new TokenList();
        tokens.addToken(Operand.ID, "x")
            .addToken(Operator.PLUS)
            .addToken(Operand.ID, "y")
            .addToken(Operator.PLUS)
            .addToken(Operand.ID, "z")
            .addToken(Punctuation.EOF);

        assertTrue(grammar.accepts(tokens));
        
        System.out.println();
        tokens = new TokenList();
        tokens.addToken(Punctuation.LEFTPAREN)
            .addToken(Operand.ID, "x")
            .addToken(Punctuation.RIGHTPAREN)
            .addToken(Punctuation.EOF);
        
        assertTrue(grammar.accepts(tokens));
        
        System.out.println();
        tokens = new TokenList();
        tokens.addToken(Punctuation.LEFTPAREN)
            .addToken(Operand.ID, "x")
            .addToken(Operator.PLUS)
            .addToken(Operand.ID, "y")
            .addToken(Punctuation.RIGHTPAREN)
            .addToken(Punctuation.EOF);
        
        assertTrue(grammar.accepts(tokens));
        
        System.out.println();
        tokens = new TokenList();
        tokens.addToken(Punctuation.LEFTPAREN)
            .addToken(Operand.ID, "x")
            .addToken(Operator.MINUS)
            .addToken(Operand.ID, "y")
            .addToken(Punctuation.RIGHTPAREN)
            .addToken(Punctuation.EOF);
        
        assertFalse(grammar.accepts(tokens));
    }

    @Test
    public void testK1Grammar() {        
        GrammarBuilder gb = new GrammarBuilder();
        gb.addProduction(NonTerminal.GOAL,
                NonTerminal.EXPRESSION)
            .addProduction(NonTerminal.EXPRESSION,
                NonTerminal.EXPRESSION,
                Operator.PLUS,
                NonTerminal.TERM)
            .addProduction(NonTerminal.EXPRESSION,
                NonTerminal.TERM)
            .addProduction(NonTerminal.TERM,
                NonTerminal.TERM,
                Operator.MULTIPLY,
                NonTerminal.FACTOR)
            .addProduction(NonTerminal.TERM,
                NonTerminal.FACTOR)
            .addProduction(NonTerminal.FACTOR,
                Operand.ID)
            .addProduction(NonTerminal.FACTOR,
                Punctuation.LEFTPAREN,
                NonTerminal.EXPRESSION,
                Punctuation.RIGHTPAREN);
        
        Grammar grammar = gb.toGrammar(1);
        
        grammar.printLRTable();

        TokenList tokens = new TokenList();
        tokens.addToken(Operand.ID, "x")
            .addToken(Operator.PLUS)
            .addToken(Operand.ID, "y")
            .addToken(Operator.MULTIPLY)
            .addToken(Operand.ID, "z")
            .addToken(Punctuation.EOF);

        assertTrue(grammar.accepts(tokens));
    }
}
