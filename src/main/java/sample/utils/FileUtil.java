package sample.utils;

import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 查找9数字，若读取文本为连续则查找PCR开头 若文本有换行符分隔直接查找9位数。
 * 验证可以查看开头数字
 */
public class FileUtil {

    public static String getStringValue(File file) throws Exception{
        InputStream in
                = new BufferedInputStream(new FileInputStream(file));
        BufferedReader br
                = new BufferedReader(new InputStreamReader(in));
        String value = "";
        StringBuffer sb = new StringBuffer();

        while((value = br.readLine()) != null && !"".equals(value)) {
            sb.append(value).append("/n") ;
        }
        in.close();
        br.close();

        return sb.toString();

    }

    public static void exlToText(String pathname, Workbook wb) throws IOException {

        File file = new File(pathname);
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);

        Sheet sheet = wb.getSheetAt(0);
        int MaxRow = sheet.getPhysicalNumberOfRows();

        for (int i = 0; i < MaxRow; i++) {
            Row row = sheet.getRow(i);
            int MaxCol = row.getLastCellNum();

            for(int j=0; j<MaxCol; j++){
                Cell cell = row.getCell(j);
                if(cell !=null) {
                    CellType type = cell.getCellType();
                    String s ="";
                    if(type==CellType.NUMERIC){
                         s = String.valueOf((int)cell.getNumericCellValue());
                    }else if(type==CellType.STRING){
                         s = cell.getStringCellValue();
                    }
                    bw.write(s.trim());
                    System.out.println("==" + s.trim() + "==");
                }
                if (j != MaxCol - 1) {
                    bw.write("\t");
                }
            }
            bw.newLine();
            bw.flush();
        }
        wb.close();
        bw.close();
        fw.close();


    }

    public static void main(String[] args) {

        File file = new File("C:\\Users\\Administrator\\Desktop\\朱宏岩\\20200309-3.dat");
        try {

            String a =FileUtil.getStringValue(file);
            //System.out.print(FileUtil.getStringValue(file).indexOf("223633960"));

            List<String> strs = new ArrayList<String>();
            String regEx="\\d{9}";
           //String regEx="PCR";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(a);

            while(m.find()) {
                strs.add(m.group());

            }
            System.out.println(strs.size());
            System.out.println(strs);
            System.out.println(a);
        }catch (Exception e){
            System.out.println(e.fillInStackTrace());
        }


    }
}
