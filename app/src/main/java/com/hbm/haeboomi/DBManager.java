package com.hbm.haeboomi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DBManager {
	private static final String TAG = "EndHBM_DBManager";
	private Activity activity;

	public static final String SERVER_ADDRESS = "http://59.30.254.247:8080/";

	//getData
	private String[][] table = {
			{"atd_class", "atd_cname", "atd_id", "atd_check", "atd_divide", "atd_date"},
			{"bc_sn", "bc_classno"},
			{"pr_id", "pr_pw", "st_name"},
			{"ps_id", "ps_devide", "ps_subject", "ps_time", "ps_day", "ps_5075"},
			{"st_id", "st_pw", "st_pass", "st_name", "st_dept", "st_year"},
			{"ss_id", "ss_cname", "ss_proname", "ss_devide", "ss_subject", "ss_time", "ss_classno", "ss_day", "ss_5075"}
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
		URL url;
		String data = null;

		try {
			if(division == 0) {
				String u = SERVER_ADDRESS + "student_insert.php?" + "st_id=" + id
						+ "&st_pw=" + pw + "&st_pass=" + passindex;
				url = new URL(u);
			}
			else {
				String u = SERVER_ADDRESS + "professor_insert.php?" + "pr_id=" + id
						+ "&pr_pw=" + pw;
				url = new URL(u);
			}
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			BufferedReader buffer = new BufferedReader(new InputStreamReader(con.getInputStream()));

			data = buffer.readLine();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}

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
		String url = null;
		String pattern = "= ,";
		String word = null;
		for(int i = 0; i < pattern.length(); i++) {
			char p = pattern.charAt(i);
			String[] temp = str.split(String.valueOf(p));
			word = temp[0];
			for (int j = 1; j < temp.length; j++) {
				if(p == ' ')
					word += "+" + temp[j];
				else
					word += "%" + Integer.toHexString(p) + temp[j];
			}
			url += word;
		}
		return url;
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
		@Override
		protected String doInBackground(String... params) {
			String id = params[0];
			String pw = params[1];
			String division = params[2].equals("0")? "student_login.php" : "professor_login.php";

			InputStream is = null;
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("st_id", id));
			nameValuePairs.add(new BasicNameValuePair("st_pw", pw));
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
			if(activity instanceof LoginActivity)
				((LoginActivity)activity).handlerRun(1);	//processDialog 닫기
			if(result.equalsIgnoreCase("success")) {	//로그인 성공
				activity.finish();
				activity.startActivity(new Intent(activity, StudentMainActivity.class));
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
			db.execSQL("CREATE TABLE user (id VARCHAR(8), password VARCHAR(15))");
		}
		@Override	//이미 배포했던 db에 변경이 있을경우 호출/ 주로 버젼 변경시 호출됨
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
		//질의 실행
		public void execSQL(String sql) {
			db.execSQL(sql);
		}
		public String getData(String query) {
			StringBuilder sb = new StringBuilder();
			Cursor cs = db.rawQuery(query, null);

			if(cs.getCount() > 0) {
				while (cs.moveToNext()) {
					sb.append(cs.getString(0) + "!" + cs.getString(1));
				}
			}

			return sb.toString();
		}
		public void onDestroy() {
			db.close();
		}
	}
}
