package com.wudi.imclient;

public class Msg {
	public static final String TEXT = "text";
	public static final String VOICE = "voice";
	public static final String IN = "IN";
	public static final String OUT = "OUT";
	String userId;
	String content;
	long date;
	String from;
	String type;
	String filePath;

	public Msg(String userId, String content, long date, String from,
			String type, String filePath) {
		super();
		this.userId = userId;
		this.content = content;
		this.date = date;
		this.from = from;
		this.type = type;
		this.filePath = filePath;
	}

	@Override
	public String toString() {
		return "Msg [userId=" + userId + ", msg=" + content + ", date=" + date
				+ ", from=" + from + ", type=" + type + ", filePath="
				+ filePath + "]";
	}

}
