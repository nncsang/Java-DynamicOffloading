package fr.eurecom.plots;

import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;

public class LineGraph {

	ArrayList<Integer> x_axis = new ArrayList<Integer>();
	ArrayList<Double> y_axis = new ArrayList<Double>();
	String title;
	String x_name;
	String y_name;

	public LineGraph(ArrayList<Integer> x_axis, ArrayList<Double> y_axis, String title, String x_name, String y_name) {
		this.x_axis = x_axis;
		this.y_axis = y_axis;
		this.title = title;
		this.x_name = x_name;
		this.y_name = y_name;
	}

	public GraphicalView getView(Context context) {
			
		TimeSeries series = new TimeSeries("");
			
		for (int i = 0; i < x_axis.size(); i++) {
				series.add(x_axis.get(i), y_axis.get(i));
			}
			
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(series);
	
		// Customize 1st line
		XYSeriesRenderer renderer = new XYSeriesRenderer();
		renderer.setColor(Color.BLACK);
		renderer.setPointStyle(PointStyle.SQUARE);
		renderer.setFillPoints(true);
		
		// Customize Graph Settings
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		mRenderer.addSeriesRenderer(renderer);
	
		mRenderer.setChartTitle(title);
		mRenderer.setXTitle(x_name);
		mRenderer.setYTitle(y_name);
		mRenderer.setAxesColor(Color.BLACK);
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setBackgroundColor(Color.WHITE);
        mRenderer.setMarginsColor(Color.WHITE);
        mRenderer.setLabelsColor(Color.BLACK);
		mRenderer.setShowGrid(true);
		mRenderer.setYAxisMin(0, 0);
		//mRenderer.setLabelsTextSize(20);
		mRenderer.setPointSize(2);
		mRenderer.setAxisTitleTextSize(20);
		mRenderer.setLabelsTextSize(20);
		mRenderer.setXLabelsColor(Color.BLACK);
		mRenderer.setYLabelsColor(0, Color.BLACK);
		
		return ChartFactory.getLineChartView(context, dataset, mRenderer);
	}

}
