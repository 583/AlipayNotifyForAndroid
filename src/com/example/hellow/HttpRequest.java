package com.example.hellow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.util.Log;

public class HttpRequest {
    /**
     * ��ָ��URL����GET����������
     * 
     * @param url
     *            ���������URL
     * @param param
     *            ����������������Ӧ���� name1=value1&name2=value2 ����ʽ��
     * @return URL ������Զ����Դ����Ӧ���
     */
    public static void sendGet(String url, String params, HttpRequestCallback hrc) {
    	HttpGetThread hgt = new HttpGetThread(url, params, hrc);
    	hgt.start();
    }

    /**
     * ��ָ�� URL ����POST����������
     * 
     * @param url
     *            ��������� URL
     * @param param
     *            ����������������Ӧ���� name1=value1&name2=value2 ����ʽ��
     * @return ������Զ����Դ����Ӧ���
     */
    public static void sendPost(String url, String params, HttpRequestCallback hrc) {
    	HttpPostThread hpt = new HttpPostThread(url, params, hrc);
    	hpt.start();
    }    
}

class HttpPostThread extends Thread {  
    
	String url = "";
	String params = "";
	HttpRequestCallback hrc = null;
	
    public HttpPostThread(String url, String params, HttpRequestCallback hrc){  
    	this.url = url;
    	this.params = params;
    	this.hrc = hrc;
    }  
    
    public void run() {
    	PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
        	
        	TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){  
                public X509Certificate[] getAcceptedIssuers(){return null;}  
                public void checkClientTrusted(X509Certificate[] certs, String authType){}  
                public void checkServerTrusted(X509Certificate[] certs, String authType){}  
            }};  
        	
        	HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
            SSLContext sc = SSLContext.getInstance("TLS");  
            sc.init(null, trustAllCerts, new SecureRandom());  
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        	
            URL realUrl = new URL(this.url);
            // �򿪺�URL֮�������
            URLConnection conn = realUrl.openConnection();
            // ����ͨ�õ���������
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // ����POST�������������������
            conn.setDoOutput(true);
            conn.setDoInput(true);
            
            // ��ȡURLConnection�����Ӧ�������
            out = new PrintWriter(conn.getOutputStream());
            // �����������
            out.print(this.params);
            // flush������Ļ���
            out.flush();
            // ����BufferedReader����������ȡURL����Ӧ
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("���� POST ��������쳣��"+e);
            e.printStackTrace();
        }
        //ʹ��finally�����ر��������������
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        Log.w("AAAAAAAAAA", "Post�ص������"+result);
        hrc.Callback(result);
    }  
    
}  

class HttpGetThread extends Thread {  
    
	String url = "";
	String params = "";
	HttpRequestCallback hrc = null;
	
    public HttpGetThread(String url, String params, HttpRequestCallback hrc){  
    	this.url = url;
    	this.params = params;
    	this.hrc = hrc;
    }  
    
    public void run() {  
    	String result = "";
        BufferedReader in = null;
        try {
        	
        	TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){  
                public X509Certificate[] getAcceptedIssuers(){return null;}  
                public void checkClientTrusted(X509Certificate[] certs, String authType){}  
                public void checkServerTrusted(X509Certificate[] certs, String authType){}  
            }};  
        	
        	HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
            SSLContext sc = SSLContext.getInstance("TLS");  
            sc.init(null, trustAllCerts, new SecureRandom());  
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        	
            String urlNameString = this.url + "?" + this.params;
            URL realUrl = new URL(urlNameString);
            // �򿪺�URL֮�������
            URLConnection connection = realUrl.openConnection();
            // ����ͨ�õ���������
            
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // ����ʵ�ʵ�����
            connection.connect();
            // ��ȡ������Ӧͷ�ֶ�
            Map<String, List<String>> map = connection.getHeaderFields();
            // �������е���Ӧͷ�ֶ�
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // ���� BufferedReader����������ȡURL����Ӧ
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("����GET��������쳣��" + e);
            e.printStackTrace();
        }
        // ʹ��finally�����ر�������
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        hrc.Callback(result);
    }  
    
}  

class NullHostNameVerifier implements HostnameVerifier {  
	  
    @Override     
    public boolean verify(String hostname, SSLSession session) {  
        Log.i("RestUtilImpl", "Approving certificate for " + hostname);  
        return true;  
    }  
  
}  