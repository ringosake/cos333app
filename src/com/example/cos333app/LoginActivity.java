package com.example.cos333app;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
 
public class LoginActivity extends Activity {
    private static final String TAG = "LoginActivity";
    private static final String SCOPE = "audience:server:client_id:581942024733-55depc52o3jkfu57jerqs30gd6hu7okv.apps.googleusercontent.com"; //+
    		//" https://www.googleapis.com/auth/userinfo.profile";

    private AccountManager mAccountManager;
    private Spinner mAccountTypesSpinner;
    static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
    
    private String[] mNamesArray;
    private String mEmail;
    protected ProgressDialog progress;

    public static String TYPE_KEY = "type_key";
    public static enum Type {FOREGROUND, BACKGROUND, BACKGROUND_WITH_SYNC}
	
    /*****************************************************/
    Button btnLogin;
    TextView errorMsg;

 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
 
        // Importing all assets like buttons, text fields
        btnLogin = (Button) findViewById(R.id.btnLogin);
        errorMsg = (TextView) findViewById(R.id.login_error);
        
        mNamesArray = getAccountNames();
        mAccountTypesSpinner = initializeSpinner(
                R.id.accounts_tester_account_types_spinner, mNamesArray);
        if (mAccountTypesSpinner.getSelectedItemPosition() < 0) {
            // this happens when the sample is run in an emulator which has no google account
            // added yet.
            show("No account available. Please add an account to the phone first.");
        }
        initializeFetchButton();
        
        Button toReg = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        toReg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
            	i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
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
    
    private Spinner initializeSpinner(int id, String[] values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(LoginActivity.this,
                android.R.layout.simple_spinner_item, values);
        Spinner spinner = (Spinner) findViewById(id);
        spinner.setAdapter(adapter);
        return spinner;
    }
    
    private void initializeFetchButton() {
        Button getToken = (Button) findViewById(R.id.btnLogin);
        getToken.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int accountIndex = mAccountTypesSpinner.getSelectedItemPosition();
                if (accountIndex < 0)
                    return;
                mEmail = mNamesArray[accountIndex];
                progress = ProgressDialog.show(LoginActivity.this, "", "Authenticating...", true);
                new com.example.cos333app.LoginThread(LoginActivity.this, mEmail, SCOPE,
                        REQUEST_CODE_RECOVER_FROM_AUTH_ERROR).execute();
                
        		
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
            new com.example.cos333app.LoginThread(this, mEmail, SCOPE, REQUEST_CODE_RECOVER_FROM_AUTH_ERROR).execute();
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
                  LoginActivity.this,
                  REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
              d.show();
            }
        });
    }
}