/*
 * Simulator.java
 *
 * Created on April 4, 2007, 12:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package InteractionManager;

import cern.jet.random.*;
import cern.colt.matrix.impl.*;
import cern.jet.random.engine.MersenneTwister;
//import edu.cornell.lassp.houle.RngPack.*;
import edu.uci.ics.jung.graph.*;

import Agent.Agent;

import java.util.*;
import java.io.*;
/**
 *
 * @author Samarth Swarup
 */
public class InteractionManager {
    
    Debug debug;
    Parameters parameters;
    
    ArrayList agents;

    Uniform uniDistr;  //uniform probability distribution
    Normal normal; //Gaussian probability distribution
//    Ranmar ranmar; //random number generator
    MersenneTwister mt; //random number generator
    
    DenseDoubleMatrix1D initialFitnesses;
    int highestFitnessAgent;
    double highestFitness;
    
    DenseDoubleMatrix1D indegrees;
    
    double fitnessMean;
    double fitnessStdDev;
    
    double mutationProbDecayRate;
    
    int timestep;
    
    int rmatFrom;
    int rmatTo;
    
    Graph<Integer, Integer> g;
    
    /** Creates a new instance of Simulator */
//    public InteractionManager(String parameterFile, Ranmar r, ArrayList ag) {
    public InteractionManager(String parameterFile, MersenneTwister m, ArrayList ag) {
        this.debug = new Debug(parameterFile);
        this.parameters = new Parameters(parameterFile);
        
        this.agents = ag;
        
        this.parameters.numNodes = this.agents.size();
        this.initialFitnesses = new DenseDoubleMatrix1D(this.parameters.numNodes);
        
//        this.ranmar = r; //initialize using the current date
        this.mt = m;
        this.uniDistr = new Uniform(this.mt);
        
        this.mutationProbDecayRate = (this.parameters.mutationProb-this.parameters.finalMutationProb)/this.parameters.maxTimeSteps;
        
        this.timestep = 0;
        
        this.indegrees = new DenseDoubleMatrix1D(this.parameters.numNodes);
        
        this.g = new DirectedSparseGraph<Integer, Integer>();
        this.setInitialTopology();
    }
    
    public void setMaxTimeSteps(int m)
    {
        this.parameters.maxTimeSteps = m;
        this.mutationProbDecayRate = (this.parameters.mutationProb-this.parameters.finalMutationProb)/this.parameters.maxTimeSteps;
    }
    
    public void setInitialTopology()
    {
        if (this.parameters.initialTopology.equalsIgnoreCase("square"))
        {
            this.setSquareTopology();
            if (this.parameters.saveInitialTopology)
                this.saveNetworkToFile(0);
        }
        else if (this.parameters.initialTopology.equalsIgnoreCase("small world"))
        {
            this.setSmallWorldTopology();
            if (this.parameters.saveInitialTopology)
                this.saveNetworkToFile(0);
        }
        else if (this.parameters.initialTopology.equalsIgnoreCase("scale free"))
        {
            this.setScaleFreeTopology();
            if (this.parameters.saveInitialTopology)
                this.saveNetworkToFile(0);
        }
        else if (this.parameters.initialTopology.equalsIgnoreCase("scale free bidirectional"))
        {
            this.setScaleFreeBidirectionalTopology();
            if (this.parameters.saveInitialTopology)
                this.saveNetworkToFile(0);
        }
        else if (this.parameters.initialTopology.equalsIgnoreCase("random"))
        {
            this.setRandomTopology();
            if (this.parameters.saveInitialTopology)
                this.saveNetworkToFile(0);
        }
        else if (this.parameters.initialTopology.equalsIgnoreCase("fully connected"))
        { //do nothing 
            
        }
        else if (this.parameters.initialTopology.equalsIgnoreCase("read from file"))
        {
            this.readNetwork();
            if (this.parameters.saveInitialTopology)
                this.saveNetworkToFile(0);
        }
        else
            System.out.println("Unrecognized initial topology: " + this.parameters.initialTopology);
        
        if (this.debug.printIndegreesForAllAgents)
            this.printIndegrees();
        
        if (this.debug.printAllNormalizedIndegrees)
            this.printNormalizedIndegrees();
    }
    
