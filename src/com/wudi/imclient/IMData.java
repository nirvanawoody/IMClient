package com.wudi.imclient;


import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;

import android.app.Application;
import android.content.ComponentName;
import android.database.sqlite.SQLiteDatabase;

public class IMData extends Application {
	public static  boolean CHATLISTEN=false;
	public static  boolean ROSTERLISTEN=false;
	public static  boolean SERVICELISTEN=false;
	private ComponentName serviceName;
	private XMPPConnection connection;
	private String userName;
	private DBOpenHelper helper;
	private SQLiteDatabase db;
	public XMPPConnection getConnection() {
		return connection;
	}

	@Override
	public void onCreate() {
		ConnectionConfiguration config=new ConnectionConfiguration("192.168.86.64", 5222);
		Connection.DEBUG_ENABLED=true;
		connection=new XMPPConnection(config);
		helper=new DBOpenHelper(getApplicationContext(), 1);
		db=helper.getWritableDatabase();
	}
	
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public DBOpenHelper getHelper() {
		return helper;
	}

	public SQLiteDatabase getDb() {
		return db;
	}

	public ComponentName getServiceName() {
		return serviceName;
	}

	public void setServiceName(ComponentName serviceName) {
		this.serviceName = serviceName;
	}

}
