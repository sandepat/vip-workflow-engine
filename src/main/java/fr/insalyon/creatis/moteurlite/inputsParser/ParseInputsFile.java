package fr.insalyon.creatis.moteurlite.inputsParser;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class ParseInputsFile {
    private Map<String, String> resultDir;
    private List<Map<String, String>> inputsMap;
    private Map<String, String> inputsTypeMap = new HashMap<>();


    public ParseInputsFile(String inputsFilePath) {
        resultDir = setResultDirectory(inputsFilePath);
        inputsMap = setInputData(inputsFilePath);
        inputsTypeMap = setInputTypeMap(inputsFilePath);
    }

    public static List<URI> getDownloadFiles(Map<String, String> inputsMap) {
        List<URI> downloads = new ArrayList<>();
        System.out.println("Download File URIs:" + downloads);
        System.out.println("Download File URIs:" + inputsMap);
        for (Map.Entry<String, String> entry : inputsMap.entrySet()) {
            if (InputDownloads.isFileURI(entry.getValue())) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
                try {
                    URI uri = new URI(entry.getValue());
                    if (uri.getScheme() == null) {
                        // Add "lfn://" prefix if URI has no scheme
                        uri = new URI("lfn://" + entry.getValue());
                    }
                    downloads.add(uri);
                } catch (Exception e) {
                    // Handle URI parsing exception
                    System.err.println("Error parsing URI: " + entry.getValue());
                }
            }
        }
        return downloads;
    }

    private List<Map<String, String>> setInputData(String inputsFilePath) {
        return InputParser.parseInputData(inputsFilePath);
    }

    public Map<String, String> setResultDirectory(String inputsFilePath) {
        return InputParser.parseResultDir(inputsFilePath);
    }

    public Map<String, String> setInputTypeMap(String inputsFilePath) {
        return InputParser.parseInputType(inputsFilePath);
    }

    public List<Map<String, String>> getInputData() {
        return inputsMap;
    }

    public Map<String, String> getResultDirectory() {
        return resultDir;
    }

    public Map<String, String> getInputTypeMap() {
        return inputsTypeMap;
    }
}
