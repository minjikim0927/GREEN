package Green;



import java.awt.Dimension;

import java.awt.Insets;

import java.awt.print.PrinterException;
import java.io.File;

import java.io.IOException;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.Size2DSyntax;
import javax.print.attribute.standard.MediaPrintableArea;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import javax.swing.text.html.HTMLEditorKit;
import org.apache.commons.io.FileUtils;

/**
 * template.HTML 안에 측정된 data를 추가하여 tmp*.html 을 만든 다음 default 로 설정된 프린터로 바로 인쇄한다.
 *
 * @author JHLEE jhlee@jbcp.kr 2016.08.01
 */
public class PrintHtml {

    public static final String DIR = System.getProperty("user.dir");
    static MessageFormat head = new MessageFormat("");
    static MessageFormat foot = new MessageFormat("");
    private static PrintHtml instance = new PrintHtml();
    private static JEditorPane editorPane;
    static String htmlStr1;
    static String htmlStr2;

    static boolean DEBUG = Green.DEBUG;

    private PrintHtml() {

        attr = new HashPrintRequestAttributeSet();
        Insets insets = new Insets(15, 15, 15, 15);
        float leftMargin = insets.left;
        float rightMargin = insets.right;
        float topMargin = insets.top;
        float bottomMargin = insets.bottom;
        //DEBUG = Green.DEBUG;
        attr.add(OrientationRequested.PORTRAIT);
        attr.add(MediaSizeName.ISO_A4);
        MediaSize mediaSize = MediaSize.ISO.A4;
        float mediaWidth = mediaSize.getX(Size2DSyntax.MM);
        float mediaHeight = mediaSize.getY(Size2DSyntax.MM);
        attr.add(new MediaPrintableArea(
                leftMargin, topMargin,
                (mediaWidth - leftMargin - rightMargin),
                (mediaHeight - topMargin - bottomMargin), Size2DSyntax.MM));
        //   System.out.println("mediaWidth= " + mediaWidth + "    mediaHeight=" + mediaHeight);/A4 size
        //     System.out.println("프린트될 좌우영역= " + (mediaWidth - leftMargin - rightMargin) +
        //"    프린트될 상하 영역=" + (mediaHeight - topMargin - bottomMargin));//여백을 제외한 프린트될 영역
    }

    public static PrintHtml getInstance() {

        return instance;
    }

