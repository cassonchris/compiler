package casson.parser.tables;

import casson.Grammar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class holds the sets of items in a map with the item set number as the key.
 * @author Chris Casson
 */
public class ItemSets {

    Map<Integer, Set<Grammar.Production>> sets;
    
    public ItemSets() {
        sets = new HashMap<>();
    }

    public Map<Integer, Set<Grammar.Production>> getSetMap() {
        return sets;
    }
    
    /**
     * 
     * @param itemSet
     * @return the Id for the given set, or null if the set doesn't exist.
     */
    public Integer getItemSetId(Set<Grammar.Production> itemSet) {
        for (Map.Entry<Integer, Set<Grammar.Production>> set : sets.entrySet()) {            
            if (itemSet.equals(set.getValue())) {
                return set.getKey();
            }
        }
        return null;
    }
}
