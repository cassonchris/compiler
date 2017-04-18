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

/**
 * This class represents a grammar.
 * @author Chris Casson
 */
public class Grammar {

    /**
     * This class represents a grammar production.
     */
    public static class Production {

        // the left side of the production
        private final NonTerminal head;
        
        // the right side of the production
        private final List<Symbol> body;

        /**
         * 
         * @param head the left side of the production
         * @param body the right side of the production
         */
        Production(NonTerminal head, List<Symbol> body) {
            this.head = head;
            this.body = body;
        }

        /**
         * 
         * @param head the left side of the production
         * @param body the right side of the production
         */
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

        //<editor-fold defaultstate="collapsed" desc="hashCode and equals">
        
        /**
         * Netbeans generated hashCode
         * @return 
         */
        @Override
        public int hashCode() {
            int hash = 5;
            hash = 47 * hash + Objects.hashCode(this.head);
            hash = 47 * hash + Objects.hashCode(this.body);
            return hash;
        }
        
        /**
         * Netbeans generated hashCode
         * @param obj
         * @return 
         */
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
        //</editor-fold>

        @Override
        public String toString() {
            return head + " -> " + body;
        }
    }

    // map of productions that make up the grammar
    private final Map<Integer, Production> productions;
    
    // the LR(k) table that contains action and goto
    private final LRTable table;
    
    // set of symbols included in the grammar
    private final Set<Symbol> symbols;

    /**
     * 
     * @param productions the productions that make up the grammar
     * @param lookahead the number of tokens to lookahead when parsing
     */
    public Grammar(Map<Integer, Production> productions, int lookahead) {
        this.productions = productions;

        symbols = new HashSet<>();
        
        // add all the symbols from the productions given
        for (Production production : productions.values()) {
            symbols.add(production.getHead());
            symbols.addAll(production.getBody());
        }
        
        // add EOF ($), just in case it wasn't specified in the given productions
        symbols.add(Punctuation.EOF);

        // generate the LR(lookahead) table
        table = generateLRTable(lookahead);
    }

    /**
     * This method searches the productions map to find the id of the given production.
     * @param production
     * @return the id of the production, or null if the production doesn't exist in the productions map.
     */
    private Integer getProductionId(Production production) {
        // for each production entry in the map
        for (Map.Entry<Integer, Production> productionEntry : productions.entrySet()) {
            // if the Production matches what was passed in, return the key (production id)
            if (productionEntry.getValue().equals(production)) {
                return productionEntry.getKey();
            }
        }
        
        // try removing EOF and searching again
        // the passed in production might have had EOF added as part of the item generation process
        if (production.getBody().contains(Punctuation.EOF)) {
            // create a modified production with EOF removed
            Production modifiedProduction = new Production(production.getHead(), new ArrayList<>());
            modifiedProduction.getBody().addAll(production.getBody());
            modifiedProduction.getBody().remove(Punctuation.EOF);
            
            // call getProductionId with the modified production
            return getProductionId(modifiedProduction);
        }
        
        // production wasn't found, return null
        return null;
    }

