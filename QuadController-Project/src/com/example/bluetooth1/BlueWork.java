package com.example.bluetooth1;

import com.test.bluetooth.R;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;
import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("ClickableViewAccessibility")
public class BlueWork extends Activity {
	RelativeLayout layout_joystick,layout_joystick2;
	ImageView image_joystick, image_border;
	TextView textView1, textView2, textView3, textView4, textView5;
	
	 private static final String TAG = "bluetooth1";
	   
	  Button btnOn, btnOff;
	   
	  private BluetoothAdapter btAdapter = null;
	  private BluetoothSocket btSocket = null;
	  private OutputStream outStream = null;
	   
	  // SPP UUID service 
	  private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	 Main_Activity ma=new Main_Activity();
	 String addr=Main_Activity.addDev;
	  // MAC-address of Bluetooth module (you must edit this line)
	  private  String address = addr;
	   
	
	JoyS js;
	JoyS2 js2;
	int xt=0;
	int yt=0;
    @SuppressLint("ClickableViewAccessibility")
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.work);


        btnOn = (Button) findViewById(R.id.btnOn);
        btnOff = (Button) findViewById(R.id.btnOff);
         
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();
     
        btnOn.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
            sendData("1");
            Toast.makeText(getBaseContext(), "Turn on LED", Toast.LENGTH_SHORT).show();
          }
        });
     
        btnOff.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
        	  sendData("0");
        	  sendData("u");
        	  sendData("0");
              
            Toast.makeText(getBaseContext(), "Turn off LED", Toast.LENGTH_SHORT).show();
          }
        });
        
        textView1 = (TextView)findViewById(R.id.textView1);
        textView2 = (TextView)findViewById(R.id.textView2);
         textView3 = (TextView)findViewById(R.id.textView3);
        textView4 = (TextView)findViewById(R.id.textView4);
//        textView5 = (TextView)findViewById(R.id.textView5);
      
	    layout_joystick = (RelativeLayout)findViewById(R.id.layout_joystick);

        js = new JoyS(getApplicationContext()
        		, layout_joystick, R.drawable.image_button);
	    js.setStickSize(150, 150);
	    js.setLayoutSize(700, 700);
	    js.setLayoutAlpha(150);
	    js.setStickAlpha(100);
	    js.setOffset(80);
	    js.setMinimumDistance(10);
	    
	    layout_joystick2 = (RelativeLayout)findViewById(R.id.RelativeLayout01);

        js2 = new JoyS2(getApplicationContext()
        		, layout_joystick2, R.drawable.image_button);
	    js2.setStickSize(150, 150);
	    js2.setLayoutSize(700, 700);
	    js2.setLayoutAlpha(150);
	    js2.setStickAlpha(100);
	    js2.setOffset(80);
	    js2.setMinimumDistance(10);
	    
	    
	    layout_joystick2.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent arg1) {
				js2.drawStick(arg1);
				if(arg1.getAction() == MotionEvent.ACTION_DOWN
						|| arg1.getAction() == MotionEvent.ACTION_MOVE) {
					int yy=(int) ((js2.getY()*-2)+1500);
					if(yy<1000){
						yy=1000;
					}else if(yy>2000){
						yy=2000;
					}
					int xx=js2.getX();
					textView3.setText("X : " + String.valueOf(xx));
					textView4.setText("Y : " + String.valueOf(yy));
					sendData("u");
					sendData(""+yy);
					sendData("y");
					sendData(""+xx);
					
				}
				return true;
			}
			
			
		}
    );
	    
	    layout_joystick.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent arg1) {
				js.drawStick(arg1);
				if(arg1.getAction() == MotionEvent.ACTION_DOWN
						|| arg1.getAction() == MotionEvent.ACTION_MOVE) {
					int yy=js.getY()*-1;
					int xx=js.getX();
					textView2.setText("X : " + String.valueOf(xx));
					textView1.setText("Y : " + String.valueOf(yy));
					
					sendData("p");
					sendData(""+yy);
					sendData("r");
					sendData(""+xx);
					
				
				} else if(arg1.getAction() == MotionEvent.ACTION_UP) {
					
				}
				
				return true;
			}
        });
    }
    

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection",e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }
     
    @Override
    public void onResume() {
      super.onResume();
   
      Log.d(TAG, "...onResume - try connect...");
     
      // Set up a pointer to the remote node using it's address.
      BluetoothDevice device = btAdapter.getRemoteDevice(address);
     
      // Two things are needed to make a connection:
      //   A MAC address, which we got above.
      //   A Service ID or UUID.  In this case we are using the
      //     UUID for SPP.
     
  	try {
  		btSocket = createBluetoothSocket(device);
  	} catch (IOException e1) {
  		errorExit("Fatal Error", "In onResume() and socket create failed: " + e1.getMessage() + ".");
  	}
      
      /*try {
        btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
      } catch (IOException e) {
        errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
      }*/
     
      // Discovery is resource intensive.  Make sure it isn't going on
      // when you attempt to connect and pass your message.
      btAdapter.cancelDiscovery();
     
      // Establish the connection.  This will block until it connects.
      Log.d(TAG, "...Connecting...");
      try {
        btSocket.connect();
        Log.d(TAG, "...Connection ok...");
      } catch (IOException e) {
        try {
          btSocket.close();
        } catch (IOException e2) {
          errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
        }
      }
       
      // Create a data stream so we can talk to server.
      Log.d(TAG, "...Create Socket...");
   
      try {
        outStream = btSocket.getOutputStream();
      } catch (IOException e) {
        errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
      }
    }
   
    @Override
    public void onPause() {
      super.onPause();
   
      Log.d(TAG, "...In onPause()...");
   
      if (outStream != null) {
        try {
          outStream.flush();
        } catch (IOException e) {
          errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
        }
      }
   
      try     {
        btSocket.close();
      } catch (IOException e2) {
        errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
      }
    }
     
    private void checkBTState() {
      // Check for Bluetooth support and then check to make sure it is turned on
      // Emulator doesn't support Bluetooth and will return null
      if(btAdapter==null) { 
        errorExit("Fatal Error", "Bluetooth not support");
      } else {
        if (btAdapter.isEnabled()) {
          Log.d(TAG, "...Bluetooth ON...");
        } else {
          //Prompt user to turn on Bluetooth
          Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
          startActivityForResult(enableBtIntent, 1);
        }
      }
    }
   
    private void errorExit(String title, String message){
      Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
      finish();
    }
   
    private void sendData(String message) {
      byte[] msgBuffer = message.getBytes();
      try {
        outStream.write(msgBuffer);
      } catch (IOException e) {
           
      }
    }
    
    
    
}
