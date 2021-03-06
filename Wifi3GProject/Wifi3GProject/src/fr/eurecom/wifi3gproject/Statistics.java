package fr.eurecom.wifi3gproject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.preference.PreferenceManager;
import android.util.Log;

public class Statistics extends BroadcastReceiver {

    
	public static LinkedList<Double> WifiRate = new LinkedList<Double>();
	public static LinkedList<Double> CellRate = new LinkedList<Double>();
	public static LinkedList<Double> TotBytes = new LinkedList<Double>();
	public static double EMAWifi=0.0; 
	public static double EMACell=0.0; 
	public static double MeanSize=0.0; 
	public static double Variance=0.0;
	public static double EMAWifi_ = 0.0;
	public static double EMACell_ = 0.0;
	public int id;
	static boolean clear_flag = true;
	static long temp;
	long time;
	static long old_time = System.currentTimeMillis();
	static double sum = 0;
	static double rate = 0;
	static int counter;
	static long Th_aux;
	public static Context context_1;
	double usage_wifi;
	double usage_cell;
	double usage_total;
	static double previous_usage_wifi = 0.0;
	double wifi_rate;
	static int counter_periods = 1;
	public static double avg_wifi_rate = 0;
	static double sum_rate = 0;
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	
		String status = getONOFF(context);
		if(status.equalsIgnoreCase("OFF")) return;
        
		synchronized (Statistics.TotBytes) {
			MeanSize = EMA(0.8, MeanSize, TotBytes, id);
			if (Constants.debug) System.out.println("Statistics mean: "+ MeanSize);
		}
		
		synchronized (Statistics.TotBytes) {
			Variance = EMV(0.8, Variance, MeanSize, TotBytes);
			TotBytes.clear();
			if (Constants.debug) System.out.println("Statistics variance: "+ Variance);
		}
		
		synchronized (WifiRate) {
			EMAWifi_ = getInstantWifiRate();
			EMAWifi = EMA(0.8, EMAWifi, WifiRate, 0);
			WifiRate.clear();
		}
		
		synchronized (CellRate) {
			EMACell_ = getInstantCellRate();
			EMACell=EMA(0.8, EMACell, CellRate, 1);
			CellRate.clear();
		}
		
		//Log.i("RATE", "WIFI RATE: " + EMAWifi_ + " CELL RATE: " + EMACell_); // test instant rate
		//Log.i("RATE", "WEIGHTED WIFI RATE: " + EMAWifi + " WEIGHTED CELL RATE: " + EMACell + "\n");
		
		long current_time = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
		Date resultdate = new Date(current_time);
	
	// Rates computation
	if (Constants.debug){	
		
		usage_wifi = (TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes()) / 1048576.0 - MainActivity.wifi_initial_usage;
		usage_cell = (TrafficStats.getMobileRxBytes()) / 1048576.0 - MainActivity.cell_initial_usage;
		usage_total = TrafficStats.getTotalRxBytes() / 1048576.0 - MainActivity.total_initial_usage;
		wifi_rate = (usage_wifi - previous_usage_wifi) / (Constants.INTERVAL_TH / 1000.0);
		previous_usage_wifi = usage_wifi;
		sum_rate += wifi_rate;
		avg_wifi_rate = sum_rate / counter_periods;
		counter_periods++;
		
		//System.out.println("TIME USAGE: " + sdf.format(resultdate));
		//System.out.println("WIFI USAGE: " + usage_wifi);
		//System.out.println("CELL USAGE: " + usage_cell);
		//System.out.println("TOTAL USAGE: " + usage_total);
		//System.out.println("RATE WIFI USAGE: " + (wifi_rate * 8) + " Mbps");
		//System.out.println("AVG WIFI RATE USAGE: " + avg_wifi_rate * 8);
		
	}
	
//		long current_time = System.currentTimeMillis();
//      SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
//
//      Date resultdate = new Date(current_time);
//      System.out.println(sdf.format(resultdate));
//		
//		
//		wifi_bytes = (TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes());
//		// rx_bytes = TrafficStats.getTotalRxBytes();
//		
//		temp = System.currentTimeMillis();
//		time =  (long) ((temp - old_time) / 1000.0);
//		old_time = temp;
//		//System.out.println("TOTAL RECEIVED MBytes: " + (rx_bytes / 1000000.0) ;
//		System.out.println("WIFI MBytes: " + (wifi_bytes / 1000000.0));
//		System.out.println("TIME: " + time + " CURRENT: " + resultdate);
//		if (time != 0){
//			rate = ((wifi_bytes / 1000000.0) * 8 ) / time;
//			System.out.println("RATE: " + (((wifi_bytes / 1000000.0) * 8 ) / time));
//		}
//		sum = sum + rate;
//		counter ++;
//		System.out.println(" AVERAGE RATE: " + (sum / counter));
		
		
//		float WifiRate_aux = getRate(API.GetInterfaceWifi());
//		if (WifiRate_aux<20){
//			float WifiRate = WifiRate_aux;
//		}
//		float CellRate_aux = getRate(API.GetInterface3G());
//		if (CellRate_aux<20){
//			float CellRate = CellRate_aux;
//		}
		//float WifiRate = getRate(API.GetInterfaceWifi());
		//float CellRate = getRate(API.GetInterface3G());		
		
