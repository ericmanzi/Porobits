package com.example.porobits;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;


public class Porobits extends Activity implements View.OnTouchListener{

	HashMap<String, Integer> squareState = new HashMap<String, Integer>(); //each square has state 0 or 1

	private ArduinoReceiver arduinoReceiver = new ArduinoReceiver();
	private static final String DEVICE_ADDRESS =  "00:06:66:04:29:33"; //"00:06:66:03:73:7B";

	private final int INFLATE = 1;
	private final int DEFLATE = 2;
	private final int VIBRATE = 3;

	//recording/playing sound
	private MediaRecorder mRecorder;
	private MediaPlayer mPlayer;
	private boolean playing=false;
	String fileName = null; 

	private int mode; //to decide what happens when the squares are clicked
	private final int NEUTRAL=0;
	private final int RECORDING=1;
	private final int LISTENING=2;

	private int numSaved=0;

	ImageView maskImage;
	ImageView defaultImage;
	ImageView listen;
	String currentSquare="none";
	FrameLayout myframe;

	String currentRecording="";
	String currentPlaying="";
	Handler handler = new Handler();
	Activity act=this;
	
	 ImageView imgStart;
	 ImageView imgStop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_porobits);

		for (int i=1; i<=12; i++) {
			squareState.put("square"+i, 0);
		}
		listen = new ImageView(this);

		fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		myframe = (FrameLayout) findViewById(R.id.my_frame);

		maskImage = (ImageView) findViewById(R.id.mask_img);
		defaultImage = (ImageView) findViewById(R.id.default_image_id);
		//		maskImage.setScaleType(ImageView.ScaleType.CENTER);
		//		defaultImage.setScaleType(ImageView.ScaleType.CENTER);
		if (defaultImage!=null) {
			defaultImage.setOnTouchListener (this);

		}

		imgStart = new ImageView(act);
		imgStart.setImageResource(R.drawable.startrecording);
		imgStart.setVisibility(View.INVISIBLE);
		myframe.addView(imgStart);
		imgStart.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				final int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN :
					//do nothing 
					break;
				case MotionEvent.ACTION_UP :
					record(currentSquare);
					imgStart.setVisibility(View.INVISIBLE);
					imgStop.setVisibility(View.VISIBLE);
					
				}
				return true;
			}
		});
		imgStop = new ImageView(act);
		imgStop.setImageResource(R.drawable.stoprecording);
		imgStop.setVisibility(View.INVISIBLE);
		myframe.addView(imgStop);
		imgStop.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				final int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN :
					//do nothing 
					break;
				case MotionEvent.ACTION_UP :
					doneRecording();
					imgStart.setVisibility(View.INVISIBLE);
					imgStop.setVisibility(View.INVISIBLE);
					
				}
				return true;
			}
		});
		




	}
	@Override
	protected void onStart() {
		super.onStart();
		// in order to receive broadcasted intents we need to register our receiver
		registerReceiver(arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));

		// this is how you tell Amarino to connect to a specific BT device from within your own code
		Amarino.connect(this, DEVICE_ADDRESS);
	}


	@Override
	protected void onStop() {
		super.onStop();

		// if you connect in onStart() you must not forget to disconnect when your app is closed
		Amarino.disconnect(this, DEVICE_ADDRESS);

		// do never forget to unregister a registered receiver
		unregisterReceiver(arduinoReceiver);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.porobits, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}



	public boolean onTouch (View v, MotionEvent ev) {
		//		Log.i("Color", "touched");
		final int action = ev.getAction();
		final int evX = (int) ev.getX();
		final int evY = (int) ev.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN :
			//do nothing 
			break;
		case MotionEvent.ACTION_UP :
			int touchColor = getHotspotColor (R.id.mask_img, evX, evY);
			//			Log.i("Color", String.valueOf(R.id.mask_img));
			//			Log.i("Color","here2");
			ImageView iv = new ImageView(this);

			int tolerance = 25;
			if (closeMatch (Color.parseColor("#846476"), touchColor, tolerance)) {
				currentSquare = "square1";
				iv.setImageResource(R.drawable.s1);
				Log.i("Color", "touched "+currentSquare);
			}
			else if (closeMatch (Color.parseColor("#C66B2E"), touchColor, tolerance)) {
				currentSquare = "square2";
				iv.setImageResource(R.drawable.s2);
				Log.i("Color", "touched "+currentSquare);
			}
			else if (closeMatch (Color.parseColor("#FFE000"), touchColor, tolerance)) {
				currentSquare = "square3";
				iv.setImageResource(R.drawable.s3);
				Log.i("Color", "touched "+currentSquare);
			}
			else if (closeMatch (Color.parseColor("#17AA83"), touchColor, tolerance)) {
				currentSquare = "square4";
				iv.setImageResource(R.drawable.s4);
				Log.i("Color", "touched "+currentSquare);
			}
			else if (closeMatch (Color.parseColor("#FFB600"), touchColor, tolerance)) {
				currentSquare = "square5";
				iv.setImageResource(R.drawable.s5);
				Log.i("Color", "touched "+currentSquare);
			}
			else if (closeMatch (Color.parseColor("#FF00CF"), touchColor, tolerance)) {
				currentSquare = "square6";
				iv.setImageResource(R.drawable.s6);
				Log.i("Color", "touched "+currentSquare);
			}
			else if (closeMatch (Color.parseColor("#EDFF00"), touchColor, tolerance)) {
				currentSquare = "square7";
				iv.setImageResource(R.drawable.s7);
				Log.i("Color", "touched "+currentSquare);
			}
			else if (closeMatch (Color.parseColor("#0FEA4C"), touchColor, tolerance)) {
				currentSquare = "square8";
				iv.setImageResource(R.drawable.s8);
				Log.i("Color", "touched "+currentSquare);
			}
			else if (closeMatch (Color.parseColor("#0068FF"), touchColor, tolerance)) {
				currentSquare = "square9";
				iv.setImageResource(R.drawable.s9);
				Log.i("Color", "touched "+currentSquare);
			}
			else if (closeMatch (Color.parseColor("#00CFFF"), touchColor, tolerance)) {
				currentSquare = "square10";
				iv.setImageResource(R.drawable.s10);
				Log.i("Color", "touched "+currentSquare);
			}
			else if (closeMatch (Color.parseColor("#FF0C00"), touchColor, tolerance)) {
				currentSquare = "square11";
				iv.setImageResource(R.drawable.s11);
				Log.i("Color", "touched "+currentSquare);
			}
			else if (closeMatch (Color.parseColor("#B70BFF"), touchColor, tolerance)) {
				currentSquare = "square12";
				iv.setImageResource(R.drawable.s12);
				Log.i("Color", "touched "+currentSquare);
			}
			else {
				return true;
			}
			
			iv.setImageAlpha(200);

			if (squareState.get(currentSquare)==0) { //nothing store here
				//record
				startRecording(); 
				squareState.put(currentSquare, 1);
				
			} else {
				//listen
				startListening(currentSquare);
				squareState.put(currentSquare, 0);
			}

			myframe.addView(iv,0);

			break;
		} // end switch

		//do something
		return true;
	}


	public int getHotspotColor (int hotspotId, int x, int y) {

		ImageView img = (ImageView) findViewById (hotspotId);
		//		Log.i("Color", String.valueOf(hotspotId));
		//		Log.i("Color","here3");

		img.setDrawingCacheEnabled(true); 
		Bitmap hotspots = Bitmap.createBitmap(img.getDrawingCache()); 
		img.setDrawingCacheEnabled(false);
		return hotspots.getPixel(x, y);
	}

	public boolean closeMatch (int color1, int color2, int tolerance) {
		if ((int) Math.abs (Color.red (color1) - Color.red (color2)) > tolerance ) 
			return false;
		if ((int) Math.abs (Color.green (color1) - Color.green (color2)) > tolerance ) 
			return false;
		if ((int) Math.abs (Color.blue (color1) - Color.blue (color2)) > tolerance ) 
			return false;
		return true;
	} // end match


	/**
	 * ArduinoReceiver is responsible for catching broadcasted Amarino
	 * events.
	 * 
	 * It extracts data from the intent and updates the graph accordingly.
	 */
	public class ArduinoReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String data = null;

			// the device address from which the data was sent, we don't need it here but to demonstrate how you retrieve it
			final String address = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);

			// the type of data which is added to the intent
			final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);

			// we only expect String data though, but it is better to check if really string was sent
			// later Amarino will support differnt data types, so far data comes always as string and
			// you have to parse the data to the type you have sent from Arduino, like it is shown below
			if (dataType == AmarinoIntent.STRING_EXTRA){
				data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);

				if (data != null){
					// Not receiving anything
				}
			}
		}
	}


	public void startRecording() {
		//pull up recording screen
		imgStart.setVisibility(View.VISIBLE);
		//send vibrate
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, convertNameToFlag(currentSquare), VIBRATE);
		Log.i("AMARINO", "sent VIBRATE to "+currentSquare+" at "+convertNameToFlag(currentSquare));

	}



	public void startListening(String square) {
		listen.setVisibility(View.VISIBLE);
		defaultImage.setVisibility(View.INVISIBLE);
		if (square.equals("square1")) listen.setImageResource(R.drawable.ps1);
		else if (square.equals("square2")) listen.setImageResource(R.drawable.ps2);
		else if (square.equals("square3")) listen.setImageResource(R.drawable.ps3);
		else if (square.equals("square4")) listen.setImageResource(R.drawable.ps4);
		else if (square.equals("square5")) listen.setImageResource(R.drawable.ps5);
		else if (square.equals("square6")) listen.setImageResource(R.drawable.ps6);
		else if (square.equals("square7")) listen.setImageResource(R.drawable.ps7);
		else if (square.equals("square8")) listen.setImageResource(R.drawable.ps8);
		else if (square.equals("square9")) listen.setImageResource(R.drawable.ps9);
		else if (square.equals("square10")) listen.setImageResource(R.drawable.ps10);
		else if (square.equals("square11")) listen.setImageResource(R.drawable.ps11);
		else if (square.equals("square12")) listen.setImageResource(R.drawable.ps12);
		myframe.addView(listen);
		
		play(square);
		
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, convertNameToFlag(currentSquare), VIBRATE);
		Log.i("AMARINO", "sent VIBRATE to "+currentSquare+" at "+convertNameToFlag(currentSquare));

		
	}

	//recording and playing sound
	public void record(String square) {
		mRecorder=new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(fileName+"/"+square+".3gp");
		Log.i("RECORD", fileName+"/"+square+".3gp");
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		try {
			mRecorder.prepare();
			mRecorder.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("RECORDING", "prepare() failed");
		}
	} 


	public void play(String square) {
		if (!playing) {
			final String squareTemp = square;
			try {
				mPlayer = new MediaPlayer();
				mPlayer.setDataSource(fileName+"/"+square+".3gp");
				mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mPlayer) {
						donePlaying();
						File file = new File(fileName+"/"+squareTemp+".3gp");
						boolean deleted = file.delete();
						
						//						bar.setVisibility(View.INVISIBLE);
					}
				});
				mPlayer.prepare();
				long totalDuration = mPlayer.getDuration();
				mPlayer.start();

				//				bar = (ProgressBar) findViewById(R.id.progress);
				//			    bar.setProgress(0);
				//			    bar.setVisibility(View.VISIBLE);
				//			    bar.setMax(100);
				//			    final long mDuration = mPlayer.getDuration();
				//			    Log.i("PLAYER", "duration: "+mDuration);
				/* CountDownTimer starts with length of audio file and every onTick is 1 second */
				//			    CountDownTimer cdt = new CountDownTimer(mDuration-1000, 100) { 
				//
				//			        public void onTick(long millisUntilFinished) {
				//
				//			        	long dTotal = mDuration-millisUntilFinished;
				//			            int progVal = (int) ((dTotal * 100l)/mDuration);
				//			            if (millisUntilFinished<201) {
				//			            	bar.setProgress(100);
				//			            } else {
				//			            	bar.setProgress(progVal);
				//			            }
				//			            
				//			            Log.i("PLAYER", "dTotal: "+dTotal);
				//			            Log.i("PLAYER", "progVal: "+progVal);
				//			        }
				//
				//			        public void onFinish() {
				//			             // DO something when 2 minutes is up
				//			        }
				//			    }.start();
				//				playing = true;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private char convertNameToFlag(String name) {
		if (name.equals("square1")) return 'A';
		else if (name.equals("square2")) return 'B';
		else if (name.equals("square3")) return 'C';
		else if (name.equals("square4")) return 'D';
		else if (name.equals("square5")) return 'E';
		else if (name.equals("square6")) return 'F';
		else if (name.equals("square7")) return 'G';
		else if (name.equals("square8")) return 'H';
		else if (name.equals("square9")) return 'I';
		else if (name.equals("square10")) return 'J';
		else if (name.equals("square11")) return 'K';
		else if (name.equals("square12")) return 'L';
		else return 'X';
	}

	public void doneRecording() {
		//SEND INFLATE CODE
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, convertNameToFlag(currentPlaying), INFLATE);
		Log.i("AMARINO", "sent INFLATE to "+currentSquare+" at "+convertNameToFlag(currentSquare));
		
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}


	public void donePlaying() {
		//SEND DEFLATE CODE
		listen.setVisibility(View.INVISIBLE);
		defaultImage.setVisibility(View.VISIBLE);
		ImageView iv = new ImageView(act);
		if (currentSquare.equals("square1")) iv.setImageResource(R.drawable.psn1);
		else if (currentSquare.equals("square2")) iv.setImageResource(R.drawable.psn2);
		else if (currentSquare.equals("square3")) iv.setImageResource(R.drawable.psn3);
		else if (currentSquare.equals("square4")) iv.setImageResource(R.drawable.psn4);
		else if (currentSquare.equals("square5")) iv.setImageResource(R.drawable.psn5);
		else if (currentSquare.equals("square6")) iv.setImageResource(R.drawable.psn6);
		else if (currentSquare.equals("square7")) iv.setImageResource(R.drawable.psn7);
		else if (currentSquare.equals("square8")) iv.setImageResource(R.drawable.psn8);
		else if (currentSquare.equals("square9")) iv.setImageResource(R.drawable.psn9);
		else if (currentSquare.equals("square10")) iv.setImageResource(R.drawable.psn10);
		else if (currentSquare.equals("square11")) iv.setImageResource(R.drawable.psn11);
		else if (currentSquare.equals("square12")) iv.setImageResource(R.drawable.psn11);
		myframe.addView(iv);
		
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, convertNameToFlag(currentRecording), DEFLATE);
		Log.i("AMARINO", "sent DEFLATE to "+currentSquare+" at "+convertNameToFlag(currentSquare));

		if (mPlayer!=null) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer=null;
			playing=false; 
		}
	}




}
