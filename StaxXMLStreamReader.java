import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.print.attribute.standard.NumberOfDocuments;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class StaxXMLStreamReader {

    private static boolean bParent;
    private static boolean bBranch;
    private static boolean bComment;
//    public static void main(String[] args) {
//        String fileName = "employee.xml";
//        DAG<Node> dag = parseXML(fileName);
//        List<Employee> empList = parseXML(fileName);
//        for(Employee emp : empList){
//            System.out.println(emp.toString());
//        }
//    }
    public DAG parseXML(String xmlname, String fileName) {
        DAG empDag = new DAG(fileName);
        Node node = null;
         // XMLInputFactory xmlInputFactory = XMLInputFactory.;

         XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new FileInputStream(xmlname));
            int event = xmlStreamReader.getEventType();
            while(true){
                switch(event) {
                case XMLStreamConstants.START_ELEMENT:
                    if(xmlStreamReader.getLocalName().equals("Data")) {
                        empDag.cur_ver = xmlStreamReader.getAttributeValue(0);
                    }else if(xmlStreamReader.getLocalName().equals("High_branch")){
                        empDag.high_branch = Integer.parseInt(xmlStreamReader.getAttributeValue(0));
                    }
                    else if(xmlStreamReader.getLocalName().equals("Version")){
                        node = new Node();
                        node.version = xmlStreamReader.getAttributeValue(0);
                    }else if(xmlStreamReader.getLocalName().equals("parent")) {
                        bParent = true;
                    }
                    else if(xmlStreamReader.getLocalName().equals("branch")){
                        bBranch=true;
                    }else if(xmlStreamReader.getLocalName().equals("branch")){
                        bComment=true;
                    }

                    break;
                case XMLStreamConstants.CHARACTERS:
                    if(bParent){
                        if(!xmlStreamReader.getText().equals("null")) {
                            node.parent_ver = xmlStreamReader.getText();
                        }
                        else{
                            node.parent_ver = null;
                        }
                        bParent=false;
                    }else if(bBranch){
                        node.branch = xmlStreamReader.getText();
                        bBranch = false;
                    }else if(bComment){
                        node.comment = xmlStreamReader.getText();
                        bComment = false;
                    }
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    if(xmlStreamReader.getLocalName().equals("Version")){
                            empDag.add(node.parent_ver,node.version,node.branch,node.comment);
                    }
                    break;
                }
                if (!xmlStreamReader.hasNext())
                    break;
              event = xmlStreamReader.next();
            }
            
        } catch (FileNotFoundException | XMLStreamException e) {
            e.printStackTrace();
        }
        return empDag;
    }

}
