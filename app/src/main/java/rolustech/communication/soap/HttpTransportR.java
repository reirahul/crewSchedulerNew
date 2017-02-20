package rolustech.communication.soap;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.ksoap2.transport.HttpTransportSE;

import javax.net.ssl.HttpsURLConnection;

public class HttpTransportR extends HttpTransportSE {
/*
 * OverRided HttpTransportR to allow HTTPS connections
 */
	public HttpTransportR(String url) {
		super(url);
		// TODO Auto-generated constructor stub
		HttpsURLConnection.setDefaultHostnameVerifier(new AllowAllHostnameVerifier());
	}
	
/*
 * OverRided HttpTransportR to allow HTTPS connections with timeout
 */
	public HttpTransportR(String url, int timeOut) {
		super(url, timeOut);
		// TODO Auto-generated constructor stub
		HttpsURLConnection.setDefaultHostnameVerifier(new AllowAllHostnameVerifier());
	}
}
