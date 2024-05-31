import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BayesBall {

    // Colors for different types of nodes
    private static final String EVIDENCE_COLOR = "red";
    private static final String QUERY_COLOR = "blue";
    private static final String VISITED_COLOR = "green";
    private static final String TARGET_COLOR = "black";

    public static String checkIndependence(BayesianNetwork network, Node queryNode, Node targetNode, ArrayList<Node> evidences) {

        // Initial check for equality
        if(queryNode.getNodeName().equals(targetNode.getNodeName())) {
            return "no";
        } else {
            // Color the evidence nodes
            for (Node evidence : evidences) {
                evidence.setColor(EVIDENCE_COLOR);  // Mark as evidence and color it
                System.out.printf("Evidence node colored: %s [%s]%n", evidence.getNodeName(), EVIDENCE_COLOR);
            }

            // Start traversal from the query node
            Set<Node> visited = new HashSet<>();
            queryNode.setColor(QUERY_COLOR);  // Color the query node
            System.out.printf("Starting traversal from query node: %s [%s]%n", queryNode.getNodeName(), QUERY_COLOR);
            traverse(queryNode, null, visited, false, true, network, queryNode, targetNode);

            // Check if target node was visited
            System.out.printf("Checking if target node was visited: %s - Visited: %s%n", targetNode.getNodeName(), targetNode.isColored());
            return targetNode.isColored() ? "no" : "yes";  // "no" means not independent, "yes" means independent
        }
    }

    private static void traverse(Node currentNode, Node comingFrom, Set<Node> visited,
                                 boolean reachedFromChild, boolean reachedFromParent, BayesianNetwork network, Node queryNode, Node targetNode) {
        System.out.printf("Visiting node: %s from node: %s%n", currentNode.getNodeName(), comingFrom != null ? comingFrom.getNodeName() : "null");

        // Check for revisits to avoid infinite loops
        if (!visited.add(currentNode)) {
            System.out.printf("Already visited: %s%n", currentNode.getNodeName());
//            return; // Node already visited, avoid cycle
        }

        // Apply color if not a special node
        if (!currentNode.getColor().equals(EVIDENCE_COLOR) && !currentNode.getColor().equals(QUERY_COLOR)) {
            currentNode.setColor(VISITED_COLOR);
            System.out.printf("Node colored [%s]: %s%n", VISITED_COLOR, currentNode.getNodeName());
        }

        // Handle evidence node specifics
        if (currentNode.getColor().equals(EVIDENCE_COLOR)) {
            System.out.printf("Node is evidence: %s%n", currentNode.getNodeName());
            if (reachedFromChild) {
                System.out.printf("Stopping traversal at evidence node: %s because reached from child%n", currentNode.getNodeName());
                return; // Stop if reached from child
            }
            // Only traverse to other parents if reached from a parent
            if (reachedFromParent) {
                for (Node parent : currentNode.getParents()) {
                        System.out.printf("Going to parent: %s from evidence node: %s%n", parent.getNodeName(), currentNode.getNodeName());
                        traverse(parent, currentNode, visited, true, false, network, queryNode, targetNode);
                }
            }
        } else {
            // Non-evidence nodes
            if (reachedFromParent) {
                // Reached from parent: continue to children
                for (Node child : currentNode.getChildren()) {
                    if (child != comingFrom) {
                        System.out.printf("Going to child: %s from node: %s%n", child.getNodeName(), currentNode.getNodeName());
                        if (child.equals(queryNode)) {
                            System.out.println("Target node found, returning 'no' (dependent)");
                            child.setColor(TARGET_COLOR);
                            return;
                        }
                        traverse(child, currentNode, visited, false, true, network, queryNode, targetNode);
                    }
                }
            } else if (reachedFromChild) {
                // Reached from child: continue to both children and parents
                for (Node child : currentNode.getChildren()) {
                    if (child != comingFrom) {
                        System.out.printf("Going to child: %s from node: %s%n", child.getNodeName(), currentNode.getNodeName());
                        if (child.equals(queryNode)) {
                            System.out.println("Target node found, returning 'no' (dependent)");
                            child.setColor(TARGET_COLOR);
                            return;
                        }
                        traverse(child, currentNode, visited, false, true, network, queryNode, targetNode);
                    }
                }
                for (Node parent : currentNode.getParents()) {
                    if (parent != comingFrom) {
                        System.out.printf("Going to parent: %s from node: %s%n", parent.getNodeName(), currentNode.getNodeName());
                        if (parent.equals(targetNode)) {
                            System.out.println("Target node found, returning 'no' (dependent)");
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