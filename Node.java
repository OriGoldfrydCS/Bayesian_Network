import java.util.*;

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
                this.cpt.addParent(new Variable(parentName, new ArrayList<>()));
            }
        }
    }

    public void buildCPT(String[] table) {
        int index = 0;
        List<List<String>> keys = new ArrayList<>();
        for (String state : this.possibleStates) {
            List<String> key = new ArrayList<>();
            key.add(state);
            for (Node parent : this.parents) {
                key.add(parent.getPossibleStates().get(index % parent.getPossibleStates().size()));
                index /= parent.getPossibleStates().size();
            }
            Collections.reverse(key);
            keys.add(key);
            this.cpt.setProbability(key, Double.parseDouble(table[index++]));
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

    public void setPossibleStates(ArrayList<String> outcomes) {
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
        Map<List<String>, Double> probTable = this.cpt.getProbabilityTable();
        for (List<String> key : probTable.keySet()) {
            sb.append("| ");
            for (String value : key) {
                sb.append(value).append(" | ");
            }
            sb.append(probTable.get(key)).append(" |\n");
        }
        return sb.toString();
    }
}