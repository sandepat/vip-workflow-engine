package fr.insalyon.creatis.moteurlite.iterationStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class JsonIteration {

    public static List<Map<String, String>> jsonIteration(Map<String, List<String>> inputMap, Set<String> crossKeys, Set<String> dotKeys, Set<String> inputOptional) {
        
        // Check if the keys present in dot and cross are one of the keys in inputMap
        Set<String> allKeys = new HashSet<>(inputMap.keySet());
        allKeys.removeAll(dotKeys);
        allKeys.removeAll(crossKeys);

        // Remove keys from dotKeys or crossKeys if they are not present in inputMap
        dotKeys.retainAll(inputMap.keySet());
        crossKeys.retainAll(inputMap.keySet());

        // Add keys from inputMap that are not present in dotKeys to crossKeys
        crossKeys.addAll(allKeys);
        crossKeys.addAll(inputOptional);
    
        // Proceed with the rest of the logic
        List<Map<String, String>> crossCombinations = CrossIteration.crossIteration(getSelectedMap(inputMap, crossKeys));
        List<Map<String, String>> dotCombinations = DotIteration.dotIteration(getSelectedMap(inputMap, dotKeys));

        // Combine cross and dot combinations
        List<Map<String, String>> combinedCombinations = new ArrayList<>();
        if (dotCombinations.isEmpty()) {
            combinedCombinations.addAll(crossCombinations);
        } else if (crossCombinations.isEmpty()) {
            combinedCombinations.addAll(dotCombinations);
        } else {
            // Both dotCombinations and crossCombinations are non-empty
            for (Map<String, String> crossCombination : crossCombinations) {
                for (Map<String, String> dotCombination : dotCombinations) {
                    // Create a new map for the combined combination
                    Map<String, String> combinedCombination = new HashMap<>(crossCombination);
                    
                    // Add missing keys from dotCombination
                    for (String key : dotCombination.keySet()) {
                        if (!combinedCombination.containsKey(key)) {
                            combinedCombination.put(key, dotCombination.get(key));
                        }
                    }
                    combinedCombinations.add(combinedCombination); // Add combined combination to the list
                }
            }
        }
        return combinedCombinations; // Return the combined combinations
    }

    private static Map<String, List<String>> getSelectedMap(Map<String, List<String>> inputMap, Set<String> keys) {
        Map<String, List<String>> selectedMap = new HashMap<>();
        for (String key : keys) {
            if (inputMap.containsKey(key)) {
                selectedMap.put(key, inputMap.get(key));
            }
        }
        return selectedMap;
    }
}
