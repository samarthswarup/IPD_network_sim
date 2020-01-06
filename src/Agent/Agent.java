/*
 * Agent.java
 *
 * Created on May 1, 2007, 8:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Agent;

import cern.jet.random.Uniform;
import cern.jet.random.Empirical;
import cern.jet.random.EmpiricalWalker;
import cern.jet.random.engine.MersenneTwister;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayDeque;
//import edu.cornell.lassp.houle.RngPack.Ranmar;

import java.util.ArrayList;
import java.util.Iterator;
/**
 *
 * @author Samarth Swarup
 */
public class Agent {
    
    Debug debug;
    Parameters parameters;
    
    int id;
    
    String currentStrategy;
    ArrayDeque myPrevActions;
    ArrayDeque opponentPrevActions;
    int currentPayoff;
    ArrayDeque myPrevPayoffs;
    boolean zealot; //true if this agent is considered a zealot
    
    //for Moody Conditional Cooperation
    double pCAfterC; //the probability of cooperation if the previous action was C
    double pCAfterD; //the probability of cooperation if the previous action was D
    
    //for the Aspiration Learning model
    double pt; //the probability of cooperation at time t (current time step)
    
    //for the Memory2 model
    double pc; //the probability of cooperation at time t (current time step)
    double pCGivenC; //the estimated probability opponent will cooperate given subject's previous action was C
    double pCGivenD; //the estimated probability opponent will cooperate given subject's previous action was D
    
    //for the WinCount model
    double pCoopWinCount;
    
    int totalNumActions;
    int totalNumC;
    
//    Ranmar ranmar;
    MersenneTwister mt;
    Uniform uniDistr;
    EmpiricalWalker empirical;
    
    ArrayList neighbors; //neighbor nodes, i.e. agents from whom information is obtained
    
    int indegree; //the number of nodes pointing to this one
    double normalizedIndegree; //the indegree of this node divided by the sum of the indegrees of itself and all its neighbors
    
    /** Creates a new instance of Agent */
//    public Agent(int _id, Ranmar r, String parameterFile) {
    public Agent(int _id, MersenneTwister m, String parameterFile)  {
        this.debug = new Debug(parameterFile);
        this.parameters = new Parameters(parameterFile);
        this.id = _id;
        
        this.mt = m;
        this.uniDistr = new Uniform(this.mt);
        
        this.indegree = 0;
        
        this.currentPayoff = 0;
        this.currentStrategy = this.parameters.strategy;
        this.myPrevActions = new ArrayDeque<>(this.parameters.myHistoryLength);
//        for(int i = 0; i < this.parameters.myHistoryLength; i++)
//            if (this.uniDistr.nextBoolean())
//                this.myPrevActions.offer(1);
//            else
//                this.myPrevActions.offer(0);

        this.opponentPrevActions = new ArrayDeque<>(this.parameters.opponentHistoryLength);
//        for(int i = 0; i < this.parameters.opponentHistoryLength; i++)
//            if (this.uniDistr.nextBoolean())
//                this.opponentPrevActions.offer(1);
//            else
//                this.opponentPrevActions.offer(0);
        
        this.myPrevPayoffs = new ArrayDeque<>(this.parameters.myHistoryLength);
//        for(int i = 0; i < this.parameters.myHistoryLength; i++)
//            this.myPrevPayoffs.offer(this.parameters.A); //set history to aspiration level

        this.pCAfterC = (double)(this.parameters.maxPCAfterC + this.parameters.minPCAfterC)/2.0;
        this.pCAfterD = (double)(this.parameters.maxPCAfterD + this.parameters.minPCAfterD)/2.0;
        
        this.totalNumActions = 0;
        this.totalNumC = 0;
        
        this.neighbors = null;
        
        this.pt = this.parameters.initialPt;
        
        this.pc = this.parameters.initialPC;
        this.pCGivenC = this.parameters.initialPC;
        this.pCGivenD = 1-this.parameters.initialPC;
        
        this.pCoopWinCount = this.parameters.initialPC;
    }

    public void setZealot()
    {
        this.zealot = true;
    }
    
    public boolean isZealot()
    {
        return this.zealot;
    }
    
    public String getPrevAction()
    {
        if (this.myPrevActions.size() == 0)
            return "C";
        
        if ((Integer) this.myPrevActions.peekLast()==1)
            return "C";
        else
            return "D";
    }
    
    public int getMyHistoryLength()
    {
        return this.parameters.myHistoryLength;
    }
    
    public int getMyOpponentHistoryLength()
    {
        return this.parameters.opponentHistoryLength;
    }
    
    public double getA()
    {
        return this.parameters.A;
    }
    
    public double getEpsilon()
    {
        return this.parameters.epsilon;
    }
    
