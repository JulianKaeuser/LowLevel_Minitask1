package lowlevel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by theChaoS on 04.06.2017.
 */
public class Transition{
    private long input;

    private State target; //Ziel
    private State origin;


    public Transition(long input, State target, State origin){
        this.input=input;
        this.target=target;
        this.origin=origin;
    }

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

    /**
     * Takes a List<Long> with this special-kind-idontknowthename- encoding for {1,0,-} and checks
     * how many common bits that are not-dontcare they have.
     * @param a list of Longs representing inputs in this kind of notation
     * @return the amount of bits represented by the notation which are not dontcares at the same position
     * in the bit vector.
     */
    // Gibt die Anzahl der Bits an die in den Transitionen vorhanden sein müssen da diese Relefanz haben.
    //Beispiel : t1.input = 1-0- und t2.input = 11--. Dann ist das Resultat 3
    public static int getNumberOfBitsThatCare(List<Transition> inputTransitions){
        List<Long> inputs = new ArrayList<Long>();
        for(Transition t: inputTransitions){
            inputs.add(t.getInput());
        }
        int result = 0;

        long tmp = 0x0;
        for (Long in : inputs){
            tmp |= ~in.longValue();
        }
        for (int ii=0; ii<Long.bitCount(tmp); ii+=2){
            long cmp = 0x3 << ii;
            cmp = ~cmp;
            long tmp2 = (tmp | cmp);
            if (cmp==tmp2){
                result++;
            }
        }
        return result;
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
