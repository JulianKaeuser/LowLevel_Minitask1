package main;

import io.AutomaticEvaluator;
import io.Parser;
import io.StateMachineWriter;
import lowlevel.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static java.awt.SystemColor.text;


/**
 * Main class
 * @author Wolf & Gottschling
 *
 */
public class Main {

	public static void main(String[] args) throws IOException {

		String relPath = "\\lowlevel_framework\\benchmarks\\kiss_files\\";
		String userDir = System.getProperty("user.dir");
		String path = userDir+relPath;
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		openClusterBenchmakrFile();

		for (File file : listOfFiles) {
			String file_name = file.getName();
			if(file_name.endsWith(".kiss2")|| file_name.endsWith(".kiss")){
				String input_file_name = userDir+relPath+file_name;
				Parser p = new Parser();
				p.parseFile(input_file_name);
				ParsedFile fsm = p.getParsedFile();
				StateMachine myFSM = new StateMachine(fsm);

				List<Cluster> clusterBefore = new ArrayList<Cluster>(myFSM.getClusters());

			//	System.out.println(" starting Simulated Annealing");
				SimulatedAnnealing sa = new SimulatedAnnealing();
				ClusterFitnessFunction ff = new ClusterFitnessFunction(fsm.getNumInputs());


				List<Cluster> best_result=null;
				// BUGFIX :D
				for(int i=0;i<100;i++) {
					int retVar = 0;
					List<Cluster> result = null;
					while (retVar == 0) {
						result = sa.findClustering(fsm, ff, 1);
						retVar = result.size(); //irgendein Bug den ich nicht verbrochen habe ist hier drin.Bugfix
					}
					if(best_result==null || result.size()<best_result.size()){
						best_result=result;
					}
				}
				saveClusterBenchmark(file_name, best_result, clusterBefore);

				//if()
			//	printClusterList(clusterBefore);
				break;
			//	saveClusterBenchmark(file_name, result, clusterBefore);
			}
		}


		//keine Zeit mehr das ding zu verstehen oder zu reperaieren
		/*
		if(args.length>0){
			System.out.println(" Current working directory : " + System.getProperty("user.dir"));
			
			String input_file_name = args[0];
			String algo_name = input_file_name;
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
		//	System.out.println(fsm);
			

			
			// TODO - here you go

			StateMachine ourFSM = new StateMachine(fsm);
			List<Cluster> clusterBefore = new ArrayList<Cluster>(ourFSM.getClusters());
			System.out.println(" starting Simulated Annealing");
			SimulatedAnnealing sa = new SimulatedAnnealing();
			ClusterFitnessFunction ff = new ClusterFitnessFunction(fsm.getNumInputs());
			List<Cluster> result = sa.findClustering(fsm, ff, 1);

			saveClusterBenchmark(algo_name, result, clusterBefore);

			/*
			System.out.println(" starting Simulated Annealing");
			SimulatedAnnealing sa = new SimulatedAnnealing();
			ClusterFitnessFunction ff = new ClusterFitnessFunction(fsm.getNumInputs());
			List<Cluster> result = sa.findClustering(fsm, ff, 1);

			StateMachine ourFSM = new StateMachine(fsm);
			ourFSM.name = args[0].replace(".", "_");
			ourFSM.addClusteredList(result);
			ourFSM.assignCodes();
			printCodes(ourFSM);

			printClusterList(result);
			StateMachineWriter.writeFSM(ourFSM, path);

		}
		else{
			System.out.println("No input argument given");
		}

		System.out.println("Beginning Automation");
		String[] params = {};
		AutomaticEvaluator aut = new AutomaticEvaluator();

		String userDir = System.getProperty("user.dir")+"\\lowlevel_framework\\abc";
		aut.setOutputPath(userDir+"\\abc_output");
		System.out.println(userDir);
		aut.automatedAnalysis(userDir, params);

		*/
	}

	public static void openClusterBenchmakrFile() {
		String text = "Algorithm,LUT All-One-Hot,LUT Cluster,FF All-One-Hot,FF Cluster";
		try {
			writeIntoBenchmakrFile(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeIntoBenchmakrFile(String text) throws IOException {
		String relPath = "\\lowlevel_framework\\benchmarks\\kiss_files\\";
		String userDir = System.getProperty("user.dir");
		String path = userDir+relPath;

		String filePath = path+"output.csv";


		FileWriter writer = new FileWriter(filePath, true);
		writer.write(text+"\n");
		writer.flush();
		writer.close();



		//Files.write(file, text.getBytes(), StandardOpenOption.WRITE);
	}

	public static void saveClusterBenchmark(String algoName, List<Cluster> sorted_clusters, List<Cluster> unsorted_clusters){
		String text="";
		text+=algoName;
	//	System.out.println(algoName); //Algorithm
		text+=unsorted_clusters.size();
	//	System.out.println(unsorted_clusters.size()); //LUT All-One-Hot
		text+=sorted_clusters.size();
	//	System.out.println(sorted_clusters.size()); //LUT Cluster
		text+=unsorted_clusters.size()*2;
	//	System.out.println(unsorted_clusters.size()*2); //FF All-One-Hot
		int ff_sorted=0;
		for(Cluster c:sorted_clusters){
			ff_sorted+=(Math.log(c.getNumberOfStates())/Math.log(2))+1;
		}
		text+=unsorted_clusters.size()*2;
	//	System.out.println(ff_sorted); //FF Cluster

		try {
			writeIntoBenchmakrFile(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	/**
	 * Prints the codes of the given fsm to the console
	 * @param fsm
	 */
	public static void printCodes(StateMachine fsm){
		Map<Cluster, String> clusterCodes = fsm.getClusterCodes();
		Map<State, String> stateCodes = fsm.getStateCodes();

		int ii=0;
		for (Cluster cl : clusterCodes.keySet()){
			System.out.println("[Main:printCodes] Cluster: "+ii+ "; code: "+clusterCodes.get(cl));
			ii++;
			for (State state : cl.getStates()){
				System.out.println("  state: "+state.getName()+ "; code: "+ stateCodes.get(state));
			}
		}

	}
}
