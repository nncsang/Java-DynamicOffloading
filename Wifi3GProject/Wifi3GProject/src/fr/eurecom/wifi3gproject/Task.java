package fr.eurecom.wifi3gproject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;


public class Task implements  Callable<String>{
	
	private URL url;
	private double filesize = -1;
	private String ServerIp;
	private String Interface;
	private String filename="";
	public int ID;
	public long delay;
	private int N;
	private int halfSize;
	private int policy;
	public double req_time = 0;
	private double old_start_time=0;
	private double old_start_time_WF=0;
	private double old_start_time_C=0;   // previous flow starting time
	static boolean first_flow_flag = false;
	static boolean first_flow_flag_WF = false;
	static boolean first_flow_flag_C = false;
	private static double flow_idt=0;
	private static double flow_idt_WF=0;
	private static double flow_idt_C=0;
	public static double sum_idt;
	public static double sum_idt_WF;
	public static double sum_idt_C;
	private static double first_start_time;
	private static double first_start_time_WF;
	private static double first_start_time_C;
	private static double task_start_time;
	private static int count_flow = 0;
	private static long sum_reading_time = 0;
	public static long sum_wifi_time = 0;
	public static long sum_cell_time = 0;
	public static double max_duration_wifi=0;
	public static double max_duration_cell=0;
	public static double start_wifi_time;
	public static double start_cell_time;
	private static boolean first_wifi_flow = true;
	private static boolean first_cell_flow = true;
	public static double Size = 0;
	public static int counter_wifi_flows = 0;
	public static int counter_cell_flows = 0;
	double rate_wifi = 0;
	double rate_cell = 0;
	public static double sum_rate_wifi = 0;
	public static double sum_rate_cell = 0;
	private final Lock _mutex = new ReentrantLock(true);
	public String urlStr = "";
	public int num_of_retry = 0;
	DownloadFilesTask activity;
	private double start_time = 0;
	double total=0;
	long start_reading_time = 0;
	
	public Task(String url, int ID, long delay, int N, int policy, double task_start_time, DownloadFilesTask activity){
		try {
			urlStr = url;
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		this.ID=ID;
		this.delay=delay;
		this.N = N;
		this.policy = policy;
		this.task_start_time= task_start_time;
		Double obj = new Double(N/2);
		halfSize = obj.intValue();
		req_time=0;
		this.activity = activity;
		this.total = 0;
	}
	
	protected void GetFileSizeANDServerIP(){
		
			try {
				this.url = new URL(urlStr);
				HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
				
				// Log http connection errors
				if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) LoggerManager.LogErrors("REQUEST SIZE ERROR MESSAGE: " + httpConn.getResponseMessage() 
																									 + "FLOW ID: " + ID 
																									 + "URL: " + this.url); 
				//System.out.println("#### FILE SIZE REQUEST ####");
				//System.out.println("#### FLOW ID " + ID + " ####");
				//System.out.println("#### HTTP RESPONSE MESSAGE:" + httpConn.getResponseMessage() + " FOR FLOW ID: " + ID);
				//System.out.println("#########");
				filesize = (httpConn.getContentLength())*8*Math.pow(10, -6); // from byte to Mbit
				//Log.d("SIZE", "ID: " + ID + " HTTP SIZE: " + filesize);
				httpConn.disconnect();
						
				InetAddress addr = InetAddress.getByName(this.url.getHost());
				this.ServerIp = addr.getHostAddress();
				
				String fileExtenstion = MimeTypeMap.getFileExtensionFromUrl(this.url.toString());
				this.filename = URLUtil.guessFileName(this.url.toString(), null,fileExtenstion);
				
				if (Constants.debug) System.out.println("ip of " + url.toString() + " : " + this.ServerIp);
				if (Constants.debug) Log.d("SIZE","file length of " + url.toString() + " : "+ String.valueOf(this.filesize));	
				if (Constants.debug) System.out.println("name of " + url.toString() + " : " + this.filename);
				

			} catch (Exception e) {
				e.printStackTrace();
			 }
	}

