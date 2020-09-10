package sample.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.apache.poi.ss.usermodel.Workbook;
import sample.entity.ExcelDataVO;
import sample.utils.ExcelWriter;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 */

public class ThirdController {


    ObservableList<ExcelDataVO> excelDataVOList;
    @FXML private Pane cPane;
    private int i = 0;
    /**
     * 显示比对结果
     */
    public void init(){

        //创建表格
        TableView table = new TableView();

        TableColumn name_aCol = new TableColumn("字段A");
       // name_aCol.setMinWidth(100);
        name_aCol.setCellValueFactory(
                new PropertyValueFactory<>("name_a"));

        TableColumn name_bCol = new TableColumn("字段B");
       // name_bCol.setMinWidth(100);
        name_bCol.setCellValueFactory(
                new PropertyValueFactory<>("name_b"));



        table.setItems(excelDataVOList);
        table.getColumns()
                .addAll(name_aCol,name_bCol );


        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(table);


        cPane.getChildren().addAll(vbox);

    }

    @FXML
    public void exportExcelAction() throws Exception {


        int excelDataVOListSize =  excelDataVOList.size();

        //判断列表是否有数据
        if(excelDataVOListSize==0){
            return;
        }
        //TODO 获取导出文件名前缀
        String fileNamePrefix = "导出文件";
        //TODO 获取导出路径
        String filePath = "d:/";

        //每个导出文件的数据条数为92条。
        int exportFileNum = excelDataVOListSize/92+1;
        List<List<ExcelDataVO>> exportFiles = new  ArrayList<List<ExcelDataVO>>();
        for(int i =0 ; i<exportFileNum;i++){

            int begin = i*92;
            int end = (i+1)*92;

            if((i+1) == exportFileNum){//当前为尾部文档时
                exportFiles.add(excelDataVOList.subList(begin,excelDataVOListSize));
            }else{
                exportFiles.add(excelDataVOList.subList(begin,end));
            }
        }
        //导出文件
        int fileNO = 1;
        for(List<ExcelDataVO> ol : exportFiles){
            Workbook wb = ExcelWriter.exportData(ol);
            FileOutputStream fos = new FileOutputStream(filePath+fileNamePrefix+fileNO+".xls");
            wb.write(fos);
            fos.close();
            fileNO++;
        }
        //导出提醒
        //同名文件提醒

    }




}