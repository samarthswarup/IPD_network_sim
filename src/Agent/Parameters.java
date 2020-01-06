/*
 * Parameters.java
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
public class Parameters {
    
    ParameterReader pr;
    
    public String strategy;
    
    public int CCpayoff;
    public int CDpayoff; //user C, opponent D 
    public int DCpayoff;
    public int DDpayoff;
    
    public int myHistoryLength;
    public int opponentHistoryLength;
    
    //Parameters for Moody Conditional Cooperation
    public double minPCAfterC; //minimum probability of cooperating after agent cooperated in previous round
    public double maxPCAfterC;
    public double minPCAfterD;
    public double maxPCAfterD;
    
    //Parameters for Aspiration Learning
    public double initialPt; //initial value of pt parameter for the aspiration learning model
    public double A;
    public boolean adaptiveA;
    public double h;
    public double decayRate;
    public double beta;
    public double epsilon;
    public boolean adaptiveEpsilon;
    public double probIncr;
    
    //Parameters for Memory2 strategy
    public double initialPC;
    public double PCgivenCincr;
    public double PCgivenDincr;
    
    //Parameters for Random strategy
    public double pCooperate;
    
    /** Creates a new instance of Parameters
     * @param parameterFile */
    public Parameters(String parameterFile) {
        this.pr = new ParameterReader(parameterFile);
        this.pr.setContext("Agent");

        this.strategy = this.pr.getString("strategy");
        
        this.CCpayoff = this.pr.getInt("CCpayoff");
        this.CDpayoff = this.pr.getInt("CDpayoff");
        this.DCpayoff = this.pr.getInt("DCpayoff");
        this.DDpayoff = this.pr.getInt("DDpayoff");
        
        this.myHistoryLength = this.pr.getInt("myHistoryLength");
        this.opponentHistoryLength = this.pr.getInt("opponentHistoryLength");
        
        this.minPCAfterC = this.pr.getDouble("minPCoopAfterC");
        this.maxPCAfterC = this.pr.getDouble("maxPCoopAfterC");
        this.minPCAfterD = this.pr.getDouble("minPCoopAfterD");
        this.maxPCAfterD = this.pr.getDouble("maxPCoopAfterD");
        
        this.initialPt = this.pr.getDouble("initialPt");
        this.A = this.pr.getDouble("A");
        this.adaptiveA = this.pr.getBoolean("adaptiveA");
        this.h = this.pr.getDouble("h");
        this.decayRate = this.pr.getDouble("decayRate");
        this.beta = this.pr.getDouble("beta");
        this.epsilon = this.pr.getDouble("epsilon");
        this.adaptiveEpsilon = this.pr.getBoolean("adaptiveEpsilon");
        this.probIncr = this.pr.getDouble("probIncr");
        
        this.initialPC = this.pr.getDouble("initialPC");
        this.PCgivenCincr = this.pr.getDouble("PCgivenCincr");
        this.PCgivenDincr = this.pr.getDouble("PCgivenDincr");
        
        this.pCooperate = this.pr.getDouble("pCooperate");
    }
    
}
