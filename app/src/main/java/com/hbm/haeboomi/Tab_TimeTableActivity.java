package com.hbm.haeboomi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Tab_TimeTableActivity extends Activity {
    private WebView web;
    private String source;
    private Handler handler;
    private BackPressCloseHandler bpch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_time_table);

        bpch = new BackPressCloseHandler(this);

        Intent intent = getIntent();
        final String stuNum = intent.getStringExtra("student_number");
        final String password = intent.getStringExtra("password");

        //UI를 건드리는 코드는 스레드 내부가 아닌 핸들러로 처리해야 한다.
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                TextView txt = (TextView)findViewById(R.id.txtWait);
                txt.setVisibility(View.INVISIBLE);
                //WebView를 source의 코드로 불러옴, UTF-8로 인코딩
                web.loadData(source, "text/html; charset=UTF-8;", null);
                web.setVisibility(View.VISIBLE);
            }
        };

        web = (WebView)findViewById(R.id.timeTable);
        //줌이 가능하게함
        web.getSettings().setBuiltInZoomControls(true);
        //실행하면 화면에 꽉 차게 보여주는코드 / 1로 설정해야 시작하자마자 꽉차게보임
        web.getSettings().setUseWideViewPort(true);
        web.setInitialScale(1);

        new Thread() {
            @Override
            public void run() {
                source = DownloadHtml("http://128.199.144.167:8080/AnalyzeApp/hallym/login?action=5&id=" + stuNum + "&password=" + password);
                //현상이가 오류났던부분 / 길이가 0일 경우에서 코드를 자르려해서 오류가 났기 때문에 조건하나넣음
                if(source.length() != 0) {
                    //코드 정리
                    source = source.substring(23, source.length() - 2);
                    source = source.replace("\\n", " ");
                    source = source.replace("\\\"", "\"");
                    //web.loadData(source, "text/html; charset=UTF-8;", null); 코드를 핸들러에서 실행함
                    handler.sendMessage(handler.obtainMessage());
                }
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        bpch.onBackPressed();
    }

    //해당 URL로 text/html 형태의 코드를 얻음
    String DownloadHtml(String addr) {
        StringBuilder html = new StringBuilder();
        try {
            URL url = new URL(addr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn != null) {
                conn.setConnectTimeout(10000);
                conn.setUseCaches(false);
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    while(true) {
                        String line = br.readLine();
                        if (line == null) break;
                        html.append(line + '\n');
                    }
                    br.close();
                }
                conn.disconnect();
            }
        } catch (Exception ex) {
        }

        return html.toString();
    }
}
