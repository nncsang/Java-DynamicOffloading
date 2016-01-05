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

public class NewRatesPLot {

	ArrayList<Integer> x_axis = new ArrayList<Integer>();
	ArrayList<Double> y_axis_1 = new ArrayList<Double>();
	ArrayList<Double> y_axis_2 = new ArrayList<Double>();
	ArrayList<Double> y_avgWifi = new ArrayList<Double>();
	ArrayList<Double> y_avgCell = new ArrayList<Double>();
	String title;
	String x_name;
	String y_name;
	String line1;
	String line2;
	String line3;
	String line4;

	public NewRatesPLot(ArrayList<Integer> x_axis, ArrayList<Double> y_axis_1,
			ArrayList<Double> y_axis_2, ArrayList<Double> y_avgWifi,
			ArrayList<Double> y_avgCell, String title, String x_name,
			String y_name, String line1, String line2, String line3,
			String line4) {
		this.x_axis = x_axis;
		this.y_axis_1 = y_axis_1;
		this.y_axis_2 = y_axis_2;
		this.y_avgWifi = y_avgWifi;
		this.y_avgCell = y_avgCell;
		this.title = title;
		this.x_name = x_name;
		this.y_name = y_name;
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.line4 = line4;
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
		
		TimeSeries series_3 = new TimeSeries(line3);

		for (int i = 0; i < x_axis.size(); i++) {
			series_3.add(x_axis.get(i), y_avgWifi.get(i));
		}
		
		TimeSeries series_4 = new TimeSeries(line4);

		for (int i = 0; i < x_axis.size(); i++) {
			series_4.add(x_axis.get(i), y_avgCell.get(i));
		}

		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		dataset.addSeries(series_1);
		dataset.addSeries(series_2);
		dataset.addSeries(series_3);
		dataset.addSeries(series_4);

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
		
		XYSeriesRenderer renderer_3 = new XYSeriesRenderer();
		renderer_3.setColor(Color.BLUE);
		renderer_3.setPointStyle(PointStyle.DIAMOND);
		renderer_3.setFillPoints(true);
		
		XYSeriesRenderer renderer_4 = new XYSeriesRenderer();
		renderer_4.setColor(Color.GRAY);
		renderer_4.setPointStyle(PointStyle.CIRCLE);
		renderer_4.setFillPoints(true);

		// Customize Graph Settings
		XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
		mRenderer.addSeriesRenderer(renderer_1);
		mRenderer.addSeriesRenderer(renderer_2);
		mRenderer.addSeriesRenderer(renderer_3);
		mRenderer.addSeriesRenderer(renderer_4);

		//mRenderer.setChartTitle(title);
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
