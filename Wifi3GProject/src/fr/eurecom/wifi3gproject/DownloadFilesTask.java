package fr.eurecom.wifi3gproject;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.uncommons.maths.random.ExponentialGenerator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import fr.eurecom.plots.PlotList;

public class DownloadFilesTask extends AsyncTask<Void, Integer, Void>{
	
	ArrayList<String> urls;
	public static double Threshold=Long.valueOf(0);
	public static double d_wifi = 0;
	private static double d_wf = 0;
	public static ArrayList<Double> delay_wifi = new ArrayList<Double>();
	public static ArrayList<Integer> wifi_flow_index = new ArrayList<Integer>();
	private double arr_delay = 0;
	int policy = 0;
	boolean parallel=false;
	ExecutorService executor;
	Activity myAct;
	static double start_downloadfilestask=0.0;
	double end_downloadfilestask=0.0;
	public static double downloadfilestask_duration;
	
	public DownloadFilesTask(ArrayList<String> urls, int policy, boolean parallel, Activity myAct) {
		this.myAct = myAct;
		this.urls = urls;
		this.policy = policy;
		this.parallel = parallel;
		
		// randomize for flow balance case
		if (policy == Constants.FLOW_BALANCE){
			Random rand_fb = new Random();
			Collections.shuffle(this.urls, rand_fb);
		}
		
		switch(policy){
			case Constants.WIFI_3G: Threshold =  Constants.MEAN_SIZE; // threshold = Mbit
				break;
			case Constants.ONLY_WIFI: Threshold = Long.MIN_VALUE;
				break;
			case Constants.ONLY_3G: Threshold = Long.MAX_VALUE;
				break;	
		}
		
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		start_downloadfilestask = System.currentTimeMillis();
		System.out.println("WIFI USAGE START: " + (TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes()) / 1048576.0);
		System.out.println("CELL USAGE START: " + TrafficStats.getMobileRxBytes() / 1048576.0);
		System.out.println("TOTAL USAGE START: " + TrafficStats.getTotalRxBytes() / 1048576.0);
		if (Constants.debug) System.out.println("START TASK: " + start_downloadfilestask);
		
		if(parallel){
				try {
					ParalellExecution();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}else{
				SequentialExecution();
		}
			return null;
	}
	
	@Override
	protected void onCancelled() {
		super.onCancelled();
		System.out.println("Cancelled");
		executor.shutdownNow();
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		MainActivity.wl.release();
		MainActivity.progressDialog.dismiss();
		System.out.println("WIFI USAGE END: " + ((TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes()) / 1048576.0 - MainActivity.wifi_initial_usage));
		System.out.println("CELL USAGE END: " + (TrafficStats.getMobileRxBytes() / 1048576.0 - MainActivity.cell_initial_usage));
		System.out.println("TOTAL USAGE END: " + (TrafficStats.getTotalRxBytes() / 1048576.0 - MainActivity.total_initial_usage));
		// EXPERIMENT DURATION
		end_downloadfilestask = System.currentTimeMillis();//Calendar.getInstance().getTimeInMillis();
		downloadfilestask_duration = (end_downloadfilestask - start_downloadfilestask) / 1000.0;
		if (Constants.debug) System.out.println("Experiment duration: " + downloadfilestask_duration);

		LoggerManager.ReleaseLogHandler();
   	 	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.context);
   	 	SharedPreferences.Editor editor = settings.edit();
   	 	editor.putString("OnOff","OFF");
   	 	editor.commit();
   	 	editor.putString("StatOnOff","OFF");
	 	editor.commit();
   	 	myAct.finish();
   	 	Intent Plot = new Intent(myAct.getApplicationContext(),PlotList.class);
   	 	Bundle bundle = new Bundle();
   	 	bundle.putInt("policy", policy);
   	 	Plot.putExtras(bundle);
   	 	myAct.startActivity(Plot);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		MainActivity.progressDialog.incrementProgressBy(1);
	}

