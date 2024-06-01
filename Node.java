import java.util.*;
import java.util.stream.Collectors;

public class Node {
    private String nodeName;
    private List<Node> children;
    private List<Node> parents;
    private List<String> possibleStates;
    private BayesianNetwork network;
    private Factor factor;
    private CPT cpt;
    private boolean isColored;
    private boolean isVisitedFromParent;
    private boolean isVisitedFromChild;
    private String color;


    public Node(String nodeName) {
        this.nodeName = nodeName;
        this.children = new ArrayList<>();
        this.parents = new ArrayList<>();
        this.possibleStates = new ArrayList<>();
        this.factor = new Factor(new ArrayList<>(), new HashMap<>());
        this.cpt = new CPT(this);
        this.isColored = false;
        this.isVisitedFromParent = false;
        this.isVisitedFromChild = false;
        this.color = "white";  // Default color

    }

    public Node(Node other) {
        this.nodeName = other.getNodeName();
        this.parents = new ArrayList<>(other.getParents());
        this.children = new ArrayList<>(other.getChildren());
        this.possibleStates = new ArrayList<>(other.getPossibleStates());
        this.cpt = new CPT(other.getCPT());
        this.factor = new Factor(other.getFactor());
    }

    public Node(String nodeName, List<String> parentNames, BayesianNetwork network) {
        this.nodeName = nodeName;
        this.network = network;
        for (String parentName : parentNames) {
            Node parentNode = network.getNodeByName(parentName);
            if (parentNode != null) {
                this.parents.add(parentNode);
                parentNode.addChild(this);
            } else {
                this.parents.add(new Node(parentName));
                this.cpt.addParent(new Node(parentName));
            }
        }
    }

    public void buildCPT(String[] table) {
        List<List<String>> keys = new ArrayList<>();
        int numParentStates = 1;
        for (Node parent : this.parents) {
            numParentStates *= parent.getPossibleStates().size();
        }
        int totalStates = numParentStates * this.possibleStates.size();

        for (int i = 0; i < totalStates; i++) {
            int index = i;
            List<String> key = new ArrayList<>();
            // First add the parent states
            for (Node parent : this.parents) {
                int parentStateCount = parent.getPossibleStates().size();
                key.add(parent.getPossibleStates().get(index % parentStateCount));
                index /= parentStateCount;
            }
            // Then add the node state
            key.add(this.possibleStates.get(i / numParentStates % this.possibleStates.size()));
            Collections.reverse(key);
            this.cpt.setProbability(key, Double.parseDouble(table[i]));
        }
    }


    private HashMap<String, String> createCPTRow(String probability) {
        HashMap<String, String> row = new HashMap<>();
        int totalPossibleStates = possibleStates.size();
        int stateIndex = 0;
        for (String state : possibleStates) {
            row.put(this.nodeName, state);
            int parentIndex = 0;
            for (Node parent : parents) {
                row.put(parent.getNodeName(), parent.getPossibleStates().get(stateIndex % parent.getPossibleStates().size()));
                stateIndex /= parent.getPossibleStates().size();
                parentIndex++;
            }
            row.put("P", probability);
            stateIndex++;
            if (stateIndex >= totalPossibleStates) {
                break;
            }
        }
        return row;
    }

    public void addChild(Node child) {
        this.children.add(child);
    }

    public void removeChild(Node child) {
        this.children.remove(child);
    }

    public void addParent(Node parent) {
        this.parents.add(parent);
    }

    public void removeParent(Node parent) {
        this.parents.remove(parent);
    }

    public boolean hasParents() {
        return !this.parents.isEmpty();
    }

    public boolean hasChildren() {
        return !this.children.isEmpty();
    }

    public boolean isProbabilistic() {
        return !this.cpt.getProbabilityTable().isEmpty();
    }

    public Factor getFactor() {
        return this.factor;
    }

    public List<String> getVariablePossibleStates() {
        return possibleStates;
    }

    public CPT getCPT() {
        return this.cpt;
    }
//
//    public HashMap<String, String> getCPTRow(int index) {
//        return this.cpt.get(index);
//    }

    public List<String> getPossibleStates() {
        return this.possibleStates;
    }

