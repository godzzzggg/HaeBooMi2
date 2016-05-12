package com.hbm.haeboomi;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DBManager {
    private final String TAG = "DBManager";
    private static final String SERVER_ADDRESS = "http://59.30.254.247:8080";
    private Activity activity;

    public DBManager(Activity activity) {
        super();
        this.activity = activity;
    }
    public String DBLogin(String id, String pw, String division) {
        String returnVal = "fail";
        LoginAsync la = new LoginAsync();
        try {
             returnVal = la.execute(id, pw, division).get();
        }catch(InterruptedException e) {}
        catch (ExecutionException e) {}
        return returnVal;
    }
    public void DBRegister(String id, String pw, int passindex, int division) {
        URL url;
        try {
            if(division == 0) {
                String u = SERVER_ADDRESS + "/student_insert.php?" + "st_id=" + id
                        + "&st_pw=" + pw + "&st_pass=" + passindex;
                url = new URL(u);
            }
            else {
                String u = SERVER_ADDRESS + "/professor_insert.php?" + "pr_id=" + id
                        + "&pr_pw=" + pw;
                url = new URL(u);
            }
            url.openStream();
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
    }
    class LoginAsync extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String id = params[0];
            String pw = params[1];
            String division = params[2].equals("0")? "/student_login.php" : "/professor_login.php";

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
                result = sb.toString();
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
        protected void onPostExecute(String rst) {
            String s = rst.trim();
            ((LoginActivity)activity).handlerRun(1);
            if(s.equalsIgnoreCase("success")){
                Intent intent = new Intent(activity, StudentMainActivity.class);
                activity.finish();
                activity.startActivity(intent);
            }else {
                Toast.makeText(activity, "학번 혹은 비밀번호를 확인해 주세요", Toast.LENGTH_LONG).show();
            }
        }
    }
}
