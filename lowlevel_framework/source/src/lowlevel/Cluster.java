 package lowlevel;

import java.util.*;

/**
 * Created by Florian Prott on 05.06.2017.
 */
public class Cluster{
    private Set<State> states;
    private List<Transition> incomingInterClusterTransitions;
    private List<Transition> outgoingInterClusterTransitions;
    private int clusterStateLatches; // the number of latches needed to store the states in this cluster. depends on encoding (one-hot/binary)

    // should be either "binary" or "onehot"
    private String internalEncoding; //kannste das nicht per bool speicheren? Oder mach ne Klasse draus

    private long id;

    private String name;

    private FlipFlop isActiveFlipFlop;
    private List<FlipFlop> stateBitFFs;
    private List<FlipFlop> outgoingTransFFs;
    private Map<Transition, FlipFlop> outputTransOrigins;

    public Cluster(){
        this.incomingInterClusterTransitions=new ArrayList<Transition>();
        this.outgoingInterClusterTransitions=new ArrayList<Transition>();
        this.states = new HashSet<State>();
        this.outputTransOrigins = new HashMap<Transition, FlipFlop>();
    }

    public Cluster(State aState){
        this.incomingInterClusterTransitions=new ArrayList<Transition>();
        this.outgoingInterClusterTransitions=new ArrayList<Transition>();
        this.states = new HashSet<State>();
        this.addState(aState);
        this.outputTransOrigins = new HashMap<Transition, FlipFlop>();
    }

    public Cluster(Set<State> states){
        this.incomingInterClusterTransitions=new ArrayList<Transition>();
        this.outgoingInterClusterTransitions=new ArrayList<Transition>();
        this.states = new HashSet<State>();
        for(State state: states){
            this.addState(state);
        }
        this.outputTransOrigins = new HashMap<Transition, FlipFlop>();
    }

    public boolean isStateInCluster(State aState){
        return this.states.contains(aState);
    }

    public void addState(State newState){
        //Entferne die Transitionen zwischen den Clusteren wenn es sich um diesen State handelt
        for (Iterator<Transition> iter = this.incomingInterClusterTransitions.listIterator(); iter.hasNext(); ) {
            Transition trans = iter.next();
            if (trans.getOrigin()==newState) {
                iter.remove();
            }
        }

        for (Iterator<Transition> iter = this.outgoingInterClusterTransitions.listIterator(); iter.hasNext(); ) {
            Transition trans = iter.next();
            if (trans.getTarget()==newState) {
                iter.remove();
            }
        }

        //nehme die Transition in die Liste auf falls es sich nicht um einen internen State handelt
         for(Transition trans: newState.getIncomingTransitions()){
            if(!this.isStateInCluster(trans.getTarget())){
                this.incomingInterClusterTransitions.add(trans); //was ist wenn die schon da sind ?
            }
        }
        //für alle ausgehenden setzen wir als Ausganspunkt diesen Cluster
        for(Transition trans: newState.getOutgoingTransitions()){
            if(!this.isStateInCluster(trans.getOrigin())){
                this.outgoingInterClusterTransitions.add(trans);
            }
        }

        //und einfügen
    //    newState.setCluster(this);
        states.add(newState);
    }

    public void removeState(State state){ //empfehle die NICHT zu nutzen weil die halt den Cluster nicht ersetzt
        for (Iterator<Transition> iter = this.incomingInterClusterTransitions.listIterator(); iter.hasNext(); ) {
            Transition trans = iter.next();
            if (trans.getTarget()==state) {
                iter.remove();
            }
        }
        for (Iterator<Transition> iter = this.outgoingInterClusterTransitions.listIterator(); iter.hasNext(); ) {
            Transition trans = iter.next();
            if (trans.getOrigin()==state) {
                iter.remove();
            }
        }
    //    state.setCluster(null);
        states.remove(state);
    }

    public boolean tryToCombineClusterWithOtherCluster(Cluster otherCluster, int N_max){

        int N_states_1 = this.getNumberOfStates();
        int N_states_2 = otherCluster.getNumberOfStates();
        int N_states = N_states_1+N_states_2;
        if(N_states>=N_max)
            return false;

        List<Transition> myTransitions = this.getIncomingInterClusterTransitionsAsList();
        List<Transition> otherTransitions = otherCluster.getIncomingInterClusterTransitionsAsList();

        int N_transiton_1 = myTransitions.size();
        int N_transiton_2 = otherTransitions.size();
        int N_trans = N_transiton_1 + N_transiton_2;

        for (Transition otherTrans : otherTransitions){
            for(State state: this.getStates()){
                if(otherTrans.getOrigin()==state){ //wenn eine andere Transition in unserem Cluster Ihren Ursprung hat so können wir N verkleineren um 1
                    N_trans--;
                }
            }
        }
        for (Transition ourTrans : myTransitions){
            for(State state: otherCluster.getStates()){
                if(ourTrans.getOrigin()==state){ //wenn eine unserer Transition in anderem Cluster Ihren Ursprung hat so können wir N verkleineren um 1
                    N_trans--;
                }
            }
        }

        if((N_trans+N_states)>N_max)
            return false;

        this.combineCluster(otherCluster);
        return true;
    }