    public String getAction()
    {
        String action;
        switch (this.currentStrategy) 
        {
            case "AlwaysCooperate":
                this.myPrevActions.poll();
                this.myPrevActions.offer(1);
                action = "C";
                break;
            case "AlwaysDefect":
                this.myPrevActions.poll();
                this.myPrevActions.offer(0);
                action = "D";
                break;
            case "TitForTat":
                action = this.titForTatAction();
                break;
            case "TatForTit":
                action = this.tatForTitAction();
                break;
            case "TitForTatWithHistory":
                action = this.titForTatWithHistoryAction();
                break;
            case "TatForTitWithHistory":
                action = this.tatForTitWithHistoryAction();
                break;
            case "MoodyConditionalCooperation":
                action = this.moodyConditionalCooperationAction();
                break;
            case "AspirationLearning":
                action = this.aspirationLearningAction();
                break;
            case "AspirationLearningConstIncr":
                action = this.aspirationLearningAction(); //Action function is the same as for AspirationLearning
                break;
            case "Memory1":
                action = this.memory2Action(); //Action function is the same as for Memory2
                break;
            case "Memory2":
                action = this.memory2Action();
                break;
            case "WinCount":
                action = this.winCountAction();
                break;
            case "Random":
                action = this.randomAction();
                break;
            default:
                System.out.println("Unknown strategy: " + this.currentStrategy);
                action = "C";
        }
        
        if (action.equalsIgnoreCase("C"))
            this.totalNumC++;
        this.totalNumActions++;
        
        if (this.debug.printZealotAction)
            if (this.zealot)
                System.out.println("Agent " + this.id + ": Strategy = " + this.currentStrategy +
                        ", Action = "+ action);
        
        return action;
    }
    
    private String winCountAction()
    {
        int actionCode;
        
        Iterator opp = this.opponentPrevActions.iterator();
        Iterator my = this.myPrevActions.iterator();
        
        int historyLength = this.opponentPrevActions.size();
        
        int CWinCount = 0;
        int DWinCount = 0;
        while (opp.hasNext()) 
            if ((Integer)opp.next()==1)
                if ((Integer)my.next()==1)
                    CWinCount++;
                else
                    DWinCount++;

        this.pCoopWinCount = this.parameters.initialPC;
//        if (CWinCount + DWinCount > 0)
//            this.pCoopWinCount = (double)CWinCount/(CWinCount+DWinCount);

        if (CWinCount + DWinCount > 0)
            if (CWinCount >= DWinCount)
                this.pCoopWinCount = 1.0;
            else
                this.pCoopWinCount = 0.0;

//        if (CWinCount + DWinCount > 0)
//            this.pCoopWinCount = (double)(CWinCount-DWinCount+historyLength)/(historyLength*2);

        if (this.uniDistr.nextDouble() < this.pCoopWinCount)
            actionCode = 1;
        else
            actionCode = 0;
        
        if (this.uniDistr.nextDouble() < this.parameters.epsilon)
            actionCode = 1-actionCode;
        
        if (this.myPrevActions.size() == this.parameters.myHistoryLength)
            this.myPrevActions.poll();
        this.myPrevActions.offer(actionCode);
            
        if ((int)this.myPrevActions.peekLast() == 1)
            return "C";
        else
            return "D";
    }
    
    private String randomAction()
    {
        int actionCode;
        
        if (this.uniDistr.nextDouble() < this.parameters.pCooperate)
            actionCode = 1;
        else
            actionCode = 0;
        
        if (this.myPrevActions.size() == this.parameters.myHistoryLength)
            this.myPrevActions.poll();
        this.myPrevActions.offer(actionCode);
            
        if ((int)this.myPrevActions.peekLast() == 1)
            return "C";
        else
            return "D";
    }
    
    public double getOpponentHistoryFractionC()
    {
        double fracC = 0.0;
        
        for(Object b : this.opponentPrevActions) {
           fracC += (Integer)b;
        }
        
        return fracC/this.parameters.opponentHistoryLength;
    }
    
    private String memory2Action()
    {
        int actionCode;
        
        double pC = this.pc*(1-this.parameters.epsilon) + (1-this.pc)*this.parameters.epsilon;
        
        if (this.uniDistr.nextDouble() < pC)
            actionCode = 1;
        else
            actionCode = 0;
        
        if (this.myPrevActions.size() >= 2)
            this.myPrevActions.poll();
        this.myPrevActions.offer(actionCode);
            
        if ((int)this.myPrevActions.peekLast() == 1)
            return "C";
        else
            return "D";
    }
    
    public void resetWinCountParameters()
    {
        this.currentPayoff = 0;
        this.myPrevActions = new ArrayDeque<>(this.parameters.myHistoryLength);
        this.opponentPrevActions = new ArrayDeque<>(this.parameters.opponentHistoryLength);
        this.myPrevPayoffs = new ArrayDeque<>(this.parameters.myHistoryLength);
    }
    
    public void resetAspirationLearningParameters()
    {
        this.currentPayoff = 0;
        this.myPrevActions = new ArrayDeque<>(this.parameters.myHistoryLength);
//        for(int i = 0; i < this.parameters.myHistoryLength; i++)
//            if (this.uniDistr.nextBoolean())
//                this.myPrevActions.offer(1);
//            else
//                this.myPrevActions.offer(0);

        this.opponentPrevActions = new ArrayDeque<>(this.parameters.opponentHistoryLength);
//        for(int i = 0; i < this.parameters.opponentHistoryLength; i++)
//            if (this.uniDistr.nextBoolean())
//                this.opponentPrevActions.offer(1);
//            else
//                this.opponentPrevActions.offer(0);
        
        this.myPrevPayoffs = new ArrayDeque<>(this.parameters.myHistoryLength);
//        for(int i = 0; i < this.parameters.myHistoryLength; i++)
//            this.myPrevPayoffs.offer(this.parameters.A); //set history to aspiration level
        
        this.pt = this.parameters.initialPt;
    }
    
