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

//import com.example.cos333app.NewGroupActivity.DownloadImageTask;
import com.google.android.gms.auth.GoogleAuthUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.util.Calendar;

import library.DatabaseHandler;
import library.UserFunctions;

/**
 * Display personalized greeting. This class contains boilerplate code to consume the token but
 * isn't integral to getting the tokens.
 */
public abstract class LoginThreadAbs extends AsyncTask<Void, Void, Void>{
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
    private static String KEY_UID = "user_id";
    private static String KEY_EMAIL = "email";
    private static String KEY_CREATED_AT = "created_at";
    
    EditText inputPassword;
    
    LoginThreadAbs(LoginActivity activity, String email, String scope, int requestCode) {
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
        JSONObject json = userFunctions.loginUser(mEmail, token);
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
	  		    	
	  		    	// get update on user's membership info from server and store in shared prefs.
	  		    	// update image files if needed.
	  		    	String userEmail = json_user.getString(KEY_EMAIL);
	  		    	String userKey = userEmail + "_memberships";
	  		    	String newInfo = this.updateMembership(userEmail, token, userKey, prefs);
	  		    	
	  		    	// replace old memberships info with new
	  		    	if (!newInfo.equals("ERROR")) {
	  		    		edit.putString(userKey, newInfo);
	  		    	}
	  		    	// store
	  		    	edit.commit();
	  		    	Log.d("newInfo", newInfo);
	  				
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
        	mActivity.show("4");
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
     * Returns a String which describes the JSONObject detailing the current user's memberships, and updates their group's
     * images.
     * @param userEmail
     * @param token
     * @param userKey
     * @param prefs
     * @return Returns a String which describes the JSONObject detailing the current user's memberships
     */
    private String updateMembership (String userEmail, String token, String userKey, SharedPreferences prefs) {
    	// talk to server
    	UserFunctions uf = new UserFunctions();
	    JSONObject json2 = uf.getMemberships(mEmail, token);
	    //return "ERROR";	
	    
	    try {
	    	Log.d("raw", json2.toString());
	    	int jsonLength = json2.getInt("num_groups"); // check field
	    	// JSONArray json_memberships = json2.getJSONArray("dets"); //TODO: obj or array?
	    	// dets has been deleted. it's all jsonobjects now. each of the user's groups is
	    	// represented by a jsonobject named by a number between 4 and 4 + jsonLength. yep
	    	
	    	boolean haveChanges = false;
	    	
	    	// check if this data has never been stored before. if so, update all 
	    	if (!prefs.contains(userKey)) {
	    		// Starts at 4 and goes to 4 + jsonLength due to back end
	    		for (int i = 4; i < 4 + jsonLength; i++) {
	    			if (!json2.has(Integer.toString(i)))
	    				continue;
	    			JSONObject curr = json2.getJSONObject(Integer.toString(i)); //TODO: (Integer.toString(i));
	    			String groupID = curr.getString("group_id");
	    			String picURL = curr.getString("picture_url");
	    			updateGroupImage(picURL, mEmail, groupID);
	    		}
	    		reorderImages(mEmail, json2, prefs);
	    		return json2.toString();
	    	}
	    	
	    	// compare and update as needed
	    	// Starts at 4 and goes to 4 + jsonLength due to back end
	    	for (int i = 4; i < 4 + jsonLength; i++) {
	    		if (!json2.has(Integer.toString(i)))
    				continue;
	    		// get current JSONObject
	    		JSONObject curr = json2.getJSONObject(Integer.toString(i)); //TODO: see above and check object vs array
	    		// check role. if 0, continue (because they are invitees who haven't accepted)
	    		if (curr.getInt("role") == 0)
	    			continue;
	    		// check for image change. if so, update. if not, continue.
	    		if (prefs.contains(userKey)) {
	    			String oldData = prefs.getString(userKey, "");
	    			JSONObject oldJSON = new JSONObject(oldData);
	    			if (i > oldJSON.length()) {
	    				String groupID = curr.getString("group_id");
	    				String picURL = curr.getString("picture_url");
	    				updateGroupImage(picURL, mEmail, groupID); // must indicate: group index? or group id + url?
	    				haveChanges = true; // will need to update timestamps on all files to adjust
	    			} else {
	    				JSONObject oldCurr = oldJSON.getJSONObject(Integer.toString(i));
	    				if (curr.getString("picture_url").equals(oldCurr.getString("picture_url"))) // check if group ids match?
	    					continue;
	    				String groupID = curr.getString("group_id");
	    				String picURL = curr.getString("picture_url");
	    				updateGroupImage(picURL, mEmail, groupID);
	    				haveChanges = true;
	    			}
	    		}
	    		
	    	}
	   	
	    	// if any photos have been replaced, the pictures will be out of order.
	    	if (haveChanges) {
	    		reorderImages(userEmail, json2, prefs);
	    	}
	    
	    	return json2.toString();
	    } catch (JSONException e) {
	    	e.printStackTrace();
	    	return "ERROR";
	    }
    }
    
    private void updateGroupImage(String picture_url, String email, String groupID) {
    	// get pic from URL
    	ImageDownloadTask task = new ImageDownloadTask();
    	Bitmap pic = task.doInBackground(picture_url, email, groupID);
    	// save it in appropriate filename.
    	task.onPostExecute(pic);
    	//TODO: will this overwrite as needed?
    }
    
    private void reorderImages(String userEmail, JSONObject json2, SharedPreferences prefs) {
    	// cycle through this user's group image files in order determined by group join date
    	// update the timestamps of each image to match this ordering
    	String userName = prefs.getString("app_email", null);
    	userName = userName.replace("@", "");
    	userName = userName.replace(".", "");
    	File fileStump = new File(Environment.getExternalStorageDirectory() // change code above to refer to this dir
	 			+ File.separator + "group_logos" + File.separator + userName + File.separator); // + groupID + ".jpg");
    	if (!fileStump.exists())
    		fileStump.mkdirs();
    	
    	try {
    		int jsonLength = json2.getInt("num_groups");
    		// array starts at 4 due to back end. Stops 3 short because final field is length. lol
    		for (int i = 4; i < 4 + jsonLength; i++) {
    			// get current
    			if (!json2.has(Integer.toString(i)))
    				continue;
    			JSONObject curr = json2.getJSONObject(Integer.toString(i));
    			String currGroup = curr.getString("group_id");
    			File currFile = new File(fileStump.toString() + currGroup +".jpg"); //TODO: this assumes JPG
    			if (currFile.exists()) {
    				//Calendar now = Calendar.getInstance();
    				Log.d("updateAttempt", "updateAttempt");
    				//currFile.setLastModified(now.getTimeInMillis() + i);
    				currFile.setLastModified(curr.getLong("join_date"));
    			}
    		}
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}
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
