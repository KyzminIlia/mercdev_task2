package com.example.filedownloader;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ImageLoader extends AsyncTaskLoader<Bitmap> {
	private String stringUrl;
	Bitmap pic;
	private Handler handler;

	public ImageLoader(Context context, String url) {
		super(context);
		this.stringUrl = url;
	}

	protected void publishMessage(int value) {
		if (handler != null) {
			Bundle downloadData = new Bundle();
			downloadData.putInt("download", value);
			Message msg = new Message();
			msg.setData(downloadData);
			handler.sendMessage(msg);
		}
	}

	@Override
	protected void onForceLoad() {
		super.onForceLoad();

	}

	@Override
	public Bitmap loadInBackground() {
		InputStream input;
		int count;
		try {
			URL url = new URL(stringUrl);
			URLConnection connection = url.openConnection();
			connection.connect();
			int lenghtOfFile = connection.getContentLength();
			input = new BufferedInputStream(url.openStream(), 8192);
			OutputStream output = new FileOutputStream(
					"/sdcard/downloadedImage.png");
			byte data[] = new byte[1024];
			long total = 0;
			while ((count = input.read(data)) != -1) {
				total += count;
				publishMessage((int) ((total * 100) / lenghtOfFile));
				output.write(data, 0, count);
			}
			pic = BitmapFactory.decodeFile("/sdcard/downloadedImage.png");
			pic.compress(Bitmap.CompressFormat.PNG, 100, output);
			output.flush();
			output.close();
		} catch (Exception e) {

		}
		return null;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

}
