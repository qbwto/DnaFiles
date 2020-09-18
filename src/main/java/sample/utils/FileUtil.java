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
 * 文件工具类
 *
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





}
