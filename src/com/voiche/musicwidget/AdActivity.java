package com.voiche.musicwidget;

import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;
import com.startapp.android.publish.splash.SplashConfig;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class AdActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StartAppSDK.init(this, "102075883", "202008168", true);
		StartAppAd.showSplash(this, savedInstanceState,
				new SplashConfig().setTheme(SplashConfig.Theme.ASHEN_SKY));
		super.onCreate(savedInstanceState);
		finish();
	}
}
