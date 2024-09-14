import java.util.*;


/**
 * Class for performing variable elimination on a Bayesian network.
 */
public class VariableElimination {
    private BayesianNetwork network;            // The Bayesian network on which to perform variable elimination
    private List<Factor> initialFactors;        // Stores the initial factors generated from the network
    private List<Factor> factors;               // Factors to be processed during variable elimination
    private List<Node> hiddenNodes;             // Nodes to be hidden during the elimination
    private List<String> hiddenOrder;           // Order in which the hidden nodes should be processed
    private List<Node> evidenceNodes;           // Nodes representing evidence
    private Map<Node, String> nodeEvidenceMap;  // Mapping of evidence nodes to their observed states
    private Node queryNode;                     // The query node for which probability is calculated
    private String queryOutcome;                // The outcome of the query node to calculate the probability for

    /**
     * Constructor initializes the variable elimination process.
     *
     * @param network  Bayesian network
     * @param query    Query specifying the target node and outcome
     * @param hidden   Array of hidden nodes
     * @param evidence Array of evidence in the format Node=Value
     */
    public VariableElimination(BayesianNetwork network, String query, String[] hidden, String[] evidence) {
        this.network = network;
        this.initialFactors = new ArrayList<>();  // Initialize the initial factors list
        this.factors = new ArrayList<>();
        this.hiddenNodes = new ArrayList<>();
        this.hiddenOrder = new ArrayList<>(Arrays.asList(hidden));  // Initialize the order of hidden nodes from input
        this.evidenceNodes = new ArrayList<>();
        this.nodeEvidenceMap = new HashMap<>();

        parseQuery(query);                                  // Parse the query input
        initializeHidAndEviNodes(hidden, evidence);         // Initialize hidden and evidence nodes
        Set<Node> irrelevantNodes = findIrrelevantNodes();  // Identify nodes not relevant to the query
        for(Node irrelevantNode: irrelevantNodes){
            String name  = irrelevantNode.getNodeName();
            hiddenNodes.remove(irrelevantNode);
            hiddenOrder.remove(name);
        }
        generateRelevantFactors(irrelevantNodes);           // Generate factors only for relevant nodes
        saveInitialFactors();                               // Save the initial state of factors for resetting later

        runVariableElimination();                           // Start the variable elimination process

    }

    /**
     * Main method to run the variable elimination process after initializing all factors.
     */
    public void runVariableElimination() {
        Factor.resetCounts();                                               // Resetting multiplication and addition counts before each run
        applyEvidence();                                                    // Apply evidence to the factors
        Set<String> evidenceVariableNames = getEvidenceVariableNames();     // Get the names of the evidence variables
        Collections.sort(factors);                                          // Sort the factors

        // Print each factor for visualization and debugging purposes
        for(Factor factor: this.factors){
            System.out.println(factor);
        }

        // Eliminate each hidden variable as per the order provided
        for (String hiddenVar : hiddenOrder) {
            List<Factor> factorsToJoin = new ArrayList<>();

            // Find all factors that involve the hidden variable
            for (Iterator<Factor> it = factors.iterator(); it.hasNext(); ) {
                Factor factor = it.next();
                if (factor.involvesVariable(hiddenVar)) {
                    factorsToJoin.add(factor);
                    it.remove();  // Prevent reprocessing
                }
            }

            // Join factors that involve the hidden variable
            int prevMult = Factor.getMultiplicationCount();
            int prevAdd = Factor.getAdditionCount();
            Collections.sort(factorsToJoin);        // Sort factors to join for consistent processing
            while (factorsToJoin.size() > 1) {

                // Join the first two factors in the list
                Factor joinedFactor = Factor.joinFactors(factorsToJoin.get(0), factorsToJoin.get(1), evidenceVariableNames);
                prevMult = Factor.getMultiplicationCount();
                factorsToJoin.remove(0);
                factorsToJoin.remove(0);
                factorsToJoin.add(0, joinedFactor);  // Add the newly joined factor at the start of the list
            }

            // Special condition to remove factors if the resulting factor has only one row after elimination
            if(factorsToJoin.size() > 0 && (factorsToJoin.get(0).getTableSize() / (this.network.getNodeByName(hiddenVar).getPossibleStates().size())) == 1){
                Factor toRemove  = factorsToJoin.get(0);
                factors.remove(toRemove);
            }

            // After joining all factors involving the hidden variable, eliminate the variable
            if (!factorsToJoin.isEmpty()) {
                Factor remainingFactor = factorsToJoin.get(0);
                remainingFactor.eliminateFactor(hiddenVar);

                factors.add(remainingFactor);               // Add the modified factor back to the main list

                // Check if the answer can be directly determined from the remaining factor
                if (isDirectAnswer(remainingFactor)) {
                    return;
                }
            }
        }

        // Check if a single factor remains and if it can directly provide the answer
        if(this.factors.size() == 1){
            if (isDirectAnswer(this.factors.get(0))) {
                int j = 0;
                return;
            }
        }

        HashSet<Factor> toRemove = new HashSet<>();

        // Remove any remaining factors that have only one row
        for( Factor remainFactor : this.factors){
            if (remainFactor.getTableSize() == 1){
                toRemove.add(remainFactor);
            }
        }
        for(Factor desiredToRemove: toRemove){
            this.factors.remove(desiredToRemove);
        }

        // If multiple factors remain, join them into a single factor
        while (factors.size() > 1) {
            Factor joinedFactor = Factor.joinFactors(factors.get(0), factors.get(1), evidenceVariableNames);

            factors.remove(0);
            factors.remove(0);
            factors.add(joinedFactor);
        }

        // Normalize the final factor
        normalizeFinalFactors();
    }

