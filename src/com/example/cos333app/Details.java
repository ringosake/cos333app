package com.example.cos333app;

import java.util.ArrayList;
import java.util.List;

import library.UserFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Details extends FragmentActivity {
	
	UserFunctions userfunctions;
	private static String KEY_STATUS = "status";
    private static String VAL_SUCCESS = "success";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);
		
		Intent startIntent = getIntent();
		final String email = startIntent.getStringExtra("email");
		final String token = startIntent.getStringExtra("token");
		final String username = startIntent.getStringExtra("username");
		final String groupname = startIntent.getStringExtra("groupname");
		final int matchid = startIntent.getIntExtra("match_id", -1);
		if (matchid < 0) finish();
		userfunctions = new UserFunctions();
		TextView text = (TextView)findViewById(R.id.textView1);
		text.setText(username + " invited you to join the group " + groupname);
		
		ArrayList<String> list = new ArrayList<String>();
		list.add("Accept");
		list.add("Reject");
		ArrayAdapter<Model> adapter = new InteractiveArrayAdapter(this, getModel());
		ListView listview = (ListView) findViewById(R.id.detaillistview);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				int response;
				if (position == 0) { // accept
					response = 1;
				}
				else { // reject
					response = 0;
				}
				/*JSONObject json = userfunctions.respondNotification(email, token, matchid, response);
				try {
		    		if (json.getString(KEY_STATUS) != null && 
		    				VAL_SUCCESS.compareToIgnoreCase(json.get(KEY_STATUS).toString()) == 0) { ; }
				}catch (JSONException e) {
		    		Log.e("DETAILS", "respondnotif: JSON exception");
		            e.printStackTrace();
		        } catch (Exception e) {
		        	Log.e("DETAILS", "respondnotif: Exception");
		        	Log.e("DETAILS", e.getMessage());
		        }*/
				Log.d("DETAILS", "response = " + response);
			}
		});
	}
	
	private List<Model> getModel() {
	    List<Model> list = new ArrayList<Model>();
	    list.add(get("Accept"));
	    list.add(get("Reject"));
	    return list;
	  }
	
	private Model get(String s) {
	    return new Model(s);
	  }
}
