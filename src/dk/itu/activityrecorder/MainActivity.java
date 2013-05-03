package dk.itu.activityrecorder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;



/*
 * Notes..
 * 23.04.2013  Need to ask teacher about my enableDisableButtons() method. Can it be done iteratively
 * instead of the way I do it now, with one method for each situation.
 * 23.04.2013 Ask if there is a better way to update the GUI thread than the one I use when the timer
 * runs out in startbuttonclick.
 */

/*
 * BUGS:
 * 23.04.2012 When pressing the start button and then the stop button to cancel the recording sometimes 
 * it keeps running and sometimes it doesn't.  Chek it out...
 */
public class MainActivity extends Activity {
	
	
	private String lifeCycleTag = "lifecycle";
	private String buttonTag = "buttons";
	private String accelerometerTag = "acc";
	private String printDataTag = "dataprint";
	private String sceneTag = "scene";
	private String uploadDataTag = "upload";
	
	private SensorManager mSensorManager;
	private Sensor accSensor;
	private int accRefreshRate = 1; // 3 for 5 readings per second. 2 for 14 readings per second. 1 for 50 readings per second.
	private SensorMonitor sensorMonitor;
	
	// Vibrator object.
	Vibrator vib;

	// A timer to give a little delay before recording data.
	Timer timer = new Timer();
	
	// A boolean to know when to stop recording data.
	boolean running = true;
	
	// A boolean to indicate if the stop button was used to stop the recording or not.
	boolean stopButtonUsed = false;
	
	// The list that will hold the data.
	List<String> data; 
	
	// Number of samples per second.
	int numberOfSamplesPerSec = 50;  // 50 means 20 samples per second.
	
	// Same as numberofsamplepersec but this one is in use. This one can be used without calculation
	// to indicate how many samples per second you want.
	int FRAMES_PER_SECOND = 20;
	
	// The name associated with the data being written.
	String dataUser = "simmi";  // Default =  simmi
	
	// The action associated with the data being written. 
	String action = "walking";  // Default =  walking
	 
	// Number of seconds to collect data for. It is updated with the click of the radio buttons.
	int numberOfSecondsToCollectData = 10;  // Default =  10
	
	// The format of the timestamp.
	DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	DateFormat dateFormatFileName = new SimpleDateFormat("HHmmss");
	//DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // toooooo detailed
	
	// A media player to play beep short sound. Used to indicate start of recording.
	MediaPlayer beepShort;
	
	// A media player to play beep short sound. Used to indicate start of recording.
	MediaPlayer beepAlarm;
	
	// DataUploader. Used to upload data to google cloud.
	DataUploader uploader;
	
	// All the buttons in the application so I can manipulate them.
	/*
	Button startRec;
	Button stopRec;
	Button saveData;
	Button eraseData;
	*/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(lifeCycleTag,"OnCreate started");
		
		uploader = new DataUploader("http://ma3tester.appspot.com/ma3tester");
		
		beepShort = MediaPlayer.create(this, R.raw.beepshort);
		
		beepAlarm = MediaPlayer.create(this, R.raw.beepalarm);
		
		setContentView(R.layout.activity_main);
		
		beepShort.start();
		savingDataScreen("Initializing..."); // SavingDataScreen simply turns every view off. I use it here while I am initializing. 
		
		/* This is replaced with different methods for different states. Maybe this is better though. 
		 * Should ask teacher how to implement a loop that I can supply views that need turning off/on.
		startRec = (Button) findViewById(R.id.buttonStartRec);
		stopRec = (Button) findViewById(R.id.buttonStopRec);
		saveData = (Button) findViewById(R.id.buttonSaveData);
		eraseData = (Button) findViewById(R.id.buttonDeleteData);
		*/
		sensorMonitor = new SensorMonitor();
		
