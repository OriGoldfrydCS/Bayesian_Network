import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Ex1 {

    public static void main(String[] args) {
        String inputFilePath = "input.txt"; // Relative path from src to project directory

        if (args.length > 0) {
            inputFilePath = args[0]; // Use provided filename if available
        }

        try {
            // Rest of your code remains the same
            String xmlFileName = readXMLFileName(inputFilePath);
//            BayesianNetwork network = XMLParser.parse("../" + xmlFileName); // Adjust the path based on your project structure
            BayesianNetwork network = XMLParser.parse(xmlFileName); // Adjust the path based on your project structure

            List<String> queries = readQueries(inputFilePath);
            List<String> results = processQueries(network, queries);
            writeToOutputFile(results);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static String readXMLFileName(String file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            return br.readLine(); // Read the first line to get the XML file name
        }
    }

    private static List<String> readQueries(String file) {
        List<String> queries = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // Skip the first line as it contains the XML file name
            String line;
            while ((line = br.readLine()) != null) {
                queries.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return queries;
    }



    private static List<String> processQueries(BayesianNetwork network, List<String> queries) {
        List<String> results = new ArrayList<>();

        for (String query : queries) {
            if (query.startsWith("P(")) {
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

        if (parts.length > 1) {
            String[] evidenceParts = parts[1].split(",");

            for (String evidence : evidenceParts) {
                String ev = evidence.trim();
                // Split the evidence to extract the node name before '='
                String nodeName = ev.contains("=") ? ev.split("=")[0] : ev;
                Node evidenceNode = network.getNodeByName(nodeName);
                if (evidenceNode != null) {
                    evidenceList.add(evidenceNode);
                } else {
                    System.out.printf("Error: Node %s not found in network.%n", nodeName);
                }
            }
        }

        String result = BayesBall.checkIndependence(network, network.getNodeByName(nodes[0].trim()), network.getNodeByName(nodes[1].trim()), evidenceList);
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
        return ve.getFinalAnswer();
    }


    private static void writeToOutputFile(List<String> results) {
//        try (FileOutput fileOutput = new FileOutput("../output.txt")) {
            try (FileOutput fileOutput = new FileOutput("output.txt")) {
            for (String result : results) {
                fileOutput.writeLine(result);
            }
        } catch (IOException e) {
            System.err.printf("Error writing to output file: %s%n", e);
        }
    }
}