import java.util.*;

public class BayesianNetwork {
    private Map<String, Variable> variables;
    private Map<String, CPT> cpts;

    public BayesianNetwork() {
        this.variables = new HashMap<>();
        this.cpts = new HashMap<>();
    }

    public void addVariable(Variable variable) {
        this.variables.put(variable.getName(), variable);
    }

    public Variable getVariable(String name) {
        return this.variables.get(name);
    }

    public Collection<Variable> getVariables() {
        return this.variables.values();
    }

    public void addCPT(String variableName, CPT cpt) {
        this.cpts.put(variableName, cpt);
    }

    public CPT getCPT(String variableName) {
        return this.cpts.get(variableName);
    }

    public Map<String, CPT> getCPTs() {
        return this.cpts;
    }

    public void setParents(Variable variable, Variable... parents) {
        CPT cpt = this.getCPT(variable.getName());
        for (Variable parent : parents) {
            cpt.addParent(parent);
            parent.addChild(variable);
        }
    }

    private boolean delete_nodes(Map<String, String> evidence) {
        boolean deleted = false;
        for (Variable variable : this.getVariables()) {
            String name = variable.getName();
            if (!evidence.containsKey(name) && hasNoParents(name)) {
                this.variables.remove(name);
                this.cpts.remove(name);
                deleted = true;
            }
        }
        return deleted;
    }

    private boolean hasNoParents(String name) {
        CPT cpt = this.getCPT(name);
        return cpt != null && cpt.getParents().isEmpty();
    }

    public Node getNodeByName(String parent) {
        //Complete
    }
}