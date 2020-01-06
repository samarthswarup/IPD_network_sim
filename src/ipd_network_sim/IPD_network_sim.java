/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ipd_network_sim;

import Agent.Agent;
import InteractionManager.InteractionManager;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author samarthswarup
 */
public class IPD_network_sim {
    Debug debug;
    Parameters parameters;
    
    ArrayList<Agent> agents;
    HashSet zealots;
    
//    Ranmar ranmar;
    MersenneTwister mt;
    Uniform uniDistr;

    InteractionManager im;
    
    public IPD_network_sim(String parameterFile)
    {
        this.debug = new Debug(parameterFile);
        this.parameters = new Parameters(parameterFile);

        Date date = new Date();
        this.mt = new MersenneTwister(date);
        this.uniDistr = new Uniform(this.mt);
        
        //create agents
        if (this.debug.printGeneral)
            System.out.println("Creating agents.");
        
        this.agents = new ArrayList<>(this.parameters.numAgents);
        for(int i = 0; i < this.parameters.numAgents; i++)
            this.agents.add(new Agent(i, this.mt, parameterFile));
        
        if (this.debug.printGeneral)
            System.out.println("Created " + this.parameters.numAgents + " agents.");
        
        this.im = new InteractionManager(parameterFile, this.mt, this.agents);
        this.im.setMaxTimeSteps(this.parameters.maxTimeSteps);
        
        if (this.debug.printGeneral)
            System.out.println("Created Interaction Manager.");

        //set some agents to use the zealot strategy
        if (this.parameters.zealotChoice.equalsIgnoreCase("Random"))
            this.setRandomZealots();
        else if (this.parameters.zealotChoice.equalsIgnoreCase("LowDegree"))
            this.setLowDegreeZealots();
        else if (this.parameters.zealotChoice.equalsIgnoreCase("HighDegree"))
            this.setHighDegreeZealots();
        else
            System.out.println("Unknown defector assignment strategy: " + this.parameters.zealotChoice);
        
        //Inform zealots
        Iterator it = this.zealots.iterator();
        while(it.hasNext())
            ((Agent) this.agents.get((int)it.next())).setZealot();
        
        DenseDoubleMatrix1D sizes = new DenseDoubleMatrix1D(this.parameters.numAgents);
        sizes.assign(1.0);
    }
    
    private void setRandomZealots()
    {
        int numDefectorsAssigned = 0;
        this.zealots = new HashSet<>();
        while (numDefectorsAssigned < this.parameters.numZealots)
        {
            int randomAgentIndex = this.uniDistr.nextIntFromTo(0, this.parameters.numAgents-1);
            Agent randomAgent = this.agents.get(randomAgentIndex);
            if (!randomAgent.getStrategy().equalsIgnoreCase(this.parameters.zealotStrategy))
            {
                randomAgent.setStrategy(this.parameters.zealotStrategy);
                this.zealots.add(randomAgentIndex);
                numDefectorsAssigned++;
            }
        }
    }
    
