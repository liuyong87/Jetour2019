package com.semisky.carlife;


public class CarLife {
	private static final String TAG = "Carlife";
	private static CarLife mCarlife;
	static {
		System.loadLibrary("carlifevehicle");
	}
	
	public CarLife() {
		//carLifeLibInit();
		
		//adbConnectionSetup();
		
	}
	
	public static CarLife getInstance() {
		if(mCarlife == null) {
			mCarlife = new CarLife();
		}
		return mCarlife;
	}
	
	

	public native void execLinuxCmd(String cmd);

}
