package fr.insalyon.creatis.moteurlite;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.icu.text.SimpleDateFormat;

import fr.insalyon.creatis.gasw.Gasw;
import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.GaswInput;
import fr.insalyon.creatis.gasw.GaswOutput;
import fr.insalyon.creatis.gasw.parser.GaswParser;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.DataType;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Input;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.InputID;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Workflow;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.WorkflowStatus;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.InputDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.OutputDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.ProcessorDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.WorkflowDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.WorkflowsDBDAOFactory;

public class MoteurLite {
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RESET = "\u001B[0m";
    private Gasw gasw = Gasw.getInstance();
    private int sizeOfInputs;
    private String bashScript;
    ParseXMLFile xmlParser = new ParseXMLFile();
    GaswParser gaswParser = new GaswParser();
    //ParseBoutiquesFile parseBoutiquesFile = new ParseBoutiquesFile();
    IterationStrategy iterationStrategy = new IterationStrategy();
    Workflowsdb workflowsdb = new Workflowsdb();
    

    public static void main(String[] args) throws Exception {
        new MoteurLite(args);
    }

    public MoteurLite(String[] args) throws Exception {
        String workflowId = args[0];
        String boutiquesFilePath = args[1];
        String inputsFilePath = args[2];


        //load bash script and parse inputs.xml
        bashScript = loadBashScript();

        //parse boutiques file
        ParseBoutiquesFile parseBoutiquesFile = new ParseBoutiquesFile(boutiquesFilePath);
        String executableName = parseBoutiquesFile.getNameOfBoutiquesFile();
        String applicationName = parseBoutiquesFile.getApplicationName();
        HashMap<Integer, String> inputBoutiquesId = parseBoutiquesFile.getinputIdOfBoutiquesFile();
        HashMap<Integer, String> outputBoutiquesId = parseBoutiquesFile.getoutputIdOfBoutiquesFile();
        Set<String> getCrossMap = parseBoutiquesFile.getCrossMap();
        Set<String> getDotMap = parseBoutiquesFile.getDotMap();
        HashMap<String, String> inputBoutiquesType = parseBoutiquesFile.getinputTypeOfBoutiquesFile();
        HashMap<String, String> inputValueKey = parseBoutiquesFile.getinputValueKeyOfBoutiquesFile();
        HashMap<String, String> outputPathTemplate = parseBoutiquesFile.getoutputPathTemplateOfBoutiquesFile();
        Map<String, String> inputsMap = new HashMap<>();
        Map<String, String> invocation = new HashMap<>();
        Map<String, String> outputName = new HashMap<>();

        
        
        xmlParser.parseXMLFile(inputsFilePath);
        List<Map<String, String>> inputData = xmlParser.getInputData();
        //Map<String, String> inputType = xmlParser.getInputTypeMap();
        Map<String, String> inputType = inputBoutiquesType;
        Map<String, String> resultsDirectory = xmlParser.getResultDirectory();

        //System.out.println("Number of inputs: " + inputType);

        //set workflowsdb
        //workflowsdb.persistInputs(workflowId, inputData, inputType, resultsDirectory);
        //workflowsdb.persistProcessors(workflowId, inputType);


        /*workflowsdb.Outputs(workflowId, outputBoutiquesId);
        //workflowsdb.Processors(inputType);
        //createWorkflow(workflowId, inputData, inputType, resultsDirectory);*/


        //set cross and dot combinations
        iterationStrategy.IterationStratergy(inputData, resultsDirectory, getCrossMap, getDotMap);
        List<Map<String, String>> crossCominations = iterationStrategy.getCrossCombinations();
        List<Map<String, String>> dotCombinations = iterationStrategy.getDotCombinations();
        List<Map<String, String>> jsonIterations = iterationStrategy.getJsonIterations();

        System.out.println("Number of dot combinations: " + dotCombinations.size());
        System.out.println(dotCombinations);

        /*List<Map<String, String>> crossCominations = xmlParser.getCrossCombinations();
        List<Map<String, String>> dotCombinations = xmlParser.getDotCombinations();
        List<Map<String, String>> jsonIterations = xmlParser.getJsonIterations();*/

        List<Map<String, String>> inputs = new ArrayList<>();
        inputs = dotCombinations; //crossCombinations
        sizeOfInputs = inputs.size();



        //set gasw monitoring
        GaswMonitor gaswMonitor = new GaswMonitor();
        gasw.setNotificationClient(gaswMonitor);
        gaswMonitor.start();
        //createWorkflow(basename, applicationName);

        //create jobs
        for (Map<String, String> innerList : inputs) {
            for (Map.Entry<String, String> entry : innerList.entrySet()) {
                inputsMap.put(entry.getKey(), entry.getValue());
                if (!entry.getKey().equals("results-directory")) {
                    invocation.put(entry.getKey(), entry.getValue());
                }
            }
            List<URI> DownloadFiles = ParseXMLFile.getDownloadFiles(inputsMap);
            String outputDirName = outputDirectoryName(applicationName);

            outputName = getOutputPath(inputValueKey, outputPathTemplate, inputsMap);
            //workflowsdb.persistOutputs(workflowId, outputBoutiquesId, outputName, outputDirName, resultsDirectory);

            String invocationString = CreateInvocation.convertMapToJson(invocation, inputType);
            String jobId = applicationName + "-" + System.nanoTime() + ".sh";
            GaswInput gaswInput = gaswParser.getGaswInput(applicationName, inputsMap, boutiquesFilePath, executableName,
                    inputBoutiquesId, outputBoutiquesId, invocationString, resultsDirectory, jobId, bashScript, DownloadFiles, outputDirName);
            gasw.submit(gaswInput);
            System.out.println("job launched : " + jobId);
        }
        System.out.println("Waiting for Notification");
    }


