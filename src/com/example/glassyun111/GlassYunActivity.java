package com.example.glassyun111;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;


import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

public class GlassYunActivity extends Activity 
{
	
	private GestureDetector glassGestureDetector;
	
	AndroidYunConnect AandYConnect;
	
	int forwardD = 1;
	int backwardD = 2;
	int leftD = 3;
	int rightD = 4;
	int haltD = 5;
	int btn6 = 6;
	int btn7 = 7;
	int btn8 = 8;
	int btn9 = 9;
	int btn10 = 10;
	
	String serverIpAddress = "10.10.1.123";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_glass_yun);

		glassGestureDetector = createGestureDetector(this);
		
		startCom();
		startCam();
	}
	
	@Override
    protected void onResume()
    {
        super.onResume();
        Log.d("ZZZZZ","onResume() called.");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
	
	private GestureDetector createGestureDetector(Context context)
	{
		GestureDetector gestureDetector = new GestureDetector(context);
		
		gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
		
			@Override
			public boolean onGesture(Gesture gesture)
			{
				if(gesture == Gesture.TAP)
				{
					Log.d("ZZZZZ","TAP");
					AandYConnect.SendDirectionToYun(btn7);
					return true;
				} else if (gesture == Gesture.TWO_TAP) 
				{
					AandYConnect.SendDirectionToYun(btn8);
					Log.d("ZZZZZ","TWO_TAP");
                    return true;
                } else if (gesture == Gesture.SWIPE_RIGHT) 
                {
                	AandYConnect.SendDirectionToYun(btn9);
                	Log.d("ZZZZZ","SWIPE_RIGHT");
                    return true;
                } else if (gesture == Gesture.SWIPE_LEFT) 
                {
                	AandYConnect.SendDirectionToYun(btn10);
                	Log.d("ZZZZZ","SWIPE_LEFT");
                    return true;
                }
                return false;
			}
		
	});
	
		gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
            @Override
            public void onFingerCountChanged(int previousCount, int currentCount) {
              // do something on finger count changes
            }
        });
        gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
            @Override
            public boolean onScroll(float displacement, float delta, float velocity) {
            	return true;
                // do something on scrolling
            }
        });
        return gestureDetector;
    }
	
	@Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (glassGestureDetector != null) {
            return glassGestureDetector.onMotionEvent(event);
        }
        return false;
    }
	
	
	private void startCom()
	{
		AandYConnect = new AndroidYunConnect();
		AandYConnect.serverIpAddress = serverIpAddress;
		AandYConnect.execute();	
	}
	
	private void startCam()
	{
		WebView myWebView = (WebView) findViewById(R.id.webView1);
		myWebView.loadUrl("http://" + serverIpAddress + ":8080/stream_simple.html");
		
		/*
		myWebView.loadUrl("http://" + "10.0.1.12" + ":8080/stream_simple.html");
		myWebView.loadUrl("http://" + "192.168.1.107" + ":8080/stream_simple.html");
		myWebView.loadUrl("http://" + "10.16.70.118" + ":8080/stream_simple.html");
		 */
	}

	public class AndroidYunConnect extends AsyncTask<Void, byte[], Boolean>
	{
		Socket yunsocket;
		DataInputStream yunin;
		DataOutputStream yunout;
		String serverIpAddress;
		int yunData = 666;
		int yunDataCnt = 0;
		
		@Override
		protected void onPreExecute()
		{
			Log.d("ZZZZZ", "pre");
		}
		
		@Override
		protected Boolean doInBackground(Void... params) 
		{
			boolean result = false;
			
			try
			{
				Log.d("ZZZZZ", "doInBackground");
				
				InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
				yunsocket = new Socket(serverAddr, 6666);
				
				if(yunsocket.isConnected())
				{
					yunin = new DataInputStream(yunsocket.getInputStream());
					yunout = new DataOutputStream(yunsocket.getOutputStream());
					
					Log.d("ZZZZZ", "doInBackground socket connections up");
					
					
					byte[] directionBuffer = new byte[4];
					
					while(yunData != -1)
					{
						Log.d("ZZZZZ", "doInBackground loop");
						yunDataCnt = yunin.read(directionBuffer); 
					
						publishProgress(directionBuffer);
						Log.d("ZZZZZ", "doInBackground after read");
						//sleep(2000);
					}
					
					//closeSockets();
					
				}	
			}
			catch(IOException e)
			{
				e.printStackTrace();
				Log.d("ZZZZZ", "doInBackground IOException");
				result = true;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				Log.d("ZZZZZ", "doInBackground Exception");
				result = true;
			}		
			
			
			finally
			{
				Log.d("ZZZZZ", "doInBackground close sockets");
				try
				{
					yunin.close();
					yunout.close();
					yunsocket.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
					result = true;
				}
				catch(Exception e)
				{
					e.printStackTrace();
					result = true;
				}
			}
			return result;
		}
		
	
		@Override 
		protected void onProgressUpdate(byte[]...direct)
		{
			super.onProgressUpdate(direct);
			
			ByteBuffer bb = ByteBuffer.wrap(direct[0]);
			bb.order(ByteOrder.LITTLE_ENDIAN);			
			//int direction = bb.getInt();
			
			/*
			TextView dataReceived = (TextView)findViewById(R.id.DataReceived);
			
			switch (direction) {
			case 1:
				dataReceived.setText("Forward");
				break;
			case 2:
				dataReceived.setText("Backward");
				break;
			case 3:
				dataReceived.setText("Left");
				break;
			case 4:
				dataReceived.setText("Right");
				break;
			case 5:
				dataReceived.setText("Halt");
				break;
			default:
				dataReceived.setText("Nothing");
				break;
			}
			*/
		}
		
		
		public void SendDirectionToYun(int direction) 
		{
        	//TextView InfoText1 = (TextView)findViewById(R.id.infoText1);
			
			try {
                if (yunsocket.isConnected()) 
                {               	
                	//InfoText1.setText("Yun Connected");
                	
                    Log.d("ZZZZZ", "SendDirectionToYun: Writing received message to socket");
                    
                    yunout.writeInt(direction);
                    
                } else {
                	//InfoText1.setText("Yun Not Connected. Socket closed");
                    Log.d("ZZZZZ", "SendDirectionToYun: Cannot send message. Socket is closed");
                }
            } catch (Exception e) {
            	//InfoText1.setText("Yun Not Connected. Message send failed.");
                Log.d("ZZZZZ", "SendDirectionToYun: Message send failed. Caught an exception");
                e.printStackTrace();
            }
		}
		
//		public Boolean closeSockets()
//		{
//			boolean result = false;
//			
//			try
//			{
//				Log.d("ZZZZZ", "Close Datastreams");
//				yunin.close();
//				yunout.close();
//				Log.d("ZZZZZ", "Close Socket");
//				yunsocket.close();
//				InfoText1.setText("Yun Not Connected. Socket closed");
//			}
//			catch(IOException e)
//			{
//				e.printStackTrace();
//				result = true;
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace();
//				result = true;
//			}
//			return result;
//		}
	}
}
