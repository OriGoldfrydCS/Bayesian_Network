import java.util.*;
import java.util.stream.Collectors;

public class VariableElimination {
    private BayesianNetwork network;
    private List<Factor> initialFactors;  // Store initial factors
    private List<Factor> factors;
    private List<Node> hiddenNodes;
    private List<String> hiddenOrder;  // List to store order of hidden nodes
    private List<Node> evidenceNodes;
    private Map<Node, String> nodeEvidenceMap;
    private Node queryNode;
    private String queryOutcome;

    public VariableElimination(BayesianNetwork network, String query, String[] hidden, String[] evidence) {
        this.network = network;
        this.initialFactors = new ArrayList<>();  // Initialize the initial factors list
        this.factors = new ArrayList<>();
        this.hiddenNodes = new ArrayList<>();
        this.hiddenOrder = Arrays.asList(hidden);  // Initialize the order of hidden nodes from input
        this.evidenceNodes = new ArrayList<>();
        this.nodeEvidenceMap = new HashMap<>();

        parseQuery(query);
        initializeNodes(hidden, evidence);
        Set<Node> irrelevantNodes = findIrrelevantNodes();
        generateRelevantFactors(irrelevantNodes);
        saveInitialFactors();  // Save the initial state of factors after generation

        runVariableElimination();

    }

    private void saveInitialFactors() {
        for (Factor factor : factors) {
            initialFactors.add(new Factor(factor.getProbabilityTable(), String.valueOf(factor.getVariables())));  // Deep copy each factor
        }
    }

    private void parseQuery(String query) {
        System.out.println("Parsing query: " + query);
        String[] queryParts = query.split("=");
        if (queryParts.length == 2) {
            this.queryNode = network.getNodeByName(queryParts[0].trim());
            this.queryOutcome = queryParts[1].trim();
            System.out.println("Query parsed successfully: Node - " + queryNode.getNodeName() + ", Condition - " + this.queryOutcome);
        } else {
            System.err.println("Query format error: " + query);
        }
    }

    private void initializeNodes(String[] hidden, String[] evidence) {
        System.out.println("Initializing nodes for variable elimination...");
        for (String hiddenNodeName : hidden) {
            Node node = network.getNodeByName(hiddenNodeName.trim());
            if (node != null) {
                this.hiddenNodes.add(node);
                System.out.println("Added hidden node: " + hiddenNodeName);
            } else {
                System.err.println("Warning: Hidden node not found in the network - " + hiddenNodeName);
            }
        }

        for (String evidenceItem : evidence) {
            String[] parts = evidenceItem.split("=");
            if (parts.length == 2) {
                Node node = network.getNodeByName(parts[0].trim());
                if (node != null) {
                    this.evidenceNodes.add(node);
                    this.nodeEvidenceMap.put(node, parts[1].trim());
                    System.out.println("Added evidence node: " + parts[0] + " = " + parts[1]);
                } else {
                    System.err.println("Warning: Evidence node not found in the network - " + parts[0]);
                }
            } else {
                System.err.println("Warning: Incorrect evidence format - " + evidenceItem);
            }
        }
    }

    private void applyEvidence() {
        System.out.println("Applying evidence to factors...");
        Map<String, String> evidenceMap = new HashMap<>();
        for (Node node : evidenceNodes) {
            evidenceMap.put(node.getNodeName(), nodeEvidenceMap.get(node));
            System.out.println("Evidence: " + node.getNodeName() + " = " + nodeEvidenceMap.get(node));
        }

        for (Factor factor : factors) {
            boolean involvesEvidence = evidenceMap.keySet().stream().anyMatch(factor::involvesVariable);
            if (involvesEvidence) {
                System.out.println("Before applying evidence to " + factor);
                factor.filterRows(evidenceMap);
                System.out.println("After applying evidence to " + factor);
            }
        }
    }

    private void normalizeFinalFactors() {
        if (queryNode == null) {
            System.out.println("No query node set, skipping normalization.");
            return;
        }
        System.out.println("Normalizing the final factor for the query node: " + queryNode.getNodeName() + "...");
        Factor factorForQueryNode = null;
        for (Factor factor : factors) {
            if (factor.involvesVariable(queryNode.getNodeName())) {
                factorForQueryNode = factor;
                break;
            }
        }
        if (factorForQueryNode != null) {
            System.out.println("Before normalization - Factor: " + factorForQueryNode);
            factorForQueryNode.normalize();
            System.out.println("After normalization - Factor: " + factorForQueryNode);
        } else {
            System.out.println("No factor found for the query node " + queryNode.getNodeName() + " to normalize.");
        }
    }


    private Set<Node> findIrrelevantNodes() {
        System.out.println("Finding irrelevant nodes...");
        Set<Node> irrelevant = new HashSet<>();

        for (Node hiddenNode : hiddenNodes) {
            String independence = BayesBall.checkIndependence(network, queryNode, hiddenNode, new ArrayList<>(evidenceNodes));
            if (independence.equals("yes") && !isAncestor(hiddenNode, queryNode) && evidenceNodes.stream().noneMatch(e -> isAncestor(hiddenNode, e))) {
                irrelevant.add(hiddenNode);
                System.out.println("Marked node " + hiddenNode.getNodeName() + " as irrelevant.");
            }
        }

        System.out.println("Irrelevant nodes found: " + irrelevant);
        return irrelevant;
    }