    public Graph<Integer, Integer> createJUNGGraph()
    {
        //create vertices
        for(int i = 0; i < this.agents.size(); i++)
            this.g.addVertex((Integer) i);
        
        int edgeCounter = 0;
        for(int i = 0; i < this.agents.size(); i++)
        {
            Agent ag = (Agent) this.agents.get(i);
            ArrayList nbors = ag.getNeighbors();
            if (nbors != null)
            {
                for (int j = 0; j < nbors.size(); j++) 
                {
                    Agent nbor = (Agent) nbors.get(j);
                    this.g.addEdge(edgeCounter++, i, nbor.getId());
                }
            }
        }
        
        return this.g;
    }
    
    private void readNetwork()
    {
        int numNodesInFile;
        int numLinesRead = 0;
        String networkName = "null";
        try {
            BufferedReader in = new BufferedReader(new FileReader(this.parameters.initialTopologyFile));
            String str;
            
            //read network name
            str = in.readLine();
            if (!str.startsWith("*Network"))
            {
                System.out.println("Error in readNetwork: File " + this.parameters.initialTopologyFile + 
                        " does not start with *Network!!!");
                return;
            }
            else
                System.out.println("Reading network " + str.substring(8));
            
            networkName = str.substring(8);
            numLinesRead++;
            //read in vertices
            str = in.readLine();
            if (!str.startsWith("*Vertices"))
            {
                System.out.println("Error in readNetwork: Couldn't find *Vertices in file " +
                        this.parameters.initialTopologyFile);
                return;
            }
                
            numNodesInFile = Integer.parseInt(str.substring(10));
            if (numNodesInFile != this.parameters.numNodes)
            {
                System.out.println("Error in readNetwork: Number of vertices (" + numNodesInFile + ") in file" + 
                        this.parameters.initialTopologyFile + " doesn't match number of agents (" + 
                        this.parameters.numNodes + ")!!!");
                return;
            }
            numLinesRead++;
            
            while(((str = in.readLine()) != null) && (!str.equalsIgnoreCase("*arcs")))
            {
                //read past the lines containing vertices only
                
                numLinesRead++;
                if (this.debug.printGeneral)
                    if (numLinesRead % 10000 == 0)
                        System.out.println("Read " + numLinesRead + " lines.");
            }
            
            if (!str.equalsIgnoreCase("*arcs"))
            {
                System.out.println("Error in readNetwork: Failed to find *arcs in file " + 
                        this.parameters.initialTopologyFile);
                return;
            }
            
            //read arcs
            while((str = in.readLine()) != null)
            {
                numLinesRead++;
                if (this.debug.printGeneral)
                    if (numLinesRead % 10000 == 0)
                        System.out.println("Read " + numLinesRead + " lines.");
                
                
                int from = Integer.parseInt(str.split(" ")[0]) - 1;
                int to = Integer.parseInt(str.split(" ")[1]) - 1;
                if (from >= this.parameters.numNodes || to >= this.parameters.numNodes)
                {
                    System.out.println("Error in readNetwork: Link (" + from + ", " + to + 
                            ") goes outside the network!!!");
                }
                else
                {
                    Agent fromAgent = (Agent) this.agents.get(from);
                    Agent toAgent = (Agent) this.agents.get(to);
                    fromAgent.addNeighbor(toAgent);
                }
            }
            
        } catch (IOException e)
        {
            System.out.println("In readNetwork: error reading from file!!! " + e);
        }
        
        if (this.parameters.forceNumLoners)
        {
            this.forceNumLoners();
        }
        
        System.out.println("Finished reading network " + networkName);
    }
    
    public String getInitialTopology()
    {
        return this.parameters.initialTopology;
    }
    
    public int getSquareHeight()
    {
        return this.parameters.squareHeight;
    }
    
    public int getSquareWidth()
    {
        return this.parameters.squareWidth;
    }
    
    public void printNormalizedIndegrees()
    {
        for(int i = 0; i < this.agents.size(); i++)
        {
            Agent ag = (Agent) this.agents.get(i);
            System.out.println("Agent " + i + ", indegree = " + ag.getNormalizedIndegree());
        }
    }
    
    //print out the indegree of each agent
    public void printIndegrees()
    {
        for(int i = 0; i < this.agents.size(); i++)
        {
            Agent ag = (Agent) this.agents.get(i);
            System.out.println("Agent " + i + ", indegree = " + ag.getIndegree());
        }
    }
    
