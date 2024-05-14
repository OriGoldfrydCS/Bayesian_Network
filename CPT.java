import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CPT implements Comparable<CPT> {
    private final Variable variable;
    private final List<Variable> parents;
    private final List<List<String>> given;
    private final List<Double> probabilities;
    private int size;

    public CPT(Variable variable, List<Variable> parents, String[] probStrings) {
        this.variable = variable;
        this.parents = new ArrayList<>(parents);
        this.given = new ArrayList<>();
        this.probabilities = new ArrayList<>();

        // Building the given configurations
        int parentCombinations = 1;
        for (Variable parent : parents) {
            parentCombinations *= parent.getOutcomes().size();
        }

        for (int i = 0; i < probStrings.length; i++) {
            List<String> config = new ArrayList<>();
            int index = i;
            for (Variable parent : parents) {
                config.add(parent.getOutcomes().get(index % parent.getOutcomes().size()));
                index /= parent.getOutcomes().size();
            }
            config.add(variable.getOutcomes().get(i / parentCombinations));
            given.add(config);
            probabilities.add(Double.parseDouble(probStrings[i]));
        }
        this.size = given.size();
    }

    public CPT(CPT other) {
        this.variable = new Variable(other.variable.getName(), other.variable.getOutcomes());
        this.parents = new ArrayList<>();
        for (Variable parent : other.parents) {
            this.parents.add(new Variable(parent.getName(), parent.getOutcomes()));
        }
        this.given = new ArrayList<>();
        for (List<String> row : other.given) {
            this.given.add(new ArrayList<>(row));
        }
        this.probabilities = new ArrayList<>(other.probabilities);
        this.size = other.size;
    }

    public void deleteRow(int index) {
        for (List<String> row : given) {
            row.remove(index);
        }
        probabilities.remove(index);
    }

    public void deleteColumn(int index) {
        given.remove(index);
        parents.remove(index);
    }

    public void deleteColumn(String key) {
        int index = parents.indexOf(key);
        if (index != -1) {
            deleteColumn(index);
        }
    }

    public List<List<String>> getGiven() {
        return new ArrayList<>(given);
    }

    public List<String> getParentNames() {
        List<String> names = new ArrayList<>();
        for (Variable parent : parents) {
            names.add(parent.getName());
        }
        return names;
    }

    public List<Double> getProbabilities() {
        return new ArrayList<>(probabilities);
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public int compareTo(CPT o) {
        if (this.size != o.size) {
            return this.size - o.size;
        }
        int thisSum = 0, otherSum = 0;
        for (String parentName : getParentNames()) {
            thisSum += parentName.chars().sum();
        }
        for (String parentName : o.getParentNames()) {
            otherSum += parentName.chars().sum();
        }
        return thisSum - otherSum;
    }

    public List<String> getVariables() {
        List<String> vars = new ArrayList<>();
        for (Variable parent : parents) {
            vars.add(parent.getName());
        }
        vars.add(variable.getName()); // Add the target variable last
        return vars;
    }

    public Map<List<String>, Double> computeProbabilityTable() {
        Map<List<String>, Double> table = new HashMap<>();
        for (int i = 0; i < given.size(); i++) {
            List<String> key = new ArrayList<>(given.get(i));
            table.put(key, probabilities.get(i));
        }
        return table;
    }

    public void printCPT() {
        System.out.println("CPT for Variable: " + variable.getName());
        System.out.println("Parents: " + parents.stream().map(Variable::getName).collect(Collectors.toList()));
        System.out.println("Given and Probabilities:");
        for (int i = 0; i < given.size(); i++) {
            System.out.println("Given: " + given.get(i) + ", Probability: " + probabilities.get(i));
        }
    }
}