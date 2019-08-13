package cdsosobist.connectors.tcpip;

public class biosmartFilter {

    public String byUid;
    public String byName;
    public String byClockNum;
    public String byBirthDate;

    @Override
    public String toString() {
        return "biosmartFilter{" + "byUid='" + byUid + '\'' + "byName='" + byName + '\'' + ", byClockNum='" + byClockNum + '\'' + ", byBirthDate='" + byBirthDate + '\'' + '}';
    }
}
