package fr.insalyon.creatis.moteurlite;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

import fr.insalyon.creatis.gasw.Gasw;
import fr.insalyon.creatis.gasw.GaswException;
import fr.insalyon.creatis.gasw.GaswExitCode;
import fr.insalyon.creatis.gasw.GaswOutput;
import fr.insalyon.creatis.gasw.execution.GaswStatus;

/**
 * 
 * @author Sandesh Patil [https://github.com/sandepat]
 * 
 */

public class GaswMonitor extends Thread {
    private String workflowId;
    private String applicationName;
    private HashMap<Integer, String> outputBoutiquesId;
    private int sizeOfInputs;
    private Gasw gasw;
    private Workflowsdb workflowsdb = new Workflowsdb();


    public GaswMonitor(String workflowId, String applicationName, HashMap<Integer, String> outputBoutiquesId, int sizeOfInputs, Gasw gasw) {
        this.workflowId = workflowId;
        this.applicationName = applicationName;
        this.outputBoutiquesId = outputBoutiquesId;
        this.sizeOfInputs = sizeOfInputs;
        this.gasw = gasw;
        this.workflowsdb = workflowsdb;
    }

    @Override
    public void run() {
        Integer finishedJobsNumber = 0;
        Integer successfulJobsNumber = 0;
        Integer failedJobsNumber = 0;
        boolean hasSuccessfulJob = false; // Flag to track if at least one job is successful
        try {
            workflowsdb.persistProcessors(workflowId, applicationName, sizeOfInputs, successfulJobsNumber, failedJobsNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (finishedJobsNumber < sizeOfInputs) {
            synchronized (this) {
                try {
                    gasw.waitForNotification();
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    
            List<GaswOutput> finishedJobs = gasw.getFinishedJobs();
            System.out.println("Number of finished jobs: " + finishedJobs.size());
    
            if (finishedJobs.isEmpty()) {
                gasw.waitForNotification();
                continue;
            }
    
            for (GaswOutput gaswOutput : finishedJobs) {
                System.out.println("Status: " + gaswOutput.getJobID() + " " + gaswOutput.getExitCode());
                try {
                    GaswExitCode exitCode = gaswOutput.getExitCode();
                    if (exitCode == GaswExitCode.SUCCESS) {
                        successfulJobsNumber++;
                        hasSuccessfulJob = true; // At least one job is successful
                    }
                    else {
                        failedJobsNumber++;
                    }
                    List<URI> uploadedResults = gaswOutput.getUploadedResults();
                    System.out.println("Uploaded resultsAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA: " + uploadedResults);
                    if (uploadedResults != null) {
                        workflowsdb.persistOutputs(workflowId, outputBoutiquesId, uploadedResults);
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("EEEEEEEEEEEEEEEEEE" + gaswOutput.getUploadedResults());
            }
            finishedJobsNumber += finishedJobs.size();
            try {
               workflowsdb.persistProcessors(workflowId, applicationName, sizeOfInputs-finishedJobsNumber, successfulJobsNumber, failedJobsNumber);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    
        try {
            // Determine the final status of the processor based on the jobs' status
            GaswStatus finalStatus = hasSuccessfulJob ? GaswStatus.COMPLETED : GaswStatus.ERROR;
            // Persist the final processor status
            try {
                workflowsdb.persistWorkflows(workflowId,finalStatus);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    
            gasw.terminate();
            System.out.println("Completed execution of workflow");
        } catch (GaswException e) {
            e.printStackTrace();
        }
    }
}    