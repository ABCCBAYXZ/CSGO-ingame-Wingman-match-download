package CSGO;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.json.JSONObject;

public class Login {
	private int counter = 0;
	private String continueToken;
	private String directory;
	private String username;
	private String password;
	private String encryptedPassword;
	private String rsatimestamp;
	private String twofactorcode;
	private String emailauth;
	private String cookies;
	private String sessionID;
	private String steamID;
	private JSONObject jObj;
	private boolean unableToGetPersonalData;
	
	public Login() throws IOException {
		reset();
		loadUserData();
	}
	
	public void reset() {
		continueToken = "";
		directory = "";
		unableToGetPersonalData = false;
		password="";
		encryptedPassword="";
		rsatimestamp="";
		twofactorcode="";
		emailauth="";
		cookies= "";
		sessionID = "";
		steamID = "";
		jObj = new JSONObject();
	}
	
	public PublicKey createKey(String mod, String exp) throws NoSuchAlgorithmException, InvalidKeySpecException {
		BigInteger modulus = new BigInteger(mod, 16);
		BigInteger exponent = new BigInteger(exp, 16);

		RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
		KeyFactory factory = KeyFactory.getInstance("RSA");
		
		return factory.generatePublic(spec);
	}
	
	public void encryptPassword() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		URL ulu = new URL("https://steamcommunity.com/login/home/getrsakey/?username=" + username);
		URLConnection con = ulu.openConnection();
		BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));

		JSONObject jObj = new JSONObject(reader.readLine());
		
		if(jObj.getBoolean("success")) {
		
		rsatimestamp = jObj.getString("timestamp");
		
		PublicKey pub = createKey(jObj.getString("publickey_mod"),jObj.getString("publickey_exp"));
	
		Cipher encryptCipher = Cipher.getInstance("RSA");
		encryptCipher.init(Cipher.ENCRYPT_MODE, pub);

		byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
		byte[] encryptedPasswordBytes = encryptCipher.doFinal(passwordBytes);

		encryptedPassword = Base64.getEncoder().encodeToString(encryptedPasswordBytes);
		}
		
	}

	public byte[] createPostDataBytes(Map<String, Object> params) throws UnsupportedEncodingException {
		
		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String, Object> param : params.entrySet()) {
			if (postData.length() != 0)
				postData.append('&');
			postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		}

		return postData.toString().getBytes("UTF-8");
		
	}
	
	public String requestLogin() throws IOException {

		// LOGIN / POST request to obtain user data
		
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("password", encryptedPassword);
		params.put("username", username);
		params.put("rsatimestamp", rsatimestamp);
		params.put("twofactorcode", twofactorcode);
		params.put("emailauth", emailauth);
		params.put("loginfriendlyname", "");
		params.put("captchagid", -1);
		params.put("emailsteamid", "");
		params.put("remember_login", true);

		byte[] postDataBytes = createPostDataBytes(params);
		
		URL url = new URL("https://steamcommunity.com/login/home/dologin/");
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		conn.setDoOutput(true);
		conn.getOutputStream().write(postDataBytes);

		Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

		String output = "";
		for (int c; (c = in.read()) >= 0;)
			output = output + (char) c;
		
		in.close();
		

		jObj = new JSONObject(output);
		
			if (jObj.has("requires_twofactor") && jObj.getBoolean("requires_twofactor")) {
				return "enter Steam Guard code";
			} else if (jObj.has("emailauth_needed") && jObj.getBoolean("emailauth_needed")) {
				return "enter Email code";
			} else if (jObj.has("login_complete") && jObj.getBoolean("login_complete")) {
				createCookie();
				saveUserData();
				return "Logged in!";
			}
			
			
			return "error: \"" + jObj.getString("message") + "\"";

	}

	public void saveUserData() throws IOException {
		Properties p = new Properties();
		p.setProperty("cookie", this.cookies);
		p.setProperty("steamID", steamID);
		p.setProperty("directory", directory);
		p.setProperty("sessionID", sessionID);
		p.store(new FileWriter("props.properties"), "");
		
	}
	
	public void loadUserData() throws IOException {
		Properties p = new Properties();
		try {
			p.load(new FileReader("props.properties"));
		} catch (FileNotFoundException ex) {
			saveUserData();
		}
		cookies = p.getProperty("cookie");
		steamID = p.getProperty("steamID");
		directory = p.getProperty("directory");
		sessionID = p.getProperty("sessionID");
	}

	public void createCookie() {

		if (jObj.has("transfer_parameters")) {
			JSONObject paramObj = jObj.getJSONObject("transfer_parameters");
			
			if (paramObj.has("token_secure") && paramObj.has("steamid") && paramObj.has("auth")) {
			
				String token_secure = paramObj.getString("token_secure");
				sessionID = paramObj.getString("auth");
				String steamLogin = paramObj.getString("steamid") + "%7C%7C" + sessionID;
				String SteamLoginSecure = paramObj.getString("steamid") + "%7C%7C" + token_secure;
		
				cookies = "sessionid=" + sessionID + "; steamLogin=" + steamLogin + "; steamLoginSecure="
						+ SteamLoginSecure;
			}
		}
	}

	public String requestWingman() throws UnsupportedEncodingException, IOException {
		String urlA = "https://steamcommunity.com/profiles/" + steamID + "/gcpd/730/?tab=matchhistorywingman"+
				"&continue_token=" + continueToken + "&sessionid=" + sessionID;

		URL urlB = new URL(urlA);
		URLConnection urlConn = urlB.openConnection();

		urlConn.setRequestProperty("Cookie", cookies);

		urlConn.setUseCaches(true);

		urlConn.connect();

		BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF8"));

		String line = "";
		
		while ((line = reader.readLine()) != null) {
			
			if(line.contains("Personal game data for Counter-Strike: Global Offensive is currently unavailable. Please come back later."))
				return null;
				
			if(line.contains("var g_sGcContinueToken = '")) {
				continueToken = line.replace("var g_sGcContinueToken = '", "").replace("';", "").trim();
				
			}
			
			if(line.contains("replay")) {
				unableToGetPersonalData = false;
				counter = 0;
				return line;
			}
			System.out.println(line);
		}
		counter++;
		reader.close();
		
		if(counter < 2) {
			return requestWingman();
		}
		
		unableToGetPersonalData = true;
		return null;
	}
	
	public boolean cookieWorking() throws UnsupportedEncodingException, IOException {
		if(cookies == null || cookies.equals(""))
			return false;
		
		if(requestWingman() != null) 
			return true;
	
		return false;
	}
	

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTwofactorcode() {
		return twofactorcode;
	}

	public void setTwofactorcode(String twofactorcode) {
		this.twofactorcode = twofactorcode;
	}

	public String getEmailauth() {
		return emailauth;
	}

	public void setEmailauth(String emailauth) {
		this.emailauth = emailauth;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getSteamID() {
		return steamID;
	}

	public void setSteamID(String steamID) {
		this.steamID = steamID;
	}

	public boolean isUnableToGetPersonalData() {
		return unableToGetPersonalData;
	}

	public void setUnableToGetPersonalData(boolean unableToGetPersonalData) {
		this.unableToGetPersonalData = unableToGetPersonalData;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

}
