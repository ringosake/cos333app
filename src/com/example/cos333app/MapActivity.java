package com.example.cos333app;

import java.util.ArrayList;
import java.util.List;

import library.UserFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity {
	
	private GoogleMap mMap;
    private UiSettings mUiSettings;
    GPSTracker gps;
    double latitude = 0, longitude = 0;
    private int updatetime = 5000; // 5 seconds
    private Handler handler;
    private List<Marker> markers;
    private String email, token; // to identify user
    private int groupid = 1;
    
    UserFunctions userFunctions;
    LocationManager locationManager;
    Geocoder geocoder;
    
    private static String KEY_STATUS = "status";
    private static String VAL_SUCCESS = "success";
    private static String KEY_NUSERS = "num_users";
    private static String KEY_NAME = "user_name";
    private static String KEY_LAT = "latitude";
    private static String KEY_LONG = "longitude";
    
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
            updateMyLocation();
        }
    	else {
    		Log.e("MAPACTIVITY", "couldn't get gps location");
    	}
    	// update all locations
    	updateAllLocations();
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
            	updateMyLocation();
            }
            else 
            	Log.e("USERINFO", "email/token NULL");
            
            Log.d("GPS", "got gps location (" + latitude + "," + longitude + ")");
        }else{
            Log.e("GPS", "can't get location");
            // can't get location: GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
        updateAllLocations();
        moveCameraView(latitude, longitude);
    	
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setZoomControlsEnabled(false);
    }
	
	private void moveCameraView(double lat, double lng) {
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15);
        mMap.moveCamera(cameraUpdate);
	}
	
	private void updateAllLocations() {
		// query from database 
    	JSONObject jsonret = userFunctions.retrieveAllLocations(email, token, groupid);
    	try {
    		if (jsonret.getString(KEY_STATUS) != null && 
    				VAL_SUCCESS.compareToIgnoreCase(jsonret.get(KEY_STATUS).toString()) == 0) {
    			Log.d("MAPACTIVITY", "successful location retrieval");
    			int nusers = 0;
    			if (jsonret.getString(KEY_NUSERS) != null) { 
    				nusers = jsonret.getInt(KEY_NUSERS);
    				Log.d("MAPACTIVITY", "number of users = "+nusers);
    			}
    			else {
    				Log.e("MAPACTIVITY", "cannot find " + KEY_NUSERS);
    			}
    			double lat = 0, lng = 0;
    			String name = " ";
    			// clear markers
    			for (int m = 0; m < markers.size(); m++) {
    				markers.get(m).remove();
    			}
    			markers.clear();
    			// loop through all locations
    			for (int u = 0; u < nusers; u++) {
    				JSONObject jsonuser = null;
    				if (jsonret.getString(Integer.toString(u)) != null) {
    						jsonuser = jsonret.getJSONObject(Integer.toString(u));
        				if (jsonuser.getString(KEY_LAT) != null) { lat = jsonuser.getDouble(KEY_LAT); }
        				if (jsonuser.getString(KEY_LONG) != null) { lng = jsonuser.getDouble(KEY_LONG); }
        				if (jsonuser.getString(KEY_NAME) != null) { name = jsonuser.get(KEY_NAME).toString(); }
        				Log.d("RETRIEVE", name + " at (" + lat + ", " + lng + ")");
        				Marker m = mMap.addMarker(new MarkerOptions()
        										.position(new LatLng(lat, lng))
        										.title(name));//,
        										//.icon(BitmapDescriptorFactory.defaultMarker(0)));
        				m.setVisible(true);
        				markers.add(m);
    				}
    			}
    		}
    	}catch (JSONException e) {
    		Log.e("MAPACTIVITY", "JSON exception");
            e.printStackTrace();
        } catch (Exception e) {
        	Log.e("MAPACTIVITY", "Exception");
        	Log.e("MAPACTIVITY", e.getMessage());
        }
	}
	
	private void updateMyLocation() {
		JSONObject jsonupdateloc = userFunctions.updateLocation(email, token, latitude, longitude);
    	try {
    		if (jsonupdateloc.getString(KEY_STATUS) != null && 
    				VAL_SUCCESS.compareToIgnoreCase(jsonupdateloc.get(KEY_STATUS).toString()) == 0) 
    			Log.d("MAPACTIVITY", "successful location update");
    	}catch (JSONException e) {
    		Log.e("MAPACTIVITY", "JSON exception");
            e.printStackTrace();
        } catch (Exception e) {
        	Log.e("MAPACTIVITY", "Exception");
        	Log.e("MAPACTIVITY", e.getMessage());
        }
	}
}


