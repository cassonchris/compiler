package casson.parser.tables;

import java.util.Map;

public class LRTable {
    
    private final Map<ActionKey, ActionValue> actionMap;
    private final Map<GotoKey, Integer> gotoMap;
    
    public LRTable(Map<ActionKey, ActionValue> actionMap, Map<GotoKey, Integer> gotoMap) {
        this.actionMap = actionMap;
        this.gotoMap = gotoMap;
    }
    
    public ActionValue getAction(ActionKey actionKey) {
        if (actionMap.containsKey(actionKey)) {
            return actionMap.get(actionKey);
        } else {
            return null;
        }
    }
    
    public Integer getGotoState(GotoKey gotoKey) {
        if (gotoMap.containsKey(gotoKey)) {
            return gotoMap.get(gotoKey);
        } else {
            return null;
        }
    }
}
