import java.util.*;


/**
 * Represents the Conditional Probability Table (CPT) for a node in a Bayesian Network.
 * The CPT maps combinations of variable states to their corresponding probabilities.
 */
public class CPT implements Cloneable {
    private Map<List<String>, Double> probabilityTable;     // probabilityTable stores the conditional probabilities for various states

    /**
     * Constructs a CPT for the given node.
     * @param node The node for which this CPT is being created.
     */
    public CPT(Node node) {
        this.probabilityTable = new HashMap<>();
    }

    /**
     * Copy constructor to create a CPT by copying another CPT.
     * @param other The CPT to copy.
     */
    public CPT(CPT other) {
        this.probabilityTable = new HashMap<>(other.probabilityTable);  // Deep copy if necessary
    }

    /**
     * Sets the probability for a given combination of variable states.
     * @param key The combination of variable states as a list of strings.
     * @param value The probability associated with the combination of variable states.
     */
    public void setProbability(List<String> key, Double value) {
        this.probabilityTable.put(new ArrayList<>(key), value);  // Ensure key is copied if mutable
    }

    /**
     * Retrieves the probability table.
     * @return A copy of the probability table to prevent external modifications.
     */
    public Map<List<String>, Double> getProbabilityTable() {
        return new HashMap<>(this.probabilityTable);  // Return a copy to prevent external modifications
    }

    /**
     * Provides a string representation of the CPT.
     * @return A string representation of the CPT.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CPT{");
        sb.append(", probabilityTable=").append(probabilityTable);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Creates a clone of the CPT.
     * @return A cloned instance of the CPT.
     */
    @Override
    public CPT clone() {
        try {
            CPT clone = (CPT) super.clone();             // Create a shallow copy of this CPT object
            clone.probabilityTable = new HashMap<>();    // Initialize a new probability table for the clone

            // Deep copy each entry in the probability table
            for (Map.Entry<List<String>, Double> entry : this.probabilityTable.entrySet()) {
                clone.probabilityTable.put(new ArrayList<>(entry.getKey()), entry.getValue());
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
