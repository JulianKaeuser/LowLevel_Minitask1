package lowlevel;

import java.util.*;

/**
 * Created by Julian Käuser on 03.06.2017.
 */
public class StateMachine {

    public String name;

    private Set<Cluster> clusters;
    private Map<Cluster, String> clusterCodes;
    private Map<State, String> stateCodes;

    public StateMachine(){
        clusters = new HashSet<Cluster>();
        clusterCodes = new HashMap<Cluster, String>();
        stateCodes = new HashMap<State, String>();
    }

    public StateMachine(ParsedFile fsm){
        clusterCodes = new HashMap<Cluster, String>();
        stateCodes = new HashMap<State, String>();
        this.clusters= new HashSet<Cluster>();
        for(State aState : fsm.getStates()){
            this.addState(aState);
        }
    }

    public void addState(State newState){
        Cluster newCluster = new Cluster(newState);
        newCluster.setName(newState.getName()); //just for debug
        this.clusters.add(newCluster);
    }

    /**
     * Adds a cluster which may be something between brand new (uninitialized) and fully configured
     * @param newCluster
     */
    public boolean addCluster(Cluster newCluster){
        return this.clusters.add(newCluster);
    }

    public class Pair<S, T> {
        public S x;
        public T y;

        public Pair(S x, T y) {
            this.x = x;
            this.y = y;
        }
    }

    //todo
    public String getEncoding(){
        return null;
    }

    public int getNumTransitions(){
        return 0;
    }

    public int getNumStates(){
        return 0;
    }

    // todo
    public int getNumInputs() {
        return 0;
    }


    public Set<Cluster> getClusters() {
        return clusters;
    }

    // todo
    public int getNumOutputs() {
        return 0;
    }

    public State getResetState() {
        return null;
    }

    public List<Transition> getTransitions() {
        List<Transition> allTranstions=new ArrayList<Transition>();
        for(Cluster c: this.clusters){
            for(State s:c.getStates()){
                allTranstions.addAll(s.getTransitions());
            }
        }
        return allTranstions;
    }

    public void clearCluster(){
        this.clusters = new HashSet<Cluster>();
    }

    /**
     * Adds the list of configured clusters to this fsm
     * @param list
     */
    public void addClusteredList(List<Cluster> list){
        for (Cluster cl : list){
            addCluster(cl);
        }
    }

    /**
     * Does the actual encoding in each cluster, and for each cluster
     */
    public void assignCodes(){
        long clusterCode = 0x1;
        clusterCode = clusterCode << clusters.size();

        int ii = 0;
        for (Cluster cl : clusters){
            int stateCode = 0x0;
            String str = "";
            for (int aa= 0; aa<ii; aa++){
                str +="0";
            }
            clusterCodes.put(cl, str+Long.toBinaryString(clusterCode));
            clusterCode = clusterCode>>1;   // one-hot


            for (State state : cl.getStates()){
                stateCodes.put(state, Integer.toBinaryString(stateCode));
                stateCode++;    // binary
            }
            ii++;
        }
    }

    /**
     * Returns the cluster codes
     * @return
     */
    public Map<Cluster, String> getClusterCodes(){
        return clusterCodes;
    }

    /**
     * Returns the state codes. Attention: Some states may have the same code, but this does not matter
     * because they are not in the same cluster!
     * @return
     */
    public Map<State, String> getStateCodes() {
        return stateCodes;
    }


    public void initFlipFlops(){
        long id = 0;
        for (Cluster cluster : clusters){
            cluster.setID(id);
            FlipFlop isActive = new FlipFlop();
            isActive.cluster= cluster;
            isActive.input="next_isActive_Cluster"+id;
            isActive.output="isActive_Cluster"+id;
            cluster.setIsActiveFF(isActive);

            int numStateFFs = (int)Math.ceil(Math.log(cluster.getNumberOfStates())/Math.log(2));
            FlipFlop[] stateFFs = new FlipFlop[numStateFFs];
            for (int ii=0; ii<stateFFs.length; ii++){
                FlipFlop stateFF = new FlipFlop();
                stateFFs[ii]=stateFF;
                stateFF.cluster=cluster;
                stateFF.input="next_state_ff_"+ii+"_cluster_"+id;
                stateFF.output="state_ff_"+ii+"_cluster_"+id;
            }

            int numOutTrans = cluster.getOutgoingInterClusterTransitions().size();
            FlipFlop[] outTransFFs = new FlipFlop[numOutTrans];
            for (int ii=0; ii<outTransFFs.length; ii++){
                FlipFlop outTransFF = new FlipFlop();
                outTransFFs[ii]=outTransFF;
                outTransFF.cluster=cluster;
                outTransFF.input="next_outTrans_ff_"+ii+"_cluster_"+id;
                outTransFF.output="outTrans_ff_"+ii+"_cluster_"+id;
            }
        }

    }

}
