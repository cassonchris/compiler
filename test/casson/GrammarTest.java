package casson;

import casson.parser.symbols.NonTerminal;
import casson.parser.symbols.Operand;
import casson.parser.symbols.OperandToken;
import casson.parser.symbols.Operator;
import casson.parser.symbols.Punctuation;
import casson.parser.symbols.Symbol;
import casson.parser.symbols.Token;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public class GrammarTest {

    public GrammarTest() {
    }
    
    @Test
    public void testAccepts() {
        Map<Integer, Map.Entry<NonTerminal, List<Symbol>>> productions = new HashMap<>();
        productions.put(1, new AbstractMap.SimpleEntry<>(
                NonTerminal.GOAL,
                new ArrayList<>(Arrays.asList(
                                NonTerminal.EXPRESSION))));
        productions.put(2, new AbstractMap.SimpleEntry<>(
                NonTerminal.EXPRESSION,
                new ArrayList<>(Arrays.asList(
                                NonTerminal.EXPRESSION,
                                Operator.PLUS,
                                NonTerminal.TERM))));
        productions.put(3, new AbstractMap.SimpleEntry<>(
                NonTerminal.EXPRESSION,
                new ArrayList<>(Arrays.asList(
                                NonTerminal.TERM))));
        productions.put(4, new AbstractMap.SimpleEntry<>(
                NonTerminal.TERM,
                new ArrayList<>(Arrays.asList(
                                Operand.ID))));
        productions.put(5, new AbstractMap.SimpleEntry<>(
                NonTerminal.TERM,
                new ArrayList<>(Arrays.asList(
                                Punctuation.LEFTPAREN,
                                NonTerminal.EXPRESSION,
                                Punctuation.RIGHTPAREN))));

        Grammar grammar = new Grammar(productions);

        List<Token> tokens = new ArrayList<>();
        tokens.add(new OperandToken(Operand.ID, "x"));
        tokens.add(Operator.PLUS);
        tokens.add(new OperandToken(Operand.ID, "y"));
        tokens.add(Operator.MULTIPLY);
        tokens.add(new OperandToken(Operand.ID, "z"));
        tokens.add(Punctuation.EOF);

        assertTrue(grammar.accepts(tokens));
        
        Set<Set<Map.Entry<NonTerminal, List<Symbol>>>> items = grammar.getItems();
        System.out.println(items);
    }
}
