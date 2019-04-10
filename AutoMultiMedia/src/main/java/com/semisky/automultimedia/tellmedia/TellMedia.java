package com.semisky.automultimedia.tellmedia;

import android.util.Log;

public class TellMedia {

	private static final String TAG = "TellMedia";

	public TellMediaListener mListener = null;

	private TellMediaThread mTellMediaThread = null;

	public void CreateThread()
	{
		mTellMediaThread = new TellMediaThread();
		mTellMediaThread.start();
	}

	public void DestroyThread( )
	{
		// finalization code here

		if (mTellMediaThread != null)
			mTellMediaThread.interrupt();
	}

	public void SetListenser(TellMediaListener listener)
	{
		mListener = listener;
	}

	private class TellMediaThread extends Thread {

		@Override
		public void run() {
			
			int rc;
			
			super.run();
			
			while(!isInterrupted()) {
				rc = select_tellmedia();
				if(rc == 1)
				{
					CallUdisk1();
				}
				else if(rc == 2)
				{
					CallUdisk0();
				}
			}			
		}
	}
  	
  	public void CallUdisk1()
    {
			Log.e(TAG, "tellmedia enter CallUdisk1");

			if(mListener != null)
    		{
						mListener.onUdisk1Unplugged();
    		}    		
  	}
  	
  	public void CallUdisk0()
    {
			Log.e(TAG, "tellmedia enter CallUdisk0");

			if(mListener != null)
    		{
					mListener.onUdisk0Unplugged();
    		}    		
  	}

	public native int select_tellmedia();
	
	static {
		System.loadLibrary("tellmedia");
	}
}
