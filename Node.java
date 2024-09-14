import java.util.*;


/**
 * Represents a node in a Bayesian Network.
 * A node can have parents and children, and it holds a conditional probability table (CPT).
 */
public class Node implements Cloneable {
    private String nodeName;                           // Name of the node
    private List<Node> children;                       // List of child nodes
    private List<Node> parents;                        // List of parent nodes
    private List<String> possibleStates;               // List of possible states for the node
    private BayesianNetwork network;                   // The Bayesian Network this node belongs to
    private Factor factor;                             // The factor associated with this node
    private CPT cpt;                                   // The conditional probability table (CPT) for this node
    private boolean isColored;                         // Indicates if the node is colored (visited)
    private boolean isVisitedFromParent;               // Indicates if the node is visited from a parent node
    private boolean isVisitedFromChild;                // Indicates if the node is visited from a child node
    private String color;                              // Color of the node (used in algorithms)
                                                       // NOTE: the last 4 variables are used in BayesBall algo
    /**
     * Constructs a Node with the specified name.
     * @param nodeName The name of the node.
     */
    public Node(String nodeName) {
        this.nodeName = nodeName;
        this.children = new ArrayList<>();
        this.parents = new ArrayList<>();
        this.possibleStates = new ArrayList<>();
        this.cpt = new CPT(this);
        this.isColored = false;
        this.isVisitedFromParent = false;
        this.isVisitedFromChild = false;
        this.color = "white";                   // Default color
    }

    /**
     * Copy constructor for creating a Node from another Node.
     * @param other The other Node to copy.
     */
    public Node(Node other) {
        this.nodeName = other.getNodeName();
        this.parents = new ArrayList<>(other.getParents());
        this.children = new ArrayList<>(other.getChildren());
        this.possibleStates = new ArrayList<>(other.getPossibleStates());
        this.cpt = new CPT(other.getCPT());
    }

    /**
     * Builds the Conditional Probability Table (CPT) for the node.
     * @param table Array of probabilities as strings.
     */
    public void buildCPT(String[] table) {
        int numParentStates = 1;

        // Calculate the number of possible states for the parent nodes
        for (Node parent : this.parents) {
            numParentStates *= parent.getPossibleStates().size();
        }

        // Calculate the total number of states for this node and its parents
        int totalStates = numParentStates * this.possibleStates.size();

        // Initialize the keys for the CPT
        List<String> curr_keys = new ArrayList<>(Collections.nCopies(totalStates, ""));
        if (!this.parents.isEmpty()) {
            int size_of_steps = curr_keys.size();
            for (Node parent : this.parents) {
                size_of_steps = size_of_steps / parent.getPossibleStates().size();
                int k = 0, i = 0, counter = 0;

                // Generate the keys based on parent states
                while (k < curr_keys.size()) {
                    if (counter < size_of_steps) {
                        curr_keys.set(k, curr_keys.get(k) + parent.getPossibleStates().get(i) + ",");
                        counter++;
                        k++;
                    } else {
                        i++;
                        i = i % parent.getPossibleStates().size();
                        counter = 0;
                    }
                }
            }
        }

        int stateIndex = 0;

        // Append this node's states to the keys
        for (int i = 0; i < curr_keys.size(); i++) {
            curr_keys.set(i, curr_keys.get(i) + this.possibleStates.get(stateIndex) + ",");
            stateIndex = (stateIndex + 1) % this.possibleStates.size();
        }

        // Fill the CPT with probabilities
        for (int i = 0; i < curr_keys.size(); i++) {
            String key = curr_keys.get(i);
            if (key.endsWith(",")) {
                key = key.substring(0, key.length() - 1);   // Remove the last comma
            }
            List<String> keyComponents = Arrays.asList(key.split(","));
            this.cpt.setProbability(keyComponents, Double.parseDouble(table[i]));
        }
    }

