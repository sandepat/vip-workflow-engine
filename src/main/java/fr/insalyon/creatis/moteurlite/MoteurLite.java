package fr.insalyon.creatis.moteurlite;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.insalyon.creatis.gasw.Gasw;
import fr.insalyon.creatis.gasw.GaswInput;
import fr.insalyon.creatis.gasw.parser.GaswParser;
import fr.insalyon.creatis.moteurlite.boutiquesParser.BoutiquesEntities;
import fr.insalyon.creatis.moteurlite.boutiquesParser.BoutiquesService;
import fr.insalyon.creatis.moteurlite.inputsParser.ParseInputsFile;
import fr.insalyon.creatis.moteurlite.iterationStrategy.IterationStrategy;

/**
 * 
 * Author: Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class MoteurLite {
    private Gasw gasw = Gasw.getInstance();
    private BoutiquesService boutiquesService = new BoutiquesService();
    private int sizeOfInputs;
    private IterationStrategy iterationStrategy = new IterationStrategy();
    private Workflowsdb workflowsdb = new Workflowsdb();

    private String applicationName;
    private String boutiquesFilePath;
    private String executableName;
    private HashMap<Integer, String> inputBoutiquesId;
    private HashMap<Integer, String> outputBoutiquesId;
    List<Map<String, String>> jsonIterations;
    private Map<String, String> inputType;
    private Map<String, String> resultsDirectory;
    private String workflowId;

    public static void main(String[] args) throws Exception {
        new MoteurLite(args);
    }

    public MoteurLite(String[] args) throws Exception {
        // Verify arguments
        if (args.length != 3) {
            throw new IllegalArgumentException("Exactly 3 arguments are required: workflowId, boutiquesFilePath, inputsFilePath.");
        }

        workflowId = args[0];
        boutiquesFilePath = args[1];
        String inputsFilePath = args[2];

        // Check if arguments are not null or empty
        for (String arg : args) {
            if (arg == null || arg.trim().isEmpty()) {
                throw new IllegalArgumentException("Arguments cannot be null or empty.");
            }
        }

        // Validate workflowId as a non-empty string
        if (workflowId == null || workflowId.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid workflowId. It should be a simple non-empty string.");
        }

        // Validate boutiquesFilePath as a JSON file
        if (!boutiquesFilePath.endsWith(".json")) {
            throw new IllegalArgumentException("Invalid boutiquesFilePath. It should be a JSON file.");
        }

        // Validate inputsFilePath as an XML file
        if (!inputsFilePath.endsWith(".xml")) {
            throw new IllegalArgumentException("Invalid inputsFilePath. It should be an XML file.");
        }

        // Initialize boutiques.json and inputs.xml
        init(boutiquesFilePath, inputsFilePath);

        // Set gasw monitoring
        GaswMonitor gaswMonitor = new GaswMonitor(workflowId, applicationName, outputBoutiquesId, sizeOfInputs, gasw);
        gasw.setNotificationClient(gaswMonitor);
        gaswMonitor.start();
        
        // Create jobs
        createJobs(jsonIterations);
    }

    private void init(String boutiquesFilePath, String inputsFilePath) throws Exception {
        BoutiquesEntities boutiquesEntities = boutiquesService.parseFile(boutiquesFilePath);
        executableName = boutiquesService.getNameOfBoutiquesFile(boutiquesEntities);
        applicationName = boutiquesService.getApplicationName(boutiquesEntities);
        inputBoutiquesId = boutiquesService.getInputIdOfBoutiquesFile(boutiquesEntities);
        outputBoutiquesId = boutiquesService.getOutputIdOfBoutiquesFile(boutiquesEntities);
        inputType = boutiquesService.getInputTypeOfBoutiquesFile(boutiquesEntities);
        Set<String> crossMap = boutiquesService.getCrossMap(boutiquesEntities);
        Set<String> dotMap = boutiquesService.getDotMap(boutiquesEntities);
        Set<String> inputOptional = boutiquesService.getInputOptionalOfBoutiquesFile(boutiquesEntities);
        
        // Parse inputs
        ParseInputsFile inputsParser = new ParseInputsFile(inputsFilePath);
        List<Map<String, String>> inputData = inputsParser.getInputData();
        resultsDirectory = inputsParser.getResultDirectory();

        // Set workflowsdb
        workflowsdb.persistInputs(workflowId, inputData, inputType, resultsDirectory);

        // Set iteration strategy
        iterationStrategy.IterationStratergy(inputData, resultsDirectory, crossMap, dotMap, inputOptional);
        jsonIterations = iterationStrategy.getJsonIterations();
        sizeOfInputs = jsonIterations.size();
    }

    public void createJobs(List<Map<String, String>> inputs) throws Exception {
        for (Map<String, String> innerList : inputs) {
            Map<String, String> inputsMap = new HashMap<>();
            Map<String, String> invocation = new HashMap<>();

            for (Map.Entry<String, String> entry : innerList.entrySet()) {
                inputsMap.put(entry.getKey(), entry.getValue());
                if (!entry.getKey().equals("results-directory")) {
                    invocation.put(entry.getKey(), entry.getValue());
                }
            }

            List<URI> downloadFiles = ParseInputsFile.getDownloadFiles(inputsMap);
            GaswParser gaswParser = new GaswParser();
            String invocationString = CreateInvocation.convertMapToJson(invocation, inputType);
            String jobId = applicationName + "-" + System.nanoTime() + ".sh";

            // Convert boutiquesFilePath to absolute path
            File boutiquesFile = new File(boutiquesFilePath);
            if (!boutiquesFile.isAbsolute()) {
                boutiquesFilePath = boutiquesFile.getAbsolutePath();
            }

            // Check if boutiquesFilePath starts with /var/www/ and add file:// prefix if necessary
            if (boutiquesFilePath.startsWith("/var/www/")) {
                boutiquesFilePath = "file:/" + boutiquesFilePath;
            }
            System.out.println(("Boutiques file path: " + boutiquesFilePath));

            GaswInput gaswInput = gaswParser.getGaswInput(applicationName, inputsMap, executableName,
                    inputBoutiquesId, outputBoutiquesId, invocationString, resultsDirectory, jobId, downloadFiles, boutiquesFilePath);
            gasw.submit(gaswInput);
            System.out.println("Job launched: " + jobId);
        }
    }
}
