package main;

import io.AutomaticEvaluator;
import io.Parser;
import io.StateMachineWriter;
import lowlevel.*;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Main class
 * @author Wolf & Gottschling
 *
 */
public class Main {

	public static void main(String[] args) {
				
		if(args.length>0){
			System.out.println(" Current working directory : " + System.getProperty("user.dir"));
			
			String input_file_name = args[0];
			String relPath = "\\lowlevel_framework\\benchmarks\\kiss_files\\";
			String userDir = System.getProperty("user.dir");
			String path = userDir+relPath;
			input_file_name = userDir+relPath+input_file_name;

			File dir = new File(path);
			System.out.println(path);

			ParsedFile[] inputFiles = null;
			if (args.length>1 && args[1]!=null  && args[1].equals("-all") && dir.isDirectory()){
				// args[0] is a directory, and all files of the directory shall be parsed
				FilenameFilter filter = new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						if (name.endsWith(".kiss") || name.endsWith(".kiss2")){
							return true;
						}
						return false;
					}
				};

				inputFiles = new ParsedFile[dir.listFiles(filter).length];

				int ii=0;
				for (File file : dir.listFiles(filter)) {
					Parser p = new Parser();
					p.parseFile(file.getAbsolutePath());
					inputFiles[ii]=p.getParsedFile();
					ii++;
				}
			}// from this point on one could work on the whole folder... just for benchmarking reasons

			if (inputFiles!=null) {
				System.out.println("Number of read files: "+inputFiles.length);
				int ii=0;
				for (ParsedFile pf : inputFiles) {
					System.out.println("file #"+ii+" states count: "+pf.getNum_states()+ "\t # inputs: "+pf.getNumInputs()+ "\t #outputs: "+pf.getNumOutputs());
					ii++;
				}
			}
			Parser p = new Parser();
			p.parseFile(input_file_name);
			
			// Representation of the FSM
			ParsedFile fsm = p.getParsedFile();
			System.out.println(fsm);
			

			
			// TODO - here you go
			System.out.println(" starting Simulated Annealing");
			SimulatedAnnealing sa = new SimulatedAnnealing();
			ClusterFitnessFunction ff = new ClusterFitnessFunction(fsm.getNumInputs());
			List<Cluster> result = sa.findClustering(fsm, ff, 1);

			StateMachine ourFSM = new StateMachine();
			ourFSM.name = args[0];
			ourFSM.addClusteredList(result);
			ourFSM.assignCodes();
			printCodes(ourFSM);

			printClusterList(result);

		}
		else{
			System.out.println("No input argument given");
		}
		/*
		System.out.println("Beginning Automation");
		String[] params = {};
		AutomaticEvaluator aut = new AutomaticEvaluator();

		String userDir = System.getProperty("user.dir")+"\\lowlevel_framework\\abc";
		aut.setOutputPath(userDir+"\\abc_output");
		System.out.println(userDir);
		aut.automatedAnalysis(userDir, params);
		*/
	}


	public static void printClusterList(List<Cluster> list){
		int numStates = 0;
		for (Cluster cl : list){
			numStates += cl.getNumberOfStates();
		}
		System.out.println("[Main:printClusterList] printing new list with "+ numStates + " states, "+list.size()+" clusters");
		int ii=0;
		for (Cluster cl : list){
			System.out.println("[Main] Cluster "+ ii+ "; states: "+ cl.getStates().size()+ "; N: "+cl.getN()+ "; InTrans: "+ cl.getIncomingInterClusterTransitions().size());
			ii++;
		}
		System.out.println("[Main]____states:_______");
		ii=0;
		Set<State> states = new HashSet<State>();
		for (Cluster cl : list){
			System.out.println("[Main] Cluster "+ ii);
			ii++;
			for (State state : cl.getStates()){
				System.out.print("[Main]      state "+state.getName()+ "; ");
				if (!states.add(state)) System.out.print(" double");
				System.out.println("");
			}
		}

	}

	public static void printCodes(StateMachine fsm){
		Map<Cluster, String> clusterCodes = fsm.getClusterCodes();
		Map<State, String> stateCodes = fsm.getStateCodes();

		for (Cluster cl : clusterCodes.keySet()){
			System.out.println("[Main:printCodes] Cluster: "+cl.getName()+ "; code: "+clusterCodes.get(cl));
			for (State state : cl.getStates()){
				System.out.println("  state: "+state.getName()+ "; code: "+ stateCodes.get(state));
			}
		}

	}
}
