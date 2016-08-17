package com.voiche.musicwidget;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.drive.internal.t;
import com.sonyericsson.advancedwidget.music.Worker;
import com.sonyericsson.advancedwidget.music.serviceconnection.MediaPlayerServiceConnectionListener;
import com.sonyericsson.music.IPlayback;

public class WidgetService extends Service implements
		MediaPlayerServiceConnectionListener, ServiceConnection {

	static IPlayback mSemcService;
	private volatile static boolean mConnected = false;
	private Object mConnectionLock = new Object();
	private MediaPlayerServiceConnectionListener mListener;
	private boolean mServiceBindSuccess = false;
	private Worker mWorker = new Worker(10);
	boolean mFirstToBePrepared, mIsRegistered = false;
	Timer myTimer;
	long curDuration = 0;
	boolean registered = false;
	boolean screenState = false;
	long duration = -1;
	boolean timerStarted = false;
	int dtap = 0;
	int auc = 0;
	boolean destroy = false;
	private Context contxt;
	WalkmanWidget small;
	WalkmanWidget_4x3 large;
	WalkmanWidget_4x4 compact;
	WalkmanWidget_4x2 medium;
	WalkmanWidget_simple simple;
	boolean pressable = true;
	int pressCount = 0;
	Bitmap def;
	File cacheDir;
	File f;

	@Override
	public void onCreate() {
		try {
			getConnection();

			cacheDir = getBaseContext().getCacheDir();
			f = new File(cacheDir, "pic");

			small = new WalkmanWidget();
			small.create(this);
			large = new WalkmanWidget_4x3();
			large.create(this);
			compact = new WalkmanWidget_4x4();
			compact.create(this);
			medium = new WalkmanWidget_4x2();
			medium.create(this);
			simple = new WalkmanWidget_simple();
			simple.create(this);

			updateUI();
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			screenState = pm.isScreenOn();

			contxt = this;
		} catch (Exception e) {
		}
	}

	private void getConnection() {
		bindToService(getBaseContext(), this);
		registerBroadcastReceivers();
		registered = true;
	}

	private void timer() {
		timerStarted = true;
		myTimer = new Timer();
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					if (mConnected == true && mSemcService.isPlaying()
							&& screenState == true)
						updater();
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}, 0, 1000);
	}

	public void updater() {
		long pos = getPosition();
		int progress = (int) ((double) pos * 1000 / duration);

		String cur = String.format(
				"%02d:%02d",
				TimeUnit.MILLISECONDS.toMinutes(pos),
				TimeUnit.MILLISECONDS.toSeconds(pos)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
								.toMinutes(pos)));

		try {
			small.updateIndicators(cur, progress);
		} catch (Exception e) {
		}
		try {
			large.updateIndicators(cur, progress);
		} catch (Exception e) {
		}

		auc++;
		if (auc >= 60) {
			updateAlbumArt();
			auc = 0;
		}
	}

	private void updateAlbumArt() {
		getAlbumArt(getAlbumId());
		try {
			small.updateAlbumArt();
		} catch (Exception e) {
		}
		try {
			large.updateAlbumArt();
		} catch (Exception e) {
		}
		try {
			compact.updateAlbumArt();
		} catch (Exception e) {
		}
		try {
			medium.updateAlbumArt();
		} catch (Exception e) {
		}
	}

	@Override
	public void onDestroy() {
		if (registered == true) {
			unregisterReceiver(mMediaPlaybackListener);
			unbindFromService(this);
			registered = false;
			mIsRegistered = false;
		}
		// ystem.out.println("onDestroy");
		if (timerStarted) {
			timerStarted = false;
			myTimer.cancel();
			myTimer.purge();
		}
		if (destroy != true) {
			final Context context = this;
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					startService(new Intent(context, WidgetService.class));
				}
			}, 0);
		}
		super.onDestroy();
	}

	private Intent getStartIntent(Context paramContext) {
		Intent localIntent = new Intent();
		try {
			paramContext.startService(new Intent(
					"com.sonyericsson.music.SERVICE"));
			localIntent.setAction("com.sonyericsson.music.SERVICE");
			return localIntent;
		} catch (RuntimeException localRuntimeException) {
			Log.e("SemcMusicWidget", "Problem starting the Music Service. "
					+ localRuntimeException.getMessage());
		}
		return localIntent;
	}

	public void bindToService(
			Context paramContext,
			MediaPlayerServiceConnectionListener paramMediaPlayerServiceConnectionListener) {
		synchronized (this.mConnectionLock) {
			this.mListener = paramMediaPlayerServiceConnectionListener;
			if (!this.mServiceBindSuccess)
				this.mServiceBindSuccess = paramContext.bindService(
						getStartIntent(paramContext), this, 1);
			return;
		}
	}

	public void unbindFromService(Context paramContext) {
		synchronized (this.mConnectionLock) {
			if (this.mServiceBindSuccess) {
				paramContext.unbindService(this);
				this.mServiceBindSuccess = false;
				this.mConnected = false;
			}
			this.mListener = null;
			this.mWorker.cancelAll();
			// //ystem.out.println("unbinded");
			return;
		}
	}

	private void registerBroadcastReceivers() {
		if (!this.mIsRegistered) {
			this.mIsRegistered = true;
			IntentFilter localIntentFilter1 = new IntentFilter();
			localIntentFilter1.addAction("com.voiche.musicwidget.DESTROY");
			localIntentFilter1
					.addAction("com.sonyericsson.music.playbackcontrol.ACTION_TRACK_STARTED");
			localIntentFilter1
					.addAction("com.sonyericsson.music.playbackcontrol.ACTION_PAUSED");
			localIntentFilter1
					.addAction("com.sonyericsson.music.TRACK_TO_BE_PREPARED");
			localIntentFilter1
					.addAction("com.sonyericsson.music.TRACK_PREPARED");
			localIntentFilter1
					.addAction("com.sonyericsson.music.PLAYBACK_ERROR");
			localIntentFilter1.addAction("com.sonyericsson.music.SERVICE");
			localIntentFilter1.addAction("com.voiche.musicwidget.PLAYPAUSE");
			localIntentFilter1.addAction("com.voiche.musicwidget.PREV");
			localIntentFilter1.addAction("com.voiche.musicwidget.NEXT");
			localIntentFilter1.addAction("com.voiche.musicwidget.OPENPLAYER");
			localIntentFilter1.addAction("com.voiche.musicwidget.OPENSETTINGS");
			localIntentFilter1
					.addAction("com.voiche.musicwidget.OPENSETTINGS_4x3");
			localIntentFilter1
					.addAction("com.voiche.musicwidget.OPENSETTINGS_4x4");
			localIntentFilter1
					.addAction("com.voiche.musicwidget.OPENSETTINGS_4x2");
			localIntentFilter1
					.addAction("com.voiche.musicwidget.OPENSETTINGS_simple");
			localIntentFilter1.addAction("com.voiche.musicwidget.UPDATE");
			localIntentFilter1.addAction("com.voiche.musicwidget.OPENLE");
			localIntentFilter1.addAction(Intent.ACTION_SCREEN_ON);
			localIntentFilter1.addAction(Intent.ACTION_SCREEN_OFF);
			localIntentFilter1.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
			registerReceiver(mMediaPlaybackListener, localIntentFilter1);
		}
	}

	private final BroadcastReceiver mMediaPlaybackListener = new BroadcastReceiver() {
		public void onReceive(Context context, Intent paramAnonymousIntent) {
			String str = paramAnonymousIntent.getAction();
			// ystem.out.println(str);
			if (mConnected != true)
				getConnection();
			if (str.equals("com.voiche.musicwidget.DESTROY")) {
				destroy = true;
				stopSelf();
			} else if (str.equals("android.intent.action.SCREEN_OFF")) {
				screenState = false;
			} else if (str.equals("android.intent.action.SCREEN_ON")) {
				screenState = true;
			} else if (str.equals("com.voiche.musicwidget.UPDATE")) {
				small.create(context);
				large.create(context);
				compact.create(context);
				medium.create(context);
				simple.create(context);
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						duration = -2;
						updateUI();
					}
				}, 1000);
			} else if (str
					.equals("android.intent.action.CONFIGURATION_CHANGED")) {
				small.create(context);
				large.create(context);
				compact.create(context);
				medium.create(context);
				simple.create(context);
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						duration = -2;
						updateUI();
					}
				}, 1000);
			} else if (str.equals("com.voiche.musicwidget.OPENPLAYER")) {
				if (isAppInstalled(context, "com.sonyericsson.music")) {
					Intent intent2 = new Intent(Intent.ACTION_MAIN);
					intent2.setComponent(new ComponentName(
							"com.sonyericsson.music",
							"com.sonyericsson.music.PlayerActivity"));
					intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					try {
						context.startActivity(intent2);
					} catch (RuntimeException e) {
					}
				} else
					Toast.makeText(
							context,
							"In order to use this widget, you should have WALKMAN music player intalled.",
							Toast.LENGTH_LONG).show();
			} else if (str.equals("com.voiche.musicwidget.OPENLE")) {
				if (isAppInstalled(context, "com.voiche.lyricsaddon")) {
					Intent intent3 = new Intent(Intent.ACTION_MAIN);
					intent3.setComponent(new ComponentName(
							"com.voiche.lyricsaddon",
							"com.voiche.lyricsaddon.MainActivity"));
					context.startActivity(intent3
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				} else {
					startActivity(new Intent(
							Intent.ACTION_VIEW,
							Uri.parse("market://details?id=com.voiche.lyricsaddon"))
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					Toast.makeText(
							context,
							"Lyrics Music Extension not installed. \nPlease install Lyrics Music Extension from Play Store.",
							Toast.LENGTH_LONG).show();
				}
			} else if (str.equals("com.voiche.musicwidget.OPENSETTINGS")) {
				dtap++;
				SharedPreferences shared = getSharedPreferences("settings",
						MODE_PRIVATE);
				if (dtap >= 2)
					contxt.startActivity(new Intent(contxt, Configure.class)
							.setAction(
									"android.appwidget.action.APPWIDGET_CONFIGURE")
							.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
									shared.getInt("awID", 0))
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						if (dtap == 100)
							sendBroadcast(new Intent()
									.setAction("com.voiche.musicwidget.OPENPLAYER"));
						dtap = 0;
					}
				}, 250);
			} else if (str.equals("com.voiche.musicwidget.OPENSETTINGS_4x3")) {
				dtap++;
				SharedPreferences shared = getSharedPreferences("settings_4x3",
						MODE_PRIVATE);
				if (dtap >= 2)
					contxt.startActivity(new Intent(contxt, Configure_4x3.class)
							.setAction(
									"android.appwidget.action.APPWIDGET_CONFIGURE")
							.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
									shared.getInt("awID", 0))
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						if (dtap == 100)
							sendBroadcast(new Intent()
									.setAction("com.voiche.musicwidget.OPENPLAYER"));
						dtap = 0;
					}
				}, 250);
			} else if (str.equals("com.voiche.musicwidget.OPENSETTINGS_4x4")) {
				dtap++;
				SharedPreferences shared = getSharedPreferences("settings_4x4",
						MODE_PRIVATE);
				if (dtap >= 2)
					contxt.startActivity(new Intent(contxt, Configure_4x4.class)
							.setAction(
									"android.appwidget.action.APPWIDGET_CONFIGURE")
							.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
									shared.getInt("awID", 0))
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						if (dtap == 1)
							sendBroadcast(new Intent()
									.setAction("com.voiche.musicwidget.OPENPLAYER"));
						dtap = 0;
					}
				}, 250);
			} else if (str.equals("com.voiche.musicwidget.OPENSETTINGS_4x2")) {
				dtap++;
				SharedPreferences shared = getSharedPreferences("settings_4x2",
						MODE_PRIVATE);
				if (dtap >= 2)
					contxt.startActivity(new Intent(contxt, Configure_4x2.class)
							.setAction(
									"android.appwidget.action.APPWIDGET_CONFIGURE")
							.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
									shared.getInt("awID", 0))
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						if (dtap == 100)
							sendBroadcast(new Intent()
									.setAction("com.voiche.musicwidget.OPENPLAYER"));
						dtap = 0;
					}
				}, 250);
			} else if (str.equals("com.voiche.musicwidget.OPENSETTINGS_simple")) {
				dtap++;
				SharedPreferences shared = getSharedPreferences(
						"settings_simple", MODE_PRIVATE);
				if (dtap >= 2)
					contxt.startActivity(new Intent(contxt,
							Configure_simple.class)
							.setAction(
									"android.appwidget.action.APPWIDGET_CONFIGURE")
							.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
									shared.getInt("awID", 0))
							.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						if (dtap == 100)
							sendBroadcast(new Intent()
									.setAction("com.voiche.musicwidget.OPENPLAYER"));
						dtap = 0;
					}
				}, 250);
			}
			if (mConnected != true
					|| str.equals("com.sonyericsson.music.TRACK_TO_BE_PREPARED")) {
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					public void run() {
						updateUI();
					}
				}, 250);
				return;
			} else if (str.equals("com.voiche.musicwidget.PLAYPAUSE")) {
				if (!pressable)
					return;
				pressCount++;
				if (pressCount >= 2)
					pressable = false;
				try {
					if (mSemcService.isPlaying())
						mSemcService.pause();
					else
						mSemcService.play();
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						public void run() {
							pressCount = 0;
							pressable = true;
						}
					}, 500);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
				// //ystem.out.println("PlayPause");
			} else if (str.equals("com.voiche.musicwidget.PREV")) {
				if (!pressable)
					return;
				pressCount++;
				if (pressCount > 1)
					pressable = false;
				try {
					mSemcService.prev();
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						public void run() {
							pressCount = 0;
							pressable = true;
						}
					}, 500);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
				updater();
				// //ystem.out.println("Prev");
			} else if (str.equals("com.voiche.musicwidget.NEXT")) {
				if (!pressable)
					return;
				pressCount++;
				if (pressCount >= 2)
					pressable = false;
				try {
					mSemcService.next();
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						public void run() {
							pressCount = 0;
							pressable = true;
						}
					}, 500);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
				updater();
				// //ystem.out.println("Next");
			} else if (str
					.equals("com.sonyericsson.music.playbackcontrol.ACTION_TRACK_STARTED")) {
				// playpausebutton.setBackgroundResource(R.drawable.button_pause);
				// fetchTrackData(paramAnonymousIntent);
			} else if (str
					.equals("com.sonyericsson.music.playbackcontrol.ACTION_PAUSED")) {
				// playpausebutton.setBackgroundResource(R.drawable.button_play);
				// fetchTrackData(paramAnonymousIntent);
			} else if (str.equals("com.sonyericsson.music.TRACK_PREPARED")) {
				// fetchTrackData(paramAnonymousIntent);
			}
			updateUI();
		}
	};

	private boolean isAppInstalled(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		boolean exists = false;
		try {
			pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			exists = true;
		} catch (PackageManager.NameNotFoundException e) {
			exists = false;
		}
		return exists;
	}

	private void updateUI() {
		try {
			if (duration != getDuration() && mConnected) {
				duration = getDuration();
				String total = String.format(
						"%02d:%02d",
						TimeUnit.MILLISECONDS.toMinutes(duration),
						TimeUnit.MILLISECONDS.toSeconds(duration)
								- TimeUnit.MINUTES
										.toSeconds(TimeUnit.MILLISECONDS
												.toMinutes(duration)));
				String title = getTrackName();
				String artist = getArtistName();
				String album = getAlbumName();
				if (artist.contains("<unknown"))
					artist = "Unknown artist";
				if (title.contains("<unknown"))
					title = "Unknown title";
				if (album.contains("<unknown"))
					album = "Unknown album";
				getAlbumArt(getAlbumId());
				try {
					small.updateInfo(title, artist, album, total);
				} catch (Exception e) {
				}
				try {
					large.updateInfo(title, artist, album, total);
				} catch (Exception e) {
				}
				try {
					compact.updateInfo(title, artist, album, total);
				} catch (Exception e) {
				}
				try {
					medium.updateInfo(title, artist, album, total);
				} catch (Exception e) {
				}
				updateAlbumArt();
				updater();
			}
			if (mConnected == true) {
				boolean playing = mSemcService.isPlaying();
				try {
					small.updatePlayButton(playing);
				} catch (Exception e) {
				}
				try {
					large.updatePlayButton(playing);
				} catch (Exception e) {
				}
				try {
					compact.updatePlayButton(playing);
				} catch (Exception e) {
				}
				try {
					medium.updatePlayButton(playing);
				} catch (Exception e) {
				}
				try {
					simple.updatePlayButton(playing);
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private long getPosition() {
		if (mConnected == true)
			try {
				return mSemcService.getPosition();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		return 0;
	}

	private long getDuration() {
		if (mConnected == true)
			try {
				return mSemcService.getDuration();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return -1;
	}

	private void getAlbumArt(long albumId) {
		// ystem.out.println("albumID" + albumId);
		Bitmap bitmap = null;
		def = BitmapFactory.decodeResource(getResources(),
				R.drawable.default_album);
		if (albumId != -1) {
			Uri sArtworkUri = Uri
					.parse("content://media/external/audio/albumart");
			try {
				Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri,
						albumId);
				try {
					bitmap = MediaStore.Images.Media.getBitmap(getBaseContext()
							.getContentResolver(), albumArtUri);
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (bitmap != null)
					def = bitmap;
			} catch (RuntimeException e) {

			}
		}

		try {
			FileOutputStream out = new FileOutputStream(f);
			def.compress(Bitmap.CompressFormat.JPEG, 100, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		def.recycle();
		// bitmap.recycle();
	}

	@Override
	public void onServiceConnected(ComponentName paramComponentName,
			IBinder paramIBinder) {
		// TODO Auto-generated method stub
		synchronized (this.mConnectionLock) {
			this.mSemcService = IPlayback.Stub.asInterface(paramIBinder);
			if (this.mSemcService != null)
				this.mConnected = true;
			if (this.mListener != null)
				this.mListener.onNotifyConnected(true);
			// //ystem.out.println("connected");
			timer();
			duration = -1;
			updateUI();
			return;
		}
	}

	public String getTrackName() {
		if (mConnected == true) {
			try {
				return mSemcService.getTrackName();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String getArtistName() {
		if (mConnected == true) {
			try {
				return mSemcService.getArtistName();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String getAlbumName() {
		if (mConnected == true) {
			try {
				return mSemcService.getAlbumName();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public long getAlbumId() {
		if (mConnected == true)
			try {
				return mSemcService.getAlbumId();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return -1;
	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		// TODO Auto-generated method stub
		synchronized (this.mConnectionLock) {
			this.mConnected = false;
			if (this.mListener != null)
				this.mListener.onNotifyConnected(false);
			// //ystem.out.println("disconnected");
			return;
		}
	}

	@Override
	public void onAlbumIdUpdated(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onArtistNameUpdated(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDurationUpdated(long arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNotifyConnected(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayingStateUpdated(boolean arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPositionUpdated(long arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTrackInfoUpdated(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTrackNameUpdated(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTrackUriUpdated(Uri arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void PlayPause() {
		// TODO Auto-generated method stub
		// ystem.out.println("PlayPause()"+mConnected);
		if (mConnected == true) {
			try {
				if (mSemcService.isPlaying())
					mSemcService.pause();
				else
					mSemcService.play();
				// ystem.out.println(mSemcService.isPlaying());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
		// ystem.out.println("onStartCommand");
	}

}