		//synchronized (DownloadFilesTask.Threshold) {
			//DownloadFilesTask.Threshold = (long) ComputeThreshold.getThreshold(0.2, 40, 50, 0.1, 9, 109, 100, 35, 40, 5, 7);
			//long Th_aux=(long) ComputeThreshold.getThreshold(0.2, EMAWifi, EMACell, 1, 0.001, 170, Constants.DMAX, Constants.LAMBDA_IN, Constants.DWF, Constants.MEAN_SIZE, Constants.VAR_SIZE);
			//long Th_aux=(long) ComputeThreshold.getThreshold(0.2, EMAWifi, EMACell, 1, 0.001, 170, Constants.DMAX, Constants.LAMBDA_IN, DownloadFilesTask.d_wifi, Constants.MEAN_SIZE, Constants.VAR_SIZE);
		
		if(MainActivity.policy == Constants.WIFI_3G){
			Th_aux = (long) ComputeThreshold.getThreshold(0.2, EMAWifi, EMACell, 1, 0.001, 170, Constants.DMAX, Constants.LAMBDA_IN, DownloadFilesTask.d_wifi, MeanSize, Variance);
			//Log.i("RATE", "Threshold " + Th_aux);
			if (Th_aux >=  0){
				DownloadFilesTask.Threshold = Th_aux; //(long) ComputeThreshold.getThreshold(0.2, WifiRate, CellRate, 0.1, 0.001, 8000, 300, Constants.LAMBDA_IN, 0, 40, 250);				
			}else{
				if (Constants.debug) System.out.println("Negative threshold!!!");
			}		
		}
		if (Constants.debug) System.out.println("CHECK TH "+ Th_aux);
			
		
		//DELIA (q, rateWifi [Mbit/s], rateCell  [Mbit/s], epsilon, Smin [Mbit], Smax [Mbit], Dmax [s], lambda [1/s], DWF [s], media [Mbit], variance [Mbit])
		//}
		
		//LoggerManager.LogStatistics(EMAWifi + " " + EMACell + " " + DownloadFilesTask.Threshold);
		if (Constants.debug) System.out.println("Value: I am updating at time: "+System.currentTimeMillis()+" value Thresh: "+DownloadFilesTask.Threshold+" value wifiRate: " + WifiRate + " value 3gRate " + CellRate);
		
