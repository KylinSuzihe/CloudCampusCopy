package com.example.cc;

import static java.lang.Double.max;
import static java.lang.Double.min;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;

public class PingActivity extends AppCompatActivity {

    private Button pingStart;
    private Button pingStop;
    private Button gateway;
    private Button DNS;
    private Button baidu;
    private AutoCompleteTextView ipAddr;
    private static List<Receive> reciveList = new ArrayList<>();
    private static TextView textView;
    private static boolean is_pinging = false;
    private ImageButton history;
    private ImageButton setting;
    private static ListView listView;

    private static DatabaseHelper dbHelper = null;

    private static Context context = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping);

        context = getApplicationContext();
        dbHelper = new DatabaseHelper(context);
        dbHelper.getWritableDatabase();

        //获取控件
        getButton();
        //绑定监听器
        addListener();
    }
    private void getButton() {
        pingStart = findViewById(R.id.ping_btn);
        pingStop = findViewById(R.id.stop_ping);
        gateway = findViewById(R.id.gateway);
        DNS = findViewById(R.id.dns);
        baidu = findViewById(R.id.baidu);
        ipAddr = findViewById(R.id.input_ip);
        textView = findViewById(R.id.answer);
        history = findViewById(R.id.history_ping);
        setting = findViewById(R.id.setting_ping);
        listView = findViewById(R.id.lv);
    }
    private void addListener() {
        pingStart.setOnClickListener(startPing);
        pingStop.setOnClickListener(endPing);

        baidu.setOnClickListener(baiduListener);
        DNS.setOnClickListener(dnsListener);
        gateway.setOnClickListener(gatewayListener);

        history.setOnClickListener(historyListener);
        setting.setOnClickListener(settingListener);
    }
    private void toStart() {
        pingStart.setVisibility(View.GONE);
        pingStop.setVisibility(View.VISIBLE);
        is_pinging = true;
        textView.setText("");
        listView.setAdapter(null);
    }

    private View.OnClickListener  startPing = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            toStart();
            String ip = ipAddr.toString();
            getPingValue(ip, false);
        }
    };
    private View.OnClickListener  endPing = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            pingStart.setVisibility(View.VISIBLE);
            pingStop.setVisibility(View.GONE);
            is_pinging = false;
        }

    };

    private View.OnClickListener baiduListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            toStart();
            ipAddr.setText("www.baidu.com");
            getPingValue("39.156.66.18", false);
        }
    };

    private View.OnClickListener dnsListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            toStart();
            String ip = DnsUtil.getDns(context);
            ipAddr.setText(ip);
            getPingValue(ip, false);
        }
    };

    private View.OnClickListener gatewayListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            toStart();
            String ip = GateWayUtil.getGateWay(context);
            ipAddr.setText(ip);
            getPingValue(ip, false);
        }
    };

    private View.OnClickListener historyListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent historyIntent = new Intent(PingActivity.this, HistoryActivity.class);
            historyIntent.putExtra("type", "ping");
            startActivity(historyIntent);
        }
    };
    private View.OnClickListener settingListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent settingIntent = new Intent(PingActivity.this, SettingActivity.class);
            startActivity(settingIntent);
        }
    };

    private static void getDB() {
        Cursor cursor=dbHelper.queryOne("ping");
        String[] from={"ipaddr", "ping_start_time", "ping_run_time","score","sendPkgNum", "receivePkgNum", "packetLossNum", "minRTT", "maxRTT", "meanRTT"};
        int[] to={R.id.destination, R.id.ping_start_time, R.id.ping_run_time, R.id.score, R.id.sendPkgNum, R.id.receivePkgNum, R.id.packetLossNum, R.id.minRTT, R.id.maxRTT, R.id.meanRTT};
        SimpleCursorAdapter scadapter=new SimpleCursorAdapter(context,R.layout.ping_statistic,cursor,from,to);
        listView.setAdapter(scadapter);
    }

    private static final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Bundle bundleCode = msg.getData();
                    String answer = bundleCode.getString("answer");
                    textView.append(answer);
                    break;
                case 2:
                    Bundle bundle = msg.getData();
                    String time = bundle.getString("time");
                    String grade = bundle.getString("grade");
                    String send = bundle.getString("send");
                    String receive = bundle.getString("receive");
                    String lost = bundle.getString("lost");
                    String minRTT = bundle.getString("minRTT");
                    String maxRTT = bundle.getString("maxRTT");
                    String avgRTT = bundle.getString("avgRTT");
                    String ipaddr = bundle.getString("ipaddr");
                    SimpleDateFormat formatter   =   new   SimpleDateFormat   ("yyyy-MM-dd HH:mm:ss");
                    Date curDate =  new Date(System.currentTimeMillis());
                    String   str   =   formatter.format(curDate);
                    final ContentValues values=new ContentValues();
                    values.put("ping_run_time",time);
                    values.put("score",grade);
                    values.put("sendPkgNum",send);
                    values.put("receivePkgNum",receive);
                    values.put("packetLossNum",lost);
                    values.put("minRTT",minRTT);
                    values.put("maxRTT",maxRTT);
                    values.put("meanRTT",avgRTT);
                    values.put("ipaddr", ipaddr);
                    values.put("ping_start_time", str);
                    dbHelper.insert(values, "ping");
                    String result = String.format("Time: %s Grade: %s Send: %s Receive: %s Lost: %s minRTT: %s maxRTT: %s avgRTT: %s", time, grade, send, receive, lost, minRTT, maxRTT, avgRTT);
                    textView.append(result);
                    getDB();
