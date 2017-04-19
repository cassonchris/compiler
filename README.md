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