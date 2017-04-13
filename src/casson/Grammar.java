package casson;

import casson.parser.symbols.NonTerminal;
import casson.parser.symbols.Punctuation;
import casson.parser.symbols.Symbol;
import casson.parser.symbols.Token;
import casson.parser.tables.Action;
import casson.parser.tables.ActionKey;
import casson.parser.tables.ActionValue;
import casson.parser.tables.GotoKey;
import casson.parser.tables.LRTable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public class Grammar {

    private final Map<Integer, Map.Entry<NonTerminal, List<Symbol>>> productions;
    private LRTable table;

    public Grammar(Map<Integer, Map.Entry<NonTerminal, List<Symbol>>> productions) {
        this.productions = productions;
        
        // TODO - generate the LRTable
    }
    
    private Set<Map.Entry<NonTerminal, List<Symbol>>> closure(Set<Map.Entry<NonTerminal, List<Symbol>>> iSet) {
        Set<Map.Entry<NonTerminal, List<Symbol>>> closuredProductions = new HashSet<>(iSet);
        for (Map.Entry<NonTerminal, List<Symbol>> production : iSet) {
            List<Symbol> productionValue = production.getValue();
            
            Symbol dot = productionValue.stream()
                    .filter(s -> s.equals(Punctuation.DOT)).findFirst().get();
            Symbol symbolAfterDot;
            int symbolAfterDotIndex = productionValue.indexOf(dot) + 1;
            if (symbolAfterDotIndex > 0
                    && productionValue.size() > symbolAfterDotIndex) {
                symbolAfterDot = productionValue.get(symbolAfterDotIndex);
            } else {
                return closuredProductions;
            }
            
            if (symbolAfterDot instanceof NonTerminal) {
                Set<Map.Entry<NonTerminal, List<Symbol>>> symbolAfterDotProductions = productions.values().stream()
                                .filter(p -> p.getKey().equals(symbolAfterDot)).collect(Collectors.toSet());
                Set<Map.Entry<NonTerminal, List<Symbol>>> dottedProductions = new HashSet<>();
                for (Map.Entry<NonTerminal, List<Symbol>> originalProduction : symbolAfterDotProductions) {
                    Map.Entry<NonTerminal, List<Symbol>> newProduction = new AbstractMap.SimpleEntry<>(originalProduction.getKey(), new ArrayList<Symbol>());
                    newProduction.getValue().add(Punctuation.DOT);
                    newProduction.getValue().addAll(originalProduction.getValue());
                    dottedProductions.add(newProduction);
                }
                closuredProductions.addAll(
                        closure(dottedProductions)
                );
            }
        }
        return closuredProductions;
    }
    
    private Set<Map.Entry<NonTerminal, List<Symbol>>> getGoto(Set<Map.Entry<NonTerminal, List<Symbol>>> iSet, Symbol symbol) {
        Set<Map.Entry<NonTerminal, List<Symbol>>> gotoSet = new HashSet<>();
        
        for (Map.Entry<NonTerminal, List<Symbol>> iEntry : iSet) {
            List<Symbol> productionValue = iEntry.getValue();
            Symbol dot = productionValue.stream()
                    .filter(s -> s.equals(Punctuation.DOT)).findFirst().get();
            
            int dotIndex = productionValue.indexOf(dot);
            int symbolAfterDotIndex = dotIndex + 1;
            
            if (symbolAfterDotIndex > 0
                    && productionValue.size() > symbolAfterDotIndex) {
                Collections.swap(productionValue, dotIndex, symbolAfterDotIndex);

                gotoSet.add(iEntry);
            }
        }
        
        return closure(gotoSet);
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
