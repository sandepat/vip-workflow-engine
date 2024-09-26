package fr.insalyon.creatis.moteurlite.iterationStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * Author: Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class IterationStrategy {
    private List<Map<String, String>> crossCombinations = new ArrayList<>();
    private List<Map<String, String>> dotCombinations = new ArrayList<>();
    private List<Map<String, String>> jsonCombinations = new ArrayList<>();

    public void IterationStratergy(List<Map<String, String>> inputsMap, Map<String, String> resultDir, Set<String> crossJson, Set<String> dotJson, Set<String> inputOptional) {
        crossCombinations = setCrossIteration(inputsMap, resultDir);
        dotCombinations = setDotIteration(inputsMap, resultDir);
        jsonCombinations = setJsonIteration(inputsMap, resultDir, crossJson, dotJson, inputOptional);
    }

    private List<Map<String, String>> setCrossIteration(List<Map<String, String>> inputsMap, Map<String, String> resultDir) {
        Map<String, List<String>> valuesMap = new HashMap<>();
        for (Map<String, String> map : inputsMap) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                valuesMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }
        }
        List<Map<String, String>> crossCombinations = CrossIteration.crossIteration(valuesMap);
        addResultsDir(crossCombinations, resultDir);
        return crossCombinations;
    }

    private List<Map<String, String>> setDotIteration(List<Map<String, String>> inputsMap, Map<String, String> resultDir) {
        Map<String, List<String>> valuesMap = new HashMap<>();
        for (Map<String, String> map : inputsMap) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                valuesMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }
        }
        List<Map<String, String>> dotCombinations = DotIteration.dotIteration(valuesMap);
        addResultsDir(dotCombinations, resultDir);
        return dotCombinations;
    }

    private List<Map<String, String>> setJsonIteration(List<Map<String, String>> inputsMap, Map<String, String> resultDir, Set<String> crossJson, Set<String> dotJson, Set<String> inputOptional) {
        Map<String, List<String>> valuesMap = new HashMap<>();
        for (Map<String, String> map : inputsMap) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                valuesMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }
        }
        List<Map<String, String>> jsonCombinations = JsonIteration.jsonIteration(valuesMap, crossJson, dotJson, inputOptional);
        //System.out.println("json" + jsonCombinations);
        addResultsDir(jsonCombinations, resultDir);
        return jsonCombinations;
    }
    private static void addResultsDir(List<Map<String, String>> combinations, Map<String, String> resultsDirectory) {
        for (Map<String, String> map : combinations) {
           map.putAll(resultsDirectory);
        }
     }

    public List<Map<String, String>> getJsonIterations() {
        return jsonCombinations;
    }

    public List<Map<String, String>> getCrossCombinations() {
        return crossCombinations;
    }

    public List<Map<String, String>> getDotCombinations() {
        return dotCombinations;
    }
}
