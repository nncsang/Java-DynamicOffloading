package fr.eurecom.wifi3gproject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.IBinder;
import android.preference.PreferenceManager;

public class BatteryService extends Service {

        private static BroadcastReceiver batteryInfo = new BroadcastReceiver() {
            
        	long TimeOld = 0;
        	long TimeOut = Constants.INTERVAL_BATTERY*1000; // every INTERVAL_BATTERY seconds
        	
                @Override
                public void onReceive(Context context, Intent intent) {
                    
                	    long TimeNew = System.currentTimeMillis();
                     
                	    //if OFF then ignore
                        String status = getONOFF(context);
                        if(status.equalsIgnoreCase("OFF")) {
                            return;
                        }
                       
                       
                        String action = intent.getAction();
                       
                        if(Intent.ACTION_BATTERY_CHANGED.equals(action) && (TimeNew-TimeOld)>TimeOut) {
                                                       
                            int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                            int level = -1;
                            if (rawlevel >= 0 && scale > 0) {
                                level = (rawlevel * 100) / scale;
                            }
                            //save
                            LoggerManager.LogBatteryLevel(level);
                            if (Constants.debug) System.out.println("level battery: ######## "+level+" ##### time: "+(TimeNew-TimeOld));
                            TimeOld=TimeNew;
                        }
                }
        };
       
        @Override
        public IBinder onBind(Intent intent) {
                // TODO Auto-generated method stub
                return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
                //register for intent
                registerReceiver(batteryInfo, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                return START_STICKY;
        }
       
        public static String getONOFF(Context context) {
            String status = "";
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            status = settings.getString("OnOff", "OFF");
            return status;
        }
}