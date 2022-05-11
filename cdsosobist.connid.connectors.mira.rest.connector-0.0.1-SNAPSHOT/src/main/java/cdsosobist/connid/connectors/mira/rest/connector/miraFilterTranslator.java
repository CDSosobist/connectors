package cdsosobist.connid.connectors.mira.rest.connector;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AbstractFilterTranslator;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;

public class miraFilterTranslator extends AbstractFilterTranslator<MiraFilter> {
    private static final Log LOG = Log.getLog(miraFilterTranslator.class);

    public miraFilterTranslator() {
    }

    protected MiraFilter createEqualsExpression(EqualsFilter filter, boolean not) {
        LOG.ok("createEqualsExpression, filter: {0}, not: {1}", filter, not);
        if (not) {
            return null;
        } else {
            Attribute attr = filter.getAttribute();
            LOG.ok("attr.getName:  {0}, attr.getValue: {1}, Uid.NAME: {2}, Name.NAME: {3}", attr.getName(), attr.getValue(), Uid.NAME, Name.NAME);
            MiraFilter lf;
            if (Uid.NAME.equals(attr.getName())) {
                if (attr.getValue() != null && attr.getValue().get(0) != null) {
                    lf = new MiraFilter();
                    lf.byUid = String.valueOf(attr.getValue().get(0));
                    LOG.ok("lf.byUid: {0}, attr.getValue().get(0): {1}", lf.byUid, attr.getValue().get(0));
                    return lf;
                }
            } else if (Name.NAME.equals(attr.getName())) {
                if (attr.getValue() != null && attr.getValue().get(0) != null) {
                    lf = new MiraFilter();
                    lf.byName = String.valueOf(attr.getValue().get(0));
                    return lf;
                }
            } else if ("mail".equals(attr.getName()) && attr.getValue() != null && attr.getValue().get(0) != null) {
                lf = new MiraFilter();
                lf.byEmailAddress = String.valueOf(attr.getValue().get(0));
                return lf;
            }

            return null;
        }
    }
}
