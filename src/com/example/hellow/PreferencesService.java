package com.example.hellow;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferencesService {
	private Context context;
	
	public PreferencesService(Context context) {
		this.context = context;
	}

	public void save(String key, String value){
		//也可采用this.getPreferences()来获取SharedPreferences
		//这样不必指定名称，他默认用MainActivity名称作为文件名称
		SharedPreferences preferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.commit();//把内存中存放的数据存放到文件中
	}

	public String getPreferences(String key){
		SharedPreferences preferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		return preferences.getString(key, "");
	}
	
}