    /**
     * Parses the query into node and outcome.
     *
     * @param query The query string in format Node=Outcome
     */
    private void parseQuery(String query) {
        String[] queryParts = query.split("=");

        // Check if the query is in the correct format
        if (queryParts.length == 2) {
            this.queryNode = network.getNodeByName(queryParts[0].trim());
            this.queryOutcome = queryParts[1].trim();
        } else {

        }
    }

    /**
     * Initializes hidden and evidence nodes based on input arrays.
     *
     * @param hidden   Array of hidden nodes
     * @param evidence Array of evidence nodes with their values
     */
    private void initializeHidAndEviNodes(String[] hidden, String[] evidence) {
        System.out.println("Initializing nodes for variable elimination...");

        // Add hidden nodes to the list
        for (String hiddenNodeName : hidden) {
            Node node = network.getNodeByName(hiddenNodeName.trim());
            if (node != null) {
                this.hiddenNodes.add(node);
            } else {
                System.err.println("Warning: Hidden node not found in the network - " + hiddenNodeName);
            }
        }

        // Add evidence nodes to the list and map
        for (String evidenceItem : evidence) {
            String[] parts = evidenceItem.split("=");
            if (parts.length == 2) {
                Node node = network.getNodeByName(parts[0].trim());
                if (node != null) {
                    this.evidenceNodes.add(node);
                    this.nodeEvidenceMap.put(node, parts[1].trim());
                } else {
                    System.err.println("Warning: Evidence node not found in the network - " + parts[0]);
                }
            } else {
                System.err.println("Warning: Incorrect evidence format - " + evidenceItem);
            }
        }
    }

