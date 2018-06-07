package CDVYBPushPlugin;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import io.yunba.android.manager.YunBaManager;
import receiver.NotificationBroadcastReceiver;
import service.NotificationMonitor;
import util.DemoUtil;
import util.OSUtils;
import util.YunBaIntent;

/**
 * This class echoes a string called from JavaScript.
 */
public class CDVYBPushPlugin extends CordovaPlugin {
    private YunBaIntent yunBaIntent;
    public static PropertyChangeSupport changes;
    private boolean isEnabledNLS = false;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
      YunBaManager.setThirdPartyEnable(this.cordova.getActivity(), true);
      YunBaManager.setXMRegister(getMetaData(this.cordova.getActivity(),"XIAOMI_APPID"),getMetaData(this.cordova.getActivity(),"XIAOMI_APPKEY"));

      isEnabledNLS = isEnabled();
      if (!isEnabledNLS) {
        showConfirmDialog();
      }else{
        toggleNotificationListenerService();
      }

      YunBaManager.start(this.cordova.getActivity());
      if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }else if (action.equals("addSubscribe")){
            String message=args.getString(0);
            addSubscribe(message,callbackContext);
           return true;
        }else if (action.equals("topSkip")){
          Log.d("YUNBA","执行了");
          topSkip("",callbackContext);
          return true;
        }else if (action.equals("addAlias")){
          String message=args.getString(0);
          addAlias(message,callbackContext);
        }else if (action.equals("unSubscribe ")){
          String message=args.getString(0);
          unSubscribe(message,callbackContext);
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {

            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
  private String getMetaData(Activity activity1, String key)
  {
    ApplicationInfo appInfo;
    try
    {
      appInfo = activity1.getPackageManager().getApplicationInfo(activity1.getPackageName(), PackageManager.GET_META_DATA);
      String value = appInfo.metaData.get(key).toString();
      return value;
    }
    catch (PackageManager.NameNotFoundException e)
    {
      e.printStackTrace();
      return null;
    }
  }
  private void addSubscribe(String message,CallbackContext callbackContext){
    if (message != null && message.length() > 0) {
          /*ActivityInfo info= null;
          try {
            info = this.cordova.getActivity().getPackageManager()
              .getActivityInfo(this.cordova.getActivity().getComponentName(),
                PackageManager.GET_META_DATA);
            String xmAppid =info.metaData.getString("XIAOMI_APPID");
            String xmAppkey=info.metaData.getString("XIAOMI_APPKEY");
            YunBaManager.setXMRegister(xmAppid,xmAppkey);
          } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
          }*/

            try {

                YunBaManager.subscribe(cordova.getActivity(), message, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        StringBuilder str = new StringBuilder();
                        str.append("subscribe ").append(YunBaManager.MQTT_TOPIC)
                                .append(" = ").append(asyncActionToken.getTopics()).append(" succeed");
                        Log.d("YUNBA","succeed"+asyncActionToken.getTopics());
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        String str = "subscribe topic = " + asyncActionToken.getTopics() + " failed : " + exception.getMessage();
                        Log.d("YUNBA","fail"+exception.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            callbackContext.success("频道订阅成功");
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
//    点击跳转
    private void topSkip(final String message, final CallbackContext callbackContext){
      yunBaIntent= NotificationBroadcastReceiver.yunBaIntent;
      changes = new PropertyChangeSupport(yunBaIntent);
      changes.addPropertyChangeListener(new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
          // TODO Auto-generated method stub
          Log.d("点击跳转","change");
          Log.d("点击跳转",evt.getPropertyName());
          Log.d("点击跳转",evt.getOldValue()+"");
          Log.d("点击跳转",evt.getNewValue()+"");
            Log.d("点击跳转",callbackContext.getCallbackId());
          PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, evt.getNewValue()+"");
          pluginResult.setKeepCallback(true);
          callbackContext.sendPluginResult(pluginResult);
        }
      });

    }
//    设置别名
  private void addAlias(final String message, final CallbackContext callbackContext) {
    if (message != null && message.length() > 0) {
      YunBaManager.setAlias(this.cordova.getActivity(), message, new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg) {

        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg) {

        }
      });
      callbackContext.success("别名设置成功");
    }else {
      callbackContext.error("请输入别名");
    }
  }
//  取消订阅频道
  private void unSubscribe(final String message, final CallbackContext callbackContext){
    if (message != null && message.length() > 0) {
      YunBaManager.unsubscribe(this.cordova.getActivity(), message, new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken asyncActionToken) {

        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

        }
      });
      callbackContext.success("已取消订阅");
    }else {
      callbackContext.error("请输入要取消订阅的频道");
    }
  }

  private boolean isEnabled() {
    String pkgName = this.cordova.getActivity().getPackageName();
    final String flat = Settings.Secure.getString(this.cordova.getActivity().getContentResolver(),
      "enabled_notification_listeners");
    if (!TextUtils.isEmpty(flat)) {
      final String[] names = flat.split(":");
      for (int i = 0; i < names.length; i++) {
        final ComponentName cn = ComponentName.unflattenFromString(names[i]);
        if (cn != null) {
          if (TextUtils.equals(pkgName, cn.getPackageName())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private void showConfirmDialog() {
    new AlertDialog.Builder(cordova.getActivity())
      .setMessage("请打开通知栏权限")
      .setTitle("通知栏权限")
      .setIconAttribute(android.R.attr.alertDialogIcon)
      .setCancelable(true)
      .setPositiveButton("确认",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            cordova.getActivity().startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));

          }
        })
      .setNegativeButton("取消",
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            // do nothing
          }
        })
      .create().show();
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

  private void toggleNotificationListenerService() {
    PackageManager pm = this.cordova.getActivity().getPackageManager();
    pm.setComponentEnabledSetting(new ComponentName(this.cordova.getActivity(), service.NotificationMonitor.class),
      PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    pm.setComponentEnabledSetting(new ComponentName(this.cordova.getActivity(), service.NotificationMonitor.class),
      PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
  }
}
