package lowlevel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Julian Käuser on 11.06.2017.
 */
public class ClusterFitnessFunction {

    public int nLut;

    public ClusterFitnessFunction(int n){
        this.nLut = n;
    }

    public ClusterFitnessFunction(){
        nLut=8;
    }

    public double getFitness(List<Cluster> sCurr){
        double fitness = 1.0;
        //okay complicated. may be updated.
        /*
        Idea: the less clusters which are ok there are, the better.
         */
        int numStatesTotal = 0;
        for (Cluster cl : sCurr){
            int total = cl.getNumberOfIncomingTranstions()+ // incoming
                        getNumInputsThatCare(cl) +          // inputs
                        getInternalStateBits(cl)+           // internal states
                        1;                                  // isActive

            if (total>nLut){
                return 0.0;
            }
            numStatesTotal += cl.getNumStates();
        }
        int reduction = numStatesTotal-sCurr.size();
        fitness = (double) reduction;

        return fitness;
    }

    /**
     * Returns the number of inputs that care to be considered
     * @param cl The cluster to examine
     * @return the number of inputs that are not dont-care at the same bit position
     */
    private int getNumInputsThatCare (Cluster cl){
        int inputsThatCare = 0;
        List<Long> inputs = new ArrayList<Long>();
        for (State state : cl.getStates()){
            for (Transition trans : state.getIncomingTransitions()){
                inputs.add(trans.getInput());
            }
        }
        return getNumberBitsThatCare(inputs);

    }

    /**
     * Takes a List<Long> with this special-kind-idontknowthename- encoding for {1,0,-} and checks
     * how many common bits that are not-dontcare they have.
     * @param  inputs  A list of Longs representing inputs in this kind of notation
     * @return the amount of bits represented by the notation which are not dontcares at the same position
     * in the bit vector.
     */
    private int getNumberBitsThatCare(List<Long> inputs){

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

    /**
     * Returns the number of necessary state bits for the states in the cluster
     * @param cl the cluster to examine
     * @return the ld2 of the states amount in this cluster
     */
    private int getInternalStateBits(Cluster cl){
        double ld2 = Math.log(cl.getNumberOfStates())/Math.log(2);
        return (int) Math.ceil(ld2);
    }
}
