package dk.itu.activityrecorder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;
import android.util.Log;



// Class not used yet. Probably won't be used.
public class SaveData {
	
	String printDataTag = "";
	String buttonTag = "";
	public SaveData() {
		
	}
	
	public List<String> createDataSeries(List<String> list)
	{
		List<String> temp = new ArrayList<String>();
		
		
		
		return temp;
	}
	
	public void saveAsCsv(String nameOfFile, List<String> data){
		
		Log.v(buttonTag,"path: "+nameOfFile);
		
		// Create a path to the file to be used when the recording is saved to the phone.
		String fpathExternal = Environment.getExternalStorageDirectory().getAbsolutePath()+"/ActivityRecorder/"+nameOfFile+".txt";
		// Create a path to the file to be used when the recording is saved to the phone. For the original file.
		String fpathExternalOriginal = Environment.getExternalStorageDirectory().getAbsolutePath()+"/ActivityRecorder/"+"O"+nameOfFile+".txt";
		
		
		// Check if the file exists. If it does not exist then I need to create it. Both files.
		File logFile = new File(fpathExternal);
		File logFileOriginal = new File(fpathExternalOriginal);
		if (!logFile.exists()) {
			try
			{
				logFile.createNewFile();
		    } 
		    catch (IOException e)
		    {
		         
		    	e.printStackTrace();
		    }
		}
		
		if (!logFileOriginal.exists()) {
			try
			{
				logFileOriginal.createNewFile();
		    } 
		    catch (IOException e)
		    {
		         
		    	e.printStackTrace();
		    }
		}
		
		// Process the raw data. I apply a gaussian filter to the data. Using excel I figured out that this was 
		// a good filter and it smoothed our data sufficiently.
		List<String> processedData = gaussianFilter(data,new int[]{1,4,7,10,15,21,28,32,40,32,28,21,15,10,7,4,1});
		
		// Buffered writers to write the data to a file.
		BufferedWriter buf = null;
		BufferedWriter bufOriginal = null;
		
		
		try{
			buf = new BufferedWriter(new FileWriter(logFile,true));
			bufOriginal = new BufferedWriter(new FileWriter(logFileOriginal,true));
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		
		// Write the processed data to a file.
		if(processedData != null)
		{
			for(int i = 0; i < processedData.size();i++)
			{
				Log.v(printDataTag,processedData.get(i).toString());
				try{
				buf.append(processedData.get(i).toString());
				buf.append("\n");
				}
				catch(Exception e){
					Log.v(printDataTag,"Exception eas: "+e);
				}
			}	
			Log.v(printDataTag,"Size of data: "+processedData.size());
		}
		
		// Write the unprocessed data to a file.  Raw data from the accelerometer.
		if(data != null)
		{
			for(int i = 0; i < data.size();i++)
			{
				Log.v(printDataTag,data.get(i).toString());
				try{
				bufOriginal.append(data.get(i).toString());
				//buf.append(data.get(i).toString());
				bufOriginal.append("\n");
				}
				catch(Exception easf){
					Log.v(printDataTag,"Exception sdfsd: "+easf);
				}
			}	
			Log.v(printDataTag,"Size of data: "+data.size());
		}
		
		// Close the buffered writers.
		try {
			buf.close();
			bufOriginal.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void saveAsArff(){
		
	}
	
	public List<String> gaussianFilter(List<String> list, int[] weights) {
		List<String> temp = new ArrayList<String>();
		
		List<String> listToWorkWith = new ArrayList<String>();
		listToWorkWith.addAll(list);
		
		listToWorkWith.remove(0);
		
		String[] timestamp = new String[listToWorkWith.size()];
		float[] x = new float[listToWorkWith.size()];
		float[] y = new float[listToWorkWith.size()];
		float[] z = new float[listToWorkWith.size()];
		String[] activity_label = new String[listToWorkWith.size()];
		
		
		
		for(int i = 0;i<listToWorkWith.size();i++){
		//for(String s : list){
			
			try{
				String[] split = listToWorkWith.get(i).split(",");
				timestamp[i] = split[0];
				x[i] = Float.parseFloat(split[1]);
				y[i] = Float.parseFloat(split[2]);
				z[i] = Float.parseFloat(split[3]);
				activity_label[i] = split[4];
			}
			catch(Exception e){
				continue;
			}
		}
		
		
		
		float[] xGaussian = applyGaussianFilter(x, weights);
		float[] yGaussian = applyGaussianFilter(y, weights);
		float[] zGaussian = applyGaussianFilter(z, weights);
		
		temp.add("timestamp,x,y,z,activity_labelLALA");
		
		for(int e = 0; e < listToWorkWith.size(); e++){
			String h = timestamp[e]+","+xGaussian[e]+","+yGaussian[e]+","+zGaussian[e]+","+activity_label[e];
			temp.add(h);
		}
		
		return temp;
	}
	
	public float[] applyGaussianFilter(float[] values, int[] weights){
		float[] temp = new float[values.length];
		
		float[] digitsAround = new float[weights.length];
		
		int offset = (weights.length / 2);
		
		int denominator = 0;
		for(int r : weights){
			denominator += r;
		}
		
		for(int f = 0; f < values.length; f++){
			float numberToFilter = values[f];
			//digitsAround = new float[weights.length];
			int index = 0;
			
			for(int e = 0; e <  weights.length; e++){
				int nextNumber = e;
				//|| (e+f)+offset > values.length
				if((e+f) - offset < 0 || (e+f) - offset >= values.length){
					digitsAround[e] = 0;
				}
				else{
					digitsAround[e] = values[(e+f)-offset];
				
				}
				index++;
			}
			
			float temporary = 0;
			
			for(int g = 0; g < digitsAround.length; g++){
				temporary += digitsAround[g] * weights[g];
			}
			temp[f] = temporary / denominator;
			
		}
		
		return temp;
	}
	
}
