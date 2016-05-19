package com.hbm.haeboomi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;


public class CalendarActivity extends Activity  {
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
    private int temp = 0;
    private int day[][] = new int[12][31];
    private int mon;
    private DBManager DBM;
    boolean check = true;//그리드뷰 나누기 위한 변수
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        temp = intent.getExtras().getInt("random");
        Log.d("m_CalendarActivity", "temp : " + temp);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        tvDate = (TextView)findViewById(R.id.tv_date);
        gridView = (GridView)findViewById(R.id.gridview);
        // 오늘에 날짜를 세팅 해준다.
        long now = System.currentTimeMillis();
        final Date date = new Date(now);

        DBM = new DBManager(this);
        /////////DB가져오기
        DBManager.innerDB innerDB = new DBManager.innerDB(this);
        String id = innerDB.getData("select * from user").split("!")[0];
        String str = DBM.getSelectData("*", "attendance", "id = " + id, DBManager.GetTable.ATTENDANCE);
        String[] Std_info = str.split("!");

        for(int i = 0; i < Std_info.length; i++)
            if(Std_info[i] != null)
                Log.d("Student ", "String [" + i + "] : " + Std_info[i]);

        int j = 4;
        int cnt = 0;
        int len=Std_info.length/6;
        while(cnt != len){
            String[] day2 = Std_info[j].split("-");
            int Mon = Integer.parseInt(day2[1]);
            int Day = Integer.parseInt(day2[2]);
            Log.d("Student ", "String [day] : " + day2[1]);
            day[Mon-1][Day-1] = Integer.parseInt(Std_info[j-2]);
            j=j+6;
            cnt++;
        }
        //////////////DB가져오기
        //연,월,일을 따로 저장
        final SimpleDateFormat curYearFormat = new SimpleDateFormat("yyyy", Locale.KOREA);
        final SimpleDateFormat curMonthFormat = new SimpleDateFormat("MM", Locale.KOREA);
        final SimpleDateFormat curDayFormat = new SimpleDateFormat("dd", Locale.KOREA);
        mon = Integer.parseInt(curMonthFormat.format(date)) - 1;
        //현재 날짜 텍스트뷰에 뿌려줌
        tvDate.setText(curYearFormat.format(date) + "/" + curMonthFormat.format(date));
        //gridview 요일 표시
        dayList = new ArrayList<String>();
        dayList.add("일");
        dayList.add("월");
        dayList.add("화");
        dayList.add("수");
        dayList.add("목");
        dayList.add("금");
        dayList.add("토");
        mCal = Calendar.getInstance();
        //이번달 1일 무슨요일인지 판단 mCal.set(Year,Month,Day)
        mCal.set(Integer.parseInt(curYearFormat.format(date)), mon, 1);
        int dayNum = mCal.get(Calendar.DAY_OF_WEEK);
        //1일 - 요일 매칭 시키기 위해 공백 add
        for (int i = 1; i < dayNum; i++) {
            dayList.add("");
        }
        setCalendarDate(mCal.get(Calendar.MONTH) + 1);
        gridAdapter = new GridAdapter(getApplicationContext(), dayList);
        gridView.setAdapter(gridAdapter);

        final Button btnPrevC = (Button) findViewById(R.id.btnPrevC);
        final Button btnNextC = (Button) findViewById(R.id.btnNextC);

