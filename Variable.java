import java.util.*;
import java.util.stream.Collectors;

public class Variable {
    private final String name;
    private final List<String> outcomes;
    private final List<Variable> parents = new ArrayList<>();
    private final List<Variable> children = new ArrayList<>();

    public Variable(String name, List<String> outcomes) {
        this.name = name;
        this.outcomes = new ArrayList<>(outcomes);
    }

    public String getName() {
        return name;
    }

    public List<String> getOutcomes() {
        return new ArrayList<>(outcomes);
    }

    public void addParent(Variable parent) {
        if (!parents.contains(parent)) {
            parents.add(parent);
        }
    }

    public void addChild(Variable child) {
        if (!children.contains(child)) {
            children.add(child);
        }
    }

    public boolean hasMultipleParents() {
        return parents.size() > 1;
    }

    public List<String> getParentNames() {
        return parents.stream()
                .map(Variable::getName)
                .collect(Collectors.toList());
    }

    public boolean isAncestorInEvidence(Set<String> evidence) {
        if (evidence.contains(this.name)) {
            return true;
        }

        for (Variable parent : parents) {
            if (parent.isAncestorInEvidence(evidence)) {
                return true;
            }
        }
        return false;
    }

    public List<Variable> getParents() {
        return new ArrayList<>(parents);
    }

    public List<Variable> getChildren() {
        return new ArrayList<>(children);
    }
}