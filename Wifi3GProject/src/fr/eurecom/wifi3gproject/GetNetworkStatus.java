package fr.eurecom.wifi3gproject;

import java.io.IOException;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class GetNetworkStatus {
	
	
	public static int info() throws IOException{
		
		ConnectivityManager cm = (ConnectivityManager) MainActivity.context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if(cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected() &&
		(cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected() ||
		cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_DUN).isConnected() ||
		cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI).isConnected() ||
		cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS).isConnected() ||
		cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_SUPL).isConnected())){
			if(API.setIPDualMode() && API.SetIPGateways()) return Constants.WIFI_3G;
			return Constants.NO_CONNECTIONS;
		}
		
		if(cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) return Constants.ONLY_WIFI;
		if(cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()) return Constants.ONLY_3G;
		if(cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_DUN).isConnected()) return Constants.ONLY_3G;
		if(cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI).isConnected()) return Constants.ONLY_3G;
		if(cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS).isConnected()) return Constants.ONLY_3G;
		if(cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_SUPL).isConnected()) return Constants.ONLY_3G;
	
		return Constants.NO_CONNECTIONS;
	}

}