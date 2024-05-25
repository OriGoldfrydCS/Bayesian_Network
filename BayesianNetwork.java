import java.util.*;

public class BayesianNetwork {
    private Map<String, Variable> variables;
    private Map<String, Node> nodes;

    public BayesianNetwork() {
        this.variables = new HashMap<>();
        this.nodes = new HashMap<>();
    }

    public void addVariable(Variable variable) {
        this.variables.put(variable.getVariableName(), variable);
    }

    public Variable getVariable(String name) {
        return this.variables.get(name);
    }

    public Collection<Variable> getVariables() {
        return this.variables.values();
    }

    public void addNode(Node node) {
        this.nodes.put(node.getNodeName(), node);
    }

    public Node getNodeByName(String name) {
        return this.nodes.get(name);
    }

    public Collection<Node> getNodes() {
        return this.nodes.values();
    }

    public void setParents(Node node, List<String> parents) {
        for (String parentName : parents) {
            Node parentNode = getNodeByName(parentName);
            if (parentNode != null) {
                node.addParent(parentNode);
                parentNode.addChild(node);
            } else {
                node.addParent(new Node(parentName, new Variable(parentName, new ArrayList<>())));
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