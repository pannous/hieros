// Uploading.
// With thanks to Geoffrey Watson.

package nederhof.util;

import java.io.*;
import java.net.*;
import java.util.*;

/*
import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.mime.*;
import org.apache.http.entity.mime.content.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.*;
*/

public class Uploader {

    // Server name.
    private String serverName;

    // Construct uploader for server.
    public Uploader(String serverName) {
	this.serverName = serverName;
    }

    /*
    // Do POST with keys and values.
    // Return message from server.
    public String upload(TreeMap parameters) throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
	HttpPost httppost = new HttpPost(serverName);

	MultipartEntity reqEntity = new MultipartEntity();
	Iterator it = parameters.keySet().iterator();
	while (it.hasNext()) {
	    String key = (String) it.next();
	    String val = (String) parameters.get(key);
	    StringBody valBody = new StringBody(val);
	    reqEntity.addPart(key, valBody);
	}
	httppost.setEntity(reqEntity);

	HttpResponse response = httpclient.execute(httppost);
	HttpEntity resEntity = response.getEntity();
	int statusCode = response.getStatusLine().getStatusCode();
	if (resEntity == null) 
	    throw new IOException("no response");
	else if (statusCode != HttpStatus.SC_OK) 
	    throw new IOException("status code: " + statusCode + "\n" +
		    EntityUtils.toString(resEntity));

	String message = EntityUtils.toString(resEntity);
	try { 
	    resEntity.consumeContent();
	} catch (Exception e) {
	    // ignore
	};
	httpclient.getConnectionManager().shutdown();
	return message;
    }
    */

    // Do POST without using external JAR files.
    public String uploadSimple(TreeMap parameters) throws IOException {
	URL url = new URL(serverName);
	HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	connection.setUseCaches(false);
	connection.setDoOutput(true);
	connection.setDoInput(true);
	connection.setRequestMethod("POST");
	connection.setRequestProperty("Content-Type",
		"application/x-www-form-urlencoded");

	// Compose data.
	final String encoding = "UTF-8";
	StringBuffer data = new StringBuffer();
	Iterator it = parameters.keySet().iterator();
	while (it.hasNext()) {
	    String key = (String) it.next();
	    String val = (String) parameters.get(key);
	    String nextData = key + "=" + URLEncoder.encode(val, encoding);
	    if (data.length() != 0) 
		data.append("&");
	    data.append(nextData);
	}

	// Send.
	OutputStream basicOut = connection.getOutputStream();
	DataOutputStream out = new DataOutputStream(basicOut);
	out.writeBytes(data.toString());
	out.flush();
	out.close();

	// Check status.
	int statusCode = connection.getResponseCode();
	if (statusCode != HttpURLConnection.HTTP_OK) {
	    InputStream err = connection.getErrorStream();
	    StringBuffer error = new StringBuffer();
	    int ch;
	    while ((ch = err.read()) != -1)
		error.append((char) ch);
	    err.close();
	    throw new IOException("status code: " + statusCode + "\n" +
		    error.toString());
	}

	// Receive.
	InputStream in = connection.getInputStream();
	StringBuffer message = new StringBuffer();
	int c;
	while ((c = in.read()) != -1)
	    message.append((char) c);
	in.close();
	return message.toString();
    }

}
