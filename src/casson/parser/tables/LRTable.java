package casson.parser.tables;

import java.util.Map;

/**
 * This class represents a LR table. It uses maps to hold the action and goto values.
 * @author ccasson
 */
public class LRTable {
    
    private final Map<ActionKey, ActionValue> actionMap;
    private final Map<GotoKey, Integer> gotoMap;
    
    /**
     * 
     * @param actionMap a map containing the action portion of the LR table
     * @param gotoMap a map containing the goto portion of the LR table
     */
    public LRTable(Map<ActionKey, ActionValue> actionMap, Map<GotoKey, Integer> gotoMap) {
        this.actionMap = actionMap;
        this.gotoMap = gotoMap;
    }
    
    /**
     * 
     * @param actionKey
     * @return the ActionValue, or null if the given ActionKey doesn't exist
     */
    public ActionValue getAction(ActionKey actionKey) {
        if (actionMap.containsKey(actionKey)) {
            return actionMap.get(actionKey);
        } else {
            return null;
        }
    }
    
    /**
     * 
     * @param gotoKey
     * @return the goto state number, or null if the given GotoKey doesn't exist
     */
    public Integer getGotoState(GotoKey gotoKey) {
        if (gotoMap.containsKey(gotoKey)) {
            return gotoMap.get(gotoKey);
        } else {
            return null;
        }
    }
}
