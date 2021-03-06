package fr.eurecom.wifi3gproject;

import java.util.ArrayList;

public class Constants {
	
	// Policy Constants
	public static final int ONLY_WIFI=0;
	public static final int ONLY_3G=1;
	public static final int WIFI_3G=2;
	public static final int FLOW_BALANCE=4;
	public static final int NO_CONNECTIONS=3;
	public static final int WARM_UP=5;
	
	// Profile Constants
	public static final int WARM = 1;
	public static final int OVERLOADED = 2;
	public static final int ONLY_ARR_PATTERN = 3;
	public static final int ARR_PLUS_WIFI_PATTERN = 4;
	
	// Iptables Constants
	public static int IPTABLES_3G_MARK = 2;
	public static int IPTABLES_WIFI_MARK = 3;
	public static int IPTABLES_3G_TABLE = 4;
	public static int IPTABLES_WIFI_TABLE = 5;
	
	public static int INTERVAL_TH = 15 * 1000; // sec = interval to compute threshold
	public static int INTERVAL_BATTERY = 30; //seconds
	public static int INTERVAL_THROUGHPUT = 60; //seconds
	public static int LTIME = 20000; //ms
	
	// Less skewed list (url_1.txt): mean=5.9 MB (47.5Mb), var=260 MB (2080Mb)
	// next block constants should be computed online! Now they must change based on the file list
	public static double MEAN_SIZE = 20;  //38.4;//64;//81.6; //45 //39; //47.5; //33.0; Mbit
	public static double VAR_SIZE = 256; //Mbit
	public static double lambda1 = 0.0172; // in ComputeThreshold
	public static double lambda2 = 0.024;
	public static double rateWiFiInit = Double.MIN_VALUE; // find the starting rates with a warm up period
	public static double rateCellInit = Double.MIN_VALUE;
	// end block
	
	// Skewed list: mean=5.9 MB (47.2 Mb), var=390 MB (3119 Mb)
	// next block constants should be computed online! Now they must change based on the file list
	//	public static double MEAN_SIZE=47.2; // Mbit
	//	public static double VAR_SIZE=3119; //Mbit
	//	public static double lambda1=0.0103; // in ComputeThreshold
	//	public static double lambda2=0.0296;
	// end block
	
	 //0.5; // 1/s (inter-arrival rate in DownloadFilesTask)
	public static double DMAX = 7; // seconds
	//public static double DWF=0; // seconds
	public static double MAX_CYCLE = 200;
	
	public final static ArrayList<Integer> queue = new ArrayList<Integer>();
	// WiFi On-Off Pattern
	public static double T_ON = 200.0; //100000.0; //120.0;     // 136.0; sec
	public static double T_OFF = 5.0; //1.0; //60.0;    // 68.0; sec
	public static double L_ON = 1.0 / (T_ON * 1000.0);
	public static double L_OFF = 1.0 / (T_OFF * 1000.0);
	
	public static boolean test_no_delay = true;
	public static boolean debug = false;
	public static boolean enable_arrival_pattern;
	public static boolean enable_wifi_on_off_pattern;
	
	
	public static String URL_NAME;
	public static double LAMBDA_IN;
	public static int THREADS;
	public static double SYSTEM_UTILIZATION;
	
	
	/* ADDED CODE FOR DELAY OFFLOADING */
	
	public static int TOTAL_TASKS_COMING_DURING_OFF_PERIOD = 0;
	public static int TOTAL_TASKS_BEING_QUEUED = 0;
	
	public static boolean IS_WIFI_AVAILABLE = true;
	public static int NUM_OF_RETRIES = 1000;
	public static enum TASK_STATE {
		CREATED,
		SUMITTED_TO_EXECUTOR,
		RUNNING,
		SUCCESSED,
		CANCELED_ERRORS,
		CANCELED_WIFI_OFF,
		ADDED_TO_QUEUE,
		TAKED_OUT_FROM_QUEUE_AND_SUMITTED_TO_EXECUTOR,
		FAILED_MANY_FAILES
	}
	
	public final static ArrayList<TASK_STATE> task_state = new ArrayList<TASK_STATE>();
	
	
}