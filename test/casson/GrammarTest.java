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
    
    /**
     * S->SaSb
     *  | E
     * 
     * S is EXPRESSION
     * a is ID
     * b is NUM
     */
    @Test
    public void testGrammarA() {
        Map<Integer, Grammar.Production> productions = new HashMap<>();
        productions.put(0, new Grammar.Production(
                NonTerminal.GOAL,
                NonTerminal.EXPRESSION
        ));
        // S->SaSb
        productions.put(1, new Grammar.Production(
                NonTerminal.EXPRESSION,
                NonTerminal.EXPRESSION,
                Operand.ID,
                NonTerminal.EXPRESSION,
                Operand.NUM
        ));
        // S->E
        productions.put(2, new Grammar.Production(
                NonTerminal.EXPRESSION, 
                Epsilon.E
        ));
        
        Grammar grammar = new Grammar(productions, 1);
        
        grammar.printLRTable();
        
        List<Token> tokens = new ArrayList<>();
        tokens.add(new OperandToken(Operand.ID, "a"));
        tokens.add(new OperandToken(Operand.NUM, "b"));
        tokens.add(new OperandToken(Operand.ID, "a"));
        tokens.add(new OperandToken(Operand.ID, "a"));
        tokens.add(new OperandToken(Operand.NUM, "b"));
        tokens.add(new OperandToken(Operand.NUM, "b"));
        tokens.add(Punctuation.EOF);
        
        assertTrue(grammar.accepts(tokens));
        System.out.println();
    }
    
    /**
     * S->C
     *  | D
     * C->aC
     *  | b
     * D->aD
     *  | c
     * 
     * S is EXPRESSION
     * C is TERM
     * D is FACTOR
     * a is ID
     * b is NUM
     * c is PLUS
     */
    @Test
    public void testGrammarB() {
        Map<Integer, Grammar.Production> productions = new HashMap<>();
        productions.put(0, new Grammar.Production(
                NonTerminal.GOAL,
                NonTerminal.EXPRESSION
        ));
        // S->C
        productions.put(1, new Grammar.Production(
                NonTerminal.EXPRESSION,
                NonTerminal.TERM
        ));
        // S->D
        productions.put(2, new Grammar.Production(
                NonTerminal.EXPRESSION, 
                NonTerminal.FACTOR
        ));
        // C->aC
        productions.put(3, new Grammar.Production(
                NonTerminal.TERM,
                Operand.ID,
                NonTerminal.TERM
        ));
        // C->b
        productions.put(4, new Grammar.Production(
                NonTerminal.TERM, 
                Operand.NUM
        ));
        // D->aD
        productions.put(5, new Grammar.Production(
                NonTerminal.FACTOR,
                Operand.ID,
                NonTerminal.FACTOR
        ));
        // D->c
        productions.put(6, new Grammar.Production(
                NonTerminal.FACTOR, 
                Operator.PLUS
        ));
        
        Grammar grammar = new Grammar(productions, 2);
        
        grammar.printLRTable();
        
        List<Token> tokens = new ArrayList<>();
        tokens.add(new OperandToken(Operand.ID, "a"));
        tokens.add(new OperandToken(Operand.ID, "a"));
        tokens.add(new OperandToken(Operand.NUM, "b"));
        tokens.add(Punctuation.EOF);
        
        assertTrue(grammar.accepts(tokens));
        System.out.println();
    }
    
    /**
     * S->Ab
     *  | Bc
     * A->Aa
     *  | E
     * B->Ba
     *  | E
     * 
     * S is EXPRESSION
     * A is TERM
     * B is FACTOR
     * a is ID
     * b is NUM
     * c is PLUS
     */
    @Test
    public void testGrammarC() {
        Map<Integer, Grammar.Production> productions = new HashMap<>();
        productions.put(0, new Grammar.Production(
                NonTerminal.GOAL,
                NonTerminal.EXPRESSION
        ));
        // S->Ab
        productions.put(1, new Grammar.Production(
                NonTerminal.EXPRESSION,
                NonTerminal.TERM,
                Operand.NUM
        ));
        // S->Bc
        productions.put(2, new Grammar.Production(
                NonTerminal.EXPRESSION, 
                NonTerminal.FACTOR,
                Operator.PLUS
        ));
        // A->Aa
        productions.put(3, new Grammar.Production(
                NonTerminal.TERM,
                NonTerminal.TERM,
                Operand.ID
        ));
        // A->E
        productions.put(4, new Grammar.Production(
                NonTerminal.TERM, 
                Epsilon.E
        ));
        // B->Ba
        productions.put(5, new Grammar.Production(
                NonTerminal.FACTOR,
                NonTerminal.FACTOR,
                Operand.ID
        ));
        // B->E
        productions.put(6, new Grammar.Production(
                NonTerminal.FACTOR, 
                Epsilon.E
        ));
        
        Grammar grammar = new Grammar(productions, 2);
        
        grammar.printLRTable();
        
        List<Token> tokens = new ArrayList<>();
        tokens.add(new OperandToken(Operand.ID, "a"));
        tokens.add(new OperandToken(Operand.ID, "a"));
        tokens.add(new OperandToken(Operand.NUM, "b"));
        tokens.add(Punctuation.EOF);
        
        assertTrue(grammar.accepts(tokens));
        System.out.println();
    }
    
    /**
     * S->AB
     * A->Ba
     *  | E
     * B->Cb
     *  | C
     * C->c
     *  | E
     * 
     * S is GOAL
     * A is EXPRESSION
     * B is TERM
     * C is FACTOR
     * a is ID
     * b is NUM
     * c is PLUS
     */
    @Test
    public void testGrammarD() {
        Map<Integer, Grammar.Production> productions = new HashMap<>();
        // S->AB
        productions.put(0, new Grammar.Production(
                NonTerminal.GOAL,
                NonTerminal.EXPRESSION,
                NonTerminal.TERM
        ));
        // A->Ba
        productions.put(1, new Grammar.Production(
                NonTerminal.EXPRESSION,
                NonTerminal.TERM,
                Operand.ID
        ));
        // A->E
        productions.put(2, new Grammar.Production(
                NonTerminal.EXPRESSION,
                Epsilon.E
        ));
        // B->Cb
        productions.put(3, new Grammar.Production(
                NonTerminal.TERM, 
                NonTerminal.FACTOR,
                Operand.NUM
        ));
        // B->C
        productions.put(4, new Grammar.Production(
                NonTerminal.TERM,
                NonTerminal.FACTOR
        ));
        // C->c
        productions.put(5, new Grammar.Production(
                NonTerminal.FACTOR, 
                Operator.PLUS
        ));
        // C->E
        productions.put(6, new Grammar.Production(
                NonTerminal.FACTOR,
                Epsilon.E
        ));
        
        Grammar grammar = new Grammar(productions, 2);
        
        grammar.printLRTable();
        
        List<Token> tokens = new ArrayList<>();
        tokens.add(Operator.PLUS);
        tokens.add(new OperandToken(Operand.NUM, "b"));
        tokens.add(new OperandToken(Operand.ID, "a"));
        tokens.add(Operator.PLUS);
        tokens.add(Punctuation.EOF);
        
        assertTrue(grammar.accepts(tokens));
        System.out.println();
    }
    
    /**
     * S->AB
     * A->0A1
     *  | E
     * B->1B
     *  | 1
     * 
     * S is GOAL
     * A is EXPRESSION
     * B is TERM
     * 0 is ID
     * 1 is NUM
     */
    @Test
    public void testGrammarE() {
        Map<Integer, Grammar.Production> productions = new HashMap<>();
        // S->AB
        productions.put(0, new Grammar.Production(
                NonTerminal.GOAL,
                NonTerminal.EXPRESSION,
                NonTerminal.TERM
        ));
        // A->0A1
        productions.put(1, new Grammar.Production(
                NonTerminal.EXPRESSION,
                Operand.ID,
                NonTerminal.EXPRESSION,
                Operand.NUM
        ));
        // A->E
        productions.put(2, new Grammar.Production(
                NonTerminal.EXPRESSION,
                Epsilon.E
        ));
        // B->1B
        productions.put(3, new Grammar.Production(
                NonTerminal.TERM, 
                Operand.NUM,
                NonTerminal.TERM
        ));
        // B->1
        productions.put(4, new Grammar.Production(
                NonTerminal.TERM,
                Operand.NUM
        ));
        
        Grammar grammar = new Grammar(productions, 2);
        
        grammar.printLRTable();
        
        List<Token> tokens = new ArrayList<>();
        tokens.add(new OperandToken(Operand.NUM, "0"));
        tokens.add(new OperandToken(Operand.NUM, "1"));
        tokens.add(new OperandToken(Operand.NUM, "1"));
        tokens.add(Punctuation.EOF);
        
        assertTrue(grammar.accepts(tokens));
        System.out.println();
    }
}
