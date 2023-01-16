package com.fgtit.fpcore;

import com.fgtit.data.Conversions;

public class FPMatch {

	private static FPMatch mMatch=null;
	
	public static FPMatch getInstance(){
		if(mMatch==null){
			mMatch=new FPMatch();
		}
		return mMatch;
	}

	public native int InitMatch(int inittype, String initcode);
	public native int MatchTemplate( byte[] piFeatureA, byte[] piFeatureB);
	
	public void ToStd(byte[] input,byte[] output){
		switch(Conversions.getInstance().GetDataType(input)){
		case 1:{
				System.arraycopy(input, 0, output, 0, 256);
			}
			break;
		case 2:{
				Conversions.getInstance().IsoToStd(1,input,output);
			}
			break;
		case 3:{
				Conversions.getInstance().IsoToStd(2,input,output);
			}
			break;
		}
	}

	public boolean MatchTemplateOne( byte[] piEnl, byte[] piMat,int score){
		int n=piEnl.length/256;
		byte[] tmp=new byte[256];
		for(int i=0;i<n;i++){
			System.arraycopy(piEnl,i*256, tmp, 0, 256);
			if(MatchTemplate(tmp,piMat)>=score){
				return true;
			}
		}
		return false;
	}
	public int MatchFingerData(byte[] piFeatureA, byte[] piFeatureB){
		int at= Conversions.getInstance().GetDataType(piFeatureA);
		int bt= Conversions.getInstance().GetDataType(piFeatureB);
		if((at==1)&&(bt==1)){
			return MatchTemplate(piFeatureA,piFeatureB);
		}else{
			byte adat[]=new byte[512];
			byte bdat[]=new byte[512];
			if(at==1){
				System.arraycopy(piFeatureA, 0, adat, 0, 256);
			}else{
				byte tmp[]=new byte[512];
				ToStd(piFeatureA,tmp);	
				Conversions.getInstance().StdChangeCoord(tmp, 256, adat, 1);
			}
			if(bt==1){
				System.arraycopy(piFeatureB, 0, bdat, 0, 256);
			}else{
				byte tmp[]=new byte[512];
				ToStd(piFeatureB,tmp);
				Conversions.getInstance().StdChangeCoord(tmp, 256, bdat, 1);
			}			
			return MatchTemplate(adat,bdat);
		}
	}

	public int MatchTemplateAll(byte[] piEnl, byte[] piMat,int score){
		int n = piEnl.length/256;
		int m = piMat.length/256;
		byte[] tmpEnl =new byte[256];
		byte[] tmpMat =new byte[256];
		for(int j = 0 ; j < m; j++){
			System.arraycopy(piMat, j * 256, tmpMat, 0, 256);
			for(int i = 0 ; i < n; i++){
				System.arraycopy(piEnl,i * 256, tmpEnl , 0, 256);
				if(MatchTemplate(tmpEnl,piMat) >= score){
					return n * m;
				}
			}
		}
		return 0;
	}
	static {
		System.loadLibrary("fgtitalg");
		System.loadLibrary("fpcore");
	}
}
