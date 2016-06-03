package com.hbm.haeboomi;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class pfTimetable_Helper extends SQLiteOpenHelper {
	private final String TAG = "EndHBM_pfTT_Helper";

	private final static String db_name = "timetable.db";
	private final String db_table_name = "schedule";
	private SQLiteDatabase scheduleDB;
	private DBManager db;
	private DBManager.innerDB innerDB;
	private String[] idpw;

	public pfTimetable_Helper(Activity activity) {
		super(activity, db_name, null, 1);
		scheduleDB = this.getWritableDatabase();
		db = new DBManager(activity);
		innerDB = new DBManager.innerDB(activity);
		idpw = innerDB.getData().split("!");
		syncDB();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql = "create table if not exists " + db_table_name + "("
				+ " _id integer PRIMARY KEY ,"
				+ " cname text, "
				+ " classno text, "
				+ " divide text)";
		db.execSQL(sql);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS" + db_table_name);
		onCreate(db);
	}

	public void onClose() {
		this.close();
		innerDB.onDestroy();
	}

	//DB에 추가하는 함수
	public void add(int id, String cname, String classno, String divide) {
		ContentValues val = new ContentValues();
		val.put("_id", id);
		val.put("cname", cname);
		val.put("classno", classno);
		val.put("divide", divide);
		scheduleDB.insert(db_table_name, null, val);

		int time = id / 5 + 9;    //9 ~ 20
		int day = id % 5 + 1;     //1 ~ 5
		db.putSchedule(day, time < 10? "0" : "" + time + ":00:00", cname, classno, divide); //내부DB에 저장된 시간표를 외부 DB에 추가
	}

	//DB수정하는 함수
	public void update(int id, String cname, String classno, String divide) {
		ContentValues val = new ContentValues();
		val.put("_id", id);
		val.put("cname", cname);
		val.put("classno", classno);
		val.put("divide", divide);
		scheduleDB.update(db_table_name, val, "_id = " + id, null);

		int time = id / 5 + 9;    //9 ~ 20
		int day = id % 5 + 1;     //1 ~ 5
		if(db.delete("id = " + idpw[0] + " and `time` = '" + (time < 10 ? "0" : "") + time + ":00:00'" + " and day = " + day, DBManager.GetTable.PRO_SCHEDULE))
			db.putSchedule(day, time < 10? "0" : "" + time + ":00:00", cname, classno, divide); //내부DB에 저장된 시간표를 외부 DB에 추가
	}

	//DB에 레코드 삭제하는 함수
	public void delete(int id) {
		//DB에 삭제하고자하는 아이디값을 넘겨 받아서 쿼리로 검색 후 해당 아이디값의 레코드 삭제
		scheduleDB.delete(db_table_name, "_id = " + id, null);

		int time = id / 5 + 9;    //9 ~ 20
		int day = id % 5 + 1;     //1 ~ 5
		db.delete("id = " + idpw[0] + " and `time` = '" + (time < 10? "0" : "") + time + ":00:00'" + " and day = " + day, DBManager.GetTable.PRO_SCHEDULE);
	}

	private void syncDB() {
		String[] schedules = db.getSelectData("*", "pro_schedule", "id = " + idpw[0], DBManager.GetTable.PRO_SCHEDULE).split("\n");
		if(schedules[0].equals("")) schedules = new String[0];
		Cursor cur = getAll();

		int gap = schedules.length - cur.getCount();
		if(gap < 0) {   //내부 DB에 있는 데이터가 더 많다. (내부DB에 저장된 시간표를 DB에 추가시켜야함)
			if(cur != null) {
				cur.moveToFirst();  //커서를 첫번째로 옮김
				while(!cur.isAfterLast()) { //마지막 커서가 아니면 루프 실행
					int position = cur.getInt(0);
					String cname = cur.getString(1);
					String classno = cur.getString(2);
					String divide = cur.getString(3);
					int time = position / 5 + 9;    //9 ~ 20
					int day = position % 5 + 1;     //1 ~ 5

					db.putSchedule(day, time < 10? "0" : "" + time + ":00:00", cname, classno, divide); //내부DB에 저장된 시간표를 외부 DB에 추가
					cur.moveToNext();   //다음 커서로 이동
				}
			}
		}
		else if(gap > 0) {  //DB에 있는 데이터가 더 많다. (내부DB에 추가시켜야함)
			for(int i = 0; i < schedules.length; i++) {
				String[] schedule = schedules[i].split("!");

				int day = Integer.parseInt(schedule[1]);
				int time = Integer.parseInt(schedule[2].split(":")[0]);

				int id = (time - 9) * 5 + (day - 1);

				ContentValues val = new ContentValues();
				val.put("_id", id);
				val.put("cname", schedule[3]);
				val.put("classno", schedule[5]);
				val.put("divide", schedule[4]);
				scheduleDB.insert(db_table_name, null, val);
			}
		}
	}

	//DB의 레코드들을 모두가져오는 함수
	public Cursor getAll() {
		//해당 테이블의 모든 레코드 리턴
		return scheduleDB.query(db_table_name, null, null, null, null, null, null);
	}
}