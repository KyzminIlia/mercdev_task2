package com.example.filedownloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadFragment extends Fragment implements
		LoaderCallbacks<Bitmap> {

	private String status;
	private String url = "";
	private boolean urlEditEnabled = true;
	private boolean buttonEnabled = true;
	private int visible = ProgressBar.INVISIBLE;
	public static final String FRAGMENT_TAG = DownloadFragment.class
			.getSimpleName();
	private static final String EXTRA_STATUS = "com.example.filedownloader.STATUS";
	private static final String EXTRA_EXCEPTION = "com.example.filedownloader.EXCEPTION";
	private static final String ACTION_CHANGE_STATUS = "com.example.filedownloader.CHANGE_STATUS";
	private static final String IMAGE_EXTENSION_FILTER = "((?i)(jpg|png|gif|bmp|jpeg|ico))";
	private boolean downloading = false;
	private boolean downloaded = false;
	public static String PREFS_URL = "com.example.filedownloader.URL";

	@Override
	public void onResume() {
		super.onResume();

		File downloadedImage = new File(Environment
				.getExternalStorageDirectory().getAbsolutePath()
				+ imgLoader.getImageName());
		if (downloadedImage.exists() && !downloading) {
			downloadButton.setEnabled(true);
			buttonEnabled = true;
			downloadButton.setText(getString(R.string.open_button_text));
			statusLabel.setText(getString(R.string.status_downloaded));
			status = statusLabel.getText().toString();
			downloadButton.setOnClickListener(new OpenClick());
			progressBar.setVisibility(ProgressBar.INVISIBLE);
			visible = ProgressBar.INVISIBLE;
			urlEditEnabled = true;
		} else {
			statusLabel.setText(status);
			downloadButton.setEnabled(buttonEnabled);
			downloadButton.setOnClickListener(new DownloadClick());
			progressBar.setVisibility(visible);
			urlEdit.setEnabled(urlEditEnabled);
			urlEdit.setText(url);
		}

	}

	@Override
	public void onStop() {

		super.onStop();
		Log.d(LOG_TAG, "fragment stopped");
		buttonEnabled = downloadButton.isEnabled();
		status = statusLabel.getText().toString();
		visible = progressBar.getVisibility();
		urlEditEnabled = urlEdit.isEnabled();
		url = urlEdit.getText().toString();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		SharedPreferences sharedURL = getActivity().getPreferences(0);
		SharedPreferences.Editor sharedURLEditor = sharedURL.edit();
		sharedURLEditor.putString(PREFS_URL, url);
		sharedURLEditor.commit();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
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
		urlEdit = (EditText) v.findViewById(R.id.url_edit);
		statusLabel.setText(status);
		downloadButton.setEnabled(buttonEnabled);
		downloadButton.setOnClickListener(new DownloadClick());
		progressBar.setVisibility(visible);
		urlEdit.setText(url);
		urlEdit.setEnabled(urlEditEnabled);
		urlEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {

				String imageName = urlEdit
						.getText()
						.toString()
						.substring(
								urlEdit.getText().toString().lastIndexOf('/') + 1);
				File downloadedImage = new File(Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/"
						+ imageName);

				if (imageName.equals("")) {
					imageName = getString(R.string.default_image_name);
					File image = new File(Environment
							.getExternalStorageDirectory().getAbsolutePath()
							+ "/" + imageName);
					if (image.exists()) {
						downloaded = false;
						imgLoader.setImageName(imageName);
						downloadButton
								.setText(getString(R.string.open_button_text));
						statusLabel
								.setText(getString(R.string.status_downloaded));
						status = statusLabel.getText().toString();
						downloadButton.setOnClickListener(new OpenClick());
					}

				} else if (downloadedImage.exists()) {
					downloaded = false;
					imgLoader.setImageName(imageName);
					downloadButton
							.setText(getString(R.string.open_button_text));
					statusLabel.setText(getString(R.string.status_downloaded));
					status = statusLabel.getText().toString();
					downloadButton.setOnClickListener(new OpenClick());

				} else {
					if (!downloading && !downloaded) {
						statusLabel.setText(getString(R.string.status_idle));
						status = statusLabel.getText().toString();
						downloadButton.setEnabled(buttonEnabled);
						downloadButton
								.setText(getString(R.string.download_button_text));
						downloadButton.setOnClickListener(new DownloadClick());
						progressBar.setVisibility(ProgressBar.INVISIBLE);
						visible = ProgressBar.INVISIBLE;
						urlEdit.setEnabled(urlEditEnabled);
					}
				}

			}
		});
		return v;
	}

	ProgressBar progressBar;
	ImageLoader imgLoader;
	String LOG_TAG = getClass().getSimpleName().toString();
	Button downloadButton;
	TextView statusLabel;
	BroadcastReceiver downloadReceiver;
	EditText urlEdit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(LOG_TAG, "fragment created");
		super.onCreate(savedInstanceState);
		SharedPreferences sharedURL = getActivity().getPreferences(0);
		url = sharedURL.getString(PREFS_URL, "");
		downloadReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context arg0, Intent arg1) {
				Intent msgIntent = arg1;
				int status = msgIntent.getIntExtra(EXTRA_STATUS, 0);
				if (getActivity() != null) {
					if (msgIntent.getStringExtra(EXTRA_EXCEPTION) == null) {
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
								msgIntent.getStringExtra(EXTRA_EXCEPTION),
								Toast.LENGTH_SHORT);
						toast.show();
					}

				}
			}
		};
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				downloadReceiver, new IntentFilter(ACTION_CHANGE_STATUS));
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
				if (urlEdit.getText().toString().equals(""))
					imgLoader.setStringUrl(getString(R.string.image_URL));
				else
					imgLoader.setStringUrl(urlEdit.getText().toString());
				progressBar.setProgress(0);
				downloading = true;
				imgLoader.forceLoad();
			} catch (Exception e) {
				statusLabel.setText(e.getMessage());
			}

		}

	}

	@Override
	public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
		imgLoader = new ImageLoader(getActivity());
		return imgLoader;
	}

	public class OpenClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://"
					+ Environment.getExternalStorageDirectory().getPath() + "/"
					+ imgLoader.getImageName()), "image/*");
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
		downloaded = true;

	}

	private static class ImageLoader extends AsyncTaskLoader<Bitmap> {
		public ImageLoader(Context context) {
			super(context);
		}

		private String stringUrl;
		private Bitmap pic;
		private String LOG_TAG = getClass().getSimpleName().toString();
		private String imageName;

		public void setImageName(String name) {
			imageName = name;
		}

		public String getImageName() {
			return imageName;
		}

		public void setStringUrl(String url) {
			stringUrl = url;
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
				imageName = stringUrl.substring(stringUrl.lastIndexOf('/') + 1);
				String extension = imageName
						.substring(imageName.indexOf('.') + 1);
				Pattern extensionPattern = Pattern
						.compile(IMAGE_EXTENSION_FILTER);
				if (extensionPattern.matcher(extension).matches()) {
					URL url = new URL(stringUrl);
					URLConnection connection = url.openConnection();
					connection.connect();
					int lenghtOfFile = connection.getContentLength();
					input = new BufferedInputStream(url.openStream(), 1024);
					OutputStream output = new FileOutputStream(Environment
							.getExternalStorageDirectory().getAbsolutePath()
							+ "/" + imageName);
					byte data[] = new byte[1024];
					long total = 0;
					while ((count = input.read(data)) != -1) {
						total += count;
						Log.d(LOG_TAG, "set message");
						int status = (int) ((total * 100) / lenghtOfFile);
						Intent msgIntent = new Intent(ACTION_CHANGE_STATUS);
						msgIntent.putExtra(DownloadFragment.EXTRA_STATUS,
								status);
						LocalBroadcastManager.getInstance(getContext())
								.sendBroadcast(msgIntent);
						output.write(data, 0, count);
						Log.d(LOG_TAG, "load " + total + "/" + lenghtOfFile);
					}
					pic = BitmapFactory.decodeFile(Environment
							.getExternalStorageDirectory().getAbsolutePath()
							+ "/" + imageName);
					pic.compress(Bitmap.CompressFormat.PNG, 100, output);
					output.flush();
					output.close();
					return pic;
				} else {
					String exception = getContext().getString(
							R.string.bad_extension_message);
					LocalBroadcastManager.getInstance(getContext())
							.sendBroadcast(
									new Intent(ACTION_CHANGE_STATUS).putExtra(
											DownloadFragment.EXTRA_EXCEPTION,
											exception));
				}
			} catch (MalformedURLException e) {
				String exception = getContext().getString(
						R.string.bad_URL_message);
				LocalBroadcastManager.getInstance(getContext()).sendBroadcast(
						new Intent(ACTION_CHANGE_STATUS).putExtra(
								DownloadFragment.EXTRA_EXCEPTION, exception));

			} catch (IOException e) {
				String exception = getContext().getString(
						R.string.connection_problem_message);
				LocalBroadcastManager.getInstance(getContext()).sendBroadcast(
						new Intent(ACTION_CHANGE_STATUS).putExtra(
								DownloadFragment.EXTRA_EXCEPTION, exception));
			}
			return null;

		}

	}

	@Override
	public void onLoaderReset(Loader<Bitmap> arg0) {

	}
}
