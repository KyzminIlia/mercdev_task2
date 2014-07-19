package com.example.filedownloader;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class DownloadActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DownloadFragment downloadFragment = (DownloadFragment) getSupportFragmentManager()
				.findFragmentByTag(DownloadFragment.FRAGMENT_TAG);
		if (downloadFragment == null) {
			downloadFragment = new DownloadFragment();
			getSupportFragmentManager()
					.beginTransaction()
					.add(android.R.id.content, downloadFragment,
							DownloadFragment.FRAGMENT_TAG).commit();
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(
				outState,
				DownloadFragment.FRAGMENT_TAG,
				getSupportFragmentManager().findFragmentByTag(
						DownloadFragment.FRAGMENT_TAG));
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(
						android.R.id.content,
						getSupportFragmentManager().getFragment(
								savedInstanceState,
								DownloadFragment.FRAGMENT_TAG),
						DownloadFragment.FRAGMENT_TAG).commit();
	}
}