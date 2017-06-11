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

    //einfache Methode die im wesentlichen wie folgt funktioniert:
    //1.) Nehme einen Cluster0, für alle Cluster "Kinder" auf die der Cluster0 verweist tue folgendes:
    //      Sortiere die anderen ClusterInputTransition nach Ihrer auftritshäufigkeit in eine Liste (d.h. wenn 3 Inputs aus Cluster C3 kommen dann sotiere diese mit Gewichtung 3)
    //
    //      Versuche den Cluster mit allen zu bilden, wenn das nicht geht streiche jenen Cluster die den seltensten Input haben
    //      REPEAT
    public void doClustering(int N){
        List<Cluster> unfinishedClusters = new ArrayList<Cluster>(this.clusters);
        List<Cluster> finishedClusters = new ArrayList<Cluster>();

        while(unfinishedClusters.size()>0) {
            Cluster clusterParent = unfinishedClusters.get(0); //wir nehmen den erstbesten Cluster

            List<Cluster> clusters = new ArrayList<Cluster>(); //die Cluster mit dennen wir arbeiten sind die auf dennen die Transitionen des ClusterParent zeigen
            for (Transition cT : clusterParent.getOutgoingInterClusterTransitionsAsList()) {
                Cluster c = cT.getTarget().getCluster();
                if(unfinishedClusters.contains(c))
                    clusters.add(cT.getTarget().getCluster()); //wir behandeln die cluster nur wenn sie noch nicht behandelt wurden !
            }

            if(clusters.size()==0) { //wenn wir keinen Cluster aus der Transition des Hauptclusters rausholen so können wir den nicht mehr verabeiten
                unfinishedClusters.remove(clusterParent);
                finishedClusters.add(clusterParent);
            }


            clusters = this.sortClusterAcordingToInterClusterTransitionFrequency(clusters); //wir sotieren die cluster so das wir sinnvoll welche entfernen können


            int N_current;
            //probier die cluster zu verbinden, schmeis welche raus wenns net geht
            while (clusters.size() > 0) {
                N_current = Cluster.callculateNofMultipleClusters(clusters);
            //    System.out.println(N_current);
                //wenn es geht mach einen großen Cluster
                if (N >= N_current) {
                //    System.out.println("GEHT!");
                    Cluster cluster_main = clusters.get(0); //wir clusteren in den ersten Cluster
                    for (int i = 1; i < clusters.size(); i++) {
                        Cluster cluster_to_combine = clusters.get(i);
                    //    System.out.println("WE COMBINE!");
                        unfinishedClusters.remove(cluster_to_combine); //alle verbunden aus der liste nehmen
                        cluster_main.combineCluster(cluster_to_combine);
                    }
                    unfinishedClusters.remove(cluster_main); //den gerade erstellten aus der Liste nehmen
                    finishedClusters.add(cluster_main);
                    break;
                }
                clusters.remove(0);//schmeis raus und wiederhole !
            }
        }
        this.clusters = new HashSet<Cluster>(finishedClusters);
    }

    public class Pair<S, T> {
        public S x;
        public T y;

        public Pair(S x, T y) {
            this.x = x;
            this.y = y;
        }
    }
    //sotiert die Cluster so das Cluster die seltene Transitionen haben vorne sind und solche die häufige haben hinten
    private List<Cluster> sortClusterAcordingToInterClusterTransitionFrequency(List<Cluster> clusters) {
        Map<Transition, Pair<Integer, List<Cluster>>> unsortedList = new HashMap<Transition, Pair<Integer, List<Cluster>>>(); //eine Map die Transitins zu anzahl der Cluster die die Transition auslösen verbindet. Wir speicheren den orginal Cluster damit wir am ende wissen wie er zu gewichten ist
        for(Cluster c:clusters) { //für alle Cluster
            for(Transition ct:c.getIncomingInterClusterTransitions()) {
                if(unsortedList.get(ct.getOrigin())==null) { //nehme alle Cluster die auf uns verweisen und merk dir wie viele Verweise es gibt
                    List<Cluster> clusterList = new ArrayList<Cluster>();
                    clusterList.add(c);
                    unsortedList.put(ct, new Pair(1, clusterList));
                }else {
                    unsortedList.get(ct).x=unsortedList.get(ct).x+1;
                    unsortedList.get(ct).y.add(c); //Speicher den Cluster auf den verwiesen wird ab
                    unsortedList.put(ct, unsortedList.get(ct));
                }
            }
        }

        Map<Integer, List<Cluster>> sortedListe = new TreeMap<Integer,List<Cluster>>(); //Eine TreeMap ist immer sotiert :D
        for(Transition key:unsortedList.keySet()) {
            sortedListe.put(unsortedList.get(key).x, unsortedList.get(key).y); //Einfach anzahl nutzen zum sotieren
        }

        List<List<Cluster>> myListOfLists = new ArrayList<List<Cluster>>(sortedListe.values());

        //wir gehen rückwerts durch  die sotierte Liste um damit die seltesten Cluster zuerst einzufügen.
        List<Cluster> orderedClusters = new ArrayList<Cluster>();
        for(int i=myListOfLists.size()-1;i>=0;i--){
            List<Cluster> cl = myListOfLists.get(i);
            for(Cluster c:cl){
                if(!orderedClusters.contains(c)){
                    orderedClusters.add(c);
                }
            }
        }
        return orderedClusters; //sotiert nach unwichtgsten zum wichtigsten !
    }
/*
    //    //sort Cluster according to the nummber of same incoming Transition orignins


        //sort the Cluster according to the nummber of diffrent inputs, most difficult cluster last
        Map<Transition,Integer> unsortedList = new HashMap<Transition,Integer>();
        for(Cluster c:clusterChildren) {
            for(Transition ct:c.getIncomingInterClusterTransitions()) {
                if(unsortedList.get(ct.getOrigin())==null)
                    unsortedList.put(ct.getOrigin().getCluster(), 1);
                else
                    unsortedList.put(ct.getOrigin().getCluster(), unsortedList.get(ct)+1);
            }
        }

        //super lazy sorting :D
        Map<Integer, Transition> sortedListe = new TreeMap<Integer,Transition>();
        for(Transition key:unsortedList.keySet()) {
            sortedListe.put(unsortedList.get(key), key);
        }
        List<Transition> orderdTransitions = new ArrayList<Transition>(sortedListe.values());
 */



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

                removeList.add(cluster);
                this.defaultCluster.combineCluster(cluster);

                System.out.println("CLUSTER N IS "+this.defaultCluster.getN());
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
        if(this.defaultCluster!=null) {
            System.out.println("DEFAULT CLUSTER HAS N OF " + this.defaultCluster.getN() + " AND CONTAINS THE FOLLOWING STATES");

            for (State state : this.defaultCluster.getStates())
                System.out.println(state.getName());
        }else{
            System.out.println("NO DEFAULT CLUSTER NEEDED!");
        }
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

    public List<Transition> getTransitions() {
        List<Transition> allTranstions=new ArrayList<Transition>();
        for(Cluster c: this.clusters){
            for(State s:c.getStates()){
                allTranstions.addAll(s.getTransitions());
            }
        }
        return allTranstions;
    }
}
