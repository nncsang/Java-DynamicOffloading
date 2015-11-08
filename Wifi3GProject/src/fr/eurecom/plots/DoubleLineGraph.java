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

public class DoubleLineGraph {
	
	ArrayList<Integer> x_axis = new ArrayList<Integer>();
	ArrayList<Double> y_axis_1 = new ArrayList<Double>();
	ArrayList<Double> y_axis_2 = new ArrayList<Double>();
	String title;
	String x_name;
	String y_name;
	String line1;
	String line2;

	public DoubleLineGraph(ArrayList<Integer> x_axis, ArrayList<Double> y_axis_1, ArrayList<Double> y_axis_2, String title, String x_name, String y_name, String line1, String line2) {
		this.x_axis = x_axis;
		this.y_axis_1 = y_axis_1;
		this.y_axis_2 = y_axis_2;
		this.title = title;
		this.x_name = x_name;
		this.y_name = y_name;
		this.line1 = line1;
		this.line2 = line2;
	}

	public GraphicalView getView(Context context) {
			
		TimeSeries series_1 = new TimeSeries(line1);
			
		
		for (int i = 0; i < x_axis.size(); i++) {
				series_1.add(x_axis.get(i), y_axis_1.get(i));
			}
			
		TimeSeries series_2 = new TimeSeries(line2);
			
		
		for (int i = 0; i < x_axis.size(); i++) {
				series_2.add(x_axis.get(i), y_axis_2.get(i));
			}

		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(series_1);
		dataset.addSeries(series_2);
	
		// Customize 1st line
		XYSeriesRenderer renderer_1 = new XYSeriesRenderer();
		renderer_1.setColor(Color.GREEN);
		renderer_1.setPointStyle(PointStyle.SQUARE);
		renderer_1.setFillPoints(true);
		
		// Customize 2st line
		XYSeriesRenderer renderer_2 = new XYSeriesRenderer();
		renderer_2.setColor(Color.RED);
		renderer_2.setPointStyle(PointStyle.POINT);
		renderer_2.setFillPoints(true);
		
		// Customize Graph Settings
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		mRenderer.addSeriesRenderer(renderer_1);
		mRenderer.addSeriesRenderer(renderer_2);
	
		mRenderer.setChartTitle(title);
		mRenderer.setBackgroundColor(Color.WHITE);
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setMarginsColor(Color.WHITE);
		mRenderer.setShowGrid(true);
		mRenderer.setYAxisMin(0, 0);
		mRenderer.setXTitle(x_name);
		mRenderer.setYTitle(y_name);
		mRenderer.setPointSize(2);
		mRenderer.setAxisTitleTextSize(20);
		mRenderer.setLabelsTextSize(20);
		mRenderer.setLabelsColor(Color.BLACK);
		mRenderer.setXLabelsColor(Color.BLACK);
		mRenderer.setYLabelsColor(0, Color.BLACK);
		
		return ChartFactory.getLineChartView(context, dataset, mRenderer);
	}

}
