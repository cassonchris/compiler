package casson;

import casson.parser.symbols.NonTerminal;
import casson.parser.symbols.Operand;
import casson.parser.symbols.Operator;
import casson.parser.symbols.Punctuation;
import casson.parser.symbols.Symbol;
import casson.parser.symbols.Token;
import casson.parser.tables.Action;
import casson.parser.tables.ActionKey;
import casson.parser.tables.ActionValue;
import casson.parser.tables.GotoKey;
import casson.parser.tables.LRTable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public class Grammar {
    
    public static class Production {
        
        private final NonTerminal head;
        private final List<Symbol> body;

        Production(NonTerminal head, List<Symbol> body) {
            this.head = head;
            this.body = body;
        }

        Production(NonTerminal head, Symbol ... body) {
            this.head = head;
            this.body = Arrays.asList(body);
        }

        public NonTerminal getHead() {
            return head;
        }

        public List<Symbol> getBody() {
            return body;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 47 * hash + Objects.hashCode(this.head);
            hash = 47 * hash + Objects.hashCode(this.body);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Production other = (Production) obj;
            if (this.head != other.head) {
                return false;
            }
            if (!Objects.equals(this.body, other.body)) {
                return false;
            }
            return true;
        }
        
        @Override
        public String toString() {
            return head + " -> " + body;
        }
    }

    private final Map<Integer, Production> productions;
    private LRTable table;
    private final Collection<Symbol> symbols;

    public Grammar(Map<Integer, Production> productions) {
        this.productions = productions;
        
        symbols = new ArrayList<>();
        symbols.addAll(Arrays.asList(NonTerminal.values()));
        symbols.addAll(Arrays.asList(Operand.values()));
        symbols.addAll(Arrays.asList(Operator.values()));
        symbols.addAll(Arrays.asList(Punctuation.values()));
        
        // TODO - generate the LRTable
    }
    
    private Set<Production> closure(Set<Production> iSet) {
        Set<Production> closuredProductions = new HashSet<>(iSet);
        for (Production production : iSet) {
            List<Symbol> productionValue = production.getBody();
            
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
            
            if (symbolAfterDot instanceof NonTerminal
                    && symbolAfterDot != production.getHead()) {
                Set<Production> symbolAfterDotProductions = productions.values().stream()
                                .filter(p -> p.getHead().equals(symbolAfterDot)).collect(Collectors.toSet());
                Set<Production> dottedProductions = new HashSet<>();
                for (Production originalProduction : symbolAfterDotProductions) {
                    Production newProduction = new Production(originalProduction.getHead(), new ArrayList<>());
                    newProduction.getBody().add(Punctuation.DOT);
                    newProduction.getBody().addAll(originalProduction.getBody());
                    dottedProductions.add(newProduction);
                }
                closuredProductions.addAll(
                        closure(dottedProductions)
                );
            }
        }
        return closuredProductions;
    }
    
    private Set<Production> getGoto(Set<Production> iSet, Symbol symbol) {
        Set<Production> gotoSet = new HashSet<>();
        
        for (Production iEntry : iSet) {
            List<Symbol> productionValue = iEntry.getBody();
            Symbol dot = productionValue.stream()
                    .filter(s -> s.equals(Punctuation.DOT)).findFirst().get();
            
            int dotIndex = productionValue.indexOf(dot);
            int symbolAfterDotIndex = dotIndex + 1;
            
            if (symbolAfterDotIndex > 0
                    && productionValue.size() > symbolAfterDotIndex
                    && productionValue.get(symbolAfterDotIndex).equals(symbol)) {
                
                Production gotoItem = new Production(iEntry.getHead(), new ArrayList<>());
                gotoItem.getBody().addAll(iEntry.getBody());
                
                Collections.swap(gotoItem.getBody(), dotIndex, symbolAfterDotIndex);

                gotoSet.add(gotoItem);
            }
        }
        
        return closure(gotoSet);
    }
    
    Set<Set<Production>> getItems() {
        Set<Set<Production>> items = new HashSet<>();
        
        // get the first production
        Production firstProduction = productions.get(0); // for zero based indexing
        if (firstProduction == null) {
            // if the user started at 1
            firstProduction = productions.get(1);
        }
        
        Production dottedFirstProduction = new Production(firstProduction.getHead(), new ArrayList<>());
        dottedFirstProduction.getBody().add(Punctuation.DOT);
        dottedFirstProduction.getBody().addAll(firstProduction.getBody());
        dottedFirstProduction.getBody().add(Punctuation.EOF);
        
        addItems(items, closure(new HashSet<>(Arrays.asList(dottedFirstProduction))));
        return items;
    }
    
    private void addItems(Set<Set<Production>> itemSet, Set<Production> productionSet) {
        itemSet.add(productionSet);
        for (Symbol symbol : symbols) {
            Set<Production> gotoSet = getGoto(productionSet, symbol);
            if (!gotoSet.isEmpty() && !itemSet.contains(gotoSet)) {
                addItems(itemSet, gotoSet);
            }
        }
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
                Production  production = productions.get(action.getNumber());
                int beta = production.getBody().size();
                for (int i = 0; i < beta; i++) {
                    stateStack.pop();
                }
                state = stateStack.peek();
                GotoKey gotoKey = new GotoKey(state, production.getHead());
                stateStack.push(table.getGotoState(gotoKey));
            }
        }
    }
}
