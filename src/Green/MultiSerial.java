package Green;



import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

// [수정 사항]
// 포트 번호를 인쇄용지에 출력 -> 완료
// csv 에 줄바꿈 문자 입력 -> saveToCSV
// `1번 혈압기 번호에 따라 인쇄 -> 포트에 따라 설정
// 움직임 1 2 3  및 평균혈압에 따른 템플릿 조절 -> 보류 
// 움직임이 없는 바른 자세의 데이터만 사용될 것이기 때문에 우선순위가 낮은 부분이라고 생각됨
// 시간 표시를 24시간 단위로 수정 -> 160426 날짜포맷의  hh 를 HH 로 수정
// 프린팅 작업 요청을 동기화 -> 160426 synchronized 추가 
// 로그 파일 생성기능 추가 -> 160426 완료
// [추가 설명]
// 인쇄 작업 현황은 윈도우 내장 프린트 작업 관리자를 통해 확인할 수 있습니다. 
// 테스트 했을 당시의 1번 혈압기는(COM2)이었습니다.
// 이는 BPtoLabel.bat 을 메모장으로 열어보면 보입니다.
// portsetting.jar 의 역할은 BPtoLabel.bat 파일을 만들어 주는 것입니다.
// COM1은 보통 표준 입출력이라 아닌 경우가 많습니다. 
public class MultiSerial {

    //the timeout value for connecting with the port
    final static int TIMEOUT = 2000;

    //some ascii values for for certain things
    final static int SPACE_ASCII = 32;
    final static int DASH_ASCII = 45;
    final static int NEW_LINE_ASCII = 10;
    
    // 프린트 비활성화를 위한 프린트 변수
    public static boolean doPrinting = true;

    // reader 쓰레드 구동 변수
    public static boolean isRunning = true;

    public MultiSerial() {
        super();
    }
public String portName=null;
public String getPortName(){
    
    return portName;
}
public CommPort commPort;
    public List<Thread> threadList = new ArrayList<Thread>();
    public List<CommPort> comPortList = new ArrayList<CommPort>();

