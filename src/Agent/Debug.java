/*
 * Debug.java
 *
 * Created on May 2, 2007, 8:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Agent;

import ParameterReader.ParameterReader;

/**
 *
 * @author Samarth Swarup
 */
public class Debug {
    
    ParameterReader pr;
    
    public boolean printOpponentHistory;
    public boolean printZealotAction;
    public boolean printAspirationLearningUpdateDetails;
    public boolean printMemory2UpdateDetails;
    
    /** Creates a new instance of Debug */
    public Debug(String parameterFile) {
        this.pr = new ParameterReader(parameterFile);
        this.pr.setContext("Agent");
        
        this.printOpponentHistory = this.pr.getBoolean("printOpponentHistory");
        this.printZealotAction = this.pr.getBoolean("printZealotAction");
        this.printAspirationLearningUpdateDetails = this.pr.getBoolean("printAspirationLearningUpdateDetails");
        this.printMemory2UpdateDetails = this.pr.getBoolean("printMemory2UpdateDetails");
    }
    
}