    public void setRandomTopology()
    {
        //add parameters.numLinks random links
        if (this.parameters.numLinks > this.parameters.numNodes*this.parameters.numNodes)
        {
            System.out.println("Error in setRandomTopology!! Trying to add too many links!");
            return;
        }
        
        int numLinksAdded = 0;
        while (numLinksAdded < this.parameters.numLinks)
        {
            //choose a random pair of agents
            int randomAgent1 = this.uniDistr.nextIntFromTo(0, this.parameters.numNodes-1);
            int randomAgent2 = this.uniDistr.nextIntFromTo(0, this.parameters.numNodes-1);
            
            Agent agent1 = (Agent)this.agents.get(randomAgent1);
            Agent agent2 = (Agent)this.agents.get(randomAgent2);
            
            //create a link from agent1 to agent2
            if (agent1.addNeighbor(agent2))
                numLinksAdded++; //increment if successful (i.e. if agent2 wasn't already a neighbor of agent1)
        }
    }
    
    public void setSquareTopology()
    {
        for(int i = 0; i < this.parameters.numNodes; i++)
        {
            //assemble neighbors of each agent
            ArrayList nbors = new ArrayList();
            
            //calculate coordinates of current agent
            int r = i/this.parameters.squareWidth;
            int c = i%this.parameters.squareWidth;
            
            if (this.debug.printRowAndColumnForEachAgent)
                System.out.println("Agent " + i + ": row = " + r + ", col = " + c);
            
            if (r > 0)
            {
                Agent tempAgent = (Agent) this.agents.get((r-1)*this.parameters.squareWidth + c);
                nbors.add(tempAgent);
            }
            
            if (r < this.parameters.squareHeight-1)
            {
                Agent tempAgent = (Agent) this.agents.get((r+1)*this.parameters.squareWidth + c);
                nbors.add(tempAgent);
            }
            
            if (c > 0)
            {
                Agent tempAgent = (Agent) this.agents.get(r*this.parameters.squareWidth + c-1);
                nbors.add(tempAgent);
            }
            
            if (c < this.parameters.squareWidth-1)
            {
                Agent tempAgent = (Agent) this.agents.get(r*this.parameters.squareWidth + c+1);
                nbors.add(tempAgent);
            }
            
            Agent currAgent = (Agent) this.agents.get(i);
            currAgent.setNeighbors(nbors);
        }
        
        if (this.debug.printGeneral)
            System.out.println("Created initial square topology");
    }
    
    public int[] getAgentPair()
    {
        int[] agentPair;
        if (this.parameters.initialTopology.equalsIgnoreCase("fully connected"))
            agentPair = this.getRandomAgentPair();
        else
        {
            agentPair = new int[2];
            agentPair[0] = -1;
            agentPair[1] = -1;
            Agent firstAgent = null;
            Agent neighborAgent = null;
            while(neighborAgent == null)
            {
                //choose a uniformly random agent
                int randomAgent = this.uniDistr.nextIntFromTo(0, this.parameters.numNodes-1);
                firstAgent = (Agent) this.agents.get(randomAgent);
                
                //choose one of its neighbors
                neighborAgent = firstAgent.getRandomNeighbor();
            }
            
            if (firstAgent == null)
            {
                System.out.println("Agent.getAgentPair(): failed to find agent pair!");
                System.exit(1);
            }
            
            agentPair[0] = firstAgent.getId();
            agentPair[1] = neighborAgent.getId();
        }
        
        this.timestep++;
        return agentPair;
    }
    
    private int[] getRandomAgentPair()
    {
        int[] nodes = new int[2];
        nodes[0] = this.uniDistr.nextIntFromTo(0, this.parameters.numNodes-1);
        nodes[1] = nodes[0];
        while (nodes[1] == nodes[0])
            nodes[1] = this.uniDistr.nextIntFromTo(0, this.parameters.numNodes-1);
        
        return nodes;
    }
    
