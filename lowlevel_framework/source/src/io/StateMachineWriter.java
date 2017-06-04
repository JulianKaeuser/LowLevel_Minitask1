package io;

import lowlevel.Cluster;
import lowlevel.State;
import lowlevel.StateMachine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

        // some lazy KOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOmment

        bld.append(".start_kiss");
        bld.append(".i "+fsm.getNumInputs()+"\n");
        bld.append(".o "+fsm.getNumOutputs()+"\n");
        // states and transitions
        //bld.append(".p "+fsm.getNumTransistions()+"\n");
        //bld.append(".s "+fsm.getNumStates()+"\n");
        //bld.append(".r "+fsm.getResetState());


        bld.append(".end_kiss\n");
        //latch mapping
        bld.append(".latch_order ");


        for (int ii=0; ii<latches.length; ii++){
            bld.append(latches[ii]);
        }
        bld.append("\n");

        // print codes of states
        for (Cluster c : fsm.getClusters()){
            for (State s : c.getStateArray()){
                bld.append(".code "+s.getName()+" "+c.getCode()+s.getCode()+"\n");
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

    private static String[] getLatches(StateMachine fsm){
        String[] latches = null;
        if (fsm==null) {
            latches = new String[1];
            latches[0] = "";
            return latches;
        }


        return latches;

    }

    private static String[] getExternalClusterLatches(StateMachine fsm){
        String[] latches;
        Set<Cluster> clusters = fsm.getClusters();
        latches = new String[clusters.size()];
        int ii=0;
        for (Cluster cluster : clusters){
            latches[ii] = "v"+cluster.getID()+ " ";
        }
        return latches;
    }

    private static String[] getClusterInternalLatches(Cluster cluster){
        String[] latches = null;
        int len;
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
}
