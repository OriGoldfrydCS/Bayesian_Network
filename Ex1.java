import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Ex1 {
    public static void main(String[] args) {
        try {
            BayesianNetwork network = XMLParser.parse("alarm_net.xml");
            List<String> queries = readQueries("input.txt");
//            BayesianNetwork network = XMLParser.parse("big_net.xml");
//            List<String> queries = readQueries("input2.txt");
//            BayesianNetwork network = XMLParser.parse("my_net.xml");
//            List<String> queries = readQueries("input3.txt");
            List<String> results = processQueries(network, queries);
            writeToOutputFile(results);
        } catch (Exception e) {
            System.err.printf("Error reading XML %s\n", e.getMessage());
        }
    }

    private static List<String> readQueries(String file) {
        List<String> queries = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();      // We will Skip the first line in the XML file (XML file name)
            String line;
            while ((line = br.readLine()) != null) {
                queries.add(line);
            }
        } catch (IOException e) {
            System.err.printf("Error reading file: %s\n", e.getMessage());
        }
        return queries;
    }

    private static List<String> processQueries(BayesianNetwork network, List<String> queries) {
        List<String> results = new ArrayList<>();
//        VariableElimination ve = new VariableElimination(network);

        for (String query : queries) {
            if (query.startsWith("P(")) {
//                String result = processVariableEliminationQuery(ve, query);
                String result = processVariableEliminationQuery(network, query);
                results.add(result);
            } else if (!(query.startsWith("P("))) {
                String result = processIndependenceQuery(network, query);
                results.add(result);
            } else {
                System.out.printf("Unhandled query format: %s%n", query);
            }
        }
        return results;
    }

    private static String processIndependenceQuery(BayesianNetwork network, String query) {
        String[] parts = query.split("\\|");
        String[] nodes = parts[0].split("-");
        ArrayList<Node> evidenceList = new ArrayList<>();
//        System.out.printf("Processing query: %s%n", query);

        if (parts.length > 1) {
            String[] evidenceParts = parts[1].split(",");
//            System.out.printf("Evidence provided: %s%n", String.join(", ", evidenceParts));

            for (String evidence : evidenceParts) {
                String ev = evidence.trim();
                // Split the evidence to extract the node name before '='
                String nodeName = ev.contains("=") ? ev.split("=")[0] : ev;
                Node evidenceNode = network.getNodeByName(nodeName);
                if (evidenceNode != null) {
                    evidenceList.add(evidenceNode);
//                    System.out.printf("Added %s to evidence list.%n", nodeName);
                } else {
                    System.out.printf("Error: Node %s not found in network.%n", nodeName);
                }
            }
        }
//        else {
//            System.out.println("No evidence provided for this query.");
//        }

        String result = BayesBall.checkIndependence(network, network.getNodeByName(nodes[0].trim()), network.getNodeByName(nodes[1].trim()), evidenceList);
//        System.out.printf("Result of independence check between %s and %s: %s%n", nodes[0].trim(), nodes[1].trim(), result);
        return result;
    }

    private static String processVariableEliminationQuery(BayesianNetwork network, String query) {
        System.out.println("Received query: " + query);

        // Split the query by space to separate parts
        String[] splitQuery = query.split(" ");
        if (splitQuery.length < 2) {
            System.out.println("Error: Query format is incorrect.");
            return "Error: Query format is incorrect.";
        }

        // Handling the probability and elimination parts
        String probabilityPart = splitQuery[0].replace("P(", "").replace(")", "");
        String eliminationOrderStr = splitQuery[1];

        System.out.println("Formatted probability part: " + probabilityPart);
        System.out.println("Elimination order from query: " + eliminationOrderStr);

        // Further split the probability part into query and evidence
        String[] parts = probabilityPart.split("\\|");
        if (parts.length < 2) {
            System.out.println("Error: Probability query part is missing evidence.");
            return "Error: Probability query part is missing evidence.";
        }

        String queryNode = parts[0];
        String[] evidence = parts[1].split(",");

        System.out.println("Query node extracted: " + queryNode);
        System.out.println("Evidence extracted: " + Arrays.toString(evidence));

        // Split the elimination order string
        String[] hiddenVariables = eliminationOrderStr.split("-");

        System.out.println("Hidden variables extracted: " + Arrays.toString(hiddenVariables));

        // Create a new instance of VariableElimination
        VariableElimination ve = new VariableElimination(network, queryNode, hiddenVariables, evidence);
//        System.out.println("VariableElimination instance created with query node, hidden variables, and evidence.");
        return ve.getFinalAnswer();
//        return "0";
        // Assuming ve.query() method returns results that are processed below
        // This line would actually run the variable elimination process
        // Let's assume the query method returns an array of objects with results
//        try {
//            Object[] result = ve.query( evidence,  queryNode, hiddenVariables);  // Replace with the actual method call
//            String formattedResult = String.format("%.5f,%d,%d", (double) result[0], (int) result[1], (int) result[2]);
//            System.out.println("Query Result: " + formattedResult);
//            return formattedResult;
//        } catch (Exception e) {
//            System.out.println("Error processing variable elimination query: " + e.getMessage());
//            return "Error processing query: " + e.getMessage();
//        }
//        return "0";
    }


