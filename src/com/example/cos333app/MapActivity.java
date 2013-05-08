package com.example.cos333app;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MapActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		// did we get here via the group creation button?
		boolean isNewGroup = getIntent().getExtras().getBoolean("newGroup");
		//Log.d("first", "Started MapActivity and got boolean");
		if (isNewGroup) {
			//Log.d("second", "Boolean was false!");
			// launch the new group dialog
			//try {
				this.newGroup();
			//} catch (Exception e) {
			//	e.printStackTrace();
			//	String msg = e.getMessage();
			//	Log.d("MapActiv calling newGroup", msg);
			//}
		}
	}

	public void newGroup() {
	    Intent intent = new Intent(this, NewGroupActivity.class);
	    startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map, menu);
		return true;
	}

	public void goHome(View view) {
	    // Do something in response to button
		Intent intent = new Intent(this, MainActivity.class);
	    startActivity(intent);
	}
	
	public void goToMessages(View view) {
	    // Do something in response to button
		Intent intent = new Intent(this, MessageActivity.class);
	    startActivity(intent);
	}
}
