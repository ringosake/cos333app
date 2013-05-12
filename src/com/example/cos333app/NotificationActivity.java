package com.example.cos333app;

import java.util.ArrayList;
import java.util.HashMap;

import library.UserFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class NotificationActivity extends Activity {

    private UserFunctions userFunctions;
	private String email, token; // to identify user
    //private int uid;
	
	// message info
	private ArrayList< HashMap<String, String> > messagelist;
	//private ArrayAdapter adapter;
	private ArrayList<Integer> messagetypes;
	private ArrayList<Integer> messagematches;
    
    private static String KEY_STATUS = "status";
    private static String VAL_SUCCESS = "success";
    private static String KEY_NUMNOTIFS = "num_notifs";
    private static String KEY_TYPE = "type";
    private static String KEY_USERNAME = "inviter_user_name";
    private static String KEY_GROUPNAME = "group_name";
    private static String KEY_MATCHID = "match_invitation_id";
    private static final int TYPE_INVITE = 0;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);
		
		// get user
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		email = prefs.getString("app_email", null);
		token = prefs.getString("app_token", null);
		//uid = Integer.parseInt(prefs.getString("app_uid", null));
		
		userFunctions = new UserFunctions();
		
		createList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.notification, menu);
		return true;
	}
	
	private void createList() {
		// query database for all notifications associated with this userid
		int numnotifs = 0;
		int type = -1;
		messagelist = new ArrayList< HashMap<String, String> >();
		messagetypes = new ArrayList<Integer>();
		messagematches = new ArrayList<Integer>();
		JSONObject json = userFunctions.retrieveNotifications(email, token);
		
    	try {
    		if (json.getString(KEY_STATUS) != null && 
    				VAL_SUCCESS.compareToIgnoreCase(json.get(KEY_STATUS).toString()) == 0) { 
    			if (json.getString(KEY_NUMNOTIFS) != null) { numnotifs = json.getInt(KEY_NUMNOTIFS); }
    			Log.d("NOTIFICATION_ACTIVITY", "found " + numnotifs + " notifications");
    			// iterate through notifications
    			for (int n = 4; n < numnotifs+4; n++) {
    				JSONObject jsonnotif = null;
    				if (json.getString(Integer.toString(n)) != null) {
    					int matchid = -1;
    					jsonnotif = json.getJSONObject(Integer.toString(n));
	    				String message = null;
		    			//if (jsonnotif.getString(KEY_TYPE) != null) { type = jsonnotif.getInt(KEY_TYPE); }
	    				if (jsonnotif.getString(KEY_MATCHID) != null) { matchid = jsonnotif.getInt(KEY_MATCHID); }
		    			type = TYPE_INVITE;
	    				switch (type) {
		    			case TYPE_INVITE: 
		    				Log.d("NOTIFICATION_ACTIVITY", "a notif");
		    				HashMap<String, String> tmp = getInviteMessage(jsonnotif);
		    				if (tmp != null) {
		    					messagelist.add(tmp);
		    					messagetypes.add(type);
			    				messagematches.add(matchid);
		    				}
		    				break;
		    			default:
		    				Log.e("NOTIFICATION_ACTIVITY", "type " + type + " not supported");
		    				break;
		    			}
    				}
    			}
    		}
    	}catch (JSONException e) {
    		Log.e("NOTIFICATION_ACTIVITY", "retrieveNotifications: JSON exception");
            e.printStackTrace();
        } catch (Exception e) {
        	Log.e("NOTIFICATION_ACTIVITY", "retrieveNotifications: Exception");
        	Log.e("NOTIFICATION_ACTIVITY", e.getMessage());
        }
    	final SimpleAdapter adapter = new SimpleAdapter(this, messagelist, R.layout.custnotif ,new String[] { "user", "group"}, new int[] { R.id.notifname, R.id.notifgroup});
		ListView listview = (ListView) findViewById(R.id.listview);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				final HashMap<String, String> item = (HashMap<String, String>) parent.getItemAtPosition(position);
				// show options  !!!!!!!!!!!!!
				Intent detailIntent =new Intent(NotificationActivity.this, Details.class); 
				detailIntent.putExtra("email", email);
				detailIntent.putExtra("token", token);
				detailIntent.putExtra("username", messagelist.get(position).get("user"));
				detailIntent.putExtra("groupname", messagelist.get(position).get("group"));
				detailIntent.putExtra("match_id", messagematches.get(position));
	            startActivity(detailIntent);
				messagelist.remove(item);
				adapter.notifyDataSetChanged();
			}
		});
	}
	
	private HashMap<String, String> getInviteMessage(JSONObject json) {
		HashMap<String, String> hm = new HashMap<String, String>();
		String username = "", groupname = "";
		Log.d("NOTIFICATION_ACTIVITY", json.toString());
		try {
			username = json.get(KEY_USERNAME).toString();
			groupname = json.get(KEY_GROUPNAME).toString();
		}catch (JSONException e) {
    		Log.e("NOTIFICATION_ACTIVITY", "getInviteMessage(): JSON exception");
            e.printStackTrace();
            return null;
        } catch (Exception e) {
        	Log.e("NOTIFICATION_ACTIVITY", "getInviteMessage(): Exception");
        	Log.e("NOTIFICATION_ACTIVITY", e.getMessage());
        	return null;
        }
		hm.put("user", username);
		hm.put("group", groupname);
		return hm;
	}

}
