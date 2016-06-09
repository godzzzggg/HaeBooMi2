package com.hbm.haeboomi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class CalendarActivity extends Activity implements View.OnClickListener {
	private static String TAG = "EndHBM_CalendarActivity";
	// 연/월 텍스트뷰
	private TextView tvDate;
	// 그리드뷰 어댑터
	private GridAdapter gridAdapter;
	// 일 저장 할 리스트
	private ArrayList<String> dayList;
	// 그리드뷰
	private GridView gridView;
	// 캘린더 변수
	private Calendar mCal;
	private int temp = 4;
	private int day[][] = new int[12][31];
	private int year;
	private int mon;
	private int nowMon;
	private String[] days = {"일", "월", "화", "수", "목", "금", "토"};
	private DBManager db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calendar);

		Intent intent = getIntent();
		temp = intent.getIntExtra("random", 4);
		String cname = intent.getStringExtra("cname");
		Log.d(TAG, "temp : " + temp);
		tvDate = (TextView)findViewById(R.id.tv_date);
		gridView = (GridView)findViewById(R.id.gridview);
		// 오늘에 날짜를 세팅 해준다.
		long now = System.currentTimeMillis();
		Date date = new Date(now);

		db = new DBManager(this);
		/////////DB가져오기
		String[] str = db.getSelectData("*", "attendance", "id = " + db.getId() + " and cname = '" + cname + "'", DBManager.GetTable.ATTENDANCE).split("\n");

		for(int i = 0; i < day.length; i++)
			for(int j = 0; j < day[i].length; j++)
				day[i][j] = 44;

		for(String s : str) {
			String[] std_info = s.split("!");
			if(std_info.length != 1) {
				String[] day2 = std_info[4].split("-");
				int Mon = Integer.parseInt(day2[1]) - 1;
				int Day = Integer.parseInt(day2[2]) - 1;
				int d = day[Mon][Day];
				if(d != 44) {
					if(d % 10 == 4)
						day[Mon][Day] += Integer.parseInt(std_info[2]) - 4;
				}
				else
					day[Mon][Day] = Integer.parseInt(std_info[2]) * 10 + 4;
			}
		}
		//////////////DB가져오기
		//연,월,일을 따로 저장
		SimpleDateFormat curYearFormat = new SimpleDateFormat("yyyy", Locale.KOREA);
		SimpleDateFormat curMonthFormat = new SimpleDateFormat("MM", Locale.KOREA);
		year = Integer.parseInt(curYearFormat.format(date));
		nowMon = mon = Integer.parseInt(curMonthFormat.format(date));

		dayList = new ArrayList<>();

		gridAdapter = new GridAdapter(getApplicationContext(), dayList);
		setCalendarDate(mon);
		gridView.setAdapter(gridAdapter);

		findViewById(R.id.btnPrevC).setOnClickListener(this);
		findViewById(R.id.btnNextC).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btnPrevC:
				if(--mon < 2)
					v.setVisibility(View.INVISIBLE);
				else
					v.setVisibility(View.VISIBLE);
				((View)v.getParent()).findViewById(R.id.btnNextC).setVisibility(View.VISIBLE);

				setCalendarDate(mon);
				gridAdapter.notifyDataSetChanged();
				break;
			case R.id.btnNextC:
				if(++mon > 11)
					v.setVisibility(View.INVISIBLE);
				else
					v.setVisibility(View.VISIBLE);
				((View)v.getParent()).findViewById(R.id.btnPrevC).setVisibility(View.VISIBLE);

				setCalendarDate(mon);
				gridAdapter.notifyDataSetChanged();
				break;
		}
	}

	// 해당 월에 표시할 일 수 구함
	private void setCalendarDate(int month) {
		dayList.clear();
		for(int i = 0; i < days.length; i++)    //일 ~ 토까지 출력
			dayList.add(days[i]);

		mCal = Calendar.getInstance();
		mCal.set(year, mon - 1, 1);

		tvDate.setText(year + "/" + (mon < 10 ? "0" : "") + mon);
		int dayNum = mCal.get(Calendar.DAY_OF_WEEK);
		//1일 - 요일 매칭 시키기 위해 공백 add
		for(int i = 1; i < dayNum; i++)
			dayList.add("");

		//오늘 day 가져옴
		Integer today = mCal.get(Calendar.DAY_OF_MONTH);
		mCal.set(Calendar.MONTH, month - 1);
		for(int i = 1; i <= mCal.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
			if(i == today)
				dayList.add("" + i);
			else {
				String text = "" + i;
				int d = day[month - 1][i - 1];
				switch(d / 10) {
					case 0: text += "\n결석"; break;
					case 1: text += "\n지각"; break;
					case 2: text += "\n출석"; break;
					case 3: text += "\n임시"; break;
				}
				switch(d % 10) {
					case 0: text += "\n결석"; break;
					case 1: text += "\n지각"; break;
					case 2: text += "\n출석"; break;
					case 3: text += "\n임시"; break;
				}
				dayList.add(text);
			}
		}
	}

	//그리드뷰 어댑터
	private class GridAdapter extends BaseAdapter {
		private List<String> list;
		private LayoutInflater inflater;
		private Context context;

		// 생성자
		public GridAdapter(Context con, List<String> list) {
			this.list = list;
			context = con;
			this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return list.size(); }

		@Override
		public String getItem(int position) { return list.get(position); }

		@Override
		public long getItemId(int position) { return position; }

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = inflater.inflate(R.layout.item_calendar_gridview, parent, false);
			TextView tvItemGridView = (TextView)convertView.findViewById(R.id.tv_item_gridview);
			convertView.setTag(tvItemGridView);

			tvItemGridView.setText(getItem(position));

			mCal = Calendar.getInstance();
			//오늘 day 가져옴
			Integer today = mCal.get(Calendar.DAY_OF_MONTH);
			String sToday = today.toString();

			if(position % 7 == 0)   //일요일
				tvItemGridView.setTextColor(ContextCompat.getColor(context, R.color.calendarRed));
			else if(position % 7 == 6)  //토요일
				tvItemGridView.setTextColor(ContextCompat.getColor(context, R.color.calendarBlue));

			if(nowMon == mon && sToday.equals(getItem(position).split("\n")[0])) { //오늘 day 텍스트 컬러 변경
				tvItemGridView.setTextColor(ContextCompat.getColor(context, R.color.calendarBlack));
				String text = getItem(position);
				switch(temp) {
					case 0: text += "\n결석"; break;
					case 1: text += "\n지각"; break;
					case 2: text += "\n출석"; break;
					case 3: text += "\n임시출석"; break;
				}
				tvItemGridView.setText(text);
			}
			return convertView;
		}
	}
}