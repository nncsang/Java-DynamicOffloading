package fr.eurecom.plots;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class RatesPlot extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.graphs);

		File dir = new File(Environment.getExternalStorageDirectory().getPath()+ "/SpringProject2014/Log");
		File[] files = dir.listFiles();
		for(int i=0; i<files.length; i++){
			if(files[i].getName().startsWith("statistics")){
					parseRatesLog(files[i]);
					break;
		 		}
		}
	}

	private static String getValue(String tag, Element element) {
		NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
		Node node = (Node) nodes.item(0);
		return node.getNodeValue();
	}

	private void parseRatesLog(File myFile){
		
		ArrayList<Integer> x_axis = new ArrayList<Integer>();
		ArrayList<Double> y_axis_1 = new ArrayList<Double>();
		ArrayList<Double> y_axis_2 = new ArrayList<Double>();
		ArrayList<Double> y_avgWifi = new ArrayList<Double>();
		ArrayList<Double> y_avgCell = new ArrayList<Double>();
		
		
		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = dBuilder.parse(myFile);
			doc.getDocumentElement().normalize();

			NodeList nodes = doc.getElementsByTagName("record");
			
			Node node = nodes.item(0);
			int tot_old=0;
			
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;

				String data = getValue("date", element);
				Pattern pattern = Pattern.compile("(0*[0-9]|1[0-2]):[0-5][0-9]:[0-5][0-9]");
				Matcher matcher = pattern.matcher(data);
				matcher.find();
				String hour = matcher.group(0);
				System.out.println(hour);
				String[] vett = hour.split(":");
				int sec = Integer.parseInt(vett[2]);
				int mm = Integer.parseInt(vett[1]);
				int hh = Integer.parseInt(vett[0]);
				tot_old = hh*3600+mm*60+sec;
				x_axis.add(0);
				String msg = new String(getValue("message", element));
				String[] parts=msg.split(" ");
				y_axis_1.add(Double.parseDouble(parts[0]));
				y_axis_2.add(Double.parseDouble(parts[1]));

			}
			
			int tot_new=0;
			int diff=0;
			double tot_wifi=0;
			double tot_cell=0;
			double aveRwf=0;
			double aveRc=0;
			for (int i = 0; i < nodes.getLength(); i++) {
				
				   node = nodes.item(i);
				
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					String data = getValue("date", element);
					Pattern pattern = Pattern.compile("(0*[0-9]|1[0-2]):[0-5][0-9]:[0-5][0-9]");
					Matcher matcher = pattern.matcher(data);
					matcher.find();
					String hour = matcher.group(0);
					String[] vett = hour.split(":");
					int sec = Integer.parseInt(vett[2]);
					int mm = Integer.parseInt(vett[1]);
					int hh = Integer.parseInt(vett[0]);
					tot_new = hh*3600+mm*60+sec;
					
					diff += tot_new-tot_old;
					if (Constants.debug) System.out.println("Hour: "+hour+" DIFF: "+diff);
					x_axis.add(diff);
					tot_old=tot_new;

					String msg = new String(getValue("message", element));
					String[] parts=msg.split(" ");
					y_axis_1.add(Double.parseDouble(parts[0]));
					y_axis_2.add(Double.parseDouble(parts[1]));
					tot_wifi +=Double.parseDouble(parts[0]);
					tot_cell +=Double.parseDouble(parts[1]);
				}
				
		}
			// DELIA average rates
			
			aveRwf = tot_wifi / nodes.getLength(); //Average rate wf
			aveRc = tot_cell / nodes.getLength(); //Average rate cell
			
			for(int j=0; j <= nodes.getLength(); j++){
				y_avgWifi.add(aveRwf);
				y_avgCell.add(aveRc);
			}
			
			
			if (Constants.debug) System.out.println("AVERAGE RATE WF "+aveRwf);
			if (Constants.debug) System.out.println("AVERAGE RATE cell "+aveRc);
			
			if (Constants.debug) System.out.println("SIZEEEEE X "+ x_axis.size());
			if (Constants.debug) System.out.println("SIZEEEEE Y1 "+y_axis_1.size());
			if (Constants.debug) System.out.println("SIZEEEEE Y2 "+y_axis_2.size());
			if (Constants.debug) System.out.println("SIZEEEEE Y3 "+y_avgWifi.size());
			if (Constants.debug) System.out.println("SIZEEEEE Y4 "+y_avgCell.size());
			
			PrintPlot(x_axis, y_axis_1,y_axis_2, y_avgWifi, y_avgCell);
			
	} catch (Exception ex) {
		ex.printStackTrace();
	}
 }
	
	public void PrintPlot(ArrayList<Integer> x_axis, ArrayList<Double> y_axis_1, ArrayList<Double> y_axis_2,
						  ArrayList<Double> y_avgWifi, ArrayList<Double> y_avgCell)
			throws DocumentException, MalformedURLException, IOException{
		String title = "Measured Rates"; 
		String x_name = "Time [s]";
		String y_name = "Rates [Mbit/s]";
		String line1 = "WiFi";
		String line2 = "Cell";
		String line3 = "avgWifi";
		String line4 = "avgCell";
		NewRatesPLot line = new NewRatesPLot(x_axis,y_axis_1,y_axis_2, y_avgWifi, y_avgCell, title, x_name, y_name, line1, line2, line3, line4);
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
		PdfWriter.getInstance(document,new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+ "/SpringProject2014/Rates.pdf"));
		document.open();
		img = Image.getInstance(bArray);
		if (document.add(img))
		Toast.makeText(getApplicationContext(), "Pdf created!", Toast.LENGTH_SHORT).show();
		document.close();
	}
	
}