    private void setSmallWorldTopology()
    {
        //start by setting square topology
        this.setSquareTopology();
        
        //rewire some links randomly
        for(int i = 0; i < this.parameters.numLinksToRewire; i++)
        {
            //choose two agents randomly
            int randomAgent1 = this.uniDistr.nextIntFromTo(0, this.parameters.numNodes-1);
            int randomAgent2 = this.uniDistr.nextIntFromTo(0, this.parameters.numNodes-1);
            
            Agent agent1 = (Agent)this.agents.get(randomAgent1);
            Agent agent2 = (Agent)this.agents.get(randomAgent2);
            
            //get the neighbors of the first agent
            ArrayList nbors = agent1.getNeighbors();
            
            //choose neighbor to drop
            int neighborToDrop = this.uniDistr.nextIntFromTo(0, nbors.size()-1);
            
            //make agent2 the new neighbor of agent1
            nbors.set(neighborToDrop, agent2);
        }
        if (this.debug.printGeneral)
            System.out.println("Created initial small world topology");
    }
    
    private void rmatFindPos(int l, int r, int u, int d)
    {
//        if (this.debug.printRmatLRUD)
//            System.out.println("In rmatFindPos: l=" + l + ", r=" + r + ", u=" + u + ", d=" + d);
        
//        if ((l+1==r && u+1==d) || (l==r && u==d) || (l+1==r && u==d) || (l==r && u+1==d))
        if (l==r && u==d)
        {
            this.rmatFrom = u;
            this.rmatTo = l;
            return;
        }
        
        double p = this.uniDistr.nextDoubleFromTo(0,1);

        if (this.parameters.rmatA > p)
            this.rmatFindPos(l, (l+r)/2, u, (u+d)/2);
        else
        {
            if (this.parameters.rmatA + this.parameters.rmatB > p)
                this.rmatFindPos(l, (l+r)/2, (u+d)/2, d);
            else
            {
                if (this.parameters.rmatA + this.parameters.rmatB + this.parameters.rmatC > p)
                    this.rmatFindPos((l+r)/2, r, u, (u+d)/2);
                else
                    this.rmatFindPos((l+r)/2, r, (u+d)/2, d);
            }
        }
    }
    
    //Create a scale free small world bidirectional network using RMAT
    private void setScaleFreeBidirectionalTopology()
    {
        if (this.debug.printGeneral)
            System.out.println("Creating scale-free small-world bidirectional network using R-MAT.");
        
        SparseDoubleMatrix2D adjmat = new SparseDoubleMatrix2D(this.parameters.numNodes, this.parameters.numNodes);
        adjmat.assign(0.0);
        
        for(int i = 0; i < this.parameters.numLinks; i++)
        {
//            if (this.debug.printGeneral)
//                if (i%100 == 0)
//                    System.out.println("Created " + i + " links.");
            
            this.rmatFindPos(0, this.parameters.numNodes, 0, this.parameters.numNodes);
            if (this.rmatFrom != this.rmatTo)
                adjmat.set(this.rmatFrom, this.rmatTo, 1.0);
        }

        //To make the network bidirectional, we copy the upper triangle to the lower triangle.
        for(int i = 0; i < this.parameters.numNodes; i++)
            for(int j = i+1; j < this.parameters.numNodes; j++)
                adjmat.set(j, i, adjmat.get(i, j));
        
        
        //Now we create actual links between agents according to the adjmat
        for(int i = 0; i < this.parameters.numNodes; i++)
        {
            for(int j = 0; j < this.parameters.numNodes; j++)
            {
                if (adjmat.get(i, j) == 1)
                {
                    Agent fromAgent = (Agent) this.agents.get(i);
                    Agent toAgent = (Agent) this.agents.get(j);
                    fromAgent.addNeighbor(toAgent);
                }
            }
        }
        
        //add a random bidirectional link for any isolate
        this.removeIsolates();
        
        if (this.parameters.forceNumLoners)
        {
            this.forceNumLoners();
        }
        
        if (this.debug.printGeneral)
            System.out.println("Created initial scale-free small-world bidirectional topology.");
    }
    
