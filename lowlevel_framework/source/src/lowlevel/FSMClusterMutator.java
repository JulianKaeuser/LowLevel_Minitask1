package lowlevel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Julian Käuser on 11.06.2017.
 */
public class FSMClusterMutator {

    /**
     * Returns an initial clustering, where
     * - each state in the parsed fíle has its own cluster
     * @param fsm the input parsed file
     * @return a List of Clusters
     */
    public List<Cluster> getInitialS(ParsedFile fsm){
        List<Cluster> initialList = new ArrayList<Cluster>();

        for (State state : fsm.getStates()){
            Cluster nC = new Cluster();
            nC.addState(state);
            // transitions here

            initialList.add(nC);
        }

        return initialList;
    }

    /**
     * Mutates the current solution. Essential for the quality of the SA!
     * @param sCurr the current clustering solution
     * @return a new solution
     */
    public List<Cluster> mutate(List<Cluster> sCurr){
        List<Cluster> newList = new ArrayList<Cluster>();


        return newList;
    }
}
