import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BayesBall {

    // Colors for different types of nodes
    public static final String UNVISITED_COLOR = "white";
    public static final String EVIDENCE_COLOR = "red";
    public static final String QUERY_COLOR = "blue";
    public static final String VISITED_COLOR = "green";
    public static final String TARGET_COLOR = "black";


    public static String checkIndependence(BayesianNetwork network, Node queryNode, Node targetNode, ArrayList<Node> evidences) {

        // Reset all node colors to 'white' before starting a new query
        for (Node node : network.getNodes()) {
            node.setColor(UNVISITED_COLOR);
            node.setIsColored(false);
        }

        // Initial check for direct child or parent relationship
        if (queryNode.getChildren().contains(targetNode) || queryNode.getParents().contains(targetNode)) {
//            System.out.printf("Direct dependency found between %s and %s%n", queryNode.getNodeName(), targetNode.getNodeName());
            return "no";  // Direct dependency exists
        }

        // Initial check for equality
        if (queryNode.getNodeName().equals(targetNode.getNodeName())) {
            return "no";  // Same node, dependent
        }

        // Color the evidence nodes
        for (Node evidence : evidences) {
            evidence.setColor(EVIDENCE_COLOR);  // Mark as evidence and color it
        }

        // Start traversal from the query node
        Set<Node> visited = new HashSet<>();
        queryNode.setColor(QUERY_COLOR);  // Color the query node
        traverse(queryNode, null, visited, false, false, network,
                queryNode, targetNode);

        // Also start traversal from each parent of the query node
//        for (Node parent : queryNode.getParents()) {
//            if (!visited.contains(parent)) {
//                traverse(parent, null, visited, true, false, network, queryNode, targetNode);
//            }
//        }

        // Check if target node was visited
        return targetNode.isColored() ? "no" : "yes";  // "no" means not independent, "yes" means independent
    }

    static void traverse(Node currentNode, Node comingFrom, Set<Node> visited,
                         boolean reachedFromChild, boolean reachedFromParent, BayesianNetwork network,
                         Node queryNode, Node targetNode) {

        if (currentNode == null) {
//            System.out.println("Warning: currentNode is null, skipping this step of the traversal.");
            return;
        }

        visited.add(currentNode);

        // Apply color if not a special node
        if (currentNode.getColor().equals(UNVISITED_COLOR)) {
            currentNode.setColor(VISITED_COLOR);
//            System.out.printf("Node colored [%s]: %s%n", VISITED_COLOR, currentNode.getNodeName());
        }

        // Handle evidence node specifics
        if (currentNode.getColor().equals(EVIDENCE_COLOR)) {
            if (reachedFromChild) {
//                System.out.printf("Stopping traversal at evidence node: %s because reached from child%n", currentNode.getNodeName());
                return; // Stop if reached from child
            }
            // Only traverse to other parents if reached from a parent
            if (reachedFromParent) {
                for (Node parent : currentNode.getParents()) {
                    traverse(parent, currentNode, visited, true, false, network, queryNode, targetNode);
                }
            }

        } else {
            // Non-evidence nodes
            if (reachedFromParent || comingFrom == null) {
                // Reached from parent: continue to children
                for (Node child : currentNode.getChildren()) {
                    if (!visited.contains(child)) {

                        if (child.equals(queryNode)) {
                            child.setColor(TARGET_COLOR);
                            return;
                        }
                        traverse(child, currentNode, visited, false, true, network, queryNode, targetNode);
                    }
                }
            } else if (reachedFromChild) {
                // Reached from child: continue to both children and parents
                for (Node child : currentNode.getChildren()) {
                    if (!visited.contains(child) && child != comingFrom) {
                        if (child.equals(queryNode)) {
                            child.setColor(TARGET_COLOR);
                            return;
                        }
                        traverse(child, currentNode, visited, false, true, network, queryNode, targetNode);
                    }
                }
                for (Node parent : currentNode.getParents()) {
                    if (!visited.contains(parent) && parent != comingFrom) {
                        if (parent.equals(targetNode)) {
                            parent.setColor(TARGET_COLOR);
                            return;
                        }
                        traverse(parent, currentNode, visited, true, false, network, queryNode, targetNode);
                    }
                }
            }
        }
    }
}