package fr.eurecom.wifi3gproject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.os.Environment;


public final class LoggerManager {
	
	private static Logger logger_data = Logger.getLogger("Data");
	private static Logger logger_Battery = Logger.getLogger("Battery");
	private static Logger logger_stats = Logger.getLogger("Statistics");
	private static Logger logger_rates = Logger.getLogger("Rates");
	private static Logger logger_errors = Logger.getLogger("Errors");
	private static FileHandler fh1=null;
	private static FileHandler fh2=null;
	private static FileHandler fh3=null;
	private static FileHandler fh4=null;
	private static FileHandler fh5=null;
	
	public static void CreateLogs(){
		String HomeDirectory = "SpringProject2014";
		String sdcard_home_path = Environment.getExternalStorageDirectory().getPath()+ "/"+HomeDirectory;
		File dir = new File(sdcard_home_path);
		if(!dir.exists()) dir.mkdir();
		
		String Repository = "Repository";
		String sdcard_repository_path = Environment.getExternalStorageDirectory().getPath()+ "/" + HomeDirectory + "/" + Repository;
		File rep = new File(sdcard_repository_path);
		if(!rep.exists()) rep.mkdir();
		
		String sdcard_log_path = Environment.getExternalStorageDirectory().getPath()+ "/"+HomeDirectory+"/Log";
		File log_dir = new File(sdcard_log_path);

		if(log_dir.exists()){
			File[] files = log_dir.listFiles();
			for (File f : files){
				f.renameTo(new File(sdcard_repository_path+ "/" +f.getName()));
				f.delete();
			}
		}else{
			log_dir.mkdir();
		}
		
		
		String date = new SimpleDateFormat("dd-MM-yyyy_hh:mm:ss", Locale.FRANCE).format(new Date());
		// new phone
		File data = new File(sdcard_log_path+"/"+"data_"+date+".log");
		File battery = new File(sdcard_log_path+"/"+"battery_"+date+".log");
		File stats = new File(sdcard_log_path+"/"+"statistics_"+date+".log");
		File rates = new File(sdcard_log_path+"/"+"rates_"+date+".log");
		File errors = new File(sdcard_log_path+"/"+"errors_"+date+".log");
		
		// Old phone
//		File data = new File(Environment.getExternalStorageDirectory().getPath() +"/SpringProject2014/Log/data.log");
//		File battery = new File(Environment.getExternalStorageDirectory().getPath() +"/SpringProject2014/Log/battery.log");
//		File stats = new File(Environment.getExternalStorageDirectory().getPath() +"/SpringProject2014/Log/statistics.log");
//		File rates = new File(Environment.getExternalStorageDirectory().getPath() +"/SpringProject2014/Log/rates.log");
		
		try {
			data.createNewFile();
			battery.createNewFile();
			stats.createNewFile();
			rates.createNewFile();
			errors.createNewFile();
			
			// new phone
			fh1 = new FileHandler(sdcard_log_path+"/"+"data_"+date+".log");
			logger_data.addHandler(fh1);
			fh2 = new FileHandler(sdcard_log_path+"/"+"battery_"+date+".log");
			logger_Battery.addHandler(fh2);
			fh3 = new FileHandler(sdcard_log_path+"/"+"statistics_"+date+".log");
			logger_stats.addHandler(fh3);
			fh4 = new FileHandler(sdcard_log_path+"/"+"rates_"+date+".log");
			logger_rates.addHandler(fh4);
			fh5 = new FileHandler(sdcard_log_path+"/"+"errors_"+date+".log");
			logger_errors.addHandler(fh5);
			
			// old phone
//			fh1 = new FileHandler(Environment.getExternalStorageDirectory().getPath() +"/SpringProject2014/Log/data.log");
//			logger_data.addHandler(fh1);
//			fh2 = new FileHandler(Environment.getExternalStorageDirectory().getPath() +"/SpringProject2014/Log/battery.log");
//			logger_Battery.addHandler(fh2);
//			fh3 = new FileHandler(Environment.getExternalStorageDirectory().getPath() +"/SpringProject2014/Log/statistics.log");
//			logger_stats.addHandler(fh3);
//			fh4 = new FileHandler(Environment.getExternalStorageDirectory().getPath() +"/SpringProject2014/Log/rates.log");
//			logger_rates.addHandler(fh4);
			
			
		} catch (IOException e) {
//			e.printStackTrace();
		}
		
	}
	
	
	public static void LogBatteryLevel(int batteryPct){
		logger_Battery.log(Level.INFO, String.valueOf(batteryPct));
	}

	public static void LogData(String Msg){
		logger_data.log(Level.INFO, Msg);
	}
	
	public static void LogStatistics(String Msg){
		logger_stats.log(Level.INFO, Msg);
	}
	
	public static void LogRates(String Msg){
		logger_rates.log(Level.INFO, Msg);
	}
	
	public static void LogErrors(String Msg){
		logger_errors.log(Level.INFO, Msg);
	}
	
	public static void ReleaseLogHandler(){
		if(fh1!=null)fh1.close();
		if(fh2!=null)fh2.close();
		if(fh3!=null)fh3.close();
		if(fh4!=null)fh4.close();
		if(fh5!=null)fh5.close();
	
	}

}
