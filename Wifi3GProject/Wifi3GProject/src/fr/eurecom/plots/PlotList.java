package fr.eurecom.plots;

import fr.eurecom.wifi3gproject.Constants;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class PlotList extends ListActivity{
	
	 String[] classes = null;
	 Bundle bundle;
	
		@Override
		protected void onListItemClick(ListView l, View v, int position, long id) {
			// TODO Auto-generated method stub
			super.onListItemClick(l, v, position, id);
			try {
				Intent ourIntent = new Intent(PlotList.this,Class.forName("fr.eurecom.plots."+classes[position]));
				startActivity(ourIntent);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			bundle = getIntent().getExtras();
			
			if(bundle.getInt("policy")==Constants.WIFI_3G || bundle.getInt("policy")==Constants.FLOW_BALANCE){
				classes=new String[6];
				classes[0] = "BatteryPlot";
				classes[1] = "RatesPlot";
				classes[2] = "ThresholdPlot";
				classes[3] = "AvgThroughputPlot";
				classes[4] = "DelayPlot";
				classes[5] = "FinalStat";
			}else{
				classes=new String[5];
				classes[0] = "BatteryPlot";
				classes[1] = "RatesPlot";
				classes[2] = "AvgThroughputPlot";
				classes[3] = "DelayPlot";
				classes[4] = "FinalStat";		
				}

			setListAdapter(new ArrayAdapter<String>(PlotList.this, android.R.layout.simple_list_item_1 ,classes));
		}

}
