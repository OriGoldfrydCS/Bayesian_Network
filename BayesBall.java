import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class BayesBall {
    private BayesianNetwork network;

    public BayesBall(BayesianNetwork network) {
        this.network = network;
    }

    public boolean isIndependent(String start, String end, Map<String, String> evidence) {
        System.out.println("Starting BayesBall: Start=" + start + ", End=" + end + ", Evidence=" + evidence);
        Set<String> visited = new HashSet<>();
        Stack<String> stack = new Stack<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            String current = stack.pop();
            System.out.println("Visiting: " + current);
            if (current.equals(end)) {
                System.out.println("Path found: " + start + " to " + end);
                return false; // Found a path, thus dependent
            }
            if (visited.contains(current)) {
                System.out.println("Already visited: " + current);
                continue; // Skip already visited nodes
            }
            visited.add(current);

            Variable var = network.getVariable(current);
            if (var == null) continue; // Safety check

            // Process parents
            for (Variable parent : var.getParents()) {
                if (!evidence.containsKey(parent.getName()) && !visited.contains(parent.getName())) {
                    stack.push(parent.getName());
                    System.out.println("Adding parent to stack: " + parent.getName());
                }
            }
            // Process children
            for (Variable child : var.getChildren()) {
                if (!evidence.containsKey(child.getName()) && !visited.contains(child.getName())) {
                    stack.push(child.getName());
                    System.out.println("Adding child to stack: " + child.getName());
                }
            }
        }
        System.out.println("No path found: " + start + " to " + end);
        return true; // No path found, thus independent
    }
}