    public void resetMemory2Parameters()
    {
        this.pc = this.parameters.initialPC;
        this.pCGivenC = this.parameters.initialPC;
        this.pCGivenD = 1-this.parameters.initialPC;
    }
    
    //This returns the actual probability of cooperation 
    public double getRealizedProbability()
    {
        switch (this.currentStrategy)
        {
            case "AspirationLearning":
                return this.pt*(1-this.parameters.epsilon) + (1-this.pt)*this.parameters.epsilon;
            case "AspirationLearningConstIncr":
                return this.pt*(1-this.parameters.epsilon) + (1-this.pt)*this.parameters.epsilon;
            case "Memory1":
                return this.pc*(1-this.parameters.epsilon) + (1-this.pc)*this.parameters.epsilon;
            case "Memory2":
                return this.pc*(1-this.parameters.epsilon) + (1-this.pc)*this.parameters.epsilon;
            case "WinCount":
                return this.pCoopWinCount*(1-this.parameters.epsilon) + (1-this.pCoopWinCount)*this.parameters.epsilon;
            default:
                return -1.0;
        }        
    }
    
    public double getPt()
    {
        return this.pt;
    }
    
    private String aspirationLearningAction()
    {
        int actionCode;
        
        double pC = this.pt*(1-this.parameters.epsilon) + (1-this.pt)*this.parameters.epsilon;
        
        if (this.uniDistr.nextDouble() < pC)
            actionCode = 1;
        else
            actionCode = 0;
        
        if (this.myPrevActions.size() == this.parameters.myHistoryLength)
            this.myPrevActions.poll();
        this.myPrevActions.offer(actionCode);
            
        if ((int)this.myPrevActions.peekLast() == 1)
            return "C";
        else
            return "D";
    }
    
    private String titForTatAction()
    {
        int opponentActionCode = 1;
        if (this.opponentPrevActions.size() > 0)
            opponentActionCode = (int)this.opponentPrevActions.peekLast();

        if (this.myPrevActions.size() == this.parameters.myHistoryLength)
            this.myPrevActions.poll();
        this.myPrevActions.offer(opponentActionCode);
            
        if ((int)this.myPrevActions.peekLast() == 1)
            return "C";
        else
            return "D";
    }
    
    //TODO: Fix for case where histories are initialized to null
    private String titForTatWithHistoryAction()
    {
        int oppCount = 0;
        Iterator opp = this.opponentPrevActions.iterator();
        while (opp.hasNext()) 
            oppCount += (int) opp.next();
        
        if(oppCount >= this.parameters.opponentHistoryLength/2.0) //number of C actions is greater than number of D actions in myPrevActions
        {
            this.myPrevActions.poll();
            this.myPrevActions.offer(1);
            return "C";
        }
        else
        {
            this.myPrevActions.poll();
            this.myPrevActions.offer(0);
            return "D";
        }
    }
    
    //Opposite of tit for tat
    private String tatForTitAction()
    {
        int opponentActionCode = 1;
        if (this.opponentPrevActions.size() > 0)
            opponentActionCode = (int)this.opponentPrevActions.peekLast();

        if (this.myPrevActions.size() == this.parameters.myHistoryLength)
            this.myPrevActions.poll();
        this.myPrevActions.offer(1-opponentActionCode);
            
        if ((int)this.myPrevActions.peekLast() == 1)
            return "C";
        else
            return "D";
    }
    
    //TODO: Fix for case where histories are initialized to null
    private String tatForTitWithHistoryAction()
    {
        int oppCount = 0;
        Iterator opp = this.opponentPrevActions.iterator();
        while (opp.hasNext()) 
            oppCount += (int) opp.next();
        
        String action;
        if(oppCount >= this.parameters.opponentHistoryLength/2.0) //number of C actions is greater than number of D actions in myPrevActions
        {
            this.myPrevActions.poll();
            this.myPrevActions.offer(0);
            action = "D";
        }
        else
        {
            this.myPrevActions.poll();
            this.myPrevActions.offer(1);
            action = "C";
        }
        
        return action;
    }
    
    public double getFractionC()
    {
        return (double) this.totalNumC/this.totalNumActions;
    }
    
    public int getNumActions()
    {
        return this.totalNumActions;
    }
    
    public int getNumC()
    {
        return this.totalNumC;
    }
    
    private String moodyConditionalCooperationAction()
    {
        int actionCode;
        
        int count = 0;
        Iterator it = this.myPrevActions.iterator();
        while(it.hasNext())
            count+= (int)it.next();

        int oppCount = 0;
        Iterator opp = this.opponentPrevActions.iterator();
        while (opp.hasNext()) 
            oppCount += (int) opp.next();
        
        if(count >= this.parameters.myHistoryLength/2.0) //number of C actions is greater than number of D actions in myPrevActions
        {
            this.pCAfterC = this.parameters.minPCAfterC + (this.parameters.maxPCAfterC - 
                    this.parameters.minPCAfterC)*((double)oppCount/this.parameters.opponentHistoryLength);
            
            if (this.uniDistr.nextDouble() < this.pCAfterC)
                actionCode = 1;
            else
                actionCode = 0;
        }
        else
        {
            this.pCAfterD = this.parameters.maxPCAfterD - (this.parameters.maxPCAfterD - 
                    this.parameters.minPCAfterD)*((double)oppCount/this.parameters.opponentHistoryLength);
            
            if (this.uniDistr.nextDouble() < this.pCAfterD)
                actionCode = 1;
            else
                actionCode = 0;
        }
       
        if (this.myPrevActions.size() == this.parameters.myHistoryLength)
            this.myPrevActions.poll();
        this.myPrevActions.offer(actionCode);
            
        if ((int)this.myPrevActions.peekLast() == 1)
            return "C";
        else
            return "D";
    }
    
