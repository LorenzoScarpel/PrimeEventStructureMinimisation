import java.util.ArrayList;

public class PrimeEvent {
    private String id;
    private String label;
    private ArrayList<PrimeEvent> immediateCauses;
    private ArrayList<PrimeEvent> immediateConsequences;
    private ArrayList<PrimeEvent> nonInheritedConflicts;
    private ArrayList<PrimeEvent> inheritedConflicts;
    private Integer depth;

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public ArrayList<PrimeEvent> getImmediateCauses() {
        return immediateCauses;
    }

    public ArrayList<PrimeEvent> getImmediateConsequences() {
        return immediateConsequences;
    }

    public ArrayList<PrimeEvent> getNonInheritedConflicts() {
        return nonInheritedConflicts;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public PrimeEvent(String id, String label) {
        this.id = id;
        this.label = label;
        immediateCauses = new ArrayList<>();
        immediateConsequences = new ArrayList<>();
        nonInheritedConflicts = new ArrayList<>();
        inheritedConflicts = new ArrayList<>();
        depth = -1;
    }

    public void addImmediateCause(PrimeEvent immediateCause){
        immediateCauses.add(immediateCause);
    }

    public void addImmediateConsequence(PrimeEvent immediateConsequence){ immediateConsequences.add(immediateConsequence); }

    public void addNonInheritedConflict(PrimeEvent nonInheritedConflict){ nonInheritedConflicts.add(nonInheritedConflict); }

    public void addInheritedConflicts(ArrayList<PrimeEvent> events){
        for(PrimeEvent event : events){
            this.inheritedConflicts.add(event);
        }
    }

    public ArrayList<PrimeEvent> getInheritedConflicts(){ return inheritedConflicts; }

}
