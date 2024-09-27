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

public class CrossIteration {

       public static List<Map<String, String>> crossIteration(Map<String, List<String>> inputMap) {
          List<Map<String, String>> combinations = new ArrayList<>();
    
          // Generate cross combinations
          for (Map.Entry<String, List<String>> entry : inputMap.entrySet()) {
             String key = entry.getKey();
             List<String> values = entry.getValue();
             if (combinations.isEmpty()) {
                for (String value : values) {
                      Map<String, String> combination = new HashMap<>();
                      combination.put(key, value);
                      combinations.add(combination);
                }
             } else {
                List<Map<String, String>> temp = new ArrayList<>(combinations);
                combinations.clear();
                for (Map<String, String> combination : temp) {
                      for (String value : values) {
                         Map<String, String> newCombination = new HashMap<>(combination);
                         newCombination.put(key, value);
                         combinations.add(newCombination);
                      }
                }
             }
          }
          return combinations;
       }
}