    public void updatePayoff(String opponentAction)
    {
        switch (this.currentStrategy)
        {
            case "TitForTat":
                this.updatePayoffTitForTat(opponentAction);
                break;
            case "TatForTit":
                this.updatePayoffTatForTit(opponentAction);
                break;
            case "AspirationLearning":
                this.updatePayoffAspiration(opponentAction);
                break;
            case "AspirationLearningConstIncr":
                this.updatePayoffAspirationConstIncr(opponentAction);
                break;
            case "Memory1":
                this.updatePayoffMemory1(opponentAction);
                break;
            case "Memory2":
                this.updatePayoffMemory2(opponentAction);
                break;
            case "WinCount":
                this.updatePayoffWinCount(opponentAction);
                break;
            default:
                //System.out.println("Update payoff method not implemented for strategy " + this.currentStrategy);
        }
    }
    
    public void updatePayoffTitForTat(String opponentAction)
    {
        String combinedAction;
        
        if ((int)this.myPrevActions.peekLast()==1) 
            combinedAction = "C" + opponentAction;
        else
            combinedAction = "D" + opponentAction;
        
        double r = 0;
        switch (combinedAction)
        {
            case "CC":
                r = this.parameters.CCpayoff;
                break;
            case "CD":
                r = this.parameters.CDpayoff;
                break;
            case "DC":
                r = this.parameters.DCpayoff;
                break;
            case "DD":
                r = this.parameters.DDpayoff;
                break;
            default:
                System.out.println("Unknown pair of actions: " + combinedAction + 
                        "; opponentAction = " + opponentAction);
        }
        
        this.currentPayoff += r;
        if (this.myPrevPayoffs.size() == this.parameters.myHistoryLength)
            this.myPrevPayoffs.poll();
        this.myPrevPayoffs.offer(r);
        
        int opponentActionCode = 1;
        if (opponentAction.equalsIgnoreCase("D"))
            opponentActionCode = 0;
        
        if (this.opponentPrevActions.size() == this.parameters.opponentHistoryLength)
            this.opponentPrevActions.poll();
        this.opponentPrevActions.offer(opponentActionCode);
    }
    
    public void updatePayoffTatForTit(String opponentAction)
    {
        String combinedAction;
        
        if ((int)this.myPrevActions.peekLast()==1) 
            combinedAction = "C" + opponentAction;
        else
            combinedAction = "D" + opponentAction;
        
        double r = 0;
        switch (combinedAction)
        {
            case "CC":
                r = this.parameters.CCpayoff;
                break;
            case "CD":
                r = this.parameters.CDpayoff;
                break;
            case "DC":
                r = this.parameters.DCpayoff;
                break;
            case "DD":
                r = this.parameters.DDpayoff;
                break;
            default:
                System.out.println("Unknown pair of actions: " + combinedAction + 
                        "; opponentAction = " + opponentAction);
        }
        
        this.currentPayoff += r;
        if (this.myPrevPayoffs.size() == this.parameters.myHistoryLength)
            this.myPrevPayoffs.poll();
        this.myPrevPayoffs.offer(r);
        
        int opponentActionCode = 1;
        if (opponentAction.equalsIgnoreCase("D"))
            opponentActionCode = 0;
        
        if (this.opponentPrevActions.size() == this.parameters.opponentHistoryLength)
            this.opponentPrevActions.poll();
        this.opponentPrevActions.offer(opponentActionCode);
    }
    
    public void updatePayoffWinCount(String opponentAction)
    {
        String combinedAction;
        
        if ((int)this.myPrevActions.peekLast()==1) 
            combinedAction = "C" + opponentAction;
        else
            combinedAction = "D" + opponentAction;
        
        double r = 0;
        switch (combinedAction)
        {
            case "CC":
                r = this.parameters.CCpayoff;
                break;
            case "CD":
                r = this.parameters.CDpayoff;
                break;
            case "DC":
                r = this.parameters.DCpayoff;
                break;
            case "DD":
                r = this.parameters.DDpayoff;
                break;
            default:
                System.out.println("Unknown pair of actions: " + combinedAction + 
                        "; opponentAction = " + opponentAction);
        }
        
        this.currentPayoff += r;
        if (this.myPrevPayoffs.size() == this.parameters.myHistoryLength)
            this.myPrevPayoffs.poll();
        this.myPrevPayoffs.offer(r);
        
        int opponentActionCode = 1;
        if (opponentAction.equalsIgnoreCase("D"))
            opponentActionCode = 0;
        
        if (this.debug.printOpponentHistory)
        {
            if (this.zealot)
            {
                Iterator opp = this.opponentPrevActions.iterator();
                String opponentHistory = "";
                while (opp.hasNext()) 
                    opponentHistory += opp.next();
                System.out.println("Agent " + this.id + ": Strategy = " + this.currentStrategy 
                        + "Opponent history = " + opponentHistory);
            }
        }
        
        if (this.opponentPrevActions.size() == this.parameters.opponentHistoryLength)
            this.opponentPrevActions.poll();
        this.opponentPrevActions.offer(opponentActionCode);
    }
    