	@Override
	public String call() throws Exception {
		  	
			Constants.task_state.set(ID, Constants.TASK_STATE.RUNNING);
			//System.out.println("entering the call() function " + policy);
			//if(policy == Constants.WIFI_3G){
			
			num_of_retry++;
			if (num_of_retry == 1){
				double req_time_start = System.currentTimeMillis();
				double task_waiting_time =  req_time_start - this.task_start_time;
				if (Constants.debug) System.out.println("Waiting time for this task ID " +ID + " for " + task_waiting_time + "ms");
				GetFileSizeANDServerIP();
				double req_time_end = System.currentTimeMillis();
				req_time = (req_time_end > req_time_start) ? (req_time_end - req_time_start) : 0;
				
				while(this.filesize < 0) {
					LoggerManager.LogErrors("FAIL SIZE ERROR FOR URL: " + url.toString());
					GetFileSizeANDServerIP();
					//return "FAIL SIZE: " + this.filesize + " ID: " + ID + "url " + url.toString();
				}
				
				if (Constants.debug) System.out.println("FILE "+ID+" file size: "+this.filesize+" th: "+DownloadFilesTask.Threshold + " req time: " + req_time);
				if(this.filesize > DownloadFilesTask.Threshold){
					 if(first_wifi_flow){
						 start_wifi_time = System.currentTimeMillis();
						 if (Constants.debug) System.out.println("FIRST_WIFI_FLOW: " + start_wifi_time);
						 first_wifi_flow = false;
					 }
				 }else{	 
					 if(first_cell_flow){
						 start_cell_time = System.currentTimeMillis();
						 if (Constants.debug)System.out.println("FIRST_CELL_FLOW: " + start_cell_time);
						 first_cell_flow = false;
					 }
				 }
			
				switch (policy) {
				case Constants.WIFI_3G :
					if(this.filesize > DownloadFilesTask.Threshold){
						ConfigureWifi();
						Interface=API.GetInterfaceWifi();
					}else{
						Configure3G();
						Interface=API.GetInterface3G();
					}
					break;
				case Constants.ONLY_3G :
					Configure3G();
					Interface=API.GetInterface3G();
					break;	
				case  Constants.ONLY_WIFI :
					ConfigureWifi();
					Interface=API.GetInterfaceWifi();
					break;
				case Constants.FLOW_BALANCE:
					if (ID % 2 == 0){
						ConfigureWifi();
						Interface=API.GetInterfaceWifi();
					}else{
						Configure3G();
						Interface=API.GetInterface3G();
					}
					break;	
				case  Constants.WARM_UP :
					//if (ID < halfSize) {
						ConfigureWifi();
					//	Interface=API.GetInterfaceWifi();
					//}else{
					//	Configure3G();
					//	Interface=API.GetInterface3G();
					//}
					break;
				default:
					Log.e("Policy", + policy + " not supported\n");
					break;
				}
			}
			// !!!!!  Change also in DownloadFilesTask class !!!!!!
		
		return startDownload(req_time);				
}
	
	protected void ConfigureWifi(){
		
		String script = "iptables -t mangle -A OUTPUT -d "
				+ this.ServerIp
				+ " -j MARK --set-mark "
				+ String.valueOf(Constants.IPTABLES_WIFI_MARK)
				+ " ; "
				+ "ip rule add fwmark "
				+ String.valueOf(Constants.IPTABLES_WIFI_MARK)
				+ " tab "
				+ String.valueOf(Constants.IPTABLES_WIFI_TABLE)
				+ " ; "
				+ "ip route add default via "
				+ API.GetGatewayWifiIP()
				+ " dev "
				+ API.GetInterfaceWifi()
				+ " tab "
				+ String.valueOf(Constants.IPTABLES_WIFI_TABLE)
				+ " ; " + "ip route flush cache; "
				+ "iptables -t nat -A POSTROUTING -o "
				+ API.GetInterfaceWifi() + " -j SNAT --to-source "
				+ API.GetWifiIP() + " ; " ;

		API.runScript("offloading.sh",script, new StringBuilder(), 5000, true);
		
		Log.e("WIFI3G","download " + ID + " via wifi");
	}
	