    /**
     * Identifies irrelevant nodes that do not affect the query outcome.
     * @return Set of irrelevant nodes
     */
    private Set<Node> findIrrelevantNodes() {
        ArrayList<String> norellevent = new ArrayList<>();          // List to store names of irrelevant nodes
        Set<Node> irrelevant = new HashSet<>();                     // Set to store irrelevant nodes
        ArrayList<Node> queryAndEvidence = new ArrayList<>();       // List to store query and evidence nodes
        queryAndEvidence.addAll(this.evidenceNodes);
        queryAndEvidence.add(queryNode);

        // Check independence of each hidden node from the query node and evidence
        for (Node hiddenNode : hiddenNodes) {
            String name = hiddenNode.getNodeName();

            // Check if the hidden node independent of the query node given the evidence nodes
            String independence = BayesBall.checkIndependence(network, queryNode, hiddenNode, new ArrayList<>(evidenceNodes));

            // Check the reverse independence as well
            String independence2 = BayesBall.checkIndependence(network, hiddenNode, queryNode, new ArrayList<>(evidenceNodes));

            // If both checks indicate independence, mark the node as irrelevant
            if((independence.equals("yes") && independence2.equals("yes"))){
                irrelevant.add(hiddenNode);

                // Add the children of the irrelevant node to the irrelevant set
                for(Node childrenIrrelevant : hiddenNode.getChildren()){
                    irrelevant.add(childrenIrrelevant);
                    norellevent.add(hiddenNode.getNodeName());
                }
                norellevent.add(hiddenNode.getNodeName());
                continue;
            }

            boolean isRelevant = false;  // Flag to check if the hidden node is relevant due to any evidence node

            // Check if any evidence/query node is an ancestor of the hidden node
            for (Node groupNode : queryAndEvidence) {
                if (isAncestor(hiddenNode, groupNode)) {
                    isRelevant = true;
                    break;  // If any evidence node is an ancestor, the hidden node is relevant
                }
            }

            // If the hidden node is not relevant, mark it as irrelevant
            if (!isRelevant) {
                irrelevant.add(hiddenNode);

                // Add the children of the irrelevant node to the irrelevant set
                for(Node childrenIrrelevant : hiddenNode.getChildren()){
                    irrelevant.add(childrenIrrelevant);
                    norellevent.add(hiddenNode.getNodeName());
                }
                norellevent.add(hiddenNode.getNodeName());
                System.out.println("Marked node " + hiddenNode.getNodeName() + " as irrelevant.");
            }
        }

        Collections.sort(norellevent);      // Sort the list of irrelevant node names
        return irrelevant;                  // Return the set of irrelevant nodes
    }

    /**
     * Generates factors only for nodes that are relevant to the query.
     *
     * @param irrelevantNodes Nodes that have been determined to be irrelevant
     */
    private void generateRelevantFactors(Set<Node> irrelevantNodes) {
        ArrayList<String> factories_vars = new ArrayList<>();       // List to store variable names of the relevant factors

        // Iterate through all nodes in the network and add their factors to the factors list
        for (Node node : network.getNodes()) {
            String name = node.getNodeName();

            // Check if the node is not in the set of irrelevant nodes
            if (!irrelevantNodes.contains(node)) {
                Factor factor = node.getFactor();       // Get the factor of the node

                // If the node has a factor, add it to the factors list and track its variable name
                if (factor != null) {
                    factories_vars.add(node.getNodeName());
                    factors.add(factor);
                }
            }
        }

        // Sort the list of variable names
        Collections.sort(factories_vars);
    }

    /**
     * Saves a copy of the current factors as initial factors.
     */
    private void saveInitialFactors() {
        // Iterate through the factors and create a deep copy of each one
        for (Factor factor : factors) {
            // Deep copy each factor
            initialFactors.add(new Factor(factor.getProbabilityTable(), String.valueOf(factor.getVariables())));
        }
    }

    /**
     * Applies evidence to the factors, means minimize them according to their states.
     */
    private void applyEvidence() {
        Map<String, String> evidenceMap = new HashMap<>();

        // Create a map of evidence variable names and their observed values
        for (Node node : evidenceNodes) {
            evidenceMap.put(node.getNodeName(), nodeEvidenceMap.get(node));
        }

        // Apply the evidence to each factor
        for (Factor factor : factors) {
            boolean involvesEvidence = false;
            // Check if the factor involves any of the evidence variables
            for (String variable : evidenceMap.keySet()) {
                if (factor.involvesVariable(variable)) {
                    involvesEvidence = true;
                    break;
                }
            }
            if (involvesEvidence) {
                factor.filterRows(evidenceMap);
            }
        }
    }