    /**
     * For a production that has a non-terminal directly to the right of the DOT,
     * closure adds to the set productions where the non-terminal is the head (left side) of the production.
     * 
     * @param itemSet a set of dotted productions
     * @return the result of closure on itemSet
     */
    private Set<Production> closure(Set<Production> itemSet) {
        // create a copy of the given iSet
        Set<Production> closuredProductions = new HashSet<>(itemSet);
        
        // for each production in iSet
        for (Production production : itemSet) {
            List<Symbol> productionBody = production.getBody();

            // find the dot
            Symbol dot = productionBody.stream()
                    .filter(s -> s.equals(Punctuation.DOT)).findFirst().get();
            Symbol symbolAfterDot;
            
            // get the index of the symbol after the dot
            int symbolAfterDotIndex = productionBody.indexOf(dot) + 1;
            
            // if the index is valid ...
            if (symbolAfterDotIndex > 0
                    && productionBody.size() > symbolAfterDotIndex) {
                // ... assign that symbol to symbolAfterDot
                symbolAfterDot = productionBody.get(symbolAfterDotIndex);
            } else {
                // the closured set is the original iSet
                return closuredProductions;
            }

            // symbolAfterDot != production.getHead() is to prevent infinite recursion
            if (symbolAfterDot instanceof NonTerminal
                    && symbolAfterDot != production.getHead()) {
                
                // get all the productions where symbolAfterDot is the head
                Set<Production> symbolAfterDotProductions = productions.values().stream()
                        .filter(p -> p.getHead().equals(symbolAfterDot)).collect(Collectors.toSet());
                                
                Set<Production> dottedProductions = new HashSet<>();
                
                // for each production in symbolAfterDotProductions
                for (Production originalProduction : symbolAfterDotProductions) {
                    // create a new production with a DOT as the first symbol of the body
                    Production newProduction = new Production(originalProduction.getHead(), new ArrayList<>());
                    newProduction.getBody().add(Punctuation.DOT);
                    newProduction.getBody().addAll(originalProduction.getBody());
                    
                    // add the production to the dottedProductions set
                    dottedProductions.add(newProduction);
                }
                
                // closure the dottedProductions set and add the result to closuredProductions
                closuredProductions.addAll(
                        closure(dottedProductions)
                );
            }
        }
        
        return closuredProductions;
    }

    /**
     * This method builds a goto set based on the given itemSet and symbol.
     * If the symbol to the right of the dot matches the given symbol,
     * swap the dot with the symbol to the right and add the new production to the goto set.
     * Closure the goto set and return it.
     * 
     * @param itemSet
     * @param symbol
     * @return the goto set
     */
    private Set<Production> getGoto(Set<Production> itemSet, Symbol symbol) {
        // create a set to hold the new items
        Set<Production> gotoSet = new HashSet<>();

        // for each production in the itemSet
        for (Production iEntry : itemSet) {
            // find the dot
            List<Symbol> productionBody = iEntry.getBody();
            Symbol dot = productionBody.stream()
                    .filter(s -> s.equals(Punctuation.DOT)).findFirst().get();

            int dotIndex = productionBody.indexOf(dot);
            int symbolAfterDotIndex = dotIndex + 1;

            // if the symbolAfterDotIndex is valid
            // and the symbol after the dot matches the given symbol
            if (symbolAfterDotIndex > 0
                    && productionBody.size() > symbolAfterDotIndex
                    && productionBody.get(symbolAfterDotIndex).equals(symbol)) {

                // create a goto item based on the current production
                Production gotoItem = new Production(iEntry.getHead(), new ArrayList<>());
                gotoItem.getBody().addAll(iEntry.getBody());

                // swap the dot and the symbol immediately following
                Collections.swap(gotoItem.getBody(), dotIndex, symbolAfterDotIndex);

                // add the new item to the set
                gotoSet.add(gotoItem);
            }
        }

        // closure the goto set and return
        return closure(gotoSet);
    }

    /**
     * This method builds the item sets based on the grammar productions.
     * @return the item sets
     */
    private ItemSets getItems() {
        ItemSets items = new ItemSets();

        // get the first production
        Production firstProduction = productions.get(0); // for zero based indexing
        if (firstProduction == null) {
            // if the user started at 1
            firstProduction = productions.get(1);
        }

        // create a copy of the first production with a dot as the first symbol
        Production dottedFirstProduction = new Production(firstProduction.getHead(), new ArrayList<>());
        dottedFirstProduction.getBody().add(Punctuation.DOT);
        dottedFirstProduction.getBody().addAll(firstProduction.getBody());
        
        // add an EOF symbol if it doesn't exist
        if (!dottedFirstProduction.getBody().get(dottedFirstProduction.getBody().size() - 1).equals(Punctuation.EOF)) {
            dottedFirstProduction.getBody().add(Punctuation.EOF);
        }

        // closure the dotted first production and add to items
        // addItems will recursively call itself until all item sets have been added
        addItems(items, closure(new HashSet<>(Arrays.asList(dottedFirstProduction))));
        
        return items;
    }

