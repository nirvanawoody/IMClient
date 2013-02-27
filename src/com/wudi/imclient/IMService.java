package com.wudi.imclient;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class IMService extends Service {
	
	ChatManager cm;
	XMPPConnection con;
	private ChatManagerListener listener=new ChatManagerListener() {
		
		@Override
		public void chatCreated(Chat chat, boolean createdLocally) {
			// TODO Auto-generated method stub
			chat.addMessageListener(new MessageListener() {
				
				@Override
				public void processMessage(Chat chat, Message message) {
					// TODO Auto-generated method stub
					Log.e("error", "Service ---From:"+message.getFrom()+",body:"+message.getBody());
				}
			});
		}
	};
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		IMData imdata=(IMData) getApplication();
		con=imdata.getConnection();
		cm=con.getChatManager();
		cm.addChatListener(listener);
		IMData.SERVICELISTEN=true;
		Log.e("error", "Service onCreate");
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		cm.removeChatListener(listener);
		cm=null;
		System.gc();
		super.onDestroy();
		Log.e("error","Service onDestory");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		if(IMData.CHATLISTEN||IMData.ROSTERLISTEN){
			stopSelf();
		}
		Log.e("error","Service onStart");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.e("error","Service onStartCommand");
		return super.onStartCommand(intent, flags, startId);
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.e("error","Service onBind");
		return null;
	}
	
	

}
