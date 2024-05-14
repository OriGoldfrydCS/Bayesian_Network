import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BayesianNetwork {
    private final Map<String, Variable> variables = new HashMap<>();
    private final Map<String, CPT> cpts = new HashMap<>();
    private BayesBall bayesBall;
    private VariableElimination variableElimination;

    public BayesianNetwork() {
        bayesBall = new BayesBall(variables, cpts);
        variableElimination = new VariableElimination(variables, cpts);
    }

    public BayesianNetwork(BayesianNetwork other) {
        for (Map.Entry<String, Variable> entry : other.variables.entrySet()) {
            variables.put(entry.getKey(), new Variable(entry.getValue().getName(), entry.getValue().getOutcomes()));
        }
        for (Map.Entry<String, CPT> entry : other.cpts.entrySet()) {
            List<Variable> parents = new ArrayList<>();
            for (String parentName : entry.getValue().getParentNames()) {
                parents.add(variables.get(parentName));
            }
            cpts.put(entry.getKey(), new CPT(variables.get(entry.getKey()), parents, entry.getValue().getProbabilities().stream()
                    .map(String::valueOf)
                    .toArray(String[]::new)));
        }
        bayesBall = new BayesBall(variables, cpts);
        variableElimination = new VariableElimination(variables, cpts);
    }

    public void addVariable(Variable variable) {
        variables.put(variable.getName(), variable);
    }

    public void addCPT(String variableName, List<String> parentNames, String[] probabilities) {
        Variable child = variables.get(variableName);
        if (child == null) {
            throw new IllegalStateException("Child variable '" + variableName + "' not found.");
        }

        List<Variable> parentVariables = new ArrayList<>();
        for (String parentName : parentNames) {
            Variable parent = variables.get(parentName);
            if (parent == null) {
                throw new IllegalStateException("Parent variable '" + parentName + "' not found.");
            }
            parent.addChild(child);
            child.addParent(parent);
            parentVariables.add(parent);
        }

        CPT cpt = new CPT(child, parentVariables, probabilities);
        cpts.put(variableName, cpt);
    }

    public Variable getVariable(String name) {
        return variables.get(name);
    }

    public BayesBall getBayesBall() {
        return bayesBall;
    }

    public VariableElimination getVariableElimination() {
        return variableElimination;
    }
}