package Green.menu;


import Green.Green;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpUtils;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

/**
 * TimeSyncNTPClient polls an NTP server with UDP and returns milli seconds with
 currentTimeMillis() intended as drop in replacement for
 System.currentTimeMillis()
 *
 * @author Will Shackleford
 */
public final class TimeSyncNTPClient implements AutoCloseable {
          String hosts[] = new String[]{"0.asia.pool.ntp.org", "time2.kriss.re.kr", "2.kr.pool.ntp.org", "time-a.nist.gov"};

//    private static final NumberFormat numberFormat = new java.text.DecimalFormat("0.00");
//
//    /**
//     * Process <code>TimeInfo</code> object and print its details.
//     *
//     * @param info <code>TimeInfo</code> object.
//     */
//    public static void processResponse(TimeInfo info) {
//        NtpV3Packet message = info.getMessage();
//        int stratum = message.getStratum();
//        String refType;
//        if (stratum <= 0) {
//            refType = "(Unspecified or Unavailable)";
//        } else if (stratum == 1) {
//            refType = "(Primary Reference; e.g., GPS)"; // GPS, radio clock, etc.
//        } else {
//            refType = "(Secondary Reference; e.g. via NTP or SNTP)";
//        }
//        // stratum should be 0..15...
//        System.out.println(" Stratum: " + stratum + " " + refType);
//        int version = message.getVersion();
//        int li = message.getLeapIndicator();
//        System.out.println(" leap=" + li + ", version="
//                + version + ", precision=" + message.getPrecision());
//
//        System.out.println(" mode: " + message.getModeName() + " (" + message.getMode() + ")");
//        int poll = message.getPoll();
//        // poll value typically btwn MINPOLL (4) and MAXPOLL (14)
//        System.out.println(" poll: " + (poll <= 0 ? 1 : (int) Math.pow(2, poll))
//                + " seconds" + " (2 ** " + poll + ")");
//        double disp = message.getRootDispersionInMillisDouble();
//        System.out.println(" rootdelay=" + numberFormat.format(message.getRootDelayInMillisDouble())
//                + ", rootdispersion(ms): " + numberFormat.format(disp));
//
//        int refId = message.getReferenceId();
//        String refAddr = NtpUtils.getHostAddress(refId);
//        String refName = null;
//        if (refId != 0) {
//            if (refAddr.equals("127.127.1.0")) {
//                refName = "LOCAL"; // This is the ref address for the Local Clock
//            } else if (stratum >= 2) {
//                // If reference id has 127.127 prefix then it uses its own reference clock
//                // defined in the form 127.127.clock-type.unit-num (e.g. 127.127.8.0 mode 5
//                // for GENERIC DCF77 AM; see refclock.htm from the NTP software distribution.
//                if (!refAddr.startsWith("127.127")) {
//                    try {
//                        InetAddress addr = InetAddress.getByName(refAddr);
//                        String name = addr.getHostName();
//                        if (name != null && !name.equals(refAddr)) {
//                            refName = name;
//                        }
//                    } catch (UnknownHostException e) {
//                        // some stratum-2 servers sync to ref clock device but fudge stratum level higher... (e.g. 2)
//                        // ref not valid host maybe it's a reference clock name?
//                        // otherwise just show the ref IP address.
//                        refName = NtpUtils.getReferenceClock(message);
//                    }
//                }
//            } else if (version >= 3 && (stratum == 0 || stratum == 1)) {
//                refName = NtpUtils.getReferenceClock(message);
//                // refname usually have at least 3 characters (e.g. GPS, WWV, LCL, etc.)
//            }
//            // otherwise give up on naming the beast...
//        }
//        if (refName != null && refName.length() > 1) {
//            refAddr += " (" + refName + ")";
//        }
//        System.out.println(" Reference Identifier:\t" + refAddr);
//
//        TimeStamp refNtpTime = message.getReferenceTimeStamp();
//        System.out.println(" Reference Timestamp:\t" + refNtpTime + "  " + refNtpTime.toDateString());
//
//        // Originate Time is time request sent by client (t1)
//        TimeStamp origNtpTime = message.getOriginateTimeStamp();
//        System.out.println(" Originate Timestamp:\t" + origNtpTime + "  " + origNtpTime.toDateString());
//
//        long destTime = info.getReturnTime();
//        // Receive Time is time request received by server (t2)
//        TimeStamp rcvNtpTime = message.getReceiveTimeStamp();
//        System.out.println(" Receive Timestamp:\t" + rcvNtpTime + "  " + rcvNtpTime.toDateString());
//
//        // Transmit time is time reply sent by server (t3)
//        TimeStamp xmitNtpTime = message.getTransmitTimeStamp();
//        System.out.println(" Transmit Timestamp:\t" + xmitNtpTime + "  " + xmitNtpTime.toDateString());
//
//        // Destination time is time reply received by client (t4)
//        TimeStamp destNtpTime = TimeStamp.getNtpTime(destTime);
//        System.out.println(" Destination Timestamp:\t" + destNtpTime + "  " + destNtpTime.toDateString());
//
//        info.computeDetails(); // compute offset/delay if not already done
//        Long offsetValue = info.getOffset();
//        Long delayValue = info.getDelay();
//        String delay = (delayValue == null) ? "N/A" : delayValue.toString();
//        String offset = (offsetValue == null) ? "N/A" : offsetValue.toString();
//
//        System.out.println(" Roundtrip delay(ms)=" + delay
//                + ", clock offset(ms)=" + offset); // offset in ms
//    }
    final InetAddress hostAddr;
    NTPUDPClient ntpUdpClient;
    Thread pollThread = null;
    final long poll_ms;

