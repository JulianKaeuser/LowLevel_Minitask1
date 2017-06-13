package lowlevel;

import java.util.*;

/**
 * Created by Julian Käuser on 03.06.2017.
 */
public class StateMachine {

    private  int numInputs;
    private int numOutputs;
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
        this.numOutputs = fsm.getNumOutputs();
        this.numInputs = fsm.getNumInputs();
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




    public int getNumStates(){
        return stateCodes.keySet().size();
    }


    public int getNumInputs() {
        return numInputs;
    }


    public Set<Cluster> getClusters() {
        return clusters;
    }


    public int getNumOutputs() {
        return numOutputs;
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
        clusterCode = clusterCode << clusters.size()-1;

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
                String val = Integer.toBinaryString(stateCode);
                if (val.equals("0")&&cl.getStates().size()<2){
                    val="";
                }
                stateCodes.put(state, val);

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
            List<FlipFlop> stateFFList = new ArrayList<FlipFlop>();
            for (int ii=0; ii<stateFFs.length; ii++){
                FlipFlop stateFF = new FlipFlop();
                stateFFs[ii]=stateFF;
                stateFFList.add(stateFF);
                stateFF.cluster=cluster;
                stateFF.input="next_state_ff_"+ii+"_cluster_"+id;
                stateFF.output="state_ff_"+ii+"_cluster_"+id;
            }
            cluster.setStateBitFFs(stateFFList);

            int numOutTrans = cluster.getOutgoingInterClusterTransitions().size();
            FlipFlop[] outTransFFs = new FlipFlop[numOutTrans];
            List<FlipFlop> outgoingTransFFList = new ArrayList<FlipFlop>();
            for (int ii=0; ii<outTransFFs.length; ii++){
                FlipFlop outTransFF = new FlipFlop();
                outTransFFs[ii]=outTransFF;
                outgoingTransFFList.add(outTransFF);
                outTransFF.cluster=cluster;
                outTransFF.input="next_outTrans_ff_"+ii+"_cluster_"+id;
                outTransFF.output="outTrans_ff_"+ii+"_cluster_"+id;
            }
            cluster.setOutgoingTransFFs(outgoingTransFFList);
            id++;
        }

    }

}
