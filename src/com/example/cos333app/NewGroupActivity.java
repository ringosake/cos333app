package com.example.cos333app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;

import library.UserFunctions;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class NewGroupActivity extends Activity {

	private String userID = "11"; // FIGURE THIS OUT
	//private DownloadImageTask task; // IS THIS OK?
	private EditText groupName;
	private EditText picURL;
	private Button btnConfirmGroup;
	private Button btnCancelGroup;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//this.task = new DownloadImageTask();
	    // Get the layout inflater
		setContentView(R.layout.dialog_makegroup);
		this.btnConfirmGroup = (Button) findViewById(R.id.btnConfirmGroup);
		this.btnCancelGroup = (Button) findViewById(R.id.btnCancelGroup);	
		this.groupName = (EditText) findViewById(R.id.groupName);
		this.picURL = (EditText) findViewById(R.id.picURL);
		
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
	    // LayoutInflater inflater = getActivity().getLayoutInflater();

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    // builder.setView(inflater.inflate(R.layout.dialog_makegroup, null))
	    // Add action buttons
	    //       .setPositiveButton(R.string.create_group, new DialogInterface.OnClickListener() {
	    //           @Override
	    //           public void onClick(DialogInterface dialog, int id) {
	                   // sign in the user ...
	            	   /*Activity thisActivity = getActivity();
	            	   EditText groupName = (EditText) thisActivity.findViewById(R.id.groupName);
	            	   EditText picURL = (EditText) thisActivity.findViewById(R.id.picURL);
	            	   
	            	   UserFunctions userFunctions = new UserFunctions();
	            	   // pull the strings from the edittexts. send groupname to server. get picture using url.
	       			   JSONObject json = userFunctions.createGroup(userID, groupName.getText().toString(), picURL.getText().toString());
	       			   Bitmap picture = NewGroupDialogFragment.this.task.doInBackground(picURL.getText().toString()); 
	       			   */
	    //           }
	    //       })
	    //       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	    //           public void onClick(DialogInterface dialog, int id) {
	                   // LoginDialogFragment.this.getDialog().cancel();
	            	   /*
	            	   // get the EditTexts and make sure they are cleared of content
	            	   Activity thisActivity = getActivity();
	            	   EditText groupName = (EditText) thisActivity.findViewById(R.id.groupName);
	            	   EditText picURL = (EditText) thisActivity.findViewById(R.id.picURL);
	            	   
	            	   groupName.setText("");
	       			   groupName.setHint("Enter group name");
	       			   picURL.setText("");
	       			   picURL.setHint("Enter image URL");
	            	   
	            	   // then end the dialog
	            	   NewGroupDialogFragment.this.getDialog().cancel();*/
	      //         }
	      //     });      
	    //return builder.create();

		
	}
	
	public void confirmGroup() {
 	    //UserFunctions userFunctions = new UserFunctions();
 	    // pull the strings from the edittexts. send groupname to server. get picture using url.
		//JSONObject json = UserFunctions.createGroup(userID, groupName.getText().toString(), picURL.getText().toString());
		//Bitmap picture = NewGroupDialogFragment.this.task.doInBackground(picURL.getText().toString());
		
		groupName.setText("");
	    groupName.setHint("Enter group name");
	    picURL.setText("");
	    picURL.setHint("Enter image URL");
 	   
 	   // then end the activity, returning to MainActivity
	    Intent intent = new Intent(this, MainActivity.class);
	    startActivity(intent);
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
	}
	
	// get image from URL
    /* private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
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
	        return mIcon11;
	    }

	    protected void onPostExecute(Bitmap bmp) {
	    	// Do your staff here to save image
	    	// --- this method will save your downloaded image to SD card ---
	    	ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	    	//--- you can select your preferred CompressFormat and quality. 
	    	//  I'm going to use JPEG and 100% quality ---
	    	bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
	    	//--- create a new file on SD card ---
	    	File file = new File(Environment.getExternalStorageDirectory() // change code above to refer to this dir
				 			+ File.separator + "myDownloadedImage.jpg"); // name these dynamically
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
	    	}
	    	try {
	    		fos.write(bytes.toByteArray());
	    		fos.close();
	    		//Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show();
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}

		        		
	    }
    } */
	
}