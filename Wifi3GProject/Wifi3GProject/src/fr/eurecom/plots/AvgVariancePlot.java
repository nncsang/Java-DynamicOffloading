	package fr.eurecom.plots;

	import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

	import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

	import org.achartengine.GraphicalView;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

	import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.Toast;

	import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import fr.eurecom.wifi3gproject.Constants;
import fr.eurecom.wifi3gproject.R;

public class AvgVariancePlot extends Activity {

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.graphs);

			File dir = new File(Environment.getExternalStorageDirectory().getPath()+ "/SpringProject2014/Log");
			File[] files = dir.listFiles();
			for(int i=0; i<files.length; i++){
				if(files[i].getName().startsWith("data")){
						parseDataLog(files[i]);
						break;
			 		}
			}			
		}
		
		private static String getValue(String tag, Element element) {
			NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
			Node node = (Node) nodes.item(0);
			return node.getNodeValue();
		}

		private void parseDataLog(File myFile){
			
			ArrayList<Data> values = new ArrayList<Data>();
			
			try{
				
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				org.w3c.dom.Document doc = dBuilder.parse(myFile);
				doc.getDocumentElement().normalize();

				NodeList nodes = doc.getElementsByTagName("record");
				
				for (int i = 0; i < nodes.getLength(); i++) {
					
					 Node node = nodes.item(i);
					
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) node;
						String msg = new String(getValue("message", element));
						String[] parts = msg.split(" ");

						values.add(new Data(Double.parseDouble(parts[2]),Double.parseDouble(parts[3]),Double.parseDouble(parts[4])));
				}
					
			}
				
				ComputeAvgThroughput(values);
				
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	 }
		
		public void ComputeAvgThroughput(ArrayList<Data> values) throws MalformedURLException, DocumentException, IOException{
			
			ArrayList<Integer> x_axis = new ArrayList<Integer>();
			ArrayList<Double> y_axis_1 = new ArrayList<Double>();
			ArrayList<Double> y_axis_2 = new ArrayList<Double>();
			double Timeout = 2*1000; //milliseconds
			
			Collections.sort(values, new Comparator<Data>() {
				
				@Override
				public int compare(Data d0, Data d1) {
					if(d0.GetStartTime() > d1.GetStartTime()) return 1;
					if(d0.GetStartTime() < d1.GetStartTime()) return -1;
					return 0;
				}
		    });
			
			double interval = values.get(0).GetStartTime() + Timeout;
			
			Collections.sort(values, new Comparator<Data>() {
				
				@Override
				public int compare(Data d0, Data d1) {
					if(d0.GetEndTime() > d1.GetEndTime()) return 1;
					if(d0.GetEndTime() < d1.GetEndTime()) return -1;
					return 0;
				}
		    });

			double count=0;
			double size=0;
			double second_moment=0;
			int index=0;
			
			for(Data d : values){
				
					
				if(d.GetEndTime() > interval){
					x_axis.add(index);
					index+=Timeout;
					if(size==0 && count==0){
						y_axis_1.add(0.0);
					    y_axis_2.add(0.0);
					}else{
						if (Constants.debug) System.out.println("Janis is a great man avg: "+size/count);
						//System.out.println("Janis is a great man size2: "+second_moment+" count "+count);
						y_axis_1.add((size/count));
					    y_axis_2.add(((second_moment/count)-((size/count)*(size/count))));
					}
					interval += Timeout;
				}
				
				size+=d.GetSize();
				second_moment += d.GetSize()*d.GetSize();
				count++;
				
			}
			
			x_axis.add(index);
			y_axis_1.add((size/count));
			y_axis_2.add(((second_moment/count)-((size/count)*(size/count))));
			
			PrintPlot(x_axis, y_axis_1, y_axis_2);
		}
		
		
		public void PrintPlot(ArrayList<Integer> x_axis, ArrayList<Double> y_axis_1,ArrayList<Double> y_axis_2)
				throws DocumentException, MalformedURLException, IOException{
			String title = "Variance"; // TO DO 
			String x_name = "Sample";
			String y_name = "Variance";
			String line1 = "Mean";
			String line2 = "Variance";
			DoubleLineGraph line = new DoubleLineGraph(x_axis,y_axis_1,y_axis_2, title, x_name, y_name, line1, line2);
			GraphicalView gView = line.getView(this);
			LinearLayout Layout = (LinearLayout) findViewById(R.id.myLayout);
			Layout.addView(gView);
			
			Bitmap bitmap;
			Image img;
			byte[] bArray;
			
			Layout.setDrawingCacheEnabled(true);
			Layout.measure(MeasureSpec.makeMeasureSpec(400, MeasureSpec.AT_MOST ), MeasureSpec.makeMeasureSpec(400, MeasureSpec.AT_MOST ));
			Layout.layout(0,0, Layout.getMeasuredWidth(), Layout.getMeasuredHeight());
			Layout.buildDrawingCache(true);
			bitmap = Bitmap.createBitmap(Layout.getDrawingCache());
			Layout.setDrawingCacheEnabled(false);
			
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			bArray = stream.toByteArray();
			
			Document document=new Document();
			PdfWriter.getInstance(document,new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+ "/SpringProject2014/AvgVariance.pdf"));
			document.open();
			img = Image.getInstance(bArray);
			if (document.add(img))
			Toast.makeText(getApplicationContext(), "Pdf created!", Toast.LENGTH_SHORT).show();
			document.close();
		}
		
	    private class Data{
		
	    	private double start_time;
	    	private double end_time;
	    	private double total_bytes;
		
	    public Data(double start_time, double end_time, double total_bytes){
			this.start_time=start_time;
			this.end_time=end_time;
			this.total_bytes=total_bytes;	
		}
		
		public double GetStartTime(){
			return this.start_time;
		}
		
		public double GetEndTime(){
			return this.end_time;
		}
		
		public double GetSize(){
			return this.total_bytes;
		}
	}
		
		
	}
