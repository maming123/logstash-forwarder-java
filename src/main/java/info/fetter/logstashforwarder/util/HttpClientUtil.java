package info.fetter.logstashforwarder.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class HttpClientUtil {
	
	public HttpClientUtil(){}
	
	private static final String GET="GET";
	private static final String POST="POST";
	private static final String CHARSET="UTF-8";
	private static Logger logger = Logger.getLogger(HttpClientUtil.class);
	
	public static String get(String url) {
		return get(url, null, null);
	}
	
	public static String get(String url,Map<String, String> queryParas) {
		return get(url, queryParas, null);
	}
	
	/**
	 * Send GET request
	 */
	public static String get(String url,Map<String, String> queryParas,Map<String, String> hearders) {
		HttpURLConnection conn=null;
		try {
			conn=getHttpConnection(buildUrlWithQueryString(url, queryParas), GET, hearders);
			conn.connect();
			return readResponseString(conn);
		}catch (Exception e) {
			//e.printStackTrace();
			throw new RuntimeException(e);
		}finally {
			if(conn!=null){
				conn.disconnect();
			}
		}
	}
	
	public static String post(String url,Map<String, String> queryParas,String data) {
		return post(url, queryParas, data,null);
	}
	
	public static String post(String url,String data,Map<String, String> hearders) {
		return post(url, null, data, hearders);
	}
	
	public static String post(String url,String data) {
		return post(url, null, data, null);
	}

	public static String postJson(String url,String data) {
		Map<String,String> headers =new HashMap<>();
		headers.put("Content-Type", "application/json");
		headers.put("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
		return post(url, null, data, headers);
	}

	public static String postJson(String url,String data,Map<String, String> headers) {
		if(null!=headers && headers.containsKey("Content-Type"))
		{
			headers.remove("Content-Type");
		}
		headers.put("Content-Type", "application/json");
		return post(url, null, data, null);
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
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
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
            out.print(param);
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
        return result;
    } 
	
	/**
	 * Send POST request
	 */
	public static String post(String url,Map<String, String> queryParas,String data,Map<String, String> headers) {
		HttpURLConnection conn=null;
		try {
			conn=getHttpConnection(buildUrlWithQueryString(url, queryParas), POST, headers);
			conn.connect();
			
			OutputStream out=conn.getOutputStream();
			out.write(data.getBytes(CHARSET));
			out.flush();
			out.close();
			
			return readResponseString(conn);
		}  catch (Exception e) {
			//e.printStackTrace();
			throw new RuntimeException(e);
		}finally {
			if(conn!=null){
				conn.disconnect();
			}
		}
	}
	
	private static HttpURLConnection getHttpConnection(String url,String method,Map<String, String> headers) throws IOException,Exception {
		URL _url=new URL(url);
		HttpURLConnection conn=(HttpURLConnection) _url.openConnection();
		
		conn.setRequestMethod(method);
		conn.setDoOutput(true);
		conn.setDoInput(true);
		
		conn.setConnectTimeout(19000);
		conn.setReadTimeout(19000);
		if(null==headers) {
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
		}
		if(headers!=null&&!headers.isEmpty())
			for (Entry<String, String> entry : headers.entrySet())
				conn.setRequestProperty(entry.getKey(), entry.getValue());
		return conn;
		
	}
	
	private static String readResponseString(HttpURLConnection conn){
		StringBuilder sb=new StringBuilder();
		InputStream inputStream=null;
		try {
			inputStream=conn.getInputStream();
			//将字节流转换成字符流，创建字符流缓冲
			BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream, CHARSET));
			String line=null;
			while ((line=reader.readLine())!=null) {
				sb.append(line).append("\n");
			}
			return sb.toString();
		} catch (Exception e) {
			//e.printStackTrace();
			throw new RuntimeException(e);
		}finally {
			if(inputStream!=null){
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Build queryString of the url
	 */
	private static String buildUrlWithQueryString(String url,Map<String, String> queryParas) {
		if(queryParas==null||queryParas.isEmpty())
			return url;
		StringBuilder sb=new StringBuilder(url);
		boolean isFirst;
		if(sb.indexOf("?")==-1){
			isFirst=true;
			sb.append("?");
		}else{
			isFirst=false;
		}
		
		for (Entry<String, String> entry : queryParas.entrySet()) {
			if(isFirst)isFirst=false;
			else
				sb.append("&");
			String key=entry.getKey();
			String value=entry.getValue();
			if(StringUtils.isNotBlank(value))
				try {value=URLEncoder.encode(value,CHARSET);} catch (UnsupportedEncodingException e) {throw new RuntimeException(e);}
			sb.append(key).append("=").append(value);
		}
		
		return sb.toString();
	}

}
