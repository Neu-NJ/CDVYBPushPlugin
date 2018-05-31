package receiver;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;

import CDVYBPushPlugin.CDVYBPushPlugin;

import cn.neusoft.mdp.financial.MainActivity;
import service.NotificationMonitor;
import util.YunBaIntent;

/**
 * Created by neusoft on 2018/1/24.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver {
  String message;
  public static YunBaIntent yunBaIntent=new YunBaIntent();
  public static final String TYPE = "type"; //这个type是为了Notification更新信息的，这个不明白的朋友可以去搜搜，很多

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i("NotificationMonitor", "点击时通知栏监听服务: "+isServiceRunning(context,"service.NotificationMonitor"));
    String action = intent.getAction();
    int type = intent.getIntExtra(TYPE, -1);
    message=intent.getStringExtra("message");
    if (type != -1) {
      NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      notificationManager.cancel(type);
    }

    if (action.equals("notification_clicked")) {
      //处理点击事件
      if (message==null){
      }else {
        yunBaIntent.setMessage(message);
        CDVYBPushPlugin.changes.firePropertyChange("message", "FIRST_ACCOUNT", yunBaIntent.getMessage());
      }
      Intent intent1=new Intent(context, MainActivity.class);
      intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);// 关键的一步，设置启动模式，两种情况

      context.startActivity(intent1);
    }
    if (action.equals("notification_cancelled")) {
      //处理滑动清除和点击删除事件
    }
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
}
