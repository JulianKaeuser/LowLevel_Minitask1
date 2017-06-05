package lowlevel;

import java.util.HashMap;

/**
 * Created by theChaoS on 03.06.2017.
 */
public class ClusterEncoder {


    public ClusterEncoder(){

    }

    /**
     * Encodes the cluster in the specified encoding. Each state object contained in the cluster
     * is assigned a long value (i. e. the method state.setCode(long x) is called with the assigned
     * code as parameter. Thus, any state contained in the encoded cluster afterwise has a valid code
     * @param cluster
     * @param enc the encoding which shall be used
     * @assert cluster, enc != null and no more than 63 states in cluster
     * @return A HashMap mapping a string with the code to each state, null if too many states are in the cluster
     *
     */
    public static HashMap<State, String> encodeCluster(Cluster cluster, Encoding enc){
        HashMap<State, String> map = new HashMap<State, String>();

        int numStates = cluster.getNumStates();
        if (enc.equals(Encoding.ONEHOT) && numStates>63){
            System.out.println(" Attention: Can't encode more than 63 states one-hot wise");
            return null;
        }

        if(enc.equals(Encoding.BINARY)) {

            long ii = 0;
            for (State state : cluster.getStateArray()) {
                state.setCode(ii);
                String str = Long.toBinaryString(ii);
                map.put(state, str);
                ii++;
            }
            cluster.setEncoded(true);
            cluster.setEncoding(Encoding.BINARY);
        }
        else if (enc.equals(Encoding.ONEHOT)){
            long ii=1;
            for (State state : cluster.getStateArray()){
                state.setCode(ii);
                String str = Long.toBinaryString(ii);
                map.put(state, str);
                ii*=2;
            }
            cluster.setEncoded(true);
            cluster.setEncoding(Encoding.ONEHOT);
        }

        return map;
    }

    /**
     * Assigns a code and ID to each cluster of the given fsm. The resulting codes (binary String) for each cluster
     * are retured in a HashMap of Clusters and Strings. Additionally, for each cluster in the fsm, the method
     * Cluster.setID(long id) and setCode(String code) are called with the computed value as parameter.
     * @param fsm the StateMachine to work on
     * @assert fsm!=null
     * @return A Map of Strings to clusters
     */
    public static HashMap<Cluster, String> assignClusterCodes (StateMachine fsm){
        HashMap<Cluster, String> map = new HashMap<Cluster, String>();

        long ii = 1;
        for (Cluster cluster : fsm.getClusters()){
            cluster.setID(ii);
            cluster.setCode(Long.toBinaryString(ii));
            map.put(cluster, Long.toBinaryString(ii));
            ii*=2;
        }
        return map;


    }




}
