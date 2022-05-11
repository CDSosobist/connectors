package cdsosobist.connid.connectors.naumen_rest_connector;

public class NaumenFilter {
	public String byName;
    public String byUid;
    public String byEmailAddress;
    public String byPrivateCode;

    public NaumenFilter() {
    }

    public String toString() {
        return "NaumenFilter{byName='" + this.byName + '\'' + ", byUid=" + this.byUid + ", byEmailAddress='" + this.byEmailAddress + ", byPrivateCode='" + this.byPrivateCode + '\'' + '}';
    }
}
