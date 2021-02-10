import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrimeEventStructure {
    private HashMap<String, PrimeEvent> events;
    private TreeMap<Integer, ArrayList<PrimeEvent>> depths;
    private int maxDepth;
    private HashMap<Integer, ArrayList<ArrayList<ArrayList<PrimeEvent>>>> depthGloballyValidEquivalences;
    private HashMap<Integer, ArrayList<ArrayList<PrimeEvent>>> maximalFoldingEquivalence;

    public PrimeEventStructure(String inputFile){
        events = new HashMap<>();
        depths = new TreeMap<>();
        maximalFoldingEquivalence = new HashMap<>();
        depthGloballyValidEquivalences = new HashMap<>();
        try (Stream<String> stream = Files.lines(Paths.get(inputFile))) {
            ArrayList<String> lines = (ArrayList<String>) stream.collect(Collectors.toList());
            readPrimeEventStructure(lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readPrimeEventStructure(ArrayList<String> lines){
        int i = 0;
        //read events
        while(!lines.get(i).equals("<")){
            String[] labeledEvent = lines.get(i).split(" ");
            events.put(labeledEvent[0], new PrimeEvent(labeledEvent[0], labeledEvent[1]));
            i++;
        }
        i++;
        //read immediate causality relations
        while(!lines.get(i).equals("#")){
            String[] causality = lines.get(i).split(" ");
            events.get(causality[0]).addImmediateConsequence(events.get(causality[1]));
            events.get(causality[1]).addImmediateCause(events.get(causality[0]));
            i++;
        }
        i++;
        //read non inherited conflicts
        while(i < lines.size()){
            String[] conflict = lines.get(i).split(" ");
            events.get(conflict[0]).addNonInheritedConflict(events.get(conflict[1]));
            events.get(conflict[1]).addNonInheritedConflict(events.get(conflict[0]));
            this.propagateConflict(events.get(conflict[0]), events.get(conflict[1]));
            i++;
        }
    }


    private void propagateConflict(PrimeEvent e1, PrimeEvent e2){
        ArrayList<PrimeEvent> e1Consequences = new ArrayList<>();
        ArrayList<PrimeEvent> e2Consequences = new ArrayList<>();
        this.generateConsequences(e1, e1Consequences);
        this.generateConsequences(e2, e2Consequences);
        e1.addInheritedConflicts(e2Consequences);
        e2.addInheritedConflicts(e1Consequences);
        e1Consequences.add(e1);
        e2Consequences.add(e2);
        this.addInheritedConflicts(e1, e2Consequences);
        this.addInheritedConflicts(e2, e1Consequences);
    }

    private void generateConsequences(PrimeEvent e, ArrayList<PrimeEvent> consequences){
        for(PrimeEvent consequence : e.getImmediateConsequences()){
            consequences.add(consequence);
            this.generateConsequences(consequence, consequences);
        }
    }

    private void addInheritedConflicts(PrimeEvent e, ArrayList<PrimeEvent> consequences){
        for(PrimeEvent consequence : e.getImmediateConsequences()){
            consequence.addInheritedConflicts(consequences);
            this.addInheritedConflicts(consequence, consequences);
        }
    }

    public void generateMaximalFoldingEquivalence(){
        computeDepths();
        generateDepthGloballyValidEquivalences();
        generateMaximalFoldingEquivalenceTopDown(new HashMap<>(), 0, new AtomicBoolean(false));
    }

    private void computeDepths(){
        depths.put(0, new ArrayList<PrimeEvent>());
        for(PrimeEvent event : events.values()){
            if(event.getImmediateCauses().isEmpty()){
                event.setDepth(0);
                depths.get(0).add(event);
            }
        }
        boolean allDepthsComputed = false;
        int currentDepth = 0;
        while(!allDepthsComputed){
            allDepthsComputed = true;
            depths.put(currentDepth+1, new ArrayList<>());
            for(PrimeEvent event : depths.get(currentDepth)){
                for(PrimeEvent consequence : event.getImmediateConsequences()){
                    allDepthsComputed = false;
                    consequence.setDepth(currentDepth+1);
                    for(PrimeEvent cause : consequence.getImmediateCauses()){
                        if(cause.getDepth() > currentDepth || cause.getDepth() == -1){
                            consequence.setDepth(-1);
                        }
                    }
                    if(consequence.getDepth() != -1){
                        depths.get(currentDepth+1).add(consequence);
                    }
                }
            }
            if(allDepthsComputed){
                depths.remove(currentDepth+1);
                maxDepth = currentDepth;
            }
            currentDepth++;
        }
    }

    private void generateDepthGloballyValidEquivalences(){
        ArrayList<NGloballyValidEquivalences> threadsDepthGloballyValidEquivalences = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(maxDepth+1);
        for(int n = 0; n <= maxDepth; n++){
            ArrayList<ArrayList<ArrayList<PrimeEvent>>> nGloballyValidEquivalences = new ArrayList<>();
            depthGloballyValidEquivalences.put(n, nGloballyValidEquivalences);
            NGloballyValidEquivalences thread = new NGloballyValidEquivalences(this, n, nGloballyValidEquivalences);
            threadsDepthGloballyValidEquivalences.add(thread);
            executor.execute(thread);
        }
        executor.shutdownNow();
        while(!executor.isTerminated()){
        }
    }

    private boolean checkConflictsEquivalenceClasses(ArrayList<PrimeEvent> equivalenceClass1, ArrayList<PrimeEvent> equivalenceClass2){
        ArrayList<PrimeEvent> equivalenceClass2PairwiseConflicts = new ArrayList<>();
        equivalenceClass2PairwiseConflicts.addAll(equivalenceClass2);
        boolean pairwiseConflicts = true;
        for(PrimeEvent e1 : equivalenceClass1){
            boolean e1PairwiseConflicts = true;
            for(PrimeEvent e2 : equivalenceClass2){
                if(!e1.getNonInheritedConflicts().contains(e2) && !e1.getInheritedConflicts().contains(e2)){
                    equivalenceClass2PairwiseConflicts.remove(e2);
                    pairwiseConflicts = false;
                    e1PairwiseConflicts = false;
                }
            }
            if(e1PairwiseConflicts == true && pairwiseConflicts == false){
                return false;
            }
        }
        if(pairwiseConflicts == false && !equivalenceClass2PairwiseConflicts.isEmpty()){
            return false;
        }

        return true;
    }

    private void generateMaximalFoldingEquivalenceTopDown(HashMap<Integer, ArrayList<ArrayList<PrimeEvent>>> currentEquivalence,
                                                   int depth, AtomicBoolean found){
        for(ArrayList<ArrayList<PrimeEvent>> equivalence : depthGloballyValidEquivalences.get(depth)){
            if(found.get() == true){
                return;
            }
            if(checkConsistency(equivalence)){
                ArrayList<ArrayList<PrimeEvent>> copyEquivalence = copyEquivalence(equivalence);
                currentEquivalence.put(depth, copyEquivalence);
                if(upToNGloballyValidEquivalence(currentEquivalence, depth)){
                    if(depth == maxDepth){
                        this.maximalFoldingEquivalence = (HashMap<Integer, ArrayList<ArrayList<PrimeEvent>>>) currentEquivalence.clone();
                        found.set(true);
                    }else{
                        generateMaximalFoldingEquivalenceTopDown(currentEquivalence, depth+1, found);
                    }
                }
                currentEquivalence.remove(depth);
            }
        }
    }

    private boolean checkConsistency(ArrayList<ArrayList<PrimeEvent>> equivalence) {
        for (ArrayList<PrimeEvent> equivalenceClass : equivalence) {
            HashMap<PrimeEvent, ArrayList<PrimeEvent>> neighbors = new HashMap<>();
            for (PrimeEvent event : events.values()) {
                if (checkEventConsistency(equivalenceClass, event)) {
                    neighbors.put(event, new ArrayList<>());
                }
            }
            for (PrimeEvent event : neighbors.keySet()) {
                for (PrimeEvent event1 : neighbors.keySet()) {
                    if (!event.getInheritedConflicts().contains(event1) && !event.getNonInheritedConflicts().contains(event1) && !event.equals(event1)) {
                        neighbors.get(event).add(event1);
                    }
                }
            }

            AtomicBoolean notExtendableSubset = new AtomicBoolean(false);
            ArrayList<PrimeEvent> consistentEvents = new ArrayList<>(neighbors.keySet());
            generateMaximalConsistentSubsets(new ArrayList<>(), consistentEvents, new ArrayList<>(), notExtendableSubset, neighbors, equivalenceClass);
            if (notExtendableSubset.get() == true)
                return false;
        }
        return true;
    }

    private boolean checkExtendability(ArrayList<PrimeEvent> equivalenceClass, ArrayList<PrimeEvent> subset){
        ArrayList<PrimeEvent> consistentWithAllEvents = new ArrayList<>();
        consistentWithAllEvents.addAll(equivalenceClass);
        for(PrimeEvent e1 : subset){
            for(PrimeEvent e2: equivalenceClass){
                if(e2.getNonInheritedConflicts().contains(e1) || e2.getInheritedConflicts().contains(e1)){
                    consistentWithAllEvents.remove(e2);
                    if(consistentWithAllEvents.isEmpty()){
                        return false;
                    }
                }
            }
        }
        return true;
    }


    private boolean checkEventConsistency(ArrayList<PrimeEvent> equivalenceClass, PrimeEvent event){
        for(PrimeEvent e : equivalenceClass) {
            if (!e.getNonInheritedConflicts().contains(event) && !e.getInheritedConflicts().contains(event)) {
                return true;
            }
        }
        return false;
    }

    private void generateMaximalConsistentSubsets(ArrayList<PrimeEvent> R, ArrayList<PrimeEvent> P,
                                                 ArrayList<PrimeEvent> X, AtomicBoolean notExtendableSubset,
                                                 HashMap<PrimeEvent, ArrayList<PrimeEvent>> neighbors, ArrayList<PrimeEvent> equivalenceClass){
        if(P.isEmpty() && X.isEmpty()){
            if(!checkExtendability(R, equivalenceClass)){
                notExtendableSubset.set(true);
            }
            return;
        }
        ArrayList<PrimeEvent> choosePivot = new ArrayList<>();
        choosePivot.addAll(P);
        for(PrimeEvent event : X){
            if(!P.contains(event))
                choosePivot.add(event);
        }
        Random rand = new Random();
        PrimeEvent u = choosePivot.get(rand.nextInt(choosePivot.size()));
        ArrayList<PrimeEvent> PWithoutNeighborsU = new ArrayList<>();
        PWithoutNeighborsU.addAll(P);
        PWithoutNeighborsU.removeAll(neighbors.get(u));
        for(PrimeEvent v : PWithoutNeighborsU){
            if(notExtendableSubset.get() == true){
                return;
            }
            ArrayList<PrimeEvent> R1 = new ArrayList<>();
            R1.addAll(R);
            if(!R1.contains(v))
                R1.add(v);
            ArrayList<PrimeEvent> P1 = new ArrayList<>();
            for(PrimeEvent event : neighbors.get(v)){
                if(P.contains(event))
                    P1.add(event);
            }
            ArrayList<PrimeEvent> X1 = new ArrayList<>();
            for(PrimeEvent event : neighbors.get(v)){
                if(X.contains(event))
                    X1.add(event);
            }
            generateMaximalConsistentSubsets(R1, P1, X1, notExtendableSubset, neighbors, equivalenceClass);
            if(!X.contains(v)){
                X.add(v);
            }
            P.remove(v);
        }
    }

    private boolean upToNGloballyValidEquivalence(HashMap<Integer, ArrayList<ArrayList<PrimeEvent>>> equivalence, int depth){
        if(depth == 0){
            return true;
        }
        for(ArrayList<PrimeEvent> equivalenceClassN : equivalence.get(depth)){
            for(int i = 0; i < depth; i++){
                for(ArrayList<PrimeEvent> equivalenceClassToN : equivalence.get(i)){
                    if(!checkConflictsEquivalenceClasses(equivalenceClassN, equivalenceClassToN)){
                        return false;
                    }
                }
            }
        }
        if(!checkImmediateCausesTopDown(equivalence, depth) || !checkImmediateConsequencesTopDown(equivalence, depth)){
            return false;
        }
        return true;
    }

    private boolean checkImmediateCausesTopDown(HashMap<Integer, ArrayList<ArrayList<PrimeEvent>>> equivalence, int depth){
        HashMap<String, ArrayList<PrimeEvent>> eventsEquivalenceClasses = new HashMap<>();
        for(ArrayList<ArrayList<PrimeEvent>> nGloballyValidEquivalence : equivalence.values()){
            for(ArrayList<PrimeEvent> equivalenceClass : nGloballyValidEquivalence){
                for(PrimeEvent event : equivalenceClass){
                    eventsEquivalenceClasses.put(event.getId(), equivalenceClass);
                }
            }
        }
        for(ArrayList<PrimeEvent> equivalenceClass : equivalence.get(depth)){
            for(PrimeEvent event : equivalenceClass){
                ArrayList<PrimeEvent> immediateCauses = new ArrayList<>();
                immediateCauses.addAll(equivalenceClass.get(0).getImmediateCauses());
                for(PrimeEvent cause : event.getImmediateCauses()){
                    boolean found = false;
                    for(PrimeEvent causeFirstEvent : equivalenceClass.get(0).getImmediateCauses()){
                        if(eventsEquivalenceClasses.get(causeFirstEvent.getId()).contains(cause)){
                            found = true;
                            immediateCauses.remove(causeFirstEvent);
                        }
                    }
                    if(found == false){
                        return false;
                    }
                }
                if(!immediateCauses.isEmpty()){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkImmediateConsequencesTopDown(HashMap<Integer, ArrayList<ArrayList<PrimeEvent>>> equivalence, int depth){
        HashMap<String, ArrayList<PrimeEvent>> eventsEquivalenceClassesDepth = new HashMap<>();
        for(ArrayList<PrimeEvent> equivalenceClass : equivalence.get(depth)){
            for(PrimeEvent event : equivalenceClass){
                eventsEquivalenceClassesDepth.put(event.getId(), equivalenceClass);
            }
        }
        for(int i = 0; i < depth; i++){
            for(ArrayList<PrimeEvent> equivalenceClass : equivalence.get(i)){
                for(PrimeEvent event : equivalenceClass){
                    ArrayList<PrimeEvent> consequencesFirstEventDepth = new ArrayList<>();
                    for(PrimeEvent consequence : equivalenceClass.get(0).getImmediateConsequences()){
                        if(consequence.getDepth() == depth){
                            boolean found = false;
                            for(PrimeEvent alreadyAdded : consequencesFirstEventDepth){
                                if(eventsEquivalenceClassesDepth.get(consequence.getId()).contains(alreadyAdded)){
                                    found = true;
                                }
                            }
                            if(found == false){
                                consequencesFirstEventDepth.add(consequence);
                            }
                        }
                    }
                    for(PrimeEvent consequence : event.getImmediateConsequences()){
                        if(consequence.getDepth() == depth){
                            boolean found = false;
                            for(PrimeEvent consequenceFirstEvent : equivalenceClass.get(0).getImmediateConsequences()){
                                if(eventsEquivalenceClassesDepth.get(consequenceFirstEvent.getId()).contains(consequence)){
                                    found = true;
                                    for(PrimeEvent c : (ArrayList<PrimeEvent>) consequencesFirstEventDepth.clone()){
                                        if(eventsEquivalenceClassesDepth.get(consequenceFirstEvent.getId()).contains(c)){
                                            consequencesFirstEventDepth.remove(c);
                                        }
                                    }
                                }
                            }
                            if(found == false){
                                return false;
                            }
                        }
                    }
                    if(!consequencesFirstEventDepth.isEmpty()){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private ArrayList<ArrayList<PrimeEvent>> copyEquivalence(ArrayList<ArrayList<PrimeEvent>> equivalence){
        ArrayList<ArrayList<PrimeEvent>> copyEquivalence = new ArrayList<>();
        for(ArrayList<PrimeEvent> equivalenceClass : equivalence){
            ArrayList<PrimeEvent> copyEquivalenceClass = new ArrayList<>();
            for(PrimeEvent e : equivalenceClass){
                copyEquivalenceClass.add(e);
            }
            copyEquivalence.add(copyEquivalenceClass);
        }
        return copyEquivalence;
    }

    public void printMaximalFoldingEquivalence(){
        for(ArrayList<ArrayList<PrimeEvent>> nGloballyValidEquivalence : maximalFoldingEquivalence.values()){
            for(ArrayList<PrimeEvent> equivalenceClass : nGloballyValidEquivalence){
                for(PrimeEvent event : equivalenceClass){
                    System.out.print(event.getId() + " ");
                }
                System.out.println();
            }
        }
    }

    public TreeMap<Integer, ArrayList<PrimeEvent>> getDepths(){
        return depths;
    }
}
