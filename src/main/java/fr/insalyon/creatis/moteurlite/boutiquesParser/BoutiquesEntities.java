package fr.insalyon.creatis.moteurlite.boutiquesParser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * 
 * Author: Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class BoutiquesEntities {
    private String author;
    private String commandLine;
    private String containerImage;
    private String containerIndex;
    private String containerType;
    private String containerOpts;
    private Map<String, Object> custom;
    private String description;
    private String descriptorUrl;
    private String doi;
    private List<Map<String, Object>> environmentVariables;
    private List<Map<String, Object>> errorCodes;
    private List<Map<String, Object>> groups;
    private List<Map<String, Object>> inputs;
    private String name;
    private List<String> onlinePlatformUrls;
    private List<Map<String, Object>> outputFiles;
    private String schemaVersion;
    private String shell;
    private List<Map<String, Object>> tests;
    private String toolDoi;
    private String toolVersion;
    private String url;
    Object inputId;
    Object outputId;
    Object inputType = new HashMap<>();
    Object inputValueKey = new HashMap<>();
    Object outputPathTemplate = new HashMap<>();
    Object inputOptional = new HashMap<>();

    HashMap<Integer, String> inputIdList = new HashMap<Integer, String>();
    HashMap<Integer, String> outputIdList = new HashMap<Integer, String>();
    HashMap<String, String> inputTypelist = new HashMap<String, String>();
    HashMap<String, String> inputValueKeylist = new HashMap<String, String>();
    HashMap<String, String> outputPathTemplateList = new HashMap<String, String>();
    BoutiquesObjectParser boutiquesObjectParser;
    Map<String, String> dotMap = new HashMap<>();
    private Set<String> dotSet = new HashSet<>();
    private Set<String> crossSet = new HashSet<>();
    private Set<String> containerSet = new HashSet<>();
    private Set<String> inputOptionalSet = new HashSet<>();
    
        @JsonProperty("author")
        public String getAuthor() {
            return author;
        }
    
        @JsonProperty("author")
        public void setAuthor(String author) {
            this.author = author;
        }
    
        @JsonProperty("command-line")
        public String getCommandLine() {
            return commandLine;
        }
    
        @JsonProperty("command-line")
        public void setCommandLine(String commandLine) {
            this.commandLine = commandLine;
        }

        @JsonProperty("container-image")
        public void setContainerImage(Map<String, Object> containerImageMap) {
            if (containerImageMap == null) {
                return; // Return early if containerImageMap is null
            }
    
            this.containerImage = (String) containerImageMap.get("image");
            this.containerIndex = (String) containerImageMap.get("index");
            this.containerType = (String) containerImageMap.get("type");
            this.containerOpts = (String) containerImageMap.get("container-opts");
            
            // Add the container image path to the containerSet
            if (this.containerImage != null) {
                containerSet.add(this.containerImage);
            }
        }
    
        public String getContainerImage() {
            return containerImage;
        }
    
        public String getContainerIndex() {
            return containerIndex;
        }
    
        public String getContainerType() {
            return containerType;
        }
    
        public String getContainerOpts() {
            return containerOpts;
        }
    
        @JsonProperty("custom")
        public void setCustom(Map<String, Object> custom) {
            this.custom = custom;
    
            if (custom == null) {
                return; // Return early if custom is null
            }
    
            for (Map.Entry<String, Object> entry : custom.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
    
                if (value instanceof String) {
                    handleKeyValue(key, (String) value);
                } else if (value instanceof Iterable) {
                    for (Object item : (Iterable<?>) value) {
                        handleKeyValue(key, item.toString());
                    }
                }
            }
        }
    
        private void handleKeyValue(String key, String value) {
            if (key.contains("VIP:cross")) {
                crossSet.add(value);
            } 
            else if (key.contains("VIP:dot-inputs")) {
                dotSet.add(value);
            }
            else if (key.contains("VIP:imagepath")) {
                containerSet.add(value);
            }
        }
    
        public Set<String> getCrossMap() {
            return crossSet;
        }
    
        public Set<String> getDotMap() {
            return dotSet;
        }

        @JsonProperty("description")
        public String getDescription() {
            return description;
        }

        public Set<String> getContainerSet() {
            return containerSet;
        }
    
        @JsonProperty("description")
        public void setDescription(String description) {
            this.description = description;
        }
    
        @JsonProperty("descriptor-url")
        public String getDescriptorUrl() {
            return descriptorUrl;
        }
    
        @JsonProperty("descriptor-url")
        public void setDescriptorUrl(String descriptorUrl) {
            this.descriptorUrl = descriptorUrl;
        }
    
        @JsonProperty("doi")
        public String getDoi() {
            return doi;
        }
    
        @JsonProperty("doi")
        public void setDoi(String doi) {
            this.doi = doi;
        }
    
        @JsonProperty("environment-variables")
        public List<Map<String, Object>> getEnvironmentVariables() {
            return environmentVariables;
        }
    
        @JsonSetter("environment-variables")
        public void setEnvironmentVariables(List<Map<String, Object>> environmentVariables) {
            this.environmentVariables = environmentVariables;
        }
    
        @JsonProperty("error-codes")
        public List<Map<String, Object>> getErrorCodes() {
            return errorCodes;
        }
    
        @JsonSetter("error-codes")
        public void setErrorCodes(List<Map<String, Object>> errorCodes) {
            this.errorCodes = errorCodes;
        }
    
        @JsonProperty("groups")
        public List<Map<String, Object>> getGroups() {
            return groups;
        }
    
        @JsonSetter("groups")
        public void setGroups(List<Map<String, Object>> groups) {

            this.groups = groups;
        }
    
        @JsonProperty("inputs")
        public List<Map<String, Object>> getInputs() {
            return inputs;
        }
    
        @JsonSetter("inputs")
        public void setInputs(List<Map<String, Object>> inputs) {
            for(int inputNo = 0; inputNo < inputs.size(); inputNo++) {
                Map<String, Object> input = inputs.get(inputNo);
                boutiquesObjectParser = new BoutiquesObjectParser(inputs, BoutiquesInput.class);
                inputId = input.get("id");
                inputType = input.get("type");
                inputValueKey = input.get("value-key");
                inputOptional = input.get("optional");
        
                inputIdList.put(inputNo, inputId.toString());
                inputTypelist.put(inputId.toString(), inputType.toString());
                inputValueKeylist.put(inputId.toString(), inputValueKey.toString());
        
                // Check if the "optional" field is present before accessing its value
                if (inputOptional != null && (boolean) inputOptional) {
                    inputOptionalSet.add(inputId.toString());
                }
            }
            this.inputs = inputs;
        }

        public HashMap<Integer, String> getInputId(){
            return inputIdList;
        }

        public HashMap<String, String> getInputTypes(){
            return inputTypelist;
        }

        public HashMap<String, String> getInputValueKey(){
            return inputValueKeylist;
        }

        public Set<String> getInputOptional(){ 
            return inputOptionalSet;
        }
    
        @JsonProperty("name")
        public String getName() {
            return name;
        }
    
        @JsonProperty("name")
        public void setName(String name) {
            this.name = name;
        }
    
        @JsonProperty("online-platform-urls")
        public List<String> getOnlinePlatformUrls() {
            return onlinePlatformUrls;
        }
    
        @JsonSetter("online-platform-urls")
        public void setOnlinePlatformUrls(List<String> onlinePlatformUrls) {
            this.onlinePlatformUrls = onlinePlatformUrls;
        }
    
        @JsonProperty("output-files")
        public List<Map<String, Object>> getOutputFiles() {
            return outputFiles;
        }
    
        @JsonSetter("output-files")
        public void setOutputFiles(List<Map<String, Object>> outputFiles) {
            //System.out.println("output-files:"+outputFiles);

            for(int outputNo =0; outputNo < outputFiles.size();outputNo++){
                //System.out.println("outputs:"+ outputNo+ outputFiles.get(outputNo));
                boutiquesObjectParser = new BoutiquesObjectParser(outputFiles, BoutiquesOutput.class);
                outputId = outputFiles.get(outputNo).get("id");
                outputIdList.put(outputNo,outputId.toString());
                outputPathTemplate = outputFiles.get(outputNo).get("path-template");
                outputPathTemplateList.put(outputId.toString(),outputPathTemplate.toString());
            }

            
            this.outputFiles = outputFiles;
        }

        public HashMap<Integer, String> getOutputId(){
            return outputIdList;
        }

        public HashMap<String, String> getOutputPathTemplateList(){
            return outputPathTemplateList;
        }
    
        @JsonProperty("schema-version")
        public String getSchemaVersion() {
            return schemaVersion;
        }
    
        @JsonProperty("schema-version")
        public void setSchemaVersion(String schemaVersion) {
            this.schemaVersion = schemaVersion;
        }
    
        @JsonProperty("shell")
        public String getShell() {
            return shell;
        }
    
        @JsonProperty("shell")
        public void setShell(String shell) {
            this.shell = shell;
        }    
    
        @JsonProperty("tests")
        public List<Map<String, Object>> getTests() {
            return tests;
        }
    
        @JsonSetter("tests")
        public void setTests(List<Map<String, Object>> tests) {
            this.tests = tests;
        }
    
        @JsonProperty("tool-doi")
        public String getToolDoi() {
            return toolDoi;
        }
    
        @JsonProperty("tool-doi")
        public void setToolDoi(String toolDoi) {
            this.toolDoi = toolDoi;
        }
    
        @JsonProperty("tool-version")
        public String getToolVersion() {
            return toolVersion;
        }
    
        @JsonProperty("tool-version")
        public void setToolVersion(String toolVersion) {
            this.toolVersion = toolVersion;
        }
    
        @JsonProperty("url")
        public String getUrl() {
            return url;
        }
    
        @JsonProperty("url")
        public void setUrl(String url) {
            this.url = url;
        }
    }