package CDVYBPushPlugin;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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

import io.yunba.android.manager.YunBaManager;
import receiver.NotificationBroadcastReceiver;
import util.DemoUtil;
import util.OSUtils;
import util.YunBaIntent;

/**
 * This class echoes a string called from JavaScript.
 */
public class CDVYBPushPlugin extends CordovaPlugin {
    private YunBaIntent yunBaIntent;
    public static PropertyChangeSupport changes;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
      YunBaManager.setThirdPartyEnable(this.cordova.getActivity(), true);
      YunBaManager.setXMRegister(getMetaData(this.cordova.getActivity(),"XIAOMI_APPID"),getMetaData(this.cordova.getActivity(),"XIAOMI_APPKEY"));
      YunBaManager.start(cordova.getActivity());
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
          Log.d("YUNBA","change");
          Log.d("YUNBA",evt.getPropertyName());
          Log.d("YUNBA",evt.getOldValue()+"");
          Log.d("YUNBA",evt.getNewValue()+"");
            Log.d("YUNBA",callbackContext.getCallbackId());
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
}
