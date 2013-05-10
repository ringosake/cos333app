package com.example.cos333app;

import java.util.LinkedList;

import library.UserFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.View;
import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.Polyline;

public class MapActivity extends FragmentActivity {
	
	// maps info
	private GoogleMap mMap;
    private UiSettings mUiSettings;
    private GPSTracker gps;
    private Location cur_location;
    private LatLngBounds bounds = null;
    private Handler handler;
    private LinkedList<Marker> markers;
    private LinkedList<LatLng> locations;
    private LinkedList<PolylineOptions> trails;
    private LinkedList<Polyline> traillines;
    private LinkedList<Boolean> validusers;
    private LinkedList<Integer> trailhues;
    private SparseIntArray userToIndex;
    private static int updatetime = 5000; // 5 seconds
    private static double mindist = 2; // don't update unless 2m difference
    private boolean SHOW_TRAILS;
    private float nextcolour = 0; // for colouring trails
    private float [] results; // scratch work
    private float [] hsvcolour; // scratch space
    
    // user, group identification
	private String email, token; // to identify user
    private int uid;
    private int groupid = 1;
    
    private UserFunctions userFunctions;
    
    private static String KEY_STATUS = "status";
    private static String VAL_SUCCESS = "success";
    private static String KEY_NUSERS = "num_users";
    private static String KEY_NAME = "user_name";
    private static String KEY_LAT = "latitude";
    private static String KEY_LONG = "longitude";
    private static String KEY_UID = "user_id";
    private static float SELF_COLOUR = BitmapDescriptorFactory.HUE_RED;
    private static float OTHERS_COLOUR = BitmapDescriptorFactory.HUE_AZURE;
    private static float COLOUR_INCREMENT = 30; // for colouring trails
    private static float HSV_SATURATION = 1;
    private static float HSV_VALUE = 1;
    private static int MAX_SIZE = 6000;
    
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
            Location newloc = gps.getLocation();
            if (isBetterLocation(newloc, cur_location)) {
            	Log.d("MAPACTIVITY", "better location");
            	cur_location = newloc;
            	updateMyLocation();
            }
        }
    	else {
    		Log.e("MAPACTIVITY", "couldn't get gps location");
    	}
    	// update all locations
    	updateAllLocations();
    	updateTrails();
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		SHOW_TRAILS = false;
		
		// get user
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		email = prefs.getString("app_email", null);
		token = prefs.getString("app_token", null);
		uid = Integer.parseInt(prefs.getString("app_uid", null));
		userFunctions = new UserFunctions();
		
		// get extra to identify group
		Intent intent = getIntent();
		String gid = intent.getStringExtra("GROUP_ID");
		groupid = Integer.parseInt(gid);
		
		handler = new Handler();
        markers = new LinkedList<Marker>();
        locations = new LinkedList<LatLng>();
        userToIndex = new SparseIntArray();
        validusers = new LinkedList<Boolean>();
        trails = new LinkedList<PolylineOptions>();
        traillines = new LinkedList<Polyline>();
        trailhues = new LinkedList<Integer>();
        results = new float[1];
        hsvcolour = new float[3];
        hsvcolour[0] = 0;
        hsvcolour[1] = HSV_SATURATION;
        hsvcolour[2] = HSV_VALUE;
        
        setUpMapIfNeeded();
        startRepeatingTask();
	}
	
	private void cleanup() { // clean up all information related to this map
		stopRepeatingTask();
		if (markers != null) markers.clear();
		if (locations != null) locations.clear();
		if (userToIndex != null) userToIndex.clear();
		if (validusers != null) validusers.clear();
		if (trails != null) trails.clear();
		if (traillines != null) traillines.clear();
		if (trailhues != null) trailhues.clear();
		hsvcolour[0] = 0;
        hsvcolour[1] = HSV_SATURATION;
        hsvcolour[2] = HSV_VALUE;
        bounds = null;
        cur_location = null;
        nextcolour = 0;
        SHOW_TRAILS = false;
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        
        // get extra to identify group
        Intent intent = getIntent();
     	String gid = intent.getStringExtra("group_id");
     	if (gid != null) {
	     	int igid = Integer.parseInt(gid);
	        if (igid != groupid) {
	        	groupid = igid;
	        	cleanup();
	        }
     	}
        
        setUpMapIfNeeded();
        startRepeatingTask();
    }
	/*@Override
    protected void onPause() {
		Log.d("MAPACTIVITY", "PAUSE");
        super.onPause();
        stopRepeatingTask();
    }*/
	@Override
    protected void onStop() {
		Log.d("MAPACTIVITY", "STOP");
        super.onStop();
        stopRepeatingTask();
    }
	@Override
    protected void onDestroy() {
		Log.d("MAPACTIVITY", "DESTROY");
        super.onDestroy();
        stopRepeatingTask();
        gps.stopUsingGPS();
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
	
	public void viewAll(View view) {
		// recompute bounding box for everyone
		boolean foundvaliduser = false;
		if (locations.size() > 0) {
			for (int i = 0; i < locations.size(); i++) {
				if (validusers.get(i)) {
					if (!foundvaliduser) {
						foundvaliduser = true;
						bounds = new LatLngBounds(locations.get(i), locations.get(i));
					}
					else
						bounds = bounds.including(locations.get(i));
				}
			}
			if (foundvaliduser)	moveCameraView();
			else {
				Log.e("MAPACTIVITY", "no valid user locations");
				moveCameraView(cur_location.getLatitude(), cur_location.getLongitude());
			}
		}
		else {
			Log.e("MAPACTIVITY", "no locations");
			moveCameraView(cur_location.getLatitude(), cur_location.getLongitude());
		}
	}
	public void viewTrails(View view) {
		if (!SHOW_TRAILS) { // show
			traillines.clear();
			boolean foundvaliduser = false;
			for (int i = 0; i < trails.size(); i++) {
				if (validusers.get(i)) {
					foundvaliduser = true;
					Polyline pline = mMap.addPolyline(trails.get(i));
					pline.setColor(trailhues.get(i));
					pline.setWidth(5);
					traillines.add(pline);
				}
			}
			if (foundvaliduser) SHOW_TRAILS = true; // toggle
			else Log.e("MAPACTIVITY", "found no valid users for showing trails");
		}
		else { // turn off
			for (int i = 0; i < traillines.size(); i++) 
				traillines.get(i).remove();
			traillines.clear();
			SHOW_TRAILS = false; // toggle
		}
	}
	private void updateTrails() {
		if (SHOW_TRAILS) {
			for (int i = 0; i < traillines.size(); i++) 
				traillines.get(i).remove();
			traillines.clear();
			boolean foundvaliduser = false;
			for (int i = 0; i < trails.size(); i++) {
				if (validusers.get(i)) {
					foundvaliduser = true;
					Polyline pline = mMap.addPolyline(trails.get(i));
					pline.setColor(trailhues.get(i));
					pline.setWidth(5);
					traillines.add(pline);
				}
			}
			if (foundvaliduser) SHOW_TRAILS = true; // toggle
			else Log.e("MAPACTIVITY", "found no valid users for updating trails");
		}
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
        	cur_location = gps.getLocation();
            // send location to database
            if (email != null && token != null)
            	updateMyLocation();
            else 
            	Log.e("USERINFO", "email/token NULL");
        }else{
            Log.e("GPS", "can't get location");
            // can't get location: GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
        updateAllLocations();
        moveCameraView(cur_location.getLatitude(), cur_location.getLongitude());
    	
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setZoomControlsEnabled(false);
        mMap.setOnCameraChangeListener(new OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition arg0) {
                // Move camera.
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80));
                // Remove listener to prevent position reset on camera move.
                mMap.setOnCameraChangeListener(null);
            }
        });
    }
	
	/* focus view on (lat, lng) */
	private void moveCameraView(double lat, double lng) {
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15);
        mMap.moveCamera(cameraUpdate);
	}
	/* view all locations */
	private void moveCameraView() {
		if (bounds != null) 
			mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
	}
	
	/* compute latlngbounds from users */
	private void computeBounds(int[] users) {
		boolean first = true;
		int index;
		for (int i = 0; i < users.length; i++) {
			index = userToIndex.get(users[i], -1);
			if (index > 0) {
				if (first) {
					bounds = new LatLngBounds(locations.get(index), locations.get(index));
					first = false;
				}
				else
					bounds.including(locations.get(index));
			}
			else
				Log.e("MAPACTIVITY", "cannot find user " + users[i] + " in stored group info");
		}
	}
	
	/* update all locations from database */
	private void updateAllLocations() {
		// query from database 
    	JSONObject jsonret = userFunctions.retrieveAllLocations(email, token, groupid);
    	try {
    		if (jsonret.getString(KEY_STATUS) != null && 
    				VAL_SUCCESS.compareToIgnoreCase(jsonret.get(KEY_STATUS).toString()) == 0) {
    			//Log.d("MAPACTIVITY", "successful location retrieval");
    			int nusers = 0;
    			if (jsonret.getString(KEY_NUSERS) != null) { 
    				nusers = jsonret.getInt(KEY_NUSERS);
    			}
    			else {
    				Log.e("MAPACTIVITY", "cannot find " + KEY_NUSERS);
    			}
    			double lat = 0, lng = 0;
    			String name = " ";
    			int userid = 0;
    			float colour;
    			// clear markers
    			for (int m = 0; m < markers.size(); m++)
    				markers.get(m).remove();
    			/*markers.clear();
    			locations.clear();
    			userToIndex.clear();*/
    			for (int i = 0; i < validusers.size(); i++)
    				validusers.set(i,  false);
				bounds = new LatLngBounds(new LatLng(cur_location.getLatitude(), cur_location.getLongitude()), 
										  new LatLng(cur_location.getLatitude(), cur_location.getLongitude()));
    			// loop through all locations
				int index;
    			for (int u = 0; u < nusers; u++) {
    				JSONObject jsonuser = null;
    				if (jsonret.getString(Integer.toString(u)) != null) {
    						jsonuser = jsonret.getJSONObject(Integer.toString(u));
        				if (jsonuser.getString(KEY_LAT) != null) { lat = jsonuser.getDouble(KEY_LAT); }
        				if (jsonuser.getString(KEY_LONG) != null) { lng = jsonuser.getDouble(KEY_LONG); }
        				if (jsonuser.getString(KEY_NAME) != null) { name = jsonuser.get(KEY_NAME).toString(); }
        				if (jsonuser.getString(KEY_UID) != null) { userid = jsonuser.getInt(KEY_UID); } 
        				if (userid != uid) { colour = OTHERS_COLOUR; }
        				else               { colour = SELF_COLOUR;   }
        				//Log.d("RETRIEVE", name + " at (" + lat + ", " + lng + ")");
                        bounds = bounds.including(new LatLng(lat, lng));
        				Marker m = mMap.addMarker(new MarkerOptions()
        										.position(new LatLng(lat, lng))
        										.title(name)
        										.icon(BitmapDescriptorFactory.defaultMarker(colour)));
        				m.setVisible(true);
        				if (userid == uid) m.showInfoWindow();
        				index = userToIndex.get(userid, -1);
        				if (index < 0)  { // new user
                            index = validusers.size();
                            if (index < MAX_SIZE) {
	                            userToIndex.put(userid, index);
	            				markers.add(m);
	            				locations.add(new LatLng(lat, lng));
	            				validusers.add(true);
	                            PolylineOptions polyoptions = new PolylineOptions().add(new LatLng(lat, lng));
	                            trails.add(polyoptions);
	                            hsvcolour[0] = nextcolour;
	                            trailhues.add(Color.HSVToColor(hsvcolour));
	                            nextcolour = (nextcolour + COLOUR_INCREMENT) % 360;
                            }
                        }
                        else {
                        	Location.distanceBetween(locations.get(index).latitude, locations.get(index).longitude,
                        			lat, lng, results);
                        	if (results[0] > mindist) 
                        		trails.get(index).add(new LatLng(lat, lng)); // only add to trail if sufficiently far from last recorded location
                            markers.set(index, m);
                            locations.set(index,  new LatLng(lat, lng));
                            validusers.set(index, true);
                        }
    				}
    			}
    		}
    	}catch (JSONException e) {
    		Log.e("MAPACTIVITY", "updateAllLocations(): JSON exception");
            e.printStackTrace();
        } catch (Exception e) {
        	Log.e("MAPACTIVITY", "updateAllLocations(): Exception");
        	Log.e("MAPACTIVITY", e.getMessage());
        }
	}
	
	private void updateMyLocation() {
		JSONObject jsonupdateloc = userFunctions.updateLocation(email, token, cur_location.getLatitude(), cur_location.getLongitude());
    	try {
    		if (jsonupdateloc.getString(KEY_STATUS) != null && 
    				VAL_SUCCESS.compareToIgnoreCase(jsonupdateloc.get(KEY_STATUS).toString()) == 0) { ; }
    			//Log.d("MAPACTIVITY", "successful location update");
    	}catch (JSONException e) {
    		Log.e("MAPACTIVITY", "updateMyLocation(): JSON exception");
            e.printStackTrace();
        } catch (Exception e) {
        	Log.e("MAPACTIVITY", "updateMyLocation(): Exception");
        	Log.e("MAPACTIVITY", e.getMessage());
        }
	}
	
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
}


