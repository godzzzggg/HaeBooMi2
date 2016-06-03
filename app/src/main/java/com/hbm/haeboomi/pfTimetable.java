package com.hbm.haeboomi;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class pfTimetable extends Activity implements OnClickListener {
	private final String TAG = "EndHBM_pfTimetable";

	private BackPressCloseHandler bpch;

	//DatabaseFile의 경로를 가져오기위한 변수
	private String db_name = "timetable.db";

	//Database를 생성 관리하는 클래스
	private pfTimetable_Helper helper;

	private DBManager db;
	private Cursor cur;

	private LinearLayout lay[] = new LinearLayout[12];
	private LinearLayout lay_time;

	private String time_line[] = {"1교시\n09:00", "2교시\n10:00", "3교시\n11:00", "4교시\n12:00", "5교시\n13:00", "6교시\n14:00",
						          "7교시\n15:00", "8교시\n16:00", "9교시\n17:00", "10교시\n18:00", "11교시\n19:00", "12교시\n20:00"};
	private String day_line[] = {"시간", "월", "화", "수", "목", "금"};

	private TextView time[] = new TextView[time_line.length];
	private TextView day[] = new TextView[day_line.length];
	private TextView data[] = new TextView[time_line.length * day_line.length];
	private TextView db_data[] = new TextView[time_line.length * day_line.length];

	private EditText put_subject;
	private EditText put_classroom;
	private EditText put_divide;

	private int titleBarHeight;
	private int db_id;
	private String db_classroom, db_subject, db_divide;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timetable);//time.xml 을 가져와 화면에 뿌림

		bpch = new BackPressCloseHandler(this);

		//db파일이 저장된 곳의 위치를 읽어와서 String변수 dbPath에 넣어준다.
		//String dbPath = getApplicationContext().getDatabasePath(db_name).getPath();
		//Log.d("my db path=", dbPath);

		//DataBase 관리 클래스의 객체 생성을 해줌으로써 timetable.db파일과 schedule 테이블 생성
		helper = new pfTimetable_Helper(this);

		//상단 상태바 Height를 구하는 메소드
		getStatusBarSizeOnCreate();
		//테이블을 그려준다.
		drawTable();
	}

	//상단 상태바 Height를 구하는 메소드
	private void getStatusBarSizeOnCreate() {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);

		switch (displayMetrics.densityDpi) {
			case DisplayMetrics.DENSITY_HIGH:
				titleBarHeight = 38;
				break;
			case DisplayMetrics.DENSITY_MEDIUM:
				titleBarHeight = 25;
				break;
			case DisplayMetrics.DENSITY_LOW:
				titleBarHeight = 19;
				break;
			default:
				titleBarHeight = 25;
		}
	}
	//테이블을 그려준다.
	private void drawTable() {
		//레이아웃을 어떻게 그릴지 설정
		LayoutParams params_1 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		final LayoutParams params_2 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		params_1.weight = 1; //레이아웃의 weight를 동적으로 설정 (칸의 비율)
		params_1.width = getLcdSizeWidth() / 6;
		params_1.height = getLcdSizeHeight() / 20;
		params_1.setMargins(1, 1, 1, 1);
		params_1.gravity = Gravity.CENTER;

		params_2.weight = 1; //레이아웃의 weight를 동적으로 설정 (칸의 비율)
		params_2.width = getLcdSizeWidth() / 6;
		params_2.height = (int)((getLcdSizeHeight() - ProfessorMainActivity.TAB_HEIGHT - params_1.height - titleBarHeight) / 12.5);
		params_2.setMargins(1, 1, 0, 0);
		params_2.gravity = Gravity.CENTER;

		//레이아웃 배열로 선언
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
		lay[10] = (LinearLayout)findViewById(R.id.lay_10);
		lay[11] = (LinearLayout)findViewById(R.id.lay_11);

		//요일 생성
		for(int i = 0; i < day.length; i++) {
			day[i] = new TextView(this);
			day[i].setText(day_line[i]);//텍스트에 보여줄 내용
			day[i].setGravity(Gravity.CENTER);//정렬
			day[i].setBackgroundColor(Color.parseColor("#eff9ff"));//배경색
			day[i].setTextSize(15);//글자크기
			lay_time.addView(day[i], params_1);//레이아웃에 출력
		}
		//교시 생성
		for(int i = 0; i < time.length; i++) {
			time[i] = new TextView(this);
			time[i].setText(time_line[i]);
			time[i].setGravity(Gravity.CENTER);
			time[i].setBackgroundColor(Color.parseColor("#f1f1f1"));
			time[i].setTextSize(15);
			lay[i].addView(time[i], params_2);
		}

		cur = helper.getAll();
		cur.moveToFirst();
		// data값 생성
		for(int i = 0, id = 0; i < lay.length; i++) {   //12개 (1교시 ~ 12교시)
			for(int j = 1; j < day_line.length; j++, id++) {  //5개 (월 ~ 금)
				data[id] = new TextView(this);
				data[id].setId(id);
				data[id].setTextSize(13);
				data[id].setGravity(Gravity.CENTER);
				data[id].setBackground(new Drawable() {
					@Override
					public void draw(Canvas canvas) {
						Rect rect = new Rect(1, 0, params_2.width, params_2.height);    //left와 bottom만 그려지게 된다.
						Paint paint = new Paint();

						paint.setColor(Color.WHITE);    //배경을 흰색으로 그린다.
						canvas.drawRect(rect, paint);

						paint.setColor(Color.BLACK);    //테두리선은 검은색으로
						paint.setStrokeWidth(1);        //border = 1
						paint.setStyle(Paint.Style.STROKE); //테두리만 그린다
						canvas.drawRect(rect, paint);
					}

					@Override
					public void setAlpha(int alpha) {
					}

					@Override
					public void setColorFilter(ColorFilter colorFilter) {
					}

					@Override
					public int getOpacity() {
						return 0;
					}
				});

				//시간표를 입력하기 위한 곳을 클릭하면 클릭이벤트를 처리하기위해 동작처리함수
				data[id].setOnClickListener(this);

				if((cur != null) && (!cur.isAfterLast())) {
					db_id = cur.getInt(0);
					db_subject = cur.getString(1);
					db_classroom = cur.getString(2);
					db_divide = cur.getString(3);
					if(data[id].getId() == db_id) {
						data[id].setText(db_subject + "/" + db_classroom + "/" + db_divide);
						cur.moveToNext();
					}
				}
				else if(cur.isAfterLast()) {
					cur.close();
				}
				lay[i].addView(data[id], params_2); //시간표 데이터 출력
			}
		}
	}

	public void onDestroy() {
		super.onDestroy();
		helper.onClose();
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		Cursor cursor = helper.getAll();//테이블의 모든 데이터를 커서로 리턴.
		int get[] = new int[data.length];

		if(cursor != null) {
			cursor.moveToFirst();

			//커서가 데이터의 마지막일때 까지 커서가 이동할 수있도록 해준다.
			while(!cursor.isAfterLast()) {
				//정수배열의 테이블의 id값의 배열에 id값을 넣어준다.(get[3]=3)
				get[cursor.getInt(0)] = cursor.getInt(0);
				cursor.moveToNext();//커서를 이동시켜준다.
			}
			for(int i = 0; i < get.length; i++) {//배열의 길이만큼
				//배열에 데이터가 있고,클릭한곳에 데이터가 있을시
				if((get[i] != 0) && (get[i] == id)) {
					update_timetable_dig(cursor, id);   //클릭한곳의 아이디 값을 업데이트 다이얼로그로 넣어 불러준다.
					break;
				}
				//배열에 데이터가 없고,클릭한곳이 데이터가 없을때
				else if((get[i] == 0) && (id == data[i].getId())) {
					add_timetable_dig(id);  //해당 다이얼로그를 불러줌
					break;
				}
			}
		}
	}

	//클릭한 곳에 데이터가 있을 경우 띄어주는 수정가능한 다이얼로그
	public void update_timetable_dig(Cursor cur, final int id) {
		final LinearLayout lay = (LinearLayout)View.inflate(pfTimetable.this, R.layout.timetable_input_dig, null);
		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		ad.setTitle("TimeTable");
		ad.setView(lay);

		put_subject = (EditText)lay.findViewById(R.id.input_subject);
		put_classroom = (EditText)lay.findViewById(R.id.input_classroom);
		put_divide = (EditText)lay.findViewById(R.id.input_divide);

		//데이터를 수정, 삭제 하기 위한 다이얼로그
		Cursor c = cur;     //해당 뷰에 데이터가 있으면 다이얼로그 텍스트창에 출력해주기 위해 커서 사용.

		if(c != null) {     //커서의 데이터가 있으면
			c.moveToFirst();    //커서를 처음으로 옮겨준다.
			while(!c.isAfterLast()) {   //커서가 데이터의 마지막일때 까지 커서가 이동할 수있도록 해준다.
				if(c.getInt(0) == id) {
					//2열 3열, 강의명,강의실명을 가져와 텍스트에 보여주도록 설정
					put_subject.setText(c.getString(1));
					put_classroom.setText(c.getString(2));
					put_divide.setText(c.getString(3));
					break;
				}
				c.moveToNext();//커서를 다음 행으로 이동시켜주는 역할
			}
		}
		//강의명, 강의실을 적는 창을 각각 클릭했을 때 출력된 데이터를 지워준다.
		put_subject.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((EditText)v).setText(null);
			}
		});
		put_classroom.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((EditText)v).setText(null);
			}
		});
		put_divide.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((EditText)v).setText(null);
			}
		});
		//수정 버튼이 눌렸을때 처리하는 명령어
		ad.setPositiveButton("수정", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				int get_id = data[id].getId();
				String cname = put_subject.getText().toString().replaceAll(" ", "");
				String classno = put_classroom.getText().toString().replaceAll(" ", "");
				String divide = put_divide.getText().toString().replaceAll(" ", "");

				if(cname.length() != 0 && classno.length() != 0 && divide.length() != 0) {
					helper.update(get_id, cname, classno, divide);
					data[id].setText(cname + "/" + classno + "/" + divide);
				}
			}
		});
		ad.setNegativeButton("삭제", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				helper.delete(id);
				data[id].setText(null);
			}
		});
		ad.show();
	}

	//데이터가 없는 곳을 클릭했을 때 띄어주는 다이얼로그
	public void add_timetable_dig(final int id) {
		//inflate메서드는 컴파일된 리소스정보를 순서대고 해석해 뷰를생성하고 '루트'뷰를 리턴
		//쉽게말해 inflate는 xml과 activiy를 연결해 동작을 처리하고 그 결과를 xml을 통해 화면에 보여줌
		final LinearLayout lay = (LinearLayout)View.inflate(pfTimetable.this, R.layout.timetable_input_dig, null);
		AlertDialog.Builder ad = new AlertDialog.Builder(this);//빌더 객체 생성
		ad.setTitle("TimeTable");//다이얼로그의 제목
		ad.setView(lay);//화면에 보여줄 대상을 설정

		put_subject = (EditText)lay.findViewById(R.id.input_subject);
		put_classroom = (EditText)lay.findViewById(R.id.input_classroom);
		put_divide = (EditText)lay.findViewById(R.id.input_divide);

		//저장 버튼이 눌렸을 때 처리
		ad.setPositiveButton("저장", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				//저장버튼일때 DB_table에 데이터 쓰기
				int get_id = data[id].getId();
				String cname = put_subject.getText().toString().replaceAll(" ", "");
				String classno = put_classroom.getText().toString().replaceAll(" ", "");
				String divide = put_divide.getText().toString().replaceAll(" ", "");

				if(cname.length() != 0 && classno.length() != 0 && divide.length() != 0) {
					helper.add(get_id, cname, classno, divide);
					//editText를 통해 사용자에게 입력 받았던 데이터를 해당 텍스트뷰에 출력
					data[id].setText(cname + "/" + classno + "/" + divide);
				}
			}
		});
		//취소버튼이 눌렸을 경우
		ad.setNegativeButton("취소", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();//다이얼로그 종료
			}
		});
		ad.show();
	}

	//정렬해주기 위해 디스플레이의 가로 세로 정보를 리턴해주는 함수
	public int getLcdSizeWidth() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}
	public int getLcdSizeHeight() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}

	@Override
	public void onBackPressed() {
		bpch.onBackPressed();
	}
}
