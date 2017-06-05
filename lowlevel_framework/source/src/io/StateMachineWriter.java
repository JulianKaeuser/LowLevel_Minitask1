package io;

import lowlevel.Cluster;
import lowlevel.State;
import lowlevel.StateMachine;
import lowlevel.Transition;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Julian Käuser on 03.06.2017.
 */
public class StateMachineWriter {

    /**
     *
     * @param fsm the StateMachine object to write out
     * @param destination the directory where the written .kiss2
     *                    file shall be placed
     */
    public static void writeFSM(StateMachine fsm, String destination){

        if (fsm==null || destination==null){
            System.out.println("no fsm written - destination or fsm unknown");
            return;
        }
        // catch the "fsm-has-no-name"-case
        if (fsm.name==null){
            System.out.println("fsm has no name, name set to time+date of execution");
            fsm.name=new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        }
        StringBuilder bld = new StringBuilder();
        bld.append("# "+fsm.name +" encoded cluster-wise\n");
        bld.append(".model "+fsm.name);

        //in and outputs for the _logic_ model
        bld.append(buildInputs(fsm));
        bld.append(buildOutputs(fsm));

        // latch initilizations
        String[] latches = getLatches(fsm);
        for (String l : latches){
            bld.append(".latch "+l+"\n");
        }


        bld.append(".start_kiss");
        bld.append(".i "+fsm.getNumInputs()+"\n");
        bld.append(".o "+fsm.getNumOutputs()+"\n");
        // states and transitions
        bld.append(".p "+fsm.getNumTransitions()+"\n");
        bld.append(".s "+fsm.getNumStates()+"\n");
        if (fsm.getResetState()!=null){
            bld.append(".r "+fsm.getResetState().getName());
        }
        // build functional description of fsm (which actually exists with the input file...
        for (Transition trans : fsm.getTransitions()){
            bld.append(Long.toBinaryString(trans.getInput()));
            bld.append(" ");
            bld.append(trans.getOriginState().getName());
            bld.append(" ");
            bld.append(trans.getTargetState().getName());
            bld.append(Long.toBinaryString(trans.getOriginState().output(trans.getInput())));
            bld.append("\n");
        }

        bld.append(".end_kiss\n");
        //latch mapping
        bld.append(".latch_order ");


        for (int ii=0; ii<latches.length; ii++){
            bld.append(latches[ii]);
            /*
            note: the first n latches are associated with the one-hot cluster encoding,
            then the next
             */
        }
        bld.append("\n");




        // print codes of states
        for (Cluster cluster : fsm.getClusters()){
            for (State state : cluster.getStateArray()){
                int offset = getClusterOffset(latches, cluster);
                String stateCode = getStateCodeFromLong(state.getCode());
                bld.append(".code "+state.getName()+" "+cluster.getCode()+getStateCode(offset, latches.length, stateCode) +"\n");
            }
        }

        bld.append(".end");
        destination = (destination.endsWith(System.lineSeparator())) ? destination : (destination + System.lineSeparator());
        destination += fsm.name+"_clusterEncoded.kiss2";

        try (FileWriter out = new FileWriter(destination)) {
            BufferedWriter buf = new BufferedWriter(out);
            buf.write(bld.toString());
            buf.flush();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Builds a string which holds the latch order list
     * @param fsm the modeled state machine
     * @return a string which has all latches described
     */
    private static String[] getLatches(StateMachine fsm){
        String[] latches = null;
        if (fsm==null) {
            latches = new String[1];
            latches[0] = "";
            return latches;
        }

        int actualNumOfLatches = 0;
        actualNumOfLatches += fsm.getClusters().size(); // number of one-hot encoded clusters +
        for (Cluster cluster : fsm.getClusters()){
            actualNumOfLatches += getClusterInternalLatches(cluster).length; //for each cluster, the amount of internal latches
        }
        latches = new String[actualNumOfLatches];
        String[] externalClusterLatches = getExternalClusterLatches(fsm);
        int ii=0;
        for (; ii<externalClusterLatches.length; ii++){
            latches[ii]=externalClusterLatches[ii]; // first n (# of clusters) latches for external cluster one hot
        }
        int handled = ii;
        for (Cluster cluster : fsm.getClusters()){
            String[] internalClusterLatches = getClusterInternalLatches(cluster);
            int jj=0;
            for (; ii<handled+internalClusterLatches.length; ii++){
                latches[ii] = internalClusterLatches[jj];
                jj++;
            }
            handled=ii;
        }
        return latches;
    }

    /**
     * Returns a String[] which holds the names of inter-cluster latches
     * Latches for a cluster are named:
     *  vCl+ClusterID
     * for each cluster
     * @param fsm the modeled StateMachine
     * @return an String[] if fsm!=null
     */
    private static String[] getExternalClusterLatches(StateMachine fsm){
        String[] latches;
        Set<Cluster> clusters = fsm.getClusters();
        latches = new String[clusters.size()];
        int ii=0;
        for (Cluster cluster : clusters){
            latches[ii] = "vCl"+cluster.getID()+ " ";
        }
        return latches;
    }

    /**
     * Returns an array of names for the intra-cluster necessary latches.
     * Latches are named:
     *  vS+ClID+ClusterID+S+ii
     * where
     *  ClusterID is the ID of the correspondent cluster and
     *  ii is the number of the latch
     * @param cluster The cluster whose internals are modeled
     * @return a String[] if cluster!=null
     */
    private static String[] getClusterInternalLatches(Cluster cluster){
        String[] latches = new String[cluster.getCode().length()]; // as many latches as bits there are in the code, is it?
        for (int ii=0; ii<latches.length; ii++){
            latches[ii]= "vSClID"+cluster.getID()+"S"+ii;
        }
        return latches;

    }

    /**
     * Returns a string containing all inputs declarations for the blif file, .inputs included
     * @param fsm the state machine modeled
     * @return a string in the fitting format if fsm!= null
     */
    private static String buildInputs(StateMachine fsm){
        String ins = "";

        for (int ii=0; ii<fsm.getNumInputs(); ii++){
            ins+=("in"+ii+" ");
        }
        return ins;

    }

    /**
     * Returns a model descprition output string in the format .output
     * @param fsm the modeled state machine
     * @return a fitting string if fsm!=null
     */
    private static String buildOutputs(StateMachine fsm){
        String outs = "";
        for (int ii=0; ii<fsm.getNumOutputs(); ii++){
            outs+=("in"+ii+" ");
        }
        return outs;

    }

    /**
     * Returns a string which places the given code right. all other values are assigned to zero (??)
     * @param offset
     * @param totalLength the total length (=number of latches) for the encoding
     * @param code
     * @return a String of length totalLength with the code placed at position offset
     */
    private static String getStateCode(int offset, int totalLength, String code){
        String str = "";
        for (int ii=0; ii<offset; ii++){
            str+="0";
        }
        str+=code;
        for (int ii=0; ii<totalLength-offset-code.length(); ii++){
            str+="0";
        }
        return str;
    }

    /**
     * Returns the offset in the latch-order list string for the given cluster latches
     * @param latches the String[] with the latch strings
     * @param cluster the cluster whose latches' offset shall be found
     * @return the offset from zero of the latches in the latch list for the given cluster's latches storing the states
     */
    private static int getClusterOffset(String[] latches, Cluster cluster){
        int offset = 0;

        while (offset<latches.length){
            if (latches[offset].startsWith("vSClID"+cluster.getID())) {
                return offset;
            }
            offset++;
        }
        return offset;
    }

    /**
     * Transforms the states code (which has been assigned in the encoding) from a 'long' representation to a
     * String representation (easier to print in BLIF)
     * @param code the state's code as java.long
     * @return the state's code as java.String
     */
    public static String getStateCodeFromLong(long code){
        // TODO: 04.06.2017 implement method
        return "";
    }
}
