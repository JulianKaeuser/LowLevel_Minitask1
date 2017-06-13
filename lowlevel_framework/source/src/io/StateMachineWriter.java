package io;

import lowlevel.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
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
        fsm.assignCodes();
        fsm.initFlipFlops();
        StringBuilder bld = new StringBuilder();
        bld.append("# "+fsm.name +" encoded cluster-wise\n"); //commentar, therefore correct
        bld.append(".model "+fsm.name+"\n"); // Correct for BLIF

        //in and outputs for the _logic_ model
        bld.append(".inputs "+buildInputs(fsm)+"\n"); // correct for BLIF
        bld.append(".outputs "+buildOutputs(fsm)+"\n"); // Correct for BLIF

        bld.append(".m "+ fsm.getNumStates()+" \n"); // is that correct?
        bld.append(".clock clk\n\n"); // correct for BLIF

        // create a commentary section with states and their codes/cluisters and their codes
        for (Cluster cluster : fsm.getClusterCodes().keySet()){
            for (State state : fsm.getStateCodes().keySet()){
                if  (cluster.getStates().contains(state)){
                    bld.append("# "+ state.getName()+ " "+ fsm.getClusterCodes().get(cluster)+ " "+fsm.getStateCodes().get(state)+"\n");
                }
            }
        }
        bld.append("\n# transition section begins\n\n");


        // all latches in all clusters for transitions
        Map<Cluster, Map<Transition, FlipFlop>> outTransMap = new HashMap<>();
        Map<Transition, FlipFlop> interClusterTransitionFFs = new HashMap<Transition, FlipFlop>();
        for (Cluster cl : fsm.getClusters()){
            bld.append(".latch "+cl.getIsActiveFlipFlop().input+ " "+cl.getIsActiveFlipFlop().output+ " re clk 0\n");
            if(cl.getStateBitFFs()!=null) {
                for (FlipFlop ff : cl.getStateBitFFs()) {
                    bld.append(".latch " + ff.input + " " + ff.output + " re clk 0\n");
                }
            }
            if(cl.getOutgoingTransFFs()!=null) {
                int nn=0;
                for (FlipFlop ff : cl.getOutgoingTransFFs()) {
                    interClusterTransitionFFs.put(cl.getOutgoingInterClusterTransitionsAsList().get(nn), ff);
                    bld.append(".latch " + ff.input + " " + ff.output + " re clk 0\n");
                    nn++;
                }
            }
            outTransMap.put(cl, cl.getOutputTransitionOrigins());
        }
        System.out.println(" fliflops in clusters etc set");
        bld.append("\n");

        // write transitions for cluster activeness //tut was es soll
        for (Cluster cl : fsm.getClusters()){
            String str = "";
            int stateBits=0;
            if(cl.getStateBitFFs()!=null) {
                for (FlipFlop ff : cl.getStateBitFFs()) {
                    stateBits++;
                    str += " " + ff.output;
                }
            }
            String inInterTrans = getAllInterClusterInputFFs(cl, fsm, interClusterTransitionFFs);
            //inInterTrans = getAllInterClusterInputFFs( cl, fsm, i);
            // stateBits+inputs+inInterTrans+isActiveSelf+output
            bld.append("#defines active bit of cluster "+cl.getID()+"\n");
            bld.append(".names"+str+" "+buildInputs(fsm)+" "+inInterTrans+cl.getIsActiveFlipFlop().output+" "+cl.getIsActiveFlipFlop().input+"\n");
            //input lines which indicate that this cluster is active
            for (Transition trans : cl.getIncomingInterClusterTransitions()) {

                String stateBitsVector = "";
                for (int ii = 0; ii < stateBits; ii++) {
                    stateBitsVector += "-";
                } ;
                String inInterTransString = "";
                int ii=0;
                for (Transition t2 : cl.getIncomingInterClusterTransitions()){
                    if (trans.equals(t2)) inInterTransString+="1";
                    else inInterTransString+="-";
                }
                bld.append(stateBitsVector + Helper.longToOutputString(trans.getInput()) +inInterTransString +"0 1\n");
            }

            for (State state : cl.getStates()){
                String inInterTransString = "";
                int ii=0;
                for (Transition t2 : cl.getIncomingInterClusterTransitions()){
                    inInterTransString+="-";
                }
                for (Transition trans : state.getOutgoingTransitions()){
                    String stateBitsVector = fsm.getStateCodes().get(state);
                    if(cl.getNumberOfStates()<2) stateBitsVector = "";
                    String inputs = Helper.longToOutputString(trans.getInput());
                    if (cl.isStateInCluster(trans.getTarget())){
                        bld.append(stateBitsVector+inputs+inInterTransString+"1 1\n");
                    }
                }
            }
            bld.append("\n");
        }// did it for active state
        System.out.println(" active state set");

        //write transitions for each output transition flipflop  // tut was es soll
        for (Cluster cl : fsm.getClusters()){
            Map<Transition, FlipFlop> outGoingTransitionFFs = outTransMap.get(cl);
            String str = "";
            int stateBits=0;
            if(cl.getStateBitFFs()!=null) {
                for (FlipFlop ff : cl.getStateBitFFs()) {
                    stateBits++;
                    str += " " + ff.output;
                }
            }
            if(cl.getOutgoingTransFFs()!=null) {
                for (FlipFlop ff : cl.getOutgoingTransFFs()) {
                    // stateBits+inputs+isActive=outgoingTransitionOutput
                    bld.append("# outgoing intercluster transition flipflop of cluster "+cl.getID()+"\n");
                    bld.append(".names " + str + " " + buildInputs(fsm) + " " + cl.getIsActiveFlipFlop().output + " " + ff.input + "\n");
                    for (Transition trans : cl.getOutgoingInterClusterTransitions()) {
                        if (outGoingTransitionFFs.get(trans).equals(ff)) {
                            String insert = fsm.getStateCodes().get(trans.getOrigin());
                                    for(Cluster cl2 : fsm.getClusters()){
                                        insert =  cl2.getNumberOfStates()<2 ?  "" :  insert;
                                    }
                            bld.append(insert + Helper.longToOutputString(trans.getInput()) + "1 1\n");
                        }
                        //bld.append("\n");
                    }
                    bld.append("\n");

                }
            }

        }//did it for outgoing transitions
        System.out.println(" outgoing trans  set");

        //write transitions for each state bit
        for (Cluster cl : fsm.getClusters()){
            String str = "";
            int stateBits=0;
            for (FlipFlop ff : cl.getStateBitFFs()) {
                    stateBits++;
                    str += " " + ff.output;
            }
            // todo da stimmt glaub was nicht
            String inInterTrans = "";
            for (Transition trans : cl.getIncomingInterClusterTransitions()){
                for (Cluster cl2 : fsm.getClusters()) {
                    if (cl2.isStateInCluster(trans.getOrigin())){
                        if(outTransMap.get(cl2)!=null && outTransMap.get(cl2).get(trans)!=null) {
                            inInterTrans += outTransMap.get(cl2).get(trans).output + " ";
                        }
                    }
                }
            }
            int aa=0;

            for (FlipFlop ff : cl.getStateBitFFs()) {
                    bld.append("\n# stateBit transition for cluster "+ cl.getID()+", bit "+ff.output+"\n");
                    // stateBits+inputs+inComingTrans+isActive=ffInput
                    bld.append(".names " + str + " " + buildInputs(fsm) + " " + inInterTrans + cl.getIsActiveFlipFlop().output + " " + ff.input + "\n");
                    for (State st : cl.getStates()) {
                       for (Transition trans : st.getIncomingTransitions()) {
                            String lastBit = "";
                            if(fsm.getStateCodes().get(trans.getOrigin())!=null && fsm.getStateCodes().get(trans.getTarget()).length()>aa) {
                                 lastBit = Character.toString(fsm.getStateCodes().get(trans.getTarget()).charAt(aa));
                            }
                            aa++;
                            if (cl.getIncomingInterClusterTransitions().contains(trans)) {
                                // is intercluster, set state bits to dont care
                                String dcStateBits = "";
                                for (int ii = 0; ii < cl.getStateBitFFs().size(); ii++) {
                                    dcStateBits += "-";
                                }
                                String inTransBitVector = "";
                                for (Transition t2 : cl.getIncomingInterClusterTransitions()) {
                                    if (t2.equals(trans)) inTransBitVector += "1";
                                    else {
                                        inTransBitVector += "-";
                                    }
                                }
                                bld.append(dcStateBits + Helper.longToOutputString(trans.getInput()) + inTransBitVector + "0 " + lastBit + "\n");
                            } else {
                                // in intracluster, take care only of state bits and inputs and isActive. yay
                                String inTransBitVector = "";
                                for (Transition t2 : cl.getIncomingInterClusterTransitions()) {

                                    inTransBitVector += "-";

                                }
                                // stateBits+inputs+inComingTrans+isActive=1
                                bld.append(fsm.getStateCodes().get(trans.getOrigin()) + Helper.longToOutputString(trans.getInput()) + inTransBitVector + "1 " + lastBit + "\n");
                            }
                        }
                    }
            }


        } //did it for state bit
        System.out.println(" state bits set");

        bld.append("\n");
        bld.append("# transition section ending\n\n#output section begin\n");

        /*
        begin output section
         */
        FlipFlop[] outputsFFs = new FlipFlop[fsm.getNumOutputs()];
        for (int ii=0; ii<fsm.getNumOutputs(); ii++){
            outputsFFs[ii]= new FlipFlop();
            outputsFFs[ii].output = "o"+ii+"out";
            outputsFFs[ii].input = "next_o"+ii;
            bld.append("# output "+ii+"\n.latch "+outputsFFs[ii].input+ " "+outputsFFs[ii].output+ " re clk 0\n");
        }
        bld.append("\n");

        // write output combinations.
        int bb=0;
        for (FlipFlop ff : outputsFFs){
            String stateBits = "";
            int stateBitCount = 0;
            String clusterBits ="";
            int clusterBitCount = 0;
            for (Cluster cluster : fsm.getClusters()){
                for (FlipFlop ff2 : cluster.getStateBitFFs()) {
                    stateBits += ff2.output + " ";
                    stateBitCount++;
                }
                clusterBits += cluster.getIsActiveFlipFlop().output + " ";
                clusterBitCount++;

            }

            //header

            bld.append(".names "+stateBits+clusterBits+" "+buildInputs(fsm)+" "+ff.input+"\n\n");
            // output codes depending on transition
            for (State state : fsm.getStateCodes().keySet() ){
                for (Transition trans : state.getTransitions()){

                }
            }
        }

        bld.append("#output section ending");
        // end output section
        bld.append("\n");
        //destination = (destination.endsWith(System.lineSeparator())) ? destination : (destination + System.lineSeparator());
        destination += fsm.name+"_clusterEncoded.blif";

        try (FileWriter out = new FileWriter(destination)) {
            System.out.println("printed");
            BufferedWriter buf = new BufferedWriter(out);
            buf.write(bld.toString());
            buf.flush();
        }
        catch (IOException e){
            e.printStackTrace();
        }
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
            outs+=("out"+ii+" ");
        }
        return outs;

    }



    private static String getAllInterClusterInputFFs(Cluster cl, StateMachine fsm, Map<Transition, FlipFlop> map){
        String str = "";
        for (Transition trans : cl.getIncomingInterClusterTransitionsAsList()){
            str += " "+map.get(trans).output;
        }
        return str;
    }
}
