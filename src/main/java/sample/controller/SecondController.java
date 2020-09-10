package sample.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import sample.entity.ExcelDataVO;
import sample.utils.FileUtil;
import sample.entity.OpsLogDataVO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 *
 */

public class SecondController {

    ObservableList<OpsLogDataVO> opsLogDataVOList ;
    ObservableList<ExcelDataVO> excelDataVOList = FXCollections.observableArrayList();
    String dir_a;
    String dir_b;
    @FXML private Pane cPane;
    @FXML private BorderPane borderPane;
    /**
     * 比对文件列表基本信息
     */
    public void init(){

        //创建表格
        TableView table = new TableView();


        // TODO 表格序号

        TableColumn fileName_aCol = new TableColumn("路径A文件名");
        //fileName_aCol.setMinWidth(100);
        fileName_aCol.setCellValueFactory(
                new PropertyValueFactory<>("fileName_a"));

        TableColumn file_a_quantityCol = new TableColumn("路径A文件数据量");
        //file_a_quantityCol.setMinWidth(100);
        file_a_quantityCol.setCellValueFactory(
                new PropertyValueFactory<>("file_a_quantity"));

        TableColumn fileName_bCol = new TableColumn("路径B文件名");
        //fileName_bCol.setMinWidth(100);
        fileName_bCol.setCellValueFactory(
                new PropertyValueFactory<>("fileName_b"));

        TableColumn file_b_quantityCol = new TableColumn("路径B文件数据量");
        //file_b_quantityCol.setMinWidth(100);
        file_b_quantityCol.setCellValueFactory(
                new PropertyValueFactory<>("file_b_quantity"));

        TableColumn stateCol = new TableColumn("状态");
        //stateCol.setMinWidth(100);
        stateCol.setCellValueFactory(
                new PropertyValueFactory<>("state"));

        TableColumn hitQuantityCol = new TableColumn("命中数量");
        //hitQuantityCol.setMinWidth(100);
        hitQuantityCol.setCellValueFactory(
                new PropertyValueFactory<>("hitQuantity"));

        TableColumn missQuantityCol = new TableColumn("丢失数量");
        //missQuantityCol.setMinWidth(100);
        missQuantityCol.setCellValueFactory(
                new PropertyValueFactory<>("missQuantity"));


        table.setItems(opsLogDataVOList);
        table.getColumns()
                .addAll(fileName_aCol,file_a_quantityCol, fileName_bCol,file_b_quantityCol,
                        stateCol ,hitQuantityCol ,missQuantityCol);



        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(table);

        borderPane.setLeft(vbox);

    }

    /**
     * 开始文件比对流程
     * @param event
     */
    @FXML
    protected void startCompareAction(ActionEvent event) {

        for (OpsLogDataVO opsLogDataVO: opsLogDataVOList){

            //读取路径a中文件数据
            List<String> strs_a = matcherStr(opsLogDataVO.getFile_a());
            //读取路径b中文件数据
            List<String> strs_b = matcherStr(opsLogDataVO.getFile_b());

            //System.out.print("strs_a:"+strs_a.size());
            //System.out.println(strs_a);
            //System.out.print("strs_b:"+strs_b.size());
            //System.out.println(strs_b);

            //求出差集
            strs_a.removeAll(strs_b);
            //保存差集数据
            System.out.print("strs_remove:"+strs_a.size());
            System.out.println(strs_a);

            excelDataVOList.removeAll();//清空数据
            for(String s :strs_a){
                excelDataVOList.add(new ExcelDataVO(opsLogDataVO.getFileName_a(),s));
            }

            //TODO 充值表格数据

        }

        // 提示框代码
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText("比对完成");
        alert.show();


    }


    /**
     * 匹配文件数据方式1
     * @return
     */
    private List<String> matcherStr (File f){

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
     * 显示对比结果
     * @throws Exception
     */
    @FXML
    public void showBDResult() throws Exception {

        //构建第三页面
        Stage stage=new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/third.fxml"));
        Parent root = (Parent)loader.load();
        //获取第二页面控制器并传参
        ThirdController controller = (ThirdController)loader.getController();
        controller.excelDataVOList =excelDataVOList;
        controller.init();//控制器初始化

        Scene scene = new Scene(root);
        stage.setTitle("标题位置");
        stage.setWidth(1000);
        stage.setHeight(600);
        stage.setScene(scene);
        stage.show();

    }
}