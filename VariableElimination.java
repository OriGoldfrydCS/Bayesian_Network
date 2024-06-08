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
//    private int multiplicationCount;
//    private int additionCount;

    public VariableElimination(BayesianNetwork network, String query, String[] hidden, String[] evidence) {
        this.network = network;
        this.initialFactors = new ArrayList<>();  // Initialize the initial factors list
        this.factors = new ArrayList<>();
        this.hiddenNodes = new ArrayList<>();
        this.hiddenOrder = Arrays.asList(hidden);  // Initialize the order of hidden nodes from input
        this.evidenceNodes = new ArrayList<>();
        this.nodeEvidenceMap = new HashMap<>();
//        this.multiplicationCount = 0;
//        this.additionCount = 0;


        parseQuery(query);
        initializeNodes(hidden, evidence);
        Set<Node> irrelevantNodes = findIrrelevantNodes();
        generateRelevantFactors(irrelevantNodes);
        saveInitialFactors();  // Save the initial state of factors after generation

//        applyEvidence();
//        eliminateHiddenNodes(hidden);
//        normalizeFinalFactors();
        runVariableElimination();


//        generateRelevantFactors();
//        removeOneRowFactors();
//        sortFactors();
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

//    private void eliminateHiddenNodes(Node hiddenNode) {
//        System.out.println("Eliminating hidden node: " + hiddenNode.getNodeName());
//        Factor combinedFactor = null;
//        Iterator<Factor> it = factors.iterator();
//        while (it.hasNext()) {
//            Factor factor = it.next();
//            if (factor.involvesVariable(hiddenNode.getNodeName())) {
//                combinedFactor = combinedFactor == null ? factor : Factor.joinFactors(combinedFactor, factor);
//                it.remove();
//            }
//        }
//        if (combinedFactor != null) {
//            combinedFactor.eliminateFactor(hiddenNode.getNodeName());
//            factors.add(combinedFactor);
//        }
//    }


//    private void generateRelevantFactors() {
//        System.out.println("Generating relevant factors...");
//        Set<Node> irrelevantNodes = findIrrelevantNodes();
//
//        for (Node node : network.getNodes()) {
//            if (!irrelevantNodes.contains(node)) {
//                String[] evidenceArray = generateEvidenceArray(node);
//                System.out.println("Attempting to create factor for node: " + node.getNodeName() + " with evidence array: " + Arrays.toString(evidenceArray));
//                if (evidenceArray == null || evidenceArray.length == 0) {
//                    System.out.println("No evidence to process for node: " + node.getNodeName());
//                } else {
//                    try {
//                        Factor factor = new Factor(node.getCPT().getProbabilityTable(), evidenceArray);
//                        factor.filterIrrelevantRows();
//                        this.factors.add(factor);
//                        System.out.println("Successfully added factor for node: " + node.getNodeName());
//                    } catch (Exception e) {
//                        System.out.println("Error generating factor for node: " + node.getNodeName() + " - " + e.getMessage());
//                    }
//                }
//            } else {
//                System.out.println("Skipping irrelevant node: " + node.getNodeName());
//            }
//        }
//        System.out.println("Relevant factors generated.");
//    }


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

//    private String[] generateEvidenceArray(Node node) {
//        List<String> evidenceList = new ArrayList<>();
//        for (Map.Entry<Node, String> entry : nodeEvidenceMap.entrySet()) {
//            if (entry.getKey().equals(node)) {
//                String[] parts = entry.getValue().split("=");
//                if (parts.length == 2) {
//                    evidenceList.add(parts[1]);
//                } else {
//                    evidenceList.add(entry.getValue());
//                }
//            }
//        }
//        System.out.println("Evidence array for node " + node.getNodeName() + ": " + evidenceList);
//        return evidenceList.isEmpty() ? null : evidenceList.toArray(new String[0]);
//    }


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

    /// important ///////////////////////////////////////////////////////
//    private void removeOneRowFactors() {
//        System.out.println("Removing factors with only one row...");
//        Iterator<Factor> iterator = factors.iterator();
//        while (iterator.hasNext()) {
//            Factor factor = iterator.next();
//            if (factor.getTableEntries().size() <= 1) {
//                iterator.remove();
//                System.out.println("Removed factor: " + factor);
//            }
//        }
//        System.out.println("Factors with only one row removed.");
//    }

    private void sortFactors() {
        System.out.println("Sorting factors...");
        factors.sort(null);
        System.out.println("Factors sorted.");
    }

    /// important ///////////////////////////////////////////////////////

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

//                if (isDirectAnswer(joinedFactor)) {
//                    System.out.println("Direct answer possible after joining, proceeding to normalization.");
//                    factors.add(joinedFactor);
//                    normalizeFinalFactors();
//                    return;
//                }
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

//            if (isDirectAnswer(joinedFactor)) {
//                System.out.println("Final join provides direct answer, normalizing.");
//                normalizeFinalFactors();
//                return;
//            }
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



//    private void runVariableElimination() {
//        applyEvidence();
//        for (Node hiddenNode : hiddenNodes) {
//            System.out.println("Eliminating hidden variable: " + hiddenNode.getNodeName());
//            eliminateHiddenNodes(hiddenNode);
//        }
//        normalizeFinalFactors();
//    }

//    public String runVariableElimination() {
//        System.out.println("Running variable elimination...");
//        if (answerInFactor(queryNode)) {
//            System.out.println("The answer is already in the query factor.");
//            return getAnswerFromFactor();
//        }
//
//        eliminateHiddenNodes();
//        normalizeFinalFactor();
//        return formatResult();
//    }

//    private boolean answerInFactor(Node queryNode) {
//        System.out.println("Checking if the answer is in the query factor...");
//        String[] q = this.queryString.split("=");
//        for (Factor factor : factors) {
//            if (factor.getVariables().contains(q[0])) {
//                System.out.println("The answer is in the query factor.");
//                return true;
//            }
//        }
//        System.out.println("The answer is not in the query factor.");
//        return false;
//    }
//
//    private String getAnswerFromFactor() {
//        String[] q = this.queryString.split("=");
//        for (Factor factor : factors) {
//            for (Map.Entry<List<String>, Double> entry : factor.getFactorTable().entrySet()) {
//                if (entry.getKey().contains(q[0]) && entry.getValue().equals(q[1])) {
//                    System.out.println("Found answer in factor: " + entry.getValue());
//                    return entry.getValue() + "," + multiplicationCount + "," + additionCount;
//                }
//            }
//        }
//        return ""; // This should not happen, but just in case
//    }
//
//    private void eliminateHiddenNodes() {
//        System.out.println("Eliminating hidden nodes...");
//        for (Node hiddenNode : hiddenNodes) {
//            String hiddenVar = hiddenNode.getNodeName();
//            List<Factor> relevantFactors = new ArrayList<>();
//
//            // Collect all factors that include the hidden variable
//            for (Factor factor : factors) {
//                if (factor.getVariables().contains(hiddenVar)) {
//                    relevantFactors.add(factor);
//                    System.out.println("Added relevant factor: " + factor);
//                }
//            }
//
//            // Join factors containing the hidden variable
//            while (relevantFactors.size() > 1) {
//                List<Factor> newRelevantFactors = new ArrayList<>();
//                for (int i = 0; i < relevantFactors.size() - 1; i += 2) {
//                    Factor joinedFactor = joinFactors(relevantFactors.get(i), relevantFactors.get(i + 1), hiddenVar);
//                    newRelevantFactors.add(joinedFactor);
//                    System.out.println("Joined factors " + relevantFactors.get(i) + " and " + relevantFactors.get(i + 1) + " to create: " + joinedFactor);
//                    multiplicationCount++;
//                    additionCount++;
//                }
//
//                if (relevantFactors.size() % 2 != 0) {
//                    newRelevantFactors.add(relevantFactors.get(relevantFactors.size() - 1));
//                }
//                relevantFactors = newRelevantFactors;
//            }
//
//            if (!relevantFactors.isEmpty()) {
//                factors.removeAll(relevantFactors);
//                factors.add(relevantFactors.get(0));
//            }
//        }
//        System.out.println("Hidden nodes eliminated.");
//    }
//
//    private Factor joinFactors(Factor factor1, Factor factor2, String hiddenNode) {
//        System.out.println("Joining factors: " + factor1 + " and " + factor2);
//        Map<List<String>, Double> newProbTable = new HashMap<>();
//        Set<String> newVariables = new HashSet<>(factor1.getVariables());
//        newVariables.addAll(factor2.getVariables());
//        if (hiddenNode != null && !hiddenNode.isEmpty()) {
//            newVariables.remove(hiddenNode);
//            System.out.println("Removed hidden node " + hiddenNode + " from new variables");
//        }
//
//        for (Map.Entry<List<String>, Double> entry1 : factor1.getFactorTable().entrySet()) {
//            for (Map.Entry<List<String>, Double> entry2 : factor2.getFactorTable().entrySet()) {
//                List<String> newKey = new ArrayList<>();
//                double combinedProbability = entry1.getValue() * entry2.getValue();
//
//                multiplicationCount++;
//                System.out.println("Multiplied probabilities: " + entry1.getValue() + " and " + entry2.getValue() + " = " + combinedProbability);
//
//                newProbTable.merge(newKey, combinedProbability, (existingProb, newProb) -> {
//                    additionCount++;
//                    System.out.println("Added probabilities: " + existingProb + " and " + newProb + " = " + (existingProb + newProb));
//                    return existingProb + newProb;
//                });
//
//                Map<String, String> varStateMap = new HashMap<>();
//                updateVarStateMap(varStateMap, entry1.getKey(), factor1.getVariables());
//                updateVarStateMap(varStateMap, entry2.getKey(), factor2.getVariables());
//
//                for (String var : newVariables) {
//                    if (varStateMap.containsKey(var)) {
//                        newKey.add(varStateMap.get(var));
//                    }
//                }
//
//                newProbTable.merge(newKey, combinedProbability, Double::sum);
//            }
//        }
//
//        List<String> variablesList = new ArrayList<>(newVariables);
//        Factor newFactor = new Factor(newProbTable, variablesList.toArray(new String[0]));
//        System.out.println("Joined factors to create new factor: " + newFactor);
//        return newFactor;
//    }
//
//    private void updateVarStateMap(Map<String, String> varStateMap, List<String> states, Set<String> variables) {
//        Iterator<String> stateIterator = states.iterator();
//        for (String var : variables) {
//            if (stateIterator.hasNext()) {
//                varStateMap.put(var, stateIterator.next());
//            }
//        }
//        System.out.println("Updated variable state map for variables: " + variables);
//    }
//
//    private void normalizeFinalFactor() {
//        System.out.println("Normalizing final factor...");
//        if (factors.isEmpty()) {
//            System.out.println("No factors remaining to normalize.");
//            return;
//        }
//        Factor finalFactor = factors.get(0);
//        double totalProb = 0;
//        for (Double prob : finalFactor.getFactorTable().values()) {
//            totalProb += prob;
//            additionCount++;
//        }
//        if (totalProb == 0) {
//            System.out.println("Total probability is 0, no normalization needed.");
//            return;
//        }
//
//        Map<List<String>, Double> newTable = new HashMap<>();
//        for (Map.Entry<List<String>, Double> entry : finalFactor.getFactorTable().entrySet()) {
//            double normalizedProb = entry.getValue() / totalProb;
//            newTable.put(entry.getKey(), normalizedProb);
//            multiplicationCount++;
//            System.out.println("Normalized probability for key " + entry.getKey() + " to " + normalizedProb);
//        }
//        finalFactor.setFactorTable(newTable);
//        System.out.println("Final factor normalized.");
//    }
//
//    private String formatResult() {
//        System.out.println("Formatting result...");
//        if (factors.isEmpty()) {
//            return "No factors remaining to provide a result.";
//        }
//        StringBuilder result = new StringBuilder();
//        Factor finalFactor = factors.get(0);
//        for (Map.Entry<List<String>, Double> entry : finalFactor.getFactorTable().entrySet()) {
//            result.append(new DecimalFormat("#.00000").format(entry.getValue()))
//                    .append(",").append(multiplicationCount)
//                    .append(",").append(additionCount).append("\n");
//        }
//        System.out.println("Result formatted.");
//        return result.toString().trim();
//    }
}
