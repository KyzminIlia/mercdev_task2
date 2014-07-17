package com.example.filedownloader;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.content.Intent;
import android.content.Loader.OnLoadCanceledListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.Loader.OnLoadCompleteListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadFragment extends Fragment implements
		LoaderCallbacks<Bitmap> {
	SavedData data = new SavedData();

	@Override
	public void onStop() {
		super.onStop();
		Log.d(LOG_TAG, "fragment stopped");
		data.setEnable(downloadButton.isEnabled());
		data.setStatus(statusLabel.getText().toString());
		data.setVisible(pb.getVisibility());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.d(LOG_TAG, "view created");
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.f_download, null);
		pb = (ProgressBar) v.findViewById(R.id.downloadPB);
		statusLabel = (TextView) v.findViewById(R.id.status_label);
		statusLabel.setText(data.getStatus());
		downloadButton = (Button) v.findViewById(R.id.downloadButton);
		downloadButton.setEnabled(data.isEnable());
		downloadButton.setOnClickListener(new DownloadClick());
		pb.setVisibility(data.getVisible());
		return v;
	}

	ProgressBar pb;
	ImageLoader imgLoader;
	String LOG_TAG = getClass().getSimpleName().toString();
	Button downloadButton;
	TextView statusLabel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(LOG_TAG, "fragment created");
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		data.setStatus(getString(R.string.status_idle));
	}

	public class DownloadClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			Loader<Bitmap> loader;
			Button btnDownload = (Button) v;
			btnDownload.setEnabled(false);
			pb.setVisibility(ProgressBar.VISIBLE);
			try {
				imgLoader.forceLoad();
			} catch (Exception e) {
				statusLabel.setText(e.getMessage());
			}

		}

	}

	@Override
	public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
		imgLoader = new ImageLoader(getActivity(),
				getString(R.string.image_URL));
		imgLoader.setHandler(handler);
		return imgLoader;
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg != null) {
				if (getView() != null)
					if (msg.getData().getString("exception") == null) {
						Log.d(LOG_TAG, "message delivered");
						statusLabel
								.setText(getString(R.string.status_downloading));
						int value = msg.getData().getInt("download");
						pb.setProgress(value);
					} else {
						Log.d(LOG_TAG, "message delivered");
						imgLoader.cancelLoad();
						statusLabel.setText(getString(R.string.status_idle));
						downloadButton.setEnabled(true);
						downloadButton
								.setText(getString(R.string.download_button_text));
						pb.setVisibility(ProgressBar.INVISIBLE);
						Toast toast = Toast.makeText(getView().getContext(),
								msg.getData().getString("exception"),
								Toast.LENGTH_LONG);
						toast.show();
					}
			}
		}
	};

	public class OpenClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://"
					+ "/sdcard/downloadedImage.png"), "image/*");
			startActivity(intent);
		}

	}

	@Override
	public void onLoadFinished(Loader<Bitmap> arg0, Bitmap arg1) {
		statusLabel.setText(getString(R.string.status_downloaded));
		downloadButton.setEnabled(true);
		downloadButton.setText(getString(R.string.open_button_text));
		downloadButton.setOnClickListener(new OpenClick());
		pb.setVisibility(ProgressBar.INVISIBLE);

	}

	@Override
	public void onLoaderReset(Loader<Bitmap> arg0) {
		// TODO Auto-generated method stub

	}

	private static class ImageLoader extends AsyncTaskLoader<Bitmap> {
		private boolean canceled = true;
		private String stringUrl;
		private Bitmap pic;
		private Handler handler;
		private String LOG_TAG = getClass().getSimpleName().toString();

		public ImageLoader(Context context, String url) {
			super(context);
			this.stringUrl = url;
			Log.d(LOG_TAG, "loader constructor");
		}

		protected void publishMessage(int value) {
			if (handler != null) {
				Log.d(LOG_TAG, "message has published");
				Bundle downloadData = new Bundle();
				downloadData.putInt("download", value);
				Message msg = new Message();
				msg.setData(downloadData);
				handler.sendMessage(msg);
			}
		}

		protected void publishException(String message) {
			if (handler != null) {
				Log.d(LOG_TAG, "exception has published");
				Bundle exceptionData = new Bundle();
				exceptionData.putString("exception", message);
				Message msg = new Message();
				msg.setData(exceptionData);
				handler.sendMessage(msg);
			}
		}

		@Override
		protected void onForceLoad() {
			super.onForceLoad();
			canceled = false;

		}

		@Override
		public void onCanceled(Bitmap data) {
			super.onCanceled(data);
			canceled = true;

		}

		@Override
		public Bitmap loadInBackground() {
			InputStream input;
			int count;
			try {
				Log.d(LOG_TAG, "background load");
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
					Log.d(LOG_TAG, "set message");
					publishMessage((int) ((total * 100) / lenghtOfFile));
					output.write(data, 0, count);
					Log.d(LOG_TAG, "load " + total + "/" + lenghtOfFile);
				}
				pic = BitmapFactory.decodeFile("/sdcard/downloadedImage.png");
				pic.compress(Bitmap.CompressFormat.PNG, 100, output);
				output.flush();
				output.close();
				return pic;
			} catch (IOException e) {
				publishException("Ошибка ввода/вывода :" + e.getMessage());
			} catch (NullPointerException e) {
				publishException("Ошибка : Не удалось соединиться с сайтом");
			}
			return null;

		}

		public void setHandler(Handler handler) {
			this.handler = handler;
		}

		public boolean isCanceled() {
			return canceled;
		}

		public void setCanceled(boolean cancelled) {
			this.canceled = cancelled;
		}

	}
}
