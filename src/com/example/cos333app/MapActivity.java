package com.example.cos333app;

import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
/*import android.app.Activity;*/
import android.support.v4.app.FragmentManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import android.view.Menu;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.CameraUpdate;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;

import library.UserFunctions;
import android.location.*;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.util.Log;

public class MapActivity extends FragmentActivity {
	
	private GoogleMap mMap;
    private UiSettings mUiSettings;
    GPSTracker gps;
    double latitude = 0, longitude = 0;
    private int updatetime = 5000; // 5 seconds
    private Handler handler;
    private List<Marker> markers;
    private String email, token; // to identify user
    
    UserFunctions userFunctions;
    LocationManager locationManager;
    Geocoder geocoder;
    TextView locationText;
    
    private int debug = 0;
    
    Runnable statusChecker = new Runnable() {
    	@Override
    	public void run() {
    		updateStatus();
    		handler.postDelayed(statusChecker, updatetime);
    	}
    };
    void startRepeatingTask() {
    	statusChecker.run();
    }
    void stopRepeatingTask() {
    	handler.removeCallbacks(statusChecker);
    }
    void updateStatus() {
    	// query location
    	if(gps.canGetLocation()){
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            int test = gps.getDebug();
            // send location to database !!!!!!
            TextView text = (TextView) findViewById(R.id.tv);
            text.setText(debug + " (" + latitude + "," + longitude + ") " + test);
            debug++;
        }
    	else {
    		TextView text = (TextView) findViewById(R.id.tv);
            text.setText("couldn't get gps location");
    	}
    	// query database for other locations !!!!!!!!
    	
    	// update markers
    	for (int i = 0; i < markers.size(); i++) {
    		markers.get(i).setPosition(new LatLng(latitude, longitude));
    	}
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		// get user
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		email = prefs.getString("app_email", null);
		token = prefs.getString("app_token", null);
		userFunctions = new UserFunctions();
		
		handler = new Handler();
        markers = new ArrayList<Marker>();
        locationManager = (LocationManager)this.getSystemService(LOCATION_SERVICE);
        geocoder = new Geocoder(this);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
          latitude = location.getLatitude();
          longitude = location.getLongitude();
        }
        
        setUpMapIfNeeded();
        startRepeatingTask();
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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
	
	private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            //map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }
	
	private void setUpMap() {
    	gps = new GPSTracker(MapActivity.this);
    	// check if GPS enabled
        if(gps.canGetLocation()){
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            // send location to database
            if (email != null && token != null) {
            	JSONObject json = userFunctions.updateLocation(email, token, latitude, longitude);
            }
            else 
            	Log.e("USERINFO", "email/token NULL");
            
            TextView text = (TextView) findViewById(R.id.tv);
            text.setText("got gps location (" + latitude + "," + longitude + ")");
            Log.e("GPS", "got gps location (" + latitude + "," + longitude + ")");
        }else{
        	TextView text = (TextView) findViewById(R.id.tv);
            text.setText("can't get location");
            Log.e("GPS", "can't get location");
            // can't get location: GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
        if (markers.isEmpty()) {
        	// query from database 
        	JSONObject json = userFunctions.retrieveAllLocations(email, token, 1);
            // loop through all locations
        	markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You")));
        }
        CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15);
        mMap.moveCamera(cameraUpdate);
    	
        mUiSettings = mMap.getUiSettings();
        //mMap.setMyLocationEnabled(true);
        //mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setZoomControlsEnabled(false);
    }
}