    //Remove isolates by adding a bidirectional link to a randomly chosen agent
    private void removeIsolates()
    {
        for(int i = 0; i < this.parameters.numNodes; i++)
        {
            Agent ag = (Agent) this.agents.get(i);
            if (ag.getIndegree() == 0 && ag.isLoner()) //ag is an isolate
            {
                int randomAgentIndex = this.uniDistr.nextIntFromTo(0, this.parameters.numNodes-1);
                Agent randomAgent = (Agent)this.agents.get(randomAgentIndex);
                ag.addNeighbor(randomAgent);
                randomAgent.addNeighbor(ag);
            }
            
            if (ag.getIndegree() == 1) //make sure it is not just a self-link
            {
                if (((Agent)ag.getNeighbors().get(0)).getId() == ag.getId())
                {
                    int randomAgentIndex = this.uniDistr.nextIntFromTo(0, this.parameters.numNodes-1);
                    System.out.println("In InteractionManager::removeIsolates, found agent " + ag.getId() + 
                            " with just a self-link. Adding link to randomly chosen agent " + randomAgentIndex);
                    Agent randomAgent = (Agent)this.agents.get(randomAgentIndex);
                    ag.addNeighbor(randomAgent);
                    randomAgent.addNeighbor(ag);
                }
            }
        }
    }
    
    
    //Create a scale free small world network using RMAT
    private void setScaleFreeTopology()
    {
        if (this.debug.printGeneral)
            System.out.println("Creating scale-free small-world network using R-MAT.");
         
        for(int i = 0; i < this.parameters.numLinks; i++)
        {
//            if (this.debug.printGeneral)
//                if (i%100 == 0)
//                    System.out.println("Created " + i + " links.");

            Agent fromAgent, toAgent;
            
            this.rmatFindPos(0, this.parameters.numNodes, 0, this.parameters.numNodes);
            if (this.rmatFrom != this.rmatTo)  //disallow self-loops
            {
                fromAgent = (Agent) this.agents.get(this.rmatFrom);
                toAgent = (Agent) this.agents.get(this.rmatTo);
                fromAgent.addNeighbor(toAgent);
            }
        }
        
        if (this.parameters.forceNumLoners)
        {
            this.forceNumLoners();
        }
        
        if (this.debug.printGeneral)
            System.out.println("Created initial scale-free small-world topology.");
    }
    
    //force the number of loners to be parameters.numLoners
    private void forceNumLoners()
    {
        //create a sorted list of agents by indegree
        ArrayList agentsByIndegree = new ArrayList();
        
        for(int i = 0; i < this.parameters.numNodes; i++)
        {
            int indeg = ((Agent) this.agents.get(i)).getIndegree();
            int index = binarySearch(agentsByIndegree, indeg, 0, agentsByIndegree.size()-1);
            agentsByIndegree.add(index, new Integer(i));
        }
        
        if (this.debug.printAgentsSortedByIndegree)
        {
            System.out.println();
            System.out.println("In InteractionManager::forceNumLoners:");
            System.out.println("Agent   Indegree");
            for(int i = 0; i < agentsByIndegree.size(); i++)
            {
                int agentIndex = ((Integer) agentsByIndegree.get(i)).intValue();
                System.out.println(agentIndex + "\t" + ((Agent) this.agents.get(agentIndex)).getIndegree());
            }
            System.out.println();
        }
        
        int numLonersFound = 0;
        int i = 0;
        
        //skip over nodes with zero indegree
        int agentIndex = ((Integer) agentsByIndegree.get(i)).intValue();
        Agent tempAgent = (Agent) this.agents.get(agentIndex); 
        int indegree = tempAgent.getIndegree();
        while (indegree == 0)
        {
            //if the agent is a loner (in which case it is an isolate), make it not a loner
            if (tempAgent.isLoner())
            {
                int randomAgentIndex = this.uniDistr.nextIntFromTo(0, this.parameters.numNodes-1);
                tempAgent.addNeighbor((Agent)this.agents.get(randomAgentIndex));
            }            
            i++;
            
            //get the next agent
            agentIndex = ((Integer) agentsByIndegree.get(i)).intValue();
            tempAgent = (Agent) this.agents.get(agentIndex); 
            indegree = tempAgent.getIndegree();
        }
        
        while (numLonersFound < this.parameters.numLoners)
        {
            agentIndex = ((Integer) agentsByIndegree.get(i)).intValue();
            tempAgent = (Agent) this.agents.get(agentIndex);
            
            if (tempAgent.isLoner())
                numLonersFound++;
            else
            {
                tempAgent.makeLoner(); //make this agent a loner
                numLonersFound++;
            }
            
            i++;
        }
        
        //turn any remaining loners into non-loners by adding a random outlink
        while (i < this.parameters.numNodes)
        {
            agentIndex = ((Integer) agentsByIndegree.get(i)).intValue();
            tempAgent = (Agent) this.agents.get(agentIndex);
            
            if (tempAgent.isLoner())
            {
                int randomAgentIndex = this.uniDistr.nextIntFromTo(0, this.parameters.numNodes-1);
                tempAgent.addNeighbor((Agent)this.agents.get(randomAgentIndex));
            }
            i++;
        }
    }
    