    public void updatePayoffMemory1(String opponentAction)
    {
        String combinedAction;
        
        if ((int)this.myPrevActions.peekLast()==1) 
            combinedAction = "C" + opponentAction;
        else
            combinedAction = "D" + opponentAction;
        
//        double r = 0;
        switch (combinedAction)
        {
            case "CC":
//                r = this.parameters.CCpayoff;
                this.pCGivenC += this.parameters.PCgivenCincr;
                break;
            case "CD":
//                r = this.parameters.CDpayoff;
                this.pCGivenC -= this.parameters.PCgivenCincr;
                break;
            case "DC":
//                r = this.parameters.DCpayoff;
                this.pCGivenD += this.parameters.PCgivenDincr;
                break;
            case "DD":
//                r = this.parameters.DDpayoff;
                this.pCGivenD -= this.parameters.PCgivenDincr;
                break;
            default:
                System.out.println("Unknown pair of actions: " + combinedAction + 
                        "; opponentAction = " + opponentAction);
        }
        
//        this.currentPayoff += r;
//        this.myPrevPayoffs.poll();
//        this.myPrevPayoffs.offer(r);
        
        if (this.pCGivenC > 1.0)
            this.pCGivenC = 1.0;
        if (this.pCGivenC < 0.0)
            this.pCGivenC = 0.0;
        
        if (this.pCGivenD > 1.0)
            this.pCGivenD = 1.0;
        if (this.pCGivenD < 0.0)
            this.pCGivenD = 0.0;
        
        this.parameters.PCgivenCincr *= this.parameters.decayRate;
        this.parameters.PCgivenDincr *= this.parameters.decayRate;
        
        //Update the probability that this agent will cooperate in the next round
//        this.pc = 1-this.pCGivenD;
//        if (this.pCGivenC > this.pCGivenD)
//            this.pc = this.pCGivenC;

//        this.pc = 0.0;
//        if (this.pCGivenC >= this.pCGivenD)
//        {
//            if (this.pCGivenC >= 0.5)
//                this.pc = 1.0;
//        }
        
        this.pc = this.pCGivenD;
        if (this.pCGivenC >= this.pCGivenD)
            this.pc = this.pCGivenC;
        
        if (this.parameters.adaptiveEpsilon)
        {
            if (opponentAction.equalsIgnoreCase("C"))
            {
                this.parameters.epsilon -= 0.01;
                if (this.parameters.epsilon < 0.0)
                    this.parameters.epsilon = 0.0;
            }
            else
            {
                this.parameters.epsilon += 0.01;
                if (this.parameters.epsilon > 0.2)
                    this.parameters.epsilon = 0.2;
            }
        }   
        
        if (this.debug.printMemory2UpdateDetails)
        {
            NumberFormat fm = new DecimalFormat("#0.000");
            System.out.println("Memory2 update: " + combinedAction + " pCGivenC=" + fm.format(this.pCGivenC) + ","
            + " pCGivenD=" + fm.format(this.pCGivenD) + ", pc=" + fm.format(this.pc));
        }
        
        int opponentActionCode = 1;
        if (opponentAction.equalsIgnoreCase("D"))
            opponentActionCode = 0;
        
        if (this.debug.printOpponentHistory)
        {
            if (this.zealot)
            {
                Iterator opp = this.opponentPrevActions.iterator();
                String opponentHistory = "";
                while (opp.hasNext()) 
                    opponentHistory += opp.next();
                System.out.println("Agent " + this.id + ": Strategy = " + this.currentStrategy 
                        + "Opponent history = " + opponentHistory);
            }
        }
        
        if (this.opponentPrevActions.size() == this.parameters.opponentHistoryLength)
            this.opponentPrevActions.poll();
        this.opponentPrevActions.offer(opponentActionCode);
    }
    
