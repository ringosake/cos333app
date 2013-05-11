package com.example.cos333app;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
 
public class RegisterActivity extends Activity {
    private static final String TAG = "RegisterActivity";
    private static final String SCOPE = "audience:server:client_id:581942024733-55depc52o3jkfu57jerqs30gd6hu7okv.apps.googleusercontent.com"; //+
    		//" https://www.googleapis.com/auth/userinfo.profile";

    private AccountManager mAccountManager;
    private Spinner mAccountTypesSpinner;
    static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    
    private String[] mNamesArray;
    private String mEmail;
    private String mNumber;
    private String mName;
    private String mCode;
    boolean visited = false;
    private AlertDialog alert;
    protected ProgressDialog progress;
    
    public static String TYPE_KEY = "type_key";
    public static enum Type {FOREGROUND, BACKGROUND, BACKGROUND_WITH_SYNC}
	
    /*****************************************************/
    Button btnRegister;
    TextView errorMsg;
    Button btnLinkToLogin;
    EditText name;
    EditText phoneNumber;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
 
        // Importing all assets like buttons, text fields
        btnRegister = (Button) findViewById(R.id.btnRegister);
        errorMsg = (TextView) findViewById(R.id.register_error);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
        name = (EditText) findViewById(R.id.editText_name);
        phoneNumber = (EditText) findViewById(R.id.editText_number);
        
        mNamesArray = getAccountNames();
        mAccountTypesSpinner = initializeSpinner(
                R.id.accounts_tester_account_types_spinner_reg, mNamesArray);
        if (mAccountTypesSpinner.getSelectedItemPosition() < 0) {
            // this happens when the sample is run in an emulator which has no google account
            // added yet.
            show("No account available. Please add an account to the phone first.");
        }
        
        // try to fill in the phone number
        TelephonyManager tMgr =(TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (tMgr != null) {
        	String tmpnum = tMgr.getLine1Number();
        	if (tmpnum != null)
        		phoneNumber.setText(tmpnum);
        }
        
        initializeFetchButton();
        
        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                // Close Registration View
                finish();
            }
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_RECOVER_FROM_AUTH_ERROR) {
            handleAuthorizeResult(resultCode, data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      super.onSaveInstanceState(savedInstanceState);
      // Save UI state changes to the savedInstanceState.
      // This bundle will be passed to onCreate if the process is
      // killed and restarted.
      savedInstanceState.putString("mNumber", mNumber);
      savedInstanceState.putString("code", mCode);
      savedInstanceState.putBoolean("visited", visited);
      // etc.
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      // Restore UI state from the savedInstanceState.
      // This bundle has also been passed to onCreate.
      visited = savedInstanceState.getBoolean("visited");
      mNumber = savedInstanceState.getString("mNumber");
      mCode = savedInstanceState.getString("code");
    }
    @Override
    protected void onResume() 
    {
    	 super.onResume();
    	 if (visited) {
    		 alert.show();
    	 }
    }
    
    private Spinner initializeSpinner(int id, String[] values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(RegisterActivity.this,
                android.R.layout.simple_spinner_item, values);
        Spinner spinner = (Spinner) findViewById(id);
        spinner.setAdapter(adapter);
        return spinner;
    }
    
    private String sendVerif(String phoneNumber) {
	    GenerateRandomString grs = new GenerateRandomString();
	    String code = grs.getAlphaNumeric(5);
	    SmsManager smsManager = SmsManager.getDefault();
	    smsManager.sendTextMessage(phoneNumber, null, ("Your verification code is " + code), null, null);
	    return code;
    }
    
    private void initializeFetchButton() {
        Button getToken = (Button) findViewById(R.id.btnRegister);
        
        getToken.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int accountIndex = mAccountTypesSpinner.getSelectedItemPosition();
                if (accountIndex < 0)
                    return;
                mEmail = mNamesArray[accountIndex];
                mName = name.getText().toString();
                String numStrip = PhoneNumberUtils.extractNetworkPortion(phoneNumber.getText().toString());
                if (PhoneNumberUtils.isWellFormedSmsAddress(numStrip)) {
                	
                	if (mNumber == null || mNumber.compareTo(numStrip) != 0)
                		mCode = sendVerif(numStrip);
                	mNumber = numStrip;
                	
                	// Set an EditText view to get user input 
                	final EditText input = new EditText(RegisterActivity.this);
                	
                	alert = new AlertDialog.Builder(RegisterActivity.this)
                    .setView(input)
                    .setTitle("Verification")
                    .setMessage("Enter the verification code sent to your phone number:")
                    .setPositiveButton(android.R.string.ok,
                            new Dialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int which) {
                                    //Do nothing here. We override the onclick
                                }
                            })
                    .setNeutralButton("Send code",
                            new Dialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int which) {
                                    //Do nothing here. We override the onclick
                                }
                            })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();

		            alert.setOnShowListener(new DialogInterface.OnShowListener() {
		
		                @Override
		                public void onShow(DialogInterface dialog) {
		
		                    Button b = alert.getButton(AlertDialog.BUTTON_POSITIVE);
		                    b.setOnClickListener(new View.OnClickListener() {
		
		                        @Override
		                        public void onClick(View view) {
		                        	String value = input.getText().toString();
		                			if (value.equalsIgnoreCase(mCode)) {
		                				alert.dismiss();
		                				progress = ProgressDialog.show(RegisterActivity.this, "", "Authenticating...", true);
		                				new com.example.cos333app.RegisterThread(RegisterActivity.this, mEmail, mName, mNumber, SCOPE,
		                            			REQUEST_CODE_RECOVER_FROM_AUTH_ERROR).execute();
		                			} else
		                				alert.setMessage("Incorrect verification code");
		                        }
		                    });
		                    Button b1 = alert.getButton(AlertDialog.BUTTON_NEUTRAL);
		                    b1.setOnClickListener(new View.OnClickListener() {
		
		                        @Override
		                        public void onClick(View view) {
		                        	mCode = sendVerif(mNumber);
		                        }
		                    });
		                }
		            });
		            alert.show();
                	
                } else {
                	show("Invalid phone number");
                	//TODO: popup with invalid phone number
                }
            }
        });
    }
    
    private String[] getAccountNames() {
        mAccountManager = AccountManager.get(this);
        Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = accounts[i].name;
        }
        return names;
    }
        
    private void handleAuthorizeResult(int resultCode, Intent data) {
        if (data == null) {
            show("Unknown error, click the button again");
            return;
        }
        if (resultCode == RESULT_OK) {
            Log.i(TAG, "Retrying");
            new com.example.cos333app.RegisterThread(this, mEmail, mName, mNumber, SCOPE, REQUEST_CODE_RECOVER_FROM_AUTH_ERROR).execute();
            return;
        }
        if (resultCode == RESULT_CANCELED) {
            show("User rejected authorization.");
            return;
        }
        show("Unknown error, click the button again");
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
    

    /**
     * This method is a hook for background threads and async tasks that need to launch a dialog.
     * It does this by launching a runnable under the UI thread.
     */
    public void showErrorDialog(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Dialog d = GooglePlayServicesUtil.getErrorDialog(
                  code,
                  RegisterActivity.this,
                  REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
              d.show();
            }
        });
    }
    
    private class GenerateRandomString {
    	private static final String ALPHA_NUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    	public String getAlphaNumeric(int len) {
    		StringBuffer sb = new StringBuffer(len);
    		for (int i = 0; i < len; i++) {
    			int ndx = (int) (Math.random() * ALPHA_NUM.length());
    			sb.append(ALPHA_NUM.charAt(ndx));
    		}
    		return sb.toString();
    	}
    }
}