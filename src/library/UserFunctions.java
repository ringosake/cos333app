package library;

import java.util.ArrayList;
import java.util.List;
 
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
 
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
 
public class UserFunctions {
 
    private JSONParser jsonParser;
 
    // Testing in localhost using wamp or xampp
    // use http://10.0.2.2/ to connect to your localhost ie http://localhost/
    private static String loginURL = "http://murmuring-inlet-4150.herokuapp.com/";
    private static String registerURL = "http://murmuring-inlet-4150.herokuapp.com/";
    private static String sendRegEmailURL = "http://murmuring-inlet-4150.herokuapp.com/";
    private static String makeGroupURL = "http://murmuring-inlet-4150.herokuapp.com/";
 
    private static String login_tag = "login";
    private static String register_tag = "register";
    private static String sendRegEmail_tag = "send_reg_email";
    private static String createGroup_tag = "create_group";
    
    // constructor
    public UserFunctions(){
        jsonParser = new JSONParser();
    }
    
    /**
     * function make Login Request
     * @param email
     * @param password
     * */
    public JSONObject loginUser(String email, String token){
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", login_tag));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("access_token", token));
        JSONObject json = jsonParser.getJSONFromUrl(loginURL, params);
        // return json
     // Log.e("JSON", json.toString());
        return json;
    }
    
    /**
     * function create group request
     * @param userid
     * 
     * */
    public JSONObject createGroup(String userid, String groupName, String picURL) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("tag", createGroup_tag));
		params.add(new BasicNameValuePair("userid", userid));
		params.add(new BasicNameValuePair("groupName", groupName));
		params.add(new BasicNameValuePair("picURL", picURL));
		JSONObject json = jsonParser.getJSONFromUrl(makeGroupURL, params);
		return json;
	}
    
    // TODO: this function is not used
    /**
     * function make send registration email when a user signs up
     * @param email
     * @param password
     * */
    public JSONObject sendRegEmail(String email){
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", sendRegEmail_tag));
        params.add(new BasicNameValuePair("email", email));
        JSONObject json = jsonParser.getJSONFromUrl(sendRegEmailURL, params);
        // return json
        return json;
    }
 
    /**
     * Function get Login status
     * */
    public boolean isUserLoggedIn(Context context){
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	return prefs.contains("app_token");

        /*DatabaseHandler db = new DatabaseHandler(context);
        int count = db.getRowCount();
        if(count > 0){
            // user logged in
            return true;
        } return false;*/
    }
 
    /**
     * Function to logout user
     * Reset Database
     * */
    public boolean logoutUser(Context context){
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	final Editor edit = prefs.edit();
    	edit.remove("app_token");
    	edit.remove("app_email");
    	edit.commit();
    	return true;
        /*DatabaseHandler db = new DatabaseHandler(context);
        db.resetTables();
        return true;*/
    }

	public JSONObject registerUser(String email, String name, String number, String token) {
		// Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", register_tag));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("number", number));
        params.add(new BasicNameValuePair("access_token", token));
        JSONObject json = jsonParser.getJSONFromUrl(registerURL, params);
        // return json
     // Log.e("JSON", json.toString());
        return json;
	}
 
}