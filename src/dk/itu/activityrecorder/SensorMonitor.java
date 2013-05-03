package dk.itu.activityrecorder;

import java.util.Calendar;
import java.util.Date;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class SensorMonitor implements SensorEventListener {

	
	private String accelerometerTag = "acc";
	private String rateTag = "rate";
	private int second = 1000;
	private long milliSinceLast = 0; 
	private int howManyTicksPerSecond = 0;
	private int numberOfSamples;
	private int numberOfSamplesPerSec = 50; // 50 means 20 samples per second
	
	Date date = new Date();
	
	public float x = 0;
	public float y = 0;
	public float z = 0;
	
	public SensorMonitor()
	{
		
		
	}
	
	public SensorMonitor(int numberOfSamplesPerSec, int numberOfSecs)
	{
		
		milliSinceLast = date.getTime();
		this.numberOfSamplesPerSec = numberOfSamplesPerSec;
		this.numberOfSamples = numberOfSamplesPerSec * numberOfSecs;
		
		
	}
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		
		Log.v(accelerometerTag,"For some reason the accuracy has changed on "+arg0.getName()+".");
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		
		x = arg0.values[0];
		y = arg0.values[1];
		z = arg0.values[2];
		
	}
	
	public float getX()
	{
		return x;
		
	}
	
	public float getY()
	{
		return y;
		
	}
	public float getZ()
	{
		return z;
		
	}
	/*
	@SuppressWarnings("deprecation")
	@Override
	public void onSensorChanged(SensorEvent arg0) {
		
		//Log.v(rateTag,"second: "+second+"  Calendar.second: "+date.getSeconds());
		date = new Date();
		long milli = date.getTime();
		
		if(milli - milliSinceLast > numberOfSamplesPerSec) {
			Log.v(rateTag,"Rate: " + howManyTicksPerSecond);
			
			milliSinceLast = date.getTime();;
			
			howManyTicksPerSecond++;
			
		}
		else {
		Log.v(accelerometerTag,"Sensor: "+arg0.sensor.getName());
		Log.v(accelerometerTag,"X: "+arg0.values[0]+ " Y: "+arg0.values[1]+ " Z: "+arg0.values[2]);
		
		howManyTicksPerSecond = 0;
		}
	}
	*/

}