    public static void main(String[] argv) throws IOException, PrintException {

        DateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        BloodPressure bp = new BloodPressure("bp,00000000000000000001,2015/08/02,15:42,085,092,080,177,0\r",
                sdFormat.format(new Date()), 1);
        //	BloodPressure bp = new BloodPressure("0,2015/08/02 15:42:20,85,92,080,177,0");

        try {
            TemplateReader reader = new TemplateReader();
            reader.read();
            PrintHtml ph = new PrintHtml();
            ph.DEBUG = true;
            ph.print(bp, true);

        } catch (PrinterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
 public synchronized void print(String portNum, String date, String sys, String dia, String pr, boolean useTemplate) throws IOException, PrinterException {

        StringBuilder sb = new StringBuilder();

        if (useTemplate) {
            for (int i = 0; i < TemplateReader.templateHtmlArr.length; i++) {
                sb.append(TemplateReader.templateHtmlArr[i]);
                if (i == TemplateReader.DATE) {
                    sb.append(date);
                } else if (i == TemplateReader.SYS) {
                    sb.append(sys);
                } else if (i == TemplateReader.DIA) {
                    sb.append(dia);
                } else if (i == TemplateReader.PR) {
                    sb.append(pr);
                } else if (i == TemplateReader.PORT) {
                    sb.append(portNum);
                } 
            }
        } else {


            sb.append("<html> <head>	<meta  charset=utf-8>"
                    + "<style> body { font-family:\"Segoe UI\", Frutiger, \"Frutiger Linotype\", \"Dejavu Sans\","
                    + " \"Helvetica Neue\", Arial, sans-serif; font-size:10pt; }"
                    + ".data{font-size:14px; font-weight: bold;}"
                    + "</style></head><body >"+ "<div height=\"200px\"> </div>"
                    + "<table  class=\"box\"   align=\"center\"  style=\"line-height:50px;\"  border=\"0\" cellpadding=\"0\" rowspacing=\"20\"    > ");
            sb.append("<tr><td>" + date + "&nbsp; &nbsp; &nbsp; Device No." + portNum + "</td> </tr>");

            sb.append("<tr><td>Systolic blood pressure</td><td   align=\"right\" > <p class=\"data\">" + sys
                    + " </p></td><td  align=\"left\"><small> &nbsp; mm Hg</small></td></tr>");
            sb.append("<tr><td>Diastolic blood pressure</td><td  align=\"right\" ><p class=\"data\"> " +dia
                    + " </p></td><td align=\"left\"><small>&nbsp; mm Hg</small></td></tr>");
            sb.append(" <tr><td>Pulse rate</td><td align=\"right\"><p class=\"data\">" + pr
                    + "   </p> </td><td align=\"left\"><small>&nbsp; bpm</small></td></tr>");
            sb.append("</table></body></html> ");

        }

        editorPane = new JEditorPane(new HTMLEditorKit().getContentType(), sb.toString());

      //  editorPane.setText(sb.toString());
        editorPane.setEditable(false);
        editorPane.setSize(new Dimension(670, 785));

        if (DEBUG) {
            JFrame f = new JFrame();
            javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
            jScrollPane1.setAutoscrolls(true);
            jScrollPane1.setViewportView(editorPane);
            f.add(jScrollPane1);
            // f.getContentPane().add(jScrollPane1, BorderLayout.CENTER);
            f.setSize(new Dimension(670, 785));
            // f.pack();
            f.setVisible(true);

            //to test printing, comment this line
            return;
        }

        Runnable pTask = new Runnable() {

            @Override
            public void run() {
                try {

                    editorPane.print(
                            // Two "false" args mean "no print dialog" and
                            // "non-interactive" (ie, batch-mode printing).
                            null, null, false, printService, attr, false);
                } catch (PrinterException pe) {
                    JOptionPane.showMessageDialog(null,
                            "Error printing " + editorPane.getPage() + "\n" + pe,
                            "Print Error", JOptionPane.WARNING_MESSAGE);
                }
            }

        };
        new Thread(pTask).start();

    }

    public synchronized void print(BloodPressure bp, boolean useTemplate) throws IOException, PrinterException {

        StringBuilder sb = new StringBuilder();

        if (useTemplate) {
            for (int i = 0; i < TemplateReader.templateHtmlArr.length; i++) {
                sb.append(TemplateReader.templateHtmlArr[i]);
                if (i == TemplateReader.DATE) {
                    sb.append(bp.getStrTime());
                } else if (i == TemplateReader.SYS) {
                    sb.append(bp.getSysStateHtml());
                } else if (i == TemplateReader.DIA) {
                    sb.append(bp.getDiaStateHtml());
                } else if (i == TemplateReader.PR) {
                    sb.append(bp.getPrStateHtml());
                } else if (i == TemplateReader.PORT) {
                    sb.append(bp.getPortNum());
                } 
            }
        } else {


            sb.append("<html> <head>	<meta  charset=utf-8>"
                    + "<style> body { font-family:\"Segoe UI\", Frutiger, \"Frutiger Linotype\", \"Dejavu Sans\","
                    + " \"Helvetica Neue\", Arial, sans-serif; font-size:10pt; }"
                    + ".data{font-size:14px; font-weight: bold;}"
                    + "</style></head><body >"+ "<div height=\"200px\"> </div>"
                    + "<table  class=\"box\"   align=\"center\"  style=\"line-height:50px;\"  border=\"0\" cellpadding=\"0\" rowspacing=\"20\"    > ");
            sb.append("<tr><td>" + bp.getStrTime() + "&nbsp; &nbsp; &nbsp; Device No." + bp.getPortNum() + "</td> </tr>");

            sb.append("<tr><td>Systolic blood pressure</td><td   align=\"right\" > <p class=\"data\">" + bp.getSysStateHtml()
                    + " </p></td><td  align=\"left\"><small> &nbsp; mm Hg</small></td></tr>");
            sb.append("<tr><td>Diastolic blood pressure</td><td  align=\"right\" ><p class=\"data\"> " + bp.getDiaStateHtml()
                    + " </p></td><td align=\"left\"><small>&nbsp; mm Hg</small></td></tr>");
            sb.append(" <tr><td>Pulse rate</td><td align=\"right\"><p class=\"data\">" + bp.getPrStateHtml()
                    + "   </p> </td><td align=\"left\"><small>&nbsp; bpm</small></td></tr>");
            sb.append("</table></body></html> ");

        }

        editorPane = new JEditorPane(new HTMLEditorKit().getContentType(), sb.toString());

      //  editorPane.setText(sb.toString());
        editorPane.setEditable(false);
        editorPane.setSize(new Dimension(670, 785));

        if (DEBUG) {
            JFrame f = new JFrame();
            javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
            jScrollPane1.setAutoscrolls(true);
            jScrollPane1.setViewportView(editorPane);
            f.add(jScrollPane1);
            // f.getContentPane().add(jScrollPane1, BorderLayout.CENTER);
            f.setSize(new Dimension(670, 785));
            // f.pack();
            f.setVisible(true);

            //to test printing, comment this line
            return;
        }

        Runnable pTask = new Runnable() {

            @Override
            public void run() {
                try {

                    editorPane.print(
                            // Two "false" args mean "no print dialog" and
                            // "non-interactive" (ie, batch-mode printing).
                            null, null, false, printService, attr, false);
                } catch (PrinterException pe) {
                    JOptionPane.showMessageDialog(null,
                            "Error printing " + editorPane.getPage() + "\n" + pe,
                            "Print Error", JOptionPane.WARNING_MESSAGE);
                }
            }

        };
        new Thread(pTask).start();

    }
    static private PrintRequestAttributeSet attr;
    static PrintService printService
            = PrintServiceLookup.lookupDefaultPrintService();

}
