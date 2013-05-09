package com.example.cos333app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.io.OutputStream;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import library.UserFunctions;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class NewGroupActivity extends Activity {

	private String email; // FIGURE THIS OUT
	private String token;
	private DownloadImageTask task; // IS THIS OK?
	private EditText groupName;
	private EditText picURL;
	private Button btnConfirmGroup;
	private Button btnCancelGroup;
	UserFunctions uf;
	private static String STATUS = "status";
	private static String ERROR = "error";
	ProgressBar progressbar;
	ImageView imgLogo;
	private ImageView image;
	private String groupID; //= "12"; //TODO: get the real one somehow
	
	// for contact autocomplete
	MultiAutoCompleteTextView contactView;
	public ArrayList<String> c_Name = new ArrayList<String>();
	public ArrayList<ArrayList<String>> c_Number = new ArrayList<ArrayList<String>>();
	String[] name_Val=null;
	
	public void readContacts(){
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
               null, null, null, null);
        
        if (cur.getCount() > 0) {
           while (cur.moveToNext()) {
               String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
               String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
               if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
            	   c_Name.add(name);
            	   ArrayList<String> nums = new ArrayList<String>();
                   // get the phone number
                   Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                          ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                                          new String[]{id}, null);
                   while (pCur.moveToNext()) {
                         String phone = pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                         nums.add(phone);
                   }
                   c_Number.add(nums);
                   pCur.close();
               }
           }
      }
   }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.task = new DownloadImageTask();
	    // Get the layout inflater
		setContentView(R.layout.dialog_makegroup);
		this.btnConfirmGroup = (Button) findViewById(R.id.btnConfirmGroup);
		this.btnCancelGroup = (Button) findViewById(R.id.btnCancelGroup);	
		this.groupName = (EditText) findViewById(R.id.groupName);
		this.picURL = (EditText) findViewById(R.id.picURL);
		// get user
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		this.email = prefs.getString("app_email", null);
		this.token = prefs.getString("app_token", null);
        
		// multiautocomplete
        contactView = (MultiAutoCompleteTextView) findViewById(R.id.multiautocomp_contacts);
        contactView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        
        readContacts();
        
        name_Val = (String[]) c_Name.toArray(new String[c_Name.size()]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, name_Val);
        contactView.setThreshold(1);
        contactView.setAdapter(adapter);
        
		this.uf = new UserFunctions();
		this.image = (ImageView) findViewById(R.id.imageView1);
		
		btnConfirmGroup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	NewGroupActivity.this.confirmGroup();
            }
        });
		
		btnCancelGroup.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				NewGroupActivity.this.cancelGroup();
			}
		});
	}
	
	public void confirmGroup() {
 	    // pull the strings from the edittexts. send groupname to server. get picture using url.
		
		if (email == null || token == null) {
			Log.e("USERINFO", "email / token NULL");
			// end activity due to login failure. print an error message?
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}
		JSONObject grpJson = uf.createGroup(email, token, groupName.getText().toString(), picURL.getText().toString());
		try {
			Log.d("toString", grpJson.toString());
			String ourStatus = "";
			if (grpJson.has(STATUS)) {
				ourStatus = grpJson.getString(STATUS);
			}
			if (grpJson.has("group_id")) {
				this.groupID = grpJson.getString("group_id");
			}
			//Log.d("JSON status", ourStatus);
			
			// if (!ourStatus.equals(ERROR)) {
			if (true) {
				Bitmap picture = this.task.doInBackground(picURL.getText().toString());
				picture = Bitmap.createScaledBitmap(picture, 160, 160, true);
				BitmapDrawable drawpic = new BitmapDrawable(getResources(), picture);
				this.image.setImageDrawable(drawpic);
				
				this.task.onPostExecute(picture);
				Log.d("after task", "Finished task.OnPostExecute");
				groupName.setText("");
				groupName.setHint("Enter group name");
				picURL.setText("");
				picURL.setHint("Enter image URL");
 	   
				// then end the activity, returning to MainActivity
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
				finish();
			} else {
				// Inform user that group creation has failed?
				// this.show("Group creation failed. Please try again.");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// print message?
		}
	}
	
	public void cancelGroup() {
	    // get the EditTexts and make sure they are cleared of content
  	    groupName.setText("");
	    groupName.setHint("Enter group name");
	    picURL.setText("");
	    picURL.setHint("Enter image URL");
 	   
 	   // then end the activity, returning to MainActivity
	    Intent intent = new Intent(this, MainActivity.class);
	    startActivity(intent);
	    finish();
	}
	
	// get image from URL
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	    FileOutputStream fos;
			 
	    protected Bitmap doInBackground(String... urls) {
	        String urldisplay = urls[0];
	        Bitmap mIcon11 = null;
	        try {
	            InputStream in = new java.net.URL(urldisplay).openStream();
	            mIcon11 = BitmapFactory.decodeStream(in);
	        } catch (Exception e) {
	            Log.e("Error", e.getMessage());
	            e.printStackTrace();
	        }
	        Log.d("downloaded", "Successfully downloaded file");
	        return mIcon11;
	    }

	    protected void onPostExecute(Bitmap bmp) {
	    	// Do your staff here to save image
	    	// --- this method will save your downloaded image to SD card ---
	    	
	    	//ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	    	OutputStream fOut = null;
	    	
	    	//--- you can select your preferred CompressFormat and quality. 
	    	//  I'm going to use JPEG and 100% quality ---
	    	//bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
	    	//--- create a new file on SD card ---
	    	File file = new File(Environment.getExternalStorageDirectory() // change code above to refer to this dir
				 			+ File.separator + "group_logos" + File.separator + NewGroupActivity.this.email + File.separator + groupID + ".jpg"); // name these dynamically
	    	Log.d("filez", Environment.getExternalStorageDirectory().toString());
	    	if (!file.exists()) {
	    		try {
	    			file.createNewFile();
	    		} catch (IOException e) {
	    			e.printStackTrace();
	    		}
	    	}
	    	try {
	    		fOut = new FileOutputStream(file);
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    	bmp.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
	    	/*
	    	try {
	    		file.createNewFile();
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    	//--- create a new FileOutputStream and write bytes to file ---
	    	try {
	    		FileOutputStream fos = new FileOutputStream(file);
	    	} catch (FileNotFoundException e) {
	    		e.printStackTrace();
	    	}*/
	    	Log.d("make file", "Got past the attempt at making a new file");
	    	/*
	    	try {
	    		fos.write(bytes.toByteArray());
	    		fos.close();
	    		//Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show();
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	} */
	    	Log.d("write file", "Got past the attempt to write the data into the new file");

		    return;
	    }
    } 
	
}
