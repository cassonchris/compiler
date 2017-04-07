package casson.regexp;

public class RegularExpression {

    private class SyntaxTree {

        private SyntaxTree parentTree;
        private SyntaxTree leftTree;
        private SyntaxTree rightTree;
        private final char content;

        private SyntaxTree(char content) {
            this.content = content;
        }

        private void setLeftTree(SyntaxTree leftTree) {
            this.leftTree = leftTree;
            leftTree.parentTree = this;
        }

        private void setRightTree(SyntaxTree rightTree) {
            this.rightTree = rightTree;
            rightTree.parentTree = this;
        }

        private void replace(SyntaxTree oldTree, SyntaxTree newTree) {
            if (leftTree == oldTree) {
                setLeftTree(newTree);
            } else if (rightTree == oldTree) {
                setRightTree(newTree);
            }
        }
    }

    private final SyntaxTree syntaxTree;

    public RegularExpression(String expression) {
        syntaxTree = generateSyntaxTree(expression);
    }

    private int getPrecedence(char a) {
        switch (a) {
            case '*':
                return 1;
            case '+':
                return 2;
            case '|':
                return 3;
            default:
                return 4;
        }
    }

    private SyntaxTree generateSyntaxTree(String expression) {
        if (expression == null || expression.isEmpty()) {
            throw new IllegalArgumentException("Regular expression cannot be empty.");
        }

        SyntaxTree previousCharacterTree = null;
        for (char character : expression.toCharArray()) {
            SyntaxTree currentTree = new SyntaxTree(character);
            if (character == '*') { // closure
                if (previousCharacterTree == null) {
                    throw new IllegalArgumentException(expression + " is not a valid regular expression.");
                }
                if (previousCharacterTree.parentTree != null) {
                    previousCharacterTree.parentTree.replace(previousCharacterTree, currentTree);
                }
                currentTree.setLeftTree(previousCharacterTree);
            } else if (character == '|') { // union
                if (previousCharacterTree == null) {
                    throw new IllegalArgumentException(expression + " is not a valid regular expression.");
                }
                while (previousCharacterTree.parentTree != null
                        && getPrecedence(previousCharacterTree.content) > getPrecedence(previousCharacterTree.parentTree.content)) {
                    previousCharacterTree = previousCharacterTree.parentTree;
                }
                if (previousCharacterTree.parentTree != null) {
                    previousCharacterTree.parentTree.replace(previousCharacterTree, currentTree);
                }
                currentTree.setLeftTree(previousCharacterTree);
            } else { // character literal
                if (previousCharacterTree != null) { // current is not the root
                    if (previousCharacterTree.content == '|') {
                        previousCharacterTree.setRightTree(currentTree);
                    } else {
                        SyntaxTree concatTree = new SyntaxTree('+');
                        if (previousCharacterTree.parentTree != null) { // previous is not the root
                            previousCharacterTree.parentTree.replace(previousCharacterTree, concatTree);
                        }

                        concatTree.setLeftTree(previousCharacterTree);
                        concatTree.setRightTree(currentTree);
                    }
                }
            }

            previousCharacterTree = currentTree;
        }

        SyntaxTree root = previousCharacterTree;
        while (root.parentTree != null) {
            root = root.parentTree;
        }
        return root;
    }

    public void printSyntaxTree() {
        printSyntaxTree(syntaxTree, 0);
    }

    private void printSyntaxTree(SyntaxTree tree, int tabsIndented) {
        if (tree.leftTree != null) {
            printSyntaxTree(tree.leftTree, tabsIndented + 1);
        }
        for (int i = 0; i < tabsIndented; i++) {
            System.out.print("\t");
        }
        System.out.println(tree.content);
        if (tree.rightTree != null) {
            printSyntaxTree(tree.rightTree, tabsIndented + 1);
        }
    }
}
