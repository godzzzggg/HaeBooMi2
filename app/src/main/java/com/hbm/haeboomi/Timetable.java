package com.hbm.haeboomi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class Timetable extends Activity implements OnClickListener{
	
	private final String tag = "timetable.class";
	
	//DatabaseFile�� ��θ� ������������ ����
	private String db_name = "timetable.db";
	
	//Database�� ���� �����ϴ� Ŭ����
	private Timetable_Helper helper;
	
	SQLiteDatabase db;
	Cursor cur;

	LinearLayout lay[] = new LinearLayout[10];
	LinearLayout lay_time;
	
	String time_line[]={"1����\n09:00","2����\n10:00","3����\n11:00","4����\n12:00","5����\n13:00",
			"6����\n14:00","7����\n15:00","8����\n16:00","9����\n17:00","10����\n18:00"};
	String day_line[]={"�ð�","��","ȭ","��","��","��"};
	
	TextView time[] = new TextView[time_line.length]; 
	TextView day[] = new TextView[day_line.length];
	TextView data[] = new TextView[time_line.length * day_line.length];
	TextView db_data[] = new TextView[time_line.length * day_line.length];
	
	EditText put_subject;
	EditText put_classroom;
	
	int db_id;
	String db_classroom,db_subject;	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timetable);//time.xml �� ������ ȭ�鿡 �Ѹ�

		//db������ ����� ���� ��ġ�� �о�ͼ� String���� dbPath�� �־��ش�.
		String dbPath = getApplicationContext().getDatabasePath(db_name).getPath();
		Log.i("my db path=", ""+dbPath);
		
		//DataBase ���� Ŭ������ ��ü ������ �������ν� timetable.db���ϰ� schedule ���̺� ����
		helper = new Timetable_Helper(this); 
		int counter = helper.getCounter();
		Log.i(tag,"counter = "+counter);
		
		//���� ����ִ� �����͸� logâ���� Ȯ����
		//HelperŬ������ search�Լ��� ������ �ڼ��� ��������.
		helper.search_data();
		
		//���̾ƿ��� ��� �׸��� ����
		@SuppressWarnings("deprecation")
		LayoutParams params_1 = new LayoutParams(
		LayoutParams.FILL_PARENT,
		LayoutParams.FILL_PARENT);
		params_1.weight = 1; //���̾ƿ��� weight�� �������� ���� (ĭ�� ����)
		params_1.width=getLcdSizeWidth()/6; 
		params_1.height=getLcdSizeHeight()/14;
		params_1.setMargins(1, 1, 1, 1);
		params_1.gravity=1; //ǥ�� ��Ʋ���� ���� ����
		
		@SuppressWarnings("deprecation")
		LayoutParams params_2 = new LayoutParams(
		LayoutParams.FILL_PARENT,
		LayoutParams.FILL_PARENT);
		params_2.weight = 1; //���̾ƿ��� weight�� �������� ���� (ĭ�� ����)
		params_2.width=getLcdSizeWidth()/6; 
		params_2.height=getLcdSizeHeight()/20;
		params_2.setMargins(1, 1, 1, 1);
			
			
		//���̾ƿ� �迭�� ����
		lay_time = (LinearLayout)findViewById(R.id.lay_time);
		lay[0] = (LinearLayout)findViewById(R.id.lay_0);
		lay[1] = (LinearLayout)findViewById(R.id.lay_1);
		lay[2] = (LinearLayout)findViewById(R.id.lay_2);
		lay[3] = (LinearLayout)findViewById(R.id.lay_3);
		lay[4] = (LinearLayout)findViewById(R.id.lay_4);
		lay[5] = (LinearLayout)findViewById(R.id.lay_5);
		lay[6] = (LinearLayout)findViewById(R.id.lay_6);
		lay[7] = (LinearLayout)findViewById(R.id.lay_7);
		lay[8] = (LinearLayout)findViewById(R.id.lay_8);
		lay[9] = (LinearLayout)findViewById(R.id.lay_9);
		
		//���� ����
		for (int i = 0; i < day.length; i++) {
			day[i] = new TextView(this);
			day[i].setText(day_line[i]);//�ؽ�Ʈ�� ������ ����
			day[i].setGravity(Gravity.CENTER);//����
			day[i].setBackgroundColor(Color.parseColor("#FAF4C0"));//����
			day[i].setTextSize(10);//����ũ��
			lay_time.addView(day[i], params_2);//���̾ƿ��� ���
		}
		//���� ����
		for (int i = 0; i < time.length; i++) {
			time[i] = new TextView(this);
			time[i].setText(time_line[i]);
			time[i].setGravity(Gravity.CENTER);
			time[i].setBackgroundColor(Color.parseColor("#EAEAEA"));
			time[i].setTextSize(10);
			lay[i].addView(time[i],params_1);
		}
		
		cur =  helper.getAll();
		cur.moveToFirst();
		// data�� ����
		for (int i = 0, id=0; i < lay.length; i++) { //10��
			for (int j = 1; j <day_line.length; j++) { //6��
				data[id] = new TextView(this);
				data[id].setId(id);//data[0]  =  0
				data[id].setTextSize(10);
				//�ð�ǥ�� �Է��ϱ� ���� ���� Ŭ���ϸ� Ŭ���̺�Ʈ�� ó���ϱ����� ����ó���Լ�
				data[id].setOnClickListener(this);
				data[id].setGravity(Gravity.CENTER);
				data[id].setBackgroundColor(Color.parseColor("#EAEAEA"));
				if((cur!=null) && (!cur.isAfterLast())){
					db_id = cur.getInt(0);
					db_subject = cur.getString(1);
					db_classroom = cur.getString(2);
					if(data[id].getId()==db_id){
						data[id].setText(db_subject+"\n"+db_classroom);
						cur.moveToNext();
					}
				}
				else if(cur.isAfterLast()){
					cur.close();
				}
				lay[i].addView(data[id], params_1); //�ð�ǥ ������ ���
				id++;
			}//End of Second For
        }//End of First For
    }//End of OnCreate Method
	
	public void onDestroy(){
		super.onDestroy();
		helper.close();
	}

	@Override
	public void onClick(View view) {
		Cursor cursor = null;
		cursor = helper.getAll();//���̺��� ��� �����͸� Ŀ���� ����.
		int get[] = new int[50];
		if(cursor!=null){
			Log.i(tag, "cursor is not null");
			cursor.moveToFirst();
			for(int i=0;i<50;i++){
				get[i]=0;//�迭 �ʱ�ȭ
			}
			//Ŀ���� �������� �������϶� ���� Ŀ���� �̵��� ���ֵ��� ���ش�.
			while(!cursor.isAfterLast()){
				//�����迭�� ���̺��� id���� �迭�� id���� �־��ش�.(get[3]=3)
				get[cursor.getInt(0)] = cursor.getInt(0); 
				Log.i(tag, "get "+get[cursor.getInt(0)]);
				cursor.moveToNext();//Ŀ���� �̵������ش�.
			}
			for(int i=0;i<50;i++){//�迭�� ���̸�ŭ
				Log.i(tag, "get[i] ="+get[i]+ 
						"   view.getid ="+view.getId()+ 
						"   data[i].getId() ="+data[i].getId());
				//�迭�� �����Ͱ� �ְ�,Ŭ���Ѱ��� �����Ͱ� ������
				if( (get[i]!=0) && (get[i]==view.getId()) ){
					//Ŭ���Ѱ��� ���̵� ���� ������Ʈ ���̾�α׷� �־� �ҷ��ش�.
					update_timetable_dig(view.getId());
					break;
				}
				//�迭�� �����Ͱ� ����,Ŭ���Ѱ��� �����Ͱ� ������
				else if((get[i]==0) && (view.getId() == data[i].getId()) ){
					add_timetable_dig(view.getId());//�ش� ���̾�α׸� �ҷ���
					break;
				}
			}//End of For
		}//End of   if(cursor!=null)
	}//End of OnClick

	
	/*Ŭ���� ���� �����Ͱ� ���� ��� ����ִ� ���������� ���̾�α�*/
	public void update_timetable_dig(final int id){
		final LinearLayout lay = (LinearLayout)
				View.inflate(Timetable.this, R.layout.timetable_input_dig, null);
		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		ad.setTitle("TimeTable");
		//ad.setIcon(R.drawable.timetable);//���̾�α��� ������
		ad.setView(lay);
		put_subject = (EditText)lay.findViewById(R.id.input_subject);
		put_classroom = (EditText)lay.findViewById(R.id.input_classroom);
		/*�����͸� ����, ���� �ϱ� ���� ���̾�α�*/
		Cursor c;//�ش� �信 �����Ͱ� ������ ���̾�α� �ؽ�Ʈâ�� ������ֱ� ���� Ŀ�� ���.
		c = helper.getAll();//Ŀ���� �����ͺ��̽� ���̺��� ��� �����͸� ��������.
		if(c!=null){//Ŀ���� �����Ͱ� ������
			c.moveToFirst();//Ŀ���� ���̺� ���� ó��, �� ���̺��� �� 1���� ����Ű���� �Ѵ�.
			while(!c.isAfterLast()){//Ŀ���� �������� �������϶� ���� Ŀ���� �̵��� ���ֵ��� ���ش�.
				//Ŀ���� ����Ű�� ���� �� 1��, id�� ����Ǿ��ִ� ���� id���� ����ڰ� �������� id���� ������,
				//����ڰ� Ŭ���� ���� �����Ͱ� ������ �����ϰ� �ݺ��� ����
				if(c.getInt(0)== id){ 
					//2�� 3��, ���Ǹ�,���ǽǸ��� ������ �ؽ�Ʈ�� �����ֵ��� ����
					put_subject.setText(c.getString(1));
					put_classroom.setText(c.getString(2));
					break;
				}
				c.moveToNext();//Ŀ���� ���� ������ �̵������ִ� ����
			}
		}
		//���Ǹ�, ���ǽ��� ���� â�� ���� Ŭ������ �� ��µ� �����͸� �����ش�. 
		put_subject.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
		    	put_subject.setText(null);
		    }});
		put_classroom.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
		    		put_classroom.setText(null);
		    }});
		//���� ��ư�� �������� ó���ϴ� ��ɾ�
		ad.setPositiveButton("����"/*��ư�� ������ text*/, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				int get_id = data[id].getId();  
				helper.update(get_id,put_subject.getText().toString(),put_classroom.getText().toString());
				data[id].setText(""+put_subject.getText()+ "\n" + put_classroom.getText());
			}});
		ad.setNegativeButton("����"/*��ư�� ������ text*/, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				helper.delete(id);
				data[id].setText(null);
			}});
		ad.show();
	}//End of dialog 
	
	/*�����Ͱ� ���� ���� Ŭ������ �� ����ִ� ���̾�α�*/
	public void add_timetable_dig(final int id){
		//inflate�޼���� �����ϵ� ���ҽ������� ������� �ؼ��� �並�����ϰ� '��Ʈ'�並 ����
		//���Ը��� inflate�� xml�� activiy�� ������ ������ ó���ϰ� �� ����� xml�� ���� ȭ�鿡 ������
		final LinearLayout lay = (LinearLayout)View.inflate
				(Timetable.this, R.layout.timetable_input_dig, null);
		AlertDialog.Builder ad = new AlertDialog.Builder(this);//���� ��ü ����
		ad.setTitle("TimeTable");//���̾�α��� ����
		//ad.setIcon(R.drawable.timetable);//���̾�α��� ������
		ad.setView(lay);//ȭ�鿡 ������ ����� ����
		/*���� ��ư�� ������ �� ó��*/
		ad.setPositiveButton("����"/*��ư�� ������ text*/, new DialogInterface.OnClickListener() {
		EditText put_subject = (EditText)lay.findViewById(R.id.input_subject);
		EditText put_classroom = (EditText)lay.findViewById(R.id.input_classroom);	
			public void onClick(DialogInterface dialog, int which) {
				/*�����ư�϶� DB_table�� ������ ����*/
				//�������� ���̵� ���� ������� add�Լ��� �Ѱ��ش�. 
				//�� ���̵� ���� ����Ǿ��ִ� �����͸� ����� �� �� ������ִ� ���� ��ġ��Ű�� ���� ���̵��� ���
				int get_id = data[id].getId(); 
				//EditTextâ���� �Է��� ���� �������� ���ؼ��� EditTextâ ���̵�.getText().toString()���� ���־���Ѵ�.
				//EditTextâ���� �Է��� ���� ������� ���������� EditTextâ ���̵�.getText().toString().trim() ���� ���־���Ѵ�.
				helper.add(get_id,put_subject.getText().toString(),put_classroom.getText().toString());
				//editText�� ���� ����ڿ��� �Է� �޾Ҵ� �����͸� �ش� �ؽ�Ʈ�信 ���
				data[id].setText(""+put_subject.getText()+ "\n" + put_classroom.getText());
			}
		});
		//��ҹ�ư�� ������ ���
		ad.setNegativeButton("���"/*��ư�� ������ text*/, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();//���̾�α� ����
			}
		});
		ad.show();
	}//End of dialog 

	/*�������ֱ� ���� ���÷����� ���� ���� ������ �������ִ� �Լ�*/
	@SuppressWarnings("deprecation")
	public int getLcdSizeWidth() {
		// TODO Auto-generated method stub
		return  ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
	}//End of getLcdSizeWidth Method

    @SuppressWarnings("deprecation")
	public int getLcdSizeHeight() {
		// TODO Auto-generated method stub
		return ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
	}//End of getLcdSizeHeight Method

	
} //END of Timetable Class
