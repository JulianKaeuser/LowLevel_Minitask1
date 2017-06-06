package main;

import io.Parser;
import io.StateMachineWriter;
import lowlevel.*;

import java.io.File;
import java.io.FilenameFilter;

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
			/*
			for(int i=0;i<fsm.getStates().length;i++){
				System.out.println("State: "+fsm.getStates()[i].getName());
				for(StateTransition aTras : fsm.getStates()[i].getOutgoingTransitions()){
					System.out.println("Transition to: "+aTras.getTarget().getName());

				}
			}*/


		//	fsm.getStates()[0].getOutgoingTransitions();

			StateMachine myStateMachine = new StateMachine(fsm);
		/*	for(State aState : fsm.getStates()){
				myStateMachine.addState(aState);
			} */
			myStateMachine.combineClusters(6);
			myStateMachine.debugPrintClusters();
			//myCluster.addState(fsm.getStates()[0]); //DAS kommentar !!!

			// here the output file of the state machine should be printed
			/*
			ClusterEncoder.assignClusterCodes(myStateMachine);
			for (Cluster cluster : myStateMachine.getClusters()){
				ClusterEncoder.encodeCluster(cluster, Encoding.BINARY);
			}
			StateMachineWriter.writeFSM(myStateMachine, System.lineSeparator()+"output");
			*/
		}
		else{
			System.out.println("No input argument given");
		}
	}
}
