package dk.itu.activityrecorder;


import java.util.Date;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class SensorMonitor implements SensorEventListener {

	
	private String accelerometerTag = "acc";
	
	
	Date date = new Date();
	
	public float x = 0;
	public float y = 0;
	public float z = 0;
	
	public SensorMonitor()
	{
		
		
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
	

}
