package lowlevel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Julian Käuser on 11.06.2017.
 */
public class ClusterMutator {

    /**
     * Returns an initial clustering, where
     * - each state in the parsed fíle has its own cluster
     * @param fsm the input parsed file
     * @return a List of Clusters
     */
    public List<Cluster> getInitialS(ParsedFile fsm){
        List<Cluster> initialList = new ArrayList<Cluster>();

        for (State state : fsm.getStates()){
            Cluster nC = new Cluster(state);

            initialList.add(nC);
        }

        return initialList;
    }

    /**
     * Mutates the current solution. Essential for the quality of the SA!
     * @param currentClusters the current clustering solution
     * @param numOfOperations how many things should be changed
     * @return a new solution
     */
    public List<Cluster> mutatedClusters (List<Cluster> currentClusters, int numOfOperations, int N_max){
        List<Cluster> clusters = currentClusters;
        List<State> allStates = new ArrayList<State>();
        for(Cluster c: currentClusters){
            allStates.addAll(c.getStatesAsList());
        }
        //schnap dir irgendein state, löse diesen aus dem cluster den er jetzt hat. Steck ihn in ne neuen cluster und versuch diesen zu verwursten.
        boolean reuse_cluster=false;
        int times_cluster_was_reused=0;
        Cluster choosen_cluster=null;
        if(numOfOperations>0){
            //schnap dir irgendein state
            int state_pos=(int)(Math.random() * (allStates.size()-0.00001)); //-0.00001 damit wir keinen Überlauf bekommen ^^
            //löse den aus den Cluster den er jetzt hat
            State chosen_state=allStates.get(state_pos);
            Cluster c=chosen_state.getCluster();
            c.removeState(chosen_state); //ACHTUNG, der State ist momentan NICHT assigned
            if(c.getNumberOfStates()==0){ //wenn der Cluster empty ist dann weg damit
                clusters.remove(c);
            }

            //mach den State zu einen neuen Cluster
            Cluster newCluster = new Cluster(chosen_state);
            //verwurste den State
            if(reuse_cluster==false) {
                int cluster_pos = (int) (Math.random() * (clusters.size() - 0.00001)); //-0.00001 damit wir keinen Überlauf bekommen ^^
                choosen_cluster = clusters.get(cluster_pos);
            }
            choosen_cluster.combineCluster(newCluster);
            // ich füg mal nen kommi ein

            //ausrechnen ob man nochmal in den gleichen Cluster schreibt
            if( Math.random()*100-times_cluster_was_reused*5 > 75) { //wir wollen mit kleiner Warscheinlichkeit (maximal 25 %) den Cluster reusen
                times_cluster_was_reused++;
                reuse_cluster=true;
            }else{
                reuse_cluster=false;
                times_cluster_was_reused=0;
            }
            numOfOperations--;
        }

        return clusters;
    }
}
