package lowlevel;

import java.util.*;

/**
 * Represents state of an FSM.
 * @author Wolf & Gottschling
 *
 */

// Da diese Klasse ungeignet war wurde sie umgebaut!

public class State{
	private String stateName;

    private Map<Long, Transition> incomingTransitions;
    private Map<Long, Transition> outgoingTransitions;
    private long code=-1;

	private List<Long> inputs = new ArrayList<Long>();
	private Map<Long, Long> outputMap = new HashMap<Long, Long>();

//	private Cluster cluster=null;

	public State(){
		this.stateName="Name NOT set!";
	}

	public State(String name){
		this.stateName=name;
        this.incomingTransitions= new HashMap<Long, Transition>();
        this.outgoingTransitions= new HashMap<Long, Transition>();
	}

	public List<State> getNextStates(){
		List<State> myStates = new ArrayList<State>();
		for(Transition aTransition: incomingTransitions.values()){
			myStates.add(aTransition.getTarget()); //furchtbarer code sorry
		}
		return myStates;
	}

/*	public Cluster getCluster(){
	    return this.cluster;
    }

    public void setCluster(Cluster cluster){
	    this.cluster = cluster;
    }
*/
	public String getName(){
		return this.stateName;
	}

	/**
	 * Adds a transition to the state
	 * @param input condition of the transition
	 * @param nextState related state
	 */
	public void addTransition(long input, State nextState){
		inputs.add(input);


		if(nextState!=this){						// increase transition count for both, this and nextState
			nextState.addIngoingTransition(this, input);
		}
        Transition newTranstion = new Transition(input, nextState, this);
		this.outgoingTransitions.put(input, newTranstion);
	//	nextStateMap.put(input, nextState);
	}

	public void addOutput(long input, long output){
		outputMap.put(input, output);
	}

	public State getNextState(long input){
		return (State) ((Transition)this.outgoingTransitions.get(input)).getTarget();
	}

	public long output(long input){
		return this.outputMap.get(input);
	}

	public long getCode() {
		return code;
	}

	public int getShortCode(){
		int scode=0;
		long tcode = this.code;
		int i = 0;
		while( ( tcode & (long) 0x3 ) > 0){
			if( (tcode & 0x3 ) == 2){
				scode |= (1<<i);
			}
			i++;
			tcode >>= 2;
		}
		return scode;
	}

	public void setCode(long x) {
		this.code = x;
	}

	/**
	 * Adds an ingoing transition
	 * @param state previous state
	 */
	public void addIngoingTransition(State state, long input){
        Transition newTranstion = new Transition(input, this, state);
		this.incomingTransitions.put(input, newTranstion);
		/*
		if(transitionCount.get(state)==null){
			transitionCount.put(state, 1);
		}
		transitionCount.put(state, transitionCount.get(state)+1);
		total_transitions++;
		*/
	}

	public int getTotalTransitionCount(){
		return this.outgoingTransitions.size()+this.incomingTransitions.size();
	//	return total_transitions;
	}

	/**
	 * Returns a 2-dim array of all transitions
	 * @return transition
	 */
	/*
	public long[][] getTransitions(){
		long[][] transitions = new long[nextStateMap.size()][3];

		int i=0;
		for(Map.Entry<Long, State> entry : nextStateMap.entrySet()){
			transitions[i][0] = this.code; //current State in code ?
			transitions[i][1] = entry.getKey(); //Input Code ?
			transitions[i][2] = entry.getValue().getCode(); //next State Code ?
			i++;
		}

		return transitions;
	}
	*/

	public List<Transition> getTransitions(){
		List<Transition> retList = new ArrayList<Transition>(this.outgoingTransitions.values());
		retList.addAll(this.incomingTransitions.values());
        return retList;
	}


	public List<Transition> getOutgoingTransitions(){
		return new ArrayList(this.outgoingTransitions.values());
	}

	public List<Transition> getIncomingTransitions(){
        return new ArrayList(this.incomingTransitions.values());
	}
	/**
	 * Returns a 2-dim array representing the outputs.
	 * @return outputs
	 */
	public long[][] getOutputs(){
		long[][] outputs = new long[outputMap.size()][3];

		int i=0;
		for(Map.Entry<Long, Long> entry : outputMap.entrySet()){
			outputs[i][0] = this.code;
			outputs[i][1] = entry.getKey();
			outputs[i][2] = entry.getValue();
			i++;
		}

		return outputs;
	}


}