    private void pollNtpServer() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(poll_ms);
                    TimeInfo ti = ntpUdpClient.getTime(hostAddr);
//                    long diff0 = ti.getMessage().getReceiveTimeStamp().getTime() - System.currentTimeMillis();
//                    System.out.println("diff0 = " + diff0);
                    this.setTimeInfo(ti);
                } catch (SocketTimeoutException ste) {
                }
            }
        } catch (InterruptedException interruptedException) {
        } catch (IOException ex) {
            Logger.getLogger(TimeSyncNTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Connect to host and poll the host every poll_ms milliseconds. Thread is
     * started in the constructor.
     *
     * @param host
     * @param poll_ms
     * @throws UnknownHostException
     * @throws SocketException
     */
    public TimeSyncNTPClient(String host, int poll_ms) throws UnknownHostException, SocketException {
        this.poll_ms = poll_ms;
        hostAddr = InetAddress.getByName(host);

        ntpUdpClient = new NTPUDPClient();
        ntpUdpClient.setDefaultTimeout(10000);
        ntpUdpClient.open();
        ntpUdpClient.setSoTimeout(poll_ms * 2 + 20);
        pollThread = new Thread(this::pollNtpServer, "pollNtpServer(" + host + "," + poll_ms + ")");
        pollThread.start();
    }

    private TimeInfo timeInfo;
    private long timeInfoSetLocalTime;

    /**
     * Get the value of timeInfo
     *
     * @return the value of timeInfo
     */
    public synchronized TimeInfo getTimeInfo() {
        return timeInfo;
    }

    private synchronized void setTimeInfo(TimeInfo timeInfo) {
        this.timeInfo = timeInfo;
        timeInfoSetLocalTime = System.currentTimeMillis();
    }

    /**
     * Returns milliseconds just as System.currentTimeMillis() but using the
     * latest estimate from the remote time server.
     *
     * @return the difference, measured in milliseconds, between the current
     * time and midnight, January 1, 1970 UTC.
     */
    public long currentTimeMillis() {
        long diff = System.currentTimeMillis() - timeInfoSetLocalTime;
//        System.out.println("diff = " + diff);
        return timeInfo.getMessage().getReceiveTimeStamp().getTime() + diff;
    }
  public boolean syncSystemTime(long time) {
             DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        String now= dateFormat.format(time);
       int split=now.lastIndexOf(' ');
      
        String dateCmd[] = {"cmd.exe", "runas /profile /savecred /user:administrator", "cmd.exe", "/C", " date " + now.substring(0, split)};
        String timeCmd[] = {"cmd.exe", "runas /profile /savecred /user:administrator", "cmd.exe", "/C", " time " + now.substring(split+1)};

         Process p1 = null;
                Process p2 = null;

        try {
            p1 = Runtime.getRuntime().exec(dateCmd);
            //  p = Runtime.getRuntime().exec("cmd /c " + "time 01:01");
            // "cmd.exe /c runas /user:administrator dir"

            p1.waitFor();
                       

            p1.exitValue();

               p2 = Runtime.getRuntime().exec(timeCmd);
            //  p = Runtime.getRuntime().exec("cmd /c " + "time 01:01");
            // "cmd.exe /c runas /user:administrator dir"

            p2.waitFor();
            p2.exitValue();
                //  System.out.println(p.exitValue());
            // 외부 프로그램 출력 읽기
//            BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
//            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//
//            // "표준 출력"과 "표준 에러 출력"을 출력
//            while ((s = stdOut.readLine()) != null) {
//                System.out.println("=="+s);
//            }
//            while ((s = stdError.readLine()) != null) {
//                System.err.println(s);
//            }

            // 외부 프로그램 반환값 출력 (이 부분은 필수가 아님)
            System.out.println("Exit Code: " + p1.exitValue());
            
            // System.exit(p.exitValue()); // 외부 프로그램의 반환값을, 이 자바 프로그램 자체의 반환값으로 삼기
            if (p1.exitValue() == 1) {
                JOptionPane.showConfirmDialog(null, "관리자 권한으로 프로그램을 실행시켜 주십시오.", "에러", JOptionPane.CLOSED_OPTION);

                System.out.println("관리자 권한으로 프로그램을 실행시켜 주십시오.");
                p1.destroy();
                p2.destroy();
            }
            else {
                   Green.message("동기화되었습니다. ");
                   return true;
            }
           
        } catch (IOException ex) {
            JOptionPane.showConfirmDialog(null, "IOException", "에러", JOptionPane.CLOSED_OPTION);

        //    Logger.getLogger(TimeSyncNTPClient.class.getName()).log(Level.SEVERE, null, ex);
            p1.destroy();
            p2.destroy();

        } catch (InterruptedException ex) {
            JOptionPane.showConfirmDialog(null, "InterruptedException", "에러", JOptionPane.CLOSED_OPTION);
         //   Logger.getLogger(TimeSyncNTPClient.class.getName()).log(Level.SEVERE, null, ex);
              p1.destroy();
                p2.destroy();
        }
        
      
    
        return false;
    }

  
    /**
     * Polls an NTP server printing the current Date as recieved from it an the
     * difference between that and System.currentTimeMillis()
     *
     * @param args host name of ntp server in first element
     * @throws UnknownHostException
     * @throws SocketException
     * @throws InterruptedException
     * @throws IOException
     * @throws Exception
     */
    public static void main(String[] args) throws UnknownHostException, SocketException, InterruptedException, IOException, Exception {
        if (args.length < 1) {
            //   args = new String[]{"time-a.nist.gov"};
            args = new String[]{"0.asia.pool.ntp.org", "time2.kriss.re.kr", "2.kr.pool.ntp.org", "time-a.nist.gov"};
        }
              TimeSyncNTPClient ntp = null;
        long result=0;
        for (String host : args) {
            try {

                 ntp = new TimeSyncNTPClient(host, 100);
                Thread.sleep(1000);
                //  long t1 = System.currentTimeMillis();
                long t2 = ntp.currentTimeMillis();
                long t3 = System.currentTimeMillis();

                Date d = new Date(t2);
//              if(Rooibos.DEBUG) {
//                  Rooibos.setErrorMessage("\tconnected to :: "+host+
//                "\n\t"+d + " :  diff = " + (t3 - t2) + " ms\n");
//                
//              }
              System.out.println("\tconnected to :: "+host+
                "\n\t"+d + " :  diff = " + (t3 - t2) + " ms\n");
                 
                result = ntp.timeInfo.getMessage().getTransmitTimeStamp().getTime();
           
                if (result > 1477035840881L) { //dump date
                   break;
                }
            } catch (Exception e) {
               // System.out.println(e.getStackTrace());
            }
        }

        if (result==0) {
            JOptionPane.showConfirmDialog(null, "리스트에 있는 모든 인터넷 서버에 연결할 수 없습니다.", "에러", JOptionPane.CLOSED_OPTION);     
        }
       else  ntp.syncSystemTime(result);

    }

    private boolean closed = false;

    @Override
    public void close() throws Exception {
        if (null != pollThread) {
            pollThread.interrupt();
            pollThread.join(200);
            pollThread = null;
        }
        if (null != ntpUdpClient) {
            ntpUdpClient.close();
            ntpUdpClient = null;
        }

    }

    protected void finalizer() {
        try {
            this.close();
        } catch (Exception ex) {
            Logger.getLogger(TimeSyncNTPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
