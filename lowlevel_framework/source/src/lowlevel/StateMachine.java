package lowlevel;

import java.util.Collection;
import java.util.Set;

/**
 * Created by Julian Käuser on 03.06.2017.
 */
public class StateMachine {

    public String name;

    private Encoding enc;

    private Set<Cluster> clusters;

    public void setEncoding(Encoding enc){
        this.enc = enc;
    }
    public Encoding getEncoding(){
        return enc;
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

    // todo
    public int getNumTransitions(){
        return 0;
    }

    // todo
    public State getResetState(){
        return null;
    }
    //todo
    public int getNumStates(){
        return 0;
    }

    // todo
    public Collection<Transition> getTransitions(){
        return null;
    }
}
