package com.example.filedownloader;

import java.io.BufferedInputStream;
import java.io.File;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
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

	private String status;
	private boolean enable = true;
	private int visible = ProgressBar.INVISIBLE;
	public static final String FRAGMENT_TAG = "DownloadFragment";
	private static final String STATUS_TAG = "status";
	private static final String EXCEPTION_TAG = "exception";
	private static final String BROADCAST_RECIEVER_ACTION = "com.example.filedownloader";
	private boolean downloading = false;

	@Override
	public void onStop() {

		super.onStop();
		Log.d(LOG_TAG, "fragment stopped");
		enable = downloadButton.isEnabled();
		status = statusLabel.getText().toString();
		visible = progressBar.getVisibility();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		getActivity().getApplicationContext().unregisterReceiver(
				downloadReceiver);
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
		statusLabel = (TextView) v.findViewById(R.id.status_label);
		downloadButton = (Button) v.findViewById(R.id.download_button);
		progressBar = (ProgressBar) v.findViewById(R.id.download_progress_bar);
		File downloadedImage = new File(Environment
				.getExternalStorageDirectory().getAbsolutePath()
				+ "/downloadedImage.png");
		if (downloadedImage.exists() && !downloading) {
			downloadButton.setEnabled(true);
			enable = true;
			downloadButton.setText(getString(R.string.open_button_text));
			statusLabel.setText(getString(R.string.status_downloaded));
			status = statusLabel.getText().toString();
			downloadButton.setOnClickListener(new OpenClick());
			progressBar.setVisibility(ProgressBar.INVISIBLE);
			visible = ProgressBar.INVISIBLE;
		} else {
			statusLabel.setText(status);
			downloadButton.setEnabled(enable);
			downloadButton.setOnClickListener(new DownloadClick());
			progressBar.setVisibility(visible);
		}
		return v;
	}

	ProgressBar progressBar;
	ImageLoader imgLoader;
	String LOG_TAG = getClass().getSimpleName().toString();
	Button downloadButton;
	TextView statusLabel;
	BroadcastReceiver downloadReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(LOG_TAG, "fragment created");
		super.onCreate(savedInstanceState);
		downloadReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				Intent msgIntent = arg1;
				int status = msgIntent.getIntExtra(STATUS_TAG, 0);
				if (getActivity() != null) {
					if (msgIntent.getStringExtra(EXCEPTION_TAG) == null) {
						Log.d(LOG_TAG, "message delivered");
						statusLabel
								.setText(getString(R.string.status_downloading));
						progressBar.setProgress(status);
					} else {
						Log.d(LOG_TAG, "message delivered");
						downloading = false;
						imgLoader.cancelLoad();
						statusLabel.setText(getString(R.string.status_idle));
						downloadButton.setEnabled(true);
						downloadButton
								.setText(getString(R.string.download_button_text));
						progressBar.setVisibility(ProgressBar.INVISIBLE);
						Toast toast = Toast.makeText(getView().getContext(),
								msgIntent.getStringExtra(EXCEPTION_TAG),
								Toast.LENGTH_SHORT);
						toast.show();
					}

				}
			}
		};
		getActivity().getApplicationContext().registerReceiver(
				downloadReceiver, new IntentFilter(BROADCAST_RECIEVER_ACTION));
		setRetainInstance(true);
		status = getString(R.string.status_idle);
	}

	public class DownloadClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			Button btnDownload = (Button) v;
			btnDownload.setEnabled(false);
			progressBar.setVisibility(ProgressBar.VISIBLE);
			try {
				downloading = true;
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
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(
					Uri.parse("file://"
							+ Environment.getExternalStorageDirectory()
									.getPath() + "/downloadedImage.png"),
					"image/*");
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
		downloading = false;

	}

	private static class ImageLoader extends AsyncTaskLoader<Bitmap> {
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
				input = new BufferedInputStream(url.openStream(), 1024);
				OutputStream output = new FileOutputStream(Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/downloadedImage.png");
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
				pic = BitmapFactory.decodeFile(Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/downloadedImage.png");
				pic.compress(Bitmap.CompressFormat.PNG, 100, output);
				output.flush();
				output.close();
				return pic;
			} catch (IOException e) {
				String exception = "Невозможно соединиться с сайтом";
				getContext().sendBroadcast(
						new Intent(BROADCAST_RECIEVER_ACTION).putExtra(
								DownloadFragment.EXCEPTION_TAG, exception));
			}
			return null;

		}

	}

	@Override
	public void onLoaderReset(Loader<Bitmap> arg0) {

	}
}
