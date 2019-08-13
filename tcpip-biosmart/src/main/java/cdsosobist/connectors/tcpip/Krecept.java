package cdsosobist.connectors.tcpip;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "KRECEPT")
@XmlAccessorType(XmlAccessType.FIELD)
public class Krecept {

    @XmlElement(name = "REQUEST")
    private Request request;
    public Request getRequest() {return this.request;}
    public void setRequest(Request request) {this.request = request;}

    @XmlElement(name = "answer")
    private Answer answer;
    public Answer getAnswer() {return answer;}
    public void setAnswer(Answer answer) {this.answer = answer;}
}