package casson;

import casson.parser.symbols.Epsilon;
import casson.parser.symbols.NonTerminal;
import casson.parser.symbols.Operand;
import casson.parser.symbols.OperandToken;
import casson.parser.symbols.Operator;
import casson.parser.symbols.Punctuation;
import casson.parser.symbols.Token;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class GrammarTest {

    public GrammarTest() {
    }

    @Test
    public void testK0Grammar() {
        Map<Integer, Grammar.Production> productions = new HashMap<>();
        productions.put(1, new Grammar.Production(
                NonTerminal.GOAL,
                NonTerminal.EXPRESSION));
        productions.put(2, new Grammar.Production(
                NonTerminal.EXPRESSION,
                NonTerminal.EXPRESSION,
                Operator.PLUS,
                NonTerminal.TERM));
        productions.put(3, new Grammar.Production(
                NonTerminal.EXPRESSION,
                NonTerminal.TERM));
        productions.put(4, new Grammar.Production(
                NonTerminal.TERM,
                Operand.ID));
        productions.put(5, new Grammar.Production(
                NonTerminal.TERM,
                Punctuation.LEFTPAREN,
                NonTerminal.EXPRESSION,
                Punctuation.RIGHTPAREN));

        Grammar grammar = new Grammar(productions, 0);

        List<Token> tokens = new ArrayList<>();
        tokens.add(new OperandToken(Operand.ID, "x"));
        tokens.add(Operator.PLUS);
        tokens.add(new OperandToken(Operand.ID, "y"));
        tokens.add(Operator.PLUS);
        tokens.add(new OperandToken(Operand.ID, "z"));
        tokens.add(Punctuation.EOF);

        assertTrue(grammar.accepts(tokens));
        
        System.out.println();
        tokens = new ArrayList<>();
        tokens.add(Punctuation.LEFTPAREN);
        tokens.add(new OperandToken(Operand.ID, "x"));
        tokens.add(Punctuation.RIGHTPAREN);
        tokens.add(Punctuation.EOF);
        
        assertTrue(grammar.accepts(tokens));
        
        System.out.println();
        tokens = new ArrayList<>();
        tokens.add(Punctuation.LEFTPAREN);
        tokens.add(new OperandToken(Operand.ID, "x"));
        tokens.add(Operator.PLUS);
        tokens.add(new OperandToken(Operand.ID, "y"));
        tokens.add(Punctuation.RIGHTPAREN);
        tokens.add(Punctuation.EOF);
        
        assertTrue(grammar.accepts(tokens));
        
        System.out.println();
        tokens = new ArrayList<>();
        tokens.add(Punctuation.LEFTPAREN);
        tokens.add(new OperandToken(Operand.ID, "x"));
        tokens.add(Operator.MINUS);
        tokens.add(new OperandToken(Operand.ID, "y"));
        tokens.add(Punctuation.RIGHTPAREN);
        tokens.add(Punctuation.EOF);
        
        assertFalse(grammar.accepts(tokens));
    }

    @Test
    public void testK1Grammar() {
        Map<Integer, Grammar.Production> productions = new HashMap<>();
        productions.put(1, new Grammar.Production(
                NonTerminal.GOAL,
                NonTerminal.EXPRESSION));
        productions.put(2, new Grammar.Production(
                NonTerminal.EXPRESSION,
                NonTerminal.EXPRESSION,
                Operator.PLUS,
                NonTerminal.TERM));
        productions.put(3, new Grammar.Production(
                NonTerminal.EXPRESSION,
                NonTerminal.TERM));
        productions.put(4, new Grammar.Production(
                NonTerminal.TERM,
                NonTerminal.TERM,
                Operator.MULTIPLY,
                NonTerminal.FACTOR));
        productions.put(5, new Grammar.Production(
                NonTerminal.TERM,
                NonTerminal.FACTOR));
        productions.put(6, new Grammar.Production(
                NonTerminal.FACTOR,
                Operand.ID));
        productions.put(7, new Grammar.Production(
                NonTerminal.FACTOR,
                Punctuation.LEFTPAREN,
                NonTerminal.EXPRESSION,
                Punctuation.RIGHTPAREN));

        Grammar grammar = new Grammar(productions, 1);
        
        grammar.printLRTable();

        List<Token> tokens = new ArrayList<>();
        tokens.add(new OperandToken(Operand.ID, "x"));
        tokens.add(Operator.PLUS);
        tokens.add(new OperandToken(Operand.ID, "y"));
        tokens.add(Operator.MULTIPLY);
        tokens.add(new OperandToken(Operand.ID, "z"));
        tokens.add(Punctuation.EOF);

        assertTrue(grammar.accepts(tokens));
    }
}
