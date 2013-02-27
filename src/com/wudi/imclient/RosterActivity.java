package com.wudi.imclient;

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RosterActivity extends Activity {
	ExpandableListView expandableListView;
	ArrayList<String> groupList = new ArrayList<String>();
	ArrayList<ArrayList<RosterEntry>> children = new ArrayList<ArrayList<RosterEntry>>();
	XMPPConnection con;
	IMData imData;
	ChatManager cm;
	private ChatManagerListener listener=new ChatManagerListener() {
		
		@Override
		public void chatCreated(Chat chat, boolean createdLocally) {
			// TODO Auto-generated method stub
			chat.addMessageListener(new MessageListener() {
				
				@Override
				public void processMessage(Chat chat,
						org.jivesoftware.smack.packet.Message message) {
					// TODO Auto-generated method stub
					Log.e("error", "Roster---From:"+message.getFrom()+",body:"+message.getBody());
				}
			});
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rosterlist);
		expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
		imData = (IMData) this.getApplication();		
		con= imData.getConnection();
		
		Roster roster = con.getRoster();
		Collection<RosterGroup> groups = roster.getGroups();
		roster.addRosterListener(new RosterListener() {
			
			@Override
			public void presenceChanged(Presence presence) {
				// TODO Auto-generated method stub
				Log.i("info", presence.getFrom()+"的状态变为"+presence);
				Message msg=new Message();
				msg.what=0;
				msg.obj=presence;
				handler.sendMessage(msg);
			}
			
			@Override
			public void entriesUpdated(Collection<String> addresses) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void entriesDeleted(Collection<String> addresses) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void entriesAdded(Collection<String> addresses) {
				// TODO Auto-generated method stub
				
			}
		});
		for (RosterGroup group : groups) {
			String groupName = group.getName();
			Log.i("info", "组名："+groupName);
			if (!groupList.contains(groupName)) {
				groupList.add(groupName);
			}
			Collection<RosterEntry> entries = group.getEntries();
			ArrayList<RosterEntry> groupRosters = new ArrayList<RosterEntry>();
			for (RosterEntry entry : entries) {
				groupRosters.add(entry);
			}
			children.add(groupRosters);
		}
		expandableListView.setAdapter(new RosterListAdapter());
		expandableListView.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				String toUserId=children.get(groupPosition).get(childPosition).getUser();
				Intent intent=new Intent(RosterActivity.this, ChatActivity.class);
				intent.putExtra("toUserId", toUserId);
				startActivity(intent);
				return false;
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
//		con.disconnect();
		super.onDestroy();
	}
	

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		if(IMData.SERVICELISTEN){
			stopService(new Intent(this,IMService.class));
		}
		cm=con.getChatManager();
		cm.addChatListener(listener);
		IMData.ROSTERLISTEN=true;
		super.onStart();
		
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		cm.removeChatListener(listener);
		IMData.ROSTERLISTEN=false;
		cm=null;
		System.gc();
		Intent intent=new Intent(this, IMService.class);
		ComponentName serviceName=startService(intent);
		imData.setServiceName(serviceName);
	}


	private Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:
				Presence presence=(Presence) msg.obj;
				Toast.makeText(RosterActivity.this, presence.getFrom()+"的状态变为"+presence, Toast.LENGTH_LONG).show();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	};
	private class RosterListAdapter extends BaseExpandableListAdapter {

		private TextView getTextView() {
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT, 64);
			TextView textView = new TextView(RosterActivity.this);
			textView.setLayoutParams(lp);
			textView.setGravity(Gravity.CENTER_VERTICAL);
			textView.setPadding(36, 0, 0, 0);
			textView.setTextSize(20);
			textView.setTextColor(Color.BLACK);
			return textView;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return children.get(groupPosition).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			LinearLayout ll=new LinearLayout(RosterActivity.this);
			ll.setOrientation(0);
			ImageView head=new ImageView(RosterActivity.this);
			head.setImageResource(R.drawable.im);
			ll.addView(head);
			TextView childTextView=getTextView();
			childTextView.setText(children.get(groupPosition).get(childPosition).getName());
			ll.addView(childTextView);
			return ll;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			return children.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			return groupList.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			// TODO Auto-generated method stub
			return groupList.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			LinearLayout ll=new LinearLayout(RosterActivity.this);
			ll.setOrientation(0);
			ll.setPadding(36, 0, 0, 0);
			TextView groupTextView=getTextView();
			groupTextView.setText(groupList.get(groupPosition)+"	(0/0)");
			ll.addView(groupTextView);
			return ll;
		}

		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return true;
		}

	}
}
