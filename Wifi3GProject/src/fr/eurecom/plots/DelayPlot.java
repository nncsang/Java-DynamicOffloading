package fr.eurecom.plots;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

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
import fr.eurecom.wifi3gproject.DownloadFilesTask;
import fr.eurecom.wifi3gproject.R;

public class DelayPlot extends Activity {

			@Override
			protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.graphs);

				File dir = new File(Environment.getExternalStorageDirectory().getPath()+ "/SpringProject2014/Log");
				File[] files = dir.listFiles();
				for(int i=0; i<files.length; i++){
					if(files[i].getName().startsWith("data")){
							parseDelayLog(files[i]);
							break;
				 		}
				}			
			}
			private static String getValue(String tag, Element element) {
				NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
				Node node = (Node) nodes.item(0);
				return node.getNodeValue();
			}

			private void parseDelayLog(File myFile){
				
				ArrayList<Integer> x_axis = new ArrayList<Integer>();
				ArrayList<Double> y_axis_1 = new ArrayList<Double>();
				ArrayList<Double> y_axis_2 = new ArrayList<Double>();
				
				try{
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					org.w3c.dom.Document doc = dBuilder.parse(myFile);
					doc.getDocumentElement().normalize();

					NodeList nodes = doc.getElementsByTagName("record");
					double tot=0;
					double sum_req=0;
					//double ave_D=0;
					for (int i = 0; i < nodes.getLength(); i++) {
						
						boolean check = true;
						Node node = nodes.item(i);
						
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element element = (Element) node;
							x_axis.add(i);
							String msg = new String(getValue("message", element));
							String[] parts=msg.split(" ");
							
							int id = Integer.parseInt(parts[0]);
							
							if(DownloadFilesTask.wifi_flow_index.size() != 0){
								
								for (int j=0; j < DownloadFilesTask.wifi_flow_index.size(); j++){
									
									if ( (id == DownloadFilesTask.wifi_flow_index.get(j))){
										//double temp =  (Double.parseDouble(parts[3]) - Double.parseDouble(parts[2]) + Double.parseDouble(parts[7]) + DownloadFilesTask.delay_wifi.get(j)) / 1000;
										double temp =  (Double.parseDouble(parts[3]) - Double.parseDouble(parts[2]) + DownloadFilesTask.delay_wifi.get(j)) / 1000;
										y_axis_1.add(temp);
										tot = tot + temp;
										sum_req += Double.parseDouble(parts[7]);
										if (Constants.debug) System.out.println("WIFI DELAYED ID: " + id + "DELAY: " + temp);
										check = false;
										break;
									}
								}
								
							}
								if (check){
									//double temp2 =  (Double.parseDouble(parts[3]) - Double.parseDouble(parts[2]) + Double.parseDouble(parts[7])) / 1000;
									double temp2 =  (Double.parseDouble(parts[3]) - Double.parseDouble(parts[2])) / 1000;
									y_axis_1.add(temp2);
									tot = tot + temp2;
									sum_req += Double.parseDouble(parts[7]);
									if (Constants.debug) System.out.println("NORMAL DELAYED ID: " + id + "DELAY: " + temp2);
								}		
						}		
				}
					// DELIA average delay
					for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
						if (node.getNodeType() == Node.ELEMENT_NODE) {
							Element element = (Element) node;
							y_axis_2.add((tot/nodes.getLength())); //Average delay
						}
				    }
					if (Constants.debug) System.out.println("SIZEEEEE X " + x_axis.size());
					if (Constants.debug) System.out.println("SIZEEEEE Y1 " + y_axis_1.size());
					if (Constants.debug) System.out.println("SIZEEEEE Y2 " + y_axis_2.size());
					if (Constants.debug) System.out.println(" AVERAGE REQ DELAY: " + (sum_req / nodes.getLength()));
					
					PrintPlot(x_axis, y_axis_1, y_axis_2);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		 }
			
			public void PrintPlot(ArrayList<Integer> x_axis, ArrayList<Double> y_axis_1, ArrayList<Double> y_axis_2)
					throws DocumentException, MalformedURLException, IOException{
				String title = "Per-flow delay"; 
				String x_name = "Flow id";
				String y_name = "Delay [s]";
				String line1 = "Flow";
				String line2 = "Mean";
				//LineGraph line = new LineGraph(x_axis,y_axis,title,x_name,y_name);
				DoubleLineGraph line = new DoubleLineGraph(x_axis,y_axis_1,y_axis_2, title, x_name, y_name, line1, line2);
				GraphicalView gView = line.getView(this);
				LinearLayout Layout = (LinearLayout) findViewById(R.id.myLayout);
				Layout.addView(gView);
				
				Bitmap bitmap;
				Image img;
				byte[] bArray;
				
				// creazione pdf file
				
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
				PdfWriter.getInstance(document,new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+ "/SpringProject2014/Delay.pdf"));
				document.open();
				img = Image.getInstance(bArray);
				if (document.add(img))
				Toast.makeText(getApplicationContext(), "Pdf created!", Toast.LENGTH_SHORT).show();
				document.close();
			}
			
		}