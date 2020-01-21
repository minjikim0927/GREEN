package Green;



import Green.util.AEClassification;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author JHLEE
 */
public class TemplateReader {

  //  public static final String DIR = System.getProperty("user.dir");
    public static String[] templateHtmlArr;

    /*만약 템플릿에 들어가야하는 데이타가 0개이면 템플릿 문서는 그냥 하나의 스트링(String[0])
     데이타가 2개 인 경우 템플릿 문서는 3부분으로 나뉜다.=>String[0] ~$data1 ~ String[1]~$data2 ~ String [2] */
    static int DATE;
    static int SYS;
    static int DIA;
    static int PR;
    static int PORT;

    // static int INFO;
    public TemplateReader() throws IOException {
templateHtmlArr = new String[6];
    }

    public void read() throws IOException {

        int arrIndex = 0;
        StringBuilder contentBuilder = new StringBuilder();

        try {
            FileInputStream fis = new FileInputStream(new File(Green.DIR + "/template/PrintTemplate.html"));
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader in = new BufferedReader(isr);
            String str;
            while ((str = in.readLine()) != null) {
    
                if (str.contains("$date")) {

                    int dataIndex = str.indexOf('$');
                    contentBuilder.append(str.substring(0, dataIndex));

                    DATE = arrIndex++;
                    templateHtmlArr[DATE] = contentBuilder.toString();
                    contentBuilder = new StringBuilder();
                    contentBuilder.append(str.substring(dataIndex + 5));//"$data"를 제외한 string 저장

                } else if (str.contains("$sys")) {
                    int dataIndex = str.indexOf('$');
                    contentBuilder.append(str.substring(0, dataIndex));
                    SYS = arrIndex++;
                    templateHtmlArr[SYS] = contentBuilder.toString();
                    contentBuilder = new StringBuilder();
                    contentBuilder.append(str.substring(dataIndex + 4));
                } else if (str.contains("$dia")) {
                    int dataIndex = str.indexOf('$');
                    contentBuilder.append(str.substring(0, dataIndex));
                    DIA = arrIndex++;
                    templateHtmlArr[DIA] = contentBuilder.toString();
                    contentBuilder = new StringBuilder();
                    contentBuilder.append(str.substring(dataIndex + 4));
                } else if (str.contains("$pr")) {
                    int dataIndex = str.indexOf('$');
                    contentBuilder.append(str.substring(0, dataIndex));
                    PR = arrIndex++;
                    templateHtmlArr[PR] = contentBuilder.toString();
                    contentBuilder = new StringBuilder();
                    contentBuilder.append(str.substring(dataIndex + 3));
                } else if (str.contains("$port")) {
                    int dataIndex = str.indexOf('$');
                    contentBuilder.append(str.substring(0, dataIndex));
                    PORT = arrIndex++;
                    templateHtmlArr[PORT] = contentBuilder.toString();

                    contentBuilder = new StringBuilder();
                    contentBuilder.append(str.substring(dataIndex + 5));
                } else if (str.contains("$")) {
                    if (str.contains("$SYS_NCS_CS")) {
                        str = str.replace("$SYS_NCS_CS", "" + AEClassification.getRange(AEClassification.SYS_NCS_CS));
                    }
                    if (str.contains("$SYS_NCS_NORM")) {
                        str = str.replace("$SYS_NCS_NORM", "" + AEClassification.getRange(AEClassification.SYS_NCS_NORM));
                    }
                    if (str.contains("$SYS_CS_NCS")) {
                        str = str.replace("$SYS_CS_NCS", "" + AEClassification.getRange(AEClassification.SYS_CS_NCS));
                    }
                    if (str.contains("$SYS_NORM_NCS")) {
                        str = str.replace("$SYS_NORM_NCS", "" + AEClassification.getRange(AEClassification.SYS_NORM_NCS));
                    }
                    if (str.contains("$DIA_NORM_NCS")) {
                        str = str.replace("$DIA_NORM_NCS", "" + AEClassification.getRange(AEClassification.DIA_NORM_NCS));
                    }
                    if (str.contains("$DIA_NCS_CS")) {
                        str = str.replace("$DIA_NCS_CS", "" + AEClassification.getRange(AEClassification.DIA_NCS_CS));
                    }
                    if (str.contains("$PR_NCS_NORM")) {
                        str = str.replace("$PR_NCS_NORM", "" + AEClassification.getRange(AEClassification.PR_NCS_NORM));
                    }
                    if (str.contains("$PR_NCS_CS")) {
                        str = str.replace("$PR_NCS_CS", "" + AEClassification.getRange(AEClassification.PR_NCS_CS));
                    }
                    if (str.contains("$PR_CS_NCS")) {
                        str = str.replace("$PR_CS_NCS", "" + AEClassification.getRange(AEClassification.PR_CS_NCS));
                    }
                    if (str.contains("$PR_NORM_NCS")) {
                        str = str.replace("$PR_NORM_NCS", "" + AEClassification.getRange(AEClassification.PR_NORM_NCS));
                    }

                    contentBuilder.append(str);
                } else {
                    contentBuilder.append(str);
                }

            }

            templateHtmlArr[arrIndex] = contentBuilder.toString();
            in.close();
        } catch (IOException e) {
            Green.message(e.toString());
        }       
    }

}
