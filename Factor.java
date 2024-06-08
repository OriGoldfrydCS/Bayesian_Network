import java.util.*;
import java.util.regex.Pattern;

public class Factor implements Comparable<Factor> {
    private static int lastAssignedId = 0;              // Static variable to track the last assigned ID

    private String factorLabel;                         // Stores the label of the factor
    private Map<String, Double> probabilityTable;       // Represents probabilities, initially based on a conditional probability table
//    private String uniqueIdentifier;                    // Unique identifier for this factor
    private int factorId; // Simplified unique identifier

    // Constructor to initialize the factor with a probability table and a numeric index
    public Factor(Map<String, Double> CptTable, List<String> dependencies) {
        this.probabilityTable = new HashMap<>(CptTable);
        this.factorId = ++lastAssignedId;  // Increment and assign unique identifier
//        this.uniqueIdentifier = String.valueOf(id);   // Increment and assign unique identifier
        this.factorLabel = generateLabel(dependencies);
    }

    private String generateLabel(List<String> dependencies) {
        if (dependencies.isEmpty()) {
            return "f" + this.factorId + "()";
        }
        return "f" + this.factorId + "(" + String.join(", ", dependencies) + ")";
    }

//    private void updateLabel() {
//        this.factorLabel = "f(" + factorLabel + ")" + getUniqueIdentifier();
//    }

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
        return factorLabel;
    }

    public int getTableSize() {
        return probabilityTable.size();
    }

//    public String getUniqueIdentifier() {
//        return uniqueIdentifier;
//    }



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

