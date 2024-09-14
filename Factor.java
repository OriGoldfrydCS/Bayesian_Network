import java.util.*;


/**
 * Represents a factor in a probabilistic model, used for variable elimination and probability calculations.
 */
public class Factor implements Comparable<Factor>, Cloneable {
    private static int lastAssignedId = 0;              // Static variable to track the last assigned ID
    private String factorLabel;                         // Stores the label describing the factor
    private Map<String, Double> probabilityTable;       // Table mapping variable states to probabilities
    private int factorId;                               // Unique ID for this factor instance
    private static int multiplicationCount = 0;         // Tracks the number of multiplications performed
    private static int additionCount = 0;               // Tracks the number of additions performed
    private Set<String> variables;                      // Set of variables involved in this factor

    /**
     * Constructs a Factor with a given probability table and a list of variables.
     * @param CptTable Conditional probability table as a map
     * @param variables List of variable names involved in the factor
     */
    public Factor(Map<String, Double> CptTable, List<String> variables) {
        this.probabilityTable = new HashMap<>(CptTable);
        this.variables = new HashSet<>(variables);      // Initialize variables set
        this.factorId = ++lastAssignedId;               // Increment and assign unique identifier
        this.factorLabel = generateLabel(variables);

        // Debug print to show the variables being initialized
    }

    /**
     * Another constructor that initializes the factor with a specified label.
     * @param initialTable The probability table to be assigned
     * @param label A custom label for the factor
     */
    public Factor(Map<String, Double> initialTable, String label) {
        this.probabilityTable = new HashMap<>(initialTable);
        this.factorId = lastAssignedId++;
        this.factorLabel = label;
    }

    /**
     * Generates a descriptive label for the factor based on its variables.
     * @param dependencies List of variable names that define the factor's scope
     * @return A formatted string representing the label of the factor
     */
    private String generateLabel(List<String> dependencies) {
        if (dependencies.isEmpty()) {
            return "f" + this.factorId + "()";
        }
        return "f" + this.factorId + "(" + String.join(", ", dependencies) + ")";
    }

    /**
     * Retrieves the factor's label.
     * @return The label of the factor
     */
    public String getFactorLabel() {
        return "f" + getFactorId() + factorLabel;
    }

    /**
     * Gets the size of the probability table.
     * @return The number of entries in the probability table
     */
    public int getTableSize() {
        return probabilityTable.size();
    }

