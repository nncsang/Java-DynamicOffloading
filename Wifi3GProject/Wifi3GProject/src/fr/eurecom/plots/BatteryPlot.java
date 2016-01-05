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

public class BatteryPlot extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.graphs);

		File dir = new File(Environment.getExternalStorageDirectory().getPath()+ "/SpringProject2014/Log");
		File[] files = dir.listFiles();
		for(int i=0; i<files.length; i++){
			if(files[i].getName().startsWith("battery")){
					parseBatteryLog(files[i]);
					break;
		 		}
		}			
	}
	private static String getValue(String tag, Element element) {
		NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
		Node node = (Node) nodes.item(0);
		return node.getNodeValue();
	}

	private void parseBatteryLog(File myFile){
		
		ArrayList<Integer> x_axis = new ArrayList<Integer>();
		ArrayList<Double> y_axis = new ArrayList<Double>();
		
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
				if (Constants.debug) System.out.println(hour);
				String[] vett = hour.split(":");
				int sec = Integer.parseInt(vett[2]);
				int mm = Integer.parseInt(vett[1]);
				int hh = Integer.parseInt(vett[0]);
				tot_old = hh*3600+mm*60+sec;
				x_axis.add(0);
				
				y_axis.add(Double.parseDouble(getValue("message", element)));

			}
			
			int tot_new=0;
			int diff=0;
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

					y_axis.add(Double.parseDouble(getValue("message", element)));
				}
	
		}
			if (Constants.debug) System.out.println("SIZEEEEE X "+x_axis.size());
			if (Constants.debug) System.out.println("SIZEEEEE Y "+y_axis.size());
			PrintPlot(x_axis, y_axis);
	} catch (Exception ex) {
		ex.printStackTrace();
	}
 }
	
	public void PrintPlot(ArrayList<Integer> x_axis, ArrayList<Double> y_axis)
			throws DocumentException, MalformedURLException, IOException{
		String title = "Battery level"; 
		String x_name = "Time [s]"; //"Time [s]"; // DC: TO MODIFY
		String y_name = "Battery [%]";
		LineGraph line = new LineGraph(x_axis,y_axis,title,x_name,y_name);
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
		PdfWriter.getInstance(document,new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+ "/SpringProject2014/BatteryLevel.pdf"));
		document.open();
		img = Image.getInstance(bArray);
		if (document.add(img))
		Toast.makeText(getApplicationContext(), "Pdf created!", Toast.LENGTH_SHORT).show();
		document.close();
	}
	
}