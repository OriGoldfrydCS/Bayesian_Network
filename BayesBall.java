import java.util.*;

public class BayesBall {
    private Map<String, Variable> variables;
    private Map<String, CPT> cpts;

    public BayesBall(Map<String, Variable> variables, Map<String, CPT> cpts) {
        this.variables = variables;
        this.cpts = cpts;
    }

    public boolean query(String start, String end, Set<String> evidence) {
        System.out.println("Querying BayesBall: Start=" + start + ", End=" + end + ", Evidence=" + evidence);

        Set<String> visited = new HashSet<>();
        Queue<Visit> queue = new LinkedList<>();
        queue.add(new Visit(start, null, false));

        while (!queue.isEmpty()) {
            Visit visit = queue.poll();
            String current = visit.node;
            System.out.println("Visiting: " + current);

            String from = visit.cameFrom;
            boolean cameFromParent = visit.cameFromParent;

            if (current.equals(end)) {
                if (!cameFromParent || evidence.contains(current)) {
                    System.out.println("Dependent (current node is evidence or reached via parent)");
                    return false; // Dependent
                }
            }

            if (!visited.contains(current)) {
                visited.add(current);
                Variable currentVar = variables.get(current);
                if (currentVar != null) {
                    if (!cameFromParent || evidence.contains(current)) {
                        for (Variable child : currentVar.getChildren()) {
                            if (!visited.contains(child.getName())) {
                                queue.add(new Visit(child.getName(), current, false));
                            }
                        }
                    }

                    if (!evidence.contains(current) && !currentVar.hasMultipleParents()) {
                        for (Variable parent : currentVar.getParents()) {
                            if (!visited.contains(parent.getName())) {
                                queue.add(new Visit(parent.getName(), current, true));
                            }
                        }
                    }
                }
            }
        }

        return true; // Independent by default if no dependent path found
    }

    private static class Visit {
        String node;
        String cameFrom;
        boolean cameFromParent;

        Visit(String node, String cameFrom, boolean cameFromParent) {
            this.node = node;
            this.cameFrom = cameFrom;
            this.cameFromParent = cameFromParent;
        }
    }
}
