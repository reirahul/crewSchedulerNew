package rolustech.helper;

import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class XmlHelper {

	public static Document getXMLDocument(String userRoles) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        return (Document) builder.parse(new ByteArrayInputStream(userRoles.replaceAll(" ", "").getBytes()));
	}
	
}
