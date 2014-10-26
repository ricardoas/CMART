package server.XMLReader;

import org.w3c.dom.Element;

/**
 * Represent information for each VM disk image
 * 
 * @author Ji Yang
 * @author Andy Turner
 */
public class VMImageSetting {
	public String type;
	public String imagePath;
	public String xmlPath;
	
	/**
	 * Create a VM image from the XML file
	 * @param elementNode
	 */
	public VMImageSetting(Element elementNode) {
		this.type = elementNode.getAttribute("Type");
		this.imagePath = elementNode.getElementsByTagName("ImagePath").item(0).getFirstChild().getNodeValue();
		this.xmlPath = elementNode.getElementsByTagName("XMLPath").item(0).getFirstChild().getNodeValue();
	}
	
	/**
	 * Create a VM image passing each value
	 * 
	 * @param type
	 * @param imagePath
	 * @param xmlPath
	 */
	public VMImageSetting(String type, String imagePath, String xmlPath) {
		this.type = type;
		this.imagePath = imagePath;
		this.xmlPath = xmlPath;
	}
	
	/*
	 * Print the image to a string
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "For " + type +": The image path is " + imagePath + "; and the XML path is " + xmlPath;
	}
}

