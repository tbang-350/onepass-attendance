package com.fgtit.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtil {

	public static void showToast(Context context,String msg) {
         Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
	
	
	public static void showToast(Context context,int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
	}

	public static void showToastTop(Context context,String msg) {
		Toast toast=Toast.makeText(context, msg, Toast.LENGTH_SHORT/*2000 */);
		toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 120);  
		toast.show();
	}

	public static void showToastDef(Context context,String msg,int pos,int tm) {
		Toast toast=Toast.makeText(context, msg, tm);
		toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, pos);  
		toast.show();
	}
	
	public static void showToastEx(Context context,String msg,int tm) {
		Toast toast=Toast.makeText(context, msg, tm);
		toast.setGravity(Gravity.CENTER, 0, 0);  
		toast.show();
	}
	
	/*
	Toast toast=Toast.makeText(getApplicationContext(), "toast", 3000);
	toast.setGravity(Gravity.CENTER, 0, 0);
	ImageView imageView= new ImageView(getApplicationContext());
	imageView.setImageResource(R.drawable.ic_launcher);
	LinearLayout toastView = (LinearLayout) toast.getView();
	toastView.setOrientation(LinearLayout.HORIZONTAL);
	toastView.addView(imageView, 0); 
	toast.show();
	*/

	/*
	private void showToast() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.toast, null);
        image = (ImageView) view.findViewById(R.id.image);
        title = (TextView) view.findViewById(R.id.title);
        content = (TextView) view.findViewById(R.id.content);
        image.setBackgroundResource(R.drawable.ic_launcher);
        title.setText("toast");
        content.setText("hello,self toast");
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }
	*/

}
