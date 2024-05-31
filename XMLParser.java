import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

/**
 * The `XMLParser` class is responsible for parsing an XML file representing a Bayesian network
 * and creating a `BayesianNetwork` object from the parsed data.
 */
public class XMLParser {

    /**
     * Parses the given XML file and creates a `BayesianNetwork` object.
     *
     * @param xmlFile the path to the XML file containing the Bayesian network data
     * @return a `BayesianNetwork` object representing the parsed network
     * @throws Exception if any errors occur during the parsing process
     */
    public static BayesianNetwork parse(String xmlFile) throws Exception {
        BayesianNetwork network = new BayesianNetwork();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(xmlFile));
        doc.getDocumentElement().normalize();

        // Parse the VARIABLE elements and add them to the network
        NodeList variableList = doc.getElementsByTagName("VARIABLE");
        for (int i = 0; i < variableList.getLength(); i++) {
            Element element = (Element) variableList.item(i);
            String name = element.getElementsByTagName("NAME").item(0).getTextContent();
            List<String> outcomes = new ArrayList<>();
            NodeList outcomeList = element.getElementsByTagName("OUTCOME");
            for (int j = 0; j < outcomeList.getLength(); j++) {
                outcomes.add(outcomeList.item(j).getTextContent());
            }
            Variable variable = new Variable(name, outcomes);
            network.addVariable(variable);
        }

        // Parse the DEFINITION elements and create or retrieve the corresponding nodes
        NodeList definitionList = doc.getElementsByTagName("DEFINITION");
        for (int i = 0; i < definitionList.getLength(); i++) {
            Element element = (Element) definitionList.item(i);
            String forVariable = element.getElementsByTagName("FOR").item(0).getTextContent();
            Node node = network.getNodeByName(forVariable);
            if (node == null) {
                node = new Node(forVariable);
                network.addNode(node);
            }

            // Parse the GIVEN elements and set the parents of the node
            NodeList givenList = element.getElementsByTagName("GIVEN");
            List<String> parents = new ArrayList<>();
            for (int j = 0; j < givenList.getLength(); j++) {
                String parentName = givenList.item(j).getTextContent();
                parents.add(parentName);
            }
            network.setParents(node, parents);

            // Parse the CPT table and set the CPT for the node
            String table = element.getElementsByTagName("TABLE").item(0).getTextContent();
            String[] probabilities = table.split(" ");
            node.buildCPT(probabilities);
        }

        System.out.println("Variables:");
        for (Node node : network.getNodes()) {
            System.out.println(node);
        }

        return network;
    }
}