    /**
     * Adds productionSet to itemSet, then generates the goto set for each symbol and adds it recursively.
     * @param itemSet the object holding all item sets
     * @param productionSet the set of productions to add
     */
    private void addItems(ItemSets itemSet, Set<Production> productionSet) {
        // add productionSet to the item set map
        itemSet.getSetMap().put(itemSet.getSetMap().size(), productionSet);
        
        // for each symbol
        for (Symbol symbol : symbols) {
            // get the goto set
            Set<Production> gotoSet = getGoto(productionSet, symbol);
            
            // if the goto set isn't empty and hasn't already been added ...
            if (!gotoSet.isEmpty() && !itemSet.getSetMap().values().contains(gotoSet)) {
                // ... add it to itemSet
                addItems(itemSet, gotoSet);
            }
        }
    }

    /**
     * TODO - add lookup table and fix infinite recursion issues
     * Builds the set of terminal symbols that are valid in the initial position.
     * 
     * @param symbol the head (left side) of the production
     * @param lookahead the number of tokens to lookahead
     * @return the set of terminals that begin strings derived from symbol
     */
    Set<Terminal> first(Symbol symbol, int lookahead) {
        // if the symbol is a terminal, the first set is itself
        if (symbol instanceof Terminal) {
            return new HashSet<>(Arrays.asList((Terminal) symbol));
        }

        // create a set for the first terminals
        Set<Terminal> firstSet = new HashSet<>();

        // get the productions where the given symbol is the head (left side) of the production
        Set<Production> symbolProductions = productions.values().stream()
                .filter(p -> p.getHead().equals(symbol)).collect(Collectors.toSet());

        // for each production
        for (Production symbolProduction : symbolProductions) {
            // if the body (right side) of the production is epsilon ...
            if (symbolProduction.getBody().size() == 1 && symbolProduction.getBody().get(0).equals(Epsilon.E)) {
                // ... add epsilon to firstSet
                firstSet.add(Epsilon.E);
            } else {
                // get the first symbol from the body of the production
                Symbol y1Symbol = symbolProduction.getBody().get(0);
                // make sure it doesn't match the existing symbol, to prevent infinite recursion
                if (!y1Symbol.equals(symbol)) {
                    // get the first set for the first symbol
                    Set<Terminal> y1Set = first(y1Symbol, lookahead);
                    // remove epsilon
                    y1Set.remove(Epsilon.E);
                    // add the first set for the first symbol
                    firstSet.addAll(y1Set);
                }

                // continue getting the first set for the rest of the symbols
                // until lookahead is reached or all symbols have been processed
                for (int i = 1; i <= lookahead && i < symbolProduction.getBody().size(); i++) {
                    // get the symbol
                    Symbol yiSymbol = symbolProduction.getBody().get(i);
                    // make sure it doesn't match the existing symbol, to prevent infinite recursion
                    if (!yiSymbol.equals(symbol)) {
                        // get the first set for the symbol
                        Set<Terminal> yiSet = first(yiSymbol, lookahead);
                        // if yiSet contains epsilon ...
                        if (yiSet.contains(Epsilon.E)) {
                            // ... remove epsilon and add it to firstSet
                            yiSet.remove(Epsilon.E);
                            firstSet.addAll(yiSet);
                        } else {
                            // stop processing symbols
                            break;
                        }
                    }
                }

                // if first(s) contains epsilon for all s in symbolProduction.body ...
                if (symbolProduction.getBody().stream()
                        .filter(s -> !s.equals(symbol))
                        .allMatch(s -> first(s, lookahead).contains(Epsilon.E))) {
                    // ... add epsilon to firstSet
                    firstSet.add(Epsilon.E);
                }
            }
        }

        // return the firstSet
        return firstSet;
    }

