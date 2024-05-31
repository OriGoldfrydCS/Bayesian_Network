import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Factor {
    private List<Node> nodes;
    private Map<List<String>, Double> probabilityTable;

    public Factor(List<Node> newNodes) {
        this.nodes = newNodes;
        this.probabilityTable = new HashMap<>();
    }

    public Factor(List<Node> newNodes, Map<List<String>, Double> probTable) {
        this.nodes = newNodes;
        this.probabilityTable = probTable;
    }

    public Factor(Factor other) {
        this.nodes = new ArrayList<>(other.nodes);
        this.probabilityTable = new HashMap<>(other.probabilityTable);
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

        Map<List<String>, Double> newProbTable = new HashMap<>();

        for (Map.Entry<List<String>, Double> entry1 : this.probabilityTable.entrySet()) {
            for (Map.Entry<List<String>, Double> entry2 : other.probabilityTable.entrySet()) {
                if (compatible(entry1.getKey(), entry2.getKey(), other)) {
                    List<String> newAssignment = new ArrayList<>();
                    for (Node node : newNodes) {
                        int index1 = nodes.indexOf(node);
                        int index2 = other.nodes.indexOf(node);
                        if (index1 >= 0) {
                            newAssignment.add(entry1.getKey().get(index1));
                        } else {
                            newAssignment.add(entry2.getKey().get(index2));
                        }
                    }
                    double newProb = entry1.getValue() * entry2.getValue();
                    newProbTable.put(newAssignment, newProb);
                }
            }
        }

        return new Factor(newNodes, newProbTable);
    }

    public Factor sumOut(Node node) {
        int nodeIndex = nodes.indexOf(node);
        if (nodeIndex == -1) {
            return this;
        }

        Map<List<String>, Double> newProbTable = new HashMap<>();
        List<Node> newNodes = new ArrayList<>(nodes);
        newNodes.remove(nodeIndex);

        for (Map.Entry<List<String>, Double> entry : probabilityTable.entrySet()) {
            List<String> newAssignment = new ArrayList<>(entry.getKey());
            newAssignment.remove(nodeIndex);
            newProbTable.merge(newAssignment, entry.getValue(), Double::sum);
        }

        return new Factor(newNodes, newProbTable);
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

    public double getProbability(List<String> key) {
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