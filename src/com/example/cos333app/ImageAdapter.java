package com.example.cos333app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;

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
          
            return button2;
        }
        RelativeLayout gridobj = (RelativeLayout)view.findViewById(R.id.gridobj_wrapper);
        gridobj.setLayoutParams(new LayoutParams(gridobj.getMeasuredWidth(), gridobj.getMeasuredWidth()));
        ImageView imageView = (ImageView)view.findViewById(R.id.imagepart);
        imageView.setImageResource(mThumbIds[position]);
        TextView textView = (TextView)view.findViewById(R.id.textpart1);
        textView.setText(String.valueOf(position));
        textView = (TextView)view.findViewById(R.id.textpart2);
        textView.setText(String.valueOf(position));
        return view;
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