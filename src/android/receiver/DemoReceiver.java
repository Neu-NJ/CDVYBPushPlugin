package receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import io.yunba.android.manager.YunBaManager;
import util.DemoUtil;


public class DemoReceiver extends BroadcastReceiver {
	private final static String REPORT_MSG_SHOW_NOTIFICARION = "1000";
	private final static String REPORT_MSG_SHOW_NOTIFICARION_FAILED = "1001";
    private final static String TAG="YUNBA";
	@Override
	public void onReceive(Context context, Intent intent) {


		if (YunBaManager.MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
			String status = "Yunba - Connected";
			String topicORalias = intent.getStringExtra(YunBaManager.MQTT_TOPIC);
			String message = intent.getStringExtra(YunBaManager.MQTT_MSG);
			boolean flag = DemoUtil.showNotification(context, topicORalias, message);
			//上报显示通知栏状态， 以方便后台统计
			if (flag)
				YunBaManager.report(context, REPORT_MSG_SHOW_NOTIFICARION, topicORalias);
			else
				YunBaManager.report(context, REPORT_MSG_SHOW_NOTIFICARION_FAILED, topicORalias);

			StringBuilder msgsb = new StringBuilder();
			msgsb.append("receive message from server: ")
					.append("topic/alias").append(" = ").append(topicORalias).append(" ,")
					.append(YunBaManager.MQTT_MSG).append(" = ").append(message);

		} else if (YunBaManager.PRESENCE_RECEIVED_ACTION.equals(intent.getAction())) {
			String status = "Yunba - Connected";
			String topic = intent.getStringExtra(YunBaManager.MQTT_TOPIC);
			String message = intent.getStringExtra(YunBaManager.MQTT_MSG);
			StringBuilder msgsb = new StringBuilder();
			msgsb.append("receive message from presence: ")
					.append(YunBaManager.MQTT_TOPIC).append(" = ").append(topic).append(" ,")
					.append(YunBaManager.MQTT_MSG).append(" = ").append(message);

		} else if (YunBaManager.MESSAGE_CONNECTED_ACTION.equals(intent.getAction())) {
			String status = "Yunba - Connected";

		} else if (YunBaManager.MESSAGE_DISCONNECTED_ACTION.equals(intent.getAction())) {
			String status = "Yunba - DisConnected";
		} else if (YunBaManager.MESSAGE_HMS_CONNECTED_ACTION.equals(intent.getAction())) {
			Log.d(TAG,"华为连接成功");
		} else if (YunBaManager.MESSAGE_HMS_CONNECTION_FAILED_ACTION.equals(intent.getAction())) {
			Log.d(TAG,"华为连接失败");
		}
	}

}
