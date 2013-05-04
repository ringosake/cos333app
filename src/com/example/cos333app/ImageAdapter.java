package com.example.cos333app;

import library.UserFunctions;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;

public class ImageAdapter extends BaseAdapter {
	private static final int SELECT_PHOTO = 100;
	private Context mContext;
	private EditText group;
	private EditText picURL;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            view = new ImageView(mContext);
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view=inflater.inflate(R.layout.gridobj, parent, false);
        } else {
            view = convertView;
        }
        if (position==(mThumbIds.length - 1)) { // if we're at a position beyond all the images?
        	Context context = parent.getContext();
            
            int image = 0;
            ImageButton button2 = new ImageButton(context);
            image = R.drawable.plus;
            button2.setImageResource(image);
            
            
            // new, broken stuff below here.
            button2.setOnClickListener(new View.OnClickListener() {
            	public void onClick(View arg0) {
                    // create group. launch new activity for this?
            		// take name via text box
            		String userid = "4"; // FIX THIS HOW DO I GET THIS
            		ImageAdapter.this.group = new EditText(arg0.getContext());
            		group.setHint("Enter group name");
            		ImageAdapter.this.picURL = new EditText(arg0.getContext());
            		picURL.setHint("Enter image URL");
            		// create button to save new group name
            		Button accept = new Button(arg0.getContext());
            		accept.setText("Save group");
            		
            		accept.setOnClickListener(new ImageAdapter.MyClickListener(group.getText().toString(), picURL.getText().toString(), userid));
            		// save group name when button is pressed
            		
            		// select image
            		//Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            		//photoPickerIntent.setType("image/*");
            		//startActivityForResult(photoPickerIntent, SELECT_PHOTO); 
                }
            });	
          
            return button2;
        }	
        ImageView imageView = (ImageView)view.findViewById(R.id.imagepart);
        imageView.setImageResource(mThumbIds[position]);
        imageView.setMaxHeight(view.getWidth());
        imageView.setMinimumHeight(view.getWidth());
        TextView textView = (TextView)view.findViewById(R.id.textpart1);
        textView.setText(String.valueOf(position));
        textView = (TextView)view.findViewById(R.id.textpart2);
        textView.setText(String.valueOf(position));
        return view;
    }

    
    // doesn't seem to work
	private class MyClickListener implements OnClickListener {
		private String groupName;
		private String picURL;
		private String userID;
		
		public MyClickListener(String groupName, String picURL, String userID) {
			this.groupName = groupName;
			this.picURL = picURL;
			this.userID = userID;
		}
		
		public void onClick(View v) {
			// save the group somehow.
			UserFunctions userFunctions = new UserFunctions();
			JSONObject json = userFunctions.createGroup(userID, groupName, picURL);
		}
	}

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
    };
}