package casson;

import casson.parser.symbols.NonTerminal;
import casson.parser.symbols.Symbol;
import casson.parser.symbols.Token;
import casson.parser.tables.Action;
import casson.parser.tables.ActionKey;
import casson.parser.tables.ActionValue;
import casson.parser.tables.GotoKey;
import casson.parser.tables.LRTable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Grammar {

    private final Map<Integer, Map.Entry<NonTerminal, List<Symbol>>> productions;
    private LRTable table;

    public Grammar(Map<Integer, Map.Entry<NonTerminal, List<Symbol>>> productions) {
        this.productions = productions;
        
        // TODO - generate the LRTable
    }
    
    public boolean accepts(List<Token> inputTokens) {
        Stack<Integer> stateStack = new Stack<>();
        stateStack.push(0);
        
        Iterator<Token> tokenIterator = inputTokens.iterator();
        Token token = tokenIterator.next();
        
        while (true) {
            int state = stateStack.peek();
            
            ActionKey actionKey = new ActionKey(state, token.getTerminalType());
            ActionValue action = table.getAction(actionKey);
            
            if (action == null) {
                return false;
            } else if (action.getAction() == Action.ACCEPT) {
                return true;
            } else if (action.getAction() == Action.SWITCH) {
                stateStack.push(action.getNumber());
                token = tokenIterator.next();
            } else if (action.getAction() == Action.REDUCE) {
                Map.Entry<NonTerminal, List<Symbol>>  production = productions.get(action.getNumber());
                int beta = production.getValue().size();
                for (int i = 0; i < beta; i++) {
                    stateStack.pop();
                }
                state = stateStack.peek();
                GotoKey gotoKey = new GotoKey(state, production.getKey());
                stateStack.push(table.getGotoState(gotoKey));
            }
        }
    }
}
