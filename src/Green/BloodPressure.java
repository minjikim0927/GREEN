package Green;



import Green.util.AEClassification;



public class BloodPressure {
	//	 this.type = 'blood pressure';
    //	 this.data = data;

	//sys :이완기 혈압 -> 최저혈압
    //dia : 수축기 혈압 -> 최고혈압
    //map : 평균혈압
    //pr : 맥박
    //motion : 자세 
	//160501
    //portNum : 몇번 혈압기에서 측정되었는가 
    private int year = 0, month, day, hour, min, sys, map, dia, pr, motion, portNum;
    private String strTime;
    private String deviceID;
    private String sysState, diaState, prState; // CS, NCS, Normal state 
    private String cmt="";
//"bp,00000000000000000001,2015/08/02,15:42,080,092,018,120,0\r",//
    // csv 파싱
    public BloodPressure(String str) {
//System.out.println(str+ "===bp 파싱 from serial data ===="+str.length());
        if (str != null) {
            String[] token = str.split(",", -1);
            // portnum, time, dia, sys, map, pr, motion

            for (int i = 0; i < token.length; i++) {
                //	System.out.println(token[i]);
                switch (i) {


                    case 0:
                        this.strTime = token[i];
                        break;
                    case 1:
                        this.portNum = Integer.parseInt(token[i]);
                        break;
      case 2: // sys
                        this.sys = Integer.parseInt(token[i]);
                        break;
      case 3: // map
                        this.map = Integer.parseInt(token[i]);
                        break;            
                    case 4: // dia

                        this.dia = Integer.parseInt(token[i]);
                        break;




                    case 5: // pr
                        this.pr = Integer.parseInt(token[i]);
                        break;

//		    case 6: // cmt
//			this.motion = Integer.parseInt(token[i]);
//			break;
                    //    default: System.out.println(token);
                }
            }
        }
        //state check
//        sysState = AEClassification.isNCS_CS_SYS(this.sys);
//        diaState = AEClassification.isNCS_CS_DIA(this.dia);
//        prState = AEClassification.isNCS_CS_PR(this.pr);

    }
    //parsing from csv file 
//    public BloodPressure(String str, boolean includeCmt) {
//        if(!includeCmt){
//               System.out.println(" error: it should be called when cmt is contained in the String=> " + str);
//               return;
//        }
//        if (str != null) {
//            String[] token = str.split(",", -1);
//            // portnum, time, dia, sys, map, pr, motion
//
//            for (int i = 0; i < token.length; i++) {
//              	System.out.println(i+"\t"+token[i]);
//                switch (i) {
//
//
//                    case 0:
//                        this.strTime = token[i];
//                        break;
//                    case 1:
//                        this.portNum = Integer.parseInt(token[i]);
//                        break;
//      case 2: // sys
//                        this.sys = Integer.parseInt(token[i]);
//                        break;
//      case 3: // map
//                        this.map = Integer.parseInt(token[i]);
//                        break;            
//                    case 4: // dia
//
//                        this.dia = Integer.parseInt(token[i]);
//                        break;
//
//                    case 5: // pr
//                        this.pr = Integer.parseInt(token[i]);
//                        break;
//
//	    case 6: // cmt
//		this.cmt = token[i];
//		break;
//                    //    default: System.out.println(token);
//                }
//            }
//        }
//        //state check
////        sysState = AEClassification.isNCS_CS_SYS(this.sys);
////        diaState = AEClassification.isNCS_CS_DIA(this.dia);
////        prState = AEClassification.isNCS_CS_PR(this.pr);
//
//    }
    public BloodPressure(String time, int port, int sys, int dia, int pr) {

        this.strTime = time;
        this.portNum = port;

        this.dia = dia;
        this.sys = sys;
        this.pr = pr;

        //state check
//        sysState = AEClassification.isNCS_CS_SYS(this.sys);
//        diaState = AEClassification.isNCS_CS_DIA(this.dia);
//        prState = AEClassification.isNCS_CS_PR(this.pr);

    }
    //    }
    public BloodPressure(String time, String  port, String sys, String dia, String pr) {

        this.strTime = time;
        this.portNum = Integer.parseInt(port);
 this.sys = Integer.parseInt(sys);
                this.dia = Integer.parseInt(dia);
            this.pr = Integer.parseInt(pr);
   

        //state check
//        sysState = AEClassification.isNCS_CS_SYS(this.sys);
//        diaState = AEClassification.isNCS_CS_DIA(this.dia);
//        prState = AEClassification.isNCS_CS_PR(this.pr);

    }

    public BloodPressure(String str, String strTime, int portNum) {

        this.strTime = strTime;
        this.portNum = portNum;

		//str= bp,00000000000000000001,2015/08/02,15:42,120,088,080,077,0
        int startPos = str.indexOf("bp,");

        // find End Byte's position
        int endPos = str.indexOf("\r");

        if (startPos == -1 || endPos == -1) {
            System.out.println("Err: can't find start or end");
            return;
        }
        String arr[] = str.split(",");
//System.out.println("=======(String str, String strTime, int portNum)========"+str);
        // valid check
        if (endPos == startPos + 58) {

            this.year = Integer.parseInt(str.substring(startPos + 24, startPos + 28));
            this.month = Integer.parseInt(str.substring(startPos + 29, startPos + 31));
            this.day = Integer.parseInt(str.substring(startPos + 32, startPos + 34));
            this.hour = Integer.parseInt(str.substring(startPos + 35, startPos + 37));
            this.min = Integer.parseInt(str.substring(startPos + 38, startPos + 40));
            this.sys = Integer.parseInt(str.substring(startPos + 41, startPos + 44));
            this.map = Integer.parseInt(str.substring(startPos + 45, startPos + 48));
            this.dia = Integer.parseInt(str.substring(startPos + 49, startPos + 52));
            this.pr = Integer.parseInt(str.substring(startPos + 53, startPos + 56));
            this.motion = Integer.parseInt(str.substring(startPos + 57, startPos + 58));
        } else {
            System.err.println("Err: index size is not valid : " + endPos + "," + (startPos + 58) + "," + str.length());
        }

    }

    public int getPortNum() {
        return portNum;
    }

    public void setPortNum(int portNum) {
        this.portNum = portNum;
    }

    public String getStrTime() {
        return strTime;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMin() {
        return min;
    }

    public int getSys() {
        return sys;
    }

    public int getMap() {
        return map;
    }

    public int getDia() {
        return dia;
    }

    public int getPr() {
        return pr;
    }

    public int getMotion() {
        return motion;
    }

    public String getSysState() {
        //state check
        sysState = AEClassification.isNCS_CS_SYS(this.sys);

        return sysState;
    }

    public String getDiaState() {
      
        diaState = AEClassification.isNCS_CS_DIA(this.dia);

        return diaState;
    }

    public String getPrState() {
        prState = AEClassification.isNCS_CS_PR(this.pr);
        return prState;
    }

    public String getSysStateHtml() {
        return getSysState() + "<span class=\"data\">" + getSys() + "</span>";
    }

    public String getDiaStateHtml() {
        return getDiaState() + "<span class=\"data\">" + getDia() + "</span>";
    }

    public String getPrStateHtml() {
        return getPrState() + "<span class=\"data\">" + getPr() + "</span>";
    }

}
