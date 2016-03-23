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
     * 向指定URL发送GET方法的请求
     * 
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static void sendGet(String url, String params, HttpRequestCallback hrc) {
    	HttpGetThread hgt = new HttpGetThread(url, params, hrc);
    	hgt.start();
    }

    /**
     * 向指定 URL 发送POST方法的请求
     * 
     * @param url
     *            发送请求的 URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
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
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(this.params);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
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
        Log.w("AAAAAAAAAA", "Post回调结果："+result);
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
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
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