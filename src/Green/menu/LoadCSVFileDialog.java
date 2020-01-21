package Green.menu;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import Green.BloodPressure;
import Green.CSVControl;
import Green.Green;
import Green.PrintHtml;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;


/**
 *
 * @author JHLEE
 */
public  class LoadCSVFileDialog extends javax.swing.JFrame {

   DefaultTableCellRenderer centerRenderer;
    DefaultTableModel dtm ;
    private int row;
    public LoadCSVFileDialog( File file) {
       // super(parent, modal);
        this.setTitle(file.getName());
        row = 0;
      
        initComponents();
        
        
           dtm=(DefaultTableModel) oldTable.getModel();        
   DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
//resultTable.setDefaultRenderer(Object.class, centerRenderer);
        for (int x = 0; x < dtm.getColumnCount(); x++) {
            oldTable.getColumnModel().getColumn(x).setCellRenderer(centerRenderer);
        }
        
             loadFile(file);
        //non editable
               oldTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    JTable table = (JTable) me.getSource();
                    int selectedrow = table.getSelectedRow();

                    String time = dtm.getValueAt(selectedrow, 1).toString();
                    int portNum = Integer.parseInt(table.getModel().getValueAt(selectedrow, 2).toString());

                    int sys = Integer.parseInt(table.getModel().getValueAt(selectedrow, 3).toString());
                    int dia = Integer.parseInt(table.getModel().getValueAt(selectedrow, 4).toString());
                    int pr = Integer.parseInt(table.getModel().getValueAt(selectedrow, 5).toString());
                    BloodPressure bp = new BloodPressure(time, portNum, sys, dia, pr);
                    try {
                        PrintHtml.getInstance().print(bp, Green.useTemplate);
                    //    message(bp.getPortNum() + " 번 혈압기에서 " + bp.getStrTime() + "에 측정된 결과를 인쇄합니다.");

                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                     //   message(" bptolabel ::: 374  Error: I/O 오류");

                    } catch (PrinterException e1) {
                        // TODO Auto-generated catch block

                        e1.printStackTrace();
//  System.out.println("Error: 인쇄오류. 프린터와의 연결 상태를 확인해주세요.");
                      //  message(" Error: 인쇄오류. 프린터와의 연결 상태를 확인해주세요.");

                    }
                }}});
                     setDefaultCloseOperation(this.DISPOSE_ON_CLOSE);
    }


  public void loadFile(File file){
   ArrayList<BloodPressure> list=loadFileNoneSynk(file);
        for (BloodPressure bp : list) {
            Object rowObj[] = {++row, bp.getStrTime(), bp.getPortNum(), bp.getSys(),
                bp.getDia(), bp.getPr()};
           dtm.addRow(rowObj);

        }
       // table.repaint();
        //non editable
    }
     private ArrayList<BloodPressure> loadFileNoneSynk(File file) {
      ArrayList<BloodPressure> bpList = CSVControl.getInstance().load(file);
       return bpList;
     }
//   private ArrayList<BloodPressure> loadFileNoneSynkOld(File file) {
//
//        ArrayList<BloodPressure> bpList = new ArrayList<>();
//
//		// 파싱하는 단계
//        BufferedReader br = null;
//        try {
//            br = new BufferedReader(new FileReader(file));
//
//            String line = null;
//            while ((line = br.readLine()) != null) {
//                // -1 옵션은 마지막 "," 이후 빈 공백도 읽기 위한 옵션
//                //  System.out.println(line);
//                if (line.trim().equals("")) {
//
//  //   System.out.println("line trim nothing" + line);
//                    continue;
//
//                }
//              
//                BloodPressure bp = new BloodPressure(line);
//                //System.out.println(bp.getDia() + ", " + bp.getSys());
//                bpList.add(bp);
//
//                //				String[] token = line.split("\n", -1);
//                //
//                //				for (String output : token) {
//                //					System.out.println(output);
//                //				}
//            }
//            br.close();
//
////            Green.message("읽기 성공! csv 파일에서 " + bpList.size() + " 건의 측정 결과를 불러옴.");
//
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            Green.message("ERROR =====> 읽기 실패! csv 파일을 찾지 못했습니다.");
//
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException ex) {
//                    Logger.getLogger(CSVControl.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }
//        return bpList;
//
//    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        oldTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jScrollPane1.setMaximumSize(new java.awt.Dimension(610, 32767));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(600, 27));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(610, 402));

        oldTable.setModel(new javax.swing.table.DefaultTableModel(
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
        oldTable.setFillsViewportHeight(true);
        oldTable.setMinimumSize(new java.awt.Dimension(580, 0));
        oldTable.setRowHeight(20);
        oldTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(oldTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 634, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 422, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LoadCSVFileDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LoadCSVFileDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LoadCSVFileDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LoadCSVFileDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
               File file = new File("BpRecord.csv");

                LoadCSVFileDialog frame = new LoadCSVFileDialog(file);
                frame.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                frame.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable oldTable;
    // End of variables declaration//GEN-END:variables
}