    /**
     * TODO - add lookup table and fix infinite recursion issues
     * Builds the set of terminals that can immediately follow the given symbol.
     * 
     * @param symbol can be any production symbol
     * @param lookahead the number of tokens to lookahead
     * @return the set of terminals that can immediately follow symbol
     */
    Set<Terminal> follow(Symbol symbol, int lookahead) {
        // create a set to store the terminals
        Set<Terminal> followSet = new HashSet<>();

        // EOF ($) always gets added to goal's follow set
        if (symbol.equals(NonTerminal.GOAL)) {
            followSet.add(Punctuation.EOF);
        }

        // get the productions that contain the given symbol in the body (right side)
        Collection<Production> symbolProductions = productions.values().stream()
                .filter(p -> p.getBody().contains(symbol)).collect(Collectors.toList());

        // for each production
        for (Production symbolProduction : symbolProductions) {
            // create an iterator for looping through the symbols in the body
            ListIterator<Symbol> currentSymbolIterator = symbolProduction.getBody().listIterator();

            // while there are more symbols to process
            while (currentSymbolIterator.hasNext()) {
                // get the next symbol
                Symbol currentSymbol = currentSymbolIterator.next();
                
                // if it matches the given symbol,
                // process the symbols that follow
                if (currentSymbol.equals(symbol)) {
                    
                    // create a second iterator to loop through the rest of the symbols
                    ListIterator<Symbol> nextSymbolIterator = symbolProduction.getBody().listIterator(currentSymbolIterator.nextIndex());

                    // while there are more symbols to process
                    while (nextSymbolIterator.hasNext()) {
                        // get the next symbol and its firstSet
                        Symbol nextSymbol = nextSymbolIterator.next();
                        Set<Terminal> firstSet = first(nextSymbol, lookahead);

                        // add everything from firstSet to followSet except for epsilon
                        followSet.addAll(firstSet.stream().filter(t -> !t.equals(Epsilon.E)).collect(Collectors.toSet()));

                        // if firstSet doesn't contain epsilon ...
                        if (!firstSet.contains(Epsilon.E)) {
                            // ... we're done processing this symbol
                            break;
                        }
                    }
                    
                    // if follow should continue to the head (left side) of the production ...
                    if (!nextSymbolIterator.hasNext()
                            && !symbolProduction.getHead().equals(symbol)
                            && (!currentSymbolIterator.hasNext()
                                || first(nextSymbolIterator.previous(), lookahead).contains(Epsilon.E))) {
                        // ... add the follow set for the head symbol
                        followSet.addAll(follow(symbolProduction.getHead(), lookahead));
                    }
                }
            }
        }

        // return the follow set
        return followSet;
    }

    /**
     * Tests whether the input tokens are accepted by the grammar
     * 
     * @param inputTokens list of tokens forming an input string
     * @return true if accepted, otherwise false
     */
    public boolean accepts(List<Token> inputTokens) {
        // create the state stack and push 0
        Stack<Integer> stateStack = new Stack<>();
        stateStack.push(0);

        // create a token iterator and get the first token
        Iterator<Token> tokenIterator = inputTokens.iterator();
        Token token = tokenIterator.next();

        // loop until the grammar accepts or an action isn't found
        while (true) {
            // peek the top of the state stack
            int state = stateStack.peek();

            // use the state and token to create an action key
            ActionKey actionKey = new ActionKey(state, token.getTerminalType());
            // use the action key to get the action value from the LR table
            ActionValue action = table.getAction(actionKey);

            // print the action key and action
            System.out.print(actionKey);
            System.out.print(" -> ");
            System.out.print(action);

            if (action == null) {
                // the input is not accepted
                System.out.println();
                return false;
            } else if (action.getAction() == Action.ACCEPT) {
                // the input is accepted
                System.out.println();
                return true;
            } else if (action.getAction() == Action.SHIFT) {
                // push the action value number onto the state stack
                stateStack.push(action.getNumber());
                // get the next token
                token = tokenIterator.next();
            } else if (action.getAction() == Action.REDUCE) {
                // get the reduce production
                Production production = productions.get(action.getNumber());
                
                // get the number of body symbols and pop that many states
                int beta = production.getBody().size();
                for (int i = 0; i < beta; i++) {
                    stateStack.pop();
                }
                
                // peek the top of the state stack
                state = stateStack.peek();

                // use the state and production head to create a goto key
                GotoKey gotoKey = new GotoKey(state, production.getHead());
                // use the goto key to lookup the goto state in the LR table
                int gotoState = table.getGotoState(gotoKey);
                // push the goto state onto the stack
                stateStack.push(gotoState);
                
                // print the reduce production
                System.out.print(" (");
                System.out.print(production);
                System.out.print(")");
                System.out.println();
                
                // print the goto state
                System.out.print(gotoKey);
                System.out.print(" -> ");
                System.out.print("goto " + gotoState);
            }

            // println so each step is on its own line
            System.out.println();
        }
    }

