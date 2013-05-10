package com.example.cos333app;

import org.json.JSONException;
import org.json.JSONObject;

import library.UserFunctions;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

public class MainActivity extends Activity {
	UserFunctions userFunctions;
	Button btnLogout;
	Button btnNotif;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    
		// Check login status 
        userFunctions = new UserFunctions();
        if(userFunctions.isUserLoggedIn(getApplicationContext())){
        	// user already logged in show databoard
        	// TODO: check with server that the token is valid in order to retrieve user's groups
            setContentView(R.layout.activity_main);
            GridView gridview = (GridView) findViewById(R.id.gridview);
    	    gridview.setAdapter(new ImageAdapter(this));
    	    
    	    gridview.setOnItemClickListener(new OnItemClickListener() { //TODO: check if this overwrites special handling of last space. if so, fix.
    	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    	            // Toast.makeText(HelloGridView.this, "" + position, Toast.LENGTH_SHORT).show();
    	        	openMap(v, position);
    	        }
    	    });
    	    
    	    btnLogout = (Button) findViewById(R.id.button_logout);
    	    btnNotif = (Button) findViewById(R.id.button_notif);

    	    
            btnLogout.setOnClickListener(new View.OnClickListener() {
 
                public void onClick(View arg0) {
                    userFunctions.logoutUser(getApplicationContext());
                    Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                    login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(login);
                    // Closing dashboard screen
                    finish();
                }
            });
            
            btnNotif.setOnClickListener(new View.OnClickListener() {
            	 
                public void onClick(View arg0) {
                    Intent login = new Intent(getApplicationContext(), NotificationActivity.class);
                    login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(login);
                    // Closing dashboard screen
                    finish();
                }
            });
    	    
        }else{
            // user is not logged in show login screen
            Intent login = new Intent(getApplicationContext(), LoginActivity.class);
            login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(login);
            // Closing dashboard screen
            finish();
        }
	}

	public void openMap(View view) {
	    // Do something in response to button
		Intent intent = new Intent(this, MapActivity.class);
		
		// attempting to make it possible to start MapActivity in two ways
        String key = "newGroup";
        intent.putExtra(key, false);
        
	    startActivity(intent);
	}
	
	/**
	 * Find the groupID for the group stored at position. Then start a MapActivity with the given groupID as an Extra.
	 * @param view
	 * @param position
	 */
	public void openMap(View view, int position) {
		String groupID = getGroupIDfromPosition(position);
		
	    // Do something in response to button
		Intent intent = new Intent(this, MapActivity.class);
		
		// attempting to make it possible to start MapActivity in two ways
        String key = groupID;
        intent.putExtra("GROUP_ID", key);
        Log.d("GROUP!", groupID);
        Log.d("POSITION!", Integer.toString(position));
	    startActivity(intent);
	}
	
	public String getGroupIDfromPosition(int position) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		String userEmail = prefs.getString("app_email", null);
		String userKey = userEmail + "_memberships";
		String rawJSON = prefs.getString(userKey, null);
		try {
			JSONObject json = new JSONObject(rawJSON);
			int index = position + 4; // due to the backend. first group is in position "4" of the JSONObject.
			JSONObject curr = json.getJSONObject(Integer.toString(index));
			String groupID = curr.getString("group_id");
			return groupID;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "-1";
	}
	
	public void openNewMap(View view) {
	    // Do something in response to button
		Intent intent = new Intent(this, NewGroupActivity.class);
        
	    startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
