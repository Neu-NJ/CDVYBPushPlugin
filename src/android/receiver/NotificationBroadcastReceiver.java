package receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import CDVYBPushPlugin.CDVYBPushPlugin;
import cn.neusoft.mdp.financial.MainActivity;
import cn.neusoft.mdp.financial.MainActivity;
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
    String action = intent.getAction();
    int type = intent.getIntExtra(TYPE, -1);
    message=intent.getStringExtra("message");
    if (type != -1) {
      NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      notificationManager.cancel(type);
    }
    if (action.equals("notification_clicked")) {
      //处理点击事件
      Log.d("YUNBA","点击了通知");
      if (message==null){
      }else {
        yunBaIntent.setMessage(message);
        Log.d("YUNBA",yunBaIntent.getMessage());
        CDVYBPushPlugin.changes.firePropertyChange("message", "FIRST_ACCOUNT", yunBaIntent.getMessage());
      }
      Intent intent1=new Intent(context, MainActivity.class);
      intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent1);
    }
    if (action.equals("notification_cancelled")) {
      //处理滑动清除和点击删除事件
    }
  }
}