    private int binarySearch(ArrayList ar, int element, int begin, int end)
    {
//        System.out.println("In binarySearch: begin = " + begin + " end = " + end);
        if (end <= begin)
            return begin;
        int pos = (end + begin)/2;
        Agent tempAgent = (Agent) this.agents.get(((Integer) ar.get(pos)).intValue());
        if (element < tempAgent.getIndegree())
            return binarySearch(ar, element, begin, pos);
        else if (element > tempAgent.getIndegree())
            return binarySearch(ar, element, pos+1, end);
        else
            return pos;
    }
    
    public DenseDoubleMatrix1D getIndegrees()
    {
        this.indegrees.assign(1.0);
        if (this.parameters.initialTopology.equalsIgnoreCase("fully connected"))
            return this.indegrees;

        for(int i = 0; i < this.parameters.numNodes; i++)
        {
            Agent tempAgent = (Agent) this.agents.get(i);
            ArrayList nbors = tempAgent.getNeighbors();
            if (nbors != null)
            {
                for (int j = 0; j < nbors.size(); j++) 
                {
                    Agent nborAgent = (Agent) nbors.get(j);
                    int id = nborAgent.getId();
                    this.indegrees.set(id, this.indegrees.get(id) + 1);
                }
            }
        }
        return this.indegrees;
    }
    
    private void findPayoffMeanAndStdDev()
    {
        //find mean fitness
        this.fitnessMean = 0;
        for(int i = 0; i < this.parameters.numNodes; i++)
        {
            Agent tempAgent = (Agent) this.agents.get(i);
            this.fitnessMean += tempAgent.getPayoff();
        }
        this.fitnessMean /= this.parameters.numNodes;
        
        //find std dev of fitness
        this.fitnessStdDev = 0;
        for(int i = 0; i < this.parameters.numNodes; i++)
        {
            Agent tempAgent = (Agent) this.agents.get(i);
            this.fitnessStdDev += (tempAgent.getPayoff()- this.fitnessMean)*(tempAgent.getPayoff()- this.fitnessMean);
        }
        this.fitnessStdDev /= this.parameters.numNodes;
        this.fitnessStdDev = Math.sqrt(this.fitnessStdDev);
    }
    
    public void saveNetworkToFile(int num)
    {
        //fully connected topology is not written to file
        if (this.parameters.initialTopology.equalsIgnoreCase("fully connected"))
            return;
        
        PrintWriter netWriter = null;
        String filename = this.parameters.networkDir + "network";
        //add the right number of leading zeroes to maintain lexicographic ordering of filenames
        int numZeroes = String.valueOf(this.parameters.maxTimeSteps).length() - String.valueOf(num).length();
        for(int i = 0; i < numZeroes; i++)
            filename += "0";
        filename += num + ".net";
        File netFile = new File(filename);
        
        if (this.debug.printGeneral)
            System.out.println("Writing network to file " + filename);
        
        try {
            netWriter = new PrintWriter(new FileOutputStream(netFile), true);
        } catch(IOException e) {
            System.out.println("Error opening PrintWriter: " + e);
        } 
        
        netWriter.println("*Network IPDSim");
        netWriter.print("*Vertices ");
        netWriter.println(this.parameters.numNodes);
        
        for(int i = 0; i < this.parameters.numNodes; i++) {
            //netWriter.println((i+1) + " " + (i+1) + " ellipse"); //format is:  nodeNum label shape
            netWriter.println((i+1));
        }
        
        netWriter.println("*arcs");
        
        for(int i = 0; i < this.parameters.numNodes; i++) {
            Agent tempAgent = (Agent) this.agents.get(i);
            ArrayList nbors = tempAgent.getNeighbors();
            
            if (nbors != null) 
            {
                for(int j = 0; j < nbors.size(); j++) 
                {
                    Agent tempNbor = (Agent) nbors.get(j);
                    int toNode = tempNbor.getId();
                    netWriter.println((i+1) + " " + (toNode+1) + " 1");
                }
            }
        }
        
        netWriter.close();
    }
}
