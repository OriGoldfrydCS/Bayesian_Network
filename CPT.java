import java.util.*;

public class CPT {
    private Variable variable;
    private List<Variable> parents;
    private Map<List<String>, Double> probabilityTable;

    public CPT(Variable variable) {
        this.variable = variable;
        this.parents = new ArrayList<>();
        this.probabilityTable = new HashMap<>();
    }

    public void addParent(Variable parent) {
        this.parents.add(parent);
    }

    public void setProbability(List<String> key, double probability) {
        this.probabilityTable.put(key, probability);
    }

    public double getProbability(List<String> key) {
        return this.probabilityTable.getOrDefault(key, 0.0);
    }

    public Variable getVariable() {
        return variable;
    }

    public List<Variable> getParents() {
        return parents;
    }

    @Override
    public String toString() {
        return "CPT{" +
                "variable=" + variable +
                ", parents=" + parents +
                ", probabilityTable=" + probabilityTable +
                '}';
    }
}
