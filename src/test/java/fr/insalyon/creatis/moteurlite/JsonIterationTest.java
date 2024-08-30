package fr.insalyon.creatis.moteurlite;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import fr.insalyon.creatis.moteurlite.iterationStrategy.JsonIteration;

public class JsonIterationTest {

    static List<Map<String, String>> combinedCombinations;
    static Map<String, List<String>> inputMap = new HashMap<>();
    static Set<String> crossKeys = new HashSet<>();
    static Set<String> dotKeys = new HashSet<>();
    static Set<String> inputOptional = new HashSet<>();

    @Test
    public void testJsonIteration() {
        // Populate input map
        inputMap.put("sleepTime", Arrays.asList("0", "1"));
        inputMap.put("file", Arrays.asList("grep.json", "grep.XML"));
        inputMap.put("flag", Arrays.asList("false", "true"));
        inputMap.put("text", Arrays.asList("text1", "text2"));

        testDotKeys();
        testNoKeys();
        testIncorrectKeys();
    }

    // Test 1: Dot keys provided
    public void testDotKeys() {
        System.out.println("Test 1: Dot keys provided");
        dotKeys.addAll(Arrays.asList("text", "flag"));
        combinedCombinations = JsonIteration.jsonIteration(inputMap, crossKeys, dotKeys, inputOptional);
        String expectedOutput = getExpectedOutputForDotKeys();
        String actualOutput = combinedCombinations.toString();
        System.out.println("Combined Combinations: " + actualOutput);
        assertEquals("Size of combined combinations with dot keys should be 8", expectedOutput, actualOutput);
        crossKeys.clear();
        dotKeys.clear();
    }

    // Test 2: No keys provided. All keys should be considered cross
    public void testNoKeys() {
        System.out.println("Test 2: All keys should be considered cross");
        combinedCombinations = JsonIteration.jsonIteration(inputMap, crossKeys, dotKeys, inputOptional);
        String expectedOutput = getExpectedOutputForNoKeys();
        String actualOutput = combinedCombinations.toString();
        System.out.println("Combined Combinations: " + actualOutput);
        assertEquals("Size of combined combinations with empty keys should be 16", expectedOutput, actualOutput);
        crossKeys.clear();
        dotKeys.clear();
    }

    // Test 3: Incorrect keys in dot
    public void testIncorrectKeys() {
        System.out.println("Test 3: Incorrect keys in dot and/or cross");
        dotKeys.addAll(Collections.singletonList("fil")); // Incorrect dot key
        combinedCombinations = JsonIteration.jsonIteration(inputMap, crossKeys, dotKeys, inputOptional);
        String expectedOutput = getExpectedOutputForIncorrectKeys();
        String actualOutput = combinedCombinations.toString();
        System.out.println("Combined Combinations: " + actualOutput);
        assertEquals("Size of combined combinations with incorrect keys should be 16", expectedOutput, actualOutput);
        crossKeys.clear();
        dotKeys.clear();
    }

    private String getExpectedOutputForIncorrectKeys() {
        return "[{sleepTime=0, file=grep.json, flag=false, text=text1}, {sleepTime=0, file=grep.json, flag=false, text=text2}, {sleepTime=1, file=grep.json, flag=false, text=text1}, {sleepTime=1, file=grep.json, flag=false, text=text2}, {sleepTime=0, file=grep.json, flag=true, text=text1}, {sleepTime=0, file=grep.json, flag=true, text=text2}, {sleepTime=1, file=grep.json, flag=true, text=text1}, {sleepTime=1, file=grep.json, flag=true, text=text2}, {sleepTime=0, file=grep.XML, flag=false, text=text1}, {sleepTime=0, file=grep.XML, flag=false, text=text2}, {sleepTime=1, file=grep.XML, flag=false, text=text1}, {sleepTime=1, file=grep.XML, flag=false, text=text2}, {sleepTime=0, file=grep.XML, flag=true, text=text1}, {sleepTime=0, file=grep.XML, flag=true, text=text2}, {sleepTime=1, file=grep.XML, flag=true, text=text1}, {sleepTime=1, file=grep.XML, flag=true, text=text2}]";
    }

    private String getExpectedOutputForNoKeys() {
        return "[{sleepTime=0, file=grep.json, flag=false, text=text1}, {sleepTime=0, file=grep.json, flag=false, text=text2}, {sleepTime=1, file=grep.json, flag=false, text=text1}, {sleepTime=1, file=grep.json, flag=false, text=text2}, {sleepTime=0, file=grep.json, flag=true, text=text1}, {sleepTime=0, file=grep.json, flag=true, text=text2}, {sleepTime=1, file=grep.json, flag=true, text=text1}, {sleepTime=1, file=grep.json, flag=true, text=text2}, {sleepTime=0, file=grep.XML, flag=false, text=text1}, {sleepTime=0, file=grep.XML, flag=false, text=text2}, {sleepTime=1, file=grep.XML, flag=false, text=text1}, {sleepTime=1, file=grep.XML, flag=false, text=text2}, {sleepTime=0, file=grep.XML, flag=true, text=text1}, {sleepTime=0, file=grep.XML, flag=true, text=text2}, {sleepTime=1, file=grep.XML, flag=true, text=text1}, {sleepTime=1, file=grep.XML, flag=true, text=text2}]";
    }

    private String getExpectedOutputForDotKeys() {
        return "[{sleepTime=0, file=grep.json, flag=false, text=text1}, {sleepTime=0, file=grep.json, flag=true, text=text2}, {sleepTime=1, file=grep.json, flag=false, text=text1}, {sleepTime=1, file=grep.json, flag=true, text=text2}, {sleepTime=0, file=grep.XML, flag=false, text=text1}, {sleepTime=0, file=grep.XML, flag=true, text=text2}, {sleepTime=1, file=grep.XML, flag=false, text=text1}, {sleepTime=1, file=grep.XML, flag=true, text=text2}]";
    }
}