package com.voiche.musicwidget;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.R.color;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.View;
import android.widget.RemoteViews;

import com.voiche.musicwidget.R.drawable;

public class WalkmanWidget_4x3 extends AppWidgetProvider {
	AppWidgetManager manager;
	ComponentName appWidgetIds;
	RemoteViews views, updaterviews, buttonviews, albumviews, infoviews;
	Context context;
	int buttonType = 0;
	String les;
	int lsm = 0;
	int showProgress = -1, showTime = -1;
	String daa = "true", dan = "true";
	boolean adjustUi = false;
	static SharedPreferences shared;

	@Override
	public void onReceive(Context context, Intent intent) {
		// ystem.out.println(intent.getAction());

		shared = context.getSharedPreferences("settings_4x3",
				context.MODE_PRIVATE);

		boolean wait = shared.getBoolean("wait", true);

		if (intent.getAction().equals(
				"android.appwidget.action.APPWIDGET_DELETED")
				|| intent.getAction().equals(
						"android.appwidget.action.APPWIDGET_DISABLED")) {
			// Intent destroyIntent = new Intent();
			// destroyIntent.setAction("com.voiche.musicwidget.DESTROY");
			// context.sendBroadcast(destroyIntent);
		} else if (wait != true)
			context.startService(new Intent(context, WidgetService.class));

		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// TODO Auto-generated method stub

		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	public void create(Context context) {
		this.context = context;
		adjustUi = false;

		shared = context.getSharedPreferences("settings_4x3",
				context.MODE_PRIVATE);

		views = new RemoteViews(this.context.getPackageName(),
				R.layout.main_4x3);
		updaterviews = new RemoteViews(this.context.getPackageName(),
				R.layout.main_4x3);
		buttonviews = new RemoteViews(this.context.getPackageName(),
				R.layout.main_4x3);
		albumviews = new RemoteViews(this.context.getPackageName(),
				R.layout.main_4x3);
		infoviews = new RemoteViews(this.context.getPackageName(),
				R.layout.main_4x3);
		appWidgetIds = new ComponentName(context, WalkmanWidget_4x3.class);
		manager = AppWidgetManager.getInstance(this.context);

		adjustUI();
	}

	private void adjustUI() {

		boolean update = shared.getBoolean("update", false);
		if (adjustUi != true)
			update = true;

		if (update != true)
			return;

		adjustUi = true;

		views = new RemoteViews(this.context.getPackageName(),
				R.layout.main_4x3);
		updaterviews = new RemoteViews(this.context.getPackageName(),
				R.layout.main_4x3);
		buttonviews = new RemoteViews(this.context.getPackageName(),
				R.layout.main_4x3);
		albumviews = new RemoteViews(this.context.getPackageName(),
				R.layout.main_4x3);
		infoviews = new RemoteViews(this.context.getPackageName(),
				R.layout.main_4x3);

		String bgrcolor = (shared.getString("bgrcolor", "16777215"));// -1509949440
																		// for
																		// normal
		String fontcolor = shared.getString("fontcolor", "-1");
		String tnfontcolor = shared.getString("tnfontcolor", "-1");
		String fontsize = shared.getString("fontsize", "14");// 12 for normal
		String tnfontsize = shared.getString("tnfontsize", "16");// 14 for
																	// normal
		String buttons = (shared.getString("buttons", "2"));// 2 for normal
		les = shared.getString("les", "true");
		daa = shared.getString("daa", "true");
		dan = shared.getString("dan", "true");
		String lsms = shared.getString("lsm", "false");
		String spb = shared.getString("spb", "true");
		String sti = shared.getString("sti", "true");

		buttonType = Integer.parseInt(buttons);

		if (!dan.equals("true"))
			views.setViewVisibility(R.id.album, View.GONE);
		else
			views.setViewVisibility(R.id.album, View.VISIBLE);
		if (!les.equals("true"))
			views.setViewVisibility(R.id.LES, View.GONE);
		else
			views.setViewVisibility(R.id.LES, View.VISIBLE);
		if (!daa.equals("true"))
			views.setViewVisibility(R.id.imageView1, View.GONE);
		else
			views.setViewVisibility(R.id.imageView1, View.VISIBLE);
		if (!lsms.equals("false"))
			lsm = 2;
		else
			lsm = 1;
		if (spb.equals("true")) {
			showProgress = 1;
			views.setViewVisibility(R.id.progress, View.VISIBLE);
		} else {
			views.setViewVisibility(R.id.progress, View.GONE);
			showProgress = 0;
		}
		if (sti.equals("true")) {
			showTime = 1;
			views.setViewVisibility(R.id.totaltime, View.VISIBLE);
			views.setViewVisibility(R.id.curtime, View.VISIBLE);
		} else {
			views.setViewVisibility(R.id.totaltime, View.GONE);
			views.setViewVisibility(R.id.curtime, View.GONE);
			showTime = 0;
		}
		if (!spb.equals("true") && !sti.equals("true")) {
			views.setViewVisibility(R.id.timelayout, View.GONE);
		} else {
			views.setViewVisibility(R.id.timelayout, View.VISIBLE);
		}

		if (buttonType == 2) {

			views.setImageViewResource(R.id.PrevIV, color.transparent);
			views.setImageViewResource(R.id.NextIV, color.transparent);
			buttonviews.setImageViewResource(R.id.PlayPauseIV,
					color.transparent);
			views.setInt(R.id.prev_button, "setBackgroundResource",
					drawable.prev_xs_xml);
			views.setInt(R.id.next_button, "setBackgroundResource",
					drawable.next_xs_xml);
			updatePlayButton(false);

		} else if (buttonType == 1) {
			views.setImageViewResource(R.id.PrevIV, drawable.prev);
			views.setImageViewResource(R.id.NextIV, drawable.next);
			views.setInt(R.id.play_button, "setBackgroundResource",
					drawable.button_background);
			views.setInt(R.id.prev_button, "setBackgroundResource",
					drawable.button_background);
			views.setInt(R.id.next_button, "setBackgroundResource",
					drawable.button_background);
		} else if (buttonType == 3) {
			views.setImageViewResource(R.id.PrevIV, color.transparent);
			views.setImageViewResource(R.id.NextIV, color.transparent);
			buttonviews.setImageViewResource(R.id.PlayPauseIV,
					color.transparent);
			views.setInt(R.id.prev_button, "setBackgroundResource",
					drawable.prev_filled_xml);
			views.setInt(R.id.next_button, "setBackgroundResource",
					drawable.next_filled_xml);
			updatePlayButton(false);
		}

		views.setInt(R.id.backgroundIV, "setColorFilter",
				Integer.parseInt(bgrcolor));
		if (Build.VERSION.SDK_INT >= 16)
			views.setInt(R.id.backgroundIV, "setImageAlpha",
					Color.alpha(Integer.parseInt(bgrcolor)));
		else
			views.setInt(R.id.backgroundIV, "setAlpha",
					Color.alpha(Integer.parseInt(bgrcolor)));
		views.setTextColor(R.id.track, Integer.parseInt(tnfontcolor));
		views.setTextColor(R.id.artist, Integer.parseInt(fontcolor));
		views.setTextColor(R.id.album, Integer.parseInt(fontcolor));
		views.setFloat(R.id.track, "setTextSize", Float.parseFloat(tnfontsize));
		views.setFloat(R.id.artist, "setTextSize", Float.parseFloat(fontsize));

		buttonListeners();

		SharedPreferences.Editor editor = shared.edit();
		editor.putBoolean("update", false);
		editor.commit();
	}

	private void buttonListeners() {
		PendingIntent Prev = PendingIntent.getBroadcast(context, 0, new Intent(
				"com.voiche.musicwidget.PREV"), 0);
		PendingIntent PlayPause = PendingIntent.getBroadcast(context, 0,
				new Intent("com.voiche.musicwidget.PLAYPAUSE"), 0);
		PendingIntent Next = PendingIntent.getBroadcast(context, 0, new Intent(
				"com.voiche.musicwidget.NEXT"), 0);
		PendingIntent OpenPlayer = PendingIntent.getBroadcast(context, 0,
				new Intent("com.voiche.musicwidget.OPENPLAYER"), 0);
		PendingIntent OpenSettings = PendingIntent.getBroadcast(context, 0,
				new Intent("com.voiche.musicwidget.OPENSETTINGS_4x3"), 0);
		PendingIntent OpenLE = PendingIntent.getBroadcast(context, 0,
				new Intent("com.voiche.musicwidget.OPENLE"), 0);

		views.setOnClickPendingIntent(R.id.imageView1, OpenPlayer);
		views.setOnClickPendingIntent(R.id.mainlayout, OpenSettings);
		views.setOnClickPendingIntent(R.id.lyrics, OpenLE);
		views.setOnClickPendingIntent(R.id.prev_button, Prev);
		views.setOnClickPendingIntent(R.id.play_button, PlayPause);
		views.setOnClickPendingIntent(R.id.next_button, Next);
		views.setOnClickPendingIntent(R.id.track, OpenPlayer);
		manager.updateAppWidget(appWidgetIds, views);
	}

	public void updatePlayButton(boolean bool) {
		if (buttonType == 0) {
			SharedPreferences shared = context.getSharedPreferences(
					"settings_4x3", context.MODE_PRIVATE);
			SharedPreferences.Editor editor = shared.edit();
			editor.putBoolean("update", true);
			editor.commit();
			adjustUI();
		}

		if (buttonType == 2) {
			if (bool == true)
				buttonviews.setInt(R.id.play_button, "setBackgroundResource",
						drawable.pause_xs_xml);
			else
				buttonviews.setInt(R.id.play_button, "setBackgroundResource",
						drawable.play_xs_xml);
		} else if (buttonType == 1) {
			if (bool == true)
				buttonviews.setImageViewResource(R.id.PlayPauseIV,
						drawable.pause);
			else
				buttonviews.setImageViewResource(R.id.PlayPauseIV,
						drawable.play);
		} else if (buttonType == 3) {
			if (bool == true)
				buttonviews.setInt(R.id.play_button, "setBackgroundResource",
						drawable.pause_filled_xml);
			else
				buttonviews.setInt(R.id.play_button, "setBackgroundResource",
						drawable.play_filled_xml);
		}
		if (lsm == 0) {
			SharedPreferences shared = context.getSharedPreferences(
					"settings_4x3", context.MODE_PRIVATE);
			SharedPreferences.Editor editor = shared.edit();
			editor.putBoolean("update", true);
			editor.commit();
			adjustUI();
		}
		if (lsm == 2 && bool != true)
			buttonviews.setViewVisibility(R.id.mainlayout, View.INVISIBLE);
		else if (lsm == 2)
			buttonviews.setViewVisibility(R.id.mainlayout, View.VISIBLE);
		manager.updateAppWidget(appWidgetIds, buttonviews);
		buttonListeners();
	}

	public void updateInfo(String title, String artist, String album,
			String total) {
		adjustUI();
		infoviews = new RemoteViews(this.context.getPackageName(),
				R.layout.main_4x3);
		infoviews.setTextViewText(R.id.track, title);
		infoviews.setTextViewText(R.id.artist, artist);
		if (dan.equals("true"))
			infoviews.setTextViewText(R.id.album, album);
		manager.updateAppWidget(appWidgetIds, infoviews);
		if (showTime == 1)
			updaterviews.setTextViewText(R.id.totaltime, total);
		buttonListeners();
	}

	public void updateIndicators(String cur, int progress) {
		if (showProgress == -1 || showTime == -1) {
			SharedPreferences shared = context.getSharedPreferences(
					"settings_4x3", context.MODE_PRIVATE);
			SharedPreferences.Editor editor = shared.edit();
			editor.putBoolean("update", true);
			editor.commit();
			adjustUI();
		}
		if (showProgress == 1)
			updaterviews.setProgressBar(R.id.progress, 1000, progress, false);
		if (showTime == 1) {
			updaterviews.setTextViewText(R.id.curtime, cur);
		}
		if (showProgress == 1 || showTime == 1) {
			manager.updateAppWidget(appWidgetIds, updaterviews);
			buttonListeners();
		}
	}

	public void updateAlbumArt() {
		if (!daa.equals("true"))
			return;
		File cacheDir = context.getCacheDir();
		File f = new File(cacheDir, "pic");
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Bitmap art = BitmapFactory.decodeStream(fis);
		try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		albumviews = new RemoteViews(this.context.getPackageName(),
				R.layout.main_4x3);
		albumviews.setImageViewBitmap(R.id.imageView1, art);
		manager.updateAppWidget(appWidgetIds, albumviews);
		buttonListeners();
	}

	public void reset() {
		manager.updateAppWidget(appWidgetIds, updaterviews);
		manager.updateAppWidget(appWidgetIds, buttonviews);
		manager.updateAppWidget(appWidgetIds, albumviews);
		manager.updateAppWidget(appWidgetIds, infoviews);
		buttonListeners();
	}
}