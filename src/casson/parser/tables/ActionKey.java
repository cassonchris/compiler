package casson.parser.tables;

import casson.parser.symbols.Terminal;
import java.util.Objects;

/**
 * This class represents the key used to look up an action in a LR table.
 * @author Chris Casson
 */
public class ActionKey {

    private int state;
    private Terminal terminal;
    
    public ActionKey(int state, Terminal terminal) {
        this.state = state;
        this.terminal = terminal;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.state;
        hash = 67 * hash + Objects.hashCode(this.terminal);
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
        final ActionKey other = (ActionKey) obj;
        if (this.state != other.state) {
            return false;
        }
        if (!Objects.equals(this.terminal, other.terminal)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[ state = " + state + ", terminal = " + terminal + " ]";
    }
}
