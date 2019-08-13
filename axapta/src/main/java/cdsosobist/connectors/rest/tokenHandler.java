package cdsosobist.connectors.rest;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;


public class tokenHandler {

    @SuppressWarnings("unused")
	private String tokenType;
    @SuppressWarnings("unused")
	private String tokenValue;

    private JSONParser parser = new JSONParser();
    private Object obj;

    public String getTokenValue() {
        try {
            obj = parser.parse(new FileReader("/home/kra/midpoint/var/token.txt"));
            JSONObject jsonObject = (JSONObject) obj;
            return tokenValue = (String) jsonObject.get("access_token");
        } catch (IOException e) {e.printStackTrace();} catch (ParseException e) {e.printStackTrace();}
        return null;
    }


    public String getTokenType() {
        try {
            obj = parser.parse(new FileReader("/home/kra/midpoint/var/token.txt"));
            JSONObject jsonObject = (JSONObject) obj;
            return tokenType = (String) jsonObject.get("token_type");
        } catch (IOException e) {e.printStackTrace();} catch (ParseException e) {e.printStackTrace();}
        return null;
    }
}