    public void updatePayoffMemory2(String opponentAction)
    {
        String combinedAction;
        
        if ((int)this.myPrevActions.peekFirst()==1) //assumes myPrevActions.size()==2
            combinedAction = "C" + opponentAction;
        else
            combinedAction = "D" + opponentAction;
        
//        double r = 0;
        switch (combinedAction)
        {
            case "CC":
//                r = this.parameters.CCpayoff;
                this.pCGivenC += this.parameters.PCgivenCincr;
                break;
            case "CD":
//                r = this.parameters.CDpayoff;
                this.pCGivenC -= this.parameters.PCgivenCincr;
                break;
            case "DC":
//                r = this.parameters.DCpayoff;
                this.pCGivenD += this.parameters.PCgivenDincr;
                break;
            case "DD":
//                r = this.parameters.DDpayoff;
                this.pCGivenD -= this.parameters.PCgivenDincr;
                break;
            default:
                System.out.println("Unknown pair of actions: " + combinedAction + 
                        "; opponentAction = " + opponentAction);
        }
        
//        this.currentPayoff += r;
//        this.myPrevPayoffs.poll();
//        this.myPrevPayoffs.offer(r);
        
        if (this.pCGivenC > 1.0)
            this.pCGivenC = 1.0;
        if (this.pCGivenC < 0.0)
            this.pCGivenC = 0.0;
        
        if (this.pCGivenD > 1.0)
            this.pCGivenD = 1.0;
        if (this.pCGivenD < 0.0)
            this.pCGivenD = 0.0;
        
        this.parameters.PCgivenCincr *= this.parameters.decayRate;
        this.parameters.PCgivenDincr *= this.parameters.decayRate;
        
        //Update the probability that this agent will cooperate in the next round
//        this.pc = 1-this.pCGivenD;
//        if (this.pCGivenC > this.pCGivenD)
//            this.pc = this.pCGivenC;

        this.pc = 0.0;
        if (this.pCGivenC >= this.pCGivenD)
        {
            if (this.pCGivenC >= 0.5)
                this.pc = 1.0;
        }
        
        if (this.parameters.adaptiveEpsilon)
        {
            if (opponentAction.equalsIgnoreCase("C"))
            {
                this.parameters.epsilon -= 0.01;
                if (this.parameters.epsilon < 0.0)
                    this.parameters.epsilon = 0.0;
            }
            else
            {
                this.parameters.epsilon += 0.01;
                if (this.parameters.epsilon > 0.2)
                    this.parameters.epsilon = 0.2;
            }
        }   
        
        if (this.debug.printMemory2UpdateDetails)
        {
            NumberFormat fm = new DecimalFormat("#0.000");
            System.out.println("Memory2 update: " + combinedAction + " pCGivenC=" + fm.format(this.pCGivenC) + ","
            + " pCGivenD=" + fm.format(this.pCGivenD) + ", pc=" + fm.format(this.pc));
        }
        
        int opponentActionCode = 1;
        if (opponentAction.equalsIgnoreCase("D"))
            opponentActionCode = 0;
        
        if (this.debug.printOpponentHistory)
        {
            if (this.zealot)
            {
                Iterator opp = this.opponentPrevActions.iterator();
                String opponentHistory = "";
                while (opp.hasNext()) 
                    opponentHistory += opp.next();
                System.out.println("Agent " + this.id + ": Strategy = " + this.currentStrategy 
                        + "Opponent history = " + opponentHistory);
            }
        }
        
        if (this.opponentPrevActions.size() == this.parameters.opponentHistoryLength)
            this.opponentPrevActions.poll();
        this.opponentPrevActions.offer(opponentActionCode);
    }
    
    public void updatePayoffAspiration(String opponentAction)
    {
        String combinedAction;
        
        if ((int)this.myPrevActions.peekLast()==1)
            combinedAction = "C" + opponentAction;
        else
            combinedAction = "D" + opponentAction;
        
        double r = 0;
        switch (combinedAction)
        {
            case "CC":
                r = this.parameters.CCpayoff;
                break;
            case "CD":
                r = this.parameters.CDpayoff;
                break;
            case "DC":
                r = this.parameters.DCpayoff;
                break;
            case "DD":
                r = this.parameters.DDpayoff;
                break;
            default:
                System.out.println("Unknown pair of actions: " + combinedAction + 
                        "; opponentAction = " + opponentAction);
        }
        
        this.currentPayoff += r;
        if (this.myPrevPayoffs.size() > this.parameters.myHistoryLength)
            this.myPrevPayoffs.poll();
        this.myPrevPayoffs.offer(r);
        
        double avgPayoff = 0.0;
        Iterator payoffIter = this.myPrevPayoffs.iterator();
        while (payoffIter.hasNext())
            avgPayoff += (double) payoffIter.next();
        avgPayoff /= this.parameters.myHistoryLength;
        
        //The learning model below is taken from
        //Ezaki T, Horita Y, Takezawa M, Masuda N (2016) Reinforcement Learning Explains Conditional
        //Cooperation and Its Moody Cousin. PLoS Comput Biol 12(7): e1005034. doi:10.1371/journal.pcbi.1005034
        double s = Math.tanh(this.parameters.beta * (avgPayoff - this.parameters.A));
        
        //Adapting the value of epsilon is not in the above paper.
        if (this.parameters.adaptiveEpsilon)
        {
            if (avgPayoff > this.parameters.A)
            {
                this.parameters.epsilon -= 0.01;
                if (this.parameters.epsilon < 0.0)
                    this.parameters.epsilon = 0.0;
            }
            else
            {
                this.parameters.epsilon += 0.01;
                if (this.parameters.epsilon > 0.2)
                    this.parameters.epsilon = 0.2;
            }
        }   
        
        //Adapting the value of A is not in the above paper.
        if (this.parameters.adaptiveA)
        {
            if (s>=0)
//                this.parameters.h *= this.parameters.decayRate;
                this.parameters.h = 1/((1/this.parameters.h)+1);
            else
            {
                if (this.parameters.h < 1.0)
                    this.parameters.h = 1/((1/this.parameters.h)-1);
                else
                    this.parameters.h = 1.0;
            }
            this.parameters.A = (1-this.parameters.h)*this.parameters.A + this.parameters.h*avgPayoff;
        }
        
        if (this.debug.printAspirationLearningUpdateDetails)
        {
            if (this.id==0)
            {
                System.out.println("Aspiration learning update:");
                System.out.println("p(t-1)=" + this.pt);
                System.out.println("a(t-1)=" + combinedAction.substring(0, 1));
                System.out.println("Avg payoff=" + avgPayoff);
                System.out.println("s(t-1)=" + s);
            }
        }
        
        if (s >= 0)
        {
            if (combinedAction.equals("CC") || combinedAction.equals("CD"))
                this.pt += (1-this.pt)*s;
            else
                this.pt -= pt*s;
        }
        else
        {
            if (combinedAction.equals("CC") || combinedAction.equals("CD"))
                this.pt += pt*s;
            else
                this.pt -= (1-this.pt)*s;
        }
        
        if (this.debug.printAspirationLearningUpdateDetails)
            if (this.id==0)
                System.out.println("p(t)=" + this.pt);
        
        int opponentActionCode = 1;
        if (opponentAction.equalsIgnoreCase("D"))
            opponentActionCode = 0;
        
        if (this.debug.printOpponentHistory)
        {
            if (this.zealot)
            {
                Iterator opp = this.opponentPrevActions.iterator();
                String opponentHistory = "";
                while (opp.hasNext()) 
                    opponentHistory += opp.next();
                System.out.println("Agent " + this.id + ": Strategy = " + this.currentStrategy 
                        + "Opponent history = " + opponentHistory);
            }
        }
        
        if (this.opponentPrevActions.size() == this.parameters.opponentHistoryLength)
            this.opponentPrevActions.poll();
        this.opponentPrevActions.offer(opponentActionCode);
    }
    
