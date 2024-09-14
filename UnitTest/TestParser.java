import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestParser {
    public static List<TestCase> parseTestFile(String filePath) throws IOException {
        List<TestCase> testCases = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("##")) { // comment line
                    continue;
                }
                if (line.trim().isEmpty()) { // skip empty lines
                    continue;
                }
                String xmlFileName = line.trim();
                List<String> queries = new ArrayList<>();
                while ((line = reader.readLine()) != null && !line.equals("END_INPUT")) {
                    queries.add(line.trim());
                }
                List<String> expectedOutputs = new ArrayList<>();
                if (line != null && line.equals("END_INPUT")) {
                    while ((line = reader.readLine()) != null && !line.equals("END_OUTPUT")) {
                        expectedOutputs.add(line.trim());
                    }
                }
                testCases.add(new TestCase(xmlFileName, queries, expectedOutputs));
            }
        }
        return testCases;
    }
}

class TestCase {
    String xmlFileName;
    List<String> queries;
    List<String> expectedOutputs;

    public TestCase(String xmlFileName, List<String> queries, List<String> expectedOutputs) {
        this.xmlFileName = xmlFileName;
        this.queries = queries;
        this.expectedOutputs = expectedOutputs;
    }
}
