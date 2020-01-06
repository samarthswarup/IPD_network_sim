/*
 * Debug.java
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
public class Debug {
    
    ParameterReader pr;
    
    public boolean quitAfterInitialization;
    
    public boolean printGeneral;
    public boolean printStepwiseDetails;
    public boolean printTimeSteps;
    public boolean printTestingSetExamples;
    public boolean printEvaluationDetails;
    
    /** Creates a new instance of Debug */
    public Debug(String parameterFile) {
        this.pr = new ParameterReader(parameterFile);
        this.pr.setContext("ipd_network_sim");
        
        this.quitAfterInitialization = this.pr.getBoolean("quitAfterInitialization");
        
        this.printGeneral = this.pr.getBoolean("printGeneral");
        this.printStepwiseDetails = this.pr.getBoolean("printStepwiseDetails");
        this.printTimeSteps = this.pr.getBoolean("printTimeSteps");
        this.printTestingSetExamples = this.pr.getBoolean("printTestingSetExamples");
        this.printEvaluationDetails = this.pr.getBoolean("printEvaluationDetails");
    }
    
}
