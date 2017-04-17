package casson.parser.tables;

import casson.Grammar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ItemSets {

    Map<Integer, Set<Grammar.Production>> sets;
    
    public ItemSets() {
        sets = new HashMap<>();
    }

    public Map<Integer, Set<Grammar.Production>> getSetMap() {
        return sets;
    }
    
    public Integer getItemSetId(Set<Grammar.Production> itemSet) {
        for (Map.Entry<Integer, Set<Grammar.Production>> set : sets.entrySet()) {            
            if (itemSet.equals(set.getValue())) {
                return set.getKey();
            }
        }
        return null;
    }
}
