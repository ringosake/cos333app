package com.example.cos333app;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
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
            File file = new File(fileStump + File.separator + "group_logos" + File.separator);
            File[] thePics = file.listFiles();
            File fake = new File(fileStump + File.separator + "MyDownloadedImage.jpg");
            this.grpPics = new File[thePics.length + 1]; 
            
            // we want an array with one extra element - we need one spot for each image file and one extra for the 
            // image button at the end.
            for (int i = 0; i < thePics.length; i++)
            {
            	grpPics[i] = thePics[i];
            }
            grpPics[grpPics.length - 1] = fake;
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

    protected void onCreate(Bundle savedInstanceState) {
    	
    }
    
    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        view = new View(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        
       // if (position==(mThumbIds.length - 1)) { // if we're at a position beyond all the images?
        if (position == grpPics.length - 1) { //TODO: Check for off by 1 error
            view=inflater.inflate(R.layout.gridobj_plus, parent, false);
            return view;
        }
        view=inflater.inflate(R.layout.gridobj, parent, false);
        ImageView imageView = (ImageView)view.findViewById(R.id.imagepart);
        //imageView.setImageResource(mThumbIds[position]);
        Bitmap bmp = BitmapFactory.decodeFile(grpPics[position].toString());
        BitmapDrawable drawpic = new BitmapDrawable(bmp);
        imageView.setImageDrawable(drawpic);
        TextView textView = (TextView)view.findViewById(R.id.textpart1);
        textView.setText(String.valueOf(position));
        textView = (TextView)view.findViewById(R.id.textpart2);
        textView.setText(String.valueOf(position));
        return view;
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