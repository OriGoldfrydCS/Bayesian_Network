import java.util.*;

public class CPT {
    private Map<List<String>, Double> probabilityTable;  // Changed to Map to directly handle probabilities

    public CPT(Node node) {
        this.probabilityTable = new HashMap<>();
    }

    public CPT(CPT other) {
        this.probabilityTable = new HashMap<>(other.probabilityTable);  // Deep copy if necessary
    }

    public void setProbability(List<String> key, Double value) {
        this.probabilityTable.put(new ArrayList<>(key), value);  // Ensure key is copied if mutable
    }

    public Map<List<String>, Double> getProbabilityTable() {
        return new HashMap<>(this.probabilityTable);  // Return a copy to prevent external modifications
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CPT{");
        sb.append(", probabilityTable=").append(probabilityTable);
        sb.append('}');
        return sb.toString();
    }
}
