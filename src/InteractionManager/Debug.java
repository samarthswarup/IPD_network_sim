/*
 * Debug.java
 *
 * Created on October 14, 2005, 3:13 PM
 */

package InteractionManager;

import ParameterReader.ParameterReader;
/**
 *
 * @author Samarth Swarup
 */
public class Debug {
    
    ParameterReader pr;
    
    public boolean printGeneral;
    public boolean printDegDistrConstrInfo;
    public boolean printRowAndColumnForEachAgent;
    public boolean printRmatLRUD;
    public boolean printIndegreesForAllAgents;
    public boolean printAllNormalizedIndegrees;
    public boolean printAgentsSortedByIndegree;
    
    /** Creates a new instance of Debug */
    public Debug(String parameterFile) {
        this.pr = new ParameterReader(parameterFile);
        this.pr.setContext("InteractionManager");
        
        this.printGeneral = this.pr.getBoolean("printGeneral");
        this.printDegDistrConstrInfo = this.pr.getBoolean("printDegDistrConstrInfo");
        this.printRowAndColumnForEachAgent = this.pr.getBoolean("printRowAndColumnForEachAgent");
        this.printRmatLRUD = this.pr.getBoolean("printRmatLRUD");
        this.printIndegreesForAllAgents = this.pr.getBoolean("printIndegreesForAllAgents");
        this.printAllNormalizedIndegrees = this.pr.getBoolean("printAllNormalizedIndegrees");
        this.printAgentsSortedByIndegree = this.pr.getBoolean("printAgentsSortedByIndegree");
    }
    
}
