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
		//Ҳ�ɲ���this.getPreferences()����ȡSharedPreferences
		//��������ָ�����ƣ���Ĭ����MainActivity������Ϊ�ļ�����
		SharedPreferences preferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.commit();//���ڴ��д�ŵ����ݴ�ŵ��ļ���
	}

	public String getPreferences(String key){
		SharedPreferences preferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
		return preferences.getString(key, "");
	}
	
}