	public void SequentialExecution(){
		
		executor = Executors.newFixedThreadPool(1);
		if (Constants.debug) System.out.println("#### Sequential");
		
		for(int i=0; i<urls.size(); i++){
			
			double task_start_time = System.currentTimeMillis();
			FutureTask<String> future = new FutureTask<String>(new Task(urls.get(i), i, 0, urls.size(), policy, task_start_time));
			executor.execute(future);	
				try {
					if (Constants.debug) System.out.println("Result of thread " + i + ": " + future.get());
					publishProgress(1);	
					
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			
		if(isCancelled())break;
		}
		executor.shutdown();
	return;
	
	}
	
	public void ParalellExecution() throws MalformedURLException, IOException{
		
		System.out.println("HERE 3");
		LinkedList<DelayedTask> ListTask = new LinkedList<DelayedTask>();
		ArrayList<Long> wifi_on_off_pattern = new ArrayList<Long>();
		
		long sum_dwf = 0;
		int count_wifi = 0;
		long delay = 0;
		
		// Create WiFi on-off pattern
		wifi_on_off_pattern = WifiPattern();
		
		Random rand = new Random();
		rand.setSeed(300);
		ExponentialGenerator arr = new ExponentialGenerator(Constants.LAMBDA_IN, rand);
		
		int MAX_THREADS = Constants.THREADS; 
		executor = Executors.newFixedThreadPool(MAX_THREADS);
		if (Constants.debug) System.out.println("#### Parallel: "+ MAX_THREADS);
		
		CompletionService ecs = new ExecutorCompletionService(executor);
		
		int N = urls.size();
		long s = System.currentTimeMillis();
		for(int i=0; i < N; i++){

			if (Constants.enable_arrival_pattern) {

				double size = FileSize(new URL(urls.get(i)));

				if ((policy == Constants.WIFI_3G)
						|| (policy == Constants.FLOW_BALANCE)) {

					if (size < DownloadFilesTask.Threshold) {   // if it is going to the slower interface
						
						delay = (long) (arr_delay + arr.nextValue());
						arr_delay = delay;
						if (Constants.debug)
							System.out.println("Flow: " + i
									+ " ARRIVAL Delay: " + arr_delay
									+ " CELLULAR");
						Log.e("TEST", "ID: " + i + " TOTAL DELAY: " + arr_delay + " CELLULAR");
						
					}else { // if it is going to the faster interface

						if (Constants.enable_wifi_on_off_pattern) {
							delay = GetTotalDelay(wifi_on_off_pattern, i, arr);
							if (Constants.debug)
								System.out.println("Flow: " + i
										+ " ARRIVAL Delay: " + arr_delay
										+ " WIFI");
							sum_dwf += d_wf;
							count_wifi++;
						} else {
							delay = (long) (arr_delay + arr.nextValue());
							arr_delay = delay;
							Log.e("TEST", "ID: " + i + " TOTAL DELAY: " + delay);
						}
					}
				}else if (policy == Constants.ONLY_3G) {
					
					delay = (long) (arr_delay + arr.nextValue());
					arr_delay = delay;
					if (Constants.debug)
						System.out.println("Flow: " + i + " ARRIVAL Delay: "
								+ delay + " CELLULAR");
					Log.e("TEST", "ID: " + i + " TOTAL DELAY: " + arr_delay + " CELLULAR");
					
				}else if (policy == Constants.ONLY_WIFI) {

					if (Constants.enable_wifi_on_off_pattern) {

						delay = GetTotalDelay(wifi_on_off_pattern, i, arr);
						if (Constants.debug)
							System.out.println("Flow: " + i
									+ " ARRIVAL Delay: " + delay + " WIFI");
						sum_dwf += d_wf;
						count_wifi++;
						Log.v("DELAY_WIFI", "D_WIFI: " + d_wf + " COUNT: " + count_wifi);
						
					} else {
						delay = (long) (arr_delay + arr.nextValue());
						arr_delay = delay;
						Log.e("TEST", "ID: " + i + " TOTAL DELAY: " + delay);
					}
				}
			}
			
			double task_start_time = System.currentTimeMillis();

			Task t = new Task(urls.get(i), i, delay * 1000, urls.size(), policy, task_start_time);
			
			ListTask.add(new DelayedTask(t, delay * 1000, ecs, i));
			
			if(isCancelled()){
				Log.i("Download_task", "task cancelled ");	
				for(DelayedTask Dt : ListTask) Dt.Cancel();
		
				return;
			}
			
		}
		
		long fi = System.currentTimeMillis();
		long en = fi - s;
		System.out.println("TIME: " + en);
		
		if(count_wifi == 0){ 
			count_wifi = 1;
		}
		
		d_wifi = (sum_dwf / count_wifi);
		
		try { 
	
	     for (int i = 0; i < N; i++) {
	         Future f = ecs.take();
	         if (Constants.debug){
	        	 System.out.println("Result of thread: " + f.get());
	        	 if (!f.get().toString().startsWith("SUCCESS")) LoggerManager.LogErrors("Result of thread: " + f.get());
	         }
	     	 publishProgress(1);
	     	 if(isCancelled())break;	 
	     }
		     
	    ListTask.clear();
	     
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		executor.shutdown();
	}
	
	private double FileSize(URL url) throws IOException{
		
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			double file_size = (httpConn.getContentLength())*8*Math.pow(10, -6); // from byte to Mbit
			httpConn.disconnect();
			
		return file_size;
	}
	
	public ArrayList<Long> WifiPattern(){
		
		ArrayList<Long> wifi_on_off = new ArrayList<Long>();
		
		long interval_on = 0;
		long interval_off = 0;
		double sim_t = (urls.size() / (Constants.LAMBDA_IN / 1000.0))  / ((1.0 / Constants.L_ON + 1.0 / Constants.L_OFF)/2.0);
		int sim = (int) Math.ceil(sim_t / 2) + 5;
		
		if (Constants.debug) System.out.println("SIM: " + sim);
		
		Random rng = new Random();
		rng.setSeed(150);
		
		ExponentialGenerator gen_on = new ExponentialGenerator(Constants.L_ON, rng);
		ExponentialGenerator gen_off = new ExponentialGenerator(Constants.L_OFF, rng);
		
		for (int j=0; j < 12; j++){
			
			interval_on = (long) (interval_off + gen_on.nextValue());
			wifi_on_off.add(interval_on / 1000);
			
		    interval_off = (long) (interval_on + gen_off.nextValue());
		    wifi_on_off.add(interval_off / 1000);
		    
		    if (Constants.debug)  Log.e("TEST" , "ID: " + j + " Interval_ON: " + interval_on / 1000);   // in seconds
		    if (Constants.debug)  Log.e("TEST" , "ID: " + j + " Interval_OFF: " + interval_off / 1000);
		}
		
		
		return wifi_on_off;
	}
	
	public long GetTotalDelay(ArrayList<Long> wifi_pattern, int index, ExponentialGenerator arr){
		
		long d = (long) (arr_delay + arr.nextValue());
		arr_delay = d;
		Log.e("TEST", "ID: " + index + " ARRIVAL DELAY: " + d);
					
			for(int k=0; k < wifi_pattern.size(); k++){
								
				if (d <= wifi_pattern.get(k)){
								
					if ( (k % 2) == 0){
						
						d_wf = 0;
						if (Constants.debug) Log.i("TEST" , "ID: " + index +  " TOTAL DELAY: " + d);
						return d;
						
					}else{
						
						d_wf = wifi_pattern.get(k) - d;
						wifi_flow_index.add(index);
						delay_wifi.add(d_wf);
						//if (Constants.debug) Log.e("TEST" , "D_WF: " + d_wf);
						if (Constants.debug) Log.i("TEST" ,"ID: " + index + " TOTAL DELAY: " + wifi_pattern.get(k) + " D_WIFI: " + d_wf);
						return (wifi_pattern.get(k));
					}
				}		
		}
			
		return 0;
	}
	
	
	private class DelayedTask{
		
		public Task task;
		
		public long delay;
		public CompletionService ecs;
		public int ID;
		
		TimerTask TT;
		Timer timer = new Timer();
		
		public DelayedTask(final Task t, final long delay, final CompletionService ecs, int ID){
			task = t;
			this.delay = delay;
			this.ecs=ecs;
			this.ID=ID;
			StartTimer();
		}
		
		public void StartTimer(){
				
			TT = new TimerTask() {
				
					@Override
					public void run() {
						ecs.submit(task);
						if (Constants.debug) System.out.println("Delayed Flow: "+ID+" "+delay);
					}
										}; 		

			timer.schedule(TT, this.delay);
		}
	
		public void Cancel(){
			TT.cancel();
			timer.cancel();
		}
	}
	
}