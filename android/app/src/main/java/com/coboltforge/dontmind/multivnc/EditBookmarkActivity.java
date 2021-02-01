package com.coboltforge.dontmind.multivnc;

/*
 * Bookmark editing activity for MultiVNC.
 * 
 * Copyright © 2011-2012 Christian Beier <dontmind@freeshell.org>
 */


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


public class EditBookmarkActivity extends Activity {
	
	private static final String TAG = "EditBookmarkActivity";
	private VncDatabase database;
	private ConnectionBean bookmark = new ConnectionBean();

	// GUI elements 
	private EditText bookmarkNameText;
	private EditText ipText;
	private EditText portText;
	private EditText usernameText;
	private EditText passwordText;
	private CheckBox checkboxKeepPassword;
	private TextView repeaterText;
	private Spinner colorSpinner;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.editbookmarks);
		bookmarkNameText = (EditText) findViewById(R.id.textNicknameBookmark);
		ipText = (EditText) findViewById(R.id.textIPBookmark);
		portText = (EditText) findViewById(R.id.textPORTBookmark);
		passwordText = (EditText) findViewById(R.id.textPASSWORDBookmark);
		usernameText = (EditText) findViewById(R.id.textUsernameBookmark);
		checkboxKeepPassword = (CheckBox)findViewById(R.id.checkboxKeepPasswordBookmark);
		repeaterText = (TextView)findViewById(R.id.textRepeaterIdBookmark);
		colorSpinner = (Spinner)findViewById(R.id.spinnerColorMode);
		COLORMODEL[] models = {COLORMODEL.C24bit, COLORMODEL.C16bit};
		ArrayAdapter<COLORMODEL> colorSpinnerAdapter = new ArrayAdapter<COLORMODEL>(this, android.R.layout.simple_spinner_item, models);
		colorSpinner.setAdapter(colorSpinnerAdapter);
		
		database = VncDatabase.getInstance(this);

		// default return value
		setResult(RESULT_CANCELED);


		// read connection from DB
		Intent intent = getIntent();
		long connID = intent.getLongExtra(Constants.CONNECTION, 0);
		bookmark = database.getConnectionDao().get(connID);
		if (bookmark != null)
		{
			Log.d(TAG, "Successfully read connection " + connID + " from database");

			updateViewsFromBookmark();
		}
		else 
		{
			Log.e(TAG, "Error reading connection " + connID + " from database!");
		}

		Button saveButton = (Button) findViewById(R.id.buttonSaveBookmark);
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				updateBookmarkFromViews();
				saveBookmark(bookmark);
				
				setResult(RESULT_OK);
				finish();
			}
		});
		
		
		Button cancelButton = (Button) findViewById(R.id.buttonCancelBookmark);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(this, MainMenuActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void updateViewsFromBookmark() {

		bookmarkNameText.setText(bookmark.getNickname());
		ipText.setText(bookmark.getAddress());
		portText.setText(Integer.toString(bookmark.getPort()));
		if (bookmark.getKeepPassword() || bookmark.getPassword().length()>0) {
			passwordText.setText(bookmark.getPassword());
		}
		checkboxKeepPassword.setChecked(bookmark.getKeepPassword());
		usernameText.setText(bookmark.getUserName());

		COLORMODEL cm;
		try {
			cm = COLORMODEL.valueOf(bookmark.getColorModel());
		} catch (IllegalArgumentException e) {
			// there was a value bookmarked that we don't have anymore in the 1.9+ releases
			cm = COLORMODEL.C16bit;
		}
		COLORMODEL[] colors = {COLORMODEL.C24bit, COLORMODEL.C16bit};

		for (int i=0; i<colors.length; ++i)
			if (colors[i] == cm) {
				colorSpinner.setSelection(i);
				break;
			}

		if(bookmark.getUseRepeater())
			repeaterText.setText(bookmark.getRepeaterId());
	}
	
	
	private void updateBookmarkFromViews() {

		bookmark.setAddress(ipText.getText().toString());
		try
		{
			bookmark.setPort(Integer.parseInt(portText.getText().toString()));
		}
		catch (NumberFormatException nfe)
		{
			
		}
		bookmark.setNickname(bookmarkNameText.getText().toString());
		bookmark.setUserName(usernameText.getText().toString());
		bookmark.setPassword(passwordText.getText().toString());
		bookmark.setKeepPassword(checkboxKeepPassword.isChecked());
		bookmark.setUseLocalCursor(true); // always enable
		bookmark.setColorModel(((COLORMODEL)colorSpinner.getSelectedItem()).nameString());
		if (repeaterText.getText().length() > 0)
		{
			bookmark.setRepeaterId(repeaterText.getText().toString());
			bookmark.setUseRepeater(true);
		}
		else
		{
			bookmark.setUseRepeater(false);
		}
	}
	
	
	
	private void saveBookmark(ConnectionBean conn) 	{
		try {
			database.getConnectionDao().save(conn);
		}
		catch(Exception e) {
			Log.e(TAG, "Error saving bookmark: " + e.getMessage());
		}
	}
}