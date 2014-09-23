package com.harbinpointech.carcenter.activity;

import android.os.Bundle;
import android.widget.ProgressBar;

import com.harbinpointech.carcenter.R;

import java.io.File;

public class ShowNormalFileActivity extends BaseActivity {
	private ProgressBar progressBar;
	private File file;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_file);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);


		
	}
}
