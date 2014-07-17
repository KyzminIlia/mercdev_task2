package com.example.filedownloader;

import android.widget.ProgressBar;

public class SavedData {
	private String status;
	private boolean enable = true;
	private int visible = ProgressBar.INVISIBLE;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public int getVisible() {
		return visible;
	}

	public void setVisible(int visible) {
		this.visible = visible;
	}

}
