package lowlevel;

import java.util.*;

/**
 * Created by Julian Käuser on 03.06.2017.
 */
public class StateMachine {

    public String name;

    private Set<Cluster> clusters;
    private Cluster defaultCluster = null; // der ResteCluster für alle Cluster die sich nicht richtig Clusteren lassen (d.h. alle Cluster die N nicht erfüllen oder nur einen Zustand haben)

    public StateMachine(ParsedFile fsm){
        this.clusters= new HashSet<Cluster>();
        for(State aState : fsm.getStates()){
            this.addState(aState);
        }
    }

    public void addState(State newState){
        Cluster newCluster = new Cluster(newState);
        newCluster.setName(newState.getName()); //just for debug
        this.clusters.add(newCluster);
    }

    //kombiniert die Cluster so das es nur Cluster gibt deren Eingänge plus Zustände kleiner gleich N sind AUßER der erste Cluster der alle "Reste" hällt!
    public void combineClusters(int N){
        //nimm dir einen Cluster un versuch Ihn mit seinem Nachbarn zu verschmelzen. Wenn es geht enferne den Nachbarn aus der todoliste und versuch ihn weiter zu verschmelzen bis nichts mehr geht
        List<Cluster> todoList = new ArrayList<Cluster>(this.clusters);
        while(!todoList.isEmpty()){
            Cluster myCluster = todoList.remove(0);

        /*    String retStr="";
            for (State s: myCluster.getStates()){
                retStr += " "+ s.getName();
            } */
        //    System.out.println("TAKE CLUSTER WITH STATES"+retStr);
            System.out.println("TAKE CLUSTER "+myCluster.getName());
            for (Iterator<Transition> iter = myCluster.getOutgoingInterClusterTransitionsAsList().listIterator(); iter.hasNext(); ) {
                Transition trans = iter.next();
                Cluster otherCluster = trans.getTarget().getCluster();
                if(otherCluster!=myCluster) {
                    if (myCluster.tryToCombineClusterWithOtherCluster(otherCluster, N)){ //wenn ich kombiniere löschen !
                        this.clusters.remove(otherCluster);
                        System.out.println("COMBINE WITH CLUSTER " + otherCluster.getName());
                        todoList.remove(otherCluster);
                    }
                    /*
                    retStr="";
                    for (State s: otherCluster.getStates()){
                        retStr += " "+ s.getName();
                    }
                    System.out.println("ADD STATES"+retStr);
                    */
                    /*
                    if (myCluster.getN() == N) { //ACHTUNG, wir können noch Zustände aufnehmen und gleichgut bleien UND auch besser werden !
                        System.out.println("CLUSTER IS DONE");
                        break;
                    }
                    */
                }
            }
        }

        //jetzt gibt es Cluster die gut sortiert sind (N erfüllen und mehere Zustände haben), solche die nicht N erfüllen und sollche die nur 1 Zustand haben
        //wir packen alle Cluster die
        this.defaultCluster = new Cluster();
        defaultCluster.setName("Default Cluster");
        List<Cluster> removeList = new ArrayList<Cluster>(); //bugfix, super umständlich aber mir gerade egal weil es nicht viel laufzeit frist und java problem ist
        for(Cluster cluster : this.clusters){
            if((cluster.getN()> N) || (cluster.getNumberOfStates()==1)){
                System.out.println("CLUSTER N IS "+cluster.getN());
                removeList.add(cluster);
                this.defaultCluster.forceCombineCluster(cluster);
            }
        }
        for(Cluster cluster: removeList){
            this.clusters.remove(cluster);
        }
    }

    public void debugPrintClusters(){
        System.out.println("NOW PRINTING CLUSTER STRUCTURE");
        int i=0;
        for(Cluster cluster: this.clusters){
            System.out.println("CLUSTER "+i+" HAS N OF "+ cluster.getN() +" AND CONTAINS THE FOLLOWING STATES");
            for(State state: cluster.getStates())
                System.out.println(state.getName());
            i++;
        }
        System.out.println("DEFAULT CLUSTER HAS N OF "+this.defaultCluster.getN()+" AND CONTAINS THE FOLLOWING STATES");
        for(State state: this.defaultCluster.getStates())
            System.out.println(state.getName());
    }

    //todo
    public String getEncoding(){
        return null;
    }

    public int getNumTransitions(){
        return 0;
    }

    public int getNumStates(){
        return 0;
    }

    // todo
    public int getNumInputs() {
        return 0;
    }


    public Set<Cluster> getClusters() {
        return clusters;
    }

    // todo
    public int getNumOutputs() {
        return 0;
    }

    public State getResetState() {
        return null;
    }

    public Transition[] getTransitions() {
        return null;
    }
}
