package util;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Random;
import receiver.NotificationBroadcastReceiver;
import util.FakeR;
import io.yunba.android.manager.YunBaManager;


public class DemoUtil {
  /*定义不同类型的消息，只是为了在APP的“屏幕”上显示不同的颜色加以区分
    * 下面的消息类型均是按发送数据包的类型来划分，ACK对应包的应答*/
  public final static int CONNECT = 0;
  public final static int DISCONNET = 1;
  public final static int SUBSCRIBE = 2;
  public final static int SUBSCRIBEACk = 3;
  public final static int PUBLISH = 4;
  public final static int PUBLISHACK = 5;
  public final static int EXPAND = 6;
  public final static int EXPANDACK = 7;
  //特殊的类型，仅记录用户的操作并以日志在屏幕上输出
  public final static int USERLOG = 8;

  public final static String CONNECT_STATUS = "connect_status";
  private static FakeR fakeR;

  public static boolean isEmpty(String s) {
    if (s == null)
      return true;
    if (s.length() == 0)
      return true;
    if (s.trim().length() == 0)
      return true;
    return false;
  }

  //判断应用是否还活着，是否在前台
  public static boolean isAppOnForeground(Context context) {
    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    // Returns a list of application processes that are running on the device
    List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
    if (appProcesses == null)
      return false;
    for (RunningAppProcessInfo appProcess : appProcesses) {
      /* importance:
			 The relative importance level that the system places on this process.
			 May be one of IMPORTANCE_FOREGROUND,IMPORTANCE_BACKGROUND,
			 IMPORTANCE_VISIBLE,IMPORTANCE_SERVICE, or IMPORTANCE_EMPTY.
			 These constants are numbered so that "more important" values
			 are always smaller than "less important" values.
			 processName:
			 The name of the process that this object is associated with.*/
      if (appProcess.processName.equals(context.getPackageName())
        && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
        return true;
      }
    }
    return false;
  }

  public static boolean isNetworkConnected(Context context) {
    ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo info = conn.getActiveNetworkInfo();
    if (info != null && info.isConnected()) {
      if (isNetworkAvailable())
        return true;
      else {
        showToast("Network is not available ", context);
        return false;
      }
    } else {
      showToast("Network is not connected ", context);
      return false;
    }
  }

  private static boolean isNetworkAvailable() {
    Runtime runtime = Runtime.getRuntime();
    try {
      Process ipProcess = runtime.exec("/system/bin/ping -c 1 www.baidu.com");
      int exitValue = ipProcess.waitFor();
      return (exitValue == 0);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return false;
  }


  public static boolean showNotification(Context context, String topic, String msg) {
    int type = 0;
    fakeR = new FakeR(context);
    try {
      Intent intentClick = new Intent(context, NotificationBroadcastReceiver.class);
      intentClick.setAction("notification_clicked");
      intentClick.putExtra(NotificationBroadcastReceiver.TYPE, type);
      intentClick.putExtra("message",msg);
      PendingIntent pendingIntentClick = PendingIntent.getBroadcast(context, 0, intentClick, PendingIntent.FLAG_ONE_SHOT);
      Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
      NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
        .setSmallIcon(fakeR.getId("mipmap", "icon"))
        .setContentTitle(topic)
        .setContentText(msg)
        .setSound(defaultSoundUri)
        .setContentIntent(pendingIntentClick);
      NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      notificationManager.notify(type, notificationBuilder.build());
    }catch (Exception e){
      return false;
    }
		/*try {
			Uri alarmSound = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			long[] pattern = { 500, 500, 500 };
			NotificationCompat.Builder mBuilder = new
			 NotificationCompat.Builder(context)
			.setSmallIcon(fakeR.getId("mipmap","icon"))
			.setContentTitle(topic)
			.setContentText(msg)
			.setSound(alarmSound)
			.setVibrate(pattern)
			.setAutoCancel(true);

			//要进入的app的activity
			Intent resultIntent = new Intent(context, NotificationBroadcastReceiver.class);
			if (!DemoUtil.isEmpty(topic))
				resultIntent.putExtra(YunBaManager.MQTT_TOPIC, topic);
			if (!DemoUtil.isEmpty(msg))
				resultIntent.putExtra(YunBaManager.MQTT_MSG, msg);
       resultIntent.putExtra("type",0);
       resultIntent.setAction("notification_clicked");
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
			//从通知页面按把back键回退到主页面
			stackBuilder.addParentStack(MainActivity.class);
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
					PendingIntent.FLAG_UPDATE_CURRENT);
			mBuilder.setContentIntent(resultPendingIntent);
			NotificationManager mNotificationManager = (NotificationManager)context.
					getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(new Random().nextInt(), mBuilder.build());

		} catch (Exception e) {
			return false;
		}*/
    return true;
  }

  public static void showToast(final String content, final Context context) {
    if (!isAppOnForeground(context))
      return;
    new Thread(new Runnable() {
      @Override
      public void run() {
        Looper.prepare();
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
        Looper.loop();
      }
    }).start();
  }

  public static String getAppKey(Context context) {
    Bundle metaData = null;
    String appKey = null;
    try {
      ApplicationInfo ai = context.getPackageManager()
        .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
      if (ai != null) {
        metaData = ai.metaData;
      }
      if (metaData != null) {
        appKey = metaData.getString("YUNBA_APPKEY");
        if ((appKey == null) || appKey.length() != 24) {
          appKey = "error";
        }
      }
    } catch (NameNotFoundException e) {
    }
    return appKey;
  }

  public static String getHWAppid(Context context) {
    Bundle metaData = null;
    String appKey = null;
    try {
      ApplicationInfo ai = context.getPackageManager()
        .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
      if (ai != null) {
        metaData = ai.metaData;
      }
      if (metaData != null) {
        appKey = String.valueOf(metaData.getInt("com.huawei.hms.client.appid"));
      }
    } catch (NameNotFoundException e) {
    }
    return appKey;
  }

  public static String getSecretKey(Context context) {
    Bundle metaData = null;
    String seckey = null;
    try {
      ApplicationInfo ai = context.getPackageManager()
        .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
      if (ai != null) {
        metaData = ai.metaData;
      }
      if (metaData != null) {
        seckey = metaData.getString("YUNBA_SECRETKEY");
        if (seckey == null) {
          seckey = "error";
        }
      }
    } catch (NameNotFoundException e) {
    }
    return seckey;
  }


  //将数组按指定分割符连接成字符串
  public static <T> String join(T[] array, String cement) {
    StringBuilder builder = new StringBuilder();
    if (array == null || array.length == 0) {
      return null;
    }
    for (T t : array) {
      builder.append(t).append(cement);
    }
    builder.delete(builder.length() - cement.length(), builder.length());
    return builder.toString();
  }

}
