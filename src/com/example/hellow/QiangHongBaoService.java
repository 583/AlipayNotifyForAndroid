package com.example.hellow;

import java.util.Date;
import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

public class QiangHongBaoService extends AccessibilityService {

    static final String TAG = "QiangHongBao";
    private String cb_url = "";
    private String cb_methodget = "";
    private String cb_params = "";

    /** ֧�����İ���*/
    //static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    static final String WECHAT_PACKAGENAME = "com.eg.android.AlipayGphone";
    /** �����Ϣ�Ĺؼ���*/
    static final String HONGBAO_TEXT_KEY = "�ɹ�";

    Handler handler = new Handler();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();

        Log.d(TAG, "�¼�---->" + event);
        
        //֪ͨ���¼�
        if(eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            List<CharSequence> texts = event.getText();
            if(!texts.isEmpty()) {
                for(CharSequence t : texts) {
                    String text = String.valueOf(t);
                    
                    if(text.contains(HONGBAO_TEXT_KEY)) {
                    	//Toast.makeText(this, "ת���տ�ɹ�", Toast.LENGTH_SHORT).show();
                        openNotify(event);
                        break;
                    }
                }
            }
        } else if(eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            openHongBao(event);
        }
    }

    /*@Override
    protected boolean onKeyEvent(KeyEvent event) {
        //return super.onKeyEvent(event);
        return true;
    }*/

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "�ж����������", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Toast.makeText(this, "�������������", Toast.LENGTH_SHORT).show();
    }

    private void sendNotifyEvent(){
        AccessibilityManager manager= (AccessibilityManager)getSystemService(ACCESSIBILITY_SERVICE);
        if (!manager.isEnabled()) {
            return;
        }
        AccessibilityEvent event=AccessibilityEvent.obtain(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
        event.setPackageName(WECHAT_PACKAGENAME);
        event.setClassName(Notification.class.getName());
        CharSequence tickerText = HONGBAO_TEXT_KEY;
        event.getText().add(tickerText);
        manager.sendAccessibilityEvent(event);
    }

    /** ��֪ͨ����Ϣ*/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openNotify(AccessibilityEvent event) {
        if(event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            return;
        }
        //�����Ǿ�������΢�ŵ�֪ͨ����Ϣ��
        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openHongBao(AccessibilityEvent event) {
    	/*System.out.println("-------- " + event.getClassName().toString());
        if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName())) {
            //�����˺������һ������ȥ����
            checkKey1();
        } else if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())) {
            //����������ϸ�ļ�¼����
            //nonething
        } else if("com.tencent.mm.ui.LauncherUI".equals(event.getClassName())) {
            //���������,ȥ���к��
            checkKey2();
        }*/
    	getPaymentAmount();
    }
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void getPaymentAmount() {
    	AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindowΪ��");
            return;
        }
        
        //Toast.makeText(this, "88888888888888888888888888888", Toast.LENGTH_LONG).show();
        
        List<AccessibilityNodeInfo> listAmount = nodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.chatapp:id/biz_title");
        List<AccessibilityNodeInfo> listRemark = nodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.chatapp:id/biz_app_desc");
        
        PreferencesService pService = new PreferencesService(getApplicationContext());
        cb_url = pService.getPreferences("input_cburl");
        cb_params = pService.getPreferences("input_cbparams");
        cb_methodget = pService.getPreferences("input_method");
        String tempAmount = "";
        String tempRemark = "";
        
        for(int i=0; i<listAmount.size() && i<listRemark.size(); i++) {
        	tempAmount = listAmount.get(i).getText().toString().replace("Ԫ", "");
        	tempRemark = listRemark.get(i).getText().toString();
        	cb_params = cb_params.replace("{trade_amount}", tempAmount).replace("{trade_title}", tempRemark).replace("{trade_no}", String.valueOf(new Date().getTime()));
        	//Toast.makeText(this, "cb_params: " + cb_params, Toast.LENGTH_LONG).show();
        	if(cb_methodget.equals("1")){
        		HttpRequest.sendGet(cb_url, cb_params, new QianghongbaoHttpRequestCallback(this){
        			
        			@Override
        			public void Callback(String requestContent){
        				//Toast.makeText(this.context, "�ص������"+requestContent, Toast.LENGTH_LONG).show();
        				Log.w(TAG, "GET�ص��� "+requestContent);
        			}
        			
        		});
        	}
        	else{
        		HttpRequest.sendPost(cb_url, cb_params,new QianghongbaoHttpRequestCallback(this){
        			
        			@Override
        			public void Callback(String requestContent){
        				//Toast.makeText(this.context, "�ص������"+requestContent, Toast.LENGTH_LONG).show();
        				Log.w(TAG, "POST�ص��� "+requestContent);
        			}
        			
        		});
        	}
        }
        
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkKey1() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindowΪ��");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("����");
        for(AccessibilityNodeInfo n : list) {
            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkKey2() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindowΪ��");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("��ȡ���");
        if(list.isEmpty()) {
            list = nodeInfo.findAccessibilityNodeInfosByText(HONGBAO_TEXT_KEY);
            for(AccessibilityNodeInfo n : list) {
                Log.i(TAG, "-->΢�ź��:" + n);
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        } else {
            //���µĺ������
            for(int i = list.size() - 1; i >= 0; i --) {
                AccessibilityNodeInfo parent = list.get(i).getParent();
                Log.i(TAG, "-->��ȡ���:" + parent);
                if(parent != null) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
            }
        }
    }

}

class QianghongbaoHttpRequestCallback implements HttpRequestCallback{
	
	public Context context = null;
	
	public QianghongbaoHttpRequestCallback(Context context){
		this.context = context;
	}
	
	public void Callback(String requestContent){
		
	}
	
}
