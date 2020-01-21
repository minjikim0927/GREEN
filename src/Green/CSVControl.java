package Green;



import Green.util.CryptoUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class CSVControl {

    //	public ArrayList<Option> optionList = new ArrayList<Option>();
    private boolean encrypt = true;
    private static CSVControl instance = new CSVControl();
    // private static String key="암호 데이타 one 1";
    public static File csvFile = new File("BpRecord.csv");
  private  CryptoUtils crypt;
    private CSVControl() {
        try {
            crypt= new CryptoUtils();
        } catch (Exception ex) {
            Logger.getLogger(CSVControl.class.getName()).log(Level.SEVERE, null, ex);
        }
//   System.out.println("CSV Control Instance created!!");
    }

    public static CSVControl getInstance() {
        return instance;
    }

//	public synchronized ArrayList<Option> loadOptions() {
//
//		ArrayList<Option> optionList = new ArrayList<Option>();	
//		// 파싱하는 단계
//
//		File file = new File("config.csv");
//
//		BufferedReader br;
//		try {
//			br = new BufferedReader(new FileReader(file));
//
//			String line = "";
//			while ((line = br.readLine()) != null) {
//				// -1 옵션은 마지막 "," 이후 빈 공백도 읽기 위한 옵션
//				Option bp = new Option(line);
//
//				optionList.add(bp);
//			}
//			br.close();
//
//
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//
//
//			BPtoLabel.appandTextArea("\n 설정 값이 없어 초기 설정 적용");
//
//			optionList.add(new Option("sysmin","85"));
//			optionList.add(new Option("sysmax","140"));
//			optionList.add(new Option("diamax","90"));
//			optionList.add(new Option("prmin","49"));
//			optionList.add(new Option("prmax","116"));
//			optionList.add(new Option("prmax","116"));
//
//			FileWriter fw;
//			try {
//				fw = new FileWriter(file, false);
//				
//				for(Option option: optionList) {
//					fw.write(option.name + "," + option.value + "\r\n");
//					
//				}
//
//				fw.flush();
//				fw.close();
//
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				
//				BPtoLabel.appandTextArea("\n 설정 저장 중 오류가 발생하였습니다.");
//
//			}
//			
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//
//		return optionList;
//	}
//	public synchronized void saveOptions(ArrayList<Option> optionList) {
//
//		File file = new File("config.csv");
//		FileWriter fw;
//		try {
//			fw = new FileWriter(file, false);
//			
//			for(Option option: optionList) {
//				fw.write(option.name + "," + option.value + "\r\n");
//				
//			}
//
//			fw.flush();
//			fw.close();
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			
//			BPtoLabel.appandTextArea("\n 설정 저장 중 오류가 발생하였습니다.");
//
//		}
//	}
    
    public synchronized ArrayList<BloodPressure> load() {
     return load(csvFile)   ;
     
    }
    public synchronized ArrayList<BloodPressure> load(File file) {
        
//System.out.println("loading");
        ArrayList<BloodPressure> bpList = new ArrayList<>();

		// 파싱하는 단계
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));

            String line = null;
            while ((line = br.readLine()) != null) {
                // -1 옵션은 마지막 "," 이후 빈 공백도 읽기 위한 옵션
                  System.out.println(line);
                if (line.trim().equals("")) {

                    System.out.println("line trim nothing" + line);
                    continue;

                }
                if (encrypt) {
                    try {
                        // byte[] base64decodedBytes = Base64.getDecoder().decode(line);
                        
                        line =  crypt.decrypt(line);
                        
 //System.out.println("after encrption "+line+"\tlength="+line.length());
            //  System.out.println("Original String: " + line);
                    } catch (Exception ex) {
                        Green.message("암호화에 실패하였습니다. " +line);
                      // Logger.getLogger(CSVControl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                BloodPressure bp = new BloodPressure(line);
                //System.out.println(bp.getDia() + ", " + bp.getSys());
                bpList.add(bp);

                //				String[] token = line.split("\n", -1);
                //
                //				for (String output : token) {
                //					System.out.println(output);
                //				}
            }
            br.close();

            Green.message("읽기 성공! csv 파일에서 " + bpList.size() + " 건의 측정 결과를 불러옴.");

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Green.message("ERROR =====> 읽기 실패! csv 파일을 찾지 못했습니다.");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(CSVControl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return bpList;

    }
  public synchronized void save(BloodPressure bp) throws Exception {
      save(bp, csvFile);
  
  }
    // csv format 
    // portnum, time, dia, sys, map, pr, motion
    public synchronized void save(BloodPressure bp, File file) throws Exception {
		//	jhlee@jbcp: thread sychronization  필요  http://www.tutorialspoint.com/java/java_thread_synchronization.htm

        FileWriter fw = null;
        try {
            fw = new FileWriter(file, true);

            String saveLine = bp.getStrTime() + "," + bp.getPortNum() + "," + bp.getSys() + ","
                    + bp.getMap()+ "," + bp.getDia() + ","
                    + bp.getPr();        
           
            if (encrypt) {
// System.out.println(saveLine+"===>"+saveLine.length()+"   com   "+tmp.length());
                fw.write( crypt.encrypt(saveLine)+ "\n");
            } else {
                fw.write(saveLine + "\n");
            }

//          fw.flush();
//          fw.close();
        } catch (java.io.FileNotFoundException ex) {
         JOptionPane.showConfirmDialog(null, file.getAbsolutePath()+" 파일이 닫고 다시 시도하십시오.", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);

            Logger.getLogger(CSVControl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (fw != null) {
                try {
                    fw.flush();
                    fw.close();
                } catch (IOException ex) {
                    Logger.getLogger(CSVControl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
//        if (encrypt) {
//            String encrpyedLine = null;
//            try {
//                encrpyedLine = CryptoUtils.doCryptoStr(key, saveLine) + "\n";
//            } catch (Exception ex) {
//                Logger.getLogger(CSVControl.class.getName()).log(Level.SEVERE, null, ex);
//                encrpyedLine = saveLine;
//            } finally {
//
//                fw.write(encrpyedLine + "\n");
//
//                fw.flush();
//                fw.close();
//            }
//        } else {
//
//            fw.write(saveLine + "\n");
//            fw.flush();
//            fw.close();
//        }

    }
}
