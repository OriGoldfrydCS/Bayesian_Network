import java.util.ArrayList;

public class BayesBall {

    private static ArrayList<Node> visitedNodes = new ArrayList<>();

    public static String checkIndependence(BayesianNetwork network, Node source, Node target, ArrayList<Node> evidence) {
        visitedNodes.clear(); // Reset the list of visited nodes for each new check.
        System.out.println("Starting new independence check between " + source.getNodeName() + " and " + target.getNodeName());
        if (runCheck(network, source, target, evidence, null)) {
            return "yes"; // Independent
        } else {
            return "no"; // Dependent
        }
    }

    private static boolean runCheck(BayesianNetwork network, Node source, Node target, ArrayList<Node> evidence, Node lastVisited) {
        System.out.println("Visiting Node: " + source.getNodeName() + " from Node: " + (lastVisited == null ? "Start" : lastVisited.getNodeName()) + ". Evidence status: " + evidence.contains(source));

        if (source.equals(target)) {
            System.out.println("Reached target node from source node, nodes are dependent.");
            return false;
        }

//        if (visitedNodes.contains(source)) {
//            System.out.println("Node " + source.getNodeName() + " has been already visited. Assuming independence for this path.");
//            return true;  // Avoid cycles.
//        }

        visitedNodes.add(source);
        System.out.println("Adding Node " + source.getNodeName() + " to visited list.");

        // Case 1: The current node is in evidence
        if (evidence.contains(source)) {
            // Case 1a: The current node is in evidence and was reached from a parent
            if (lastVisited != null && source.getParents().contains(lastVisited)) {
                System.out.println("Node " + source.getNodeName() + " is in evidence and was reached from a parent, going to parent(s)");
                for (Node parent : source.getParents()) {
                    if (visitedNodes.contains(parent)) {
                        if (!runCheck(network, parent, target, evidence, source)) {
                            return false;
                        }
                    }
                }
                // After visiting the parents, visit the children
                for (Node child : source.getChildren()) {
                    if (!visitedNodes.contains(child)) {
                        if (!runCheck(network, child, target, evidence, source)) {
                            return false;
                        }
                    }
                }
                return true;
            }
            // Case 1b: The current node is in evidence and was reached from a child
            else if (lastVisited != null && source.getChildren().contains(lastVisited)) {
                System.out.println("Node " + source.getNodeName() + " is in evidence and was reached from a child, going to parent(s)");
                for (Node parent : source.getParents()) {
                    if (!visitedNodes.contains(parent)) {
                        if (!runCheck(network, parent, target, evidence, source)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        // Case 2: The current node is not in evidence
        else {
            // Case 2a: The current node is not in evidence and came from a child or the start
            if (lastVisited == null || source.getChildren().contains(lastVisited)) {
                for (Node parent : source.getParents()) {
                    if (!visitedNodes.contains(parent)) {
                        if (!runCheck(network, parent, target, evidence, source)) {
                            return false;
                        }
                    }
                }
                for (Node child : source.getChildren()) {
                    if (!visitedNodes.contains(child)) {
                        if (!runCheck(network, child, target, evidence, source)) {
                            return false;
                        }
                    }
                }
                return true;
            }
            // Case 2b: The current node is not in evidence and came from a parent
            else {
                for (Node child : source.getChildren()) {
                    if (!visitedNodes.contains(child)) {
                        if (!runCheck(network, child, target, evidence, source)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }

        return true;
    }
}