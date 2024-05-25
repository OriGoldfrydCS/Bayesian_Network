import java.util.*;

public class Variable {
    private String name;
    private List<String> outcomes;
    private List<Variable> parents;
    private List<Variable> children;
    private double[] probabilities;

    public Variable(String name, List<String> outcomes) {
        this.name = name;
        this.outcomes = outcomes;
        this.parents = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<String> getOutcomes() {
        return outcomes;
    }

    public void setParents(List<Variable> parents) {
        this.parents = parents;
    }

    public List<Variable> getParents() {
        return parents;
    }

    public List<Variable> getChildren() {
        return children;
    }

    public void addChild(Variable child) {
        if (!children.contains(child)) {
            children.add(child);
        }
    }

    public void setProbabilities(String[] probabilities) {
        this.probabilities = Arrays.stream(probabilities)
                .mapToDouble(Double::parseDouble)
                .toArray();
    }

    public double[] getProbabilities() {
        return probabilities;
    }

    public double getProbability(String outcome) {
        int index = outcomes.indexOf(outcome);
        if (index >= 0 && index < probabilities.length) {
            return probabilities[index];
        }
        return 0.0;
    }

    @Override
    public String toString() {
        return "Variable{" +
                "name='" + name + '\'' +
                ", outcomes=" + outcomes +
                ", parents=" + parents +
                ", children=" + children +
                ", probabilities=" + Arrays.toString(probabilities) +
                '}';
    }
}
