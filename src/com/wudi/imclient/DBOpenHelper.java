package com.wudi.imclient;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {

	private static final String DBNAME="imdatabase";
	public DBOpenHelper(Context context, 
			int version) {
		super(context, DBNAME, null, version);
	}

	

	@Override
	public void onCreate(SQLiteDatabase db) {
		
	}



	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}



	@Override
	public void onOpen(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		super.onOpen(db);
	}

	public void createTable(String tableName, SQLiteDatabase db) {
		String sql = "create table if not exists "
				+ tableName
				+ " ("
				+ "_id integer primary key autoincrement ,_from char(2) CHECK(_from in('IN','OUT')),"
				+ " toUserName varchar(20) not null, content varchar(100), type varchar(10) not null," +
				"filePath varchar(10) default null ,date timestamp not null default (datetime('now','localtime')) )";
		db.execSQL(sql);
	}

}
