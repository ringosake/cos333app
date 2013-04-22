package com.example.cos333app;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class MessageActivity extends Activity {
	EditText mEdit;
	Button mButton;
	ListView lv;
	ArrayList<String> messages;
	private ArrayAdapter<String> adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		
		mButton = (Button)findViewById(R.id.buttonSendMessage);
		mEdit = (EditText)findViewById(R.id.editMessage);
		lv = (ListView)findViewById(R.id.messageList);
		
		messages = new ArrayList<String>(); // LOAD WITH MESSAGES IN THE DATABASE!!!!
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messages);
		lv.setAdapter(adapter);
		
	    mButton.setOnClickListener(
	        new View.OnClickListener()
	        {
	            public void onClick(View view)
	            {
	                String message = mEdit.getText().toString();
	                mEdit.setText("");
	                messages.add(message);
	                adapter.notifyDataSetChanged();
	            }
	        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.message, menu);
		return true;
	}

}
