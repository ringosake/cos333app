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

package com.example.cos333app;


import com.google.android.gms.auth.GoogleAuthUtil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import library.DatabaseHandler;
import library.UserFunctions;

/**
 * Display personalized greeting. This class contains boilerplate code to consume the token but
 * isn't integral to getting the tokens.
 */
public abstract class RegisterThreadAbs extends AsyncTask<Void, Void, Void>{
    private static final String TAG = "TokenInfoTask";
    private static final String NAME_KEY = "given_name";
    protected RegisterActivity mActivity;

    protected String mScope;
    protected String mEmail;
    protected String mName;
    protected String mNumber;
    protected int mRequestCode;
    
    // JSON Response node names
    private static String KEY_SUCCESS = "success";
    private static String KEY_ERROR = "error";
    private static String KEY_ERROR_MSG = "error_msg";
    private static String KEY_UID = "user_id";
    private static String KEY_EMAIL = "email";
    private static String KEY_CREATED_AT = "created_at";
    
    EditText inputPassword;
    
    RegisterThreadAbs(RegisterActivity activity, String email, String name, String number, String scope, int requestCode) {
        this.mActivity = activity;
        this.mScope = scope;
        this.mEmail = email;
        this.mName = name;
        this.mNumber = number;
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
    
    @Override
    protected void onPostExecute(Void result) {
        mActivity.progress.dismiss();
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
        
        UserFunctions userFunctions = new UserFunctions();
        mActivity.show("0");
        JSONObject json = userFunctions.registerUser(mEmail, mName, mNumber, token);
        mActivity.show("1");
        //InputStream is = con.getInputStream();
        //String name = getFirstName(readResponse(is));
        //mActivity.show("Hello " + name + "!");
        //is.close();
        try {
	  		if (json.getString(KEY_SUCCESS) != null) {
	  			mActivity.show("2");
	  			String res = json.getString(KEY_SUCCESS);
	  			if(Integer.parseInt(res) == 1){
	  				JSONObject json_user = json.getJSONObject("user");
		
	  				// store login infos
	  				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext());
	  		    	final Editor edit = prefs.edit();
	  		    	edit.putString("app_email", json_user.getString(KEY_EMAIL));
	  		    	edit.putString("app_token", token);
	  		    	edit.putString("app_uid", json.getString("user_id"));
	  		    	edit.commit();
	  				
  					// Launch Dashboard Screen
	              	Intent dashboard = new Intent(mActivity.getApplicationContext(), MainActivity.class);
	
	              	// Close all views before launching Dashboard
	              	dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                  	mActivity.startActivity(dashboard);

                  	// Close Login Screen
                  	mActivity.finish();
		        } else {
		        	mActivity.show("3");
		        	GoogleAuthUtil.invalidateToken(mActivity, token);
		          	// display the error
		          	if (json.getString(KEY_ERROR_MSG) != null) {
		          		res = json.getString(KEY_ERROR_MSG);
		          		mActivity.show(res);
		          	}
		        }
		    }
        } catch (JSONException e) {
        	mActivity.show(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
        	mActivity.show(e.getMessage());
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
