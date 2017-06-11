package lowlevel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Julian Käuser on 11.06.2017.
 */
public class SimulatedAnnealing {

    private List<Cluster> clusters;

    private static final double innerNumber = 1.0;
    private static clusterMutator mutator;

    private static void setMutator(){
        mutator = new FSMClusterMutator();
    }

    /*
    S=RandomConfiguration();
    T=InitialTemperature();
    while (ExitCriterion()==false){
        while (InnerLoopCriterion() == false){
            Snew = Generate(S);
            deltaC = Cost(Snew)-Cost(S);
            r = random(0,1);
            if (r < e-deltaC/T){
             S = Snew}
        }
        T=UpdateTemperature();
     }
     */
    /**
     * Starts the Simulated Annealing Process with the given fitnessFunction.
     * It is performed in a way that the List of clusters produced by this method can be set into a StateMachine
     * Object and this object can be encoded afterwards.
     * @param fsm the state machine to find the best clustering for
     * @return A List of clusters which have been found as near optimal. Each Cluster in this list will
     * have a the correct states included.
     */
    public List<Cluster> findClustering(ParsedFile fsm, ClusterFitnessFunction ff){
        List<Cluster> bestClusteringSolution = new ArrayList<Cluster>();

        List<Cluster> sCurr = mutator.getInitialS(fsm);
        double currentFitness = ff.getFitness(sCurr);

        // we have N=numStates elements to alter
        double nIterations = Math.pow(innerNumber, (4/3))* fsm.getNum_states();

        double t = getInitialTemperature(fsm, ff);

        double overallBestFitness = currentFitness;


        double alpha = 1.0;
        while(alpha >= 0.01){                   // experimental amount... in hope that there arent so big fsms
            int ii = 0; // number of inner iterations
            int accepted = 0;
            while(ii<= nIterations){
                ii++;
                List<Cluster> sNew = mutator.mutate(sCurr);
                double newFitness = ff.getFitness(sNew);
                double deltaFitness = newFitness - currentFitness;
                if(newFitness==0.0){
                    continue;
                }
                if(deltaFitness>0){
                    sCurr = sNew;
                    currentFitness = newFitness;
                    accepted++;
                }
                else if(Math.random()<Math.exp(deltaFitness/t)){
                    sCurr = sNew;
                    currentFitness = newFitness;
                    accepted++;
                }
                if (currentFitness>=overallBestFitness){
                    overallBestFitness = currentFitness;
                    bestClusteringSolution = sCurr;
                }
            }
             alpha = (double)(accepted/ii);
            t = updateTemperature(alpha, t);
        }
        // finished; set best solution as return value


        return bestClusteringSolution;
    }

    /**
     * Returns the initial temperature...
     * @return 20*standard deviation of #numStates random changes fitness difference
     */
    private double getInitialTemperature(ParsedFile fsm, ClusterFitnessFunction ff){
        int n = fsm.getNum_states();
        double sDev = 0.0;
        double[] fitnesses = new double[n];
        List<Cluster> list = mutator.getInitialS(fsm);
        list = mutator.mutate(list);
        double currentFitness = ff.getFitness(list);
        double mean = 0.0;
        for (int ii= 0; ii<n; ii++){
            double newFitness = ff.getFitness(mutator.mutate(list));
            fitnesses[ii] = currentFitness - newFitness;
            currentFitness = newFitness;
            mean += fitnesses[ii];
        }
        mean = mean/n;
        double var = 0.0;
        for (double a : fitnesses){
            var+= (a-mean)*(a-mean);
        }
        var = var/n;
        sDev = Math.sqrt(var);
        return 20*sDev;
    }

    /**
     * Updates the temperature according to table.
     *
     * @param alpha the current acceptance rate
     * @param tCurr the current temperature
     * @return the updated temperature
     * @assert 0<=alpha<=1
     */
    private double updateTemperature(double alpha, double tCurr){
        if (alpha>=0.96) return 0.5*tCurr;
        else if (0.8<=alpha && alpha <0.96) return 0.9*tCurr;
        else if(0.15<=alpha && alpha<0.8) return 0.95*tCurr;
        else return 0.8*tCurr;
    }

    ///kommi zum commiten
}