    public void updatePayoffAspirationConstIncr(String opponentAction)
    {
        String combinedAction;
        
        if ((int)this.myPrevActions.peekLast()==1)
            combinedAction = "C" + opponentAction;
        else
            combinedAction = "D" + opponentAction;
        
        double r = 0;
        switch (combinedAction)
        {
            case "CC":
                r = this.parameters.CCpayoff;
                break;
            case "CD":
                r = this.parameters.CDpayoff;
                break;
            case "DC":
                r = this.parameters.DCpayoff;
                break;
            case "DD":
                r = this.parameters.DDpayoff;
                break;
            default:
                System.out.println("Unknown pair of actions: " + combinedAction + 
                        "; opponentAction = " + opponentAction);
        }
        
        this.currentPayoff += r;
        this.myPrevPayoffs.poll();
        this.myPrevPayoffs.offer(r);
        
        double avgPayoff = 0.0;
        Iterator payoffIter = this.myPrevPayoffs.iterator();
        while (payoffIter.hasNext())
            avgPayoff += (double) payoffIter.next();
        avgPayoff /= this.parameters.myHistoryLength;
        
        //The learning model below is adapted from
        //Ezaki T, Horita Y, Takezawa M, Masuda N (2016) Reinforcement Learning Explains Conditional
        //Cooperation and Its Moody Cousin. PLoS Comput Biol 12(7): e1005034. doi:10.1371/journal.pcbi.1005034
        //Our version has constant increments and decrements to pt
//        double s = Math.tanh(this.parameters.beta * (avgPayoff - this.parameters.A));
        double s = -1.0;
        if (avgPayoff > this.parameters.A)
            s = 1.0;
        
        //Adapting the value of epsilon is not in the above paper.
        if (this.parameters.adaptiveEpsilon)
        {
            if (avgPayoff > this.parameters.A)
            {
                this.parameters.epsilon -= 0.01;
                if (this.parameters.epsilon < 0.0)
                    this.parameters.epsilon = 0.0;
            }
            else
            {
                this.parameters.epsilon += 0.01;
                if (this.parameters.epsilon > 0.2)
                    this.parameters.epsilon = 0.2;
            }
        }   
        
        if (this.debug.printAspirationLearningUpdateDetails)
        {
            if (this.id==0)
            {
                System.out.println("Aspiration learning update:");
                System.out.println("p(t-1)=" + this.pt);
                System.out.println("a(t-1)=" + combinedAction.substring(0, 1));
                System.out.println("Avg payoff=" + avgPayoff);
                System.out.println("s(t-1)=" + s);
            }
        }
        
        if (s >= 0)
        {
            if (combinedAction.equals("CC") || combinedAction.equals("CD"))
                this.pt += this.parameters.probIncr;
            else
                this.pt -= this.parameters.probIncr;
        }
        else
        {
            if (combinedAction.equals("CC") || combinedAction.equals("CD"))
                this.pt -= this.parameters.probIncr;
            else
                this.pt += this.parameters.probIncr;
        }
        
        if (this.pt > 1.0)
            this.pt = 1.0;
        if (this.pt < 0.0)
            this.pt = 0.0;
        
        if (this.debug.printAspirationLearningUpdateDetails)
            if (this.id==0)
                System.out.println("p(t)=" + this.pt);
        
        int opponentActionCode = 1;
        if (opponentAction.equalsIgnoreCase("D"))
            opponentActionCode = 0;
        
        if (this.debug.printOpponentHistory)
        {
            if (this.zealot)
            {
                Iterator opp = this.opponentPrevActions.iterator();
                String opponentHistory = "";
                while (opp.hasNext()) 
                    opponentHistory += opp.next();
                System.out.println("Agent " + this.id + ": Strategy = " + this.currentStrategy 
                        + "Opponent history = " + opponentHistory);
            }
        }
        
        if (this.opponentPrevActions.size() == this.parameters.opponentHistoryLength)
            this.opponentPrevActions.poll();
        this.opponentPrevActions.offer(opponentActionCode);
    }
    