		// Get instance of Vibrator from current Context
		vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		// Get the sensor service.
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// Check if this phone has an accelerometer and create the sensor if found.
		if ((accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)) != null) {  
			// Success! There's an accelerometer.
			Toast.makeText(this,"Accelerometer found.", Toast.LENGTH_SHORT).show();
			
			// Register accelerometer so I can listen to it.
			if(mSensorManager.registerListener(sensorMonitor, accSensor,accRefreshRate )) {
				Toast.makeText(this,"Accelerometer found and registered.", Toast.LENGTH_SHORT).show();
				// Enable buttons.
				//enableDisableAllButtons(true);  // Deprecated. Use the XScreen(String Message) instead.
				TextView statusTextView = (TextView) findViewById(R.id.textViewStatus);
				statusTextView.setText("Accelerometer found and registered.");
				mSensorManager.unregisterListener(sensorMonitor);
			}
			// Registering the accelerometer unsuccessful for some reason.
			else {
				Toast.makeText(this,"Accelerometer NOT found.", Toast.LENGTH_LONG).show();
				// Disable buttons.
				//enableDisableAllButtons(false);  // Deprecated. Use the XScreen(String Message) instead.
				TextView statusTextView = (TextView) findViewById(R.id.textViewStatus);
				statusTextView.setText("Accelerometer found and registered.");
			}
			
		}
		else {  
			// Failure. No accelerometer.
			Toast.makeText(this,"Accelerometer NOT found. Disabling buttons.", Toast.LENGTH_SHORT).show();
			
			// Disable buttons.
			//enableDisableAllButtons(false);  // Deprecated. Use the XScreen(String Message) instead.
			savingDataScreen("Accelerometer NOT found. Disabling buttons."); // SavingDataScreen simply turns every view off. I use it here while I am initializing. 
			
		}
		initialScreen("Ready. If you did not hear a beep then turn media volume up.");
		//initialScreen("Everything is fine and ready to start."); // Turn on the initial screen with the program ready to act.
		Log.v(lifeCycleTag,"OnCreate finished.");
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		Log.v(lifeCycleTag,"OnStart started");
		
		Log.v(lifeCycleTag,"OnStart finished");
		
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		Log.v(lifeCycleTag,"OnResume started");
		
		Log.v(lifeCycleTag,"OnResume finished");
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		Log.v(lifeCycleTag,"OnPause started");
		
		Log.v(lifeCycleTag,"OnPause finished");
		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Log.v(lifeCycleTag,"OnStop started");
		
		Log.v(lifeCycleTag,"OnStop finished");
		super.onStop();
	} 
	
	@Override
	public void onRestart() {
		super.onRestart();
		Log.v(lifeCycleTag,"OnRestart started");
		
		Log.v(lifeCycleTag,"OnRestart finished");
		super.onStop();
	} 
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.v(lifeCycleTag,"OnDestroy started");
		mSensorManager.unregisterListener(sensorMonitor);
		timer.cancel();
		Log.v(lifeCycleTag,"OnDestroy finished");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	// Initial screen. String Message will be displayed on the status label. 
	// String Message can take approximately this much: ABCDEFGHIJKLMNOPQRSTUVXYZABCDEFGHIJKLMN
	// before needing a new line. Maximum of two lines.
	public void initialScreen(String message) {
		Log.v(sceneTag,"initial Scene");
		
		TextView mess = (TextView) findViewById(R.id.textViewStatus);
		mess.setText(message);
		
		Button startRec = (Button) findViewById(R.id.buttonStartRec);
		startRec.setEnabled(true);
		Button stopRec = (Button) findViewById(R.id.buttonStopRec);
		stopRec.setEnabled(false);
		
		Button saveData = (Button) findViewById(R.id.buttonSaveData);
		saveData.setEnabled(false);
		Button uploadData = (Button) findViewById(R.id.buttonUploadData);
		uploadData.setEnabled(true);
		Button eraseData = (Button) findViewById(R.id.buttonDeleteData);
		eraseData.setEnabled(false);
		
		Spinner spinDelay = (Spinner) findViewById(R.id.spinnerStartDelay);
		spinDelay.setEnabled(true);
		Spinner spinCutOff = (Spinner) findViewById(R.id.spinnerStopCut);
		spinCutOff.setEnabled(true);
		
		RadioGroup action = (RadioGroup) findViewById(R.id.radioGroupAction);
		int numberOfChilds = action.getChildCount();
		for(int i = 0;i < numberOfChilds;i++){
			action.getChildAt(i).setEnabled(true);
		}
		RadioGroup user = (RadioGroup) findViewById(R.id.radioGroupUser);
		numberOfChilds = user.getChildCount();
		for(int i = 0;i < numberOfChilds;i++){
			user.getChildAt(i).setEnabled(true);
		}
		RadioGroup time = (RadioGroup) findViewById(R.id.radioGroupTime);
		numberOfChilds = time.getChildCount();
		for(int i = 0;i < numberOfChilds;i++){
			time.getChildAt(i).setEnabled(true);
		}
		/*
		RadioGroup action = (RadioGroup) findViewById(R.id.radioGroupAction);
		action.setEnabled(true);
		RadioGroup user = (RadioGroup) findViewById(R.id.radioGroupUser);
		user.setEnabled(true);
		RadioGroup time = (RadioGroup) findViewById(R.id.radioGroupTime);
		time.setEnabled(true);
		*/
	}
	
	// Recording screen. Disenables every view except the stop recording button.
	// String Message will be displayed on the status label. 
	// String Message can take approximately this much: ABCDEFGHIJKLMNOPQRSTUVXYZABCDEFGHIJKLMN
	// before needing a new line. Maximum of two lines.
	public void recordingScreen(String message) {
		Log.v(sceneTag,"recording Scene");
		
		TextView mess = (TextView) findViewById(R.id.textViewStatus);
		mess.setText(message);
		
		Button startRec = (Button) findViewById(R.id.buttonStartRec);
		startRec.setEnabled(false);
		Button stopRec = (Button) findViewById(R.id.buttonStopRec);
		stopRec.setEnabled(true);
		
		Button saveData = (Button) findViewById(R.id.buttonSaveData);
		saveData.setEnabled(false);
		Button uploadData = (Button) findViewById(R.id.buttonUploadData);
		uploadData.setEnabled(false);
		Button eraseData = (Button) findViewById(R.id.buttonDeleteData);
		eraseData.setEnabled(false);
		
		Spinner spinDelay = (Spinner) findViewById(R.id.spinnerStartDelay);
		spinDelay.setEnabled(false);
		Spinner spinCutOff = (Spinner) findViewById(R.id.spinnerStopCut);
		spinCutOff.setEnabled(false);
		
		RadioGroup action = (RadioGroup) findViewById(R.id.radioGroupAction);
		int numberOfChilds = action.getChildCount();
		for(int i = 0;i < numberOfChilds;i++){
			action.getChildAt(i).setEnabled(false);
		}
		RadioGroup user = (RadioGroup) findViewById(R.id.radioGroupUser);
		numberOfChilds = user.getChildCount();
		for(int i = 0;i < numberOfChilds;i++){
			user.getChildAt(i).setEnabled(false);
		}
		RadioGroup time = (RadioGroup) findViewById(R.id.radioGroupTime);
		numberOfChilds = time.getChildCount();
		for(int i = 0;i < numberOfChilds;i++){
			time.getChildAt(i).setEnabled(false);
		}
		
		/*
		RadioGroup action = (RadioGroup) findViewById(R.id.radioGroupAction);
		action.setEnabled(false);
		RadioGroup user = (RadioGroup) findViewById(R.id.radioGroupUser);
		user.setEnabled(false);
		RadioGroup time = (RadioGroup) findViewById(R.id.radioGroupTime);
		time.setEnabled(false);
		*/
	}
	
	// Finished recording screen. Turns everything off except the save/erase data buttons and the
	// radio button group views because the user might want to change something there before saving.
	//String Message will be displayed on the status label. 
	// String Message can take approximately this much: ABCDEFGHIJKLMNOPQRSTUVXYZABCDEFGHIJKLMN
	// before needing a new line. Maximum of two lines.
	public void finishedRecordingScreen(String message) {
		Log.v(sceneTag,"finished Recording Scene");
		
		TextView mess = (TextView) findViewById(R.id.textViewStatus);
		mess.setText(message);
		
		Button startRec = (Button) findViewById(R.id.buttonStartRec);
		startRec.setEnabled(false);
		Button stopRec = (Button) findViewById(R.id.buttonStopRec);
		stopRec.setEnabled(false);
		
		Button saveData = (Button) findViewById(R.id.buttonSaveData);
		saveData.setEnabled(true);
		Button uploadData = (Button) findViewById(R.id.buttonUploadData);
		uploadData.setEnabled(true);
		Button eraseData = (Button) findViewById(R.id.buttonDeleteData);
		eraseData.setEnabled(true);
		
		Spinner spinDelay = (Spinner) findViewById(R.id.spinnerStartDelay);
		spinDelay.setEnabled(false);
		Spinner spinCutOff = (Spinner) findViewById(R.id.spinnerStopCut);
		spinCutOff.setEnabled(false);
		
		RadioGroup action = (RadioGroup) findViewById(R.id.radioGroupAction);
		int numberOfChilds = action.getChildCount();
		for(int i = 0;i < numberOfChilds;i++){
			action.getChildAt(i).setEnabled(true);
		}
		RadioGroup user = (RadioGroup) findViewById(R.id.radioGroupUser);
		numberOfChilds = user.getChildCount();
		for(int i = 0;i < numberOfChilds;i++){
			user.getChildAt(i).setEnabled(true);
		}
		RadioGroup time = (RadioGroup) findViewById(R.id.radioGroupTime);
		numberOfChilds = time.getChildCount();
		for(int i = 0;i < numberOfChilds;i++){
			time.getChildAt(i).setEnabled(false);
		}
		/*
		RadioGroup action = (RadioGroup) findViewById(R.id.radioGroupAction);
		action.setEnabled(true);
		RadioGroup user = (RadioGroup) findViewById(R.id.radioGroupUser);
		user.setEnabled(true);
		RadioGroup time = (RadioGroup) findViewById(R.id.radioGroupTime);
		time.setEnabled(false);
		*/
	}
	
	// Saving data screen. Everything off. Used while initializing program or saving data.
	// String Message will be displayed on the status label. 
	// String Message can take approximately this much: ABCDEFGHIJKLMNOPQRSTUVXYZABCDEFGHIJKLMN
	// before needing a new line. Maximum of two lines.
	public void savingDataScreen(String message) {
		
		Log.v(sceneTag,"saving Data Scene");
		
		TextView mess = (TextView) findViewById(R.id.textViewStatus);
		mess.setText(message);
		
		Button startRec = (Button) findViewById(R.id.buttonStartRec);
		startRec.setEnabled(false);
		Button stopRec = (Button) findViewById(R.id.buttonStopRec);
		stopRec.setEnabled(false);
		
		Button saveData = (Button) findViewById(R.id.buttonSaveData);
		saveData.setEnabled(false);
		Button uploadData = (Button) findViewById(R.id.buttonUploadData);
		uploadData.setEnabled(false);
		Button eraseData = (Button) findViewById(R.id.buttonDeleteData);
		eraseData.setEnabled(false);
		
		Spinner spinDelay = (Spinner) findViewById(R.id.spinnerStartDelay);
		spinDelay.setEnabled(false);
		Spinner spinCutOff = (Spinner) findViewById(R.id.spinnerStopCut);
		spinCutOff.setEnabled(false);
		
		RadioGroup action = (RadioGroup) findViewById(R.id.radioGroupAction);
		int numberOfChilds = action.getChildCount();
		for(int i = 0;i < numberOfChilds;i++){
			action.getChildAt(i).setEnabled(false);
		}
		RadioGroup user = (RadioGroup) findViewById(R.id.radioGroupUser);
		numberOfChilds = user.getChildCount();
		for(int i = 0;i < numberOfChilds;i++){
			user.getChildAt(i).setEnabled(false);
		}
		RadioGroup time = (RadioGroup) findViewById(R.id.radioGroupTime);
		numberOfChilds = time.getChildCount();
		for(int i = 0;i < numberOfChilds;i++){
			time.getChildAt(i).setEnabled(false);
		}
		
		/*
		RadioGroup action = (RadioGroup) findViewById(R.id.radioGroupAction);
		action.setEnabled(false);
		RadioGroup user = (RadioGroup) findViewById(R.id.radioGroupUser);
		user.setEnabled(false);
		RadioGroup time = (RadioGroup) findViewById(R.id.radioGroupTime);
		time.setEnabled(false);
		*/
	}
	
	
	// Need to see if this can be done. Ask teacher.
	public void enableDisableButtons(boolean enableOrDisable, Button[] buttons){
		for(Button s : buttons) {
			s.setEnabled(enableOrDisable);
		}
	}
	
	public void startRecButtonClicked(View view) {
		Log.v(buttonTag,"Start record clicked");
		// Register the listener in the beginning to make the sensor "settle". A theory of mine.
		// Was probably just the fact that I used the vibrator to indicate the starting of a record and
		// that added noise.
		mSensorManager.registerListener(sensorMonitor, accSensor,accRefreshRate );
		
		recordingScreen("About to start recording. put phone in pocket...");  // Go to recording screen.
		
		// spinnerStartTime holds the delay in seconds before starting to record. 
		// This delay is used on the timer as a delay.
		Spinner delaySpinner = (Spinner) findViewById(R.id.spinnerStartDelay);
		int timeToStart = Integer.parseInt(delaySpinner.getSelectedItem().toString());
		
		// Instantiate the list that will hold the data.
		data = new ArrayList<String>();
		
		// Add the header to the CSV file. First column is time stamp
		// but sometimes I leave it blank so I don't need to delete 
		// it when plotting it in excel.
		data.add("timestamp,X,Y,Z");
		
		running = true;
		stopButtonUsed = false;
		timer.schedule(new TimerTask() {
			  @Override
			  public void run() {
				  
				  // Vibrate to let the user know that the recording is starting.
				  // THIS IS NOT A GOOD IDEA. The vibrator will be recorded in the data.
				  // Maybe a sound is better.
				  //vib.vibrate(150);
				  beepShort.start();
				  
				  // These variables are used by the while(running) loop. It is used to
				  // run the loop at a constant speed.
				  int SKIP_TICKS = 1000 / FRAMES_PER_SECOND;
				  int sleep_time = 0;
				  long next_game_tick = GetTickCount();
				  
				  
				
				  int numberOfEntries = 0;
				  
				  while(running)
				  {
					  Date date = new Date();
					  Log.v(accelerometerTag,"Date: "+dateFormat.format(date));
					  data.add(dateFormat.format(date)+","+sensorMonitor.getX()+","+sensorMonitor.getY()+","+sensorMonitor.getZ());
					  numberOfEntries++;
					  next_game_tick += SKIP_TICKS;
					  sleep_time = (int) (next_game_tick - GetTickCount());
					  if( sleep_time >= 0 ) {
				            try {
				            	
								Thread.sleep( sleep_time );
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				        }
				        else {
				            
				        }
					  
					  if(numberOfEntries >=( numberOfSecondsToCollectData * FRAMES_PER_SECOND) ) {
					  //if(date.getTime() > timeStarted.getTime()+(numberOfSecondsToCollectData*1000) && numberOfEntries >=( numberOfSecondsToCollectData * FRAMES_PER_SECOND)  ) {
						  running = false;
						  // Even though data has been recorded I still do not use the vibration to
						  // indicate that the data has been recorded. I do not have the time to test
						  // if it affects the data or not. Just play a sound instead.
						  //vib.vibrate(150);
						  beepAlarm.start();
						  
					  }
					  
					 
				  }
				  if(!stopButtonUsed)
				  {
					  
				  // Ask teacher if there is any better way to do this.
				  Runnable changeToFinishedRecording = new Runnable() {
				        @Override
				        public void run() {
				        	finishedRecordingScreen("Finished recording. Data in buffer. Save or erase?");
				        }
				    };
				    runOnUiThread(changeToFinishedRecording);
				  }
				  
				  
				  mSensorManager.unregisterListener(sensorMonitor);
				  
				  
				  
			  }
			}, timeToStart*1000);
		
		
				  
		Log.v(buttonTag,"Start record exiting. Timer might still be running though...");	  
		
	}
	
	public void stopRecordButtonClicked(View view) {
		stopButtonUsed = true;
		running = false;
		
		Log.v(buttonTag,"Stop record clicked");
		
		
		//mSensorManager.unregisterListener(sensorMonitor);
		
		initialScreen("Recording was stopped by user. Ready for another go..");
		
		vib.vibrate(50);
	}

	public void saveDataButtonClicked(View view) throws IOException {
		
		Log.v(buttonTag,"Save data clicked");
		
		savingDataScreen("Data is being SAVED TO DISK and you probably won't even see this message. Blabla.");
		
		Date now = new Date();

		String nameOfFile = numberOfSecondsToCollectData+dataUser+action+dateFormatFileName.format(now);
		//String nameOfFile = "sid";
		Log.v(buttonTag,"path: "+nameOfFile);
		String fpath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/ActivityRecorder/"+nameOfFile+".txt";
		
		File logFile = new File(fpath);
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
		BufferedWriter buf = null;
		
		try{
			buf = new BufferedWriter(new FileWriter(logFile,true));
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		if(data != null)
		{
			for(int i = 0; i < data.size();i++)
			{
				Log.v(printDataTag,data.get(i).toString());
				buf.append(data.get(i).toString());
				buf.append("\n");
			}	
			Log.v(printDataTag,"Size of data: "+data.size());
		}
		
		buf.close();
		initialScreen("Data saved to: "+fpath+" Ready for another go..");
		data.clear();
	}
	
	public void uploadDataButtonClicked(View view){
		savingDataScreen("Data is being UPLOADED and you probably won't even see this message. Blabla.");
		Date now = new Date();
		
		List<String> tempData = new ArrayList<String>();
		//tempData.add("index,timestamp,x,y,z");
		int time = 0;
		int xs = 0;
		int ys = 0;
		int zs = 0;
		int index = 0;
		for(int i = 0; i < 2; i++)
		{
			tempData.add("13:40:"+time+","+xs+","+ys+","+zs);
			time++;
			xs += ys + zs +1;
			ys += zs + 2;
			zs += ys - 2;
			index++;
		}
		
		String nameOfFile = numberOfSecondsToCollectData+dataUser+action+dateFormatFileName.format(now);
		//String returnString = "String was never changed. Upload data button clicked.";
		try {
			Log.v(uploadDataTag,"name of file: "+nameOfFile);
			uploader.uploadList(nameOfFile, tempData, 6);
		
		}
		catch(Exception e) {
			Log.v(uploadDataTag,"Exception: "+e);
			initialScreen("Exception in uploading..");
		}
		Log.v(uploadDataTag,"Upload finished.");
		initialScreen("Upload finished.");
		/*
		if(returnString.equals("Exception: ClientProtocolException")){
			Log.v(uploadDataTag,"Exception: ClientProtocolException");
			initialScreen(returnString);
			
		}
		if(returnString.equals("Exception: IOException")){
			Log.v(uploadDataTag,"Exception: IOException");
			initialScreen(returnString);
		}
		else
		{
			initialScreen(returnString);
		}
		*/
	}
	
	public void eraseDataButtonClicked(View view) {
		Log.v(buttonTag,"Erase data clicked");
		
		// AlertDialog for confirmation
        AlertDialog.Builder clearConfirmDialog = new AlertDialog.Builder(this);
        clearConfirmDialog.setMessage("Sure you want to erase data?").setCancelable(false)
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Action for 'Yes' Button
                data.clear();
                initialScreen("Data erased. Ready for another go..");
            }
        })
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //  Action for 'NO' Button
                
            }
        });
        AlertDialog alert = clearConfirmDialog.create();
        alert.setTitle("Erase?");
        
        alert.show();

		
	}
	
	public void buttonTestSoundClicked(View view) {
		Log.v(buttonTag,"Test sound button clicked");
		if(!beepShort.isPlaying()) {
			beepShort.start();
		}
		if(!beepAlarm.isPlaying()) {
			beepAlarm.start();
		}
		
	}
	
	public long GetTickCount(){
		Date d = new Date();
		return d.getTime();
	}

	// Radio buttons time to collect data
	public void radioButton10Clicked(View view) {
		Log.v(buttonTag,"Radio button 10 clicked");
		numberOfSecondsToCollectData = 10;
		
	}
	public void radioButton15Clicked(View view) {
		Log.v(buttonTag,"Radio button 15 clicked");
		numberOfSecondsToCollectData = 15;
		
	}
	public void radioButton30Clicked(View view) {
		Log.v(buttonTag,"Radio button 30 clicked");
		numberOfSecondsToCollectData = 30;
		
	}
	
	// Radio buttons specific users.
	public void radioButtonSimmiClicked(View view) {
		Log.v(buttonTag,"Radio button Simmi clicked");
		dataUser = "simmi";
	}
	
	public void radioButtonDanielClicked(View view) {
		Log.v(buttonTag,"Radio button Daniel clicked");
		dataUser = "daniel";
	}
	
	public void radioButtonAlien1Clicked(View view) {
		Log.v(buttonTag,"Radio button Alien1 clicked");
		dataUser = "alien1";
	}
	
	public void radioButtonAlien2Clicked(View view) {
		Log.v(buttonTag,"Radio button Alien2 clicked");
		dataUser = "alien2";
	}
	
	// Radio buttons specific action.
	public void radioButtonWalkingClicked(View view) {
		Log.v(buttonTag,"Radio button Alien2 clicked");
		action = "walking";
	}
	
	public void radioButtonRunningClicked(View view) {
		Log.v(buttonTag,"Radio button running clicked");
		action = "running";
	}
	
	public void radioButtonJumpingClicked(View view) {
		Log.v(buttonTag,"Radio button jumping clicked");
		action = "jumping";
	}
	
	public void radioButtonSittingClicked(View view) {
		Log.v(buttonTag,"Radio button sitting clicked");
		action = "sitting";
	}
	public void radioButtonStairsUpClicked(View view) {
		Log.v(buttonTag,"Radio button stairsup clicked");
		action = "stairsup";
	}
	
	public void radioButtonStairsDownClicked(View view) {
		Log.v(buttonTag,"Radio button stairsdown clicked");
		action = "stairsdown";
	}
	
	// Spinners on click.
	public void startDelaySpinnerClicked(View view) {
		Log.v(buttonTag,"Start delay spinner clicked");
	}
}
