package com.alexleykin.sunalarm;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Alex on 10/21/2016.
 */

public class MiLightRunnable implements Runnable {
    static String rgb = "20";
    static String all_off = "210055";
    static String all_on = "220055";
    static String brighter = "230055";
    static String dimmer = "240055";
    public static int startColor;
    public static int COLOR_STAGES;
    public static int BRIGHTNESS_STAGES = 8;
    private static int delayMS;
    private static int turnoffMS;

    public static String MILIGHT_IP = "192.168.1.5";
    public MiLightRunnable(Alarm alarm, int startColor, int COLOR_STAGES) {
        this.delayMS = alarm.sunrise_duration*60*1000;
        this.turnoffMS = alarm.sunrise_turnoff*60*1000;
        this.startColor = startColor;
        this.COLOR_STAGES = COLOR_STAGES;
    }

    public static void turnoff() {
        sendSymbol(all_off);
    }

    public static void sendSymbol(String symbol){
        try {
            InetAddress address = InetAddress.getByName(MILIGHT_IP);
            byte[] buffer = new BigInteger(symbol,16).toByteArray();
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.send(new DatagramPacket(buffer, buffer.length, address, 8899));
            datagramSocket.close();
        }
        catch (Exception ex) {
            Log.e("Exception while communicating with a lightbulb", ex);
        }
    }
    public static void reset() {
        for (int i=0;i<9;i++)
            sendSymbol(dimmer);
        pause(100);
        sendSymbol(rgb+Integer.toHexString(startColor)+"55");
    }
    private static void pause(long ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException ex) {
            Log.e("Light bulb thread sleep interrupted", ex);
        }
    }
    public void run() {
        reset();
        pause(1000);
        sendSymbol(all_on);
        pause(1000);
        reset();
        pause(1000);
        for (int i=0;i<COLOR_STAGES;i++) {
            sendSymbol(rgb + Integer.toHexString(startColor-i) + "55");
            if (i % (COLOR_STAGES/BRIGHTNESS_STAGES) == 0) {
                pause(1000);
                sendSymbol(brighter);
            }
            pause(delayMS/COLOR_STAGES);
        }
    }
}
