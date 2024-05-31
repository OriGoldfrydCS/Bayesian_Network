import java.util.*;

public class VariableElimination {
    private BayesianNetwork network;
    private int multiplications;
    private int additions;

    public VariableElimination(BayesianNetwork network) {
        this.network = network;
        this.multiplications = 0;
        this.additions = 0;
    }

    public Object[] query(Map<String, String> evidence, String queryVariable, char algorithm) {
        System.out.println("Query: " + queryVariable + ", Evidence: " + evidence);
        List<Factor> factors = initializeFactors(evidence);
        System.out.println("Initial Factors: " + factors);
        List<Node> hidden = getSortedHiddenNodes(evidence, queryVariable, algorithm);
        for (Node node : hidden) {
            factors = sumOut(node, factors);
            System.out.println("Factors after summing out " + node.getNodeName() + ": " + factors);
        }
        Factor result = combineFactors(factors);
        System.out.println("Final Combined Factor: " + result);

        List<String> queryVariables = new ArrayList<>();
        queryVariables.add(queryVariable);
        double probability = normalize(result.getProbability(queryVariables));
        System.out.println("Probability: " + probability);
        return new Object[]{probability, this.additions, this.multiplications};
    }

    private List<Node> getSortedHiddenNodes(Map<String, String> evidence, String queryVariable, char algorithm) {
        List<Node> hidden = new ArrayList<>();
        for (Node node : this.network.getNodes()) {
            if (!evidence.containsKey(node.getNodeName()) && !node.getNodeName().equals(queryVariable)) {
                hidden.add(node);
            }
        }

        if (algorithm == '2') {
            hidden.sort(Comparator.comparing(Node::getNodeName));
        } else if (algorithm == '3') {
            hidden.sort(Comparator.comparingInt(this::getFactorCount).reversed());
        }
        return hidden;
    }

    private int getFactorCount(Node node) {
        int count = 0;
        for (Node n : this.network.getNodes()) {
            if (n.getNodeName().equals(node.getNodeName())) {
                count++;
            }
        }
        return count;
    }

    private List<Factor> initializeFactors(Map<String, String> evidence) {
        List<Factor> factors = new ArrayList<>();
        for (Node node : this.network.getNodes()) {
            if (!evidence.containsKey(node.getNodeName())) {
                Map<List<String>, Double> probabilityTable = new HashMap<>();
                int stateIndex = 0;
                for (String state : node.getPossibleStates()) {
                    List<String> key = new ArrayList<>();
                    key.add(state);
                    double probability = getProbability(node, state);
                    probabilityTable.put(key, probability);
                    stateIndex++;
                }
                factors.add(new Factor(List.of(node), probabilityTable));
            }
        }
        return factors;
    }

    private double getProbability(Node node, String outcome) {
        List<String> key = new ArrayList<>();
        key.add(outcome);
        for (Node parent : node.getParents()) {
            key.add(parent.getPossibleStates().get(0)); // Assuming each parent has only one possible state
        }
        return node.getCPT().getProbability(key);
    }

    private List<Factor> sumOut(Node node, List<Factor> factors) {
        List<Factor> newFactors = new ArrayList<>();
        Factor merged = null;

        for (Factor factor : factors) {
            if (factor.contains(node.getNodeName())) {
                if (merged == null) {
                    merged = factor;
                } else {
                    merged = merged.multiply(factor);
                    this.multiplications++;
                }
            } else {
                newFactors.add(factor);
            }
        }

        if (merged != null) {
            Factor summedFactor = merged.sumOut(node);
            newFactors.add(summedFactor);
            this.additions++;
        }

        return newFactors;
    }

    private Factor combineFactors(List<Factor> factors) {
        Factor result = factors.get(0);
        for (int i = 1; i < factors.size(); i++) {
            result = result.multiply(factors.get(i));
            this.multiplications++;
        }
        return result;
    }

    private double normalize(double probability) {
        double denominator = probability + (1 - probability);
        return probability / denominator;
    }

    public int getMultiplications() {
        return this.multiplications;
    }

    public int getAdditions() {
        return this.additions;
    }
}