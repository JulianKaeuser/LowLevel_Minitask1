package lowlevel;

/**
 * Created by theChaoS on 04.06.2017.
 */
public class Transition{ //T ist Cluster ODER State
//    private State state;
    private long input;

    private State target; //Ziel
    private State origin;

  //  private boolean incoming; //was nicht incoming ist, ist outgoing !

    public Transition(long input, State target, State origin){
//        this.incoming = incoming;
//        this.state = corrospondingState;
        this.input=input;
        this.target=target;
        this.origin=origin;
    }
/*
    public boolean isIncoming(){
        return this.incoming;
    }
*/
/*    public State getState(){
        return this.state;
    }
*/
    public long getInput(){
        return this.input;
    }

    public String getInputAsBinary(){
        return Long.toBinaryString(this.input);
    }

    public State getTarget(){
        return this.target;
    }

    public State getOrigin(){
        return this.origin;
    }

    public boolean isProperlyInitalised(){
        if(this.target!=null&&this.origin!=null)
            return true;
        return false;
    }
/*
    public void setCluster(Cluster originCluster, Cluster targetCluster){
        this.originCluster = originCluster;
        this.targetCluster = targetCluster;
    }

    public boolean isOriginClusterSet(){
        if(this.getOriginCluster()!=null){
            return true;
        }
        return false;
    }

    public boolean isTargetClusterSet(){
        if(this.getTargetCluster()!=null){
            return true;
        }
        return false;
    }

    public boolean isProperlyInitalised(){
        if(this.isTargetClusterSet()&&this.isOriginClusterSet()&&this.target!=null&&this.originState!=null)
            return true;
        return false;
    }

    public Cluster getTargetCluster(){
        return this.targetCluster;
    }

    public void setTargetCluster(Cluster targetCluster){
        this.targetCluster=targetCluster;
    }

    public Cluster getOriginCluster(){
        return this.originCluster;
    }

    public void setOriginCluster(Cluster originCluster){
        this.originCluster=originCluster;
    }
    */
}