    public List<Node> getChildren() {
        return this.children;
    }

    public List<Node> getParents() {
        return this.parents;
    }

    public String getNodeName() {
        return this.nodeName;
    }

    public int getCPTRowCount() {
        return this.cpt.getProbabilityTable().size();
    }

    public double getFactorValue() {
        return this.factor.getValue();
    }

    public void addPossibleStates(ArrayList<String> outcomes) {
        this.possibleStates = new ArrayList<>(outcomes);
    }

    public void setFactor(Factor factor) {
        this.factor = factor;
    }

    public void setFactorValue(double value) {
        this.factor.setValue(value);
    }

    public void setCPTRow(int index, HashMap<String, String> row) {
        List<String> key = new ArrayList<>();
        for (Node parent : this.parents) {
            key.add(row.get(parent.getNodeName()));
        }
        key.add(row.get(this.nodeName));
        this.cpt.setProbability(key, Double.parseDouble(row.get("P")));
    }

    // Method to add an edge from this node to another (child)
    public void addEdge(Node child) {
        if (!this.children.contains(child)) {
            this.children.add(child);
            if (!child.parents.contains(this)) {
                child.parents.add(this);
            }
        }
    }
    // Setters for visitation states
    public void visitFromParent() {
        this.isVisitedFromParent = true;
    }

    public void visitFromChild() {
        this.isVisitedFromChild = true;
    }

    // Checkers for visitation states
    public boolean isVisitedFromParent() {
        return isVisitedFromParent;
    }

    public boolean isVisitedFromChild() {
        return isVisitedFromChild;
    }

    // Color handling methods
    public void setColor(String color) {
        this.color = color;
        this.isColored = true;
    }

    public String getColor() {
        return color;
    }

    public boolean isColored() {
        return isColored;
    }

    public void setIsColored(boolean bool){
        this.isColored = bool;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Node's name: ").append(nodeName).append("\n");
        sb.append("Children: ");
        for (Node child : children) {
            sb.append(child.getNodeName());
            if (children.indexOf(child) < children.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("\n");
        sb.append("Parents: ");
        for (Node parent : parents) {
            sb.append(parent.getNodeName());
            if (parents.indexOf(parent) < parents.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("\n");
        sb.append("Possible States: ");
        for (String state : possibleStates) {
            sb.append(state);
            if (possibleStates.indexOf(state) < possibleStates.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("\n");
        sb.append("CPT:\n");

        // Formatting header according to specific requirements
        if (!parents.isEmpty()) {
            for (Node parent : parents) {
                sb.append("| ").append(parent.getNodeName()).append(" ");
            }
        }
        sb.append("| ").append(nodeName).append(" | P(").append(nodeName);
        if (!parents.isEmpty()) {
            sb.append(" | ");
            for (Node parent : parents) {
                sb.append(parent.getNodeName());
                if (parents.indexOf(parent) < parents.size() - 1) {
                    sb.append(", ");
                } else {
                    sb.append(") | \n");
                }
            }
        } else {
            sb.append(") | \n");
        }

        // Sorting keys to maintain consistent order from all 'T' to all 'F'
        Map<List<String>, Double> probTable = this.cpt.getProbabilityTable();
        List<List<String>> sortedKeys = new ArrayList<>(probTable.keySet());
        sortedKeys.sort((a, b) -> {
            for (int i = a.size() - 1; i >= 0; i--) { // Reverse for proper T-F order
                int comp = b.get(i).compareTo(a.get(i)); // Reverse comparison for T to F
                if (comp != 0) return comp;
            }
            return 0;
        });

        // Adding rows
        for (List<String> key : sortedKeys) {
            sb.append("| ");
            for (String value : key) {
                sb.append(value).append(" | ");
            }
            sb.append(probTable.get(key)).append(" |\n");
        }
        return sb.toString();
    }

//    public String toString() {
//        return String.format("Node{%s, States=%s, Parents=%s, Children=%s}",
//                nodeName,
//                possibleStates.toString(),
//                parents.stream().map(Node::getNodeName).collect(Collectors.toList()),
//                children.stream().map(Node::getNodeName).collect(Collectors.toList()));
//    }



}