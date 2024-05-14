import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Ex1 {
    public static void main(String[] args) {
        try {
            BayesianNetwork network = XMLParser.parseXML("alarm_net.xml");
            List<String> queries = readQueries("input.txt");
            List<String> results = processQueries(network, queries);
            writeToOutputFile("output.txt", results);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> processQueries(BayesianNetwork network, List<String> queries) {
        List<String> results = new ArrayList<>();
        for (String query : queries) {
            if (query.contains("|") && !query.startsWith("P")) { // Ensuring it's a Bayes Ball query
                results.add(processBayesBallQuery(network, query));
            } else if (query.startsWith("P")) { // For Variable Elimination queries
                results.add(processVariableEliminationQuery(network, query));
            } else {
                System.out.println("Unhandled query format: " + query);
            }
        }
        return results;
    }


    private static String processBayesBallQuery(BayesianNetwork network, String query) {
        String[] parts = query.split("\\|");
        String[] nodes = parts[0].split("-");

        // Check that there are at least two nodes specified in the query
        if (nodes.length < 2) {
            throw new IllegalArgumentException("Query must include two nodes separated by '-': " + query);
        }

        Set<String> evidence = new HashSet<>();
        if (parts.length > 1 && !parts[1].trim().isEmpty()) {
            // Process the evidence part only if it exists and is not empty
            evidence = getEvidence(parts);
        }

        boolean independent = network.getBayesBall().query(nodes[0], nodes[1], evidence);
        return independent ? "yes" : "no";
    }



    private static String processVariableEliminationQuery(BayesianNetwork network, String query) {
        QueryResult queryResult = parseVariableEliminationQuery(query);
        double result = network.getVariableElimination().computeProbability(
                queryResult.hiddenVariables, queryResult.evidence, queryResult.queryVariable);
        return String.format("%.5f,%d,%d", result, network.getVariableElimination().getAdditionOperations(),
                network.getVariableElimination().getMultiplicationOperations());
    }

    private static List<String> readQueries(String filePath) {
        List<String> queries = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip the first line (XML file name)
            String line;
            while ((line = br.readLine()) != null) {
                queries.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queries;
    }

    private static Set<String> getEvidence(String[] parts) {
        Set<String> evidence = new HashSet<>();
        if (parts.length > 1 && !parts[1].trim().isEmpty()) {
            for (String ev : parts[1].split(",")) {
                String[] evSplit = ev.trim().split("=");
                if (evSplit.length > 1) {
                    evidence.add(evSplit[0]);
                }
            }
        }
        return evidence;
    }


    private static QueryResult parseVariableEliminationQuery(String query) {
        String[] parts = query.split("\\) ");
        String probPart = parts[0].substring(2);
        String[] probParts = probPart.split("\\|");
        String queryVariable = probParts[0].substring(0, probParts[0].indexOf("="));
        Map<String, String> evidence = new HashMap<>();
        if (probParts.length > 1) {
            for (String ev : probParts[1].split(",")) {
                String[] evSplit = ev.trim().split("=");
                evidence.put(evSplit[0], evSplit[1]);
            }
        }
        List<String> hiddenVariables = Arrays.asList(parts[1].trim().split("-"));
        return new QueryResult(queryVariable, evidence, hiddenVariables);
    }

    private static void writeToOutputFile(String filePath, List<String> results) {
        try {
            Files.write(Paths.get(filePath), results, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class QueryResult {
        String queryVariable;
        Map<String, String> evidence;
        List<String> hiddenVariables;

        QueryResult(String queryVariable, Map<String, String> evidence, List<String> hiddenVariables) {
            this.queryVariable = queryVariable;
            this.evidence = evidence;
            this.hiddenVariables = hiddenVariables;
        }
    }
}
