package cdsosobist.connectors.tcpip;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Records {

    @XmlAttribute
    private Integer operation;
    public void setOperation(Integer operation) {this.operation = operation;}
    public Integer getOperation() {return operation;}

    @XmlAttribute
    private String id;
    public String getId() {return id;}
    public void setId(String id) {this.id = id;}

    @XmlAttribute
    private String name;
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    
    @XmlAttribute(name = "ext_id")
    private String extId;
	public void setExtId(String extId) {this.extId = extId;}

	@XmlElement(name = "FIELD")
    private List<RecordFields> recordFields;
    public List<RecordFields> getRecordFields() {return recordFields;}
    public void setRecordFields(List<RecordFields> recordFields) {this.recordFields = recordFields;}

    public Records() {
    }

}