        btnPrevC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Integer.parseInt(curMonthFormat.format(date))==mon) {
                    check = true;
                }
                else
                    check=false;
                if(mon ==  1)
                    btnPrevC.setVisibility(View.INVISIBLE);
                else
                    btnPrevC.setVisibility(View.VISIBLE);
                btnNextC.setVisibility(View.VISIBLE);
                dayList.clear();
                dayList.add("일");
                dayList.add("월");
                dayList.add("화");
                dayList.add("수");
                dayList.add("목");
                dayList.add("금");
                dayList.add("토");
                mCal = Calendar.getInstance();
                mCal.set(Integer.parseInt(curYearFormat.format(date)), --mon , 1);
                tvDate.setText(curYearFormat.format(date) + "/" + (mon < 10? "0" : "") + (mon+1));
                int PdayNum = mCal.get(Calendar.DAY_OF_WEEK);
                //1일 - 요일 매칭 시키기 위해 공백 add
                for (int i = 1; i < PdayNum; i++) {
                    dayList.add("");
                }
                setCalendarDate(mCal.get(Calendar.MONTH) + 1);
                gridAdapter.notifyDataSetChanged();
            }

        });

        btnNextC.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(Integer.parseInt(curMonthFormat.format(date)) == (mon+2)) {
                    check = true;
                }
                else {
                    check = false;
                }
                if(mon ==  1)
                    btnPrevC.setVisibility(View.INVISIBLE);
                else
                    btnPrevC.setVisibility(View.VISIBLE);
                if(mon ==  Integer.parseInt(curMonthFormat.format(date)) - 1 || mon == 12)
                    btnNextC.setVisibility(View.INVISIBLE);
                dayList.clear();
                dayList.add("일");
                dayList.add("월");
                dayList.add("화");
                dayList.add("수");
                dayList.add("목");
                dayList.add("금");
                dayList.add("토");
                //mCal = Calendar.getInstance();
                mCal.set(Integer.parseInt(curYearFormat.format(date)), ++mon, 1);

                tvDate.setText(curYearFormat.format(date) + "/" + (mon < 10? "0" : "") + (mon+1));
                int dayNum = mCal.get(Calendar.DAY_OF_WEEK);
                //1일 - 요일 매칭 시키기 위해 공백 add
                for (int i = 1; i < dayNum; i++) {
                    dayList.add("");
                }
                setCalendarDate(mCal.get(Calendar.MONTH) +1);
                gridAdapter.notifyDataSetChanged();
            }

        });
    }

    // 해당 월에 표시할 일 수 구함
    private void setCalendarDate(int month) {
        mCal = Calendar.getInstance();
        //오늘 day 가져옴
        Integer today = mCal.get(Calendar.DAY_OF_MONTH);
        mCal.set(Calendar.MONTH, month - 1);
        for (int i = 0; i < mCal.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
            if((i+1)==today) {
                dayList.add("" + (i + 1));
            }
            else{
                if(day[month-1][i]==1)
                    dayList.add("" + (i + 1)+"\n결석");
                else if(day[month-1][i]==2)
                    dayList.add("" + (i + 1)+"\n지각");
                else if(day[month-1][i]==3)
                    dayList.add("" + (i + 1)+"\n출석");
                else if(day[month-1][i]==4)
                    dayList.add("" + (i + 1)+"\n임시출석");
                else
                    dayList.add("" + (i + 1));
            }
        }
    }

    //그리드뷰 어댑터
    private class GridAdapter extends BaseAdapter {
        private final List<String> list;
        private final LayoutInflater inflater;

        // 생성자
        public GridAdapter(Context context, List<String> list) {
            this.list = list;
            this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public String getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            //if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_calendar_gridview, parent, false);
                holder = new ViewHolder();
                holder.tvItemGridView = (TextView)convertView.findViewById(R.id.tv_item_gridview);
                convertView.setTag(holder);
           /* } else {
                convertView = inflater.inflate(R.layout.item_calendar_gridview, parent, false);
                holder = new ViewHolder();
                holder.tvItemGridView = (TextView)convertView.findViewById(R.id.tv_item_gridview);
                convertView.setTag(holder);

                //holder = (ViewHolder)convertView.getTag();
            }*/
            holder.tvItemGridView.setText("" + getItem(position));
            //해당 날짜 텍스트 컬러,배경 변경
            mCal = Calendar.getInstance();
            //오늘 day 가져옴
            Integer today = mCal.get(Calendar.DAY_OF_MONTH);
            Integer tomonth = mCal.get(Calendar.MONTH);
            tomonth =tomonth+1;
            String sToday = String.valueOf(today);
            String sTomonth = String.valueOf(tomonth);

            //Log.d("Calendar ", "position : " + position);
            //Log.d("Calendar ", "sToday : " + getItem(position));
            if(check == true){
                if (sToday.equals(getItem(position))) { //오늘 day 텍스트 컬러 변경
                    holder.tvItemGridView.setTextColor(getResources().getColor(R.color.calendarBlack));
                    if (temp == 1) {
                        holder.tvItemGridView.setText("" + getItem(position) + "\n결석");
                    } else if (temp == 2) {
                        holder.tvItemGridView.setText("" + getItem(position) + "\n지각");
                    } else if (temp == 3) {
                        holder.tvItemGridView.setText("" + getItem(position) + "\n출석");
                    } else if (temp == 4) {
                        holder.tvItemGridView.setText("" + getItem(position) + "\n임시출석");
                    } else {
                        holder.tvItemGridView.setText("" + getItem(position));
                    }

                    check=false;
                }
             }

            return convertView;


        }
    }

    private class ViewHolder {
        TextView tvItemGridView;
    }

}