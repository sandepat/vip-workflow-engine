package fr.insalyon.creatis.moteurlite.boutiquesParser;

import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonCustomPropertyValidator {

    public void validateCustomProperty(String jsonString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Object object = new JSONParser().parse(new FileReader(jsonString));
            JsonNode rootNode = objectMapper.readTree(String.valueOf(object));

            if (rootNode.has("custom")) {
                JsonNode customNode = rootNode.get("custom");
                if (customNode.has("VIPdot") && customNode.has("VIPcross")) {
                    JsonNode dotNode = customNode.get("VIPdot");
                    JsonNode crossNode = customNode.get("VIPcross");

                    Set<String> inputValues = new HashSet<>();
                    JsonNode inputsNode = rootNode.get("inputs");
                    for (JsonNode inputNode : inputsNode) {
                        inputValues.add(inputNode.get("id").asText());
                    }

                    Set<String> dotValues = new HashSet<>();
                    for (JsonNode dot : dotNode) {
                        dotValues.add(dot.asText());
                    }

                    Set<String> crossValues = new HashSet<>();
                    for (JsonNode cross : crossNode) {
                        crossValues.add(cross.asText());
                    }

                    for (String value : inputValues) {
                        if (!dotValues.contains(value) && !crossValues.contains(value)) {
                            System.out.println("Warning: Input value '" + value + "' is not present in 'VIPdot' or 'VIPcross'");
                        }
                    }

                    Set<String> intersection = new HashSet<>(dotValues);
                    intersection.retainAll(crossValues);
                    if (!intersection.isEmpty()) {
                        System.out.println("Warning: Overlapping values detected between 'VIPdot' and 'VIPcross'");
                    }

                    System.out.println("dot values: " + dotValues);
                    System.out.println("cross values: " + crossValues);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
