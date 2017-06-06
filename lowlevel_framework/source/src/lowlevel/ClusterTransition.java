package lowlevel;

/**
 * Created by theChaoS on 05.06.2017.
 */
public class ClusterTransition{
    private Cluster target;
    private Cluster origin;
    private Transition correspondingTransition;

    public ClusterTransition(Transition Transition, Cluster target, Cluster origin){
        this.correspondingTransition=Transition;
        this.target=target;
        this.origin=origin;
    }

    public Transition getCorrespondingTransition(){
        return this.correspondingTransition;
    }

    public Cluster getTarget(){
        return this.target;
    }

    public Cluster getOrigin(){
        return this.origin;
    }

    public boolean isProperlyInitalised(){
        if(this.target!=null&&this.origin!=null)
            return true;
        return false;
    }
}
