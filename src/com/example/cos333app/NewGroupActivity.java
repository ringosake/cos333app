package com.example.cos333app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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
	private String groupID = "12"; //TODO: get the real one somehow
	TextView errorMsg;
	private Thread thread; 
	// for contact autocomplete
	MultiAutoCompleteTextView contactView;
	Map<String, String>[] people=null;
	public ArrayList<ContactMap> mPeopleList = new ArrayList<ContactMap>();
	private ArrayList<String> mNames = new ArrayList<String>();
	private SimpleAdapter mAdapter;
	boolean[] isSelected;
	
	class ContactMap extends HashMap<String, String> { 
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public String toString() { 
			return this.get("Name") + " <" + this.get("Phone") + ">"; 
		} 
	}
	
	public void readContacts() {
	    ContentResolver cr = getContentResolver();
	    Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
	           null, null, null, null);
	    
	    if (cur.getCount() > 0) {
	       while (cur.moveToNext()) {
	           String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
	           String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
	           if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
	               // get the phone number
	               Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
	                                      ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
	                                      new String[]{id}, null);
	               while (pCur.moveToNext()) {
	                     String phone = pCur.getString(
	                            pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
	                     String numberType = pCur.getString(pCur.getColumnIndex(
	                    		 ContactsContract.CommonDataKinds.Phone.TYPE));
	                     
	                     ContactMap NamePhoneType = new ContactMap();
	
	                     NamePhoneType.put("Name", name);
	                     NamePhoneType.put("Phone", phone);
	
	                     if(numberType.equals("0"))
	                    	 NamePhoneType.put("Type", "Work");
	                     else if(numberType.equals("1"))
	                    	 NamePhoneType.put("Type", "Home");
	                     else if(numberType.equals("2"))
	                    	 NamePhoneType.put("Type",  "Mobile");
	                     else
	                    	 NamePhoneType.put("Type", "Other");
	
	                     //Then add this map to the list.
	                     mPeopleList.add(NamePhoneType);
	                     mNames.add(name);
	               }
	               pCur.close();
	           }
	       }
	    }
	    cur.close();
	    //startManagingCursor(cur);
	    isSelected = new boolean[mPeopleList.size()];
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
		errorMsg= (TextView) findViewById(R.id.group_error); 
		// get user
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		this.email = prefs.getString("app_email", null);
		this.token = prefs.getString("app_token", null);
        
		// multiautocomplete
        contactView = (MultiAutoCompleteTextView) findViewById(R.id.multiautocomp_contacts);
        contactView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        
        readContacts();
        
        mAdapter = new SimpleAdapter(this, mPeopleList, R.layout.custautocomplete ,new String[] { "Name", "Phone" , "Type" }, new int[] { R.id.ccontName, R.id.ccontNo, R.id.ccontType });
        contactView.setThreshold(1);
        contactView.setAdapter(mAdapter);
        
        
		this.uf = new UserFunctions();
		this.image = (ImageView) findViewById(R.id.imageView1);
		
		// buttons
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
	
	private String[] getInvitees() {
		ArrayList<String> numbers = new ArrayList<String>();
		// add the ones with isSelected == true
		for (int i = 0; i < isSelected.length; i++)
			if (isSelected[i])
				numbers.add(PhoneNumberUtils.extractNetworkPortion(mPeopleList.get(i).get("Phone")));
		
		String[] invitees = contactView.getText().toString().split(",\\s*"); //
		for (String name : invitees) {
			// get that contact's number(s)
			int index = mNames.indexOf(name);
			// found the name
			if (index >= 0) {
				// invite if not already invited
				int tmp = index;
				while (tmp < mPeopleList.size()) {
					Map<String, String> obj = mPeopleList.get(tmp);
					String foundName = obj.get("Name");
					if (!foundName.equals(name)) {
						numbers.add(mPeopleList.get(index).get("Phone"));
						break;
					}
					if (isSelected[tmp]) break;
					tmp++;
				}
				if (tmp == mPeopleList.size())
					numbers.add(PhoneNumberUtils.extractNetworkPortion(mPeopleList.get(index).get("Phone")));
			} else {
				String inputAsPhone = PhoneNumberUtils.convertKeypadLettersToDigits(name);
				inputAsPhone = PhoneNumberUtils.stripSeparators(inputAsPhone); 
				if (PhoneNumberUtils.isWellFormedSmsAddress(inputAsPhone))
					numbers.add(inputAsPhone);
			}
		}
		return (String[])numbers.toArray();
	}
	
	public void inviteMembers(int groupId, String[] invitees) {
		// post it all
		JSONObject grpJson = uf.inviteMembers(email, token, groupId, invitees);
		try {
			Log.d("toString", grpJson.toString());
			String ourStatus = "";
			if (grpJson.has(STATUS)) {
				ourStatus = grpJson.getString(STATUS);
			}
			//Log.d("JSON status", ourStatus);
			
			if (!ourStatus.equals(ERROR)) {
				return;
			} else {
				this.show("Failure to invite members.");
				thread=  new Thread(){
			        @Override
			        public void run(){
			            try {
			                synchronized(this){
			                    wait(3000);
			                }
			            }
			            catch(InterruptedException ex){                    
			            }

			            // TODO              
			        }
			    };

			    thread.start();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// print message?
		}
	
	}
	
	public void confirmGroup() {
 	    //UserFunctions userFunctions = new UserFunctions();
 	    // pull the strings from the edittexts. send groupname to server. get picture using url.
		
		if (email == null || token == null) {
			Log.e("USERINFO", "email / token NULL");
			// end activity due to login failure. print an error message?
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}
		// some invitees are not valid.
		String[] invitees = getInvitees();
		if (invitees == null)
			return;
		
		JSONObject grpJson = uf.createGroup(email, token, groupName.getText().toString(), picURL.getText().toString());
		try {
			Log.d("toString", grpJson.toString());
			String ourStatus = "";
			if (grpJson.has(STATUS)) {
				ourStatus = grpJson.getString(STATUS);
			}
			//Log.d("JSON status", ourStatus);
			
			if (!ourStatus.equals(ERROR)) {
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
				
				inviteMembers(Integer.parseInt(grpJson.getString("group_id")), invitees);
				
				// then end the activity, returning to MainActivity
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
				finish();
			} else {
				// Inform user that group creation has failed?
				this.show("Group creation failed. Please try again.");
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
	
	/*
	public void btnLoadImageClick(View view)
    {
    	//imgLogo.setBackgroundDrawable(LoadImageFromWeb("http://www.android.com/media/wallpaper/gif/android_logo.gif"));
    	new loadImageTask().execute("http://www.android.com/media/wallpaper/gif/android_logo.gif");
    }
    
    public class loadImageTask extends AsyncTask<String, Void, Void>
    {
    	Drawable imgLoad;
    	
    	@Override
    	protected void onPreExecute() {
    		// TODO Auto-generated method stub
    		super.onPreExecute();
    		
    		progressbar.setVisibility(View.VISIBLE);
    	}
    	
    	@Override
    	protected Void doInBackground(String... params) {
    		// TODO Auto-generated method stub
    		
    		imgLoad = LoadImageFromWeb(params[0]);
			return null;
    	}
    	
    	@Override
    	protected void onPostExecute(Void result) {
    		// TODO Auto-generated method stub
    		super.onPostExecute(result);
    		
    		if(progressbar.isShown())
    		{
    			progressbar.setVisibility(View.GONE);
    			imgLogo.setVisibility(View.VISIBLE);
    			imgLogo.setBackgroundDrawable(imgLoad);
    		}
    	}
    }
    
    public static Drawable LoadImageFromWeb(String url) 
    {
        try 
        {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    } */
	
	
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
				 			+ File.separator + "group_logos" + File.separator + groupID + ".jpg"); // name these dynamically
	    	Log.d("filez", Environment.getExternalStorageDirectory().toString());
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

    /**
     * This method is a hook for background threads and async tasks that need to update the UI.
     * It does this by launching a runnable under the UI thread.
     */
    public void show(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	errorMsg.setText(message);
            }
        });
    }
}
