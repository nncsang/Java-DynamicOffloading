package fr.eurecom.wifi3gproject;

public class Profiles {
	
	//double[] lambda = {0.1, 0.5, 1000.0};
	String[] number_of_files = {"warm_up.txt","wifi_on_off.txt","new_urls_1350MB.txt", "short_list.txt"};
	int[] number_of_threads = {4, 50, 150};
	double[] utilization = {0.1, 0.5, 0.9};
	
	public void warmUp(){
		Constants.URL_NAME = number_of_files[0];
		Constants.THREADS = number_of_threads[0];
		Constants.debug = false;
		Constants.enable_arrival_pattern = false;
		Constants.enable_wifi_on_off_pattern = false;
		Constants.SYSTEM_UTILIZATION = utilization[2];
	}
	
	public void overloadedNetwork(){
		Constants.URL_NAME = number_of_files[1];
		Constants.THREADS = number_of_threads[0];
		Constants.debug = false;
		Constants.enable_arrival_pattern = false;
		Constants.enable_wifi_on_off_pattern = false;
	}
	
	public void sparseNetworkWiFiOnOffPatternDisabled(double arrival_rate){
		Constants.LAMBDA_IN = arrival_rate;
		Constants.URL_NAME = number_of_files[1];
		Constants.THREADS = number_of_threads[0];
		Constants.debug = false;
		Constants.enable_arrival_pattern = true;
		Constants.enable_wifi_on_off_pattern = false;
	}
	
	public void sparseNetworkWiFiOnOffPatternEnabled(double arrival_rate, double on, double off){
		Constants.LAMBDA_IN = arrival_rate;
		Constants.URL_NAME = number_of_files[3];
		Constants.THREADS = number_of_threads[0];
		Constants.debug = false;
		Constants.enable_arrival_pattern = true;
		Constants.enable_wifi_on_off_pattern = true;
		Constants.T_ON = on;
		Constants.T_OFF = off;
		
		Constants.L_ON = 1.0 / (Constants.T_ON * 1000.0);
		Constants.L_OFF = 1.0 / (Constants.T_OFF * 1000.0);
	}
}
