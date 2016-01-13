package fr.eurecom.wifi3gproject;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import fr.eurecom.plots.PlotList;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;




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
	
	/* ADDED FOR DELAY OFFLOADING */
	public static long currTime = 0;
	private final Lock _mutex = new ReentrantLock(true);
	private final Lock _mutex1 = new ReentrantLock(true);
	private final ArrayList<Long> DelayPattern = new ArrayList<Long>();
	static final ArrayList<Task> Tasks = new ArrayList<Task>();
	
	/*
	 *  WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
	 *  Set WiFi ON = wifiManager.setWifiEnabled(true);
	 *   
	 *   Set WiFi OFF = boolean wifiEnabled = wifiManager.isWifiEnabled()
	 */
	
	public final WifiManager wifiManager;
	CompletionService ecs;
	
	public Timer t;
	
	public DownloadFilesTask(ArrayList<String> urls, int policy, boolean parallel, Activity myAct) {
		
		/* ADDED FOR DELAY OFFLOADING 
		 * 
		 * total_processed: number of files are downloaded successfully or failly
		 * 
		 * */
		total_processed = 0;
		
		/* ADDED FOR DELAY OFFLOADING */
		this.wifiManager = (WifiManager) myAct.getSystemService(Context.WIFI_SERVICE); 
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
		
		int i = 0;
		for(String url: urls){
			System.out.println(i + ": " + url);
			i++;
		}
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		start_downloadfilestask = System.currentTimeMillis();
		//System.out.println("WIFI USAGE START: " + (TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes()) / 1048576.0);
		//System.out.println("CELL USAGE START: " + TrafficStats.getMobileRxBytes() / 1048576.0);
		//System.out.println("TOTAL USAGE START: " + TrafficStats.getTotalRxBytes() / 1048576.0);
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
		//ecs = new ExecutorCompletionService(executor);
		if (Constants.debug) System.out.println("#### Sequential");
		
		for(int i=0; i<urls.size(); i++){
			
			/* ADDED FOR DELAY OFFLOADING 
			 * 
			 * Set task's state to CREATED
			 * 
			 * */
			Constants.task_state.add(Constants.TASK_STATE.CREATED);
			double task_start_time = System.currentTimeMillis();
			FutureTask<String> future = new FutureTask<String>(new Task(urls.get(i), i, 0, urls.size(), policy, task_start_time, this));
			executor.execute(future);
			
				try {
					System.out.println("Result of thread " + i + ": " + future.get());
					//publishProgress(1);	
					
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						System.out.println(e.toString());
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
			
		if(isCancelled())break;
		}
		executor.shutdown();
	return;
	
	}
	
	public void ParalellExecution() throws MalformedURLException, IOException{
		
		//System.out.println("HERE 3");
		
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
		
		final int MAX_THREADS = Constants.THREADS; 
		executor = Executors.newFixedThreadPool(MAX_THREADS);
		ecs = new ExecutorCompletionService(executor);
		if (Constants.debug) System.out.println("#### Parallel: "+ MAX_THREADS);
		
		
		
		int N = urls.size();
		long s = System.currentTimeMillis();
		for(int i=0; i < N; i++){
		//for(int i=0; i < N; i++){

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

						//delay = GetTotalDelay(wifi_on_off_pattern, i, arr);
						//if (Constants.debug)
						//	System.out.println("Flow: " + i
						//			+ " ARRIVAL Delay: " + delay + " WIFI");
						delay = (long) (arr_delay + arr.nextValue());
						sum_dwf += d_wf;
						count_wifi++;
						arr_delay = delay;
						Log.v("DELAY_WIFI", "D_WIFI: " + d_wf + " COUNT: " + count_wifi);
						
					} else {
						delay = (long) (arr_delay + arr.nextValue());
						arr_delay = delay;
						Log.e("TEST", "ID: " + i + " TOTAL DELAY: " + delay);
					}
				}
			}
			
			
			/* ADDED FOR DELAY OFFLOADING 
			 * 
			 * Create tasks and save task's state
			 * 
			 * */
			double task_start_time = System.currentTimeMillis();
			DelayPattern.add(delay);
			Task t = new Task(urls.get(i), i, delay * 1000, urls.size(), policy, task_start_time, this);
			Tasks.add(t);
			Constants.task_state.add(Constants.TASK_STATE.CREATED);
			//ListTask.add(new DelayedTask(t, delay * 1000, ecs, i));
			
			
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
		
		/* ADDED FOR DELAY OFFLOADING 
		 * 
		 * Handle WiFi ON OFF Pattern
		 * 
		 * */
		if (Constants.enable_wifi_on_off_pattern) {
			final ArrayList<Long> wifi_pattern = WifiPattern();
			
			
			/* ADDED FOR DELAY OFFLOADING 
			 * 
			 * Create global timer
			 * 
			 * */
			t = new Timer();
			t.scheduleAtFixedRate(new TimerTask(){
	
				@Override
				public void run() {
					_mutex.lock();
					
					
					/* ADDED FOR DELAY OFFLOADING 
					 * 
					 * Check if it is an ON or OFF period
					 * 
					 * wi_fi pattern:
					 * 
					 * 10 20 30 40 50
					 * 
					 * 0-10: ON
					 * 11-20: OFF
					 * 21-30: ON
					 * 
					 * */
					int index = -1;
					for(int k=wifi_pattern.size() - 1; k >=0; k--){
						if (currTime <= wifi_pattern.get(k)){
							index = k;
						}else{
							break;
						}
					}
								
					if ( (index % 2) == 0){
						/* WIFI: ON PERIOD */
						
						if (Constants.IS_WIFI_AVAILABLE == false){
							System.out.println("-------------------------------------");
							System.out.println("************** WIFI ON **************");
							System.out.println("-------------------------------------");
							
							
							/* PERIOD TRANSFER FROM OFF TO ON */
							
							/* Restart ExecutorService */
							executor = Executors.newFixedThreadPool(MAX_THREADS);
							ecs = new ExecutorCompletionService(executor);
							
							/* Submit all Tasks in queue to ExecutorService 
							 * And set these Tasks' state to SUMITTED_TO_EXECUTOR: waiting in queue to execute
							 * Clear queue
							 * 
							 * */
							if (Constants.queue.size() > 0){
								for(int i = 0; i < Constants.queue.size(); i++){
									int task_id = Constants.queue.get(i);
									ecs.submit(Tasks.get(task_id));
									
									Constants.task_state.set(task_id, Constants.TASK_STATE.SUMITTED_TO_EXECUTOR);
									System.out.println("Take " + Tasks.get(Constants.queue.get(i)).ID + " out from queue and add to executor's queue");
								}
								
								Constants.queue.clear();
							}
						}
						
						// Set state of wifi to ON
						Constants.IS_WIFI_AVAILABLE = true;

						
						/* 
						 * Check for there are any incoming tasks
						 * Submit these tasks to  ExecutorService
						 * Set these tasks' state to SUMITTED_TO_EXECUTOR
						 * 
						 * */
						for(int i = 0; i < Tasks.size(); i++){
							if (Tasks.get(i).delay == currTime * 1000){
								//System.out.println(Tasks.get(i).delay);
								System.out.println("Time: " + currTime + " - ID: " + i + " coming and is added to executor's queue");
								ecs.submit(Tasks.get(i));
								Constants.task_state.set(i, Constants.TASK_STATE.SUMITTED_TO_EXECUTOR);
							}
						}
						
						/* */
						
						
					}else{
						/* WIFI: OFF PERIOD */
						
						/* PERIOD TRANSFER FORM ON TO OFF */
						if (Constants.IS_WIFI_AVAILABLE == true){
							System.out.println("-------------------------------------");
							System.out.println("************** WIFI OFF**************");
							System.out.println("-------------------------------------");
							
							/* Logically turn-off wifi by shutting down ExecutorService */
							executor.shutdownNow();

							/* 
							 * 
							 * Add all running tasks to queue
							 * Set these tasks to ADDED_TO_QUEUE
							 * 
							 * */
							for(int i = 0; i < Tasks.size(); i++){
								
								if (Constants.task_state.get(i) == Constants.TASK_STATE.RUNNING){
									
									/** TODO: add tasks in right order */
									//Tasks.get(i).cancled();
									Constants.queue.add(i); 
									Constants.task_state.set(i, Constants.TASK_STATE.ADDED_TO_QUEUE);
									System.out.println("Add running " + i + " to queue");
								}
							}
							
							
							/* 
							 * 
							 * Add all waiting tasks in ExecutorService to queue
							 * Set these tasks to ADDED_TO_QUEUE
							 * 
							 * */
							for(int i = 0; i < Tasks.size(); i++){
								if (Constants.task_state.get(i) == Constants.TASK_STATE.SUMITTED_TO_EXECUTOR){
									
									/** TODO: add task in right order */
									Constants.queue.add(i); 
									Constants.task_state.set(i, Constants.TASK_STATE.ADDED_TO_QUEUE);
									System.out.println("Add waitting " + i + " to queue");
								}
							}
						}
						
						/* Set state of WiFi to OFF */
						Constants.IS_WIFI_AVAILABLE = false;
							
						/* 
						 * Check for there is any incoming tasks
						 * Submit these tasks to  queue
						 * Set these tasks' state to ADDED_TO_QUEUE
						 * 
						 * */
						
						for(int i = 0; i < Tasks.size(); i++){
							if (Tasks.get(i).delay == currTime * 1000){
								Constants.queue.add(i);
								Constants.task_state.set(i, Constants.TASK_STATE.ADDED_TO_QUEUE);
								System.out.println("Add coming " + i + " to queue");
							}
						}
					}
						
					
					//if (currTime % 20 == 0){
					/*
					if (Constants.IS_WIFI_AVAILABLE == true){
						System.out.println("Current time: " + currTime + " -> WIFI ON PERIOD " + index/2 + ". Queue size: " + Constants.queue.size());
					}else{
						System.out.println("Current time: " + currTime + " -> WIFI OFF PERIOD " + index / 2 + ". Queue size: " + Constants.queue.size());
					}
					*/
					
					
					
					//}
					currTime++;
					_mutex.unlock();
				}
			}, 1000, 1000);
			

			try {
				 while(true){
					 _mutex1.lock();
					 
					 /* If all tasks are processed --> break */
					 if (total_processed == Tasks.size())
						 break;
					 _mutex1.unlock();
					 
					 Future f = ecs.take();
					 //System.out.println("Result of thread: " + f.get()); 
					 if (isCancelled())
						 break;
				 }
				
			     
				} catch (Exception e) {
					e.printStackTrace();
				}
		}else{
			
			double task_start_time = System.currentTimeMillis();
			for(int i = 0; i < N; i++){
				Task t = new Task(urls.get(i), i, DelayPattern.get(i) * 1000, urls.size(), policy, task_start_time, this);
				ListTask.add(new DelayedTask(t, DelayPattern.get(i) * 1000, ecs, i));
			}
			try { 
		
		     for (int i = 0; i < N; i++) {
		         Future f = ecs.take();
		         //System.out.println("Result of thread: " + f.get());
		         //if (Constants.debug){
		        	
		        	 //if (!f.get().toString().startsWith("SUCCESS")) LoggerManager.LogErrors("Result of thread: " + f.get());
		         //}
		     	 //publishProgress(1);
		     	 if(isCancelled())break;	 
		     }
			     
		    ListTask.clear();
		     
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			executor.shutdown();
		}
		
	}
	
	public void cancel_task_handler(int ID){
		_mutex1.lock();
		System.out.println("Task " + ID + " is canceled");
		_mutex1.unlock();
	}
	public int total_processed = 0;
	public void notifyProgress(){
		_mutex1.lock();
		publishProgress(1);
		total_processed++;
		if (total_processed == Tasks.size()){
		    t.cancel();
		    executor.shutdownNow();
		    this.cancel(true);
		    MainActivity.progressDialog.getButton(DialogInterface.BUTTON_NEGATIVE).callOnClick();
		    
		}
		_mutex1.unlock();
		
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