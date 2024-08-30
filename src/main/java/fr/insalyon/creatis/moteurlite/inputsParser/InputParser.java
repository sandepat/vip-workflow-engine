package fr.insalyon.creatis.moteurlite.inputsParser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class InputParser {
        public static Map<String, String> parseResultDir(String filePath) {
            Map<String, String> resultMap = new HashMap<>();
            try {
                File xmlFile = new File(filePath);
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(xmlFile);
                doc.getDocumentElement().normalize();
    
                NodeList nodeList = doc.getElementsByTagName("source");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        String name = element.getAttribute("name");
                        if (name.equals("results-directory")) {
                            NodeList valueNodeList = element.getElementsByTagName("item");
                            String value = valueNodeList.item(0).getTextContent().trim();
                            resultMap.put(name, value);
                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultMap;
            
        }

public static List<Map<String, String>> parseInputData(String filePath) {
    List<Map<String, String>> inputList = new ArrayList<>();
    try {
        File xmlFile = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("source");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String name = element.getAttribute("name");
                if (!name.equals("results-directory")) {
                    NodeList itemNodeList = element.getElementsByTagName("item");
                    NodeList inputtype = element.getElementsByTagName("type");
                    for (int j = 0; j < itemNodeList.getLength(); j++) {
                        Node itemNode = itemNodeList.item(j);
                        if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element itemElement = (Element) itemNode;
                            String value = itemNode.getTextContent().trim();
                            Map<String, String> inputMap = new HashMap<>();
                            inputMap.put(name, value);
                            inputList.add(inputMap);
                        }
                    }
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return inputList;
}

public static Map<String, String> parseInputType(String fileName) {
    Map<String, String> nameTypeMap = new HashMap<>();
    try {
        File inputFile = new File(fileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();

        NodeList sourceList = doc.getElementsByTagName("source");
        for (int i = 0; i < sourceList.getLength(); i++) {
            Node sourceNode = sourceList.item(i);
            if (sourceNode.getNodeType() == Node.ELEMENT_NODE) {
                Element sourceElement = (Element) sourceNode;
                String name = sourceElement.getAttribute("name");
                if (!name.equals("results-directory")) {
                    String type = sourceElement.getAttribute("type");
                    nameTypeMap.put(name, type);
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return nameTypeMap;
}

        
}
