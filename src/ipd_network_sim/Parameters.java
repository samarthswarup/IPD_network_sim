/*
 * Parameters.java
 *
 * Created on February 11, 2008, 12:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ipd_network_sim;

import ParameterReader.ParameterReader;

/**
 *
 * @author Samarth Swarup
 */
public class Parameters {
    
    ParameterReader pr;
    String dirHead;
    
    public int numAgents;
    public int numZealots;
    public String zealotStrategy;
    public String zealotChoice;
    public int maxTimeSteps; //number of timesteps for which the simulation is run
    public int timeStepsToPrint;
    
    public int numRuns;
    
    public String outputFilename;
    public String actionCountsFilename;
    public String degreeAndFractionCFilename;
    public String MCCprobabilitiesFilename;
    public String resultsDir;
    
    /** Creates a new instance of Parameters */
    public Parameters(String parameterFile) {
        this.pr = new ParameterReader(parameterFile);
        this.pr.setContext("common");
        this.dirHead = this.pr.getString("dirHead");
        
        this.pr.setContext("ipd_network_sim");
        this.numAgents = this.pr.getInt("numAgents");
        this.numZealots = this.pr.getInt("numZealots");
        this.zealotStrategy = this.pr.getString("zealotStrategy");
        this.zealotChoice = this.pr.getString("zealotChoice");
        
        this.maxTimeSteps = this.pr.getInt("maxTimeSteps");
        this.numRuns = this.pr.getInt("numRuns");
        this.timeStepsToPrint = this.pr.getInt("timeStepsToPrint");
        
        this.resultsDir = this.pr.getString("resultsDir");
        this.actionCountsFilename = this.dirHead + this.resultsDir + this.pr.getString("actionCountsFilename");
        this.degreeAndFractionCFilename = this.dirHead + this.resultsDir + this.pr.getString("degreeAndFractionCFilename");
        this.outputFilename = this.dirHead + this.resultsDir + this.pr.getString("outputFilename");
        this.MCCprobabilitiesFilename = this.dirHead + this.resultsDir + this.pr.getString("MCCprobabilitiesFilename");
    }
    
}
