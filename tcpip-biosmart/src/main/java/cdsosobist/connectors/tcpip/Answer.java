package cdsosobist.connectors.tcpip;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Answer {
    @XmlAttribute
    private Integer type;
    public void setType(Integer type) {this.type = type;}

    @XmlElement(name = "RECORD")
    private List<Records> records;
    List<Records> getRecords() {return records;}
    public void setRecords(List<Records> records) {this.records = records;}

    public Answer() {
    }

}
