package com.example.cos333app;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
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
	private int updatetime = 5000; // 5 seconds
	private Handler handler;
	private String group = "11"; //TODO: get correct group
	private long lastMessage; // the last time we got a message from the server
	private int messageLimit = 1000; // store no more than 1k msgs locally
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		
		mButton = (Button)findViewById(R.id.buttonSendMessage);
		mEdit = (EditText)findViewById(R.id.editMessage);
		lv = (ListView)findViewById(R.id.messageList);
		
		messages = new ArrayList<String>(); // LOAD WITH MESSAGES IN THE DATABASE!!!!
		// load with messages from sharedPreferences
		loadLocalMessages(this);
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
	                // send to server
	            }
	        });
	    startRepeatingTask();
	}
	
	/**
	 * Load the messages currently stored in SharedPreferences into the messages ArrayList.
	 * Cf http://stackoverflow.com/questions/7057845/save-arraylist-to-sharedpreferences
	 * @param mContext
	 */
	public void loadLocalMessages(Context mContext) {  
	    SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(mContext);
	    messages.clear();
	    int size = mSharedPreference1.getInt("Status_size", 0);  

	    for(int i = 0; i < size; i++) {
	        messages.add(mSharedPreference1.getString("Status_" + i, null));  
	    }
	}
	
	/**
	 * Save the currently loaded messages in SharedPreferences so they will persist after activity ends
	 * @return
	 */
	public boolean saveMessages(Context mContext)	{ //TODO: make this run when activity closes
	     SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
	     SharedPreferences.Editor mEdit1 = sp.edit();
	     mEdit1.putInt("Status_size", messages.size()); /*sKey is an array*/ 

	    for (int i = messages.size() - messageLimit; i < messages.size(); i++) {
	        mEdit1.remove("Status_" + i);
	        mEdit1.putString("Status_" + i, messages.get(i));  
	    }

	    return mEdit1.commit();     
	}
	
	void startRepeatingTask() {
    	messageChecker.run();
    }
	
	@Override
	protected void onDestroy() {
		
	}
	
	/**
	 * Continuously (once every updatetime) calls updateMessages().
	 */
	Runnable messageChecker = new Runnable() {
    	@Override
    	public void run() {
    		updateMessages();
    		handler.postDelayed(messageChecker, updatetime);
    	}
    };
    
    /**
     * Checks the server for new messages for the current group.
     */
    void updateMessages() {
    	// check server for new messages in relevant groups
    	
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.message, menu);
		return true;
	}

}
