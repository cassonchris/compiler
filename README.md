# Using casson.regexp.RegularExpression

### Valid modifiers
 * () - parenthesis - used for grouping and explicit order of operations.
 * \* - closure - 0 or more occurrences of the previous character or character group.
 * | - union - the character or character group on the left OR the character or character group on the right.
 * concatenation (no explicit character) - characters or character groups next to each other are concatenated together.

### Creating a RegularExpression
```java
RegularExpression re = new RegularExpression("a|(bc)*");
```

### Checking if a String is accepted by the RegularExpression
```java
boolean accepts = re.accepts("bcbc");
```

### Printing the RegularExpression's syntax tree
```java
re.printSyntaxTree();
```

# Using casson.Grammar

### Step 1: Create the productions
Grammar.Production has two constructors available:

```java
Production(NonTerminal head, List<Symbol> body)
Production(NonTerminal head, Symbol... body)
```

The productions are put into a map where the key is the production number.

```java
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
```

### Step 2: Create the Grammar
The Grammar constructor takes in the production map and the lookahead (k) value.

```java
Grammar grammar = new Grammar(productions, 0);
```

### Step 3: Use the Grammar object to check input
```java
List<Token> tokens = new ArrayList<>();
tokens.add(new OperandToken(Operand.ID, "x"));
tokens.add(Operator.PLUS);
tokens.add(new OperandToken(Operand.ID, "y"));
tokens.add(Operator.PLUS);
tokens.add(new OperandToken(Operand.ID, "z"));
tokens.add(Punctuation.EOF);

boolean accepts = grammar.accepts(tokens);
```