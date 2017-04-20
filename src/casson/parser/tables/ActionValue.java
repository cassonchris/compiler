package casson.parser.tables;

/**
 * This class represents an action value in a LR table.
 * @author Chris Casson
 */
public class ActionValue {

    private Action action;
    private int number;

    public ActionValue(Action action, int number) {
        this.action = action;
        this.number = number;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return action + "" + number;
    }
}
