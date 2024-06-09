import java.util.*;
import java.util.regex.Pattern;

public class Factor implements Comparable<Factor> {
    private static int lastAssignedId = 0;              // Static variable to track the last assigned ID

    private String factorLabel;                         // Stores the label of the factor
    private Map<String, Double> probabilityTable;       // Represents probabilities, initially based on a conditional probability table
    private int factorId; // Simplified unique identifier
    private static int multiplicationCount = 0;
    private static int additionCount = 0;
    private Set<String> variables; // Set to store variable names

    // Constructor to initialize the factor with a probability table and a numeric index
    public Factor(Map<String, Double> CptTable, List<String> variables) {
        this.probabilityTable = new HashMap<>(CptTable);
        this.variables = new HashSet<>(variables); // Initialize variables set
        this.factorId = ++lastAssignedId;  // Increment and assign unique identifier
        this.factorLabel = generateLabel(variables);

        // Debug print to show the variables being initialized
        System.out.println("Factor created with ID: " + this.factorId + " and variables: " + this.variables);
    }

    private String generateLabel(List<String> dependencies) {
        if (dependencies.isEmpty()) {
            return "f" + this.factorId + "()";
        }
        return "f" + this.factorId + "(" + String.join(", ", dependencies) + ")";
    }


    // Constructor to initialize the factor with a probability table and a specific index
    public Factor(Map<String, Double> initialTable, String label) {
        this.probabilityTable = new HashMap<>(initialTable);
        this.factorId = lastAssignedId++;
        this.factorLabel = label;
    }

    // Helper method to shift the focus in the string parsing process
    private String shiftFocus(String remainingKeys) {
        if (remainingKeys.contains("|")) {
            return remainingKeys.substring(remainingKeys.indexOf('|') + 1);
        } else if (remainingKeys.contains(",")) {
            return remainingKeys.substring(remainingKeys.indexOf(',') + 1);
        } else {
            return "";
        }
    }

    public String getFactorLabel() {
        return "f" + getFactorId() + factorLabel;
    }

    public int getTableSize() {
        return probabilityTable.size();
    }


    public void excludeEntry(String key, String labelToRemove) {
        this.probabilityTable.remove(key);
        String[] labels = this.factorLabel.split(",");
        StringBuilder newLabel = new StringBuilder();
        for (String label : labels) {
            if (!label.equals(labelToRemove)) {
                if (newLabel.length() > 0) {
                    newLabel.append(",");
                }
                newLabel.append(label);
            }
        }
        this.factorLabel = newLabel.toString();
    }

