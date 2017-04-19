package casson.parser.tables;

import casson.parser.symbols.NonTerminal;
import java.util.Objects;

/**
 * This class represents the key used to look up a goto in a LR table.
 * @author Chris Casson
 */
public class GotoKey implements Comparable<GotoKey>{

    private int state;
    private NonTerminal nonTerminal;

    public GotoKey(int state, NonTerminal nonTerminal) {
        this.state = state;
        this.nonTerminal = nonTerminal;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public NonTerminal getNonTerminal() {
        return nonTerminal;
    }

    public void setNonTerminal(NonTerminal nonTerminal) {
        this.nonTerminal = nonTerminal;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + this.state;
        hash = 89 * hash + Objects.hashCode(this.nonTerminal);
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
        final GotoKey other = (GotoKey) obj;
        if (this.state != other.state) {
            return false;
        }
        if (this.nonTerminal != other.nonTerminal) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[ state = " + state + ", nonTerminal = " + nonTerminal + " ]";
    }

    @Override
    public int compareTo(GotoKey o) {
        if (this.state == o.state) {
            return this.nonTerminal.compareTo(o.nonTerminal);
        } else {
            return this.state - o.state;
        }
    }
}
