import java.util.*;

public class BayesianNetwork {
    private List<Variable> variables;
    private List<Node> nodes;

    public BayesianNetwork() {
        this.variables = new ArrayList<>();
        this.nodes = new ArrayList<>();
    }

    public void addVariable(Variable variable) {
        this.variables.add(variable);
    }

    public Variable getVariable(String name) {
        for (Variable variable : this.variables) {
            if (variable.getVariableName().equals(name)) {
                return variable;
            }
        }
        return null;
    }

    public List<Variable> getVariables() {
        return this.variables;
    }

    public void addNode(Node node) {
        this.nodes.add(node);
    }

    public Node getNodeByName(String name) {
        for (Node node : this.nodes) {
            if (node.getNodeName().equals(name)) {
                return node;
            }
        }
        return null;
    }

    public List<Node> getNodes() {
        return this.nodes;
    }

    public void setParents(Node node, List<String> parentNames) {
        for (String parentName : parentNames) {
            Node parentNode = getNodeByName(parentName);
            if (parentNode != null) {
                node.addParent(parentNode);
                parentNode.addChild(node);
            } else {
                node.addParent(new Node(parentName));
            }
        }
    }

    private boolean deleteNodes(Map<String, String> evidence) {
        boolean deleted = false;
        for (Node node : this.getNodes()) {
            String name = node.getNodeName();
            if (!evidence.containsKey(name) && !node.hasParents()) {
                this.nodes.remove(name);
                deleted = true;
            }
        }
        return deleted;
    }

    private boolean hasNoParents(String name) {
        Node node = this.getNodeByName(name);
        return node != null && node.getParents().isEmpty();
    }
}