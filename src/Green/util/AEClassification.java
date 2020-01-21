package Green.util;





import Green.Green;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * 혈압기 측정값이 이상징후를 보일경우, 임상적 의미(Clinical Significance)인  Not Clinically Significant(NCS), Clinically Significant(CS)
 *에 해당하는 값인지 아닌지 판단한다. 
 * @author JHLEE jhlee@jbcp.kr
 * 2016.08.01
 */

public class AEClassification {

    /* NCS : 수축기혈압이 140~150 또는 85-89 mmHg, 이완기혈압이 90~95 mmHg, 맥박수 101~115 회/분 또는 50~54 회/분, 체온 38.0~38.4 °C 인 경우.
     * CS : 수축기혈압이 >150 또는 <85 mmHg, 이완기혈압이 >95 mmHg, 맥박수 >115 회/분 또는 <50 회/분, 체온 >38.5°C 인 경우.
     */
    public final static int SYS_CS_NCS = 0;// CS<85<=NCS
    public final static int SYS_NCS_NORM = 1;//NCS<=89 < Normal
    public final static int SYS_NORM_NCS = 2;// Normal<140 <= NCS
    public final static int SYS_NCS_CS = 3;//  NCS <= 150< CS
    public final static int DIA_NORM_NCS = 4;// Normal < 90 <=NCS
    public final static int DIA_NCS_CS = 5;//  NCS <=95 <CS
    public final static int PR_CS_NCS = 6;//  CS < 50 <= NCS
    public final static int PR_NCS_NORM = 7;//NCS <= 54 < Normal
    public final static int PR_NORM_NCS = 8;//Normal < 101 <= NCS
    public final static int PR_NCS_CS = 9;//NCS <= 115 < CS

    private static int[] range  = new int[]{85, 89, 140, 150, 90, 95, 50, 54, 101, 115};

    /* method: isNCS_CS_SYS(int val)
     * purpose: 수축기 혈압(val) 이 이상징후값에 포함되었는지를 알려준다.
     *return type: String
     *   리턴값이 "" 이면 정상, "*" 은 NCS, "**"은 CS 범위에 포함됨을 알려준다.
     */
    public AEClassification(){
        readRangeFile();
    }
   public static String isNCS_CS_SYS(int val) {//수축기혈압(최고혈압)
        

        /*     CS < SYS_CS_NCS <= NCS <= SYS_NCS_NORM < Normal < SYS_NORM_NCS  <= NCS <= SYS_NCS_CS < CS */
        if (val > range[SYS_NCS_NORM]) {
            if (val < range[SYS_NORM_NCS]) {
                return "" ; //(정상)
            } else if (val <= range[SYS_NCS_CS]) {
                return "* " ; //NCS
            } else {
                return "** " ;  //CS
            }
        } else {
            if (val >= range[SYS_CS_NCS]) {
                return "* " ; //NCS                 
            } else {
                return "** " ; //CS        
            }
        }
    }
    /* method: isNCS_CS_DIA(int val)
     * purpose: 이완기 혈압(val) 이 이상징후값에 포함되었는지를 알려준다.
     *return type: String
     *   리턴값이 "" 이면 정상, "*" 은 NCS, "**"은 CS 범위에 포함됨을 알려준다.    */

  public   static String isNCS_CS_DIA(int val) {//이완기 혈압(최저혈압)

        /*     정상 <  DIA_NORM_NCS <= NCS <= DIA_NCS_CS < CS */
        if (val < range[DIA_NORM_NCS]) {
            return ""  ; //(정상)
        } else if (val <= range[DIA_NCS_CS]) {
            return "* " ; //NCS
        } else {
            return "** "; //CS
        }
    }
    /* method: isNCS_CS_PR(int val)
     * purpose: 맥박수(val)가 이상징후값에 포함되었는지를 알려준다.
     *return type: String
     *   리턴값이 "" 이면 정상, "*" 은 NCS, "**"은 CS 범위에 포함됨을 알려준다.    */

   public  static String isNCS_CS_PR(int val) {//이완기 혈압(최저혈압)
  

        /*    CS < PR_CS_NCS <= NCS <= PR_NCS_NORM < Normal < PR_NORM_NCS  <= NCS <= PR_NCS_CS < CS */
        if (val > range[PR_NCS_NORM]) {
            if (val < range[PR_NORM_NCS]) {
                return "" ; //(정상)
            } else if (val <= range[PR_NCS_CS]) {
                return "* " ; //NCS
            } else {
                return "** " ;  //CS
            }
        } else {
            if (val >= range[PR_CS_NCS]) {
                return "* " ; //NCS                 
            } else {
                return "** " ; //CS        
            }
        }
    }

//    public int[] getRange() {
//        return range;
//    }
    public static int getRange(int range_index) {

        return range[range_index];
    }
    public static int getRangeLength() {

        return range.length;
    }
//    public static int[] getRange() {
//
//        return range;
//    }
    public static void setRange(int[] newrange) {
        if (newrange.length != range.length) {
            System.out.println("Range size should be " + range.length);
        } else {
            range = newrange;
        }
    }
    public static void setRange(int range_index, int newRange) {
        if (range_index < 0 || range_index >= range.length) {
            System.out.println("Range index should be >= 0 and < " + AEClassification.range.length);
        } else {
        	AEClassification.range[range_index] = newRange;
        }
    }

   private  void readRangeFile() {
      File file = new File(Green.DIR + "/config/range.txt");
        /*    CS < PR_CS_NCS <= NCS <= PR_NCS_NORM < Normal < PR_NORM_NCS  <= NCS <= PR_NCS_CS < CS */
        //  *85, 89, 140, 150, 90, 95, 50, 54, 101, 115,*
         
       // System.out.println(Green.DIR);
        
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));

            String line = "";
         
             while ((line = br.readLine()) != null) {
                 if(line.startsWith("*")){
             //     System.out.println("line.indexOf="+line.indexOf(",")+"\t"+(line.lastIndexOf(",")-1));
                     line=line.substring(line.indexOf(",")+1,line.lastIndexOf(","));
            //    System.out.println(line);
                     
                      String []  arr = line.split(",");
             //   System.out.println("arr.length= "+arr.length);
                      if(arr.length !=range.length){
                          Green.message("range.txt 파일 내용이 손상되었습니다.");
                          
                          return;
                      }
                      for(int i=0; i<arr.length;i++){
                          range[i]=Integer.parseInt(arr[i]);
                      }
                 }            
                
                
            };                
            
        
            br.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Green.message(file.getAbsolutePath()+" 에서 파일을 찾을 수 없습니다." );
            range = new int[]{85, 89, 140, 150, 90, 95, 50, 54, 101, 115};
           
        } catch (IOException ex) {
          
             Green.message("설정 읽는 중 오류가 발생하였습니다.");
            range = new int[]{85, 89, 140, 150, 90, 95, 50, 54, 101, 115};
        }catch(Exception ex){
                
             Green.message("설정 읽는 중 오류가 발생하였습니다. 설정파일의 값을 확인하여 다시 저장하여 주십시오.");
            range = new int[]{85, 89, 140, 150, 90, 95, 50, 54, 101, 115};
        }finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(AEClassification.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
     //range = new int[]{85, 89, 140, 150, 90, 95, 50, 54, 101, 115};
/*todo  read range.txt and save*/

    }
}
