package cdsosobist.connectors.tcpip;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class RecordFields {

    @XmlAttribute(name = "name")
    private String xmlFieldName;
    public String getXmlFieldName() {return xmlFieldName;}
    public void setXmlFieldName(String xmlFieldName) {this.xmlFieldName = xmlFieldName;}

    @XmlValue
    private String xmlFieldValue;
    public String getXmlFieldValue() {return xmlFieldValue;}
    public void setXmlFieldValue(String xmlFieldValue) {this.xmlFieldValue = xmlFieldValue;}

}
