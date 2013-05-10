package com.example.cos333app;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private File[] grpPics;

    public ImageAdapter(Context c) {
        mContext = c;
        // all below this point is very sketchy
        String state = Environment.getExternalStorageState();
        if(state.contentEquals(Environment.MEDIA_MOUNTED) || state.contentEquals(Environment.MEDIA_MOUNTED_READ_ONLY)) 
        {
            File homeDir = Environment.getExternalStorageDirectory();
            String fileStump = homeDir.toString();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
    		String email = prefs.getString("app_email", null);
    		String userName = email.replace("@", "");
    		userName = userName.replace(".", "");
    		
            File file = new File(fileStump + File.separator + "group_logos" + File.separator + email);
            if (!file.exists()) {
            	file.mkdirs();
            }
            File[] thePics = file.listFiles();
            File fake = new File(fileStump + File.separator + "MyDownloadedImage.jpg"); // dummy file to add to the array we feed to gridview
            if (thePics != null) {
            	this.grpPics = new File[thePics.length + 1];
            	Arrays.sort(thePics, lastModified);
            	for (int j = 0; j < thePics.length; j++) {
            		Log.d(Integer.toString(j) + "PIC", thePics[j].toString());
            	}
            } else {
            	this.grpPics = new File[1];
            }
            
            // call server to check for changes in group URL?
            // String [] groups = UserFunctions.getMembershipsByUser();
            
            
            // we want an array with one extra element - we need one spot for each image file and one extra for the 
            // image button at the end.
            if (thePics != null) {
            	for (int i = 0; i < thePics.length; i++)
            	{
            		grpPics[i] = thePics[i];
            	}
            }
            grpPics[grpPics.length - 1] = fake; // if we are at the end of the array, gridview will draw the add group button instead of
            									// this fake file
            
        } 
        else 
        {
            Log.v("Error", "External Storage Unaccessible: " + state);
        }
    }

    public int getCount() {
        //return mThumbIds.length;
    	return grpPics.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // we probably don't want/need this
    protected void onCreate(Bundle savedInstanceState) {
    	
    }
    
    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        view = new View(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String userID = prefs.getString("app_uid", null);
        String email = prefs.getString("app_email", null);
        String info = prefs.getString(email + "_memberships", null);
        String groupName = null;
        String groupID = null;
        
        try {
        	//Log.d("info", info);
        	//Log.d("pos", Integer.toString(position));
        	JSONObject json = new JSONObject(info);
        	int jsonPosition = 4 + position;
        	JSONObject curr = json.getJSONObject(Integer.toString(jsonPosition));
        	groupName = curr.getString("group_name");
        	groupID = curr.getString("group_id");
        } catch (JSONException e) {
        	e.printStackTrace();
        }
        String userName = email.replace("@", "");
        userName = userName.replace(".", "");
        File file = new File(Environment.getExternalStorageDirectory() // change code above to refer to this dir
	 			+ File.separator + "group_logos" + File.separator + userName + File.separator + groupID + ".jpg");
        
       // if (position==(mThumbIds.length - 1)) { // if we're at a position beyond all the images?
        if (position == grpPics.length - 1) { //TODO: Check for off by 1 error
            view=inflater.inflate(R.layout.gridobj_plus, parent, false);
            return view;
        }
        view=inflater.inflate(R.layout.gridobj, parent, false);
        ImageView imageView = (ImageView)view.findViewById(R.id.imagepart);
        //imageView.setImageResource(mThumbIds[position]);
        //Bitmap bmp = BitmapFactory.decodeFile(grpPics[position].toString());
        Bitmap bmp = BitmapFactory.decodeFile(file.toString());
        BitmapDrawable drawpic = new BitmapDrawable(bmp);
        imageView.setImageDrawable(drawpic);
        TextView textView = (TextView)view.findViewById(R.id.textpart1);
        // textView.setText(String.valueOf(position));
        textView.setText(groupName);
        textView = (TextView)view.findViewById(R.id.textpart2);
        textView.setText(String.valueOf(position));
        // set onClick for view?
        
        return view;
    }
    
    /**
     * Sorts an array of files based on order modified. Should put them into queue order (check for wrongness).
     * See http://www.theeggeadventure.com/wikimedia/index.php/Java_File.listFiles_order
     */
    private static final Comparator<File> lastModified = new Comparator<File>() {
		@Override
		public int compare(File o1, File o2) {
			return o1.lastModified() == o2.lastModified() ? 0 : (o1.lastModified() > o2.lastModified() ? 1 : -1 ) ;
		}
	};
	public void testFileSort() throws Exception {
		File[] files = new File(".").listFiles();
		Arrays.sort(files, lastModified);
		System.out.println(Arrays.toString(files));
	}
    
    /*
    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            R.drawable.sample_0, R.drawable.sample_1,
            R.drawable.sample_2, R.drawable.sample_3,
            R.drawable.sample_4, R.drawable.sample_5,
            R.drawable.sample_6, R.drawable.sample_7,
            -1 // a placeholder. this will need to be changed when we can actually add groups.
            //R.drawable.totoro
    };*/
}