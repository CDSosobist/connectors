package cdsosobist.connectors.tcpip;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Request {
    @XmlAttribute
    private Integer type;
    public void setType(Integer type) {this.type = type;}

    @XmlElement(name = "RECORD")
    private List<Records> records;
    public List<Records> getRecords() {return records;}
    public void setRecords(List<Records> records) {this.records = records;}

    public Request() {
    }

}
