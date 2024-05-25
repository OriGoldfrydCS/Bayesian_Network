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

        NodeList definitionList = doc.getElementsByTagName("DEFINITION");
        for (int i = 0; i < definitionList.getLength(); i++) {
            Element element = (Element) definitionList.item(i);
            String forVariable = element.getElementsByTagName("FOR").item(0).getTextContent();
            Variable variable = network.getVariable(forVariable);
            CPT cpt = new CPT(variable);

            NodeList givenList = element.getElementsByTagName("GIVEN");
            for (int j = 0; j < givenList.getLength(); j++) {
                Variable parent = network.getVariable(givenList.item(j).getTextContent());
                cpt.addParent(parent);
                parent.addChild(variable);
            }

            String table = element.getElementsByTagName("TABLE").item(0).getTextContent();
            String[] probabilities = table.split(" ");
            variable.setProbabilities(probabilities);

            network.addCPT(forVariable, cpt);
        }

        System.out.println("Variables:");
        for (Variable variable : network.getVariables()) {
            System.out.println(variable);
        }

        System.out.println("CPTs:");
        for (Variable variable : network.getVariables()) {
            CPT cpt = network.getCPT(variable.getName());
            System.out.println(cpt);
        }

        return network;
    }
}
