import java.util.ArrayList;

public class NGloballyValidEquivalences implements Runnable {
    private PrimeEventStructure pes;
    private int n;
    private ArrayList<ArrayList<ArrayList<PrimeEvent>>> nGloballyValidEquivalences;

    public NGloballyValidEquivalences(PrimeEventStructure pes, int n, ArrayList<ArrayList<ArrayList<PrimeEvent>>> nGloballyValidEquivalences){
        this.pes = pes;
        this.n = n;
        this.nGloballyValidEquivalences = nGloballyValidEquivalences;
    }
    public void run() {
        generateNGloballyValidEquivalences(n, nGloballyValidEquivalences, new ArrayList<>(), 0);
    }

    private void generateNGloballyValidEquivalences(int n, ArrayList<ArrayList<ArrayList<PrimeEvent>>> nGloballyValidEquivalences,
                                                   ArrayList<ArrayList<PrimeEvent>> currentEquivalence, int index){
        for (ArrayList<PrimeEvent> equivalenceClass : currentEquivalence) {
            if (checkLabelConflicts(pes.getDepths().get(n).get(index), equivalenceClass)) {
                equivalenceClass.add(pes.getDepths().get(n).get(index));
                if (index == pes.getDepths().get(n).size() - 1) {
                    if (checkConflictsN(currentEquivalence)) {
                        nGloballyValidEquivalences.add(copyEquivalence(currentEquivalence));
                    }
                } else {
                    generateNGloballyValidEquivalences(n, nGloballyValidEquivalences, (ArrayList<ArrayList<PrimeEvent>>) currentEquivalence.clone(), index + 1);
                }
                equivalenceClass.remove(equivalenceClass.size() - 1);
            }
        }
        ArrayList<PrimeEvent> newEquivalenceClass = new ArrayList<>();
        newEquivalenceClass.add(pes.getDepths().get(n).get(index));
        currentEquivalence.add(newEquivalenceClass);
        if (index == pes.getDepths().get(n).size() - 1) {
            if (checkConflictsN(currentEquivalence)) {
                nGloballyValidEquivalences.add(copyEquivalence(currentEquivalence));
            }
        } else {
            generateNGloballyValidEquivalences(n, nGloballyValidEquivalences, (ArrayList<ArrayList<PrimeEvent>>) currentEquivalence.clone(), index + 1);
        }
        currentEquivalence.remove(currentEquivalence.size() - 1);
    }

    public boolean checkLabelConflicts(PrimeEvent e, ArrayList<PrimeEvent> equivalenceClass){
        if(!e.getLabel().equals(equivalenceClass.get(0).getLabel())){
            return false;
        }
        for(PrimeEvent e1 : equivalenceClass){
            if(!e.getNonInheritedConflicts().contains(e1) && !e.getInheritedConflicts().contains(e1)){
                return false;
            }
        }
        return true;
    }

    public boolean checkConflictsN(ArrayList<ArrayList<PrimeEvent>> equivalence){
        for(int i = 0; i < equivalence.size(); i++){
            for(int j = i + 1; j < equivalence.size(); j++){
                if(!checkConflictsEquivalenceClasses(equivalence.get(i), equivalence.get(j))){
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkConflictsEquivalenceClasses(ArrayList<PrimeEvent> equivalenceClass1, ArrayList<PrimeEvent> equivalenceClass2){
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

    public ArrayList<ArrayList<PrimeEvent>> copyEquivalence(ArrayList<ArrayList<PrimeEvent>> equivalence){
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

}
