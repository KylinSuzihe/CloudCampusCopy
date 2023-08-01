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

public class TracertActivity extends AppCompatActivity {
    private static Button tracertStart;
    private static Button tracertStop;
    private Button gateway;
    private Button DNS;
    private Button baidu;
    private AutoCompleteTextView ipAddr;
    private static List<Receive> receiveList = new ArrayList<>();
    private static TextView textView;
    private static boolean is_tracerting = false;
    private ImageButton history;

    private static DatabaseHelper dbHelper = null;

    private static Context context = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracert);

        context = getApplicationContext();
        dbHelper = new DatabaseHelper(context);
        dbHelper.getWritableDatabase();

        //获取控件
        getButton();
        //绑定监听器
        addListener();
    }
    private void getButton() {
        tracertStart = findViewById(R.id.start_tracert);
        tracertStop = findViewById(R.id.stop_tracert);
        gateway = findViewById(R.id.gateway);
        DNS = findViewById(R.id.dns);
        baidu = findViewById(R.id.baidu);
        ipAddr = findViewById(R.id.ipaddr);
        textView = findViewById(R.id.answer_tracert);
        history = findViewById(R.id.history_ping);
    }
    private void addListener() {
        tracertStart.setOnClickListener(startTracert);
        tracertStop.setOnClickListener(endTracert);

        baidu.setOnClickListener(baiduListener);
        DNS.setOnClickListener(dnsListener);
        gateway.setOnClickListener(gatewayListener);

        history.setOnClickListener(historyListener);
    }
    private void toStart() {
        tracertStart.setVisibility(View.GONE);
        tracertStop.setVisibility(View.VISIBLE);
        is_tracerting = true;
        textView.setText("");
    }

    private View.OnClickListener  startTracert = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            toStart();
            String ip = ipAddr.toString();
            getTracertValue(ip, false);
        }
    };
    private View.OnClickListener  endTracert = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            tracertStart.setVisibility(View.VISIBLE);
            tracertStop.setVisibility(View.GONE);
            is_tracerting = false;
        }
    };
    private View.OnClickListener baiduListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            toStart();
            ipAddr.setText("www.baidu.com");
            getTracertValue("39.156.66.18", false);
        }
    };

    private View.OnClickListener dnsListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            toStart();
            String ip = DnsUtil.getDns(context);
            ipAddr.setText(ip);
            getTracertValue(ip, false);
        }
    };

    private View.OnClickListener gatewayListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            toStart();
            String ip = GateWayUtil.getGateWay(context);
            ipAddr.setText(ip);
            getTracertValue(ip, false);
        }
    };
    private View.OnClickListener historyListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent historyIntent = new Intent(TracertActivity.this, HistoryActivity.class);
            historyIntent.putExtra("type", "trace");
            startActivity(historyIntent);
        }
    };
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
                    //TODO:修改参数
                    Bundle bundle = msg.getData();
                    String text = bundle.getString("text");
                    String ssid = bundle.getString("ssid");
                    String ipaddr = bundle.getString("ipaddr");
                    SimpleDateFormat formatter   =   new   SimpleDateFormat   ("yyyy-MM-dd HH:mm:ss");
                    Date curDate =  new Date(System.currentTimeMillis());
                    String   str   =   formatter.format(curDate);
                    final ContentValues values=new ContentValues();
                    values.put("text",text);
                    values.put("ssid",ssid);
                    values.put("ipaddr", ipaddr);
                    values.put("time", str);
                    dbHelper.insert(values, "tracert");
                    tracertStart.setVisibility(View.VISIBLE);
                    tracertStop.setVisibility(View.GONE);
                default:
                    break;
            }
        }
    };

    private static void sendMessage(String strRet) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("answer", strRet);
        msg.setData(bundle);
        msg.what = 1;
        handler.sendMessage(msg);
    }

    public static int getTracertValue(String ip, boolean bResetPing) {

        final int[] m_pingVal = new int[1];
        if (bResetPing)	{ m_pingVal[0] = -1; }
        new Thread() {
            public void run() {
                int N = 0;
                boolean flag = true;
                int num = 0;
                String text = "Tracing route to " + ip + " over a maximun of 30 hops.\n";
                sendMessage(text);
                while (flag) {
                    try {
                        String tmpStr = "/system/bin/ping -c 1 -W 1 -t " + ++N + " ";
                        String cmdStr = tmpStr.concat(ip);
                        System.out.println(cmdStr);
                        Process proc = Runtime.getRuntime().exec(cmdStr);
                        // 获取输出流
                        InputStream is1 = proc.getInputStream();
                        BufferedReader buf = new BufferedReader(new InputStreamReader(is1));
                        // 解析输出流
                        String strRet = null;
                        while ((strRet = buf.readLine()) != null && is_tracerting) {
                            System.out.println("--------------Input: " + strRet);
                            // 64 bytes from 39.156.66.18: icmp_seq=11 ttl=50 time=23.3 ms


                            if (strRet.contains("Time to live exceeded")) {
                                System.out.println(strRet);
                                String ans = "  ";

                                String ipAddr = strRet.split(" ")[1].split(":")[0];
                                proc = Runtime.getRuntime().exec("/system/bin/ping -c 3 " + ipAddr);
                                is1 = proc.getInputStream();
                                buf = new BufferedReader(new InputStreamReader(is1));
                                strRet = buf.readLine();
                                int cnt = 3;
                                while ((strRet = buf.readLine()) != null && (cnt--) > 0) {
                                    System.out.println("-----ping-----Input: " + strRet);
                                    String[] tmp = strRet.split("=");
                                    if (tmp[tmp.length - 1].contains("ms") && strRet.contains("icmp_seq=")) {
                                        ans += tmp[tmp.length - 1].split("\\(")[0] + " ";
                                    } else {
                                        ans += "   * ms ";
                                    }
                                }
                                ans += ipAddr + '\n';
                                System.out.println("ANS : " + ans);
                                num++;
                                sendMessage(num + ans);
                                text += num + ans;
                                break;
                            }
                            else if (strRet.contains("bytes from")) {
                                System.out.println(strRet);
                                String ans = "  ";

                                String ipAddr = strRet.split(" ")[3].split(":")[0];
                                proc = Runtime.getRuntime().exec("/system/bin/ping -c 3 " + ipAddr);
                                is1 = proc.getInputStream();
                                buf = new BufferedReader(new InputStreamReader(is1));
                                strRet = buf.readLine();
                                int cnt = 3;
                                while ((strRet = buf.readLine()) != null && (cnt--) > 0) {

                                    if (strRet.contains("icmp_seq=")) {
                                        System.out.println("-----ping-----Input: " + strRet);
                                        String[] tmp = strRet.split("=");
                                        if (tmp[tmp.length - 1].contains("ms"))
                                            ans += tmp[tmp.length - 1].split("\\(")[0] + " ";
                                    } else {
                                        ans += "   * ms ";
                                    }
                                }
                                ans += ipAddr + '\n';
//                                System.out.println("ANS : " + ans);
                                num++;
                                sendMessage(num + ans);
                                text += num + ans + "Trace complete";
                                sendMessage("Trace complete");

                                Message msg = new Message();
                                Bundle bundle = new Bundle();
                                bundle.putString("text", text);
                                bundle.putString("ipaddr", ip);
                                bundle.putString("ssid", "<unknown ssid>");
                                msg.setData(bundle);
                                msg.what = 2;
                                handler.sendMessage(msg);
                                flag = false;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (N > 30) flag = false;
                }
            }
        }.start();
        return m_pingVal[0];
    }
}
