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

    /** 支付宝的包名*/
    //static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    static final String WECHAT_PACKAGENAME = "com.eg.android.AlipayGphone";
    /** 红包消息的关键字*/
    static final String HONGBAO_TEXT_KEY = "成功";

    Handler handler = new Handler();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();

        Log.d(TAG, "事件---->" + event);
        
        //通知栏事件
        if(eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            List<CharSequence> texts = event.getText();
            if(!texts.isEmpty()) {
                for(CharSequence t : texts) {
                    String text = String.valueOf(t);
                    
                    if(text.contains(HONGBAO_TEXT_KEY)) {
                    	//Toast.makeText(this, "转账收款成功", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, "中断抢红包服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Toast.makeText(this, "连接抢红包服务", Toast.LENGTH_SHORT).show();
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

    /** 打开通知栏消息*/
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openNotify(AccessibilityEvent event) {
        if(event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            return;
        }
        //以下是精华，将微信的通知栏消息打开
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
            //点中了红包，下一步就是去拆红包
            checkKey1();
        } else if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(event.getClassName())) {
            //拆完红包后看详细的纪录界面
            //nonething
        } else if("com.tencent.mm.ui.LauncherUI".equals(event.getClassName())) {
            //在聊天界面,去点中红包
            checkKey2();
        }*/
    	getPaymentAmount();
    }
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void getPaymentAmount() {
    	AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
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
        	tempAmount = listAmount.get(i).getText().toString().replace("元", "");
        	tempRemark = listRemark.get(i).getText().toString();
        	cb_params = cb_params.replace("{trade_amount}", tempAmount).replace("{trade_title}", tempRemark).replace("{trade_no}", String.valueOf(new Date().getTime()));
        	//Toast.makeText(this, "cb_params: " + cb_params, Toast.LENGTH_LONG).show();
        	if(cb_methodget.equals("1")){
        		HttpRequest.sendGet(cb_url, cb_params, new QianghongbaoHttpRequestCallback(this){
        			
        			@Override
        			public void Callback(String requestContent){
        				//Toast.makeText(this.context, "回调结果："+requestContent, Toast.LENGTH_LONG).show();
        				Log.w(TAG, "GET回调： "+requestContent);
        			}
        			
        		});
        	}
        	else{
        		HttpRequest.sendPost(cb_url, cb_params,new QianghongbaoHttpRequestCallback(this){
        			
        			@Override
        			public void Callback(String requestContent){
        				//Toast.makeText(this.context, "回调结果："+requestContent, Toast.LENGTH_LONG).show();
        				Log.w(TAG, "POST回调： "+requestContent);
        			}
        			
        		});
        	}
        }
        
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkKey1() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("拆红包");
        for(AccessibilityNodeInfo n : list) {
            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkKey2() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
        if(list.isEmpty()) {
            list = nodeInfo.findAccessibilityNodeInfosByText(HONGBAO_TEXT_KEY);
            for(AccessibilityNodeInfo n : list) {
                Log.i(TAG, "-->微信红包:" + n);
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
        } else {
            //最新的红包领起
            for(int i = list.size() - 1; i >= 0; i --) {
                AccessibilityNodeInfo parent = list.get(i).getParent();
                Log.i(TAG, "-->领取红包:" + parent);
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
