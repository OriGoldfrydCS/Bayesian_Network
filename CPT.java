import java.util.*;

public class CPT {
    private Node node;
    private List<Variable> parents;
    private Map<List<String>, Double> probabilityTable;

    public CPT(Node node) {
        this.node = node;
        this.parents = new ArrayList<>();
        this.probabilityTable = new HashMap<>();
    }

    public CPT(CPT other) {
        this.node = other.getNode();
        this.parents = new ArrayList<>(other.getParents());
        this.probabilityTable = new HashMap<>(other.getProbabilityTable());
    }

    public void addParent(Variable parent) {
        this.parents.add(parent);
    }

    public void removeParent(Variable parent) {
        this.parents.remove(parent);
    }

    public void setProbability(List<String> key, double probability) {
        this.probabilityTable.put(key, probability);
    }

    public double getProbability(List<String> key) {
        return this.probabilityTable.getOrDefault(key, 0.0);
    }

    public Node getNode() {
        return node;
    }

    public List<Variable> getParents() {
        return parents;
    }

    public Map<List<String>, Double> getProbabilityTable() {
        return probabilityTable;
    }

    @Override
    public String toString() {
        return "CPT{" +
                "node=" + node +
                ", parents=" + parents +
                ", probabilityTable=" + probabilityTable +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CPT cpt = (CPT) o;
        return Objects.equals(node, cpt.node) &&
                Objects.equals(parents, cpt.parents) &&
                Objects.equals(probabilityTable, cpt.probabilityTable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, parents, probabilityTable);
    }
}