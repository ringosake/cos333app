/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package library;

import com.example.cos333app.LoginActivity;
import com.example.cos333app.MainActivity;
import com.example.cos333app.R;
import com.google.android.gms.auth.GoogleAuthUtil;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Display personalized greeting. This class contains boilerplate code to consume the token but
 * isn't integral to getting the tokens.
 */
public abstract class AbstractGetNameTask extends AsyncTask<Void, Void, Void>{
    private static final String TAG = "TokenInfoTask";
    private static final String NAME_KEY = "given_name";
    protected LoginActivity mActivity;

    protected String mScope;
    protected String mEmail;
    protected int mRequestCode;
    
    // JSON Response node names
    private static String KEY_SUCCESS = "success";
    private static String KEY_ERROR = "error";
    private static String KEY_ERROR_MSG = "error_msg";
    private static String KEY_UID = "uid";
    private static String KEY_EMAIL = "email";
    private static String KEY_CREATED_AT = "created_at";
    
    EditText inputPassword;
    
    AbstractGetNameTask(LoginActivity activity, String email, String scope, int requestCode) {
        this.mActivity = activity;
        this.mScope = scope;
        this.mEmail = email;
        this.mRequestCode = requestCode;
    }

    @Override
    protected Void doInBackground(Void... params) {
      try {
        fetchNameFromProfileServer();
      } catch (IOException ex) {
        onError("Following Error occured, please try again. " + ex.getMessage(), ex);
      } catch (JSONException e) {
        onError("Bad response: " + e.getMessage(), e);
      }
      return null;
    }

    protected void onError(String msg, Exception e) {
        if (e != null) {
          Log.e(TAG, "Exception: ", e);
        }
        mActivity.show(msg);  // will be run in UI thread
    }

    /**
     * Get a authentication token if one is not available. If the error is not recoverable then
     * it displays the error message on parent activity.
     */
    protected abstract String fetchToken() throws IOException;

    /**
     * Contacts the user info server to get the profile of the user and extracts the first name
     * of the user from the profile. In order to authenticate with the user info server the method
     * first fetches an access token from Google Play services.
     * @throws IOException if communication with user info server failed.
     * @throws JSONException if the response from the server could not be parsed.
     */
    private void fetchNameFromProfileServer() throws IOException, JSONException {
        String token = fetchToken();
        if (token == null) {
          // error has already been handled in fetchToken()
          return;
        }
        URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        int sc = con.getResponseCode();
        if (sc == 200) {
          InputStream is = con.getInputStream();
          String name = getFirstName(readResponse(is));
          //mActivity.show("Hello " + name + "!");
          is.close();
          
          // check for login response
          inputPassword = (EditText) mActivity.findViewById(R.id.loginPassword);
          String password = inputPassword.getText().toString();
          UserFunctions userFunctions = new UserFunctions();
          try {
          	JSONObject json = userFunctions.loginUser(mEmail, password);
      		if (json.getString(KEY_SUCCESS) != null) {
      			
                  String res = json.getString(KEY_SUCCESS);
                  if(Integer.parseInt(res) == 1){
                      // user successfully registred
                      // Store user details in SQLite Database
                      DatabaseHandler db = new DatabaseHandler(mActivity.getApplicationContext());
                      JSONObject json_user = json.getJSONObject("user");

                      // Clear all previous data in database
                      userFunctions.logoutUser(mActivity.getApplicationContext());
                      db.addUser(json_user.getString(KEY_EMAIL), json.getString(KEY_UID), json_user.getString(KEY_CREATED_AT));
                  
                      // Check login status in database
                      if (userFunctions.isUserLoggedIn(mActivity.getApplicationContext())) {
                      		// Launch Dashboard Screen
                          	Intent dashboard = new Intent(mActivity.getApplicationContext(), MainActivity.class);

                          	// Close all views before launching Dashboard
                          	dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                          	mActivity.startActivity(dashboard);

                          	// Close Login Screen
                          	mActivity.finish();
                      }
                  } else {
                  	// display the error
                  	if (json.getString(KEY_ERROR_MSG) != null) {
                  		res = json.getString(KEY_ERROR_MSG);
                  		mActivity.show(res);
                  	}
                  }
              }
          } catch (JSONException e) {
              e.printStackTrace();
          }
          return;
        } else if (sc == 401) {
            GoogleAuthUtil.invalidateToken(mActivity, token);
            onError("Server auth error, please try again.", null);
            Log.i(TAG, "Server auth error: " + readResponse(con.getErrorStream()));
            return;
        } else {
          onError("Server returned the following error code: " + sc, null);
          return;
        }
    }

    /**
     * Reads the response from the input stream and returns it as a string.
     */
    private static String readResponse(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = new byte[2048];
        int len = 0;
        while ((len = is.read(data, 0, data.length)) >= 0) {
            bos.write(data, 0, len);
        }
        return new String(bos.toByteArray(), "UTF-8");
    }

    /**
     * Parses the response and returns the first name of the user.
     * @throws JSONException if the response is not JSON or if first name does not exist in response
     */
    private String getFirstName(String jsonResponse) throws JSONException {
      JSONObject profile = new JSONObject(jsonResponse);
      return profile.getString(NAME_KEY);
    }
}