	protected void Configure3G(){
		
		String script = "iptables -t mangle -A OUTPUT -p 6 -d " // -p 6 means only TCP
				+ this.ServerIp
				+ " -j MARK --set-mark "
				+ String.valueOf(Constants.IPTABLES_3G_MARK)
				+ " ; "
				+ "ip rule add fwmark "
				+ String.valueOf(Constants.IPTABLES_3G_MARK)
				+ " tab "
				+ String.valueOf(Constants.IPTABLES_3G_TABLE)
				+ " ; "
				+ "ip route add default via "
				+ API.Get3GIP()
				+ " tab "
				+ String.valueOf(Constants.IPTABLES_3G_TABLE)
				+ " dev " + API.GetInterface3G() + " ; " 
				+ "ip route flush cache;"
				+ "iptables -t nat -A POSTROUTING -o "
				+ API.GetInterface3G() + " -j SNAT --to-source "
				+ API.Get3GIP() + " ; ";

		API.runScript("offloading.sh",script, new StringBuilder(), 5000, true);
		
		Log.e("WIFI3G","download " + ID + " via cellular");

	}

	@SuppressWarnings("resource")
	protected String startDownload(double req_time){
		System.out.println("Start dowloading " + ID + " from " + total);	
	try{
		HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();
		
		InetAddress ia = InetAddress.getByName(this.url.getHost());
		//System.out.println("HOST NAME: " + ia.getHostName());
		//System.out.println("HOST IP: " + ia.getHostAddress());
		//System.out.println("HOST Canonical Name: " + ia.getCanonicalHostName());
		
		
	String Directory = "Downloaded Files";
	String sdcard_path = Environment.getExternalStorageDirectory().getPath()+ "/SpringProject2014/"+Directory;
	File dir = new File(sdcard_path);
	if(!dir.exists()) dir.mkdir();
	
	
	File file=new File(sdcard_path+"/"+this.filename);
	
	connection.setReadTimeout(Constants.LTIME);
	connection.setConnectTimeout(Constants.LTIME);
	connection.setInstanceFollowRedirects(false);
	
    if(file.exists() && total > 0){
    	//System.out.println(file.length() + " - " + total);
         connection.setRequestProperty("Range", "bytes="+((int)total)+"-");
    }
    
    
	/*
	if (connection.getResponseCode() != HttpURLConnection.HTTP_OK &&
			connection.getResponseMessage()  != "FOUND" && 
			connection.getResponseMessage()  != "Partial Content" && 
			connection.getResponseMessage()  != "Moved Temporarily") {
		LoggerManager.LogErrors("ERROR MESSAGE: " + connection.getResponseMessage() 
				+ "FLOW ID: " + ID 
				+ "URL: " + this.url);
		return "FAIL HTTP : "+ this.Interface+" ID: "+ID;
	}*/

    
	InputStream input = connection.getInputStream();

	OutputStream output = new FileOutputStream(sdcard_path+"/"+this.filename);
	if (total != 0)
		output = new FileOutputStream(sdcard_path+"/"+this.filename, true);

	byte data[] = new byte[4096];
	
	int count;
	
	if (num_of_retry == 1){
		start_time = System.currentTimeMillis();//Calendar.getInstance().getTimeInMillis();
		start_reading_time = System.currentTimeMillis();
	}
	
	//System.out.println("ID: " + ID + " - Number of try: " + num_of_retry + " - " + req_time +  " " + start_time);
	
//	if (policy == Constants.ONLY_3G || policy == Constants.ONLY_WIFI){
//		
//		if (first_flow_flag == false){
//			first_flow_flag = true;
//			first_start_time = start_time;
//			sum_idt_C=0;
//			sum_idt_WF=0;
//			if (Constants.debug) System.out.println("Setting the flow flag for flow IDT calculation");
//		}else{
//			flow_idt = start_time - old_start_time - first_start_time;
//			if (Constants.debug) System.out.println("FLOWIDT CELL: " + flow_idt);
//		    if ( policy == Constants.ONLY_3G){
//		    	sum_idt_C += flow_idt;
//		    }
//		    if(policy == Constants.ONLY_WIFI){
//		    	sum_idt_WF+=flow_idt;
//		    }
//		}
//	    old_start_time = start_time;
//	    
//	}else{ // Threshold POlicy
//		
//		if (this.filesize > DownloadFilesTask.Threshold){
//			
//			if (!first_flow_flag_WF){
//				first_flow_flag_WF = true;
//				first_start_time_WF = start_time;
//				sum_idt_WF=0;
//				if (Constants.debug) System.out.println("Setting the flow flag WF for flow IDT calculation" + old_start_time_WF);
//			}else{
//				flow_idt_WF = start_time - old_start_time_WF - first_start_time_WF;
//				if (Constants.debug) System.out.println("FLOWIDT WF: " + flow_idt_WF);
//				sum_idt_WF += flow_idt_WF;
//			}
//		    old_start_time_WF = start_time;
//		    
//		}else{
//			
//			if (!first_flow_flag_C){
//				first_flow_flag_C = true;
//				first_start_time_C = start_time;
//				sum_idt_C=0;
//				if (Constants.debug) System.out.println("Setting the flow flag CEL for flow IDT calculation" + old_start_time_C);
//			}else{
//				flow_idt_C= start_time - old_start_time_C - first_start_time_C;
//				if (Constants.debug) System.out.println("FLOWIDT CELL: " + flow_idt_C);
//				sum_idt_C += flow_idt_C ;
//			}
//		    old_start_time_C = start_time;
//		    
//		}	
//	}
	
	
	while ((count = input.read(data)) != -1){
		if(isCancelled()){
			input.close();
			output.flush();
			output.close();
			//_mutex.lock();
			//activity.cancel_task_handler(ID);
			//_mutex.lock();
			return "C" + ID;
		}
		total += count;
		output.write(data, 0, count);
	}
	
	
	/*
	while(true){
		_mutex.lock();
		if (Constants.IS_WIFI_AVAILABLE == true){
			if ((count = input.read(data)) != -1){
				if(isCancelled()){
					input.close();
					output.close();
					return "CANC";
				}
				total += count;
				output.write(data, 0, count);
			}else{
				break;
			}
		}else{
			//System.out.println("Pausing download due to being in wifi off period");
		}
		_mutex.unlock();
	}
	*/
	
	long end_reading_time = System.currentTimeMillis();
	long reading_time = end_reading_time - start_reading_time;
	sum_reading_time += reading_time;
	count_flow++;
	double avg_reading_time = sum_reading_time / count_flow;
	if (Constants.debug) System.out.println("READING TIME: " + reading_time + " FOR " + ID + " FLOW WITH AVERAGE FLOW TIME " + avg_reading_time);
	if (Constants.debug) System.out.println("Thread ID: " + ID + " name: " + this.filename + " Tot: " + this.filesize + " count: " + (total*8*Math.pow(10, -6)));
	
		
	double end_time = System.currentTimeMillis(); //Calendar.getInstance().getTimeInMillis();
	double task_download_time = end_time - start_time;
	if (Constants.debug) System.out.println("download time for the task ID " +ID + " is " + task_download_time + "ms");

	if (output != null)	{
		output.flush();
		output.close();
	}
	
	if (input != null) input.close();
	if (connection != null) connection.disconnect();
	
	Log.e("WIFI3G", "Download Completed for " +  ID);
	
	if(Interface==API.GetInterfaceWifi()){
		LoggerManager.LogData(ID+" "+"W"+" "+start_time+" "+end_time+" "+total+" "+this.delay + " REQ_DELAY: " + req_time);
	}else{
		LoggerManager.LogData(ID+" "+"C"+" "+start_time+" "+end_time+" "+total+" "+this.delay + " REQ_DELAY: " + req_time);
	}
	// compute rates from this data: sync ave throughput var
	
	total = total / 1048576; // MB 
	//Log.d("SIZE", "ID: " + ID + " SIZE: " + total);
	Size = Size + total * 8 ;  // Mbps
	//System.out.println("TOTAL SIZE: " + Size);
		
		synchronized (Statistics.TotBytes) {
			Statistics.TotBytes.add(total);
		}
		
		if(Interface == API.GetInterfaceWifi()){
			LoggerManager.LogRates("WIFI: ID "+ ID +" SIZE " + total + " time: " + (end_time-start_time) / 1000);
			rate_wifi = ( total / ((end_time-start_time) / 1000)) * 8;
			sum_rate_wifi += rate_wifi;
			counter_wifi_flows++;
			if (Constants.debug) System.out.println("FILESIZE "+ID+ " RATE: " + rate_wifi + " SIZE " + total +" time: "+(end_time-start_time)+" Wifi th: "+DownloadFilesTask.Threshold);
			synchronized (Statistics.WifiRate) {
				Statistics.WifiRate.add(rate_wifi);
			}
		}else{
			LoggerManager.LogRates("CELLULAR: ID "+ ID +" SIZE " + total +" time: "+(end_time-start_time) / 1000);
			rate_cell = (total / ((end_time-start_time) / 1000)) * 8;
			sum_rate_cell += rate_cell;
			counter_cell_flows++;
			if (Constants.debug) System.out.println("FILESIZE "+ID+ " RATE: " + rate_cell + " SIZE " + total +" time: "+(end_time-start_time)+ " 3G th: "+DownloadFilesTask.Threshold);
			synchronized (Statistics.CellRate) {
				Statistics.CellRate.add(rate_cell);
			}
		}
		
	double end_flow_time = System.currentTimeMillis();
	
	
	if (this.filesize > DownloadFilesTask.Threshold){
		if (end_flow_time > max_duration_wifi ) max_duration_wifi = end_flow_time;
		if (Constants.debug) System.out.println("WIFI_TIME: " + max_duration_wifi);
	}else{
		if (end_flow_time > max_duration_cell ) max_duration_cell = end_flow_time;
		if (Constants.debug) System.out.println("CELL_TIME: " + max_duration_cell);
	}
	
	_mutex.lock();
	Constants.task_state.set(ID, Constants.TASK_STATE.SUCCESSED);
	activity.notifyProgress();
	_mutex.unlock();
	
	return "S" + ID;
	
	}catch(Exception e){
		//e.printStackTrace();	
		System.out.println(e.getMessage());
		/*
		if (e.getMessage().contains("No address associated with hostname")){
			while(true){
				System.out.println(ID + ": Fail due to wifi not turn on. Retrying....");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
				
				boolean try_success = startDownload1(req_time).startsWith("SUCCESS");
				if (try_success)
					return "SUCCESS";
			}
		}
		
		if (Constants.IS_WIFI_AVAILABLE == false && num_of_retry < Constants.NUM_OF_RETRIES){
			System.out.println("Add " + ID + " to queue");
			Constants.queue.add(ID);
			return "PAUSE";
		}
		*/
		System.out.println(ID + ": is cancled");
		if (num_of_retry < Constants.NUM_OF_RETRIES){
			return "C" + ID;
		}
		else
			return "F" + ID;
		//return "FAIL EXCEP Interface :"+this.Interface+" Error type "+e.getMessage()+ " ID: "+ID;  // TO DO: reassign the flow to the Cellular!
	}finally{
	//	wl.release();
	}
  }
	
	public boolean isCancelled(){
		return Thread.currentThread().isInterrupted();
	}
}

//LOG SYNTAX
//ID_THREAD INTERFACE START_TIME END_TIME TOTAL_BYTE