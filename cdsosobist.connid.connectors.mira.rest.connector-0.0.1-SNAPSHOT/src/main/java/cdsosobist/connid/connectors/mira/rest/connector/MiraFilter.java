package cdsosobist.connid.connectors.mira.rest.connector;

public class MiraFilter {
	
	public String byName;
    public String byUid;
    public String byEmailAddress;

    public MiraFilter() {
    }

    public String toString() {
        return "miraFilter{byName='" + this.byName + '\'' + ", byUid=" + this.byUid + ", byEmailAddress='" + this.byEmailAddress + '\'' + '}';
    }
}