    private boolean isAncestor(Node start, Node target) {
        Set<Node> visited = new HashSet<>();
        LinkedList<Node> queue = new LinkedList<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current.equals(target)) {
                return true;
            }
            for (Node child : current.getChildren()) {
                if (!visited.contains(child)) {
                    visited.add(child);
                    queue.add(child);
                }
            }
        }
        return false;
    }

    private void generateRelevantFactors(Set<Node> irrelevantNodes) {
        System.out.println("Generating relevant factors...");
        for (Node node : network.getNodes()) {
            if (!irrelevantNodes.contains(node)) {
                Factor factor = node.getFactor();
                if (factor != null) {
                    factors.add(factor);
                    System.out.println("Factor added for node: " + node.getNodeName());
                }
            }
        }
    }


    public String getFinalAnswer() {
        double probability = 0;
        for (Factor factor : factors) {
            if (factor.involvesVariable(queryNode.getNodeName())) {
                probability = factor.getProbability(queryNode.getNodeName(), queryOutcome);
                break;
            }
        }

        return String.format("%.5f,%d,%d", probability, Factor.getAdditionCount(), Factor.getMultiplicationCount());
    }

    private void sortFactors() {
        System.out.println("Sorting factors...");
        factors.sort(null);
        System.out.println("Factors sorted.");
    }

    public void runVariableElimination() {
        factors = new ArrayList<>(initialFactors);  // Reset factors to initial state
        Factor.resetCounts();  // Resetting multiplication and addition counts before each run
        applyEvidence();
        Set<String> evidenceVariableNames = getEvidenceVariableNames();
        Collections.sort(factors);  // Use compareTo for sorting before joining

        System.out.println("Starting variable elimination with query node: " + queryNode.getNodeName() + "=" + queryOutcome);

        // Eliminate each hidden variable as per the order provided
        for (String hiddenVar : hiddenOrder) {
            List<Factor> factorsToJoin = new ArrayList<>();

            for (Iterator<Factor> it = factors.iterator(); it.hasNext();) {
                Factor factor = it.next();
                if (factor.involvesVariable(hiddenVar)) {
                    factorsToJoin.add(factor);
                    it.remove();  // Prevent reprocessing
                }
            }

            // Join factors that involve the hidden variable
            while (factorsToJoin.size() > 1) {

                Factor joinedFactor = Factor.joinFactors(factorsToJoin.get(0), factorsToJoin.get(1), evidenceVariableNames);
                factorsToJoin.remove(0);
                factorsToJoin.remove(0);
                factorsToJoin.add(0, joinedFactor);  // Add the newly joined factor at the start of the list
            }

            // After joining all factors involving the hidden variable, eliminate the variable
            if (!factorsToJoin.isEmpty()) {
                Factor remainingFactor = factorsToJoin.get(0);
                remainingFactor.eliminateFactor(hiddenVar);
                System.out.println("Post-elimination factor size: " + remainingFactor.getProbabilityTable().size());

                factors.add(remainingFactor);  // Add the modified factor back to the main list

                if (isDirectAnswer(remainingFactor)) {
                    System.out.println("Direct answer possible after elimination, proceeding to normalization.");
                    normalizeFinalFactors();
                    return;
                }
            }
        }

        // If multiple factors remain, join them into a single factor
        while (factors.size() > 1) {
            Factor joinedFactor = Factor.joinFactors(factors.get(0), factors.get(1), evidenceVariableNames);
            System.out.println("Final join factor size: " + joinedFactor.getProbabilityTable().size());

            factors.remove(0);
            factors.remove(0);
            factors.add(joinedFactor);
        }

        // Normalize the final factor
        normalizeFinalFactors();
    }

    private boolean isDirectAnswer(Factor factor) {
        System.out.println("Checking keys in factor's probability table...");
        if (factor.getTableSize() == 2) {
            boolean foundQueryOutcome = false;
            String otherKey = null;
            for (String key : factor.getProbabilityTable().keySet()) {
                if (key.contains(queryNode.getNodeName() + "=" + queryOutcome)) {
                    foundQueryOutcome = true;
                } else {
                    otherKey = key;
                }
            }

            if (foundQueryOutcome && otherKey != null) {
                // Check if the other key contains exactly one piece of evidence
                int evidenceCount = 0;
                for (Node evidenceNode : evidenceNodes) {
                    if (otherKey.contains(evidenceNode.getNodeName() + "=")) {
                        evidenceCount++;
                    }
                }

                if (evidenceCount == 1) {
                    System.out.println("Direct answer found with key: " + queryNode.getNodeName() + "=" + queryOutcome + " and one piece of evidence remaining.");
                    return true; // The factor provides a direct answer to the query
                }
            }
        }
        return false;
    }

    private void resetFactors() {
        factors.clear();
        for (Factor initialFactor : initialFactors) {
            factors.add(new Factor(initialFactor.getProbabilityTable(), String.valueOf(initialFactor.getVariables())));  // Deep copy each initial factor
        }
    }

    public Set<String> getEvidenceVariableNames() {
        return evidenceNodes.stream()
                .map(Node::getNodeName)
                .collect(Collectors.toSet());
    }

    public List<Node> getEvidenceNodes(){
        return evidenceNodes;
    }


    private void removeIfOneValued(Factor factor) {
        if (factor.getTableSize() == 1) {
            factors.remove(factor);
        }
    }
}
