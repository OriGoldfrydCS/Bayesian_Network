import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * BayesianNetwork class represents a network structure of nodes with parent-child relationships.
 */
public class BayesianNetwork implements Cloneable {
    private Map<String, Node> nodes;

    /**
     * Constructs an empty Bayesian Network.
     */
    public BayesianNetwork() {
        this.nodes = new HashMap<>();
    }

    /**
     * Adds a node to the network.
     * @param node The node to add.
     */
    public void addNode(Node node) {
        nodes.put(node.getNodeName(), node);
    }

    /**
     * Retrieves a node by its name.
     * @param name The name of the node.
     * @return     The node with the specified name, or null if not found.
     */
    public Node getNodeByName(String name) {
        return nodes.get(name);
    }

    /**
     * Retrieves all nodes in the network.
     * @return A collection of all nodes in the network.
     */
    public Collection<Node> getNodes() {
        return nodes.values();
    }

    /**
     * Sets the parent nodes for a given node by their names.
     * @param node        The node to set parents for.
     * @param parentNames The list of parent node names.
     */
    public void setParents(Node node, List<String> parentNames) {
        for (String parentName : parentNames) {
            Node parent = getNodeByName(parentName);
            if (parent != null) {
                node.addParent(parent);
                parent.addChild(node);
            }
        }
    }

    /**
     * Returns a string representation of the Bayesian Network, listing all nodes and their relationships.
     * @return A string representation of the network.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BayesianNetwork:\n");
        for (Node node : nodes.values()) {
            sb.append(node.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Creates a deep clone of the Bayesian Network.
     * @return A deep clone of the current Bayesian Network.
     */
    @Override
    public BayesianNetwork clone() {
        return deepClone(new HashMap<>());
    }

    /**
     * Helper method to perform a deep clone of the Bayesian Network.
     * @param clonedNodes A map to keep track of already cloned nodes to avoid duplication.
     * @return            A deep clone of the current Bayesian Network.
     */
    private BayesianNetwork deepClone(Map<Node, Node> clonedNodes) {
        try {
            BayesianNetwork clone = (BayesianNetwork) super.clone();
            clone.nodes = new HashMap<>();
            for (Map.Entry<String, Node> entry : this.nodes.entrySet()) {
                Node clonedNode = entry.getValue().deepClone(clonedNodes);
                clone.nodes.put(entry.getKey(), clonedNode);
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            System.out.println("BayesianNetwork clone error");
            throw new AssertionError();
        }
    }
}
