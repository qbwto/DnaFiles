package sample.utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

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

    public static String getPrefix(String fileName, boolean toLower) {
        if (fileName.contains(".")) {
            String prefix = fileName.substring(0,fileName.lastIndexOf("."));
            if (toLower) {
                return trimilSpace(prefix.toLowerCase());
            }
            return trimilSpace(prefix);
        }
        return "";
    }

    public static String getPostfix(String fileName, boolean toLower) {
        if (fileName.contains(".")) {
            String postfix = fileName.substring(fileName.lastIndexOf("."),fileName.length());
            if (toLower) {
                return trimilSpace(postfix.toLowerCase());
            }
            return trimilSpace(postfix);
        }
        return "";
    }

    public static String trimilSpace(String input) {
        if (input == null) {
            return null;
        }
        String result = input.replaceAll("\u00A0", "").replaceAll("\u200B", "").replaceAll("\u2002", "")
                .replaceAll("\u200C", "").replaceAll("\u200D", "").replaceAll("\uFEFF", "").trim();
        if ("".equals(input)) {
            return null;
        }
        return result;
    }

    /**
     * 读取codis文件的方法
     * @return
     */
    public static List<String> matcherStrFromCODIS (File f){

        List<String> strList = new ArrayList<String>();

        try{

            String a = FileUtil.getStringValue(f);
            String regExa="\\d{9}";
            Pattern pattern_a = Pattern.compile(regExa);
            Matcher matcher_a = pattern_a.matcher(a);
            while(matcher_a.find()) {
                strList.add(matcher_a.group());
            }
        }catch (Exception e){
            //TODO
        }

        return strList;
    }

    /**
     * 读取分析表中的实验室编号
     * @param file
     * @throws IOException
     */
    public static List<String> matcherStrFromExcel(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        Workbook workbook = new HSSFWorkbook(in);


        List<String> strList = new ArrayList<String>(96);

        Sheet sheet = workbook.getSheetAt(0);
        int MaxRow = sheet.getPhysicalNumberOfRows();
        //排除掉A1，B1，C1
        for (int i = 8; i < MaxRow; i++) {
            Row row = sheet.getRow(i);
            int MaxCol = row.getLastCellNum();
            Cell cell1 = row.getCell(1);
            String s1 ="";
            if(cell1 !=null) {
                CellType type = cell1.getCellType();
                if(type==CellType.NUMERIC){
                    s1 = String.valueOf((int)cell1.getNumericCellValue());
                    strList.add(s1.trim());
                }else if(type==CellType.STRING&&!cell1.getStringCellValue().equals("")){
                    s1 = cell1.getStringCellValue();
                    strList.add(s1.trim());
                }

            }

        }
        //排除掉H12
        for (int i = 5; i < MaxRow-1; i++) {
            Row row = sheet.getRow(i);
            int MaxCol = row.getLastCellNum();
            Cell cell2 = row.getCell(4);
            String s2="";
            if(cell2 !=null) {
                CellType type = cell2.getCellType();
                if(type==CellType.NUMERIC){
                    s2 = String.valueOf((int)cell2.getNumericCellValue());
                    strList.add(s2.trim());
                }else if(type==CellType.STRING&&!cell2.getStringCellValue().equals("")){
                    s2 = cell2.getStringCellValue();
                    strList.add(s2.trim());
                }
            }
        }

        workbook.close();
        in.close();

        return strList;
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
