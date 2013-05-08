package com.example.cos333app;

import library.UserFunctions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;

public class MainActivity extends Activity {
	UserFunctions userFunctions;
	Button btnLogout;
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
    	    
    	    btnLogout = (Button) findViewById(R.id.button_logout);
    	    
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
	
	public void openNewMap(View view) {
	    // Do something in response to button
		Intent intent = new Intent(this, MapActivity.class);
		
		// attempting to make it possible to start MapActivity in two ways
        String key = "newGroup";
        intent.putExtra(key, true);
        
	    startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
