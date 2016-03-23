package com.example.hellow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private PreferencesService service;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		service = new PreferencesService(getApplicationContext());
		loadConfig();
		
		findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                saveConfig();
                open();
                
            }
        });
		
	}
	
	private void open() {
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, "找到即时到账辅助，然后开启服务即可", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	private void loadConfig(){
		EditText input_cburl = (EditText)this.findViewById(R.id.editText_cburl);
    	EditText input_cbparams = (EditText)this.findViewById(R.id.editText_cbparams);
    	RadioButton input_cbget = (RadioButton)this.findViewById(R.id.RadioGet);
    	input_cburl.setText(service.getPreferences("input_cburl"));
    	input_cbparams.setText(service.getPreferences("input_cbparams"));
    	input_cbget.setChecked(service.getPreferences("input_method").equals("1")?true:false);
	}
	
	private void saveConfig(){
		EditText input_cburl = (EditText)this.findViewById(R.id.editText_cburl);
    	EditText input_cbparams = (EditText)this.findViewById(R.id.editText_cbparams);
    	RadioButton input_cbget = (RadioButton)this.findViewById(R.id.RadioGet);
    	service.save("input_cburl", input_cburl.getText().toString());
    	service.save("input_cbparams", input_cbparams.getText().toString());
    	service.save("input_method", input_cbget.isChecked()?"1":"0");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
