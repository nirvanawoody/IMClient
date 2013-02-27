package com.wudi.imclient;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.filetransfer.FileTransfer.Status;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity {

	private MediaRecorder recorder;
	private MediaPlayer player;
	private static final String DIR = Environment.getExternalStorageDirectory()
			+ "/IMClient";
	ListView listView;
	String toUserId;
	String toUserName;
	XMPPConnection con;
	ArrayList<Msg> msgList;
	EditText inputMsg;
	Button send, sendVoice;
	ChatManager cm;
	MyAdapter adapter;
	String userName;
	String filePath;
	FileTransferManager transferManager;
	SQLiteDatabase db;
	IMData imData;
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
//		Log.i("info", "onDestroy");
//		listView = null;
//		cm.removeChatListener(cmListener);
//		IMData.CHATLISTEN=false;
//		cm = null;
//		super.onDestroy();
//		System.gc();
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		cm.removeChatListener(cmListener);
		cm=null;
		IMData.CHATLISTEN=false;
		Intent intent=new Intent(this,IMService.class);
		startService(intent);
		System.gc();
		super.onPause();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		if(IMData.SERVICELISTEN){
			stopService(new Intent(this,IMService.class));
		}
		cm=con.getChatManager();
		cm.addChatListener(cmListener);
		super.onRestart();
		
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		Log.i("info", "onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.i("info", "onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
	}

	private ChatManagerListener cmListener = new ChatManagerListener() {

		@Override
		public void chatCreated(Chat chat, boolean createdLocally) {
			// TODO Auto-generated method stub
			chat.addMessageListener(new MessageListener() {

				@Override
				public void processMessage(Chat chat, Message message) {
					Log.i("info",
							"收到消息:" + message.getFrom() + ","
									+ message.getBody());
					Log.i("info", "toUserId:" + toUserId);
					if (message.getFrom().contains(toUserId)) {
						Msg msg = new Msg(toUserName, message.getBody(), System
								.currentTimeMillis(), Msg.IN, Msg.TEXT, null);
						android.os.Message m = new android.os.Message();
						m.what = 1;
						m.obj = msg;
						handler.sendMessage(m);
					}
				}
			});
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.i("info", "onCreate");
		setContentView(R.layout.chat);
		Intent intent = getIntent();
		toUserId = intent.getStringExtra("toUserId");
		toUserName = getNickName(toUserId);
		inputMsg = (EditText) findViewById(R.id.inputMsg);
		send = (Button) findViewById(R.id.send);
		sendVoice = (Button) findViewById(R.id.sendVoice);
		imData = (IMData) this.getApplication();
		userName = imData.getUserName();
		db = imData.getDb();
		// msgList=imData.getMsgList();
		msgList = new ArrayList<Msg>();
		Cursor cursor = db.query(userName, null, "toUserName=?",
				new String[] { toUserName }, null, null, "date asc");
		if (cursor != null) {
			while (cursor.moveToNext()) {
				String userId = null;
				String direction = cursor.getString(cursor
						.getColumnIndexOrThrow("_from"));
				if (direction.equals(Msg.IN)) {
					userId = toUserName;
				} else {
					userId = userName;
				}
				String content = cursor.getString(cursor
						.getColumnIndexOrThrow("content"));
				String type = cursor.getString(cursor
						.getColumnIndexOrThrow("type"));
				long date = cursor
						.getLong(cursor.getColumnIndexOrThrow("date"));
				String filepath = cursor.getString(cursor
						.getColumnIndex("filePath"));
				Msg m = new Msg(userId, content, date, direction, type,
						filepath);
				msgList.add(m);
			}
			cursor.close();
		}
		listView = (ListView) findViewById(R.id.listView);
		listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		adapter = new MyAdapter(this);
		listView.setAdapter(adapter);
		con = imData.getConnection();
		cm = con.getChatManager();
		transferManager = new FileTransferManager(con);
		transferManager.addFileTransferListener(new RecFileTransferListener());
		cm.addChatListener(cmListener);
		IMData.CHATLISTEN=true;
		if(IMData.SERVICELISTEN){
			stopService(new Intent(this,IMService.class));
		}
		final Chat newChat = cm.createChat(toUserId, null);
		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String sendMessage = inputMsg.getText().toString();
				if (sendMessage.length() > 0) {
					try {
						Msg msg = new Msg(userName, sendMessage, System
								.currentTimeMillis(), Msg.OUT, Msg.TEXT, null);
						newChat.sendMessage(sendMessage);
						android.os.Message m = new android.os.Message();
						m.what = 1;
						m.obj = msg;
						handler.sendMessage(m);
					} catch (XMPPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					inputMsg.setText("");
				}
			}
		});
		sendVoice.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		sendVoice.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					try {
						File dir = new File(DIR);
						if (!dir.exists()) {
							dir.mkdir();
						}
						filePath = DIR + "/" + userName
								+ System.currentTimeMillis() + ".amr";
						recorder = new MediaRecorder();
						recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
						recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
						recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
						recorder.setOutputFile(filePath);
						recorder.prepare();
						recorder.start();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case MotionEvent.ACTION_UP:
					recorder.stop();
					recorder.reset();
					recorder.release();
					new Thread() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							sendFile(filePath);
						}

					}.start();
					break;
				default:
					break;
				}
				return true;
			}
		});
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				Msg msg = msgList.get(position);
				if (msg.type.equals(Msg.VOICE)) {
					String path = msg.filePath;
					player = new MediaPlayer();
					try {
						player.reset();
						player.setDataSource(path);
						player.prepare();
						player.start();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

	private void insertMsgToDB(Msg m) {
		ContentValues values = new ContentValues();
		values.put("_from", m.from);
		values.put("toUserName", toUserName);
		values.put("content", m.content);
		values.put("type", m.type);
		values.put("date", m.date);
		values.put("filePath", m.filePath);
		db.insert(userName, null, values);
	}

	private void sendFile(String filePath) {
		File file = new File(filePath);
		OutgoingFileTransfer transfer = transferManager
				.createOutgoingFileTransfer(toUserId + "/Spark 2.6.3");
		try {
			transfer.sendFile(file, file.getName());
			while (!transfer.isDone()) {
				if (transfer.getStatus().equals(Status.error)) {
					Log.e("error", "ERROR!!! " + transfer.getError());
				}
			}
			if (transfer.isDone()) {
				Log.e("error", "stat:" + transfer.getStatus());
				if (!transfer.getStatus().equals(Status.complete)) {
					Toast.makeText(this, "Sending failed,try again later",
							Toast.LENGTH_LONG).show();
				}
				android.os.Message msg = new android.os.Message();
				msg.what = 1;
				msg.obj = new Msg(getNickName(con.getUser()), "",
						System.currentTimeMillis(), Msg.OUT, Msg.VOICE,
						file.getAbsolutePath());
				handler.sendMessage(msg);
			}
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

	public class RecFileTransferListener implements FileTransferListener {

		@Override
		public void fileTransferRequest(FileTransferRequest request) {
			// TODO Auto-generated method stub
			final IncomingFileTransfer inTransfer = request.accept();
			final String fileName = request.getFileName();
			final File file = new File(DIR + "/" + fileName);
			Log.e("error", "文件：" + fileName + "，大小:" + request.getFileSize()
					+ ",TYpe:" + request.getMimeType());

			new Thread() {

				@Override
				public void run() {
					try {
						inTransfer.recieveFile(file);

						// 能说话版

						while (!inTransfer.isDone()) {
							if (inTransfer.getStatus().equals(Status.error)) {
								Log.e("error",
										"ERROR!!! " + inTransfer.getError());
							}
						}
						if (inTransfer.isDone()) {
							Log.e("error", "stat:" + inTransfer.getStatus());
							if (inTransfer.getStatus().equals(Status.complete)) {
								android.os.Message msg = new android.os.Message();
								msg.what = 1;
								msg.obj = new Msg(toUserId, "",
										System.currentTimeMillis(), Msg.IN,
										Msg.VOICE, file.getAbsolutePath());
								handler.sendMessage(msg);
							}
						}
						// 不能说话版

						// int i=0;
						// while
						// (!inTransfer.getStatus().equals(Status.complete)) {
						// if (inTransfer.getStatus().equals(Status.error)) {
						// Log.e("error",
						// "ERROR!!! " + inTransfer.getError());
						// inTransfer.cancel();
						// inTransfer.recieveFile(file);
						// i++;
						// } else {
						// Log.i("info", "status:" + inTransfer.getStatus());
						// Log.i("info", "process:" + inTransfer.getProgress());
						// }
						// }
						// if (inTransfer.isDone()) {
						// Log.i("info",
						// "stat:"+inTransfer.getStatus()+"count:"+i);
						// android.os.Message msg = new android.os.Message();
						// msg.what = 1;
						// msg.obj = new Msg(toUserId, "", getDate(), Msg.IN,
						// Msg.VOICE, file.getAbsolutePath());
						// handler.sendMessage(msg);
						// }
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		}

	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 1:
				Msg m = (Msg) msg.obj;
				msgList.add(m);
				insertMsgToDB(m);
				adapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
		}

	};

	private String getDate(long current) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
		return simpleDateFormat.format(new Date(current));
	}

	private String getNickName(String userId) {

		String nickName;
		String[] str = userId.split("@");
		if (str.length == 2) {
			nickName = str[0];
		} else {
			nickName = userId;
		}
		return nickName;

	}

	public class MyAdapter extends BaseAdapter {

		LayoutInflater inflater;
		Context context;

		public MyAdapter(Context context) {
			this.context = context;
			inflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return msgList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return msgList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			Msg msg = msgList.get(position);
			if (msg.from.equals(Msg.IN)) {
				if (msg.type.equals(Msg.TEXT)) {
					convertView = inflater.inflate(R.layout.chat_in, null);
				} else {
					convertView = inflater.inflate(R.layout.video_in, null);
				}
			} else {
				if (msg.type.equals(Msg.TEXT)) {
					convertView = inflater.inflate(R.layout.chat_out, null);
				} else {
					convertView = inflater.inflate(R.layout.video_out, null);
				}
			}
			TextView userId = (TextView) convertView.findViewById(R.id.userId);
			TextView nowdate = (TextView) convertView
					.findViewById(R.id.nowdate);
			TextView msg_content = (TextView) convertView
					.findViewById(R.id.msg_content);
			userId.setText(msg.userId);
			nowdate.setText(getDate(msg.date));
			msg_content.setText(msg.content);
			return convertView;
		}

	}

}