//
//    // Calculates the sum of ASCII values of the keys in the first entry of the table
//    private int calculateChecksum() {
//        int checksum = 0;
//        String[] keys = this.factorLabel.split(",");
//        for (String key : keys) {
//            checksum += key.length() * 31;  // Multiplying by a prime number to spread out checksum values
//        }
//        return checksum;
//    }
    public int getFactorId(){
        return this.factorId;
    }

    public static Factor joinFactors(Factor factorA, Factor factorB, Set<String> evidenceVariables) {
        Map<String, Double> newTable = new HashMap<>();
        Set<String> combinedVariables = combineVariables(factorA, factorB, evidenceVariables);
        int newId = ++lastAssignedId;  // Ensure new ID is assigned at the time of creating a new factor
        String newLabel = "f" + newId + "(" + String.join(", ", combinedVariables) + ")";

//        String newLabel = combineLabels(factorA.getFactorLabel(), factorB.getFactorLabel());

        System.out.println("Joining Factors: " + factorA.getFactorLabel() + " and " + factorB.getFactorLabel());

        // Loop through every entry in both factor tables and multiply where keys match
        for (Map.Entry<String, Double> entryA : factorA.probabilityTable.entrySet()) {
            for (Map.Entry<String, Double> entryB : factorB.probabilityTable.entrySet()) {
                if (isCompatible(entryA.getKey(), entryB.getKey())) {
                    String newKey = mergeKeys(entryA.getKey(), entryB.getKey());
                    double newProbability = entryA.getValue() * entryB.getValue();
                    newTable.put(newKey, newProbability);
                    System.out.println("Merging: " + entryA.getKey() + " and " + entryB.getKey() + " -> " + newKey + " = " + newProbability);
                }
            }
        }

        Factor newFactor = new Factor(newTable, newLabel);
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

    // Method to extract variables from the factor label
//    private static Set<String> extractVariables(String factorLabel) {
//        if (factorLabel.contains("(") && factorLabel.contains(")")) {
//            String varPart = factorLabel.substring(factorLabel.indexOf('(') + 1, factorLabel.indexOf(')'));
//            return new HashSet<>(Arrays.asList(varPart.split(", ")));
//        }
//        return new HashSet<>();
//    }

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
        System.out.println("Eliminating variable: " + variable + " from Factor: " + this.factorLabel);

        // Group entries by keys minus the eliminated variable and sum their probabilities
        for (Map.Entry<String, Double> entry : this.probabilityTable.entrySet()) {
            String newKey = removeVariableFromKey(entry.getKey(), variable);
            newTable.merge(newKey, entry.getValue(), Double::sum);
        }

        this.probabilityTable = newTable;
        this.factorLabel = this.factorLabel.replace("," + variable, "").replace(variable + ",", "");

        System.out.println("After Elimination, New Factor: " + this);
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

        for (Map.Entry<String, Double> entry : probabilityTable.entrySet()) {
            probabilityTable.put(entry.getKey(), entry.getValue() / sum);
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

//    public static void main(String[] args) {
//        // Define factors f3, f4
//        Map<String, Double> tableF3 = new HashMap<>();
//        tableF3.put("E=F,B=T,A=T", 0.94);
//        tableF3.put("E=T,B=T,A=F", 0.05);
//        tableF3.put("E=T,B=T,A=T", 0.95);
//        tableF3.put("E=T,B=F,A=F", 0.71);
//        tableF3.put("E=T,B=F,A=T", 0.29);
//        tableF3.put("E=F,B=F,A=T", 0.001);
//        tableF3.put("E=F,B=F,A=F", 0.999);
//        tableF3.put("E=F,B=T,A=F", 0.06);
//        Factor f3 = new Factor(tableF3, "f3");
//
//        Map<String, Double> tableF5 = new HashMap<>();
//        tableF5.put("A=F,M=F", 0.99);
//        tableF5.put("A=T,M=T", 0.7);
//        tableF5.put("A=T,M=F", 0.3);
//        tableF5.put("A=F,M=T", 0.01);
//        Factor f5 = new Factor(tableF5, "f5");
//
//        Map<String, Double> tableF4 = new HashMap<>();
//        tableF4.put("A=F,J=T", 0.05);
//        tableF4.put("A=T,J=T", 0.9);
//        tableF4.put("A=F,J=F", 0.95);
//        tableF4.put("A=T,J=F", 0.1);
//        Factor f4 = new Factor(tableF4, "f4");
//
//        // Join f4 * f5 to create f6
//        Factor f6 = joinFactors(f4, f5);
//        System.out.println("Factor f6 (f4 * f5):");
//        System.out.println(f6);
//
//
//
//        // Join f6 * f3 to create f7
//        Factor f7 = joinFactors(f6, f3);
//        System.out.println("Factor f7 (f6 * f3):");
//        System.out.println(f7);
//
//        // Eliminate variable A from f7 to create f8
//        f7.eliminateFactor("A");
//        System.out.println("Factor f8 (after eliminating A from f7):");
//        System.out.println(f7);

//    }
//        Map<String, Double> tableA = new HashMap<>();
//        tableA.put("Z=A,X=A", 0.3);
//        tableA.put("Z=A,X=B", 0.5);
//        tableA.put("Z=A,X=C", 0.2);
//        tableA.put("Z=B,X=A", 0.2);
//        tableA.put("Z=B,X=B", 0.5);
//        tableA.put("Z=B,X=C", 0.3);
//        tableA.put("Z=C,X=A", 0.4);
//        tableA.put("Z=C,X=B", 0.4);
//        tableA.put("Z=C,X=C", 0.2);
//
//        Map<String, Double> tableB = new HashMap<>();
//        tableB.put("X=A,Z=A,Y=A", 0.4);
//        tableB.put("X=A,Z=A,Y=B", 0.4);
//        tableB.put("X=A,Z=A,Y=C", 0.2);
//        tableB.put("X=B,Z=A,Y=A", 0.5);
//        tableB.put("X=B,Z=A,Y=B", 0.3);
//        tableB.put("X=B,Z=A,Y=C", 0.2);
//        tableB.put("X=C,Z=A,Y=A", 0.3);
//        tableB.put("X=C,Z=A,Y=B", 0.4);
//        tableB.put("X=C,Z=A,Y=C", 0.3);
//        tableB.put("X=A,Z=B,Y=A", 0.6);
//        tableB.put("X=A,Z=B,Y=B", 0.3);
//        tableB.put("X=A,Z=B,Y=C", 0.1);
//        tableB.put("X=B,Z=B,Y=A", 0.4);
//        tableB.put("X=B,Z=B,Y=B", 0.4);
//        tableB.put("X=B,Z=B,Y=C", 0.2);
//        tableB.put("X=C,Z=B,Y=A", 0.2);
//        tableB.put("X=C,Z=B,Y=B", 0.3);
//        tableB.put("X=C,Z=B,Y=C", 0.5);
//        tableB.put("X=A,Z=C,Y=A", 0.5);
//        tableB.put("X=A,Z=C,Y=B", 0.3);
//        tableB.put("X=A,Z=C,Y=C", 0.2);
//        tableB.put("X=B,Z=C,Y=A", 0.2);
//        tableB.put("X=B,Z=C,Y=B", 0.5);
//        tableB.put("X=B,Z=C,Y=C", 0.3);
//        tableB.put("X=C,Z=C,Y=A", 0.1);
//        tableB.put("X=C,Z=C,Y=B", 0.4);
//        tableB.put("X=C,Z=C,Y=C", 0.5);
//
//        Factor factorA = new Factor(tableA, "Factor A (Z->X)");
//        Factor factorB = new Factor(tableB, "Factor B (X,Z->Y)");
//        // Join the two factors
//        Factor joinedFactor = joinFactors(factorA, factorB);
//        // Print the joint probability table before eliminating 'X'
//        System.out.println("Joint Factor Table before Eliminating X:");
//        joinedFactor.getProbabilityTable().forEach((key, value) -> System.out.println(key + " = " + value));
//
//        // Eliminate variable 'X' from the joined factor
//        joinedFactor.eliminateFactor("X");
//
//        // Print the resultant probability table after eliminating 'X'
//        System.out.println("Resultant Factor Table after Eliminating X:");
//        joinedFactor.getProbabilityTable().forEach((key, value) -> System.out.println(key + " = " + value));
//    }
}





//    public Factor(ArrayList<HashMap<String, String>> initialData, String[] conditions) {
//        System.out.println("Initializing factor with data size: " + initialData.size() + " and conditions: " + Arrays.toString(conditions));
//        this.tableEntries = new ArrayList<>();
//        this.relevantConditions = new ArrayList<>();
//
//        for (HashMap<String, String> entry : initialData) {
//            HashMap<String, String> clonedEntry = cloneHashMap(entry);
//            this.tableEntries.add(clonedEntry);
//            System.out.println("Cloned entry added: " + clonedEntry);
//        }
//        for (String condition : conditions) {
//            this.relevantConditions.add(condition);
//            System.out.println("Condition added: " + condition);
//        }
//        System.out.println("Final table entries after initialization: " + tableEntries);
//    }

//
//
//    public Factor(Map<List<String>, Double> newProbTable, String[] variableList) {
//        this();
//        // Assumes converting List<String> keys to HashMap<String, String>
//        for (Map.Entry<List<String>, Double> entry : newProbTable.entrySet()) {
//            HashMap<String, String> map = new HashMap<>();
//            for (String var : entry.getKey()) {
//                String[] splitVar = var.split("=");
//                map.put(splitVar[0], splitVar[1]);
//            }
//            this.tableEntries.add(map);
//        }
//        this.variables = new HashSet<>(Arrays.asList(variableList));
//    }
//
//    // Copy constructor
//    public Factor(Factor other) {
//        for (int i = 0; i < other.tableEntries.size(); i++) {
//            this.tableEntries.add(cloneHashMap(other.tableEntries.get(i)));
//        }
//        for (int i = 0; i < other.relevantConditions.size(); i++) {
//            this.relevantConditions.add(other.relevantConditions.get(i));
//        }
//    }
//
//    // Method to create a deep copy of a HashMap
//    private HashMap<String, String> cloneHashMap(HashMap<String, String> original) {
//        HashMap<String, String> copy = new HashMap<>();
//        for (Map.Entry<String, String> entry : original.entrySet()) {
//            copy.put(entry.getKey(), entry.getValue());
//        }
//        return copy;
//    }
//
//    public ArrayList<HashMap<String, String>> getTableEntries() {
//        return this.tableEntries;
//    }
//

//
//    public Map<List<String>, Double> getFactorTable() {
//        Map<List<String>, Double> result = new HashMap<>();
//        for (HashMap<String, String> entry : tableEntries) {
//            List<String> key = new ArrayList<>();
//            entry.forEach((k, v) -> key.add(k + "=" + v));
//            result.put(key, 1.0);  // Assuming a placeholder value
//        }
//        return result;
//    }
//
//    public void setFactorTable(Map<List<String>, Double> newProbTable) {
//        this.tableEntries.clear(); // Clear existing entries
//        for (Map.Entry<List<String>, Double> entry : newProbTable.entrySet()) {
//            HashMap<String, String> newEntry = new HashMap<>();
//            for (String varState : entry.getKey()) {
//                String[] parts = varState.split("=");
//                if (parts.length == 2) {
//                    newEntry.put(parts[0], parts[1]);
//                }
//            }
//            this.tableEntries.add(newEntry);
//        }
//    }
//
//    public Set<String> getVariables() {
//        return variables;
//    }
//
//    // Method to remove irrelevant rows from the table based on conditions
//    public void filterIrrelevantRows() {
//        ArrayList<String> conditionPairs = new ArrayList<>();
//        for (int i = 0; i < this.relevantConditions.size(); i++) {
//            String[] splitCondition = this.relevantConditions.get(i).split("=");
//            for (int j = 0; j < splitCondition.length; j++) {
//                conditionPairs.add(splitCondition[j]);
//            }
//        }
//
//        for (int i = 0; i < this.tableEntries.size(); i++) {
//            HashMap<String, String> row = this.tableEntries.get(i);
//            boolean matchesCondition = true;
//            for (int j = 0; j < conditionPairs.size() - 1; j += 2) {
//                String key = conditionPairs.get(j);
//                String value = conditionPairs.get(j + 1);
//                if (!row.containsKey(key) || !row.get(key).equals(value)) {
//                    matchesCondition = false;
//                    break;
//                }
//            }
//            if (!matchesCondition) {
//                this.tableEntries.remove(i);
//                i--;
//            }
//        }
//    }
//
//
//    // Comparison method for sorting or comparing two probability tables
//    @Override
//    public int compareTo(Factor other) {
//        int sizeComparison = Integer.compare(this.tableEntries.size(), other.tableEntries.size());
//        if (sizeComparison != 0) {
//            return sizeComparison;
//        }
//        return Integer.compare(this.calculateAsciiSum(), other.calculateAsciiSum());
//    }
//
//    @Override
//    public String toString() {
//        return "Table Entries=" + tableEntries + ", Conditions=" + relevantConditions;
//    }
//
//    // Method to remove conditions from entries after processing
//    public void cleanConditions() {
//        for (String cond : this.relevantConditions) {
//            String key = cond.split("=")[0];
//            Iterator<HashMap<String, String>> iterator = this.tableEntries.iterator();
//            while (iterator.hasNext()) {
//                HashMap<String, String> entry = iterator.next();
//                entry.remove(key);
//            }
//        }
//    }



