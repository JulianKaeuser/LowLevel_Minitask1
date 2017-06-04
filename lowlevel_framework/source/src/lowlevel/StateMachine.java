package lowlevel;

import java.util.Set;

/**
 * Created by Julian Käuser on 03.06.2017.
 */
public class StateMachine {

    public String name;

    private Set<Cluster> clusters;

    //todo
    public String getEncoding(){
        return null;
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
}
