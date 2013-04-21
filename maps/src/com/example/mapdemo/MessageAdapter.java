package com.example.cos333app;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MessageAdapter extends ArrayAdapter<String> {
	private ArrayList<String> values;
	private final Context context;

	public MessageAdapter(Context context, int textViewResourceId, ArrayList<String> values) {
		super(context, textViewResourceId, values);
		this.context = context;
		this.values = values;
	}

	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.activity_message, parent, false);
		}
		TextView textView = (TextView) rowView.findViewById(R.id.messageText);
		textView.setText(values.get(position));
		return rowView;
    }

}
