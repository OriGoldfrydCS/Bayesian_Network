import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestRunner {
    public static void main(String[] args) {
        int passedCount = 0;
        int notPassedCount = 0;
        List<Integer> passedInputs = new ArrayList<>();
        List<Integer> notPassedInputs = new ArrayList<>();

        try {
            List<TestCase> testCases = TestParser.parseTestFile("Tests.io");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("output_test.txt"))) {
                int testNumber = 1;

                for (TestCase testCase : testCases) {
                    writer.write("# Input " + testNumber + "\n");
                    writer.write("XML name: " + testCase.xmlFileName + "\n");

                    // Load the Bayesian network from XML
                    BayesianNetwork bn = XMLParser.parse(testCase.xmlFileName);

                    // Execute each query and collect results
                    List<String> actualOutputs = new ArrayList<>();
                    for (String query : testCase.queries) {
                        // Process the query (e.g., variable elimination, independence checks)
                        String result = processQuery(bn, query);
                        actualOutputs.add(result);
                    }

                    // Compare actual outputs with expected outputs
                    boolean passed = compareOutputs(actualOutputs, testCase.expectedOutputs, writer);
                    if (passed) {
                        writer.write("Result: Test Passed.\n");
                        passedCount++;
                        passedInputs.add(testNumber);
                    } else {
                        writer.write("Result: Test Failed.\n");
                        notPassedCount++;
                        notPassedInputs.add(testNumber);
                    }
                    writer.write("\n");
                    testNumber++;
                }

                writer.write("FINAL STATISTICS\n");
                writer.write("Passed by input numbers: " + passedInputs + "\n");
                writer.write("Not passed: " + notPassedCount + "\n");
                writer.write("Not passed input numbers: " + notPassedInputs + "\n");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String processQuery(BayesianNetwork bn, String query) {
        if (query.startsWith("P(")) {
            return processVariableEliminationQuery(bn, query);
        } else {
            return processIndependenceQuery(bn, query);
        }
    }

    private static boolean compareOutputs(List<String> actual, List<String> expected, BufferedWriter writer) throws IOException {
        if (actual.size() != expected.size()) {
            return false;
        }
        boolean pass = true;

        for (int i = 0; i < actual.size(); i++) {
            String actualOutput = actual.get(i).replaceAll("[\\[\\]]", "").trim();
            String expectedOutput = expected.get(i).replaceAll("[\\[\\]]", "").trim();

            if (expectedOutput.equalsIgnoreCase("yes") || expectedOutput.equalsIgnoreCase("no")) {
                // Handle yes/no output comparison
                if (!actualOutput.equalsIgnoreCase(expectedOutput)) {
                    pass = false;
                    writer.write("Expected: " + expectedOutput + "\n");
                    writer.write("Actual: " + actualOutput + "\n");
                }
            } else {
                // Handle numeric output comparison
                try {
                    String[] actualParts = actualOutput.split(",");
                    String[] expectedParts = expectedOutput.split(",");

                    double actualProbability = Double.parseDouble(actualParts[0].trim());
                    int actualAdditions = Integer.parseInt(actualParts[1].trim());
                    int actualMultiplications = Integer.parseInt(actualParts[2].trim());

                    double expectedProbability = Double.parseDouble(expectedParts[0].trim());
                    int expectedAdditions = Integer.parseInt(expectedParts[1].trim());
                    int expectedMultiplications = Integer.parseInt(expectedParts[2].trim());

                    boolean localPass = true;

                    if (Math.abs(actualProbability - expectedProbability) > 1e-5) {
                        localPass = false;
                    }

                    int additionGap = Math.abs(actualAdditions - expectedAdditions);
                    int multiplicationGap = Math.abs(actualMultiplications - expectedMultiplications);

                    if (additionGap >= 10 || multiplicationGap >= 10) {
                        localPass = false;
                    }

                    if (!localPass) {
                        writer.write("Fail\n");
                        writer.write("Expected: " + expectedOutput + "\n");
                        writer.write("Actual: " + actualOutput + "\n");
                        if (additionGap >= 10) {
                            writer.write("Addition operation gap: " + additionGap + "\n");
                        }
                        if (multiplicationGap >= 10) {
                            writer.write("Multiplication operation gap: " + multiplicationGap + "\n");
                        }
                    }

                    pass = pass && localPass;
                } catch (NumberFormatException e) {
                    pass = false;
                    writer.write("Fail\n");
                    writer.write("Expected: " + expectedOutput + "\n");
                    writer.write("Actual: " + actualOutput + "\n");
                    writer.write("Error: " + e.getMessage() + "\n");
                }
            }
        }
        return pass;
    }

    private static String processVariableEliminationQuery(BayesianNetwork bn, String query) {
        System.out.println("Received query: " + query);

        // Split the query by space to separate probability part and elimination order
        String[] splitQuery = query.split(" ");

        // If no elimination order is provided (only the probability part)
        if (splitQuery.length < 2) {
            String probabilityPart = splitQuery[0].replace("P(", "").replace(")", "");
            String[] parts = probabilityPart.split("\\|");

            if (parts.length == 1) {
                // No evidence provided
                String queryNode = parts[0];
                String[] evidence = new String[0];
                String[] hiddenVariables = new String[0]; // No hidden variables

                VariableElimination ve = new VariableElimination(bn, queryNode, hiddenVariables, evidence);
                return ve.getFinalAnswer();
            } else if (parts.length == 2) {
                // Evidence is provided
                String queryNode = parts[0];
                String[] evidence = parts[1].split(",");
                String[] hiddenVariables = new String[0]; // No hidden variables

                VariableElimination ve = new VariableElimination(bn, queryNode, hiddenVariables, evidence);
                return ve.getFinalAnswer();
            } else {
                return "Error: Probability query part is missing evidence.";
            }
        }

        // Original logic for when elimination order is provided
        String probabilityPart = splitQuery[0].replace("P(", "").replace(")", "");
        String eliminationOrderStr = splitQuery[1];

        String[] parts = probabilityPart.split("\\|");
        if (parts.length == 1) {
            // No evidence provided
            String queryNode = parts[0];
            String[] evidence = new String[0];
            String[] hiddenVariables = eliminationOrderStr.split("-");

            VariableElimination ve = new VariableElimination(bn, queryNode, hiddenVariables, evidence);
            return ve.getFinalAnswer();
        } else if (parts.length == 2) {
            // Evidence is provided
            String queryNode = parts[0];
            String[] evidence = parts[1].split(",");
            String[] hiddenVariables = eliminationOrderStr.split("-");

            VariableElimination ve = new VariableElimination(bn, queryNode, hiddenVariables, evidence);
            return ve.getFinalAnswer();
        } else {
            return "Error: Probability query part is missing evidence.";
        }
    }


    private static String processIndependenceQuery(BayesianNetwork bn, String query) {
        String[] parts = query.split("\\|");
        String[] nodes = parts[0].split("-");
        ArrayList<Node> evidenceList = new ArrayList<>();

        if (parts.length > 1) {
            String[] evidenceParts = parts[1].split(",");
            for (String evidence : evidenceParts) {
                String ev = evidence.trim();
                String nodeName = ev.contains("=") ? ev.split("=")[0] : ev;
                Node evidenceNode = bn.getNodeByName(nodeName);
                if (evidenceNode != null) {
                    evidenceList.add(evidenceNode);
                } else {
                    System.out.printf("Error: Node %s not found in network.%n", nodeName);
                }
            }
        }

        return BayesBall.checkIndependence(bn, bn.getNodeByName(nodes[0].trim()), bn.getNodeByName(nodes[1].trim()), evidenceList);
    }
}