//    private static String processVariableEliminationQuery(VariableElimination ve, String query) {
//        System.out.println("Received query: " + query);
//
//        // Split the query to separate the probability expression from the elimination order
//        String[] parts = query.split("\\) ");
//        String probabilityQuery = parts[0] + ")";  // Includes the closing parenthesis removed by split
//        String eliminationOrderStr = parts.length > 1 ? parts[1] : "";
//
//        System.out.println("Probability Query: " + probabilityQuery);
//        System.out.println("Elimination Order: " + eliminationOrderStr);
//
//        // Process the probability query to extract query part and evidence
//        String[] probabilityParts = probabilityQuery.split("\\|");
//        String queryPart = probabilityParts[0].substring(2, probabilityParts[0].length() - 2);  // Remove "P(" and the last ")"
//        String[] evidenceParts = probabilityParts.length > 1 ? probabilityParts[1].substring(0, probabilityParts[1].length() - 1).split(",") : new String[0];  // Remove the closing ")"
//
//        Map<String, String> evidenceMap = new HashMap<>();
//        for (String evidence : evidenceParts) {
//            String[] ev = evidence.split("=");
//            if (ev.length == 2) {
//                evidenceMap.put(ev[0].trim(), ev[1].trim());
//                System.out.println("Evidence mapped: " + ev[0].trim() + "=" + ev[1].trim());
//            } else {
//                System.out.println("Error parsing evidence: " + evidence);
//            }
//        }
//
//        System.out.println("Query Node: " + queryPart);
//
//        // Process the elimination order
//        String[] eliminationOrder = eliminationOrderStr.isEmpty() ? new String[0] : eliminationOrderStr.split("-");
//        System.out.println("Elimination Order Array: " + Arrays.toString(eliminationOrder));
//
//        // Execute the query using VariableElimination
//        try {
//            Object[] result = ve.query(evidenceMap, queryPart, eliminationOrder);
//            String formattedResult = String.format("%.5f,%d,%d", (double)result[0], (int)result[1], (int)result[2]);
//            System.out.println("Query Result: " + formattedResult);
//            return formattedResult;
//        } catch (Exception e) {
//            System.out.println("Error processing variable elimination query: " + e.getMessage());
//            return "Error processing query: " + e.getMessage();
//        }
//    }



    private static void writeToOutputFile(List<String> results) {
        try (FileOutput fileOutput = new FileOutput("output.txt")) {
            for (String result : results) {
                fileOutput.writeLine(result);
            }
        } catch (IOException e) {
            System.err.printf("Error writing to output file: %s%n", e);
        }
    }
}