    /**
     * Filters rows in the probability table based on the given evidence.
     * @param evidences Map of evidence variables and their observed values
     */
    public void filterRows(Map<String, String> evidences) {
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
                it.remove();
            }
        }
    }

    /**
     * Retrieves a copy of the probability table.
     * @return A new map containing the same entries as the original probability table
     */
    public Map<String, Double> getProbabilityTable() {
        return new HashMap<>(this.probabilityTable);
    }

    /**
     * Retrieves the probability associated with a specific variable outcome.
     * @param variable The variable of interest
     * @param outcome The specific outcome to query
     * @return The probability of the outcome, or 0 if not found
     */
    public double getProbability(String variable, String outcome) {
        for (Map.Entry<String, Double> entry : this.probabilityTable.entrySet()) {
            if (entry.getKey().contains(variable + "=" + outcome)) {
                return entry.getValue();
            }
        }
        return 0; // Return 0 if no matching outcome is found
    }

    /**
     * Compares this factor to another based on their probability table sizes and lexicographical order.
     * @param other The other factor to compare to
     * @return Negative, zero, or positive based on comparison result
     */
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

    /**
     * Sums the ASCII values of the keys in the probability table to use in sorting.
     * @return The sum of ASCII values
     */
    private int sumASCII() {
        int asciiSum = 0;
        for (String key : this.probabilityTable.keySet()) {
            asciiSum += key.chars().sum();  // Using streams to sum ASCII values
        }
        return asciiSum;
    }

    /**
     * Retrieves the factor's unique ID.
     * @return The unique identifier for this factor
     */
    public int getFactorId(){
        return this.factorId;
    }

    /**
     * Static method to join two factors into a new factor, combining their variables and probabilities.
     * @param factorA The first factor to join
     * @param factorB The second factor to join
     * @param evidenceVariables Set of variable names that are observed as evidence
     * @return A new factor resulting from the join operation
     */
    public static Factor joinFactors(Factor factorA, Factor factorB, Set<String> evidenceVariables) {
        int mults = 0;
        int adds = 0;
        Map<String, Double> newTable = new HashMap<>();
        Set<String> combinedVariables = new HashSet<>(factorA.getVariables());
        combinedVariables.addAll(factorB.getVariables());

        // Generate a new label for the joined factor
        String newLabel = "f" + (++lastAssignedId) + "(" + String.join(", ", combinedVariables) + ")";

        // Loop through every entry in both factor tables and multiply where keys match
        for (Map.Entry<String, Double> entryA : factorA.probabilityTable.entrySet()) {
            for (Map.Entry<String, Double> entryB : factorB.probabilityTable.entrySet()) {

                // Determine by isCompatible() if two entries (from factorA and factorB) are compatible—that is,
                // whether they can be merged based on the values assigned to their common variables.
                if (isCompatible(entryA.getKey(), entryB.getKey())) {
                    String newKey = mergeKeys(entryA.getKey(), entryB.getKey());
                    double newProbability = entryA.getValue() * entryB.getValue();
                    newTable.put(newKey, newProbability);
                    multiplicationCount++;
                    mults++;

                }
            }
        }

        // List of variables to remove from the combined set after determining unique outcomes
        ArrayList<String> combinedVariablesList = new ArrayList<String>(combinedVariables);
        ArrayList<String> toRemove = new ArrayList<>();
        for(String unionVarName : combinedVariablesList){
            HashSet<String> res = new HashSet<String>();
            for(String varsRow : newTable.keySet()){
                String[] varsIsolateWithREs =  varsRow.split(",");
                for(String var_outcome : varsIsolateWithREs){
                    String[] splitted = var_outcome.split("=");
                    String varName = splitted[0];
                    String outcoume = splitted[1];
                    if(unionVarName.equals(varName)){
                        res.add(outcoume);
                    }
                }

            }
            // Remove variable from the combined set if it has only one unique outcome
            if(res.size() == 1){
                combinedVariables.remove(unionVarName);
                toRemove.add(unionVarName);
            }
        }

        // Remove terms from the keys in the new probability table based on the variables to remove
        newTable = removeTermsFromKeys((HashMap<String, Double>) newTable, toRemove);

        // Create a new factor with the updated probability table and variables
        Factor newFactor = new Factor(newTable, new ArrayList<>(combinedVariables));
        System.out.println("New Factor Created: " + newFactor);
        return newFactor;
    }

    /**
     * Removes specified terms from the keys of the probability table.
     * @param originalMap The original probability table
     * @param termsToRemove List of terms to remove from the keys
     * @return Updated probability table with terms removed from keys
     */
    public static HashMap<String, Double> removeTermsFromKeys(HashMap<String, Double> originalMap, ArrayList<String> termsToRemove) {
        HashMap<String, Double> updatedMap = new HashMap<>();

        // Iterate over each entry in the original probability table
        for (Map.Entry<String, Double> entry : originalMap.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();

            // Find and remove the specified terms and their values (e.g., "B2=F" or "C3=H")
            String[] parts = key.split(",");
            StringBuilder newKey = new StringBuilder();
            for (String part : parts) {
                boolean keepPart = true;
                for (String term : termsToRemove) {
                    if (part.startsWith(term + "=")) {
                        keepPart = false;
                        break;
                    }
                }
                if (keepPart) {
                    if (newKey.length() > 0) {
                        newKey.append(",");
                    }
                    newKey.append(part);
                }
            }

            // Combine values for the same new key
            String newKeyStr = newKey.toString();
            updatedMap.put(newKeyStr, updatedMap.getOrDefault(newKeyStr, 0.0) + value);
        }
        return updatedMap;
    }

    /**
     * Checks if two keys from different factors are compatible based on their variable assignments.
     * @param keyA Key from the first factor
     * @param keyB Key from the second factor
     * @return true if the keys are compatible, false otherwise
     */
    private static boolean isCompatible(String keyA, String keyB) {
        String[] pairsA = keyA.split(",");     // Split the keyA string by commas to separate into variable=value pairs
        String[] pairsB = keyB.split(",");     // As below
        Map<String, String> mapA = new HashMap<>();
        Map<String, String> mapB = new HashMap<>();

        // Iterate over each pair in keyA to populate mapA
        for (String pair : pairsA) {
            String[] splitPair = pair.split("=");               // Split each pair into variable and value
            mapA.put(splitPair[0], splitPair[1]);                     // Put the variable and value into mapA
        }
        try {
            // Iterate over each pair in keyB to populate mapB
            for (String pair : pairsB) {
                String[] splitPair = pair.split("=");               // Split each pair into variable and value
                mapB.put(splitPair[0], splitPair[1]);                     // Put the variable and value into mapB
            }
        }catch (Exception e){
            int i = 0;
        }

        // Iterate through each variable in mapA's keySet
        for (String var : mapA.keySet()) {
            // Check if mapB also has the variable and their values are not equal
            if (mapB.containsKey(var) && !mapA.get(var).equals(mapB.get(var))) {
                // If a variable is in both maps but their values differ, return false
                return false;
            }
        }

        // If no different variable values are found, return true
        return true;
    }

    /**
     * Merges keys from two compatible factors into a single key for the joined factor.
     * @param keyA Key from the first factor
     * @param keyB Key from the second factor
     * @return A merged key combining variable assignments from both keys
     */
    private static String mergeKeys(String keyA, String keyB) {
        Map<String, String> combined = new HashMap<>();   // HashMap to maintain the order of insertion
        String[] pairsA = keyA.split(",");
        String[] pairsB = keyB.split(",");

        // Iterate over each pair from keyA
        for (String pair : pairsA) {
            String[] parts = pair.split("=");
            combined.put(parts[0], parts[1]);
        }

        // Iterate over each pair from keyB
        for (String pair : pairsB) {
            String[] parts = pair.split("=");
            combined.putIfAbsent(parts[0], parts[1]);
        }

        // Convert the combined map back to a string
        StringBuilder result = new StringBuilder();

        // Iterate over each entry in the combined map
        for (Map.Entry<String, String> entry : combined.entrySet()) {
            if (result.length() > 0) {                  // If not the first entry, append a comma to separate pairs
                result.append(",");
            }

            // Append the variable=value pair to the result string
            result.append(entry.getKey()).append("=").append(entry.getValue());
        }

        return result.toString();           // Convert the StringBuilder to a String and return it
    }

    /**
     * Determines whether a specified variable is part of any entry in the probabilityTable.
     * This table is assumed to contain keys that are strings formatted as combinations of variable-value pairs
     * (like "Variable1=Value1,Variable2=Value2,...").
     * @param variable The variable name to check within the factor.
     * @return true if the variable is involved in the factor, false otherwise.
     */
    public boolean involvesVariable(String variable) {
        for (String key : probabilityTable.keySet()) {
            if (key.contains(variable + "=")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Eliminates a specified variable from the factor, summing over its states.
     * @param variable The variable to eliminate
     */
    public void eliminateFactor(String variable) {
        int initSize =  this.probabilityTable.size();
        int prevAdd = additionCount;
        Map<String, Double> newTable = new HashMap<>();    // Initialize a new HashMap to store the updated probability table after process
        Map<String, Double> sums = new HashMap<>();        // Initialize a HashMap to store sums of probabilities for each unique combination of remaining variables.


        // Iterate over each entry in the current probability table
        for (Map.Entry<String, Double> entry : this.probabilityTable.entrySet()) {
            String newKey = removeVariableFromKey(entry.getKey(), variable);            // Remove the specified variable from the key to create a new key for the reduced factor
            double existingProb = sums.getOrDefault(newKey, 0.0);            // Retrieve the current probability sum for this new key, defaulting to 0.0 if it does not exist
            sums.put(newKey, existingProb + entry.getValue());                          // Update the sum for this new key by adding the current entry's probability
            newTable.put(newKey, sums.get(newKey));                                     // Update or set the probability for this new key in the new probability table
        }

        // Increment the addition count by the number of unique entries processed (i.e., the size of sums)
        additionCount += (initSize - sums.size());

        // Replace the old probability table with the new table after elimination
        this.probabilityTable = newTable;

        // Update the factor label by removing the eliminated variable from the label
        this.factorLabel = this.factorLabel.replace("," + variable, "").replace(variable + ",", "");
        System.out.println("adds after elimination " + variable+ ": " + (additionCount - prevAdd));
    }

    /**
     * Retrieves the count of multiplication operations performed.
     * @return The total count of multiplication operations since the last reset.
     */
    public static int getMultiplicationCount() {
        return multiplicationCount;
    }

    /**
     * Retrieves the count of addition operations performed.
     * @return The total count of addition operations since the last reset.
     */
    public static int getAdditionCount() {
        return additionCount;
    }

    /**
     * Resets the static counters for multiplications and additions to zero.
     */
    public static void resetCounts() {
        multiplicationCount = 0;
        additionCount = 0;
    }

    /**
     * Removes a specified variable from a key string.
     * @param key The key string from which to remove the variable
     * @param variable The variable to remove
     * @return A new key string with the variable removed
     */
    private static String removeVariableFromKey(String key, String variable) {
        String[] pairs = key.split(",");        // Split the key into pairs
        StringBuilder newKey = new StringBuilder();

        // Iterate over each pair in the key
        for (String pair : pairs) {
            // If the pair does not start with the specified variable, add it to the new key
            if (!pair.startsWith(variable + "=")) {
                if (newKey.length() > 0) {
                    newKey.append(",");     // Add a comma if the new key is not empty
                }
                newKey.append(pair);        // Append the pair to the new key
            }
        }

        // Return the new key as a string
        return newKey.toString();
    }

    /**
     * Normalizes the probabilities in the probability table so that they sum to 1.
     */
    public void normalize() {
        int prevAdd = Factor.additionCount;     // Store the previous addition count (for debug)
        double sum = 0.0;

        // Calculate the sum of all probabilities in the table
        for (double value : probabilityTable.values()) {
            sum += value;
        }

        // Avoid division by zero in case all probabilities are zero
        if (sum == 0) return;

        // Divide each probability by the sum to normalize them
        for (Map.Entry<String, Double> entry : probabilityTable.entrySet()) {
            probabilityTable.put(entry.getKey(), entry.getValue() / sum);
        }

        // If the probability table has more than one entry, update the addition count
        if (probabilityTable.size() > 1) {
            additionCount += probabilityTable.size() - 1;   // Count additions needed to sum up probabilities
            System.out.println("adds normalized: " + (additionCount - prevAdd));
        }
    }

    /**
     * Retrieves a set of variables from the factor label.
     * @return A set containing the variable names
     */
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

    /**
     * Provides a string representation of the factor for debugging purposes.
     * @return A string describing the factor
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getFactorLabel()).append(":\n");
        for (Map.Entry<String, Double> entry : this.probabilityTable.entrySet()) {
            sb.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Creates a clone of the Factor.
     * This method ensures a deep copy of the probability table and variables set.
     * @return A cloned instance of the Factor.
     */
    @Override
    public Factor clone() {
        try {
            Factor clone = (Factor) super.clone();
            clone.probabilityTable = new HashMap<>(this.probabilityTable);    // Create a deep copy of the probability table
            clone.variables = new HashSet<>(this.variables);                  // Create a deep copy of the variables set
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}