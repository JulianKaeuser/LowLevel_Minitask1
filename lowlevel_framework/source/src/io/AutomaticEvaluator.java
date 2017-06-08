package io;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Julian Käuser on 08.06.2017.
 * Attention:
 */
public class AutomaticEvaluator {

    private String outputPath;

    private static String abcLocation;

    static void setPath (){
        String os = System.getProperty("os.name").toLowerCase();
        String offset = "";
        if (os.indexOf("win") >= 0){
            // is windows
            offset="\\..\\..\\..\\abc\\windows\\abc10216.exe";
        }
        else if ((os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0 )){
            // is unix system
            offset="/../../../abc/linux/abc.bin";
        }
        else {
            System.err.println("System is not supported: "+os+"; abc not runnable");
            return;
        }
        abcLocation = AutomaticEvaluator.class.getProtectionDomain().getCodeSource().getLocation().getPath()+offset;
    }

    public String getOutputPath(){
        return outputPath;
    }

    public void setOutputPath(String path){
        this.outputPath = path;
    }

    /**
     * Does the automated abc running with the (encoded=processed) .blif files in the specified directory.
     * Attention: Might take some time...
     * @param pathToInputFiles the directory with all processed files
     * @param abcParams the parameters for the abc
     */
    public void automatedAnalysis (String pathToInputFiles, String[] abcParams){

        File dir = new File(pathToInputFiles);
        if (!dir.isDirectory()){
            System.err.println("not a directory - automatedAnalysis");
            return;
        }
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.endsWith(".blif")) return true;
                return false;

            }
        };
        File[] files = dir.listFiles(filter);


        for (File file : files){
            System.out.println("starting abc with file "+file.getName());
            List<String> commands = new LinkedList<String>();
            commands.add(abcLocation);
            commands.add(file.getAbsolutePath()); //is that right? command order?
            for (String str : abcParams){
                commands.add(str);
            }
            ProcessBuilder p = new ProcessBuilder(commands);
            p.directory(new File(outputPath));
            File log = new File (file.getName()+"log");
            p.redirectErrorStream(true);
            p.redirectOutput(ProcessBuilder.Redirect.appendTo(log));
            try {
                Process proc = p.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // so hoffen wa mal dass das geht, nech?
            System.out.println("abc'ing file"+file.getName()+" successful");
        }

    }

    public void automatedAnalysis (String pathToInputFiles, String[] abcParams, int maxInputs, int lutSize){

    }


}
