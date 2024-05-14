import java.util.*;
import java.util.stream.Collectors;

public class VariableElimination {
    private Map<String, Variable> variables;
    private Map<String, CPT> cpts;
    private int additionOperations = 0;
    private int multiplicationOperations = 0;

    public VariableElimination(Map<String, Variable> variables, Map<String, CPT> cpts) {
        this.variables = variables;
        this.cpts = cpts;
    }

    public double computeProbability(List<String> hiddenVars, Map<String, String> evidence, String target) {
        System.out.println("Starting Variable Elimination:");
        List<Factor> factors = initializeFactors(evidence);

        // Eliminate each hidden variable
        for (String var : hiddenVars) {
            System.out.println("Eliminating Variable: " + var);
            List<Factor> relevantFactors = factors.stream()
                    .filter(f -> f.getVariables().contains(var))
                    .collect(Collectors.toList());
            Factor merged = mergeFactors(relevantFactors, var);
            System.out.println("Merged Factor for " + var + ": " + merged);

            factors.removeAll(relevantFactors);
            if (merged != null) factors.add(merged);
        }

        // Merge remaining factors
        System.out.println("Merging remaining factors");

        Factor finalFactor = mergeFactors(factors, null);  // No variable to eliminate, just combine them
        double probability = finalFactor.getProbability(target, evidence.get(target));
        System.out.println("Final Probability: " + probability);
        return probability;
    }

    private List<Factor> initializeFactors(Map<String, String> evidence) {
        List<Factor> factors = cpts.values().stream()
                .map(cpt -> {
                    System.out.println("Initializing Factor from CPT:");
                    cpt.printCPT();  // Print the CPT details
                    return new Factor(cpt.getVariables(), cpt.computeProbabilityTable());
                })
                .collect(Collectors.toList());

        System.out.println("Applying evidence to Factors:");
        return factors.stream()
                .map(factor -> {
                    Factor result = factor.applyEvidence(evidence);
                    System.out.println("After applying evidence: " + result);
                    return result;
                })
                .collect(Collectors.toList());
    }

    private Factor mergeFactors(List<Factor> factors, String eliminateVar) {
        System.out.println("Factors before merging: " + factors);
        Factor result = factors.get(0);
        for (int i = 1; i < factors.size(); i++) {
            result = result.combine(factors.get(i), eliminateVar);
            System.out.println("Intermediate merged factor: " + result);

            if (eliminateVar != null) {
                result = result.sumOut(eliminateVar);
                additionOperations++;  // Assuming summing out involves additions
                System.out.println("After summing out " + eliminateVar + ": " + result);

            }
        }
        return result;
    }

    public int getAdditionOperations() {
        return additionOperations;
    }

    public int getMultiplicationOperations() {
        return multiplicationOperations;
    }
}
