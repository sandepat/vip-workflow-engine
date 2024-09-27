package fr.insalyon.creatis.moteurlite.iterationStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class DotIteration {
    public static List<Map<String, String>> dotIteration(Map<String, List<String>> inputMap) {
          List<Map<String, String>> combinations = new ArrayList<>();
    
          // Generate dot combinations
          int maxSize = inputMap.values().stream().mapToInt(List::size).min().orElse(0);
          for (int i = 0; i < maxSize; i++) {
             Map<String, String> combination = new HashMap<>();
             for (Map.Entry<String, List<String>> entry : inputMap.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
                
                if (i < values.size()) {
                      combination.put(key, values.get(i));
                }
             }
             combinations.add(combination);
          }
          return combinations;
       }
}