    public Map<String, String> getOutputPath(Map<String, String> inputValueKey, Map<String, String> outputPathTemplate, Map<String, String> inputsMap) {
        // Initialize outputName with the outputPathTemplate
        Map<String, String> equatedMap = new HashMap<>();
        String finalOutputValue = null;
        Map<String, String> outputName = new HashMap<>();

        // Iterate over the keys in inputValueKey
        for (Map.Entry<String, String> entry : inputValueKey.entrySet()) {
            String key = entry.getKey();
            String valueKey = entry.getValue();
        
            // Use the valueKey to retrieve the corresponding value from inputsMap
            String value = inputsMap.get(key);
        
            // If the value is not null, put it in the equatedMap with the original key
            if (value != null) {
                equatedMap.put(valueKey, value);
            }
        }

        for (Map.Entry<String, String> output : outputPathTemplate.entrySet()) {
            String outputKey = output.getKey();
            String outputValue = output.getValue();
            finalOutputValue = outputValue;
        
            // Print keys in equatedMap
            Pattern pattern = Pattern.compile("\\[([^\\[\\]]+)\\]");
            Matcher matcher = pattern.matcher(outputValue);
    
            // Iterate over matches and print the value corresponding to the matched key
            while (matcher.find()) {
                String match = matcher.group(); // Get the entire match including square brackets
                if (equatedMap.containsKey(match)) {
                    finalOutputValue = finalOutputValue.replace(match, equatedMap.get(match));
                    outputName.put(outputKey, finalOutputValue);
                }
            }
        }
    return outputName;
    }




