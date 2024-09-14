import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


/**
 * The Ex1 class is the entry point for processing queries on a Bayesian Network.
 * It reads the network structure from an XML file and processes a list of queries specified in an input file.
 */
public class Main {
    public static void main(String[] args) {

        // Default input file path
        String inputFilePath = "input.txt";

        if (args.length > 0) {
            inputFilePath = args[0];
        }
        BayesianNetwork network = null;
        try {
            // Read the XML file name from the input file and parse the Bayesian Network
            String xmlFileName = readXMLFileName(inputFilePath);
            network = XMLParser.parse(xmlFileName);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        // Read queries from the input file
        List<String> queries = readQueries(inputFilePath);

        // Process the queries and get the results
        List<String> results = processQueries(network, queries);

        // Write the results to the output file
        writeToOutputFile(results);
    }

    /**
     * Reads the name of the XML file from the first line of the input file.
     * @param file The input file containing the XML file name and queries.
     * @return     The name of the XML file.
     * @throws IOException If an I/O error occurs.
     */
    private static String readXMLFileName(String file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            return br.readLine(); // Read the first line to get the XML file name
        }
    }

    /**
     * Reads queries from the input file, skipping the first line which contains the XML file name.
     * @param file The input file containing the queries.
     * @return     A list of queries read from the file.
     */
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

    /**
     * Processes the list of queries on the given Bayesian Network.
     *
     * @param network The Bayesian Network on which to process the queries.
     * @param queries The list of queries to process.
     * @return        A list of results for each query.
     */
    private static List<String> processQueries(BayesianNetwork network, List<String> queries) {
        List<String> results = new ArrayList<>();
        BayesianNetwork cloneNetwork;
        for (String query : queries) {
            if (query.startsWith("P(")) {
                cloneNetwork = network.clone();
                String result = processVariableEliminationQuery(cloneNetwork, query);
                results.add(result);
            } else if (!(query.startsWith("P("))) {
                cloneNetwork = network.clone();
                String result = processIndependenceQuery(cloneNetwork, query);
                results.add(result);
            } else {
                System.out.printf("Unhandled query format: %s%n", query);
            }
        }
        return results;
    }

    /**
     * Processes an independence query on the Bayesian Network.
     * @param network The Bayesian Network on which to process the query.
     * @param query   The query string representing the independence query.
     * @return        The result of the independence query ("yes" or "no").
     */
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

        // Call to BayesBall algo
        String result = BayesBall.checkIndependence(network, network.getNodeByName(nodes[0].trim()), network.getNodeByName(nodes[1].trim()), evidenceList);
        return result;
    }

    /**
     * Processes a variable elimination query on the Bayesian Network.
     *
     * @param network The Bayesian Network on which to process the query.
     * @param query   The query string representing the variable elimination query.
     * @return        The result of the variable elimination query.
     */
    private static String processVariableEliminationQuery(BayesianNetwork network, String query) {
        System.out.println("Received query: " + query);

        // Split the query by space to separate parts
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

                VariableElimination ve = new VariableElimination(network, queryNode, hiddenVariables, evidence);
                return ve.getFinalAnswer();
            } else if (parts.length == 2) {
                // Evidence is provided
                String queryNode = parts[0];
                String[] evidence = parts[1].split(",");
                String[] hiddenVariables = new String[0]; // No hidden variables

                VariableElimination ve = new VariableElimination(network, queryNode, hiddenVariables, evidence);
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

            VariableElimination ve = new VariableElimination(network, queryNode, hiddenVariables, evidence);
            return ve.getFinalAnswer();
        } else if (parts.length == 2) {
            // Evidence is provided
            String queryNode = parts[0];
            String[] evidence = parts[1].split(",");
            String[] hiddenVariables = eliminationOrderStr.split("-");

            VariableElimination ve = new VariableElimination(network, queryNode, hiddenVariables, evidence);
            return ve.getFinalAnswer();
        } else {
            return "Error: Probability query part is missing evidence.";
        }
    }


    /**
     * Writes the results of the queries to an output file.
     * @param results The list of results to write to the file.
     */
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