//                    Cursor cursor=dbHelper.query();
//                    String[] from={"_id","time","grade","send", "receive", "lost", "minRTT", "maxRTT", "avgRTT"};
//                    int[] to={R.id.ping_run_time, R.id.score, R.id.sent, R.id.received, R.id.lossRate, R.id.minRTT, R.id.maxRTT, R.id.meanRTT};
//                    SimpleCursorAdapter scadapter=new SimpleCursorAdapter(this,R.layout.ping_statistic,cursor,from,to);
//                    listView.setAdapter(scadapter);
                default:
                    break;
            }
        }
    };

    public static double stringtoDouble(String s) {
        int i = 0;
        while (i < s.length() && (s.charAt(i) < '0' || s.charAt(i) > '9')) i++;
        int x = 0;
        while (i < s.length() && s.charAt(i) >= '0' && s.charAt(i) <= '9') {
            x = x * 10 + (s.charAt(i) - '0');
            i++;
        }
        int y = 0;
        int cnt = 1;
        if (i < s.length() && s.charAt(i) == '.') {
            i++;
            while (i < s.length() && s.charAt(i) >= '0' && s.charAt(i) <= '9') {
                y = y * 10 + (s.charAt(i) - '0');
                cnt *= 10;
                i++;
            }
        }
        return 1.0 * x + 1.0 * y / cnt;
    }

    private static String countTime(long usedTime) {
        String time = "";
        int h = (int) (usedTime / 60 / 60);
        if (h < 10) time += "0" + h;
        else time += h;
        usedTime %= 60 * 60;
        time += ":";
        int m = (int) (usedTime / 60);
        if (m < 10) time += "0" + m;
        else time += m;
        time += ":";
        usedTime %= 60;
        if (usedTime < 10) time += "0" + usedTime;
        else time += usedTime;
        return time;
    }
    private static void sendMessage(String strRet, int num) {
        String[] s = strRet.split(" ");

        Receive recive = new Receive();
        recive.bytes = s[0];
        recive.from = s[3].substring(0, s[3].length() - 1);
        recive.ttl = s[5];
        recive.time = s[6].substring(5);
        reciveList.add(recive);
        String answer =  num + ".来自" + recive.from + "的回复：\n";
        answer += "字节=" + recive.bytes + ",时间=" + recive.time + "，" + recive.ttl + "\n";

        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("answer", answer);
        msg.setData(bundle);
        msg.what = 1;
        handler.sendMessage(msg);
    }


    public static int getPingValue(String ip, boolean bResetPing) {

        final int[] m_pingVal = new int[1];
        if (bResetPing)	{ m_pingVal[0] = -1; }

        try {
            // 创建proc子进程去执行ping命令
            String tmpStr = "/system/bin/ping -W 10 -c 10 " + ip;
//            String cmdStr = tmpStr.concat(ip);
//            Timer timer = new Timer();
            Process proc = Runtime.getRuntime().exec(tmpStr);

            // 获取输出流
            final InputStream is1 = proc.getInputStream();

            new Thread() {
                public void run() {
                    System.out.println("线程一ing……");
                    long startTime =  System.currentTimeMillis();
                    BufferedReader buf = new BufferedReader(new InputStreamReader(is1));

                    try {
                        // 解析输出流
                        String strRet = null;
                        Integer num = 0, cnt = -1;
                        reciveList.clear();
                        while ((strRet = buf.readLine()) != null && is_pinging) {
                            if (!strRet.contains("---")) cnt++;
                            System.out.println("--------------Input: " + strRet);
                            // 64 bytes from 39.156.66.18: icmp_seq=11 ttl=50 time=23.3 ms

                            if (strRet.contains("time=")) {
                                sendMessage(strRet, num);
                                num++;
                            }
                        }

                        long endTime =  System.currentTimeMillis();
                        long usedTime = (endTime-startTime)/1000;

                        String time = countTime(usedTime);
                        double minRTT = 1000.0;
                        double maxRTT = 0.0;
                        double avgRTT = 0.0;
                        double lost = 1 - 1.0 * num / cnt;
                        System.out.println(String.format("num : %d, cnr : %d",  num, cnt));
                        for (int i = 0; i < reciveList.size(); ++i) {
                            double _time = stringtoDouble(reciveList.get(i).time);
                            System.out.println("time: " + reciveList.get(i).time + "  -  " + time);
                            minRTT = min(minRTT, _time);
                            maxRTT = max(maxRTT, _time);
                            avgRTT += _time;
                        }
                        avgRTT /= reciveList.size();
                        Random random = new Random();
                        int grade = random.nextInt(40) + 60;

                        System.out.println(minRTT + " " + maxRTT + " " + avgRTT + " " + lost);
                        DecimalFormat decimalFormat =new DecimalFormat("0.00");
                        Message msg = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("minRTT", decimalFormat.format(minRTT));
                        bundle.putString("maxRTT", decimalFormat.format(maxRTT));
                        bundle.putString("avgRTT", decimalFormat.format(avgRTT));
                        bundle.putString("lost", String.valueOf(lost));
                        bundle.putString("send", String.valueOf(cnt));
                        bundle.putString("receive", String.valueOf(num));
                        bundle.putString("time", time);
                        bundle.putString("grade", String.valueOf(grade));
                        bundle.putString("ipaddr", ip);
                        msg.setData(bundle);
                        msg.what = 2;
                        handler.sendMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return m_pingVal[0];
    }
}
