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

        List<Variable> hidden = getSortedHiddenVariables(evidence, queryVariable, algorithm);
        for (Variable variable : hidden) {
            factors = sumOut(variable.getName(), factors);
            System.out.println("Factors after summing out " + variable.getName() + ": " + factors);
        }

        Factor result = combineFactors(factors);
        System.out.println("Final Combined Factor: " + result);
        double probability = normalize(result.getProbability(queryVariable));
        System.out.println("Probability: " + probability);

        return new Object[] { probability, this.additions, this.multiplications };
    }

    private List<Variable> getSortedHiddenVariables(Map<String, String> evidence, String queryVariable, char algorithm) {
        List<Variable> hidden = new ArrayList<>();
        for (Variable var : this.network.getVariables()) {
            if (!evidence.containsKey(var.getName()) && !var.getName().equals(queryVariable)) {
                hidden.add(var);
            }
        }

        hidden.sort(Comparator.comparingInt(this::getFactorCount).reversed());
        return hidden;
    }

    private int getFactorCount(Variable variable) {
        int count = 0;
        for (CPT cpt : this.network.getCPTs().values()) {
            if (cpt.getParents().contains(variable)) {
                count++;
            }
        }
        return count;
    }

    private List<Factor> initializeFactors(Map<String, String> evidence) {
        List<Factor> factors = new ArrayList<>();
        for (Variable var : this.network.getVariables()) {
            factors.add(new Factor(var, evidence));
        }
        return factors;
    }

    private List<Factor> sumOut(String varName, List<Factor> factors) {
        List<Factor> newFactors = new ArrayList<>();
        Factor merged = null;

        for (Factor factor : factors) {
            if (factor.contains(varName)) {
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
            Factor summedFactor = merged.sumOut(varName);
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