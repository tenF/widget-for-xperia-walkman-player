package com.voiche.musicwidget;

import java.util.Timer;
import java.util.TimerTask;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.appbrain.AppBrain;
import com.appodeal.ads.Appodeal;
import com.appodeal.ads.InterstitialCallbacks;
import com.google.android.gms.ads.InterstitialAd;

public class Configure_4x4 extends PreferenceActivity {
	int awID;
	private InterstitialAd interstitial;
	int adHeight = 0;
	long caCount = -1;
	Timer myTimer;
	RelativeLayout adrl;
	Button closead;
	boolean showB = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences sp = getSharedPreferences("settings_4x4",
				MODE_PRIVATE);
		// ystem.out.println("ASDASD");
		// startActivity(new Intent(this, AdActivity.class));
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.configure_4x4);
		setContentView(R.layout.ad);

		adrl = (RelativeLayout) findViewById(R.id.adrl);
		closead = (Button) findViewById(R.id.cc);
		closead.setVisibility(View.GONE);
		AppBrain.init(this);
		String adType = AppBrain.getSettings().get("ad_type", "interstitial");
		String appKey = "c76589ed1c52950e0ee82fa9146f7bc4b230e872b79d47e1";
		Appodeal.disableLocationPermissionCheck();
		if (adType.equals("banner")) {
			showB = true;
			if (caCount >= 0) {
				adrl.setVisibility(View.GONE);
			}
			Appodeal.setBannerViewId(R.id.appodealBannerView);
			Appodeal.initialize(this, appKey, Appodeal.BANNER);
			Appodeal.show(this, Appodeal.BANNER_VIEW);
			timer();
		} else {
			Appodeal.setAutoCache(Appodeal.INTERSTITIAL, false);
			Appodeal.initialize(this, appKey, Appodeal.INTERSTITIAL);
			Appodeal.cache(this, Appodeal.INTERSTITIAL);
			Appodeal.setInterstitialCallbacks(new InterstitialCallbacks() {
				public void onInterstitialLoaded(boolean isPrecache) {
					Appodeal.show(Configure_4x4.this, Appodeal.INTERSTITIAL);
				}

				public void onInterstitialFailedToLoad() {
					// your code for onInterstitialFailedToLoad
				}

				public void onInterstitialShown() {
					// your code for onInterstitialShown
				}

				public void onInterstitialClicked() {
					// your code for onInterstitialClicked
				}

				public void onInterstitialClosed() {
					// your code for onInterstitialClosed
				}
			});
		}

		Intent i = getIntent();
		Bundle extras = i.getExtras();
		if (extras != null)
			awID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		// ystem.out.println("h "+awID);
		if (awID == AppWidgetManager.INVALID_APPWIDGET_ID)
			finish();

		SharedPreferences shared = getSharedPreferences("settings_4x4",
				MODE_PRIVATE);
		SharedPreferences.Editor editor = shared.edit();
		editor.putInt("awID", awID);
		editor.putBoolean("wait", true);
		editor.commit();
		this.stopService(new Intent(this, WidgetService.class));

		// Get the custom preference
		Preference customPref = (Preference) findPreference("customPref");
		customPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
								Uri.fromParts("mailto", "voichestd@gmail.com",
										null));
						emailIntent.putExtra(Intent.EXTRA_SUBJECT,
								"Music Widget for Walkman");
						startActivity(Intent.createChooser(emailIntent,
								"Send email..."));
						return true;
					}

				});
		Preference cpTwitter = (Preference) findPreference("cpTwitter");
		cpTwitter.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				String url = "http://www.twitter.com/BuzzyMind";
				Intent i = new Intent(Intent.ACTION_VIEW);
				Uri u = Uri.parse(url);
				i.setData(u);
				startActivity(i);
				return true;
			}

		});
		Preference cpRC = (Preference) findPreference("cpRC");
		cpRC.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://details?id=com.voiche.musicwidget")));
				return true;
			}

		});

		Preference cpOA = (Preference) findPreference("cpOA");
		cpOA.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://search?q=pub:Voiche")));
				return true;
			}

		});

		final ListPreference buttonstyle = (ListPreference) getPreferenceManager()
				.findPreference("buttonPref_4x4");
		buttonstyle.setSummary(buttonstyle.getEntry());

		buttonstyle
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						SharedPreferences shared = getSharedPreferences(
								"settings_4x4", MODE_PRIVATE);
						SharedPreferences.Editor editor = shared.edit();
						editor.putString("buttons", newValue.toString());
						// ystem.out.println("configure buttons "+newValue.toString());
						editor.commit();
						buttonstyle.setValue(newValue.toString());
						preference.setSummary(buttonstyle.getEntry());
						return true;
					}
				});

		final CheckBoxPreference daa = (CheckBoxPreference) getPreferenceManager()
				.findPreference("daa_4x4");

		daa.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				SharedPreferences shared = getSharedPreferences("settings_4x4",
						MODE_PRIVATE);
				SharedPreferences.Editor editor = shared.edit();
				editor.putString("daa", newValue.toString());
				editor.commit();
				return true;
			}
		});

		final CheckBoxPreference dan = (CheckBoxPreference) getPreferenceManager()
				.findPreference("dan_4x4");

		dan.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				SharedPreferences shared = getSharedPreferences("settings_4x4",
						MODE_PRIVATE);
				SharedPreferences.Editor editor = shared.edit();
				editor.putString("dan", newValue.toString());
				editor.commit();
				return true;
			}
		});

		final CheckBoxPreference lsm = (CheckBoxPreference) getPreferenceManager()
				.findPreference("lsm_4x4");

		lsm.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				SharedPreferences shared = getSharedPreferences("settings_4x4",
						MODE_PRIVATE);
				SharedPreferences.Editor editor = shared.edit();
				editor.putString("lsm", newValue.toString());
				editor.commit();
				return true;
			}
		});

		final ColorPickerPreference fontcolor = (ColorPickerPreference) getPreferenceManager()
				.findPreference("fontcolor_4x4");

		fontcolor
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						SharedPreferences shared = getSharedPreferences(
								"settings_4x4", MODE_PRIVATE);
						SharedPreferences.Editor editor = shared.edit();
						editor.putString("fontcolor", newValue.toString());
						editor.commit();
						return true;
					}
				});

		final ColorPickerPreference tnfontcolor = (ColorPickerPreference) getPreferenceManager()
				.findPreference("tnfontcolor_4x4");

		tnfontcolor
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						SharedPreferences shared = getSharedPreferences(
								"settings_4x4", MODE_PRIVATE);
						SharedPreferences.Editor editor = shared.edit();
						editor.putString("tnfontcolor", newValue.toString());
						editor.commit();
						return true;
					}
				});

		final ListPreference fontselect = (ListPreference) getPreferenceManager()
				.findPreference("fontsize_4x4");
		fontselect.setSummary(fontselect.getEntry());

		fontselect
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						SharedPreferences shared = getSharedPreferences(
								"settings_4x4", MODE_PRIVATE);
						SharedPreferences.Editor editor = shared.edit();
						editor.putString("fontsize", newValue.toString());
						editor.commit();
						fontselect.setValue(newValue.toString());
						preference.setSummary(fontselect.getEntry());
						return true;
					}
				});

		final ListPreference tnfontselect = (ListPreference) getPreferenceManager()
				.findPreference("tnfontsize_4x4");
		tnfontselect.setSummary(tnfontselect.getEntry());

		tnfontselect
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						SharedPreferences shared = getSharedPreferences(
								"settings_4x4", MODE_PRIVATE);
						SharedPreferences.Editor editor = shared.edit();
						editor.putString("tnfontsize", newValue.toString());
						editor.commit();
						tnfontselect.setValue(newValue.toString());
						preference.setSummary(tnfontselect.getEntry());
						return true;
					}
				});

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public void onBackPressed() {
		Finish();
	}

	private void Finish() {
		Intent intent = new Intent();
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, awID);
		setResult(RESULT_OK, intent);

		this.startService(new Intent(this, WidgetService.class));

		SharedPreferences shared = getSharedPreferences("settings_4x4",
				MODE_PRIVATE);
		SharedPreferences.Editor editor = shared.edit();
		editor.putBoolean("update", true);
		editor.putBoolean("wait", false);
		editor.commit();

		sendBroadcast(new Intent().setAction("com.voiche.musicwidget.UPDATE"));

		finish();
	}

	public void displayInterstitial() {
		if (interstitial.isLoaded()) {
			interstitial.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.configure, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Finish();
			return true;

		case R.id.done:
			Finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void timer() {
		myTimer = new Timer();
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				TimerMethod();
			}

		}, 0, 100);
	}

	private void TimerMethod() {
		if (adHeight <= 0) {
			adHeight = adrl.getMeasuredHeight();
			if (adHeight > 0)
				this.runOnUiThread(closeadbutton);
		}
		if (caCount >= 0) {
			caCount += (long) 1;
			if (caCount >= 4000) {
				this.runOnUiThread(showad);
				caCount = -1;
			}
		}
	}

	private Runnable showad = new Runnable() {
		public void run() {
			adrl.setVisibility(View.VISIBLE);
		}
	};

	private Runnable closeadbutton = new Runnable() {
		public void run() {
			closead.setVisibility(View.VISIBLE);
		}
	};

	public void cc(View v) {
		adrl.setVisibility(View.GONE);
		caCount = 0;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		//ystem.out.println("ShowB: " + showB);
		if (showB) {
			Appodeal.onResume(this, Appodeal.BANNER);
			closead.setVisibility(View.GONE);
			adHeight = 0;
			caCount = -1;
		}
	}

}