		Intent myIntent = new Intent(MainActivity.context, Statistics.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.context, 0, myIntent,0); 	     
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Service.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Constants.INTERVAL_TH, pendingIntent);
    
		
    }
     
    public static String getONOFF(Context context) {
    	String status = "";
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
    	status = settings.getString("StatOnOff", "OFF");
    	return status;
    }
    public double getInstantWifiRate(){
    	
    	double rateWifi = Double.MIN_VALUE;
    	double size_sum = 0;
    	int counter = 0;
    	
    	for (int i = 0; i < WifiRate.size(); i++){
    		size_sum += WifiRate.get(i);
    		counter++;
    	}
    	
    	rateWifi = size_sum / counter;
    	
    	return rateWifi;
    }
    
	public double getInstantCellRate(){
	    	
		double rateCell = Double.MIN_VALUE;
		double size_sum = 0;
		int counter = 0;

		for (int i = 0; i < CellRate.size(); i++) {
			size_sum += CellRate.get(i);
			counter++;
		}

		rateCell = size_sum / counter;

		return rateCell;
	}
    
	
     public double EMA(double alpha, double Estimated, LinkedList<Double> queue, int id) {
           
    	   this.id = id;
    	 
    	   if(queue.size() == 0 && Estimated == 0.0){
    		   
    		   if (id == 0){
    			   
    			   Estimated = Constants.rateWiFiInit;
    			   
    		   }else{
    			   
    			   Estimated = Constants.rateCellInit;
    			   
    		   }
    	   }else if(queue.size()==0){
    		   
    		   return Estimated;
    	   }
    	   
    	   int index;
    	   if(Estimated==0.0){
    		   if (id==0){
    			   Estimated = Constants.rateWiFiInit;
    		   //Estimated = queue.getFirst();
    			   index=1;
    		   }else{
    			  Estimated = Constants.rateCellInit;
    			  index = 1;
    		   }
    	   }else{
    		   index=0;
    	   }
    	   
    	   
    	   for( ;index<queue.size(); index++){
        	      Estimated = alpha * Estimated + (1-alpha) * (queue.get(index));
           }
    	   
           return Estimated;
        }
 
     public double EMV(double alpha, double Estimated, double mean, LinkedList<Double> queue) {
         
  	   if(queue.size()==0){
  		   return Estimated;
  	   }
//  	   
//  	   double sum=0.0;
//  	   for(int i=0; i<queue.size(); i++)sum+=queue.get(i);
//  	   double mean = sum / queue.size();
  	   
  	   double sum=0.0;
  	   for(int i=0; i<queue.size(); i++) sum += (queue.get(i)-mean)*(queue.get(i)-mean); 
  	   
  	   double variance = sum / queue.size();
 
       return alpha*Estimated+ (1-alpha)*variance;
      }
     
}






//public float getRate(String Interface){
//
//	String pingResult = "";
//    float rate=0;
//    float x[]={-1,-1,-1};
//    float y[]={0,0,0};
//    int L[]={64,100,1000};
//    double idt= 1;
//    String inputLine;
//    String lastLine=new String("");
//    for (int i=0; i<3;i++){
//        String pingCmd = "ping -s "+ L[i] + " -i " + idt + " -I "+ Interface +" -c 2 www.google.com";
//        try {
//        	Runtime.getRuntime().exec(pingCmd);
//        	Runtime r = Runtime.getRuntime();
//            Process p = r.exec(pingCmd);
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
//           
//            while ((inputLine = in.readLine()) != null) {
//            	lastLine=inputLine;
//                pingResult += inputLine;
//            }
//            in.close();
//            String [] st=lastLine.split("/");
//            //System.out.println("RTT measurement  " + "string length " + st.length);
//            if(st.length<5){
//            	x[i]=-1;
//            	//System.out.println("RTT measurement failed" + st.toString() + "last line is " +lastLine.toString());
//
//            }else{
//            	y[i]=Float.parseFloat(st[4]);
//            	x[i]=0;
//            	//x=((float)1300*8/(float)y)*1000;
//        //    	x=((float)1300*8*2/(float)y)/1000; //DELIA: RTT divided by 2 and in Mbit/s
//            	//System.out.println("RTT "+ y + "Inteface: "+Interface);
//            	// DC: TO MODIFY
//            }
//         } catch (IOException e) {
//            System.out.println(e);
//        }
//    }
//    
//    if ((x[1] != -1) && (x[2] != -1)){
//      	rate=((float)((L[2] - L[1])*8*2) /(float)(Math.abs((y[2] - y[1])))) / 1000; //DELIA: RTT divided by 2 and in Mbit/s	
//    }
//    
//    	System.out.println("Inteface: "+Interface+" -> "+rate+" MBit/s ");
//    return rate;
//}