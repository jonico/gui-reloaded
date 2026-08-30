package com.collabnet.ccf.db;

public class Update {
	private String columnName;
	private String value;
	private boolean stringValue = true;
	
	public Update(String columnName, String value, boolean stringValue) {
		this.columnName = columnName;
		this.stringValue = stringValue;
		this.value = value;
	}
	
	public Update(String columnName, String value) {
		this(columnName, value, true);
	}
	
	public String toString() {
		StringBuffer expression = new StringBuffer(columnName + " = ");
		if (stringValue) expression.append("'");
		expression.append(singleToDouble(value));
		if (stringValue) expression.append("'");
		return expression.toString();
	}
	
	public static String getUpdate(String sql, Update[] updates) {
		StringBuffer updateStatement = new StringBuffer(sql);
		if (null != updates) {
			updateStatement.append(" SET ");
			for (int i = 0; i < updates.length; i++) {
				if (0 < i) updateStatement.append(", ");
				updateStatement.append(updates[i].toString());
			}
		}
		return updateStatement.toString();
	}
	
	private static String singleToDouble(String text) {
		boolean done = false;
		int start = 0;
		while (!done) {
			int apos = text.indexOf("'", start);
			if (-1 != apos) {
				text = text.substring(0, apos) + "''" + text.substring(apos + 1);
				start = apos + 2;
			} else {
				done = true;
			}
		}
		return text;
	}
	
}
