package fr.insalyon.creatis.moteurlite;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.insalyon.creatis.gasw.execution.GaswStatus;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.DataType;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Input;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.InputID;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Output;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.OutputID;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Processor;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.ProcessorID;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.Workflow;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.bean.WorkflowStatus;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.InputDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.OutputDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.ProcessorDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.WorkflowDAO;
import fr.insalyon.creatis.moteur.plugins.workflowsdb.dao.WorkflowsDBDAOFactory;

/**
 * 
 * @author Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class Workflowsdb {

    public void Inputs(String workflowId, List<Map<String, String>> inputData, Map<String, String> inputType,
            Map<String, String> resultsDirectory, HashMap<Integer, String> outputData, Map<String, String> outputName, String outputDirName, String applicationName, 
            String jobId, Integer queued, Integer completed, Integer failed)
            throws Exception {
        persistInputs(workflowId, inputData, inputType, resultsDirectory);
        persistProcessors(workflowId, applicationName, queued, completed, failed);
    }

    public void persistInputs(String workflowId, List<Map<String, String>> inputData, Map<String, String> inputType,
            Map<String, String> resultsDirectory) throws Exception {
        WorkflowsDBDAOFactory workflowsDBDAOFactory = new WorkflowsDBDAOFactory();
        InputDAO inputDAO = workflowsDBDAOFactory.getInputDAO();
        Input input = new Input();
        InputID inputID = new InputID();
        for (Map<String, String> item : inputData) {
            String key = item.keySet().iterator().next();
            String value = item.get(key);
            String itemType = inputType.get(key);
            List<String> result = new ArrayList<>();
            result.add(workflowId);
            result.add(key);
            result.add(itemType);
            Map<String, List<String>> outputMap = new HashMap<>();
            outputMap.put(value, result);
            // Add result-input to the output map
            List<String> resultDirectoryList = new ArrayList<>();
            resultDirectoryList.add(workflowId);
            resultDirectoryList.add("result-directory");
            resultDirectoryList.add("String");
            outputMap.put(resultsDirectory.get("results-directory"), resultDirectoryList);
            for (Map.Entry<String, List<String>> entry : outputMap.entrySet()) {
                List<String> valueList = entry.getValue();
                if (valueList.size() > 3 && valueList.get(3).equals(entry.getKey())) {
                    valueList.remove(3); // Remove duplicate value from the list
                }
                inputID.setWorkflowID(valueList.get(0));
                inputID.setPath(entry.getKey());
                inputID.setProcessor(valueList.get(1));
                input.setInputID(inputID);
                if (isURL(entry.getKey())) {
                    input.setType(DataType.URI);
                } else {
                    input.setType(DataType.String);
                }
                inputDAO.add(input);
            }
        }
    }

    public void persistOutputs(String workflowId, HashMap<Integer, String> outputData, List<URI> uploadList)
    throws Exception {
        WorkflowsDBDAOFactory workflowsDBDAOFactory = new WorkflowsDBDAOFactory();
        OutputDAO outputDAO = workflowsDBDAOFactory.getOutputDAO();
        Output output = new Output();
        OutputID outputID = new OutputID();
        for (Map.Entry<Integer, String> entry : outputData.entrySet()) {
            outputID.setWorkflowID(workflowId);
            outputID.setProcessor(entry.getValue());
            outputID.setPath("");
            output.setOutputID(outputID);
            output.setType(DataType.String);

            int index = entry.getKey();
            if (index < uploadList.size()) {
                URI uploadURI = uploadList.get(index);
                outputID.setPath(uploadURI.toString());
            }
            outputDAO.add(output);
        }
    }

    public void persistProcessors(String workflowId, String applicationName, Integer queued, Integer completed, Integer failed) throws Exception {
        WorkflowsDBDAOFactory workflowsDBDAOFactory = new WorkflowsDBDAOFactory();
        ProcessorDAO processorDAO = workflowsDBDAOFactory.getProcessorDAO();
        Processor processors = new Processor();
        ProcessorID processorID = new ProcessorID();
        processorID.setWorkflowID(workflowId);
        processorID.setProcessor(applicationName);
        //processorID.setJobID(JobId);
        processors.setProcessorID(processorID);
        processors.setQueued(queued);
        processors.setCompleted(completed);
        processors.setFailed(failed);
        processorDAO.add(processors);
        
    }
    
    
    public void persistWorkflows(String workflowId, GaswStatus status) throws Exception {
        // Determine the final status based on the GaswStatus
        WorkflowStatus finalStatus = WorkflowStatus.Unknown;
        if (status.equals(GaswStatus.COMPLETED)) {
            finalStatus = WorkflowStatus.Completed;
        } else if (status.equals(GaswStatus.ERROR)) {
            finalStatus = WorkflowStatus.Failed;
        }
    
        // Fetch the existing Workflow entity by its ID
        WorkflowsDBDAOFactory workflowsDBDAOFactory = new WorkflowsDBDAOFactory();
        WorkflowDAO workflowDAO = workflowsDBDAOFactory.getWorkflowDAO();
        Workflow workflow = workflowDAO.get(workflowId);

        Date currentDate = new Date();
        // Format the current date and time to a string
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = formatter.format(currentDate);
        Date parsedDate = formatter.parse(formattedDateTime);
    
        // Update only the status of the Workflow entity
        if (workflow != null) {
            workflow.setStatus(finalStatus);
            workflow.setFinishedTime(null);
            workflow.setFinishedTime(parsedDate);

            workflowDAO.update(workflow);
        } else {
            throw new Exception("Workflow with ID " + workflowId + " not found.");
        }
    }

    private boolean isURL(String urlString) {
        // Check if the string contains a forward slash '/'
        return urlString.contains("/");
    }
}