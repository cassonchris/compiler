package casson;

import casson.parser.symbols.NonTerminal;
import casson.parser.symbols.Symbol;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Chris
 */
public class GrammarBuilder {

    private int _productionCount = 0;
    private final Map<Integer, Grammar.Production> _productions;
    
    public GrammarBuilder() {
        _productions = new HashMap<>();
    }

    public GrammarBuilder addProduction(Grammar.Production production) {
        _productions.put(++_productionCount, production);
        return this;
    }
    
    public GrammarBuilder addProduction(NonTerminal head, Symbol... body) {
        _productions.put(++_productionCount, new Grammar.Production(head, body));
        return this;
    }
    
    public Grammar toGrammar(int lookahead) {
        return new Grammar(_productions, lookahead);
    }
}
