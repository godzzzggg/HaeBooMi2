package com.hbm.haeboomi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Timetable_Helper extends SQLiteOpenHelper{
	
	private final String tag = "DB_helper.java";
	private final static String db_name = "timetable.db";
	private final String db_table_name = "schedule";
	SQLiteDatabase db;
	static String result;
	
	
	public Timetable_Helper(Context context) {
		super(context, db_name, null, 1);
		db = this.getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql = "create table if not exists "+ db_table_name + "("
				+ " _id integer PRIMARY KEY ,"
				+ " subject text, "
				+ " classroom text)";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS" + db_table_name);
		onCreate(db);
	}
	
	//DB�� �߰��ϴ� �Լ�
	public void add(int id, String a, String b){
		ContentValues val = new ContentValues();
		val.put("_id", id);
		val.put("subject", a);
		val.put("classroom", b);
		db.insert(db_table_name, null, val);
		search_data();
	}
	
	//DB�����ϴ� �Լ�
	public void update(long rawId, String a, String b){
		ContentValues val = new ContentValues();
		val.put("_id", rawId);
		val.put("subject", a);
		val.put("classroom", b);
		db.update(db_table_name, val, "_id = "+ rawId, null);
		search_data();
	}
	
	//DB�� ���ڵ� �����ϴ� �Լ�
	public void delete(long rawId){
		//DB�� �����ϰ����ϴ� ���̵��� �Ѱ� �޾Ƽ� ������ �˻� �� �ش� ���̵��� ���ڵ� ���� 
		db.delete(db_table_name, "_id = "+ rawId , null);
		search_data();
		
	}
	
	//�α׻����� �����͸� Ȯ���ϰ��� ���� �Լ�
	public void search_data(){
		String sql = "select * from "+ db_table_name;
		Cursor cur = db.rawQuery(sql, null);
		cur.moveToFirst();
	
		//Ŀ���� ������ ������ ������ �����Ͱ� ���������� Ȯ���� ���־�� �Ѵ�.
		//Ŀ���� �������� �������� �ƴҶ����� �ݺ��� ����
		//(Ŀ���� �ݵ�� �ݺ����ȿ��� moveToNext�� ���־�� ���� �� ���ڵ带 ���� �� �ִ�.
		while(!cur.isAfterLast()){
			//�ش� ���ڵ����� �� ���� ���� �����´�. 
			//0�� ���� ���̵�, 1�� ���� ���Ǹ�, 2�� ���� ���ǽ�
			//0���� int���̱� ������ �����ö��� cur.getInt(0); �̷��� �����´�.
			//cursor.getInt(or getString)(����ȣ);
			String subject = cur.getString(1);
			String classroom = cur.getString(2);
			result = (subject + "   " + classroom);
			Log.i(tag, result);
			cur.moveToNext();
		}
		cur.close();
	}
	
	//DB�� ���ڵ���� ��ΰ������� �Լ�
	public Cursor getAll(){
		//�ش� ���̺��� ��� ���ڵ� ����
		return db.query(db_table_name, null, null,null,null,null,null);
	}
	
	//�˻��ϰ����ϴ� ���̵����� ���̵� �ش��ϴ� ���ڵ� ��ȯ
	public Cursor getId(int id){
		Cursor cur = db.query(db_table_name , null, "_id = " + id , null,null,null,null);
		if(cur!=null&&cur.getCount() !=0)
			cur.moveToNext();
		return cur;
		
	}
	
	//���ڵ� ���� ������ ī��Ʈ ����
	public int getCounter(){
		Cursor cur = null;
		String sql = "select * from "+ db_table_name;
		cur = db.rawQuery(sql, null);
		int counter = 0;
		while(!cur.isAfterLast()){
			cur.moveToNext();
			counter++;
		}
		return counter;
	}
	
}

