package com.example.filedownloader;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class DownloadActivity extends FragmentActivity {
	DownloadFragment downloadFragment = new DownloadFragment();
	FragmentTransaction ft;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ft = getSupportFragmentManager().beginTransaction();

		if (savedInstanceState != null) {

			downloadFragment = (DownloadFragment) getSupportFragmentManager()
					.getFragment(savedInstanceState, "downloadFragment");

			ft.replace(android.R.id.content, downloadFragment,
					"downloadFragment");

		} else {

			ft.add(android.R.id.content, downloadFragment, "downloadFragment");
		}

		ft.commit();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		getSupportFragmentManager().putFragment(outState, "downloadFragment",
				downloadFragment);
	}
}