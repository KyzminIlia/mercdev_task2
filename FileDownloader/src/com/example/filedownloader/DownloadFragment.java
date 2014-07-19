package com.example.filedownloader;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
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
	public static final String FRAGMENT_TAG = "DownloadFragment";
	private static final String STATUS_TAG = "status";
	private static final String DOWNLOAD_STATUS_TAG = "download status";
	private static final String EXCEPTION_TAG = "exception";
	private static final String BROADCAST_RECIEVER_ACTION = "com.example.filedownloader";

	@Override
	public void onStop() {
		super.onStop();
		Log.d(LOG_TAG, "fragment stopped");
		data.setEnable(downloadButton.isEnabled());
		data.setStatus(statusLabel.getText().toString());
		data.setVisible(progressBar.getVisibility());
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
		progressBar = (ProgressBar) v.findViewById(R.id.download_progress_bar);
		statusLabel = (TextView) v.findViewById(R.id.status_label);
		statusLabel.setText(data.getStatus());
		downloadButton = (Button) v.findViewById(R.id.download_button);
		downloadButton.setEnabled(data.isEnable());
		downloadButton.setOnClickListener(new DownloadClick());
		progressBar.setVisibility(data.getVisible());
		return v;
	}

	ProgressBar progressBar;
	ImageLoader imgLoader;
	String LOG_TAG = getClass().getSimpleName().toString();
	Button downloadButton;
	TextView statusLabel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(LOG_TAG, "fragment created");
		super.onCreate(savedInstanceState);
		BroadcastReceiver downloadReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				Intent msgIntent = arg1;
				int status = msgIntent.getIntExtra(STATUS_TAG, 0);
				if (msgIntent.getStringExtra(EXCEPTION_TAG) == null) {
					Log.d(LOG_TAG, "message delivered");
					statusLabel.setText(getString(R.string.status_downloading));
					progressBar.setProgress(status);
				} else {
					Log.d(LOG_TAG, "message delivered");
					imgLoader.cancelLoad();
					statusLabel.setText(getString(R.string.status_idle));
					downloadButton.setEnabled(true);
					downloadButton
							.setText(getString(R.string.download_button_text));
					progressBar.setVisibility(ProgressBar.INVISIBLE);
					Toast toast = Toast.makeText(getView().getContext(),
							msgIntent.getStringExtra(EXCEPTION_TAG),
							Toast.LENGTH_LONG);
					toast.show();
				}

			}
		};
		getActivity().getApplicationContext().registerReceiver(
				downloadReceiver, new IntentFilter(BROADCAST_RECIEVER_ACTION));
		setRetainInstance(true);
		data.setStatus(getString(R.string.status_idle));
	}

	public class DownloadClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			Loader<Bitmap> loader;
			Button btnDownload = (Button) v;
			btnDownload.setEnabled(false);
			progressBar.setVisibility(ProgressBar.VISIBLE);
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
		return imgLoader;
	}

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
		progressBar.setVisibility(ProgressBar.INVISIBLE);

	}

	@Override
	public void onLoaderReset(Loader<Bitmap> arg0) {
		// TODO Auto-generated method stub

	}

	private static class ImageLoader extends AsyncTaskLoader<Bitmap> {
		private boolean canceled = true;
		private String stringUrl;
		private Bitmap pic;
		private String LOG_TAG = getClass().getSimpleName().toString();

		public ImageLoader(Context context, String url) {
			super(context);
			this.stringUrl = url;
			Log.d(LOG_TAG, "loader constructor");
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
					int status = (int) ((total * 100) / lenghtOfFile);
					Intent msgIntent = new Intent(BROADCAST_RECIEVER_ACTION);
					msgIntent.putExtra(DownloadFragment.STATUS_TAG, status);
					getContext().sendBroadcast(msgIntent);
					output.write(data, 0, count);
					Log.d(LOG_TAG, "load " + total + "/" + lenghtOfFile);
				}
				pic = BitmapFactory.decodeFile("/sdcard/downloadedImage.png");
				pic.compress(Bitmap.CompressFormat.PNG, 100, output);
				output.flush();
				output.close();
				return pic;
			} catch (IOException e) {
				String exception = "Ошибка ввода/вывода";
				getContext().sendBroadcast(
						new Intent(BROADCAST_RECIEVER_ACTION).putExtra(
								DownloadFragment.EXCEPTION_TAG, exception));
			} catch (NullPointerException e) {
				String exception = "Не удалось соединиться с сайтом";
				getContext().sendBroadcast(
						new Intent(BROADCAST_RECIEVER_ACTION).putExtra(
								DownloadFragment.EXCEPTION_TAG, exception));

			}
			return null;

		}

		public boolean isCanceled() {
			return canceled;
		}

		public void setCanceled(boolean cancelled) {
			this.canceled = cancelled;
		}

	}
}
