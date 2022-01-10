package cdsosobist.connid.connectors.mira.rest.connector;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Frsthndl {



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String minPath = "https://portaldev.cds.spb.ru/mira/service/v2/persons/54?appid=system";
		
		String sKey = "&secretkey=d^1uC8M!";
		
		String preRequest = minPath + sKey;
		
		String md5Hash = pathToHash(preRequest);
		
		String finRequest = minPath + "&sign=" + md5Hash;
		
		System.out.println(finRequest);

	}

	private static String pathToHash(String preRequest) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(preRequest.getBytes());
			BigInteger no = new BigInteger(1, messageDigest);
			String hashText = no.toString(16);
			while (hashText.length() < 32) {
                hashText = "0" + hashText;
            }
            return hashText.toUpperCase();
		} catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
	}

}
