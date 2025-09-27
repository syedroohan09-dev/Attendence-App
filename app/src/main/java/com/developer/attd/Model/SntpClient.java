package com.developer.attd.Model;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;

public class SntpClient {
    public static long getNtpTime() {
        String ntpHost = "time.google.com"; // or pool.ntp.org
        try {
            NTPUDPClient client = new NTPUDPClient();
            client.setDefaultTimeout(5000);
            client.open();

            InetAddress hostAddr = InetAddress.getByName(ntpHost);
            TimeInfo info = client.getTime(hostAddr);

            // direct server ka time stamp
            long returnTime = info.getMessage().getTransmitTimeStamp().getTime();
            return returnTime; // milliseconds since 1970 UTC
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // fail case
        }
    }
}
