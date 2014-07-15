package com.example.filedownloader;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class DownloadActivity extends Activity implements LoaderCallbacks<Bitmap> {
	ProgressBar pb;
	ImageLoader imgLoader;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_download);
		pb  =(ProgressBar) findViewById(R.id.downloadPB);
		getLoaderManager().initLoader(0, null, this);
	}

	public void btnClick(View v) {
		Button btnDownload = (Button) v;
		btnDownload.setEnabled(false);
			
	}

	@Override
	public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
		imgLoader = new ImageLoader(this,getString(R.string.image_URL));	
		imgLoader.setHandler(handler);
		return null;
	}
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			if (msg!=null){
			int value =msg.getData().getInt("download");
			pb.setProgress(value);
			}
			}
	};
	
	@Override
	public void onLoadFinished(Loader<Bitmap> arg0, Bitmap arg1) {
		// TODO Auto-generated method stub
		startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("/sdcard/downloadedImage.png")));
		
	}

	@Override
	public void onLoaderReset(Loader<Bitmap> arg0) {
		// TODO Auto-generated method stub
		
	}
}