    void connect(String portName, int portNum) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
       this.portName=portName;
        if (portIdentifier.isCurrentlyOwned()) {
          
            Green.message("현재 연결되어 있음");
            // System.err.println("Error: Port is currently in use");
            isRunning = true;
            
//			pause();
        } else {
               
                try{
             commPort = portIdentifier.open(this.getClass().getName(), TIMEOUT);
             comPortList.add(commPort);
                }catch(Exception e){
                     throw new MyException("프로그램이 두번 실행되었거나 다른 프로그램이 직렬포트 사용중");    
                }

            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;

                serialPort.setSerialPortParams(2400,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_EVEN);

                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();
               
              //  System.out.println("====portNAme="+ portName +" portNum ="+ portNum+"  ->  " + serialPort.getName());
                
                Thread t_in = new Thread(new SerialReader(in, portNum));
                t_in.start();
                threadList.add(t_in);

				//160609 여기서 오류가나서 안읽혔었음. 
                //                Thread t_out = new Thread(new SerialWriter(out));
                //
                //                t_out.start();
                //                threadList.add(t_out);
                Green.message("Success: " + portNum + "번 혈압계(" + portName + ") 연결 완료.");
            } else {
                throw new MyException("프로그램이 두번 실행되었거나 다른 프로그램이 직렬포트 사용중");
                //   System.out.println("Error: Only serial ports are handled by this example.");
                //   System.err.println("Error: Only serial ports are handled by this example.");

            }
        }
    }
  void disconnect() throws Exception {
      int i=0;
     // System.out.println(this.portName+"  my thread num = "+threadList.size());
      for (; i<threadList.size();i++){
          Thread thread=threadList.get(i);
          if(thread.isAlive()) {
              thread.interrupt();
          }
          
       Green.debugMessage(this.portName+"  thread.isAlive() "+thread.isAlive());
     
             SerialPort prt = (SerialPort) comPortList.get(i);            
            prt.close();

          }
      }
       
      
    public class MyException extends RuntimeException {

        public MyException() {
            super();
        }

        public MyException(String message) {
            super(message);
        }
    }

    /**
     *하나의 포트에서 전송되는 시리얼 정보  비트를 계속 읽다가 단위로 끊은 후 저장
     */
    public static class SerialReader implements Runnable {

        InputStream in;
        int portNum;
        boolean stop=false;

        PrintHtml printer = PrintHtml.getInstance();
        CSVControl csv = CSVControl.getInstance();

//        TempData tempData = TempData.getInstance();
        public SerialReader(InputStream in, int portNum) {
            this.in = in;
            this.portNum = portNum;
        }
        public void stop(){
            stop=true;
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int len = -1;
            if(stop){
                
                this.stop();
                
                return;
             }
            String line = "";
            //  String butmp = "bp,99999999999999999999,2015/08/02,15:42,120,088,080,077,0\rbp,22222222222222222222,2015/08/02,15:42,120,088,080,077,0 2015/08/02,15:42,120,088,080,077,0\r";
            // buffer = butmp.getBytes(Charset.forName("UTF-8"));
            //   System.out.println("buffersize  " + buffer.length);

            try {
                while ((len = this.in.read(buffer)) > -1) {
                    //     System.out.println("len " + len);
                    // bool 변수를 통한 쓰레드 종료
                    if (!isRunning) {
                        break;
                    }

                    String cur = new String(buffer, 0, len);

//					System.out.println(cur);
                    //지금 온건 공백인데 저장된 문자열이 있을때, 값이 입력됬다고 봄
                    // moxa 시리얼허브에서 작동함
                    if (cur.equals("")) {
                        if (!line.equals("")) {
                            // System.out.println("cur is empty but line has "+line);
                            //여기서 처리한다.
//							BPtoLabel.logTextArea.append("\n" +portNum + " 번 혈압계에서 측정결과 전송 : "+ line);

                            saveBpResult(line);

                            line = "";
                        }

                        // 줄바꿈문자가 오면 처리하기
                        // moxa 시리얼 허브에서 아래 분기문 건너뜀
                    } else {

                        //                        System.out.println("else=>?"+cur+"\t"+len + "\t==> " + (char) len+"\tlin="+line);
                        line += cur;
                    }
                }

            } catch (IOException e) {
                Green.debugMessage(e.toString());
                System.out.println("Error: I/O 오류");
                //파일이 어뵤어ㅛㅓ 오류가 생기긴했음
                e.printStackTrace();

                // pause();
                //            } catch (PrinterException e) {
                //                // TODO Auto-generated catch block
                //                System.out.println("Error: 인쇄오류. 프린터와의 연결 상태를 확인해주세요.");
                //
                //                e.printStackTrace();
                //                pause();
            }
            //            finally{//jhlee@jbcp : add read.close
            //                try {
            //                    in.close();
            //
            //                } catch (IOException ex) {
            //                    Logger.getLogger(MultiSerial.class.getName()).log(Level.SEVERE, null, ex);
            //                }
            //            }
            //System.exit(0);
        }

        // 측정된 결과와 전송된 시각을 
        // 1. 인쇄 2. csv 파일에 저장 3. 테이블에 표시
        //합니다.
        public void saveBpResult(String line) throws IOException {
            DateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
          
            long now=System.currentTimeMillis();
            
            if(now <= Green.lastSyncTime) {
                   Green.debugMessage("current time " + new Date() + " is behind from server time " );
            //JOptionPane.showConfirmDialog(null, "현시간이 마지막 동기화된 시간보다 늦습니다. 시간 동기화를 하여 주십시오.", "에러", JOptionPane.CLOSED_OPTION);
                Green.message("현시간이 마지막 동기화된 시간보다 늦습니다. 시간 동기화를 시작합니다.");
                Green.syncTime();
            }
            BloodPressure bp = new BloodPressure(line, sdFormat.format(new Date()), portNum);
            if (bp.getYear() == 0) {
                   Green.debugMessage("Log : Invalid line is returned");
            } else {
                //인쇄 및 csv 파일에 기록

                try {
                    //print out 
                    if (doPrinting) {
                        printer.print(bp, Green.useTemplate);
                    }

                    //update on BpTable 
                    Green.updateTable(bp);
                

                    //update on csv file
                    try {
                        //나중에 확인할 수 있도록 저장만
                        csv.save(bp);
                        //160615 임시데이터 사용
                        //   tempData.add(bp);
//Green.report();
                    } catch (Exception ex) {
                        Logger.getLogger(MultiSerial.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } catch (PrinterException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

}
