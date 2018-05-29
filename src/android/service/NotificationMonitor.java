package service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class NotificationMonitor extends NotificationListenerService {
  @Override
  public void onCreate() {
    super.onCreate();
    Log.i("NotificationMonitor", "onCreate: 收到的信息服务创建开启");
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return super.onBind(intent);
  }

  @Override
  public void onNotificationPosted(StatusBarNotification sbn) {
    Log.i("NotificationMonitor", "显示信息的标题: "+sbn.getNotification().extras.getString(Notification.EXTRA_TITLE));
    Log.i("NotificationMonitor", "获取包名: "+this.getPackageName());
    Log.i("NotificationMonitor", "三方推送的标题: "+getMetaData(this,"THIRD_TITLE"));
    if(isAppRunning(this,this.getPackageName()) && sbn.getNotification().extras.getString(Notification.EXTRA_TITLE).equals(getMetaData(this,"THIRD_TITLE"))){
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cancelNotification(sbn.getKey());
          }else {
            cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
          }
    }
  }



  @Override
  public void onNotificationRemoved(StatusBarNotification sbn) {

  }

  /**
   * 方法描述：判断某一应用是否正在运行
   *
   * @param context     上下文
   * @param packageName 应用的包名
   * @return true 表示正在运行，false表示没有运行
   */
  public static boolean isAppRunning(Context context, String packageName) {
    boolean isAppRunning = false;
    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(999);
    if (list.size() <= 0) {
      return false;
    }
    for (ActivityManager.RunningTaskInfo info : list) {
      if (info.baseActivity.getPackageName().equals(packageName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 判断服务是否开启
   *
   * @return
   */
  public static boolean isServiceRunning(Context context, String ServiceName) {
    if (("").equals(ServiceName) || ServiceName == null)
      return false;
    ActivityManager myManager = (ActivityManager) context
      .getSystemService(Context.ACTIVITY_SERVICE);
    ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
      .getRunningServices(99999);
    for (int i = 0; i < runningService.size(); i++) {
      if (runningService.get(i).service.getClassName().toString()
        .equals(ServiceName)) {
        return true;
      }
    }
    return false;
  }

  private String getMetaData(Context context, String key)
  {
    ApplicationInfo appInfo;
    try
    {
      appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
      String value = appInfo.metaData.get(key).toString();
      return value;
    }
    catch (PackageManager.NameNotFoundException e)
    {
      e.printStackTrace();
      return null;
    }
  }
}