    private void setLowDegreeZealots()
    {
        TreeMap<Integer, Integer> nodeDegrees = new TreeMap<>();
        for(Agent agent: this.agents)
            nodeDegrees.put(agent.getId(), agent.getNeighbors().size());
        
        Comparator<Map.Entry<Integer, Integer>> byMapValues = new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> left, Map.Entry<Integer, Integer> right) {
                return left.getValue().compareTo(right.getValue());
            }
        };
        
        // create a list of map entries
        List<Map.Entry<Integer, Integer>> nodesAndDegrees = new ArrayList<>();

        // add all nodes and degrees
        nodesAndDegrees.addAll(nodeDegrees.entrySet());

        // sort the collection
        Collections.sort(nodesAndDegrees, byMapValues);
        
        Iterator it = nodesAndDegrees.iterator();
        int numDefectorsAssigned = 0;
        this.zealots = new HashSet<>();
        while(it.hasNext() && numDefectorsAssigned < this.parameters.numZealots)
        {
            Map.Entry<Integer, Integer> nd = (Map.Entry<Integer, Integer>) it.next();
            int agentIndex = nd.getKey();
            Agent chosenAgent = this.agents.get(agentIndex);
            chosenAgent.setStrategy(this.parameters.zealotStrategy);
            this.zealots.add(agentIndex);
            numDefectorsAssigned++;
        }
    }
    
    private void setHighDegreeZealots()
    {
        TreeMap<Integer, Integer> nodeDegrees = new TreeMap<>();
        for(Agent agent: this.agents)
            nodeDegrees.put(agent.getId(), agent.getNeighbors().size());
        
        Comparator<Map.Entry<Integer, Integer>> byMapValues = new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> left, Map.Entry<Integer, Integer> right) {
                return left.getValue().compareTo(right.getValue());
            }
        };
        
        // create a list of map entries
        List<Map.Entry<Integer, Integer>> nodesAndDegrees = new ArrayList<Map.Entry<Integer, Integer>>();

        // add all nodes and degrees
        nodesAndDegrees.addAll(nodeDegrees.entrySet());

        // sort the collection
        Collections.sort(nodesAndDegrees, byMapValues.reversed());
        
        Iterator it = nodesAndDegrees.iterator();
        int numDefectorsAssigned = 0;
        this.zealots = new HashSet<>();
        while(it.hasNext() && numDefectorsAssigned < this.parameters.numZealots)
        {
            Map.Entry<Integer, Integer> nd = (Map.Entry<Integer, Integer>) it.next();
            int agentIndex = nd.getKey();
            Agent chosenAgent = this.agents.get(agentIndex);
            chosenAgent.setStrategy(this.parameters.zealotStrategy);
            this.zealots.add(agentIndex);
            numDefectorsAssigned++;
        }
    }
    
    private void runSimulation()
    {
        PrintWriter outputWriter = null;
        File outputFile = new File(this.parameters.outputFilename);
        
        PrintWriter actionCountsWriter = null;
        File actionCountsFile = new File(this.parameters.actionCountsFilename);
        
        PrintWriter MCCprobWriter = null;
        File MCCprobFile = new File(this.parameters.MCCprobabilitiesFilename);
        
        try {
            outputWriter = new PrintWriter(new FileOutputStream(outputFile), true);
            actionCountsWriter = new PrintWriter(new FileOutputStream(actionCountsFile), true);
            MCCprobWriter = new PrintWriter(new FileOutputStream(MCCprobFile), true);
        } catch(IOException e){
            System.out.println("Error opening PrintWriter: " + e);
            System.exit(1);
        }
        
        int historyLength = this.agents.get(0).getMyOpponentHistoryLength()+1;
        double[] aggPCafterC = new double[historyLength];
        double[] aggPCafterD = new double[historyLength];
        int[] countPCafterC = new int[historyLength];
        int[] countPCafterD = new int[historyLength];
        for (int i = 0; i < historyLength; i++)
        {
            aggPCafterC[i] = 0.0;
            aggPCafterD[i] = 0.0;
            countPCafterC[i] = 0;
            countPCafterD[i] = 0;
        }
        
        outputWriter.write("Round,Player_ID,Player_action,Player_prevAction,Player_aspiration," +
                "Player_epsilon,Player_totalPayoff,Player_pt,Player_pC,Player_totalC," + 
                "Opponent_ID,Opponent_action,Opponent_history_fractionC\n");

//        outputWriter.write("Player1_ID,Player1_action,Player1_totalPayoff,Player1_pCAfterC,"
//                + "Player1_pCAfterD,Player2_ID,Player2_action,Player2_totalPayoff,"
//                + "Player2_pCAfterC,Player2_pCAfterD\n");
        actionCountsWriter.write("Timestep,NumCooperateSoFar,NumDefectSoFar\n");
        
        NumberFormat fm = new DecimalFormat("#0.00");
        
        int numC = 0;
        int numD = 0;
	//run simulation
        for(int i = 0; i < this.parameters.maxTimeSteps; i++)
        {
            //get a random pair of agents
            int[] agentPair = this.im.getAgentPair();
            
            //For the current experiments, using network IPDSim_HSE.net,
            //one of the agents must be agent 0 (agent 1 in the file)
            //To make further computing easy, we make this agent player1.
            if (agentPair[1]==0)
            {
                agentPair[1] = agentPair[0];
                agentPair[0] = 0;
            }
            
            if (agentPair[0] != 0)
            {
                System.out.println("Error! Neither agent was agent 0: [" + agentPair[0] + ", " + agentPair[1] + "].");
                System.exit(1);
            }
            
            Agent player1 = this.agents.get(agentPair[0]);
            Agent player2 = this.agents.get(agentPair[1]);

            String player1PrevAction = player1.getPrevAction();
            String player2PrevAction = player2.getPrevAction();
            
            String player1Action = player1.getAction(); //This updates the previous action, which is why
            String player2Action = player2.getAction(); //we get the previous action first above.
            
            if (!this.zealots.contains(agentPair[0]))
                if (player1Action.equalsIgnoreCase("C"))
                    numC++;
                else
                    numD++;
            
            if (!this.zealots.contains(agentPair[1]))
                if (player2Action.equalsIgnoreCase("C"))
                    numC++;
                else
                    numD++;
            
            //Player_ID,Player_action,Player_totalPayoff,Player_pC,Opponent_ID,Opponent_action
            outputWriter.write((i+1) + "," + agentPair[0] + "," + player1Action + 
                    "," + player1PrevAction + "," + fm.format(player1.getA()) + "," 
                    + fm.format(player1.getEpsilon()) + "," + player1.getPayoff() + 
                    "," + fm.format(player1.getPt()) + "," + 
                    fm.format(player1.getRealizedProbability()) + "," + numC +"," + agentPair[1] 
                    + "," + player2Action + "," + fm.format(player1.getOpponentHistoryFractionC()) 
                    + "\n");
            
            if (player1.getStrategy().equalsIgnoreCase("AspirationLearning") || 
                    player1.getStrategy().equalsIgnoreCase("AspirationLearningConstIncr"))
            {
                if (player1PrevAction.equals("C"))
                {
                    aggPCafterC[(int)((historyLength-1)*player1.getOpponentHistoryFractionC())] += player1.getRealizedProbability();
                    countPCafterC[(int)((historyLength-1)*player1.getOpponentHistoryFractionC())]++;
                }
                else
                {
                    aggPCafterD[(int)((historyLength-1)*player1.getOpponentHistoryFractionC())] += player1.getRealizedProbability();
                    countPCafterD[(int)((historyLength-1)*player1.getOpponentHistoryFractionC())]++;
                }
            }
            
            if (player1.getStrategy().equalsIgnoreCase("Memory2") || player1.getStrategy().equalsIgnoreCase("Memory1") ||
                    player1.getStrategy().equalsIgnoreCase("WinCount"))
            {
                if (player1PrevAction.equals("C"))
                {
                    countPCafterC[(int)((historyLength-1)*player1.getOpponentHistoryFractionC())]++;
                    if (player1Action.equals("C"))
                        aggPCafterC[(int)((historyLength-1)*player1.getOpponentHistoryFractionC())]++;
                }
                else
                {
                    countPCafterD[(int)((historyLength-1)*player1.getOpponentHistoryFractionC())]++;
                    if (player1Action.equals("C"))
                        aggPCafterD[(int)((historyLength-1)*player1.getOpponentHistoryFractionC())]++;
                }
            }
            
//            outputWriter.write(agentPair[0]+ "," + player1Action + "," + player1.getPayoff() 
//                    + "," + fm.format(player1.getPCAfterC()) + "," + fm.format(player1.getPCAfterD()) + ","
//                    + agentPair[1] + "," + player2Action + "," + player2.getPayoff() 
//                    + "," + fm.format(player2.getPCAfterC()) + "," + fm.format(player2.getPCAfterD()) + "\n");
            
            if (player1.isZealot() && player2.isZealot())
            {
                player1.updatePayoffButNotHistory(player2Action);
                player2.updatePayoffButNotHistory(player1Action);
            }
            else
            {
                player1.updatePayoff(player2Action);
                player2.updatePayoff(player1Action);
            }
            
            if (i%this.parameters.timeStepsToPrint == 0)
            {
                System.out.println("Time step " + i);
                actionCountsWriter.write(i+","+numC+","+numD+"\n");
            }
            
            if (i%60 == 0)
            {
                player1.resetMemory2Parameters();
                player1.resetAspirationLearningParameters();
                player1.resetWinCountParameters();
            }
            
        }
        actionCountsWriter.write("10000,"+numC+","+numD+"\n");
        outputWriter.close();
        actionCountsWriter.close();
        this.writeDegreeAndFractionCToFile();

        MCCprobWriter.write("FractionC,PCafterC,PCafterD\n");
        
        for(int i = 0; i < historyLength; i++)
        {
            if (countPCafterC[i] > 0)
                aggPCafterC[i] /= countPCafterC[i];
            if (countPCafterD[i] > 0)
                aggPCafterD[i] /= countPCafterD[i];
            MCCprobWriter.write((double)i/(historyLength-1) + "," + aggPCafterC[i] + "," + aggPCafterD[i] + "\n");
        }
        
        MCCprobWriter.close();
    }
    
    private void writeDegreeAndFractionCToFile()
    {
        PrintWriter degAndFracCWriter = null;
        File degAndFracCFile = new File(this.parameters.degreeAndFractionCFilename);
        
        try {
            degAndFracCWriter = new PrintWriter(new FileOutputStream(degAndFracCFile), true);
        } catch(IOException e){
            System.out.println("Error opening PrintWriter: " + e);
            System.exit(1);
        }
        
        degAndFracCWriter.write("id,degree,numActions,numC,fractionC,zealot?\n");
        for (Agent agent : this.agents)
            degAndFracCWriter.write(agent.getId() + "," + agent.getNeighbors().size() + "," + 
                    agent.getNumActions() + "," + agent.getNumC() + "," + agent.getFractionC() +
                    "," + this.zealots.contains(agent.getId()) + "\n");
        degAndFracCWriter.close();
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 1)
        {
            System.out.println("Usage: java IPD_network_sim <parameters file>");
            System.exit(1);
        }
        IPD_network_sim ipdSim = new IPD_network_sim(args[0]);
        ipdSim.runSimulation();
    }
    
}