    public void combineCluster(Cluster otherCluster){
        Set<State> myStates = this.states;
        for(State s: otherCluster.getStates()){
            this.addState(s);
        }
        /*

        for (Transition otherTrans : otherTransitions){
            for(State state: this.getStates()){
                if(otherTrans.getOrigin()==state){ //wenn eine andere Transition in unserem Cluster Ihren Ursprung hat so können wir N verkleineren um 1
                    N_trans--;
                }
            }
        }
        for (Transition ourTrans : myTransitions){
            for(State state: otherCluster.getStates()){
                if(ourTrans.getOrigin()==state){ //wenn eine unserer Transition in anderem Cluster Ihren Ursprung hat so können wir N verkleineren um 1
                    N_trans--;
                }
            }
        } */

        //Mist das geht so leider nicht ! Ich muss es doch noch alles anfassen XD
        /*
        Set<State> myStates = this.states;
        myStates.addAll(otherCluster.getStates());
        Cluster newCluster = new Cluster(myStates);
        newCluster.setName("Der darf nicht exisitereN!");
        if(newCluster.getN()<=N_max) {

            this.states = myStates;
            return true;
        }
        return false;
        */
        //transitions !
        this.states = myStates;
    }

    public Set<State> getStates(){
        return this.states;
    }

    public List<State> getStatesAsList(){
        return new ArrayList<State>(this.states);
    }

    public State[] getStateArray(){
        return (State[]) states.toArray();
    }

    public String getEncoding(){
        return internalEncoding;
    }

    public void setEncoding(String enc){
        internalEncoding=enc;
    }

    public void setID(long id){
        this.id = id;
    }

    public long getID(){
        return id;
    }

    //just to debug
    public void setName(String name){
        this.name=name;
    }

    public String getName(){
        return this.name;
    }

    public int getNumStates(){
        return states.size();
    }

    public Set<Transition> getIncomingInterClusterTransitions(){
        return new HashSet<Transition>(this.incomingInterClusterTransitions);
    }

    public Set<Transition> getOutgoingInterClusterTransitions(){
        return new HashSet<Transition>(this.outgoingInterClusterTransitions);
    }

    //scheiße overloading kann java ja net
    public List<Transition> getIncomingInterClusterTransitionsAsList(){
        return this.incomingInterClusterTransitions;
    }

    public List<Transition> getOutgoingInterClusterTransitionsAsList(){
        return this.outgoingInterClusterTransitions;
    }

    public int getNumberOfIncomingTranstions(){
        return this.incomingInterClusterTransitions.size();
    }

    public int getNumberOfStates(){
        return this.states.size();
    }

    public int getN(){
        return this.getNumberOfIncomingTranstions()+((int)Math.ceil((Math.log(this.getNumberOfStates())/Math.log(2))))+Transition.getNumberOfBitsThatCare(this.getIncomingInterClusterTransitionsAsList());
    }

    public String getCode(){
        return "EIN CLUSTER HAT KEIN CODE";
    }

