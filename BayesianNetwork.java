import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BayesianNetwork {
    private Map<String, Node> nodes;

    public BayesianNetwork() {
        this.nodes = new HashMap<>();
    }

    public void addNode(Node node) {
        nodes.put(node.getNodeName(), node);
    }

    public Node getNodeByName(String name) {
        return nodes.get(name);
    }

    public void setParents(Node node, List<String> parentNames) {
        for (String parentName : parentNames) {
            Node parent = getNodeByName(parentName);
            if (parent != null) {
                node.addParent(parent);
                parent.addChild(node);
            }
        }
    }


    public Collection<Node> getNodes() {
        return nodes.values();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BayesianNetwork:\n");
        for (Node node : nodes.values()) {
            sb.append(node.toString()).append("\n");
        }
        return sb.toString();
    }


    public void printNetwork() {
        System.out.println("\nASCII Representation of Bayesian Network:");
        for (Node node : nodes.values()) {
            System.out.println("Node: " + node.getNodeName());
            if (!node.getChildren().isEmpty()) {
                System.out.print("  |---> Children: ");
                node.getChildren().forEach(child -> System.out.print(child.getNodeName() + " "));
                System.out.println();
            }
            if (!node.getParents().isEmpty()) {
                System.out.print("Parents: ");
                node.getParents().forEach(parent -> System.out.print(parent.getNodeName() + " "));
                System.out.println("|---> ");
            }
        }
    }
}
