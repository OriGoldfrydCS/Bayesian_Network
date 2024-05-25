import java.util.*;

public class Factor {
    private List<Variable> variables;
    private Map<List<String>, Double> probabilityTable;

    public Factor(Variable var, Map<String, String> evidence) {
        variables = new ArrayList<>();
        probabilityTable = new HashMap<>();
        if (!evidence.containsKey(var.getName())) {
            variables.add(var);
            for (String outcome : var.getOutcomes()) {
                List<String> key = new ArrayList<>();
                key.add(outcome);
                probabilityTable.put(key, var.getProbability(outcome));
                System.out.println("Adding probability for " + var.getName() + " = " + outcome + ": " + var.getProbability(outcome));
            }
        }
    }

    public Factor() {
        variables = new ArrayList<>();
        probabilityTable = new HashMap<>();
    }

    public Factor(Factor other) {
        // Implement
    }

    public Factor(List<Variable> newVars) {
        variables = newVars;
        probabilityTable = new HashMap<>();
    }

    public boolean contains(String varName) {
        for (Variable v : variables) {
            if (v.getName().equals(varName)) {
                return true;
            }
        }
        return false;
    }

    public Factor multiply(Factor other) {
        List<Variable> newVars = new ArrayList<>(variables);
        for (Variable var : other.variables) {
            if (!newVars.contains(var)) {
                newVars.add(var);
            }
        }
        Factor result = new Factor(newVars);

        for (List<String> assignment1 : probabilityTable.keySet()) {
            for (List<String> assignment2 : other.probabilityTable.keySet()) {
                if (compatible(assignment1, assignment2, other)) {
                    List<String> newAssignment = new ArrayList<>();
                    for (Variable var : newVars) {
                        int index1 = variables.indexOf(var);
                        int index2 = other.variables.indexOf(var);
                        if (index1 >= 0) {
                            newAssignment.add(assignment1.get(index1));
                        } else {
                            newAssignment.add(assignment2.get(index2));
                        }
                    }
                    double newProb = probabilityTable.get(assignment1) * other.probabilityTable.get(assignment2);
                    result.probabilityTable.put(newAssignment, newProb);
                    System.out.println("Multiplying factors, new assignment: " + newAssignment + " new probability: " + newProb);
                }
            }
        }
        return result;
    }

    public Factor sumOut(String varName) {
        Factor result = new Factor();
        int varIndex = -1;

        for (int i = 0; i < variables.size(); i++) {
            if (variables.get(i).getName().equals(varName)) {
                varIndex = i;
                break;
            }
        }

        if (varIndex == -1) {
            return this;
        }

        for (List<String> assignment : probabilityTable.keySet()) {
            List<String> newAssignment = new ArrayList<>(assignment);
            newAssignment.remove(varIndex);
            double newProb = probabilityTable.get(assignment);
            result.probabilityTable.merge(newAssignment, newProb, Double::sum);
            System.out.println("Summing out " + varName + ", new assignment: " + newAssignment + " new probability: " + newProb);
        }

        result.variables = new ArrayList<>(variables);
        result.variables.remove(varIndex);

        return result;
    }

    public double getProbability(String query) {
        List<String> key = Arrays.asList(query.split(","));
        return probabilityTable.getOrDefault(key, 0.0);
    }

    private boolean compatible(List<String> assignment1, List<String> assignment2, Factor other) {
        for (int i = 0; i < variables.size(); i++) {
            Variable var = variables.get(i);
            int index = other.variables.indexOf(var);
            if (index != -1 && !assignment1.get(i).equals(assignment2.get(index))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Factor{" +
                "variables=" + variables +
                ", probabilityTable=" + probabilityTable +
                '}';
    }

    public double getValue() {
        //IMPLEMENT
        return 0;
    }

    public void setValue(double value) {
        //IMPLEMENT
    }
}
