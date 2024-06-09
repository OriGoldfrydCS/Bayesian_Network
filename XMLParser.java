import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;

public class XMLParser {

    public static BayesianNetwork parse(String xmlFile) throws Exception {
        BayesianNetwork network = new BayesianNetwork();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(xmlFile));
        doc.getDocumentElement().normalize();

        // Parse the VARIABLE elements and add them to the network
        NodeList nodeList = doc.getElementsByTagName("VARIABLE");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            String name = null;
            NodeList nameList = element.getElementsByTagName("NAME");
            if (nameList.getLength() > 0) {
                name = nameList.item(0).getTextContent();
            }
            ArrayList<String> outcomes = new ArrayList<>();
            NodeList outcomeList = element.getElementsByTagName("OUTCOME");
            for (int j = 0; j < outcomeList.getLength(); j++) {
                outcomes.add(outcomeList.item(j).getTextContent());
            }
            Node node = new Node(name);
            node.addPossibleStates(outcomes);
            network.addNode(node);
        }

        // Parse the DEFINITION elements and create or retrieve the corresponding nodes
        NodeList definitionList = doc.getElementsByTagName("DEFINITION");
        int factorIndex = 1;
        for (int i = 0; i < definitionList.getLength(); i++) {
            Element element = (Element) definitionList.item(i);
            String forNode = element.getElementsByTagName("FOR").item(0).getTextContent();
            Node node = network.getNodeByName(forNode);
            if (node == null) {
                node = new Node(forNode);
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

            // Create and assign a Factor to the node
            Factor factor = node.createFactor();
            node.setFactor(factor);
        }

        // Print the initial factors
        System.out.println("\nInitial Factors:");
        for (Node node : network.getNodes()) {
            Factor factor = node.getFactor();
            if (factor != null) {
                System.out.println(factor);
            }
        }

        System.out.println("Nodes:");
        for (Node node : network.getNodes()) { // Modify this line
            System.out.println(node);
        }

        network.printNetwork();

        return network;
    }
}