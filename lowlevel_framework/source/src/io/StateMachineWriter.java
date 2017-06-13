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
        StringBuilder bld = new StringBuilder();
        bld.append("# "+fsm.name +" encoded cluster-wise\n"); //commentar, therefore correct
        bld.append(".model "+fsm.name); // Correct for BLIF

        //in and outputs for the _logic_ model
        bld.append(".inputs "+buildInputs(fsm)); // correct for BLIF
        bld.append(".outputs "+buildOutputs(fsm)); // Correct for BLIF

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
        bld.append("# transition section begins\n");

        fsm.initFlipFlops();
        // all latches in all clusters for transitions
        Map<Cluster, Map<Transition, FlipFlop>> outTransMap = new HashMap<>();
        for (Cluster cl : fsm.getClusters()){
            bld.append(".latch "+cl.getIsActiveFlipFlop().input+ " "+cl.getIsActiveFlipFlop().output+ " re clk 0\n");
            for (FlipFlop ff : cl.getStateBitFFs()){
                bld.append(".latch "+ff.input+ " "+ ff.output+ " re clk 0\n");
            }
            for (FlipFlop ff : cl.getOutgoingTransFFs()){
                bld.append(".latch "+ff.input+ " "+ ff.output+ " re clk 0\n");
            }
            outTransMap.put(cl, cl.getOutputTransitionOrigins());
        }


        // write transitions for cluster activeness //tut was es soll
        for (Cluster cl : fsm.getClusters()){
            String str = "";
            int stateBits=0;
            for (FlipFlop ff : cl.getStateBitFFs()){
                stateBits++;
                str += " "+ff.output;
            }
            String inInterTrans = "";
            for (Transition trans : cl.getIncomingInterClusterTransitions()){
                for (Cluster cl2 : fsm.getClusters()) {
                    if (cl2.isStateInCluster(trans.getOrigin())){
                        inInterTrans+= outTransMap.get(cl2).get(trans).output+" ";
                    }
                }
            }
            // stateBits+inputs+inInterTrans+isActiveSelf+output
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
                    String inputs = Helper.longToOutputString(trans.getInput());
                    if (cl.isStateInCluster(trans.getTarget())){
                        bld.append(stateBitsVector+inputs+inInterTransString+"1 1\n");
                    }
                }
            }
            bld.append("\n");
        }

        //write transitions for each output transition flipflop  // tut was es soll
        for (Cluster cl : fsm.getClusters()){
            Map<Transition, FlipFlop> outGoingTransitionFFs = outTransMap.get(cl);
            String str = "";
            int stateBits=0;
            for (FlipFlop ff : cl.getStateBitFFs()){
                stateBits++;
                str += " "+ff.output;
            }
            for (FlipFlop ff : cl.getOutgoingTransFFs()){
                // stateBits+inputs+isActive=outgoingTransitionOutput
                bld.append(".names "+str+" "+buildInputs(fsm)+" "+cl.getIsActiveFlipFlop().output+" "+ff.input+"\n");
                for(Transition trans : cl.getOutgoingInterClusterTransitions()){
                    if(outGoingTransitionFFs.get(trans).equals(ff)) {
                        bld.append(fsm.getStateCodes().get(trans.getOrigin()) + Helper.longToOutputString(trans.getInput()) + "1 1\n");
                    }
                }
                bld.append("\n");
            }
        }

        //write transitions for each state bit
        for (Cluster cl : fsm.getClusters()){
            String str = "";
            int stateBits=0;
            for (FlipFlop ff : cl.getStateBitFFs()){
                stateBits++;
                str += " "+ff.output;
            }
            String inInterTrans = "";
            for (Transition trans : cl.getIncomingInterClusterTransitions()){
                for (Cluster cl2 : fsm.getClusters()) {
                    if (cl2.isStateInCluster(trans.getOrigin())){
                        inInterTrans+= outTransMap.get(cl2).get(trans).output+" ";
                    }
                }
            }
            for (FlipFlop ff : cl.getStateBitFFs()){
                // stateBits+inputs+inComingTrans+isActive=ffInput
                bld.append(".names "+str+" "+buildInputs(fsm)+" "+inInterTrans+cl.getIsActiveFlipFlop().output+" "+ff.input+"\n");
                for (State st : cl.getStates()){
                    for (Transition trans : st.getIncomingTransitions()){
                        if (cl.getIncomingInterClusterTransitions().contains(trans)){
                            // is intercluster, set state bits to dont care
                            String dcStateBits = "";
                            for (int ii=0; ii<cl.getStateBitFFs().length; ii++){ dcStateBits+="-";}
                            bld.append(dcStateBits+Helper.longToOutputString(trans.getInput())+);
                        }
                    }
                }
            }
        }

        // write transitions for  todo
        for (Cluster cl : fsm.getClusters()){
            String str = "";
            for (FlipFlop ff : cl.getStateBitFFs()){
                str += " "+ff.output;
            }
            for (Transition trans : cl.getOutgoingInterClusterTransitions()){
                bld.append(".names"+str+buildInputs(fsm)+" "+cl.getIsActiveFlipFlop().output+"\n");
                // input lines which indicate that this transition is active
                String  stateBits = fsm.getStateCodes().get(trans.getOrigin());
                String transInput = " "+parseBack(trans.getInput());
                bld.append(stateBits+transInput+" "+cl.getIsActiveFlipFlop().output+"\n");
            }
        }

        bld.append("# transition section ending\n#outpout section begin\n");

        /*
        begin output section
         */

        bld.append("#output section ending");
        // end output section
        bld.append("\n");
        destination = (destination.endsWith(System.lineSeparator())) ? destination : (destination + System.lineSeparator());
        destination += fsm.name+"_clusterEncoded.blif";

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

    /**
     * Returns the symbolic encoding of the states for the commentary section - just for readability reasons
     * @param fsm the state machine
     * @return A string holding the encoding as commentars headed with "#" character
     */
    public String getSymbolicCodes(StateMachine fsm){
        StringBuilder bld = new StringBuilder();
        bld.append("# state codes");

        for (Cluster cluster : fsm.getClusters()){
            for (State state : cluster.getStates()){
                bld.append("# "+ state.getName()+ " ");
            }
        }
        return bld.toString();
    }

    /**
     * # relevant:
     * writes the latches associated with the isActive Bit in each cluster
     * @param bld
     * @param fsm
     * @return
     */
    public static Map<Cluster, String> writeClusterLatches(StringBuilder bld, StateMachine fsm){

        HashMap<Cluster, String> map = new HashMap<Cluster, String>();
        int ii=0;
        for (Cluster cluster : fsm.getClusterCodes().keySet()){
            bld.append(".latch next_cluster"+ii+ " cluster_"+ii+ " re clk 0\n");
            map.put(cluster, "cluster_"+ii);
            ii++;
        }
        return map;
    }

    /**
     * #relevant
     * Writes the latches associated with the binary encoding in every cluster
     * @param bld
     * @param fsm
     */
    public static void writeStateLatches(StringBuilder bld, StateMachine fsm){
        int ii=0;
        for (Cluster cluster : fsm.getClusters()){
            int iiMax = (int)Math.ceil(Math.log(cluster.getNumberOfStates())/Math.log(2));
            for (; ii<iiMax; ii++) {
                bld.append(".latch next_cluster" + ii + "StateBit cluster_" + ii + "stateBit re clk 0\n");
            }
        }
    }

    /**
     * #relevant
     *
     * @param bld
     * @param fsm
     */
    public void writeTransitions(StringBuilder bld, StateMachine fsm, Map<Cluster, String> clusterStringMap){
        int ii= 0;
        for (Cluster cluster : fsm.getClusters()){
            bld.append(".names "+)
        }
    }

    private static String parseBack(long input){
        return Helper.longToOutputString(input);
    }
}
