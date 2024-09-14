import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;


/**
 * The XMLParser class provides methods to parse an XML file representing a Bayesian Network.
 * It creates a BayesianNetwork object by reading VARIABLE and DEFINITION elements from the XML file.
 */
public class XMLParser {

    /**
     * Parses an XML file to create a Bayesian Network.
     *
     * @param xmlFile The path to the XML file to be parsed.
     * @return A BayesianNetwork object representing the parsed network.
     * @throws Exception If an error occurs during XML parsing.
     */
    public static BayesianNetwork parse(String xmlFile) throws Exception {

        // Set up the document builder for parsing the XML file
        BayesianNetwork network = new BayesianNetwork();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(xmlFile));
        doc.getDocumentElement().normalize();

        // Parse the VARIABLE elements and add them to the network
        NodeList nodeList = doc.getElementsByTagName("VARIABLE");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            String name = element.getElementsByTagName("NAME").item(0).getTextContent();
            List<String> outcomes = new ArrayList<>();
            NodeList outcomeList = element.getElementsByTagName("OUTCOME");
            for (int j = 0; j < outcomeList.getLength(); j++) {
                outcomes.add(outcomeList.item(j).getTextContent());
            }
            Node node = new Node(name);
            node.addPossibleStates(new ArrayList<>(outcomes));
            network.addNode(node);
        }

        // Parse the DEFINITION elements
        NodeList definitionList = doc.getElementsByTagName("DEFINITION");
        for (int i = 0; i < definitionList.getLength(); i++) {
            Element element = (Element) definitionList.item(i);
            String forNodeName = element.getElementsByTagName("FOR").item(0).getTextContent();
            Node forNode = network.getNodeByName(forNodeName);

            // Set parents of the node and automatically add this node as their child
            NodeList givenList = element.getElementsByTagName("GIVEN");
            for (int j = 0; j < givenList.getLength(); j++) {
                String parentName = givenList.item(j).getTextContent();
                Node parentNode = network.getNodeByName(parentName);
                if (parentNode != null) {
                    forNode.addParent(parentNode);
                    parentNode.addChild(forNode);
                }
            }

            // Set the CPT for the node
            String[] probabilities = element.getElementsByTagName("TABLE").item(0).getTextContent().trim().split("\\s+");
            forNode.buildCPT(probabilities);
            forNode.setFactor(forNode.createFactor());
        }

        return network;
    }
}
