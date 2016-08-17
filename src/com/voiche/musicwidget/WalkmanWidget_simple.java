package com.voiche.musicwidget;

import android.R.color;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import com.voiche.musicwidget.R.drawable;

public class WalkmanWidget_simple extends AppWidgetProvider {
	AppWidgetManager manager;
	ComponentName appWidgetIds;
	RemoteViews views, buttonviews, albumviews, infoviews;
	Context context;
	int buttonType = 0;
	String les;
	int lsm = 0;
	boolean showProgress = true, showTime = true;
	String daa = "true", dan = "false";
	boolean adjustUi = false;
	static SharedPreferences shared;

	@Override
	public void onReceive(Context context, Intent intent) {
		// ystem.out.println(intent.getAction());

		shared = context.getSharedPreferences("settings_simple",
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

		shared = context.getSharedPreferences("settings_simple",
				context.MODE_PRIVATE);

		views = new RemoteViews(this.context.getPackageName(),
				R.layout.main_simple);
		buttonviews = new RemoteViews(this.context.getPackageName(),
				R.layout.main_simple);
		albumviews = new RemoteViews(this.context.getPackageName(),
				R.layout.main_simple);
		infoviews = new RemoteViews(this.context.getPackageName(),
				R.layout.main_simple);
		appWidgetIds = new ComponentName(context, WalkmanWidget_simple.class);
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
				R.layout.main_simple);
		buttonviews = new RemoteViews(this.context.getPackageName(),
				R.layout.main_simple);
		albumviews = new RemoteViews(this.context.getPackageName(),
				R.layout.main_simple);
		infoviews = new RemoteViews(this.context.getPackageName(),
				R.layout.main_simple);

		String bgrcolor = (shared.getString("bgrcolor", "16777215"));// -1509949440
																		// for
																		// normal

		String buttons = (shared.getString("buttons", "2"));// 2 for normal
		String lsms = shared.getString("lsm", "false");

		buttonType = Integer.parseInt(buttons);

		if (!lsms.equals("false"))
			lsm = 2;
		else
			lsm = 1;

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
		PendingIntent OpenSettings = PendingIntent.getBroadcast(context, 0,
				new Intent("com.voiche.musicwidget.OPENSETTINGS_simple"), 0);

		views.setOnClickPendingIntent(R.id.prev_button, Prev);
		views.setOnClickPendingIntent(R.id.play_button, PlayPause);
		views.setOnClickPendingIntent(R.id.next_button, Next);
		views.setOnClickPendingIntent(R.id.mainlayout, OpenSettings);
		manager.updateAppWidget(appWidgetIds, views);
	}

	public void updatePlayButton(boolean bool) {
		if (buttonType == 0) {
			SharedPreferences shared = context.getSharedPreferences(
					"settings_simple", context.MODE_PRIVATE);
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
					"settings_simple", context.MODE_PRIVATE);
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

	public void reset() {
		manager.updateAppWidget(appWidgetIds, buttonviews);
		manager.updateAppWidget(appWidgetIds, albumviews);
		manager.updateAppWidget(appWidgetIds, infoviews);
		buttonListeners();
	}
}