    //Can be used when two zealots play each other, but don't want that to affect
    //their estimate of the level of cooperation among non-zealots
    public void updatePayoffButNotHistory(String opponentAction)
    {
        String combinedAction;
        
        if ((int)this.myPrevActions.peekLast()==1)
            combinedAction = "C" + opponentAction;
        else
            combinedAction = "D" + opponentAction;
        
        switch (combinedAction)
        {
            case "CC":
                this.currentPayoff += this.parameters.CCpayoff;
                break;
            case "CD":
                this.currentPayoff += this.parameters.CDpayoff;
                break;
            case "DC":
                this.currentPayoff += this.parameters.DCpayoff;
                break;
            case "DD":
                this.currentPayoff += this.parameters.DDpayoff;
                break;
            default:
                System.out.println("Unknown pair of actions: " + combinedAction + 
                        "; opponentAction = " + opponentAction);
        }
    }
    
    public double getPCAfterC()
    {
        return this.pCAfterC;
    }
    
    public double getPCAfterD()
    {
        return this.pCAfterD;
    }
    
    public int getPayoff()
    {
        return this.currentPayoff;
    }
   
    public String getStrategy()
    {
        return this.currentStrategy;
    }
    
    public void setStrategy(String st)
    {
        this.currentStrategy = st;
    }
    
    public void setIndegree(int in)
    {
        this.indegree = in;
        this.findNormalizedIndegree();
    }
    
    //the indegree of this node, divided by the sum of the indegrees of this node and all its neighbors
    public void findNormalizedIndegree()
    {
        int totalIndegree = 0;
        if (this.neighbors == null)
        {
            this.normalizedIndegree = 0;
            return;
        }
        
        for(int i = 0; i < this.neighbors.size(); i++)
            totalIndegree += ((Agent)this.neighbors.get(i)).getIndegree();
        totalIndegree += this.indegree;
        
        if (totalIndegree == 0)
            System.out.println("Error in findNormalizedIndegree()!! totalIndegree is zero.");
        else
            this.normalizedIndegree = (double)this.indegree/totalIndegree;
    }

    public boolean isLoner()
    {
        return this.neighbors == null || this.neighbors.isEmpty();
    }
    
    public int getIndegree()
    {
        return this.indegree;
    }
    
    public double getNormalizedIndegree()
    {
        return this.normalizedIndegree;
    }
    
    //useful when topology is being updated
    public void incrementIndegree()
    {
        this.indegree++;
        this.findNormalizedIndegree();
    }
    
    //useful when topology is being updated
    public void decrementIndegree()
    {
        if (this.indegree > 0)
            this.indegree--;
        this.findNormalizedIndegree();
    }

    //set the neighbors of this agent
    public void setNeighbors(ArrayList newNeighbors)
    {
        //decrement indegrees of all current neighbors
        if (this.neighbors != null)
        {
            for(int i = 0; i < this.neighbors.size(); i++)
            {
                Agent nbor = (Agent) this.neighbors.get(i);
                nbor.decrementIndegree();
            }
        }
        
        //replace current neighbors with new neighbors
        this.neighbors = newNeighbors;
        
        //increment indegrees of all new neighbors
        if (this.neighbors != null)
        {
            for(int i = 0; i < this.neighbors.size(); i++)
            {
                Agent nbor = (Agent) this.neighbors.get(i);
                nbor.incrementIndegree();
            }
        }
        this.findNormalizedIndegree();
    }
    
    public ArrayList getNeighbors()
    {
        return this.neighbors;
    }
    
    public Agent getRandomNeighbor()
    {
        if (this.neighbors == null || this.neighbors.isEmpty())
            return null;
        
        int rndNbor = this.uniDistr.nextIntFromTo(0, this.neighbors.size()-1);
        Agent randomNeighbor = (Agent)this.neighbors.get(rndNbor);
        
        return randomNeighbor;
    }
    
    public boolean addNeighbor(Agent newNeighbor)
    {
        if (newNeighbor == null)
            System.out.println("In Agent::addNeighbor for agent " + this.id + ", newNeighbor is null!!!");
        
        if (this.neighbors == null)
            this.neighbors = new ArrayList();
        
        //make sure the agent does not already have a link to the newNeighbor
        for(int i = 0; i < this.neighbors.size(); i++)
        {
            Agent tempAgent = (Agent) this.neighbors.get(i);
            if (tempAgent == newNeighbor)
                return false;
        }
        
        this.neighbors.add(newNeighbor);
        newNeighbor.incrementIndegree();
        this.findNormalizedIndegree();
        
        return true;
    }
    
    //Turn this agent into a loner
    public void makeLoner()
    {
        for(int i = 0; i < this.neighbors.size(); i++)
        {
            Agent tempAgent = (Agent) this.neighbors.get(i);
            tempAgent.decrementIndegree();
        }
        this.neighbors.clear();
    }
        
    public int getId()
    {
        return this.id;
    }
    
}
