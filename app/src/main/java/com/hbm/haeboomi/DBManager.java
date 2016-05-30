package com.hbm.haeboomi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

//php서버와 접근
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DBManager {
	private static final String TAG = "EndHBM_DBManager";
	private Activity activity;

	public static final String SERVER_ADDRESS = "http://hbmi.tk/";

	//getData
	private String[][] table = {
			{"id", "cname", "rst", "divide", "date", "day", "time", "chk_time"},
			{"mac", "classno"},
			{"id", "pw", "pro_name"},
			{"id", "divide", "cname", "time", "day", "5075"},
			{"id", "pw", "pass", "stu_name", "dept", "year"},
			{"id", "day", "time", "cname", "pro_name", "divide", "classno", "building", "5075"}
	};

	public DBManager(Activity activity) {
		super();
		this.activity = activity;
	}

	public void DBLogin(String id, String pw, String division) {
		LoginAsync la = new LoginAsync();
		la.execute(id, pw, division);
	}

	//가입성공 : true | 가입실패 : false
	public boolean DBRegister(String id, String pw, int passindex, int division) {
		String u = null;
		String data = null;
		int year = 0;

		u = "http://was1.hallym.ac.kr:8084/Haksa_u/menu/main.jsp?UserID=" + id + "&Password=" + pw;
		try {
			Document doc = Jsoup.parse(new URL(u).openStream(), "EUC-KR", u);
			doc.outputSettings().charset();
			Elements elements = doc.select("td[style]");
			String str = elements.iterator().next().text();
			year = str.charAt(str.indexOf("(") + 1) - 48;   //학년을 구한다.
		}catch(MalformedURLException e) {}
		catch(IOException e) {}
		catch(StringIndexOutOfBoundsException e) {
			return false;
		}

		if(division == 0)
			u = SERVER_ADDRESS + "student_insert.php?st_id=" + id + "&st_pw=" + pw + "&st_pass=" + passindex + "&year=" + year;
		else
			u = SERVER_ADDRESS + "professor_insert.php?pr_id=" + id + "&pr_pw=" + pw;
		data = SuccessFail(u);

		return Boolean.parseBoolean(data);
	}

	/**
	 *                          테이블명
	 * ex) String str = getData(DBManager.GetTable.STUDENT);
	 * 결과예시 : str : 20115169!1234!-1!null!null!null
	 * 위 결과를 String[] 배열명 = str.split("!"); 으로 분해하여 사용한다.
	 */
	public String getData(int tableName) {
		GetDataJSON g = new GetDataJSON();
		String url = DBManager.SERVER_ADDRESS + "getdata.php?index=" + tableName;
		try {	//아래의 getList()를 실행
			return getList(g.execute(url).get(), tableName);	//g.execute(url).get() 수행시 파싱된 JSON 결과를 얻을 수 있다
		}catch(InterruptedException e) {}
		catch (ExecutionException e) {}
		return null;
	}
	/**
	 * 간단한 select 질의 수행 가능
	 * ex String data = getSelectData("*", "student", null or "st_id = 20115169", DBManager.GetTable.STUDENT);
	 *                          select * from student (where st_id = 20115169)
	 */
	public String getSelectData(String select, String from, String where, int index) {
		GetDataJSON g = new GetDataJSON();
		String url = DBManager.SERVER_ADDRESS + "getdataq.php?index=" + index;

		url += "&select=" + parsePHP(select);
		url += "&from=" + parsePHP(from);
		if(where != null)
			url += "&where=" + parsePHP(where);

		try {	//아래의 getList()를 실행
			return getList(g.execute(url).get(), index);	//g.execute(url).get() 수행시 파싱된 JSON 결과를 얻을 수 있다
		}catch(InterruptedException e) {}
		catch (ExecutionException e) {}

		return null;
	}
	private String getList(String rst, int index) {
		try {
			JSONObject jsonObj = new JSONObject(rst);
			JSONArray stu = jsonObj.getJSONArray("result");

			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < stu.length(); i++) {
				JSONObject c = stu.getJSONObject(i);
				for(int j = 0; j < table[index].length; j++)
					if(!c.isNull(table[index][j]))
						sb.append(c.getString(table[index][j]) + "!");
				sb.append("\n");
			}
			return sb.toString();
		}catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	private String parsePHP(String str) {
		String url = str;
		String pattern = "= ,`:'";
		String word = null;
		for(int i = 0; i < pattern.length(); i++) {
			boolean change = false;
			char p = pattern.charAt(i);
			String[] temp = url.split(String.valueOf(p));
			word = temp[0];
			for (int j = 1; j < temp.length; j++) {
				change = true;
				if(p == ' ')
					word += "+" + temp[j];
				else
					word += "%" + Integer.toHexString(p) + temp[j];
			}
			if(change)
				url = word;
		}
		return url;
	}

	public boolean attendance(String id, String date, int day, String time, String c, String result, String chk_time) {
		try {
			String u = SERVER_ADDRESS + "attendance.php?" + "st_id=" + id + "&day=" + day + "&time=" + parsePHP(time) + "&class=" + c;
			if(SuccessFail(u).equalsIgnoreCase("success")) {    //시간표와 비교하여 해당시간에 강의가 있는지 확인
				String[] temp = getSelectData("cname, divide", "st_schedule", "id=" + id + " and time='" + parsePHP(time) + "'" + " and day=" + day, GetTable.ST_SCHEDULE).split("!");
				String[] schedule = new String[2];  //과목명과 분반정보를 얻어옴
				for (int i = 0, j = 0; j < schedule.length && i < temp.length; i++)
					if(temp[i] != null)
						schedule[j++] = temp[i];

				u = SERVER_ADDRESS + "attendance_insert.php?" + "id=" + id + "&cname=" + schedule[0] + "&divide=" + schedule[1]
						+ "&day=" + day + "&time=" + parsePHP(time) + "&date=" + date + "&result=" + result + "&chk_time=" + chk_time;

				if(SuccessFail(u).equalsIgnoreCase("success"))
					return true;
				else
					Log.e(TAG, "이미 출석 완료함");
			}
			else
				return false;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return false;
	}
	public String[] nowTime() {
		GetDataJSON json = new GetDataJSON();
		String[] data = null;
		try {
			//날짜, 시간이 각각 들어간다
			data = json.execute(SERVER_ADDRESS + "nowtime.php").get().split("!");
		}catch(InterruptedException e) {}
		catch(ExecutionException e) {}
		return data;
	}
	public String SuccessFail(String u) {
		StringBuilder sb = new StringBuilder();
		try {
			URL url = new URL(u);
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "text/plain;charset=UTF-8");
			con.setDoInput(true);
			con.setDoOutput(true);
			BufferedReader buffer = new BufferedReader(new InputStreamReader(con.getInputStream()));

			String data = null;
			while((data = buffer.readLine()) != null)
				sb.append(data + "\n");

			buffer.close();
			con.disconnect();
		}catch(Exception e) {
			Log.e(TAG, e.getMessage());
			return null;
		}
		return sb.toString().trim();
	}
	public void putSchedule() {
		try {
			innerDB innerDB = new innerDB(activity);
			String[] idpw = innerDB.getData().split("!");
			String u = "http://128.199.144.167:8080/AnalyzeApp/hallym/login?action=5&id=" + idpw[0] + "&password=" + idpw[1];
			String data = null, temp = SuccessFail(u);

			if(temp != null && temp.length() > 60) {
				//코드 정리
				temp = temp.substring(23, temp.length() - 2);
				temp = temp.replace("\\n", " ");
				temp = temp.replace("\\", "");
				data = temp;
			}
			Document doc = Jsoup.parse(data);   //HTML 데이터를 파싱
			String str = doc.toString().replaceAll("&nbsp;", "");   //32번 띄어쓰기가 아닌 160번 띄어쓰기를 모두 삭제
			doc = Jsoup.parse(str); //띄어쓰기를 모두 삭제하고 다시 파싱
			Elements elements = doc.select("table[width$=570] tbody tr td");    //학번, 성명, 학부, 학과를 가져오기위해 셀렉트함수를 사용
			String[] array = new String[4]; //학번, 성명, 학부, 학과를 저장하기위한 배열

			for(Element e : elements) { //각 엘리먼트들을 탐색
				e.select("span.ltd12").remove();    //<span class="ltd12" > 인것을 제외("YYYY년도 X학기 수업시간표"를 제외시킴)
				Iterator<Element> iter = e.getAllElements().iterator(); //반복자로 셀렉트 함수로 뽑아온 모든 엘리먼트를 가져옴
				String[] t = iter.next().text().replace(" : ", ":").replace(" ", ":").split(":");   //빈칸을 제거하고 ':'으로 쪼갬

				for(int i = 1, j = 0; i < t.length; i+=2)   //t[] 1, 3, 5, 7 번 인덱스에 학번, 성명, 학부, 학과가 저장되어있으므로 +=2씩 해주며 array에 저장
					if(t[i].length() != 0)
						array[j++] = t[i];
			}
			//학생의 정보를 갱신, 이름과 학과정보를 넣어준다.
			u = SERVER_ADDRESS + "update_student.php?id=" + idpw[0] + "&name=" + array[1] + "&dept=" + array[3];
			if(SuccessFail(u).equalsIgnoreCase("success")) {    //갱신이 성공하면
				elements = doc.select("tr td.small");   //각 테이블의 tr <td class="small">들을 모두 가져옴
				StringBuilder sb = new StringBuilder();
				for (Element e : elements) {
					e.select("strong").remove();    //<strong>X교시</strong>을 모두 제거
					e.select("span.small1").remove();   //09:00 ~ 09:50을 모두 제거
					Iterator<Element> iter = e.getAllElements().iterator(); //선택한 엘리먼트들을 반복자로 가져옴
					while(iter.hasNext()) { //가져올 값이 있으면 반복
						String text = iter.next().text();   //텍스트를 가져옴
						sb.append(text.length() == 0? " " : text + "!");    //텍스트에 아무 값이 없으면 공백(공백 1개가 테이블에 한 칸을 의미)을 넣고 아니라면 텍스트를 넣음. !는 구분자
					}
				}
				String[] s = sb.toString().split("!");  //'!'로 문자열을 분리
				String[][] ary = new String[16][];
				for(int i = 0; i < ary.length;) {   //해당 과목의 시간정보        index :   0   1   2   3   4   5   6   7  8
					ary[i++] = new String[9];   //[1, A], [4, C], [7, E], [10, G]       | [ 1, 50, 50, 75, 50, 50, 75, 50, A]
					ary[i++] = new String[6];   // 2, 5, 8, 11                          | [ 2, 50, 50, 50, 50, 50]
					ary[i++] = new String[3];   // B, D, F, H                           | [75, 75, B]
					ary[i++] = new String[6];   // 3, 6, 9, 12                          | [ 3, 50, 50, 50, 50, 50]
				}
				for(int i = 0, si = 0, sj = 0; i < ary.length; i++) {
					for (int j = 0; j < ary[i].length; j++) {
						char c = s[si].charAt(sj);  //문자 하나를 받아서 검사
						if (c == ' ') { //해당 문자가 공백이면
							ary[i][j] = String.valueOf(c);  //공백을 넣어주고 다음문자를 가리킴
							sj++;
						}
						else {          //공백이 아니면
							ary[i][j] = s[si++].substring(sj);  //이후부터 모든 문자열을 넣어줌
							sj = 0;
						}
						String[] subject = ary[i][j].split("/");    //강의명/건물명/호수
						if(subject.length > 1) {    //공백이 아닐 때
							int day = 0;
							if(i%2 == 1) {  //1, 3(2, 3, 5, 6, 8, 9, 11, 12교시)
								day = j;    //1 ~ 5(월 ~ 금)
							}
							else {      //0, 2
								if(i%4 == 0)
									switch(j) {
										case 1: //월
											day = 1;
											break;
										case 2:
										case 3: //화
											day = 2;
											break;
										case 4: //수
											day = 3;
											break;
										case 5:
										case 6: //목
											day = 4;
											break;
										case 7: //금
											day = 5;
											break;
									}
								else {  //화요일, 목요일 75분수업
									if (j == 0)     //화요일
										day = 2;
									else if (j == 1)    //목요일
										day = 4;
								}
							}
							String time = null;
							switch(i%4) {
								case 0:
									switch(i/4) {
										case 0:
											time = "09:00:00";
											break;
										case 1:
											time = "12:00:00";
											break;
										case 2:
											time = "15:00:00";
											break;
										case 3:
											time = "18:00:00";
											break;
									}
									break;
								case 1:
									switch(i/4) {
										case 0:
											time = "10:00:00";
											break;
										case 1:
											time = "13:00:00";
											break;
										case 2:
											time = "16:00:00";
											break;
										case 3:
											time = "19:00:00";
											break;
									}
									break;
								case 2:
									switch(i/4) {
										case 0:
											time = "10:30:00";
											break;
										case 1:
											time = "13:30:00";
											break;
										case 2:
											time = "16:30:00";
											break;
										case 3:
											time = "19:30:00";
											break;
									}
									break;
								case 3:
									switch(i/4) {
										case 0:
											time = "11:00:00";
											break;
										case 1:
											time = "14:00:00";
											break;
										case 2:
											time = "17:00:00";
											break;
										case 3:
											time = "20:00:00";
											break;
									}
									break;
							}
							boolean gubun = false;
							switch(i%4) {
								case 0:
									switch(j) {
										case 3:
										case 6:
											gubun = true;
											break;
									}
									break;
								case 2:
									gubun = true;
									break;
							}
							u = SERVER_ADDRESS + "schedule_insert.php?id=" + idpw[0] + "&cname=" + parsePHP(subject[0]) + "&time=" + parsePHP(time)
									+ "&classno=" + subject[2] + "&building=" + parsePHP(subject[1]) + "&day=" + day + "&gubun=" + (gubun?"1":"0"); //1 : 75분수업, 0 : 50분수업

							if(SuccessFail(u).equalsIgnoreCase("fail")) {
								Log.d(TAG, "insert 실패");
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class GetTable {
		public static int ATTENDANCE = 0;
		public static int BEACON = 1;
		public static int PROFESSOR = 2;
		public static int PRO_SCHEDULE = 3;
		public static int STUDENT = 4;
		public static int ST_SCHEDULE = 5;
	}
	private class LoginAsync extends AsyncTask<String, Void, String> {
		private String stuNum;
		private String password;
		@Override
		protected String doInBackground(String... params) {
			String id = params[0];
			String pw = params[1];
			String division = params[2].equals("0")? "student_login.php" : "professor_login.php";

			InputStream is = null;
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair(division.charAt(0)=='s'? "st_id" : "pr_id", id));
			nameValuePairs.add(new BasicNameValuePair(division.charAt(0)=='s'? "st_pw" : "pr_pw", pw));
			String result = null;

			try{
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(SERVER_ADDRESS + division);
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				HttpResponse response = httpClient.execute(httpPost);

				HttpEntity entity = response.getEntity();

				is = entity.getContent();

				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
				StringBuilder sb = new StringBuilder();

				String line = null;
				while ((line = reader.readLine()) != null)
				{
					sb.append(line + "\n");
				}
				result = sb.toString().trim();
				stuNum = id;
				password = pw;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}
		@Override
		protected void onPostExecute(String result) {
			if(activity instanceof LoginActivity)   //형변환이 가능하면
				((LoginActivity)activity).handlerRun(1);	//processDialog 닫기
			if(result.equalsIgnoreCase("success")) {	//로그인 성공
				if(activity instanceof LoginActivity)   //형변환이 가능하면
					((LoginActivity)activity).saveInfo(stuNum, password);
				if(stuNum != null & stuNum.length() == 8) {
					activity.finish();
					activity.startActivity(new Intent(activity, StudentMainActivity.class));
				}
				else {
					activity.finish();
					activity.startActivity(new Intent(activity, ProfessorMainActivity.class));
				}
			}
			else {
				Toast.makeText(activity, "학번 혹은 비밀번호를 확인해 주세요", Toast.LENGTH_LONG).show();
			}
		}
	}
	private class GetDataJSON extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			String uri = params[0];

			BufferedReader bufferedReader = null;
			try {
				URL url = new URL(uri);
				HttpURLConnection con = (HttpURLConnection)url.openConnection();
				con.setRequestMethod("POST");
				con.setDoOutput(true);
				con.setDoInput(true);
				StringBuilder sb = new StringBuilder();

				bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

				String json;
				while((json = bufferedReader.readLine()) != null) {
					sb.append(json + "\n");
				}

				return sb.toString().trim();
			}catch(Exception e) {
				return null;
			}
		}
	}

	//자신의 핸드폰의 내부 DB에 아이디와 비밀번호를 저장하여 자동로그인을 수행
	public static class innerDB extends SQLiteOpenHelper {
		private SQLiteDatabase db;

		public innerDB(Context context) {
			super(context, "data.sqlite", null, 1);//context, DB명, CursorFactory, SQLite 버젼

			//생성자 호출만으로는 DB 구축이 안된다.
			//실제 DB 구축은 getWritableDatabase()메서드를 호출하는 시점에 구축된다.

			//스마트폰 단말기 내의 data/data/database 경로에 파일이 만들어진다.
			db = this.getWritableDatabase();
		}
		@Override	//db가 새로 만들어질 때 1회 호출
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE user (id VARCHAR(8), password VARCHAR(15), PRIMARY KEY(id, password))");
		}
		@Override	//이미 배포했던 db에 변경이 있을경우 호출/ 주로 버젼 변경시 호출됨
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
		//질의 실행
		public boolean execSQL(String sql) {
			try {
				db.execSQL(sql);
				return true;
			}catch(SQLiteConstraintException e) {
				return false;
			}
		}
		public String getData() {
			StringBuilder sb = new StringBuilder();
			Cursor cs = db.rawQuery("select * from user", null);

			if(cs.getCount() > 0) {
				while (cs.moveToNext()) {
					sb.append(cs.getString(0) + "!" + cs.getString(1) + "!");
				}
			}

			return sb.toString();
		}
		public void onDestroy() {
			db.close();
		}
	}
}
