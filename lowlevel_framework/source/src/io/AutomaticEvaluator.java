package io;

/**
 * Created by Julian Käuser on 08.06.2017.
 * Attention:
 */
public class AutomaticEvaluator {

    private String outputPath;

    private static final String abcLocation;

    static setPath (){
        String os = System.getProperty("os.name").toLowerCase();
        String offset = "";
        if (os.indexOf("win") >= 0){
            // is windows
            offset="\\..\\..\\..\\abc\\windows\\abc10216.exe";
        }
        else if ((os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0 ){
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

    public void automatedAnalysis (String pathToInputFiles, String[] abcParams){


    }


}
