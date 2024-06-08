import java.util.*;

public class CPT {
//    private Node node;
//    private List<Node> parents;
    private Map<List<String>, Double> probabilityTable;  // Changed to Map to directly handle probabilities

    public CPT(Node node) {
//        this.node = node;
//        this.parents = new ArrayList<>();
        this.probabilityTable = new HashMap<>();
    }

    public CPT(CPT other) {
//        this.node = other.getNode();
//        this.parents = new ArrayList<>(other.getParents());
        this.probabilityTable = new HashMap<>(other.probabilityTable);  // Deep copy if necessary
    }

//    public void addParent(Node parent) {
//        this.parents.add(parent);
//    }
//
//    public void removeParent(Node parent) {
//        this.parents.remove(parent);
//    }

    public void setProbability(List<String> key, Double value) {
        this.probabilityTable.put(new ArrayList<>(key), value);  // Ensure key is copied if mutable
    }

    public Map<List<String>, Double> getProbabilityTable() {
        return new HashMap<>(this.probabilityTable);  // Return a copy to prevent external modifications
    }

//    public Node getNode() {
//        return node;
//    }
//
//    public List<Node> getParents() {
//        return new ArrayList<>(parents);  // Return a copy to avoid external modifications
//    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CPT{");
//        sb.append("node=").append(node);
//        sb.append(", parents=").append(parents);
        sb.append(", probabilityTable=").append(probabilityTable);
        sb.append('}');
        return sb.toString();
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof CPT)) return false;
//        CPT cpt = (CPT) o;
//        return Objects.equals(node, cpt.node) &&
//                Objects.equals(parents, cpt.parents) &&
//                Objects.equals(probabilityTable, cpt.probabilityTable);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(node, parents, probabilityTable);
//    }
}
