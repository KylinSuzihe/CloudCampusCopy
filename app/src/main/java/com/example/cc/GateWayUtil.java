package com.example.cc;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GateWayUtil {


    /**
     * int值转换为ip
     * @param addr
     * @return
     */
    public static String intToIp(int addr) {
        return ((addr & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF));
    }
    public static String getGateWay(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        DhcpInfo info=wifiManager.getDhcpInfo();

        int gateway=info.gateway;

        String ip=intToIp(gateway);
        return ip;
//        String[] arr;
//        try {
//            Process process = Runtime.getRuntime().exec("ip route list table 0");
//            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String string;
//            while ((string = in.readLine()) != null) {
//                System.out.println("GateWay : " + string);
//            }
//            arr = string.split("\\s+");
//            return arr[2];
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return "0.0.0.0";
    }
}
