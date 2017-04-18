package casson;

import casson.parser.symbols.Epsilon;
import casson.parser.symbols.NonTerminal;
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
import java.util.ListIterator;
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
    private final Set<Symbol> symbols;

    public Grammar(Map<Integer, Production> productions) {
        this.productions = productions;

        symbols = new HashSet<>();
        for (Production production : productions.values()) {
            symbols.add(production.getHead());
            symbols.addAll(production.getBody());
        }
        symbols.add(Punctuation.EOF);

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

    Set<Terminal> first(Symbol symbol, int k) {
        if (symbol instanceof Terminal) {
            return new HashSet<>(Arrays.asList((Terminal) symbol));
        }

        Set<Terminal> firstSet = new HashSet<>();

        Set<Production> symbolProductions = productions.values().stream()
                .filter(p -> p.getHead().equals(symbol)).collect(Collectors.toSet());

        for (Production symbolProduction : symbolProductions) {
            if (symbolProduction.getBody().size() == 1 && symbolProduction.getBody().get(0).equals(Epsilon.E)) {
                firstSet.add(Epsilon.E);
            } else {
                Symbol y1Symbol = symbolProduction.getBody().get(0);
                if (!y1Symbol.equals(symbol)) {
                    Set<Terminal> y1Set = first(y1Symbol, k);
                    y1Set.remove(Epsilon.E);
                    firstSet.addAll(y1Set);
                }

                for (int i = 1; i <= k && i < symbolProduction.getBody().size(); i++) {
                    Symbol yiSymbol = symbolProduction.getBody().get(i);
                    if (!yiSymbol.equals(symbol)) {
                        Set<Terminal> yiSet = first(yiSymbol, k);
                        if (yiSet.contains(Epsilon.E)) {
                            yiSet.remove(Epsilon.E);
                            firstSet.addAll(yiSet);
                        } else {
                            break;
                        }
                    }
                }

                if (symbolProduction.getBody().stream()
                        .filter(s -> !s.equals(symbol))
                        .allMatch(s -> first(s, k).contains(Epsilon.E))) {
                    firstSet.add(Epsilon.E);
                }
            }
        }

        return firstSet;
    }

    Set<Terminal> follow(Symbol symbol, int k) {
        Set<Terminal> followSet = new HashSet<>();

        if (symbol.equals(NonTerminal.GOAL)) {
            followSet.add(Punctuation.EOF);
        }

        Collection<Production> symbolProductions = productions.values().stream()
                .filter(p -> p.getBody().contains(symbol)).collect(Collectors.toList());

        for (Production symbolProduction : symbolProductions) {
            ListIterator<Symbol> currentSymbolIterator = symbolProduction.getBody().listIterator();

            while (currentSymbolIterator.hasNext()) {
                Symbol currentSymbol = currentSymbolIterator.next();
                if (currentSymbol.equals(symbol)) {
                    ListIterator<Symbol> nextSymbolIterator = symbolProduction.getBody().listIterator(currentSymbolIterator.nextIndex());

                    while (nextSymbolIterator.hasNext()) {
                        Symbol nextSymbol = nextSymbolIterator.next();
                        Set<Terminal> firstSet = first(nextSymbol, k);

                        followSet.addAll(firstSet.stream().filter(t -> !t.equals(Epsilon.E)).collect(Collectors.toSet()));

                        if (!firstSet.contains(Epsilon.E)) {
                            break;
                        }
                    }

                    if (!nextSymbolIterator.hasNext()
                            && !symbolProduction.getHead().equals(symbol)
                            && (!currentSymbolIterator.hasNext()
                            || first(nextSymbolIterator.previous(), k).contains(Epsilon.E))) {
                        followSet.addAll(follow(symbolProduction.getHead(), k));
                    }
                }
            }
        }

        return followSet;
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

            System.out.print(actionKey);
            System.out.print(" -> ");
            System.out.print(action);

            if (action == null) {
                System.out.println();
                return false;
            } else if (action.getAction() == Action.ACCEPT) {
                System.out.println();
                return true;
            } else if (action.getAction() == Action.SHIFT) {
                stateStack.push(action.getNumber());
                token = tokenIterator.next();
            } else if (action.getAction() == Action.REDUCE) {
                Production production = productions.get(action.getNumber());
                int beta = production.getBody().size();
                for (int i = 0; i < beta; i++) {
                    stateStack.pop();
                }
                state = stateStack.peek();

                GotoKey gotoKey = new GotoKey(state, production.getHead());
                int gotoState = table.getGotoState(gotoKey);
                stateStack.push(gotoState);

                System.out.print(" (");
                System.out.print(production);
                System.out.print(")");
                System.out.println();
                System.out.print(gotoKey);
                System.out.print(" -> ");
                System.out.print("goto " + gotoState);
            }

            System.out.println();
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
                        actionMap.put(new ActionKey(itemId, (Terminal) s), new ActionValue(Action.SHIFT, gotoId));
                    } else if (s instanceof NonTerminal) {
                        gotoMap.put(new GotoKey(itemId, (NonTerminal) s), gotoId);
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
                } else if (production.getBody().get(production.getBody().size() - 2).equals(Punctuation.DOT)
                        && production.getBody().get(production.getBody().size() - 1).equals(Punctuation.EOF)) {
                    Production productionNoDot = new Production(production.getHead(), new ArrayList<>());
                    productionNoDot.getBody().addAll(production.getBody());
                    productionNoDot.getBody().remove(Punctuation.DOT);
                    Integer productionId = getProductionId(productionNoDot);
                    actionMap.put(new ActionKey(itemId, Punctuation.EOF), new ActionValue(Action.ACCEPT, productionId));
                }
            }
        }

        return new LRTable(actionMap, gotoMap);
    }
}