    public String loadBashScript() {
        try (InputStream inputStream = getClass().getResourceAsStream("/script.sh")) {
            if (inputStream != null) {
                try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                    bashScript = scanner.useDelimiter("\\A").next();
                }
            } else {
                System.err.println("Script file not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bashScript;
    }

    public void createWorkflow(String workflowId, List<Map<String, String>> inputData, Map<String, String> inputType, Map<String, String> resultsDirectory) throws Exception {
        WorkflowsDBDAOFactory workflowsDBDAOFactory = new WorkflowsDBDAOFactory();
        WorkflowDAO workflowDAO = workflowsDBDAOFactory.getWorkflowDAO();
        ProcessorDAO processorDAO = workflowsDBDAOFactory.getProcessorDAO();
        InputDAO inputDAO = workflowsDBDAOFactory.getInputDAO();
        OutputDAO outputDAO = workflowsDBDAOFactory.getOutputDAO();
        Workflow workflow = new Workflow();
        Input input = new Input();
        InputID inputID = new InputID();
        /*inputID.setWorkflowID(workflowId);
        inputID.setPath("sandesh");
        inputID.setProcessor("sandesh");
        input.setInputID(inputID);
        input.setType(DataType.String);
        inputDAO.add(input);


        inputID.setWorkflowID(workflowId);
        inputID.setPath("sandesh1");
        inputID.setProcessor("sandesh1");
        input.setInputID(inputID);
        input.setType(DataType.String);
        inputDAO.add(input);*/



        Map<String, List<String>> output = new HashMap<>();
        for (Map<String, String> item : inputData) {
            String key = item.keySet().iterator().next();
            String value = item.get(key);
            String itemType = inputType.get(key);
            List<String> result = new ArrayList<>();
            result.add(workflowId);
            result.add(key);
            result.add(itemType);
            output.put(value, result);
        }
        // Add result-input to the output map
        List<String> resultDirectoryList = new ArrayList<>();
        resultDirectoryList.add(workflowId);
        resultDirectoryList.add("result-directory");
        resultDirectoryList.add("String");
        output.put(resultsDirectory.get("results-directory"), resultDirectoryList);

        // Print the output map
        for (Map.Entry<String, List<String>> entry : output.entrySet()) {
            List<String> valueList = entry.getValue();
            if (valueList.size() > 3 && valueList.get(3).equals(entry.getKey())) {
                valueList.remove(3); // Remove duplicate value from the list
            }
            inputID.setWorkflowID(valueList.get(0));
            inputID.setPath(entry.getKey());
            inputID.setProcessor(valueList.get(1));
            input.setInputID(inputID);
            input.setType(DataType.String);
            inputDAO.add(input);

            //System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue()+valueList.get(0)+" "+valueList.get(2)+" "+valueList.get(1));
        }


        System.out.println("workflowId:" + " " + workflowId);
        workflow.setId(workflowId);
        workflow.setUsername("sandesh1");
        workflow.setStatus(WorkflowStatus.Queued);
        workflow.setStartedTime(new Date());
        //Processor p = new Processor();
        //ProcessorID processorID = new ProcessorID(workflowId, processor);
        //p.setProcessorID(processorID);
        //processorDAO.add(p);
        System.out.println("adding workflow in workflowsdb : " + workflowId);
        workflowDAO.add(workflow);
        
        System.out.println("added workflow in workflowsdb : " + workflowId);
        System.out.println(workflowId + " " + inputData + " " + inputType + " " + resultsDirectory);

    }

    public class GaswMonitor extends Thread {
        public GaswMonitor() {}

        public void run() {
            Integer finishedJobsNumber = 0;

            while (finishedJobsNumber < sizeOfInputs) {
                synchronized (this) {
                    try {
                        MoteurLite.this.gasw.waitForNotification();
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                List<GaswOutput> finishedJobs = MoteurLite.this.gasw.getFinishedJobs();
                System.out.println("Number of finished jobs: " + finishedJobs.size());

                if (finishedJobs.isEmpty()) {
                    MoteurLite.this.gasw.waitForNotification();
                    continue;
                }

                for (GaswOutput gaswOutput : finishedJobs) {
                    System.out.println("Status: " + gaswOutput.getJobID() + " " + gaswOutput.getExitCode());
                }

                finishedJobsNumber += finishedJobs.size();
            }

            try {
                gasw.terminate();
                System.out.println("completed execution of workflow");
            } catch (GaswException e) {
                e.printStackTrace();
            }
        }
    }
    public String outputDirectoryName(String applicationName) {
        String folderName = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new Date());

        return applicationName + "_" + folderName;
    }
}