    /**
     * Generates the LR(lookahead) parsing table.
     * 
     * @param lookahead the number of tokens to lookahead
     * @return an LRTable containing an action map and a goto map
     */
    private LRTable generateLRTable(int lookahead) {
        // create an action map and a goto map
        Map<ActionKey, ActionValue> actionMap = new HashMap<>();
        Map<GotoKey, Integer> gotoMap = new HashMap<>();

        // get the items for the grammar
        ItemSets items = getItems();
        
        // for each set of items
        for (Map.Entry<Integer, Set<Production>> item : items.getSetMap().entrySet()) {
            // get the item set id and set of productions
            Integer itemId = item.getKey();
            Set<Production> itemSet = item.getValue();
            
            // for each symbol in the grammar
            for (Symbol s : symbols) {
                // get the goto set
                Set<Production> gotoSet = getGoto(itemSet, s);
                
                if (!gotoSet.isEmpty()) {
                    // get the id for the goto set
                    Integer gotoId = items.getItemSetId(gotoSet);
                    
                    // if the current symbol (s) is a terminal ...
                    if (s instanceof Terminal) {
                        // ... add an action key [itemId,s] and action value [SHIFT,gotoId] to the action map
                        actionMap.put(new ActionKey(itemId, (Terminal) s), new ActionValue(Action.SHIFT, gotoId));
                    // else if the current symbol (s) is a non terminal ...
                    } else if (s instanceof NonTerminal) {
                        // ... add a goto key [itemId,s] and gotoId to gotoMap
                        gotoMap.put(new GotoKey(itemId, (NonTerminal) s), gotoId);
                    }
                }
            }
            
            // for each production in the item set
            for (Production production : itemSet) {
                // if the production body ends with DOT (but not EOF DOT) ...
                if (production.getBody().get(production.getBody().size() - 1).equals(Punctuation.DOT)
                        && !production.getBody().get(production.getBody().size() - 2).equals(Punctuation.EOF)) {
                    
                    // ... create a copy of the production without the dot
                    Production productionNoDot = new Production(production.getHead(), new ArrayList<>());
                    productionNoDot.getBody().addAll(production.getBody());
                    productionNoDot.getBody().remove(Punctuation.DOT);
                    
                    // get the production id
                    Integer productionId = getProductionId(productionNoDot);
                    
                    // create and action value REDUCE<productionId>
                    ActionValue actionValue = new ActionValue(Action.REDUCE, productionId);
                    
                    // for all terminals in follow(production.head)
                    for (Terminal t : follow(production.getHead(), lookahead)) {
                        // add an action key [itemId,t] and actionValue to actionMap
                        actionMap.put(new ActionKey(itemId, t), actionValue);
                    }
                // else if production body ends with DOT EOF
                } else if (production.getBody().get(production.getBody().size() - 2).equals(Punctuation.DOT)
                        && production.getBody().get(production.getBody().size() - 1).equals(Punctuation.EOF)) {
                    
                    // create a copy of the production without the dot
                    Production productionNoDot = new Production(production.getHead(), new ArrayList<>());
                    productionNoDot.getBody().addAll(production.getBody());
                    productionNoDot.getBody().remove(Punctuation.DOT);
                    
                    // get the production id
                    Integer productionId = getProductionId(productionNoDot);
                    
                    // add an action key [itemId,$] and action value [ACCEPT,productionId] to actionMap
                    actionMap.put(new ActionKey(itemId, Punctuation.EOF), new ActionValue(Action.ACCEPT, productionId));
                }
            }
        }

        // create an LRTable with the action map and goto map and return
        return new LRTable(actionMap, gotoMap);
    }
}