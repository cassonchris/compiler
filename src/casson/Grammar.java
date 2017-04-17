package casson;

import casson.parser.symbols.NonTerminal;
import casson.parser.symbols.Operand;
import casson.parser.symbols.Operator;
import casson.parser.symbols.Punctuation;
import casson.parser.symbols.Symbol;
import casson.parser.symbols.Terminal;
import casson.parser.symbols.Token;
import casson.parser.tables.Action;
import casson.parser.tables.ActionKey;
import casson.parser.tables.ActionValue;
import casson.parser.tables.GotoKey;
import casson.parser.tables.ItemSets;
import casson.parser.tables.LRTable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

        Production(NonTerminal head, Symbol... body) {
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
    private final LRTable table;
    private final Collection<Symbol> symbols;

    public Grammar(Map<Integer, Production> productions) {
        this.productions = productions;

        symbols = new ArrayList<>();
        symbols.addAll(Arrays.asList(NonTerminal.values()));
        symbols.addAll(Arrays.asList(Operand.values()));
        symbols.addAll(Arrays.asList(Operator.values()));
        symbols.addAll(Arrays.asList(Punctuation.values()));

        table = generateLRTable();
    }

    private Integer getProductionId(Production production) {
        for (Map.Entry<Integer, Production> productionEntry : productions.entrySet()) {
            if (productionEntry.getValue().equals(production)) {
                return productionEntry.getKey();
            }
        }
        Production modifiedProduction = new Production(production.getHead(), new ArrayList<>());
        modifiedProduction.getBody().addAll(production.getBody());
        modifiedProduction.getBody().remove(Punctuation.EOF);
        for (Map.Entry<Integer, Production> productionEntry : productions.entrySet()) {
            if (productionEntry.getValue().equals(modifiedProduction)) {
                return productionEntry.getKey();
            }
        }
        return null;
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

    private ItemSets getItems() {
        ItemSets items = new ItemSets();

        // get the first production
        Production firstProduction = productions.get(0); // for zero based indexing
        if (firstProduction == null) {
            // if the user started at 1
            firstProduction = productions.get(1);
        }

        Production dottedFirstProduction = new Production(firstProduction.getHead(), new ArrayList<>());
        dottedFirstProduction.getBody().add(Punctuation.DOT);
        dottedFirstProduction.getBody().addAll(firstProduction.getBody());
        if (!dottedFirstProduction.getBody().get(dottedFirstProduction.getBody().size() - 1).equals(Punctuation.EOF)) {
            dottedFirstProduction.getBody().add(Punctuation.EOF);
        }

        addItems(items, closure(new HashSet<>(Arrays.asList(dottedFirstProduction))));
        return items;
    }

    private void addItems(ItemSets itemSet, Set<Production> productionSet) {
        itemSet.getSetMap().put(itemSet.getSetMap().size(), productionSet);
        for (Symbol symbol : symbols) {
            Set<Production> gotoSet = getGoto(productionSet, symbol);
            if (!gotoSet.isEmpty() && !itemSet.getSetMap().values().contains(gotoSet)) {
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
                System.out.println("Accept");
                return true;
            } else if (action.getAction() == Action.SWITCH) {
                System.out.println("Shift " + action.getNumber());
                stateStack.push(action.getNumber());
                
                if (tokenIterator.hasNext()) {
                    token = tokenIterator.next();
                } else {
                    // when k = 0, the algorithm will try to use the token beyond EOF
                    token = Punctuation.EOF;
                }
            } else if (action.getAction() == Action.REDUCE) {
                Production production = productions.get(action.getNumber());
                System.out.println("Reduce " + production);
                int beta = production.getBody().size();
                for (int i = 0; i < beta; i++) {
                    stateStack.pop();
                }
                state = stateStack.peek();
                GotoKey gotoKey = new GotoKey(state, production.getHead());
                int gotoState = table.getGotoState(gotoKey);
                System.out.println("Go to " + gotoState);
                stateStack.push(gotoState);
            }
        }
    }

    private LRTable generateLRTable() {
        Map<ActionKey, ActionValue> actionMap = new HashMap<>();
        Map<GotoKey, Integer> gotoMap = new HashMap<>();

        ItemSets items = getItems();
        for (Map.Entry<Integer, Set<Production>> item : items.getSetMap().entrySet()) {
            Integer itemId = item.getKey();
            Set<Production> itemSet = item.getValue();

            // A is non terminal
            // a is terminal
            // a) [ A -> .ab ] in Ii and goto(Ii, a) = Ij
            //      action[i,a] = "shift j"
            // 3) goto(Ii, A) = Ij
            //      goto[i,A] = j
            for (Symbol s : symbols) {
                Set<Production> gotoSet = getGoto(itemSet, s);
                if (!gotoSet.isEmpty()) {
                        Integer gotoId = items.getItemSetId(gotoSet);
                    if (s instanceof Terminal) {
                        actionMap.put(new ActionKey(itemId, (Terminal) s), new ActionValue(Action.SWITCH, gotoId));
                    } else if (s instanceof NonTerminal) {
                        gotoMap.put(new GotoKey(itemId, (NonTerminal)s), gotoId);
                    }
                }
            }

            // b) [ A -> a. ] in Ii, A != S'
            //      action[i,a] = "reduce A -> alpha", for all a
            // c) [s' -> s$.] in Ii
            //      action[i,a] = "accept", for all a
            for (Production production : itemSet) {
                if (production.getBody().get(production.getBody().size() - 1).equals(Punctuation.DOT)) {
                    Production productionNoDot = new Production(production.getHead(), new ArrayList<>());
                    productionNoDot.getBody().addAll(production.getBody());
                    productionNoDot.getBody().remove(Punctuation.DOT);
                    Integer productionId = getProductionId(productionNoDot);
                    ActionValue actionValue;
                    if (productionNoDot.getBody().get(productionNoDot.getBody().size() - 1).equals(Punctuation.EOF)) {
                        actionValue = new ActionValue(Action.ACCEPT, productionId);
                    } else {
                        actionValue = new ActionValue(Action.REDUCE, productionId);
                    }
                    for (Symbol s : symbols) {
                        if (s instanceof Terminal) {
                            actionMap.put(new ActionKey(itemId, (Terminal) s), actionValue);
                        }
                    }
                }
            }
        }

        return new LRTable(actionMap, gotoMap);
    }
}
