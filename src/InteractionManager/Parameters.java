/*
 * Parameters.java
 *
 * Created on October 14, 2005, 3:13 PM
 */

package InteractionManager;

import ParameterReader.ParameterReader;
/**
 *
 * @author Samarth Swarup
 */
public class Parameters {
    ParameterReader pr;
    
    public String dirHead;
    
    public int numNodes;
   
    public String initialTopology;
    public boolean updateTopology;
    public String topologyUpdateMode;
    public int topologyUpdateInterval;
    
    public boolean saveInitialTopology;
    
    public int squareWidth; //dimensions of square topology
    public int squareHeight; //Product should equal numNodes
    
    public int numLinksToRewire; //Starting with square topology, rewire these many links to generate small world topology
    
    public double rmatA; //parameters for the R-MAT algorithm, which is used to generate scale-free small-world networks
    public double rmatB;
    public double rmatC;
    public double rmatD;
    public int numLinks;
    
    public boolean forceNumLoners; //if true, we force the number of loners to be numLoners by adding or removing links
    public int numLoners; //as necessary after the network has been generated. Only valid for scale free and random topologies.
    
    public double mutationProb;
    public double finalMutationProb;
    public int maxTimeSteps;
    
    public String networkDir; //directory where networks are stored (in pajek format)
    
    public String initialTopologyFile; //filename for the initial network
    
    /** Creates a new instance of Parameters */
    public Parameters(String parameterFile) {
        this.pr = new ParameterReader(parameterFile);
        this.pr.setContext("common");
        this.dirHead = this.pr.getString("dirHead");
        
        this.pr.setContext("InteractionManager");
        
        this.numNodes = this.pr.getInt("numNodes");
        
        this.initialTopology = this.pr.getString("initialTopology");
        this.updateTopology = this.pr.getBoolean("updateTopology");
        this.topologyUpdateMode = this.pr.getString("topologyUpdateMode");
        this.topologyUpdateInterval = this.pr.getInt("topologyUpdateInterval");
        
        this.saveInitialTopology = this.pr.getBoolean("saveInitialTopology");
        
        this.mutationProb = this.pr.getDouble("mutationProb");
        this.finalMutationProb = this.pr.getDouble("finalMutationProb");
        this.maxTimeSteps = this.pr.getInt("maxTimeSteps");
        
        this.squareWidth = this.pr.getInt("squareWidth");
        this.squareHeight = this.pr.getInt("squareHeight");
        this.numLinksToRewire = this.pr.getInt("numLinksToRewire");
        
        this.rmatA = this.pr.getDouble("rmatA");
        this.rmatB = this.pr.getDouble("rmatB");
        this.rmatC = this.pr.getDouble("rmatC");
        this.rmatD = this.pr.getDouble("rmatD");
        this.numLinks = this.pr.getInt("numLinks");
        
        this.forceNumLoners = this.pr.getBoolean("forceNumLoners");
        this.numLoners = this.pr.getInt("numLoners");
        
        this.networkDir = this.dirHead + this.pr.getString("networkDir");
        this.initialTopologyFile = this.dirHead + this.pr.getString("initialTopologyFile");
    }
}