    public static int callculateNofMultipleClusters(List<Cluster> clusters){
        int N_states = 0;
        List<Transition> allTransitions = new ArrayList<Transition>();
        List<State> allStates = new ArrayList<State>();
        for(Cluster c : clusters){
            N_states += c.getNumStates(); //States zählen
            allTransitions.addAll(c.getIncomingInterClusterTransitionsAsList()); //Transitions sammeln
            allStates.addAll(c.getStates()); //States sammeln
        }
        N_states=(int)Math.ceil(Math.log(N_states)/Math.log(2));

        int N_trans = allTransitions.size();//Maximale Transtion Zahl errechnen
        // ggf abziehen !
        for (Transition t : allTransitions){
            for(State s: allStates){
                if(t.getOrigin()==s){ //wenn eine andere Transition in unserem Cluster Ihren Ursprung hat so können wir N verkleineren um 1
                    N_trans--;
                }
            }
        }

        //Bites that care errechnen
        int N_bit_care = Transition.getNumberOfBitsThatCare(allTransitions);
        return N_trans+N_states+N_bit_care;
    }


/*
    public int getInputs(){ //der Name ist blöd und Cluster soll das nicht mehr machen
        return numInputs;
    }
*/
/*
    public Set<Transition> getTransitions(){ //frag einfach den state
        return this.transitions;
    }

    public Set<Transition> getIncomingTransitions(){
        return this.transitions;
    }

    public int getNumOfTransitions(){
        return this.transitions.size();
    }
*/
/*
    public String getEncodedCluster(){
        StringBuilder bld = new StringBuilder();

        // ClusterEncoder enc = new ClusterEncoder();
        HashMap<State, String> map = null;
        switch (getEncoding()) {
            case "binary":
                //map = enc.encodeBinary(this.getStateArray(), this.getInputs());
            case "onehot":
                //map = enc.encodeOneHot(this.getStateArray(), this.getInputs());
        }
        for (State s : map.keySet()) {
            String str = ".code "+s.getName()+" "+map.get(s)+"\n";
            bld.append(str);
        }
        return bld.toString();
    }

    public String getCode(){
        return "";
    }

    public int getClusterStateLatches(){
        return clusterStateLatches;
    }

/*
    private Set<State> states;
//    private Set<Transition> clusterTransitions;
    private int clusterStateLatches; // the number of latches needed to store the states in this cluster. depends on encoding (one-hot/binary)

    // should be either "binary" or "onehot"
    private String internalEncoding; //kannste das nicht per bool speicheren? Oder mach ne Klasse draus

    private long id;

    private int numInputs;

    public Cluster(){
        this.states = new HashSet<State>();
 //       this.transitions = new HashSet<Transition>();
    }

    public Cluster(State state){
        this.states = new HashSet<State>();
 //       this.transitions = new HashSet<Transition>();
        for(Transition aTransition: state.getTransitions()){
            aTransition.setCluster(this, null);
        }
        this.addState(state);
    }

    public boolean addState(State state){
    //    System.out.println("-----TEST-----");
        /*
        long[][] crappy_transitions= state.getTransitions();
        //ACHTUNG, war NICHT dokumentiert und ist super unsicher
        for(int i=0;i<crappy_transitions.length;i++){
            State originState=state;
            long input=crappy_transitions[i][1];
            State targetState= state.getNextState((int)input);
            Transition myTransition= new Transition(input, targetState, originState, this);
            this.transitions.add(myTransition);
        }

    //    System.out.println("ENDE");
        return states.add(state);
    }

    public State[] getStateArray(){
        return (State[]) states.toArray();
    }

    public boolean removeState(State state){
        return states.remove(state);
    }

    public String getEncoding(){
        return internalEncoding;
    }

    public void setEncoding(String enc){
        internalEncoding=enc;
    }

    public void setID(long id){
        this.id = id;
    }

    public long getID(){
        return id;
    }

    public int getNumStates(){
        return states.size();
    }

    public int getInputs(){
        return numInputs;
    }

    public Set<Transition> getTransitions(){
        return this.transitions;
    }

    public Set<Transition> getIncomingTransitions(){
        return this.transitions;
    }

    public int getNumOfTransitions(){
        return this.transitions.size();
    }

    public String getEncodedCluster(){
        StringBuilder bld = new StringBuilder();

       // ClusterEncoder enc = new ClusterEncoder();
        HashMap<State, String> map = null;
        switch (getEncoding()) {
            case "binary":
                //map = enc.encodeBinary(this.getStateArray(), this.getInputs());
            case "onehot":
                //map = enc.encodeOneHot(this.getStateArray(), this.getInputs());
        }
        for (State s : map.keySet()) {
            String str = ".code "+s.getName()+" "+map.get(s)+"\n";
            bld.append(str);
        }
        return bld.toString();
    }

    public String getCode(){
        return "";
    }

    public int getClusterStateLatches(){
        return clusterStateLatches;
    }
    */


    public void setIsActiveFF(FlipFlop ff){
        this.isActiveFlipFlop = ff;
    }

    public FlipFlop getIsActiveFlipFlop(){
        return isActiveFlipFlop;
    }

    public List<FlipFlop> getStateBitFFs() {
        return stateBitFFs;
    }

    public void setStateBitFFs(List<FlipFlop> stateBitFFs) {
        this.stateBitFFs = stateBitFFs;
    }

    public List<FlipFlop> getOutgoingTransFFs() {
        return outgoingTransFFs;
    }

    public void setOutgoingTransFFs(List<FlipFlop> outgoingTransFFs) {
        this.outgoingTransFFs = outgoingTransFFs;
    }

    public Map<Transition, FlipFlop> getOutputTransitionOrigins(){
        return outputTransOrigins;
    }

    public void addOutputTransitionOriginFF(Transition trans, FlipFlop ff){
        outputTransOrigins.put(trans, ff);
    }

    public void setOutputTransOrigins(Map<Transition, FlipFlop> outputTransOrigins) {
        this.outputTransOrigins = outputTransOrigins;
    }
}
