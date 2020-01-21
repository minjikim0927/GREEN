package Green;

import Green.menu.LoadCSVFileDialog;
import Green.menu.TimeSyncNTPClient;
import Green.menu.AESettingDialog;
import Green.menu.DebugView;
import Green.util.AEClassification;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import gnu.io.CommPortIdentifier;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.PrinterException;

import java.io.File;
import java.io.FileNotFoundException;

import java.io.FileOutputStream;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.Date;
import java.util.Enumeration;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;

import javax.swing.JFileChooser;

import static javax.swing.JFileChooser.SAVE_DIALOG;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.io.FileUtils;

/*
 *file: Green.java  (main)
 *purpose: 멀티 시리얼 포트로부터 들어오는 혈압계 시리얼 데이타들을 html템플렛에 따라
 *         A4에 출력하는 프로그램의  main GUI 파일
 *         
 * @version 2.0 
 * 2016.6
 * @author Lee,Ji hyoung
 */
 /*To do
 * thread 
 *
 *
 */
public final class Green extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    DebugView debugWindow = DebugView.getInstance();
    public static boolean DEBUG = true;//to test

    SimpleDateFormat sdf = new SimpleDateFormat("YYYY/MM/dd HH:mm");

    protected static DefaultTableModel tableModel;

    private boolean stateConnection = false;
    DefaultTableCellRenderer cellRenderer;
    public static final String DIR = System.getProperty("user.dir");
    public static boolean useTemplate = true;
    public TemplateReader reader;
    //  public static boolean didSync = false;
    public static long lastSyncTime = 1477035840881L;//dump start with this number 
    //and saved the time after sync with time server

    public Green() {

        //set View align
        cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setHorizontalAlignment(JLabel.CENTER);

        //read NCS_CS range values from file.    
        AEClassification ae = new AEClassification();

        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/Img/Green_logo_short32.png")));

        initComponents();
        loadCSVtoTable();
        tableRenderer();//readencryptedData from CSV to table

        try {
            reader = new TemplateReader();
            reader.read(); //read template file to be ready to print 
            if (reader.templateHtmlArr[0] == null) {
                message("error to read template");
            }
        } catch (IOException ex) {
            message(ex.toString());
        }

        // connectToDivice();
        // Window Listeners
        connect();
        this.addWindowListener(new WindowAdapter() {
            //프로그램이 window가 종료될때 모든 port 연결을 해제합니다.
            @Override
            public void windowClosing(WindowEvent e) {
                //if(serialRx!=null) serialRx.disconnect();

                System.exit(0);
            } //windowClosing
        });
    }

    private void runWatchMenu(JMenu menu) {
        final SimpleDateFormat sdf1 = new SimpleDateFormat("YYYY-MM-dd   HH:mm:ss");
        // syncTime();//sync time with internet server

        ActionListener time = (ActionEvent evt) -> {
            long currentTime = System.currentTimeMillis();
            Calendar timeNow = Calendar.getInstance();
            // 콘솔에 현재 시간 출력
            timeNow.setTimeInMillis(currentTime);
            Date d = timeNow.getTime();

            menu.setText(sdf1.format(d));
        };
        new Timer(1, time).start();
    }

    public static void debugMessage(String s) {

        DebugView.appendDebugTextArea(s + "\n");
    }

    public static void message(String s) {

        messageLabel.setText(s);
        debugMessage(s);
    }

    /*method: getComList()
     * purpose: 연결되어 있는 모든 포트를 찾아 그 이름을 리턴한다.
     * comment: 
     * history: 
     *return type: List<String> portNameList
     */
    private List getComList() {
        List<String> portNameList = new ArrayList<>();
        // String portStr="";
        try {
            Enumeration e = CommPortIdentifier.getPortIdentifiers();
            //   + e.hasMoreElements()
            //  logTextArea.append("  연결된 모든 포트를 조회합니다. 포트의 세부정보를 보시려면 장치관리자를 이용해주세요." + "\n");

            while (e.hasMoreElements()) {
                CommPortIdentifier com = (CommPortIdentifier) e.nextElement();
                switch (com.getPortType()) {
                    case CommPortIdentifier.PORT_SERIAL:
                        portNameList.add(com.getName());

                        break;
                }
            }

            if (DEBUG) {

                System.out.println("com port: " + portNameList.toString() + "\n");
            }
        } catch (Exception e) {
            debugMessage(e.toString());
            System.out.println(e.toString());
        }
        return portNameList;
    }

    private String getDigitStr(String input) {
        int i = 0;
        while (input.startsWith("*")) {
            i++;
        }
        if (i != 0) {
            return input.substring(i);
        } else {
            return input;
        }
    }

    private void loadCSVtoTable() {

        //  final ArrayList<BloodPressure> bpList = TempData.getInstance().getAll();
        final ArrayList<BloodPressure> bpList = CSVControl.getInstance().load();
        tableModel = (DefaultTableModel) bpTable.getModel();
        int rowCnt = bpTable.getRowCount();

        for (BloodPressure bp : bpList) {

            tableModel.addRow(new Object[]{++rowCnt, bp.getStrTime(), bp.getPortNum(), bp.getSys(),
                bp.getDia(), bp.getPr()});

//        for(Object s:row)
//         System.out.print(s.toString()+"\t");
//          System.out.println();
        }

        //     rowCnt = mesurementTable.getRowCount();
        //non editable
        tableModel.fireTableDataChanged();
//resultTable.repaint();
//load last measurement for each ports

        int totalRow = tableModel.getRowCount();

        int currentRow = totalRow ;
        while (--currentRow >= 0) {
            BloodPressure bp = bpList.get(currentRow);
            if (bp.getPortNum()==1) {            
                    
        String sysStr = bp.getSysState() + bp.getSys();
        String diaStr = bp.getDiaState() + bp.getDia();
        String prStr = bp.getPrState() + bp.getPr();        
        String timeStr = bp.getStrTime();

        setTextOnViews(1, sysStr, diaStr, prStr, currentRow+1, timeStr);
              
                break;
            }
        }
     currentRow = totalRow ;
       while (--currentRow >= 0) {
            BloodPressure bp = bpList.get(currentRow);
            if (bp.getPortNum()==2) {            
                    
        String sysStr = bp.getSysState() + bp.getSys();
        String diaStr = bp.getDiaState() + bp.getDia();
        String prStr = bp.getPrState() + bp.getPr();        
        String timeStr = bp.getStrTime();

        setTextOnViews(2, sysStr, diaStr, prStr, currentRow+1, timeStr);
              
                break;
            }
        }
       currentRow = totalRow ;
        while (--currentRow >= 0) {
            BloodPressure bp = bpList.get(currentRow);
            if (bp.getPortNum()==3) {            
                    
        String sysStr = bp.getSysState() + bp.getSys();
        String diaStr = bp.getDiaState() + bp.getDia();
        String prStr = bp.getPrState() + bp.getPr();        
        String timeStr = bp.getStrTime();

        setTextOnViews(3, sysStr, diaStr, prStr, currentRow+1, timeStr);
              
                break;
            }
        }
             currentRow = totalRow ;
        while (--currentRow >= 0) {
            BloodPressure bp = bpList.get(currentRow);
            if (bp.getPortNum()==4) {            
                    
        String sysStr = bp.getSysState() + bp.getSys();
        String diaStr = bp.getDiaState() + bp.getDia();
        String prStr = bp.getPrState() + bp.getPr();        
        String timeStr = bp.getStrTime();

        setTextOnViews(4, sysStr, diaStr, prStr, currentRow+1, timeStr);
              
                break;
            }
        }
       setPortPanColor(Integer.parseInt(tableModel.getValueAt(totalRow - 1, 2).toString()));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        enablePrinting = new javax.swing.JCheckBox();
        messageLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        bpTable = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        port1Pan = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        bp1sys = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        bp1dia = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        bp1pr = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        bp1date = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        bp1num = new javax.swing.JLabel();
        port2Pan = new javax.swing.JPanel();
        jLabel51 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        bp2sys = new javax.swing.JLabel();
        jLabel55 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        bp2dia = new javax.swing.JLabel();
        jLabel58 = new javax.swing.JLabel();
        bp2pr = new javax.swing.JLabel();
        jLabel60 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        bp2num = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        bp2date = new javax.swing.JLabel();
        port3Pan = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        bp3sys = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        bp3dia = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        bp3pr = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        bp3num = new javax.swing.JLabel();
        bp3date = new javax.swing.JLabel();
        port4Pan = new javax.swing.JPanel();
        jLabel61 = new javax.swing.JLabel();
        jLabel62 = new javax.swing.JLabel();
        jLabel63 = new javax.swing.JLabel();
        bp4sys = new javax.swing.JLabel();
        jLabel65 = new javax.swing.JLabel();
        jLabel66 = new javax.swing.JLabel();
        bp4dia = new javax.swing.JLabel();
        jLabel68 = new javax.swing.JLabel();
        bp4pr = new javax.swing.JLabel();
        jLabel70 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        bp4num = new javax.swing.JLabel();
        bp4date = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        jMenu5 = new javax.swing.JMenu();
        settingMenu = new javax.swing.JMenu();
        connectMenu = new javax.swing.JMenuItem();
        disconnectMenu = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        AESettingMenuItem = new javax.swing.JMenuItem();
        templateMenuItem = new javax.swing.JCheckBoxMenuItem();
        jMenu1 = new javax.swing.JMenu();
        viewMenu = new javax.swing.JMenu();
        renewTableMenuItem = new javax.swing.JMenuItem();
        clearTableMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        fileMenu = new javax.swing.JMenu();
        openFileMenuItem = new javax.swing.JMenuItem();
        saveFileMenuItem = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        testMenu = new javax.swing.JMenu();
        testModeMenuItem = new javax.swing.JCheckBoxMenuItem();
        testPrintMenuItem = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        watchMenu = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Green ver 2.3");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setMinimumSize(null);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.PAGE_AXIS));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        enablePrinting.setSelected(true);
        enablePrinting.setText("자동 인쇄");
        enablePrinting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enablePrintingActionPerformed(evt);
            }
        });

        messageLabel.setFont(new java.awt.Font("맑은 고딕", 1, 12)); // NOI18N
        messageLabel.setForeground(new java.awt.Color(0, 153, 204));
        messageLabel.setText(" ");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(enablePrinting)
                .addGap(42, 42, 42)
                .addComponent(messageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 817, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(132, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(enablePrinting)
                    .addComponent(messageLabel))
                .addGap(13, 13, 13))
        );

        getContentPane().add(jPanel3);

        jScrollPane1.setMaximumSize(new java.awt.Dimension(510, 32767));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(470, 27));
        jScrollPane1.setName(""); // NOI18N
        jScrollPane1.setPreferredSize(new java.awt.Dimension(470, 402));

        bpTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "번호", "측정일시", "혈압계", "SYS(mmHg)", "DIA((mmHg)", "PR(bpm)"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        bpTable.setFillsViewportHeight(true);
        bpTable.setMaximumSize(new java.awt.Dimension(580, 0));
        bpTable.setMinimumSize(new java.awt.Dimension(480, 0));
        bpTable.setRowHeight(20);
        bpTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(bpTable);
        if (bpTable.getColumnModel().getColumnCount() > 0) {
            bpTable.getColumnModel().getColumn(0).setPreferredWidth(55);
            bpTable.getColumnModel().getColumn(0).setMaxWidth(60);
            bpTable.getColumnModel().getColumn(1).setPreferredWidth(150);
            bpTable.getColumnModel().getColumn(1).setMaxWidth(150);
            bpTable.getColumnModel().getColumn(2).setPreferredWidth(65);
            bpTable.getColumnModel().getColumn(2).setMaxWidth(70);
            bpTable.getColumnModel().getColumn(3).setPreferredWidth(110);
            bpTable.getColumnModel().getColumn(3).setMaxWidth(120);
            bpTable.getColumnModel().getColumn(4).setPreferredWidth(110);
            bpTable.getColumnModel().getColumn(4).setMaxWidth(120);
            bpTable.getColumnModel().getColumn(5).setPreferredWidth(90);
            bpTable.getColumnModel().getColumn(5).setMaxWidth(100);
        }

        jSplitPane2.setLeftComponent(jScrollPane1);
        jScrollPane1.getAccessibleContext().setAccessibleName("");

        jPanel4.setBackground(new java.awt.Color(204, 204, 204));
        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel4.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jPanel4.setMaximumSize(new java.awt.Dimension(440, 32767));
        jPanel4.setPreferredSize(new java.awt.Dimension(440, 600));
        jPanel4.setRequestFocusEnabled(false);
        jPanel4.setVerifyInputWhenFocusTarget(false);
        jPanel4.setLayout(new java.awt.GridLayout(2, 2, 10, 10));

        port1Pan.setBackground(new java.awt.Color(255, 255, 255));
        port1Pan.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        port1Pan.setMaximumSize(new java.awt.Dimension(250, 32767));
        port1Pan.setPreferredSize(new java.awt.Dimension(200, 200));
        port1Pan.setRequestFocusEnabled(false);

        jLabel3.setText("SYS");

        jLabel4.setText("DIA");

        jLabel5.setText("PR");

        bp1sys.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        bp1sys.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        bp1sys.setText(" ");

        jLabel7.setText("mm Hg");

        jLabel8.setText("mm Hg");

        bp1dia.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        bp1dia.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        bp1dia.setText(" ");

        jLabel10.setText("bpm");

        bp1pr.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        bp1pr.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        bp1pr.setText(" ");

        jLabel12.setBackground(new java.awt.Color(102, 255, 102));
        jLabel12.setFont(new java.awt.Font("HY헤드라인M", 0, 14)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("혈압계 #1");
        jLabel12.setToolTipText("");

        jLabel1.setText("번호");

        bp1date.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        bp1date.setText(" ");

        jLabel15.setText("측정일시");

        bp1num.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        bp1num.setText(" ");

        javax.swing.GroupLayout port1PanLayout = new javax.swing.GroupLayout(port1Pan);
        port1Pan.setLayout(port1PanLayout);
        port1PanLayout.setHorizontalGroup(
            port1PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(port1PanLayout.createSequentialGroup()
                .addGroup(port1PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(port1PanLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bp1num, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bp1date, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 8, Short.MAX_VALUE))
                    .addComponent(jLabel12, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, port1PanLayout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addGroup(port1PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(port1PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(bp1pr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bp1dia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bp1sys, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(port1PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel10))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        port1PanLayout.setVerticalGroup(
            port1PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(port1PanLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel12)
                .addGap(18, 18, 18)
                .addGroup(port1PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(bp1date)
                    .addComponent(jLabel1)
                    .addComponent(bp1num))
                .addGap(18, 18, 18)
                .addGroup(port1PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel3)
                    .addComponent(bp1sys)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(port1PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel4)
                    .addComponent(bp1dia)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(port1PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel5)
                    .addComponent(bp1pr)
                    .addComponent(jLabel10))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.add(port1Pan);

        port2Pan.setBackground(new java.awt.Color(255, 255, 255));
        port2Pan.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        port2Pan.setMaximumSize(new java.awt.Dimension(250, 32767));
        port2Pan.setPreferredSize(new java.awt.Dimension(200, 200));
        port2Pan.setVerifyInputWhenFocusTarget(false);

        jLabel51.setText("SYS");

        jLabel52.setText("DIA");

        jLabel53.setText("PR");

        bp2sys.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        bp2sys.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        bp2sys.setText(" ");

        jLabel55.setText("mm Hg");

        jLabel56.setText("mm Hg");

        bp2dia.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        bp2dia.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        bp2dia.setText(" ");

        jLabel58.setText("bpm");

        bp2pr.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        bp2pr.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        bp2pr.setText(" ");

        jLabel60.setBackground(new java.awt.Color(102, 255, 102));
        jLabel60.setFont(new java.awt.Font("HY헤드라인M", 0, 14)); // NOI18N
        jLabel60.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel60.setText("혈압계 #2");
        jLabel60.setToolTipText("");

        jLabel17.setText("측정일시");

        bp2num.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        bp2num.setText(" ");

        jLabel18.setText("번호");

        bp2date.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        bp2date.setText(" ");

        javax.swing.GroupLayout port2PanLayout = new javax.swing.GroupLayout(port2Pan);
        port2Pan.setLayout(port2PanLayout);
        port2PanLayout.setHorizontalGroup(
            port2PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(port2PanLayout.createSequentialGroup()
                .addGroup(port2PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(port2PanLayout.createSequentialGroup()
                        .addGap(55, 55, 55)
                        .addGroup(port2PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel53, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel52, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel51, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(port2PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(bp2sys, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bp2dia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bp2pr, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(port2PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel55)
                            .addComponent(jLabel56, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel58)))
                    .addGroup(port2PanLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel60, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(port2PanLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bp2num, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17)
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bp2date, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        port2PanLayout.setVerticalGroup(
            port2PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(port2PanLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel60)
                .addGap(18, 18, 18)
                .addGroup(port2PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(bp2num)
                    .addComponent(jLabel17)
                    .addComponent(bp2date))
                .addGap(18, 18, 18)
                .addGroup(port2PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel51)
                    .addComponent(bp2sys)
                    .addComponent(jLabel55))
                .addGap(18, 18, 18)
                .addGroup(port2PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel56)
                    .addComponent(bp2dia)
                    .addComponent(jLabel52))
                .addGap(18, 18, 18)
                .addGroup(port2PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel53)
                    .addComponent(bp2pr)
                    .addComponent(jLabel58))
                .addContainerGap(11, Short.MAX_VALUE))
        );

        jPanel4.add(port2Pan);

        port3Pan.setBackground(new java.awt.Color(255, 255, 255));
        port3Pan.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        port3Pan.setMaximumSize(new java.awt.Dimension(250, 32767));
        port3Pan.setPreferredSize(new java.awt.Dimension(200, 200));

        jLabel23.setText("SYS");

        jLabel33.setText("DIA");

        jLabel43.setText("PR");

        bp3sys.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        bp3sys.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        bp3sys.setText(" ");

        jLabel45.setText("mm Hg");

        jLabel46.setText("mm Hg");

        bp3dia.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        bp3dia.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        bp3dia.setText(" ");

        jLabel48.setText("bpm");

        bp3pr.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        bp3pr.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        bp3pr.setText(" ");

        jLabel50.setBackground(new java.awt.Color(102, 255, 102));
        jLabel50.setFont(new java.awt.Font("HY헤드라인M", 0, 14)); // NOI18N
        jLabel50.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel50.setText("혈압계 #3");
        jLabel50.setToolTipText("");

        jLabel21.setText("측정일시");

        jLabel22.setText("번호");

        bp3num.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        bp3num.setText(" ");

        bp3date.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        bp3date.setText(" ");

        javax.swing.GroupLayout port3PanLayout = new javax.swing.GroupLayout(port3Pan);
        port3Pan.setLayout(port3PanLayout);
        port3PanLayout.setHorizontalGroup(
            port3PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(port3PanLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(port3PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel50, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(port3PanLayout.createSequentialGroup()
                        .addComponent(jLabel22)
                        .addGap(6, 6, 6)
                        .addComponent(bp3num, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bp3date, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 26, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, port3PanLayout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addGroup(port3PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(port3PanLayout.createSequentialGroup()
                        .addGroup(port3PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel43, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(port3PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(bp3dia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bp3pr, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(port3PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel46)
                            .addComponent(jLabel48, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(port3PanLayout.createSequentialGroup()
                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(bp3sys, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel45)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        port3PanLayout.setVerticalGroup(
            port3PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(port3PanLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel50)
                .addGap(18, 18, 18)
                .addGroup(port3PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel21)
                    .addComponent(bp3date)
                    .addComponent(jLabel22)
                    .addComponent(bp3num))
                .addGap(18, 18, 18)
                .addGroup(port3PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel23)
                    .addComponent(bp3sys)
                    .addComponent(jLabel45))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(port3PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel33)
                    .addComponent(bp3dia)
                    .addComponent(jLabel46))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(port3PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel43)
                    .addComponent(bp3pr)
                    .addComponent(jLabel48))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        jPanel4.add(port3Pan);

        port4Pan.setBackground(new java.awt.Color(255, 255, 255));
        port4Pan.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        port4Pan.setMaximumSize(new java.awt.Dimension(250, 32767));
        port4Pan.setPreferredSize(new java.awt.Dimension(200, 200));

        jLabel61.setText("SYS");

        jLabel62.setText("DIA");

        jLabel63.setText("PR");

        bp4sys.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        bp4sys.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        bp4sys.setText(" ");

        jLabel65.setText("mm Hg");

        jLabel66.setText("mm Hg");

        bp4dia.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        bp4dia.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        bp4dia.setText(" ");

        jLabel68.setText("bpm");

        bp4pr.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        bp4pr.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        bp4pr.setText(" ");

        jLabel70.setBackground(new java.awt.Color(102, 255, 102));
        jLabel70.setFont(new java.awt.Font("HY헤드라인M", 0, 14)); // NOI18N
        jLabel70.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel70.setText("혈압계 #4");
        jLabel70.setToolTipText("");

        jLabel26.setText("측정일시");

        jLabel27.setText("번호");

        bp4num.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        bp4num.setText(" ");

        bp4date.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        bp4date.setText(" ");

        javax.swing.GroupLayout port4PanLayout = new javax.swing.GroupLayout(port4Pan);
        port4Pan.setLayout(port4PanLayout);
        port4PanLayout.setHorizontalGroup(
            port4PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(port4PanLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(port4PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(port4PanLayout.createSequentialGroup()
                        .addComponent(jLabel27)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bp4num, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17)
                        .addComponent(jLabel26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bp4date, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(17, Short.MAX_VALUE))
                    .addGroup(port4PanLayout.createSequentialGroup()
                        .addGap(56, 56, 56)
                        .addComponent(jLabel70, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, port4PanLayout.createSequentialGroup()
                        .addGap(55, 55, 55)
                        .addGroup(port4PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel63, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel62, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel61, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(port4PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(bp4sys, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bp4dia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bp4pr, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(port4PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel66)
                            .addComponent(jLabel65)
                            .addComponent(jLabel68))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        port4PanLayout.setVerticalGroup(
            port4PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(port4PanLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel70)
                .addGap(18, 18, 18)
                .addGroup(port4PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27)
                    .addComponent(bp4num)
                    .addComponent(jLabel26)
                    .addComponent(bp4date))
                .addGap(18, 18, 18)
                .addGroup(port4PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(bp4sys)
                    .addComponent(jLabel61)
                    .addComponent(jLabel65))
                .addGap(18, 18, 18)
                .addGroup(port4PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel62)
                    .addComponent(bp4dia)
                    .addComponent(jLabel66))
                .addGap(18, 18, 18)
                .addGroup(port4PanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel63)
                    .addComponent(bp4pr)
                    .addComponent(jLabel68))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.add(port4Pan);

        jSplitPane2.setRightComponent(jPanel4);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1085, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1085, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 487, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1);

        jPanel2.setMaximumSize(new java.awt.Dimension(36767, 55));

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/전북대병원로고.png"))); // NOI18N
        jLabel13.setAutoscrolls(true);
        jLabel13.setMaximumSize(new java.awt.Dimension(200, 35));
        jLabel13.setMinimumSize(new java.awt.Dimension(200, 35));
        jLabel13.setName(""); // NOI18N
        jLabel13.setPreferredSize(new java.awt.Dimension(200, 35));
        jLabel13.setVerifyInputWhenFocusTarget(false);

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/teasofts40.png"))); // NOI18N
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel6MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 656, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addGap(45, 45, 45))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabel13.getAccessibleContext().setAccessibleDescription("");

        getContentPane().add(jPanel2);

        menuBar.setBackground(new java.awt.Color(28, 96, 48));
        menuBar.setForeground(new java.awt.Color(255, 255, 255));
        menuBar.setMargin(null);
        menuBar.setPreferredSize(new java.awt.Dimension(128, 50));

        jMenu5.setForeground(new java.awt.Color(204, 204, 204));
        jMenu5.setEnabled(false);
        jMenu5.setFont(new java.awt.Font("맑은 고딕", 1, 12)); // NOI18N
        jMenu5.setMinimumSize(new java.awt.Dimension(20, 0));
        jMenu5.setName(""); // NOI18N
        jMenu5.setPreferredSize(new java.awt.Dimension(20, 19));
        menuBar.add(jMenu5);

        settingMenu.setForeground(new java.awt.Color(255, 255, 255));
        settingMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/setting30.png"))); // NOI18N
        settingMenu.setToolTipText("설정");

        connectMenu.setText("연결");
        connectMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectMenuActionPerformed(evt);
            }
        });
        settingMenu.add(connectMenu);
        connectMenu.setEnabled(!stateConnection);

        disconnectMenu.setText("연결해제");
        disconnectMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectMenuActionPerformed(evt);
            }
        });
        settingMenu.add(disconnectMenu);
        disconnectMenu.setEnabled(stateConnection);
        settingMenu.add(jSeparator2);

        AESettingMenuItem.setText("NCS/CS 설정");
        AESettingMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AESettingMenuItemActionPerformed(evt);
            }
        });
        settingMenu.add(AESettingMenuItem);

        templateMenuItem.setSelected(true);
        templateMenuItem.setText("템플릿 사용");
        templateMenuItem.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                templateMenuItemItemStateChanged(evt);
            }
        });
        settingMenu.add(templateMenuItem);

        menuBar.add(settingMenu);

        jMenu1.setForeground(new java.awt.Color(204, 204, 204));
        jMenu1.setText("|");
        jMenu1.setEnabled(false);
        jMenu1.setFont(new java.awt.Font("맑은 고딕", 1, 12)); // NOI18N
        menuBar.add(jMenu1);

        viewMenu.setForeground(new java.awt.Color(255, 255, 255));
        viewMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/monitor30_2.png"))); // NOI18N
        viewMenu.setToolTipText("보기");

        renewTableMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        renewTableMenuItem.setText("새로고침");
        renewTableMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renewTableMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(renewTableMenuItem);

        clearTableMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        clearTableMenuItem.setText("화면지우기");
        clearTableMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearTableMenuItemActionPerformed(evt);
            }
        });
        viewMenu.add(clearTableMenuItem);

        menuBar.add(viewMenu);

        jMenu2.setForeground(new java.awt.Color(204, 204, 204));
        jMenu2.setText("|");
        jMenu2.setEnabled(false);
        jMenu2.setFont(new java.awt.Font("맑은 고딕", 1, 12)); // NOI18N
        menuBar.add(jMenu2);

        fileMenu.setForeground(new java.awt.Color(255, 255, 255));
        fileMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/file30_1.png"))); // NOI18N
        fileMenu.setToolTipText("파일");

        openFileMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openFileMenuItem.setText("파일 열기");
        openFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openFileMenuItem);

        saveFileMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        saveFileMenuItem.setText("파일 저장");
        saveFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveFileMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveFileMenuItem);

        menuBar.add(fileMenu);

        jMenu3.setForeground(new java.awt.Color(204, 204, 204));
        jMenu3.setText("|");
        jMenu3.setEnabled(false);
        jMenu3.setFont(new java.awt.Font("맑은 고딕", 1, 12)); // NOI18N
        menuBar.add(jMenu3);

        testMenu.setForeground(new java.awt.Color(255, 255, 255));
        testMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/bug30.png"))); // NOI18N
        testMenu.setToolTipText("프로그램 디버깅");

        testModeMenuItem.setText("테스트 모드");
        testModeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testModeMenuItemActionPerformed(evt);
            }
        });
        testMenu.add(testModeMenuItem);

        testPrintMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        testPrintMenuItem.setText("테스트 인쇄");
        testPrintMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testPrintMenuItemActionPerformed(evt);
            }
        });
        testMenu.add(testPrintMenuItem);

        jMenuItem1.setText("상태창");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        testMenu.add(jMenuItem1);

        menuBar.add(testMenu);
        menuBar.add(Box.createHorizontalGlue());

        jMenu4.setForeground(new java.awt.Color(204, 204, 204));
        jMenu4.setText("|");
        jMenu4.setEnabled(false);
        jMenu4.setFont(new java.awt.Font("맑은 고딕", 1, 12)); // NOI18N
        menuBar.add(jMenu4);

        watchMenu.setForeground(new java.awt.Color(255, 255, 255));
        menuBar.add(watchMenu);
        runWatchMenu(watchMenu);
        watchMenu.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                syncTime();
            }
        });

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    public static void tableRepaint() {
        tableModel.fireTableDataChanged();
        // mesurementTable.repaint();
    }

    private void renewTableMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renewTableMenuItemActionPerformed

        clearTable();
        clearPortView();
        loadCSVtoTable();
        tableModel.fireTableDataChanged();
        // mesurementTable.repaint();

    }//GEN-LAST:event_renewTableMenuItemActionPerformed

    private void testPrintMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testPrintMenuItemActionPerformed
        try {
            message("test printing done");
            if (System.currentTimeMillis() <= lastSyncTime) {

                debugMessage("current time " + new Date() + " is behind from server time ");
                //JOptionPane.showConfirmDialog(null, "현시간이 마지막 동기화된 시간보다 늦습니다. 시간 동기화를 하여 주십시오.", "에러", JOptionPane.CLOSED_OPTION);
                message("현시간이 마지막 동기화된 시간보다 늦습니다. 시간 동기화를 시작합니다.");
                syncTime();
            }

            //측정 없이도 프린트 기능만 테스트 하기 위해 임의의 데이터를 만들어 프린트 시킴
            BloodPressure bp = new BloodPressure("bp,00000000000000000001,2015/08/02,15:42,080,092,018,120,0\r", sdf.format(new Date()), 1);
            updateTable(bp);
            if (enablePrinting.isSelected()) {
                PrintHtml.getInstance().print(bp, useTemplate);
            }
            try {
                CSVControl.getInstance().save(bp);
            } catch (Exception ex) {
                Logger.getLogger(Green.class.getName()).log(Level.SEVERE, null, ex);
            }
            //    TempData.getInstance().add(bp);

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            //System.out.println("Error: I/O 오류");
            message("Error: I/O 오류");

        } catch (PrinterException e1) {
// TODO Auto-generated catch block
            //      System.out.println("Error: 인쇄오류. 프린터와의 연결 상태를 확인해주세요.");
            message(" Error: 인쇄오류. 프린터와의 연결 상태를 확인해주세요.");

        }
    }//GEN-LAST:event_testPrintMenuItemActionPerformed

    private void testModeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testModeMenuItemActionPerformed
        // DEBUG= DEBUG? DEBUG : testModeMenuItem.isSelected();
        PrintHtml.DEBUG = testModeMenuItem.isSelected();
    }//GEN-LAST:event_testModeMenuItemActionPerformed

    private void clearTableMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearTableMenuItemActionPerformed
        clearTable();
        clearPortView();
    }//GEN-LAST:event_clearTableMenuItemActionPerformed
    List<MultiSerial> portList = new ArrayList<MultiSerial>();

    private void jLabel6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseClicked
        String str = "http://www.teasofts.com";
        //homepageLinkLabel.getText();
        //  System.out.println(str);
        //jLa"http://www.teasofts.com";
        openWebpage(str);
    }//GEN-LAST:event_jLabel6MouseClicked

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        debugWindow.setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void saveFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveFileMenuItemActionPerformed
        JFileChooser saveFile = new JFileChooser() {
            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {

                    int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);

                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }
        };
        String extension = ".csv";
        saveFile.setFileFilter(new MyCustomFilter(extension));
        int returnVal = saveFile.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            // System.out.println("000");
            File file = saveFile.getSelectedFile();
            String fileName = file.getPath();
            // String extension = saveFile.getFileFilter().getDescription();
            int offset = fileName.lastIndexOf(".");
            //   System.out.println("  offset=  " + offset + "   " + !fileName.endsWith(extension) + !fileName.endsWith(extension.toUpperCase()));

            if (offset == -1 || (!fileName.endsWith(extension) && !fileName.endsWith(extension.toUpperCase()))) {
                //    String message = "file suffix was not specified";
                //     JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
                file = new File(file.getAbsolutePath() + extension);

            }
            try {
                String str = FileUtils.readFileToString(CSVControl.csvFile, "utf-8");

                FileOutputStream writer1 = new FileOutputStream(file, false);

                writer1.write(str.getBytes("utf-8"));
                writer1.close();

                // FileUtils.copyFile(CSVControl.csvFile, file);
                FileOutputStream writer = new FileOutputStream(CSVControl.csvFile, false);
                writer.write(("").getBytes());
                writer.close();
                //    fw = new FileWriter(file, true);

            } catch (IOException ex) {
                //    System.out.println(ex.getStackTrace());
                Logger.getLogger(Green.class.getName()).log(Level.SEVERE, null, ex);
            }

            clearTable();
            clearPortView();
            // try {
            // boolean result = writeSettingFile(file);
            // message("저장되었습니다.");
            //   } catch (IOException ex) {
            // JOptionPane.showConfirmDialog(this, "오류로인해 저장할 수 없습니다", "Error", JOptionPane.YES_NO_CANCEL_OPTION);
            //  Logger.getLogger(Rooibos.class.getName()).log(Level.SEVERE, null, ex);
            //}
        }
    }//GEN-LAST:event_saveFileMenuItemActionPerformed

    private void openFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileMenuItemActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        //fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setFileFilter(new MyCustomFilter(".csv"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            loadCSVFromSavedFile(selectedFile);
        }
    }//GEN-LAST:event_openFileMenuItemActionPerformed

    private void enablePrintingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enablePrintingActionPerformed

        MultiSerial.doPrinting = enablePrinting.isSelected();
        //  System.out.println("인쇄" + MultiSerial.doPrinting);

    }//GEN-LAST:event_enablePrintingActionPerformed

    private void templateMenuItemItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_templateMenuItemItemStateChanged
        useTemplate = templateMenuItem.isSelected();
    }//GEN-LAST:event_templateMenuItemItemStateChanged

    /*set ncs cs  value */
    private void AESettingMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AESettingMenuItemActionPerformed
        AESettingDialog aesetting = new AESettingDialog(this, true);//, , setting[END], setting[GROUP], selectedGroupOfSubjectArr, this.groupSettingBT);
        aesetting.setVisible(true);
        if (aesetting.getReturnStatus() == AESettingDialog.RET_OK) {
            try {
                reader.read();
            } catch (IOException ex) {
                Logger.getLogger(Green.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /*        groupSelectDialog.setVisible(true);
        if (groupSelectDialog.getReturnStatus() == 0) {
            groupSelectDialog = null;
            return;
        }
        } else {
            groupSelectDialog.repaint(setting[START], setting[END], setting[GROUP], selectedGroupOfSubjectArr);
            //   System.out.println("groupSettingBTActionPerformed\tstart="+setting[START]+"\t end="+setting[END]+"\tgroup="+ setting[GROUP]+"\t selectedGroupOfSubjectArr size="+selectedGroupOfSubjectArr.length);
            groupSelectDialog.setVisible(true);
        }

        if (groupSelectDialog.getReturnStatus() == 1) {//ok

            setting[GROUP] = groupSelectDialog.getGroupNum();
            periodSettingTable.getColumnModel().getColumn(2).setCellEditor(new GroupCheckListEditor(setting[GROUP]));
            periodSettingTable.repaint();
            selectedGroupOfSubjectArr = groupSelectDialog.getSelectedValues();

        }*/

    }//GEN-LAST:event_AESettingMenuItemActionPerformed
    public void connect() {
        //  stateConnection = !stateConnection;
        // String state = stateConnection ? "연결끊기" : "연결";
        //  runButton.setText(state);

        try {
            //쓰레드 돌아가게끔 설정
            MultiSerial.isRunning = true;
            //port 순서대로 번호에 맞춰 놓고 매칭시킨다.
            List<String> comList = getComList();

//            if (comList.size() > 4) {
//                i = comList.size() - 4;//if com 이 5개 이상 잡히면 일단 맨 처음것은 제외
//            }
            // int portNum = 1;
//            for (int i = 0; i < comList.size(); i++) {
//                MultiSerial ms = new MultiSerial();
//                ms.connect(comList.get(i), i+1);//i=0일때 port번호는 1부터 시작.
//                portList.add(ms);
//            }
            int i = 0;
            if (comList.size() > 4) {
                i = comList.size() - 4;//if com 이 5개 이상 잡히면 일단 맨 처음것은 제외
            }
            int portNum = 1;
            for (; i < comList.size(); i++) {
                MultiSerial ms = new MultiSerial();

                ms.connect(comList.get(i), portNum++);

                portList.add(ms);
            }

            if (portList.size() == 0) {
               if(!DEBUG) message("연결이 되어 있지 않습니다. 허브와 연결하여주십시오.");
                return;
            }
            message(portList.size() + "개의 포트와 연결 되었습니다.");
            stateConnection = true;
            disconnectMenu.setEnabled(stateConnection);
            connectMenu.setEnabled(!stateConnection);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            message(e.toString());
            stateConnection = false;
            System.out.println("catch error====" + e.toString());

        } finally {

        }

    }
    private void connectMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectMenuActionPerformed
        connect();
    }//GEN-LAST:event_connectMenuActionPerformed


    private void disconnectMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectMenuActionPerformed

        disConnect();
    }//GEN-LAST:event_disconnectMenuActionPerformed
    public void disConnect() {
        //해제할것. 

        for (int i = portList.size() - 1; i >= 0; i--) {
            MultiSerial ms = portList.get(i);

            try {
                ms.disconnect();
            } catch (Exception ex) {
                //  System.out.println("eeeor====" + ex.toString());
                debugMessage("disconnecting error:  " + ex.toString());
                return;
            }
            portList.remove(i);
        }

        message(sdf.format(new Date()) + "허브와 연결이 해제되었습니다.");
        stateConnection = false;
        disconnectMenu.setEnabled(stateConnection);
        connectMenu.setEnabled(!stateConnection);

    }

    public void openWebpage(String uristr) {
        URI url = null;
        try {
            url = new URI(uristr);
        } catch (URISyntaxException ex) {
            Logger.getLogger(Green.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (Desktop.isDesktopSupported()) {
            try {
                // Windows
                Desktop.getDesktop().browse(url);
            } catch (IOException ex) {
                Logger.getLogger(Green.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            // Ubuntu
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("/usr/bin/firefox -new-window " + url);
            } catch (IOException ex) {
                Logger.getLogger(Green.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void clearTable() {
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
        tableModel.fireTableDataChanged();
        //    mesurementTable.repaint();
    }

    //setIcon(new ImageIcon(ImageIO.read(BooleanIconRenderer.class.getResourceAsStream(iconFilename))));
    protected static void updateTable(BloodPressure bp) {
        // int m=bpTable.getRowCount() + 1;
        int portNum = bp.getPortNum();
        String sysStr = bp.getSysState() + bp.getSys();
        String diaStr = bp.getDiaState() + bp.getDia();
        String prStr = bp.getPrState() + bp.getPr();
        int num = tableModel.getRowCount() + 1;
        String timeStr = bp.getStrTime();

        //     JLabel printIcon = new JLabel();
        //   printIcon.setOpaque(false); 
        ////     printIcon.setText("print");
        //  printIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Img/print202.png")));
        // printIcon.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e ){ System.out.println("mouseClicked"); } });
        //  System.out.println("in updating");
        Object row[] = new Object[]{num, timeStr, bp.getPortNum(), bp.getSys(),
            bp.getDia(), bp.getPr()};
        tableModel.addRow(row);

        tableModel.fireTableDataChanged();
        //    System.out.println("in updating" + tableModel.getColumnCount());
        // Object row[] = {, bp.getStrTime(), bp.getPortNum(), bp.getSys(), bp.getDia(), bp.getPr()};
        //  tableModel.addRow(row);

        //update 4 windows
        setTextOnViews(portNum, sysStr, diaStr, prStr, num, timeStr);
        //mesurementTable.repaint();
    }

    public static void setPortPanColor(int portNum) {
        Color yellow = new Color(255, 255, 153);
        Color white = new Color(255, 255, 255);
        if (portNum == 1) {
            port1Pan.setBackground(yellow);
            port2Pan.setBackground(white);
            port3Pan.setBackground(white);
            port4Pan.setBackground(white);

        } else if (portNum == 2) {
            port1Pan.setBackground(white);
            port2Pan.setBackground(yellow);
            port3Pan.setBackground(white);
            port4Pan.setBackground(white);

        } else if (portNum == 3) {
            port1Pan.setBackground(white);
            port2Pan.setBackground(white);
            port3Pan.setBackground(yellow);
            port4Pan.setBackground(white);

        } else if (portNum == 4) {
            port1Pan.setBackground(white);
            port2Pan.setBackground(white);
            port3Pan.setBackground(white);
            port4Pan.setBackground(yellow);

        }

    }

    public static void setTextOnViews(int portNum, String sysStr, String diaStr, String prStr, int num, String timeStr) {
        if (portNum == 1) {
            bp1sys.setText(sysStr);
            bp1dia.setText(diaStr);

            bp1pr.setText(prStr);
            bp1num.setText("" + num);
            bp1date.setText(timeStr);

            // port1Pan.setBackground(255,255,153);
        } else if (portNum == 2) {
            bp2sys.setText(sysStr);
            bp2dia.setText(diaStr);

            bp2pr.setText(prStr);
            bp2num.setText("" + num);
            bp2date.setText(timeStr);
        } else if (portNum == 3) {
            bp3sys.setText(sysStr);
            bp3dia.setText(diaStr);

            bp3pr.setText(prStr);
            bp3num.setText("" + num);
            bp3date.setText(timeStr);
        } else if (portNum == 4) {
            bp4sys.setText(sysStr);
            bp4dia.setText(diaStr);
            bp4pr.setText(prStr);
            bp4num.setText("" + num);
            bp4date.setText(timeStr);
        }
        setPortPanColor(portNum);
    }

    public static void clearPortView() {

        bp1sys.setText("");
        bp1dia.setText("");

        bp1pr.setText("");
        bp1num.setText("");
        bp1date.setText("");

        bp2sys.setText("");
        bp2dia.setText("");

        bp2pr.setText("");
        bp2num.setText("");
        bp2date.setText("");

        bp3sys.setText("");
        bp3dia.setText("");

        bp3pr.setText("");
        bp3num.setText("");
        bp3date.setText("");

        bp4sys.setText("");
        bp4dia.setText("");
        bp4pr.setText("");
        bp4num.setText("");
        bp4date.setText("");
        port1Pan.setBackground(Color.white);
        port2Pan.setBackground(Color.white);
        port3Pan.setBackground(Color.white);
        port4Pan.setBackground(Color.white);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Ocean".equals(info.getName())) { //Nimbus   Metal   Ocean 
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Green.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Green.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Green.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Green.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //  javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
//        try {
//       UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
//        } catch (Exception e) {
//        }
        /*log file 만들기   */
        //PrintStream console=System.out;//if this is uncomment, it will show the result on console
        String dir = System.getProperty("user.dir") + "\\";
        File file = new File(dir + "log.txt");
        if (!DEBUG) {
            try {
                long size = file.length();
                FileOutputStream fos;
                if (size > 100000000)//100M
                {
                    fos = new FileOutputStream(file);
                } else {
                    fos = new FileOutputStream(file, true);
                }
                PrintStream ps = new PrintStream(fos);
                System.setOut(ps);
                //System.setOut(console);//기존방법
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                Logger.getLogger(Green.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Green().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem AESettingMenuItem;
    private static javax.swing.JLabel bp1date;
    private static javax.swing.JLabel bp1dia;
    private static javax.swing.JLabel bp1num;
    private static javax.swing.JLabel bp1pr;
    private static javax.swing.JLabel bp1sys;
    private static javax.swing.JLabel bp2date;
    private static javax.swing.JLabel bp2dia;
    private static javax.swing.JLabel bp2num;
    private static javax.swing.JLabel bp2pr;
    private static javax.swing.JLabel bp2sys;
    private static javax.swing.JLabel bp3date;
    private static javax.swing.JLabel bp3dia;
    private static javax.swing.JLabel bp3num;
    private static javax.swing.JLabel bp3pr;
    private static javax.swing.JLabel bp3sys;
    private static javax.swing.JLabel bp4date;
    private static javax.swing.JLabel bp4dia;
    private static javax.swing.JLabel bp4num;
    private static javax.swing.JLabel bp4pr;
    private static javax.swing.JLabel bp4sys;
    private javax.swing.JTable bpTable;
    private javax.swing.JMenuItem clearTableMenuItem;
    private javax.swing.JMenuItem connectMenu;
    private javax.swing.JMenuItem disconnectMenu;
    private javax.swing.JCheckBox enablePrinting;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JMenuBar menuBar;
    public static javax.swing.JLabel messageLabel;
    private javax.swing.JMenuItem openFileMenuItem;
    private static javax.swing.JPanel port1Pan;
    private static javax.swing.JPanel port2Pan;
    private static javax.swing.JPanel port3Pan;
    private static javax.swing.JPanel port4Pan;
    private javax.swing.JMenuItem renewTableMenuItem;
    private javax.swing.JMenuItem saveFileMenuItem;
    private javax.swing.JMenu settingMenu;
    private javax.swing.JCheckBoxMenuItem templateMenuItem;
    private javax.swing.JMenu testMenu;
    private javax.swing.JCheckBoxMenuItem testModeMenuItem;
    private javax.swing.JMenuItem testPrintMenuItem;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenu watchMenu;
    // End of variables declaration//GEN-END:variables

    private void loadCSVFromSavedFile(File file) {

        LoadCSVFileDialog sfDialog = new LoadCSVFileDialog(file);

        sfDialog.setVisible(true);

        //  final ArrayList<BloodPressure> bpList = TempData.getInstance().getAll();
    }

    public static void syncTime() {
        String hosts[] = new String[]{"0.asia.pool.ntp.org", "time2.kriss.re.kr", "2.kr.pool.ntp.org", "time-a.nist.gov"};

        TimeSyncNTPClient ntp = null;
        long result = 0;
        long diff = 0;
        for (String host : hosts) {
            try {

                ntp = new TimeSyncNTPClient(host, 100);
                Thread.sleep(1000);
                //  long t1 = System.currentTimeMillis();
                long t2 = ntp.currentTimeMillis();
                long t3 = System.currentTimeMillis();

                Date d = new Date(t2);
                diff = t3 - t2;
                debugMessage(
                        "서버에 연결됨 : " + host
                        + "\n\t" + d + " :  시간차이 = " + diff + " ms\n");

                result = ntp.getTimeInfo().getMessage().getTransmitTimeStamp().getTime();

                if (result > lastSyncTime) {

                    break;
                }
            } catch (Exception e) {
                // System.out.println(e.getStackTrace());
            }
        }

        if (result == 0) {
            JOptionPane.showConfirmDialog(null, "인터넷 서버가 응답하지 않습니다.", "에러", JOptionPane.CLOSED_OPTION);
        } else {
            //   didSync = ntp.syncSystemTime(result);
            if (ntp.syncSystemTime(result)) {
                lastSyncTime = result;
            }
            //    JOptionPane.showConfirmDialog(null, "동기화되었습니다.", "확인", JOptionPane.CLOSED_OPTION);               
        }

    }

    private void tableRenderer() {
        ((DefaultTableCellRenderer) bpTable.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        testModeMenuItem.setSelected(DEBUG);
        tableModel = (DefaultTableModel) bpTable.getModel();

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
//resultTable.setDefaultRenderer(Object.class, centerRenderer);
        for (int x = 0; x < tableModel.getColumnCount(); x++) {
            bpTable.getColumnModel().getColumn(x).setCellRenderer(centerRenderer);
        }

        //double click
        bpTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent tableME) {
                if (tableME.getClickCount() == 2) {
                    JTable table = (JTable) tableME.getSource();
                    int selectedrow = table.getSelectedRow();
                    //   System.out.println("mouse clicked twice");
                    String time = table.getModel().getValueAt(selectedrow, 1).toString();
                    String portNum = table.getModel().getValueAt(selectedrow, 2).toString();
                    //  String systmp=getDigitStr(table.getModel().getValueAt(selectedrow, 3).toString());
//to do  systmp=systmp..replaceAll('*', '');

                    //String cmt=table.getModel().getValueAt(selectedrow, 5).toString();
                    //BloodPressure bp = new BloodPressure(time, portNum, sys, dia, pr);
                    try {

                        if ((enablePrinting.isSelected())) {
                            PrintHtml.DEBUG = testModeMenuItem.isSelected();
                            //   PrintHtml.getInstance().print(bp, useTemplate);
                            String sys = table.getModel().getValueAt(selectedrow, 3).toString();
                            String dia = table.getModel().getValueAt(selectedrow, 4).toString();
                            String pr = table.getModel().getValueAt(selectedrow, 5).toString();
                            //   if (useTemplate) {
                            BloodPressure bp = new BloodPressure(time, portNum, sys, dia, pr);

                            PrintHtml.getInstance().print(bp, useTemplate);
//                            } else {
//                                PrintHtml.getInstance().print(portNum, time, getDigitStr(sys), getDigitStr(dia), getDigitStr(pr), useTemplate);
//                            }
                            // PrintHtml.getInstance().print(portNum, time, sys, dia, pr, useTemplate);

                            message(portNum + " 번 혈압기에서 " + time + "에 측정된 결과를 인쇄했습니다.");
                        }
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        message(" bptolabel ::: 374  Error: I/O 오류");

                    } catch (PrinterException e1) {
                        // TODO Auto-generated catch block

                        e1.printStackTrace();
//  System.out.println("Error: 인쇄오류. 프린터와의 연결 상태를 확인해주세요.");
                        message(" Error: 인쇄오류. 프린터와의 연결 상태를 확인해주세요.");

                    }
                }

            }
        });

    }

//    private  void connectToDivice() {
//        
//       // String result="";
//           stateConnection = !stateConnection;
//      //  String state = stateConnection ? "연결이 끊어졌습니다." : "연결되었습니다.";
//       // runButton.setText(state);
//
//        if (stateConnection) {
//            try {
//                //쓰레드 돌아가게끔 설정
//                MultiSerial.isRunning = true;
//                //port 순서대로 번호에 맞춰 놓고 매칭시킨다. 
//                List<String> comList = getComList();
//                List<MultiSerial> portList = new ArrayList<MultiSerial>();
//                int i = 1;
//                if (comList.size() > 4) {
//                    i = comList.size() - 4;//if com 이 5개 이상 잡히면 일단 맨 처음것은 제외
//                }
//                for (; i < comList.size(); i++) {
//                    MultiSerial ms = new MultiSerial();
//                    ms.connect(comList.get(i - 1), i);
//
//                    portList.add(ms);
//                }
//
//                message(sdf.format(new Date()) + "연결 되었습니다. ");
//            } catch (Exception e) {
//                // TODO Auto-generated catch block
//                message(e.toString());
//            }
//
//        } else {
//            message(sdf.format(new Date()) + "\t연결 해제되었습니다.");
//        }           
//    }
    private static class MyCustomFilter extends javax.swing.filechooser.FileFilter {

        String extention = ".txt";

        public MyCustomFilter(String extention) {
            this.extention = extention;
        }

        @Override
        public boolean accept(File file) {
            return file.isDirectory() || file.getAbsolutePath().endsWith(extention) || file.getAbsolutePath().endsWith(extention.toUpperCase());
        }

        @Override
        public String getDescription() {
            return " documents (*" + extention + ")";
        }
    }
}
