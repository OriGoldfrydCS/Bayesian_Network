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
            List<String> results = processQueries(network, queries);
            writeToOutputFile("output.txt", results);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> processQueries(BayesianNetwork network, List<String> queries) {
        List<String> results = new ArrayList<>();
        VariableElimination ve = new VariableElimination(network);
        BayesBall bb = new BayesBall(network);

        try (FileOutput fileOutput = new FileOutput("output.txt")) {
            for (String query : queries) {
                if (query.startsWith("P(")) {
                    String result = processVariableEliminationQuery(ve, query);
                    results.add(result);
                } else if (query.contains("-")) {
                    String result = processBayesBallQuery(bb, query);
                    results.add(result);
                } else {
                    System.out.println("Unhandled query format: " + query);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }

    private static String processBayesBallQuery(BayesBall bb, String query) {
        String[] parts = query.split("\\|");
        String[] nodes = parts[0].split("-");
        Map<String, String> evidenceMap = new HashMap<>();
        if (parts.length > 1) {
            String[] evidenceParts = parts[1].split(",");
            for (String evidence : evidenceParts) {
                String[] ev = evidence.split("=");
                evidenceMap.put(ev[0], ev[1]);
            }
        }
        boolean independent = bb.isIndependent(nodes[0], nodes[1], evidenceMap);
        return independent ? "yes" : "no";
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
        Object[] result = ve.query(evidenceMap, queryPart, '2');
        return String.format("%.5f,%d,%d", (double)result[0], (int)result[1], (int)result[2]);
    }

    private static List<String> readQueries(String filePath) {
        List<String> queries = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine();      // We will Skip the first line in the XML file (XML file name)
            String line;
            while ((line = br.readLine()) != null) {
                queries.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queries;
    }

    private static void writeToOutputFile(String filePath, List<String> results) {
        try (FileOutput fileOutput = new FileOutput(filePath)) {
            for (String result : results) {
                fileOutput.writeLine(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}