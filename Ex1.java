import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        VariableElimination ve = new VariableElimination(network);

        for (String query : queries) {
            if (query.startsWith("P(")) {
                String result = processVariableEliminationQuery(ve, query);
                results.add(result);
            } else if (!(query.startsWith("P"))) {
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
        System.out.printf("Processing query: %s%n", query);

        if (parts.length > 1) {
            String[] evidenceParts = parts[1].split(",");
            System.out.printf("Evidence provided: %s%n", String.join(", ", evidenceParts));

            for (String evidence : evidenceParts) {
                String ev = evidence.trim();
                // Split the evidence to extract the node name before '='
                String nodeName = ev.contains("=") ? ev.split("=")[0] : ev;
                Node evidenceNode = network.getNodeByName(nodeName);
                if (evidenceNode != null) {
                    evidenceList.add(evidenceNode);
                    System.out.printf("Added %s to evidence list.%n", nodeName);
                } else {
                    System.out.printf("Error: Node %s not found in network.%n", nodeName);
                }
            }
        } else {
            System.out.println("No evidence provided for this query.");
        }

        String result = BayesBall.checkIndependence(network, network.getNodeByName(nodes[0].trim()), network.getNodeByName(nodes[1].trim()), evidenceList);
        System.out.printf("Result of independence check between %s and %s: %s%n", nodes[0].trim(), nodes[1].trim(), result);
        return result;
    }

    private static String processVariableEliminationQuery(VariableElimination ve, String query) {
        String[] parts = query.split("\\|");
        String queryPart = parts[0].substring(2, parts[0].length() - 1);
        String[] evidenceParts = parts[1].split(",");
        Map<String, String> evidenceMap = new HashMap<>();
        for (String evidence : evidenceParts) {
            String[] ev = evidence.split("=");
            evidenceMap.put(ev[0], ev[1]);
        }
//        Object[] result = ve.query(evidenceMap, queryPart, '2');
//        return String.format("%.5f,%d,%d", (double)result[0], (int)result[1], (int)result[2]);
        return "0";
    }

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