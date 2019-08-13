package cdsosobist.connectors.rest;

import org.apache.http.client.methods.HttpGet;
import org.identityconnectors.common.logging.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static cdsosobist.connectors.rest.currentEmpDataHandler.CED_EMP_KEY;
import static cdsosobist.connectors.rest.resourceHandler.*;

public class CurrentEmpDataCache {

    private static final Log LOG = Log.getLog(CurrentEmpDataCache.class);

    Map<String, Object> cacheByEmpKey = new HashMap<>();

    zup3Connector connector;


    public CurrentEmpDataCache(zup3Connector connector) throws IOException {

        HttpGet request = new HttpGet((connector.getConfiguration()).getServiceAddress() + CURRENT_EMP_DATA + REQ_FORMAT);
        JSONArray currEmpDatas = connector.callRequest(request);

        for (int i = 0; i < currEmpDatas.length(); i++) {
            JSONObject currEmpData = currEmpDatas.getJSONObject(i);
            String key = currEmpData.getString(CED_EMP_KEY);
            Object value = currEmpData;

            cacheByEmpKey.put(key,value);
        }

    }

    public void clear() {
        if (cacheByEmpKey != null) {
            cacheByEmpKey.clear();
            cacheByEmpKey = null;
        }

        this.connector = null;
    }

}
