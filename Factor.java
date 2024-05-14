import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Factor {
    private List<String> variables;
    private Map<List<String>, Double> probabilityTable;

    public Factor(List<String> variables, Map<List<String>, Double> probabilityTable) {
        this.variables = new ArrayList<>(variables);
        this.probabilityTable = new HashMap<>(probabilityTable);
    }

    public Factor applyEvidence(Map<String, String> evidence) {
        Map<List<String>, Double> newTable = new HashMap<>();
        for (Map.Entry<List<String>, Double> entry : this.probabilityTable.entrySet()) {
            if (matchesEvidence(entry.getKey(), evidence)) {
                newTable.put(new ArrayList<>(entry.getKey()), entry.getValue());
            }
        }
        return new Factor(this.variables, newTable);
    }

    private boolean matchesEvidence(List<String> assignment, Map<String, String> evidence) {
        return evidence.entrySet().stream()
                .allMatch(e -> {
                    int index = variables.indexOf(e.getKey());
                    return index != -1 && assignment.get(index).equals(e.getValue());
                });
    }

    public Factor combine(Factor other, String eliminateVar) {
        System.out.println("Combining Factors: " + this + " with " + other);
        List<String> newVars = new ArrayList<>(this.variables);
        other.variables.stream().filter(v -> !newVars.contains(v)).forEach(newVars::add);

        Map<List<String>, Double> newTable = new HashMap<>();
        for (Map.Entry<List<String>, Double> e1 : this.probabilityTable.entrySet()) {
            for (Map.Entry<List<String>, Double> e2 : other.probabilityTable.entrySet()) {
                if (compatibleAssignments(this.variables, e1.getKey(), other.variables, e2.getKey())) {
                    List<String> newKey = combineKeys(this.variables, e1.getKey(), other.variables, e2.getKey(), newVars);
                    newTable.put(newKey, e1.getValue() * e2.getValue());
                }
            }
        }

        Factor newFactor = new Factor(newVars, newTable);
        if (eliminateVar != null) {
            return newFactor.sumOut(eliminateVar);
        }
        return newFactor;
    }

    private List<String> combineKeys(List<String> vars1, List<String> key1, List<String> vars2, List<String> key2, List<String> newVars) {
        Map<String, String> assignments = new HashMap<>();
        for (int i = 0; i < vars1.size(); i++) {
            assignments.put(vars1.get(i), key1.get(i));
        }
        for (int j = 0; j < vars2.size(); j++) {
            assignments.putIfAbsent(vars2.get(j), key2.get(j));
        }
        List<String> newKey = new ArrayList<>();
        for (String var : newVars) {
            newKey.add(assignments.get(var));
        }
        System.out.println("Combined keys into: " + newKey);
        return newKey;
    }

    private boolean compatibleAssignments(List<String> vars1, List<String> key1, List<String> vars2, List<String> key2) {
        for (int i = 0; i < key1.size(); i++) {
            int index2 = vars2.indexOf(vars1.get(i));
            if (index2 != -1 && !key1.get(i).equals(key2.get(index2))) {
                System.out.println("Compatibility check between " + key1 + " and " + key2 + ": false");
                return false;
            }
        }
        System.out.println("Compatibility check between " + key1 + " and " + key2 + ": true");
        return true;
    }



    public Factor sumOut(String var) {
        List<String> newVars = new ArrayList<>(this.variables);
        int varIndex = newVars.indexOf(var);
        newVars.remove(var);

        Map<List<String>, Double> newTable = new HashMap<>();
        for (Map.Entry<List<String>, Double> entry : this.probabilityTable.entrySet()) {
            List<String> newKey = new ArrayList<>(entry.getKey());
            newKey.remove(varIndex);
            newTable.merge(newKey, entry.getValue(), Double::sum);
        }

        Factor result = new Factor(newVars, newTable);
        System.out.println("Factor after summing out " + var + ": " + result);
        return result;
    }

    public double getProbability(String var, String value) {
        return this.probabilityTable.entrySet().stream()
                .filter(e -> {
                    int index = variables.indexOf(var);
                    return index != -1 && e.getKey().get(index).equals(value);
                })
                .mapToDouble(Map.Entry::getValue)
                .sum();
    }

    public List<String> getVariables() {
        return new ArrayList<>(variables);
    }

    @Override
    public String toString() {
        return "Factor{" + "variables=" + variables + ", probabilityTable=" + probabilityTable +'}';
    }
}
