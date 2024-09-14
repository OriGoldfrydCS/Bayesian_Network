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


    /**
     * Checks if two nodes (queryNode and targetNode) in a Bayesian Network are conditionally independent given a set of evidence nodes.
     *
     * @param network    The Bayesian Network containing the nodes.
     * @param queryNode  The node for which independence is being queried.
     * @param targetNode The node which is being checked for independence from the queryNode.
     * @param evidences  A list of nodes that are considered as evidence.
     * @return           "yes" if queryNode and targetNode are independent given the evidences, otherwise "no".
     */
    public static String checkIndependence(BayesianNetwork network, Node queryNode, Node targetNode, ArrayList<Node> evidences) {

        // Reset all node colors to 'white' before starting a new query
        for (Node node : network.getNodes()) {
            node.setColor(UNVISITED_COLOR);
            node.setIsColored(false);
        }

        // Initial check for direct child or parent relationship
        if (queryNode.getChildren().contains(targetNode) || queryNode.getParents().contains(targetNode)) {
            return "no";  // Direct dependency exists
        }

        // Initial check for equality
        if (queryNode.getNodeName().equals(targetNode.getNodeName())) {
            return "no";  // Same node, dependent
        }

        // Check if queryNode and targetNode share a common ancestor but the common ancestor is not in the evidence
        Set<Node> queryAncestors = getAllAncestors(queryNode);
        Set<Node> targetAncestors = getAllAncestors(targetNode);

        // Find common ancestors between query and target
        queryAncestors.retainAll(targetAncestors);  // Find common ancestors

        if (!queryAncestors.isEmpty()) {
            // If there are common ancestors, check if any of them are in the evidence
            boolean hasCommonAncestorInEvidence = false;
            for (Node ancestor : queryAncestors) {
                if (evidences != null && evidences.contains(ancestor)) {
                    hasCommonAncestorInEvidence = true;
                    break;
                }
            }

            // If they share a common ancestor that is not an evidence, return independent
            if (!hasCommonAncestorInEvidence) {
                return "no";  // Independent because they share the same ancestor which is not in the evidence
            }
        }

        // Color the evidence nodes
        if (evidences != null) {
            for (Node evidence : evidences) {
                evidence.setColor(EVIDENCE_COLOR);  // Mark as evidence and color it
            }
        }

        // Start traversal from the query node
        Set<Node> visited = new HashSet<>();
        queryNode.setColor(QUERY_COLOR);  // Color the query node
        traverse(queryNode, null, visited, false, false, network, queryNode, targetNode);

        // Check if target node was visited
        boolean isIndependent = !targetNode.isColored();
        System.out.println("Independence result between " + queryNode.getNodeName() + " and " + targetNode.getNodeName() + ": " + (isIndependent ? "yes" : "no"));
        return targetNode.isColored() ? "no" : "yes";  // "no" means not independent, "yes" means independent
    }

    /**
     * Traverses the Bayesian Network to check for conditional independence.
     *
     * @param currentNode     The current node being visited.
     * @param comingFrom      The node from which the current node was reached.
     * @param visited         A set of nodes that have already been visited.
     * @param reachedFromChild Indicates if the current node was reached from a child node.
     * @param reachedFromParent Indicates if the current node was reached from a parent node.
     * @param network         The Bayesian Network containing the nodes.
     * @param queryNode       The node for which independence is being queried.
     * @param targetNode      The node which is being checked for independence from the queryNode.
     */
    static void traverse(Node currentNode, Node comingFrom, Set<Node> visited, boolean reachedFromChild, boolean reachedFromParent, BayesianNetwork network, Node queryNode, Node targetNode) {

        if (currentNode == null) {
            return;
        }

        visited.add(currentNode);

        // Apply color if not a special node
        if (currentNode.getColor().equals(UNVISITED_COLOR)) {
            currentNode.setColor(VISITED_COLOR);
        }

        // Handle evidence node specifics
        if (currentNode.getColor().equals(EVIDENCE_COLOR)) {
            if (reachedFromChild) {
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
                        if (child.equals(targetNode)) {
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
                        if (child.equals(targetNode)) {
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

    /**
     * Helper method to get all ancestors of a node.
     *
     * @param node The node whose ancestors are to be found.
     * @return A set containing all ancestors of the given node.
     */
    private static Set<Node> getAllAncestors(Node node) {
        Set<Node> ancestors = new HashSet<>();
        getAncestorsRecursive(node, ancestors);
        return ancestors;
    }

    /**
     * Recursively finds all ancestors of a given node.
     *
     * @param node The current node whose ancestors are being found.
     * @param ancestors A set to store all the found ancestors.
     */
    private static void getAncestorsRecursive(Node node, Set<Node> ancestors) {
        for (Node parent : node.getParents()) {
            if (!ancestors.contains(parent)) {
                ancestors.add(parent);
                getAncestorsRecursive(parent, ancestors);  // Recur to find ancestors of the parent
            }
        }
    }
}
