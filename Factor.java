import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Factor {
    private List<Node> nodes;
    private Map<List<String>, Double> probabilityTable;

    public Factor(Node node, Map<String, String> evidence) {
        nodes = new ArrayList<>();
        probabilityTable = new HashMap<>();
        if (!evidence.containsKey(node.getNodeName())) {
            nodes.add(node);
            int stateIndex = 0;
            for (String state : node.getPossibleStates()) {
                List<String> key = new ArrayList<>();
                key.add(state);
                probabilityTable.put(key, getProbability(node, state));
                System.out.println("Adding probability for " + node.getNodeName() + " = " + state + ": " + getProbability(node, state));
                stateIndex++;
            }
        }
    }

    public Factor() {
        nodes = new ArrayList<>();
        probabilityTable = new HashMap<>();
    }

    public Factor(Factor other) {
        nodes = new ArrayList<>(other.nodes);
        probabilityTable = new HashMap<>(other.probabilityTable);
    }

    public Factor(List<Node> newNodes) {
        nodes = newNodes;
        probabilityTable = new HashMap<>();
    }

    public boolean contains(String nodeName) {
        for (Node n : nodes) {
            if (n.getNodeName().equals(nodeName)) {
                return true;
            }
        }
        return false;
    }

    public Factor multiply(Factor other) {
        List<Node> newNodes = new ArrayList<>(nodes);
        for (Node node : other.nodes) {
            if (!newNodes.contains(node)) {
                newNodes.add(node);
            }
        }
        Factor result = new Factor(newNodes);

        for (List<String> assignment1 : probabilityTable.keySet()) {
            for (List<String> assignment2 : other.probabilityTable.keySet()) {
                if (compatible(assignment1, assignment2, other)) {
                    List<String> newAssignment = new ArrayList<>();
                    for (Node node : newNodes) {
                        int index1 = nodes.indexOf(node);
                        int index2 = other.nodes.indexOf(node);
                        if (index1 >= 0) {
                            newAssignment.add(assignment1.get(index1));
                        } else {
                            newAssignment.add(assignment2.get(index2));
                        }
                    }
                    double newProb = probabilityTable.get(assignment1) * other.probabilityTable.get(assignment2);
                    result.probabilityTable.put(newAssignment, newProb);
                    System.out.println("Multiplying factors, new assignment: " + newAssignment + " new probability: " + newProb);
                }
            }
        }
        return result;
    }

    public Factor sumOut(Node node) {
        Factor result = new Factor();
        int nodeIndex = nodes.indexOf(node);

        if (nodeIndex == -1) {
            return this;
        }

        for (List<String> assignment : probabilityTable.keySet()) {
            List<String> newAssignment = new ArrayList<>(assignment);
            newAssignment.remove(nodeIndex);
            double newProb = probabilityTable.get(assignment);
            result.probabilityTable.merge(newAssignment, newProb, Double::sum);
            System.out.println("Summing out " + node.getNodeName() + ", new assignment: " + newAssignment + " new probability: " + newProb);
        }

        result.nodes = new ArrayList<>(nodes);
        result.nodes.remove(nodeIndex);

        return result;
    }

    private boolean compatible(List<String> assignment1, List<String> assignment2, Factor other) {
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            int index = other.nodes.indexOf(node);
            if (index != -1 && !assignment1.get(i).equals(assignment2.get(index))) {
                return false;
            }
        }
        return true;
    }

    private double getProbability(Node node, String outcome) {
        for (HashMap<String, String> row : node.getCPT()) {
            if (row.get(node.getNodeName()).equals(outcome)) {
                return Double.parseDouble(row.get("P"));
            }
        }
        return 0.0;
    }


    public double getProbability(String query) {
        List<String> key = new ArrayList<>(List.of(query.split(",")));
        return probabilityTable.getOrDefault(key, 0.0);
    }

    @Override
    public String toString() {
        return "Factor{" +
                "nodes=" + nodes +
                ", probabilityTable=" + probabilityTable +
                '}';
    }

    public double getValue() {
        // IMPLEMENT
        return 0;
    }

    public void setValue(double value) {
        // IMPLEMENT
    }
}