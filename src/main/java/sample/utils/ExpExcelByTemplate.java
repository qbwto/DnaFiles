package sample.utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import sample.entity.ExcelDataVO;
import sample.entity.Plate;
import sample.entity.Well;

import java.io.InputStream;
import java.util.List;

/**
 * 根据模板导出Excel
 */
public class ExpExcelByTemplate {

    public String templateName ; // 模板名称
    public Workbook workbook;

    public ExpExcelByTemplate(String tplName) throws Exception{
        templateName = tplName;
        InputStream in = this.getClass().getResourceAsStream("/ExpFileTemplate/"+templateName);
        workbook = new HSSFWorkbook(in);
    }

    /**
     * 读取模板
     * @return
     */
    public Workbook getWorkbook() {
        return workbook;
    }

    /**
     * 将采样数据放入“分析模板.xls”
     * @param plate
     * @throws Exception
     */
    public void impDataToWorkBook(Plate plate) throws Exception{

        Sheet sheet = workbook.getSheetAt(0);

        if(templateName.equals("分析模板.xls")){

            sheet.getRow(0).getCell(1).setCellValue(plate.getPlateID());//板号
            sheet.getRow(0).getCell(3).setCellValue(plate.getReagentType());//试剂盒
            sheet.getRow(1).getCell(1).setCellValue(plate.getWellSize());//孔径
            sheet.getRow(2).getCell(1).setCellValue(plate.getSamplePerson());//取样人
            sheet.getRow(3).getCell(1).setCellValue(plate.getSampleDate());//取样日期
            sheet.getRow(3).getCell(3).setCellValue(plate.getArea());//地区

            for (int i = 0; i < plate.getWellList().size(); i++) {
                Well w = plate.getWellList().get(i);
                String sampleName = w.getSampleName();

                if (i < 48) {
                    Row row = sheet.getRow(i + 5);
                    row.getCell(1).setCellValue(sampleName);
                } else {
                    Row row = sheet.getRow((i - 48) + 5);
                    row.getCell(4).setCellValue(sampleName);
                }
            }
        }else if(templateName.equals("取样模板.xls")){

            sheet.getRow(1).getCell(0).setCellValue(plate.getPlateID());//Container Name
            sheet.getRow(1).getCell(1).setCellValue(plate.getPlateID());//Plate ID

            for (int i = 0; i < plate.getWellList().size(); i++) {
                Well w = plate.getWellList().get(i);
                String sampleName = w.getSampleName();

                Row row = sheet.getRow(i + 5);
                row.getCell(1).setCellValue(sampleName);

            }
        }


    }
    /**
     * 将比对的数据放入“复检模板.xls”
     * @throws Exception
     */

    public void impDataToWorkBook(List<ExcelDataVO> lists) throws Exception{

        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 0; i < lists.size(); i++) {

            ExcelDataVO excelDataVO = lists.get(i);
            String Name_a = excelDataVO.getName_a();
            String Name_b = excelDataVO.getName_b();

            if (i < 45) {
                Row row = sheet.getRow(i + 5);
                row.getCell(1).setCellValue(Name_b);
                row.getCell(2).setCellValue(Name_a);
            } else {
                Row row = sheet.getRow((i - 48) + 5);
                row.getCell(5).setCellValue(Name_b);
                row.getCell(6).setCellValue(Name_a);
            }
        }
    }

    public static void main(String[] args)throws Exception {
    }
}