    public void filterRows(Map<String, String> evidences) {
        System.out.println("Filtering rows based on evidence: " + evidences);
        Iterator<Map.Entry<String, Double>> it = probabilityTable.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Double> entry = it.next();
            boolean match = true;
            for (String condition : entry.getKey().split(",")) {
                String[] parts = condition.split("=");
                if (evidences.containsKey(parts[0]) && !evidences.get(parts[0]).equals(parts[1])) {
                    match = false;
                    break;
                }
            }
            if (!match) {
                System.out.println("Removing entry: " + entry.getKey() + " = " + entry.getValue());
                it.remove();
            }
        }
    }

    public Map<String, Double> getProbabilityTable() {
        return new HashMap<>(this.probabilityTable);
    }

    public double getProbability(String variable, String outcome) {
        for (Map.Entry<String, Double> entry : this.probabilityTable.entrySet()) {
            if (entry.getKey().contains(variable + "=" + outcome)) {
                return entry.getValue();
            }
        }
        return 0; // Return 0 if no matching outcome is found
    }

    // Comparison method for sorting or comparing two probability tables
    @Override
    public int compareTo(Factor other) {
        // First comparison based on size
        int result = Integer.compare(this.getTableSize(), other.getTableSize());
        if (result != 0) return result;

        // Secondary comparison based on ASCII value sums
        result = Integer.compare(this.sumASCII(), other.sumASCII());
        if (result != 0) return result;

        // Final tie-breaker using factor label comparison
        return this.factorLabel.compareTo(other.factorLabel);
    }

    // Calculates ASCII sum in a slightly obscured manner
    private int sumASCII() {
        int asciiSum = 0;
        for (String key : this.probabilityTable.keySet()) {
            asciiSum += key.chars().sum();  // Using streams to sum ASCII values
        }
        return asciiSum;
    }

    public int getFactorId(){
        return this.factorId;
    }

    public static Factor joinFactors(Factor factorA, Factor factorB, Set<String> evidenceVariables) {
        Map<String, Double> newTable = new HashMap<>();
        Set<String> combinedVariables = new HashSet<>(factorA.getVariables());
        combinedVariables.addAll(factorB.getVariables());

        String newLabel = "f" + (++lastAssignedId) + "(" + String.join(", ", combinedVariables) + ")";

        System.out.println("Joining Factors: " + factorA.getFactorLabel() + " and " + factorB.getFactorLabel());

        // Loop through every entry in both factor tables and multiply where keys match
        for (Map.Entry<String, Double> entryA : factorA.probabilityTable.entrySet()) {
            for (Map.Entry<String, Double> entryB : factorB.probabilityTable.entrySet()) {
                if (isCompatible(entryA.getKey(), entryB.getKey())) {
                    String newKey = mergeKeys(entryA.getKey(), entryB.getKey());
                    double newProbability = entryA.getValue() * entryB.getValue();
                    newTable.put(newKey, newProbability);
                    multiplicationCount++;
                    System.out.println("Merging: " + entryA.getKey() + " and " + entryB.getKey() + " -> " + newKey + " = " + newProbability);

                }
            }
        }
        System.out.println("Performed multiplications Operation. Current count: " + getMultiplicationCount());

        Factor newFactor = new Factor(newTable, new ArrayList<>(combinedVariables));
        System.out.println("New Factor Created: " + newFactor);
        return newFactor;
    }

    // Utility to combine variables from two factors into a set, excluding evidence variables
    private static Set<String> combineVariables(Factor factorA, Factor factorB, Set<String> evidenceVariables) {
        Set<String> varsA = extractVariables(factorA.getFactorLabel());
        Set<String> varsB = extractVariables(factorB.getFactorLabel());
        Set<String> combined = new HashSet<>(varsA);
        combined.addAll(varsB);  // Combine variables from both sets
        combined.removeAll(evidenceVariables);  // Remove evidence variables
        return combined;
    }

    // Method to extract variables from the factor label
    private static Set<String> extractVariables(String factorLabel) {
        Set<String> variables = new HashSet<>();
        if (factorLabel.contains("(") && factorLabel.contains(")")) {
            String varPart = factorLabel.substring(factorLabel.indexOf('(') + 1, factorLabel.indexOf(')'));
            String[] vars = varPart.split(", ");
            Collections.addAll(variables, vars);
        }
        return variables;
    }

    // Method to find common variables between two factors
    private static Set<String> findCommonVariables(Factor factorA, Factor factorB) {
        Set<String> variablesA = extractVariables(factorA.factorLabel);
        Set<String> variablesB = extractVariables(factorB.factorLabel);
        variablesA.retainAll(variablesB); // Retain only elements present in both sets
        return variablesA;
    }

    private static String combineLabels(String labelA, String labelB) {
        Set<String> combined = new LinkedHashSet<>(Arrays.asList(labelA.split(",")));
        combined.addAll(Arrays.asList(labelB.split(",")));
        return String.join(",", combined);
    }

    private static boolean isCompatible(String keyA, String keyB) {
        String[] pairsA = keyA.split(",");
        String[] pairsB = keyB.split(",");
        Map<String, String> mapA = new HashMap<>();
        Map<String, String> mapB = new HashMap<>();

        for (String pair : pairsA) {
            String[] splitPair = pair.split("=");
            mapA.put(splitPair[0], splitPair[1]);
        }

        for (String pair : pairsB) {
            String[] splitPair = pair.split("=");
            mapB.put(splitPair[0], splitPair[1]);
        }

        for (String var : mapA.keySet()) {
            if (mapB.containsKey(var) && !mapA.get(var).equals(mapB.get(var))) {
                return false; // Variables have different values in each key
            }
        }
        return true;
    }

    private static String mergeKeys(String keyA, String keyB) {
        Map<String, String> combined = new LinkedHashMap<>();
        String[] pairsA = keyA.split(",");
        String[] pairsB = keyB.split(",");

        for (String pair : pairsA) {
            String[] parts = pair.split("=");
            combined.put(parts[0], parts[1]);
        }

        for (String pair : pairsB) {
            String[] parts = pair.split("=");
            combined.putIfAbsent(parts[0], parts[1]);
        }

        // Convert the combined map back to a string
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : combined.entrySet()) {
            if (result.length() > 0) {
                result.append(",");
            }
            result.append(entry.getKey()).append("=").append(entry.getValue());
        }

        return result.toString();
    }

    // Method to check if the factor involves a particular variable
    public boolean involvesVariable(String variable) {
        for (String key : probabilityTable.keySet()) {
            if (key.contains(variable + "=")) {
                return true;
            }
        }
        return false;
    }

    public void eliminateFactor(String variable) {
        Map<String, Double> newTable = new HashMap<>();
        Map<String, Double> sums = new HashMap<>();

        System.out.println("Eliminating variable: " + variable + " from Factor: " + this.factorLabel);

        // Group entries by keys minus the eliminated variable and sum their probabilities
        for (Map.Entry<String, Double> entry : this.probabilityTable.entrySet()) {
            String newKey = removeVariableFromKey(entry.getKey(), variable);
            double existingProb = sums.getOrDefault(newKey, 0.0);
            sums.put(newKey, existingProb + entry.getValue());
            newTable.put(newKey, sums.get(newKey));
        }

        additionCount += sums.size(); // Additions occur when summing up values
        System.out.println("Additions during elimination: " + sums.size());

        this.probabilityTable = newTable;
        this.factorLabel = this.factorLabel.replace("," + variable, "").replace(variable + ",", "");

        System.out.println("After Elimination, New Factor: " + this);
    }

    public static int getMultiplicationCount() {
        return multiplicationCount;
    }

    public static int getAdditionCount() {
        return additionCount;
    }

    public static void resetCounts() {
        multiplicationCount = 0;
        additionCount = 0;
    }

    private static String removeVariableFromKey(String key, String variable) {
        String[] pairs = key.split(",");
        StringBuilder newKey = new StringBuilder();
        for (String pair : pairs) {
            if (!pair.startsWith(variable + "=")) {
                if (newKey.length() > 0) {
                    newKey.append(",");
                }
                newKey.append(pair);
            }
        }
        return newKey.toString();
    }

    public void normalize() {
        double sum = 0.0;
        for (double value : probabilityTable.values()) {
            sum += value;
        }
        if (sum == 0) return;  // Avoid division by zero in case all probabilities are zero

        System.out.println("Total before normalization: " + sum);


        for (Map.Entry<String, Double> entry : probabilityTable.entrySet()) {
            probabilityTable.put(entry.getKey(), entry.getValue() / sum);
        }

        if (probabilityTable.size() > 1) {
            additionCount += probabilityTable.size() - 1; // Counting additions needed to sum up probabilities
            System.out.println("Additions during normalization: " + (probabilityTable.size() - 1));
        }
    }

    public Set<String> getVariables() {
        Set<String> variables = new HashSet<>();
        if (factorLabel.contains("(") && factorLabel.contains(")")) {
            String varsPart = factorLabel.substring(factorLabel.indexOf('(') + 1, factorLabel.indexOf(')'));
            if (!varsPart.isEmpty()) {
                variables.addAll(Arrays.asList(varsPart.split(", ")));
            }
        }
        return variables;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getFactorLabel()).append(":\n");
        for (Map.Entry<String, Double> entry : this.probabilityTable.entrySet()) {
            sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}

