import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLParser {
    public static BayesianNetwork parseXML(String filePath) throws ParserConfigurationException, IOException, SAXException {
        File xmlFile = new File(filePath);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlFile);
        document.getDocumentElement().normalize();

        BayesianNetwork network = new BayesianNetwork();
        parseVariables(document.getElementsByTagName("VARIABLE"), network);
        parseDefinitions(document.getElementsByTagName("DEFINITION"), network);
        return network;
    }

    private static void parseVariables(NodeList variableList, BayesianNetwork network) {
        for (int i = 0; i < variableList.getLength(); i++) {
            Node node = variableList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String varName = element.getElementsByTagName("NAME").item(0).getTextContent();
                List<String> outcomes = new ArrayList<>();
                NodeList outcomeNodes = element.getElementsByTagName("OUTCOME");
                for (int j = 0; j < outcomeNodes.getLength(); j++) {
                    outcomes.add(outcomeNodes.item(j).getTextContent());
                }
                Variable var = new Variable(varName, outcomes);
                network.addVariable(var);
            }
        }
    }

    private static void parseDefinitions(NodeList definitionList, BayesianNetwork network) {
        for (int i = 0; i < definitionList.getLength(); i++) {
            Node node = definitionList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String varName = element.getElementsByTagName("FOR").item(0).getTextContent();
                NodeList givenNodes = element.getElementsByTagName("GIVEN");
                List<String> parents = new ArrayList<>();
                for (int j = 0; j < givenNodes.getLength(); j++) {
                    parents.add(givenNodes.item(j).getTextContent());
                }
                String[] probabilities = element.getElementsByTagName("TABLE").item(0).getTextContent().trim().split("\\s+");

                // Debug output for CPT structure
                System.out.println("CPT for Variable: " + varName);
                System.out.println("Parents: " + parents);
                System.out.println("Probabilities: ");
                for (String probability : probabilities) {
                    System.out.print(probability + " ");
                }
                System.out.println();  // Add a newline for better separation in output

                network.addCPT(varName, parents, probabilities);
            }
        }
    }
}