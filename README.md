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

### Step 1: Create a GrammarBuilder

```java
GrammarBuilder gb = new GrammarBuilder();
```

### Step 2: Add the Productions

```java
gb.addProduction(NonTerminal.GOAL,
		NonTerminal.EXPRESSION)
	.addProduction(NonTerminal.EXPRESSION,
		NonTerminal.EXPRESSION,
		Operator.PLUS,
		NonTerminal.TERM)
	.addProduction(NonTerminal.EXPRESSION,
		NonTerminal.TERM)
	.addProduction(NonTerminal.TERM,
		Operand.ID)
	.addProduction(NonTerminal.TERM,
		Punctuation.LEFTPAREN,
		NonTerminal.EXPRESSION,
		Punctuation.RIGHTPAREN);
```

### Step 3: Get the Grammar from the GrammarBuilder

The toGrammar method accepts the lookahead value as an argument.

```java
Grammar grammar = gb.toGrammar(0);
```

### Step 4: Use the Grammar object to check input
```java
TokenList tokens = new TokenList();
tokens.addToken(Operand.ID, "x")
    .addToken(Operator.PLUS)
    .addToken(Operand.ID, "y")
    .addToken(Operator.PLUS)
    .addToken(Operand.ID, "z")
    .addToken(Punctuation.EOF);

boolean accepts = grammar.accepts(tokens);
```