    /**
     * Creates a factor for the node using its CPT.
     * @return A Factor representing the node's CPT.
     */
    public Factor createFactor() {
        Map<String, Double> probabilityTable = new HashMap<>();
        List<String> dependencies = new ArrayList<>();

        // Node itself is also a part of the dependencies
        dependencies.add(this.nodeName);
        for (Node parent : this.parents) {
            dependencies.add(parent.getNodeName());
        }

        // Collect keys in the correct order according to the CPT
        for (Map.Entry<List<String>, Double> entry : this.cpt.getProbabilityTable().entrySet()) {
            List<String> keyComponents = entry.getKey();
            StringBuilder sb = new StringBuilder();

            // Process parents first
            for (int i = 0; i < this.parents.size(); i++) {
                if (sb.length() > 0) sb.append(",");
                sb.append(this.parents.get(i).getNodeName()).append("=").append(keyComponents.get(i));
            }

            // Add the target node (this node) with handling to not prepend a comma if there are no parents
            if (sb.length() > 0) sb.append(",");
            sb.append(this.nodeName).append("=").append(keyComponents.get(keyComponents.size() - 1));

            probabilityTable.put(sb.toString(), entry.getValue());
        }
        return new Factor(probabilityTable, dependencies);
    }

    /**
     * Adds a child node.
     * @param child The child node to add.
     */
    public void addChild(Node child) {
        this.children.add(child);
    }

    /**
     * Adds a parent node.
     * @param parent The parent node to add.
     */
    public void addParent(Node parent) {
        this.parents.add(parent);
    }

    /**
     * Gets the factor associated with the node.
     * @return The factor associated with the node.
     */
    public Factor getFactor() {
        return this.factor;
    }

    /**
     * Gets the CPT of the node.
     * @return The CPT of the node.
     */
    public CPT getCPT() {
        return this.cpt;
    }

    /**
     * Gets the possible states of the node.
     * @return A list of possible states.
     */
    public List<String> getPossibleStates() {
        return this.possibleStates;
    }

    /**
     * Gets the children of the node.
     * @return A list of child nodes.
     */
    public List<Node> getChildren() {
        return this.children;
    }

    /**
     * Gets the parents of the node.
     * @return A list of parent nodes.
     */
    public List<Node> getParents() {
        return this.parents;
    }

    /**
     * Gets the name of the node.
     * @return The name of the node.
     */
    public String getNodeName() {
        return this.nodeName;
    }

    /**
     * Gets the possible states of the node.
     * @return A list of possible states.
     */
    public void addPossibleStates(ArrayList<String> outcomes) {
        this.possibleStates = new ArrayList<>(outcomes);
    }

    /**
     * Sets the factor associated with the node.
     * @param factor The factor to set.
     */
    public void setFactor(Factor factor) {
        this.factor = factor;
    }

    /**
     * Sets the color of the node.
     * @param color The color to set.
     */
    public void setColor(String color) {
        this.color = color;
        this.isColored = true;
    }

    /**
     * Gets the color of the node.
     * @return The color of the node.
     */
    public String getColor() {
        return color;
    }

    /**
     * Checks if the node is colored (visited).
     * @return True if colored, false otherwise.
     */
    public boolean isColored() {
        return isColored;
    }

    /**
     * Sets the colored state of the node.
     * @param bool The colored state to set.
     */
    public void setIsColored(boolean bool){
        this.isColored = bool;
    }

    /**
     * Returns a string representation of the node.
     * @return The name of the node.
     */
    @Override
    public String toString() {
        return this.nodeName;
    }

    /**
     * Creates a deep clone of the node.
     * @return A deep clone of the node.
     */
    public Node clone() {
        return deepClone(new HashMap<>());
    }

    /**
     * Helper method for creating a deep clone of the node.
     * @param clonedNodes Map of already cloned nodes to avoid duplication.
     * @return A deep clone of the node.
     */
    Node deepClone(Map<Node, Node> clonedNodes) {

        // Check if this node has already been cloned
        if (clonedNodes.containsKey(this)) {
            return clonedNodes.get(this);
        }

        try {
            Node clone = (Node) super.clone();      // Create a shallow copy of this node
            clonedNodes.put(this, clone);           // Store the clone in the map to avoid duplication

            // Deep clone the children
            clone.children = new ArrayList<>();
            for (Node child : this.children) {
                Node clonedChild = child.deepClone(clonedNodes);
                clone.children.add(clonedChild);
            }

            // Deep clone the parents
            clone.parents = new ArrayList<>();
            for (Node parent : this.parents) {
                Node clonedParent = parent.deepClone(clonedNodes);
                clone.parents.add(clonedParent);
            }

            // Deep clone the possible states, CPT and factors
            clone.possibleStates = new ArrayList<>(this.possibleStates);
            clone.cpt = this.cpt.clone();  // Ensure CPT is also cloneable
            clone.factor = this.factor != null ? this.factor.clone() : null;

            return clone;
        } catch (CloneNotSupportedException e) {
            System.out.println("Node clone error");
            throw new AssertionError();
        }
    }
}