    /**
     * Checks if a node is an ancestor of another node in the network.
     *
     * @param potentialFather  Node to start the search from
     * @param potentialChild Node to find in the descendants
     * @return true if start is an ancestor of target
     */
    private boolean isAncestor(Node potentialFather, Node potentialChild) {
        Set<Node> visited = new HashSet<>();          // Create a HashSet to keep track of visited nodes to avoid revisiting
        LinkedList<Node> queue = new LinkedList<>();  // Use a LinkedList as a queue to manage nodes to be explored in BFS order
        queue.add(potentialFather);                             // Add the starting node to the queue as the initial node

        // Continue processing nodes until there are no more nodes left in the queue
        while (!queue.isEmpty()) {
            Node current = queue.poll();              // Remove the front node from the queue to process it

            // If the current node is the target, return true indicating 'start' is an ancestor of 'target'
            if (current.equals(potentialChild)) {
                return true;
            }

            // Iterate over each child of the current node to explore its descendants
            for (Node child : current.getChildren()) {
                if (!visited.contains(child)) {       // Check if the child has not been visited to avoid processing the same node multiple times
                    visited.add(child);               // Mark the child as visited by adding it to the 'visited' set
                    queue.add(child);                 // Add the child to the queue for future exploration
                }
            }
        }
        return false;           // If the queue is still empty and the target has not been found, return false.
    }


    /**
     * Retrieves the final answer for the query.
     *
     * @return String representation of the final probability, addition count, and multiplication count
     */
    public String getFinalAnswer() {
        double probability = 0;

        // Find the factor that involves the query node and retrieve the probability
        for (Factor factor : factors) {
            if (factor.involvesVariable(queryNode.getNodeName())) {
                probability = factor.getProbability(queryNode.getNodeName(), queryOutcome);
                break;
            }
        }

        return String.format("%.5f,%d,%d", probability, Factor.getAdditionCount(), Factor.getMultiplicationCount());
    }

    /**
     * Determines if the provided factor offers a direct answer to the query without further elimination.
     *
     * @param factor The factor to check
     * @return true if the factor provides a direct answer
     */
    private boolean isDirectAnswer(Factor factor) {
        boolean foundQueryOutcome = false;      // Flag to indicate if the query outcome is found
        String otherKey = null;                 // Variable to store a key that does not contain the query outcome

        // Check if the factor's probability table contains the query outcome
        for (String key : factor.getProbabilityTable().keySet()) {
            if (key.contains(queryNode.getNodeName() + "=" + queryOutcome)) {
                foundQueryOutcome = true;       // Set the flag if the query outcome is found
            } else {
                otherKey = key;                 // Store the other key that does not contain the query outcome
            }
        }

        // If the query outcome is found and there are no evidence nodes, it's a direct answer
        if (foundQueryOutcome && this.evidenceNodes.size() == 0){
            return true;
        }

        // If the query outcome is found and there is another key
        if (foundQueryOutcome && otherKey != null) {
            // Check if the other key contains exactly one piece of evidence
            int evidenceCount = 0;

            // Check if the other key contains exactly one piece of evidence
            for (Node evidenceNode : evidenceNodes) {
                if (otherKey.contains(evidenceNode.getNodeName() + "=")) {
                    evidenceCount++;
                }
            }

            // If the other key contains exactly one piece of evidence, it's a direct answer
            if (evidenceCount == 1) {
                return true;    // The factor provides a direct answer to the query
            }
        }
        return false;
    }

    /**
     * Retrieves the set of names of the evidence nodes.
     * @return Set of evidence node names
     */
    public Set<String> getEvidenceVariableNames() {
        Set<String> names = new HashSet<>();
        for (Node node : evidenceNodes) {
            names.add(node.getNodeName());
        }
        return names;
    }

    /**
     * Normalizes the final factors to ensure probabilities sum to 1.
     */
    private void normalizeFinalFactors() {
        if (queryNode == null) {
            System.out.println("No query node set, skipping normalization.");
            return;
        }
        Factor factorForQueryNode = null;

        // Find the factor that involves the query node
        for (Factor factor : factors) {
            if (factor.involvesVariable(queryNode.getNodeName())) {
                factorForQueryNode = factor;
                break;
            }
        }
        if (factorForQueryNode != null) {
            factorForQueryNode.normalize();
        }
    }
}


