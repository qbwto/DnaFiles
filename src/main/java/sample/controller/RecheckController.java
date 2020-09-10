package sample.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import sample.entity.OpsLogDataVO;

import java.io.File;

/**
 * 如何在controller中获取stage
 * 如何选择路径
 */

public class RecheckController {

    @FXML private GridPane gridPane_1;//表格面板1
    @FXML private TextField dir1; //路径A
    @FXML private TextField dir2; //路径B
    @FXML private ScrollPane scrollPane_1;//滚动面板1
    @FXML private ScrollPane scrollPane_2;//滚动面板2

    File[] files_a ; //路径A文件列表
    File[] files_b ; //路径B文件列表



    /**
     * 设置路径A的按钮动作
     * @param event
     */
    @FXML
    protected void setDirAction(ActionEvent event) {

        Stage stage = (Stage) gridPane_1.getScene().getWindow();

        //路径选择器
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择路径");
        File f = directoryChooser.showDialog(stage);

        //文件列表
        if(f==null){ return; }//未选择路径
        TreeView<String> tree = getFilesTreeView (f);

        //显示到页面组件上
        Button bu = (Button) event.getSource();
        if(bu.getId().equals("dirButton1")){
            scrollPane_1.setContent(tree);
            dir1.setText(f.getAbsolutePath());
            files_a = f.listFiles();
        }else if(bu.getId().equals("dirButton2")){
            scrollPane_2.setContent(tree);
            dir2.setText(f.getAbsolutePath());
            files_b = f.listFiles();
        }

    }

    /**
     * 刷新文件A或B文件列表
     * @param event
     */
    @FXML
    protected void FlashFileListAction(ActionEvent event) {

        if(!dir1.getText().equals("")){
            File file_dir1 = new File(dir1.getText());
            TreeView<String> tree_dir1 = getFilesTreeView(file_dir1);
            scrollPane_1.setContent(tree_dir1);
            files_a = file_dir1.listFiles();
        }

        if(!dir2.getText().equals("")){
            File file_dir2 = new File(dir2.getText());
            TreeView<String> tree_dir2 = getFilesTreeView(file_dir2);
            scrollPane_2.setContent(tree_dir2);
            files_b = file_dir2.listFiles();
        }


    }

    /**
     * 获取文件列表的树装视图
     * @param f
     * @return
     */
    private TreeView<String> getFilesTreeView (File f){

        File [] files = f.listFiles();
        TreeItem<String> rootItem = new TreeItem<> (f.getName());
        rootItem.setExpanded(true);

        for (int i = 0; i < files.length; i++) {
            TreeItem<String> item = new TreeItem<> (files[i].getName());
            rootItem.getChildren().add(item);
        }

        return new TreeView<> (rootItem);
    }


    /**
     * 开始比对，该功能会跳转页面
     * @throws Exception
     */
    @FXML
    public void startBDAction() throws Exception {

        if(dir1.getText().equals("")&&dir2.getText().equals("")){//检查路径配置情况
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("请先配置路径A、路径B !");
            alert.showAndWait();
            return;
        }else if(files_a.length==1&&files_b.length==1){//检查路径内文件数量,根节点不算

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("当前路径为空");
            alert.showAndWait();
            return;
        }


        //TODO 提取可供比对的文件名列表(文件名目前不确定)
        ObservableList<OpsLogDataVO> data = FXCollections.observableArrayList();

        for(int i =0; i<files_a.length;i++){

            OpsLogDataVO opsLogDataVO =  new OpsLogDataVO();

            for(int j =0; j<files_b.length;j++){
                //当文件名相同时，作为可比对文件
                //TODO 过滤掉文件夹

                if(files_a[i].getName().equals(files_b[j].getName())){
                    opsLogDataVO.setFileName_a(files_a[i].getName());
                    opsLogDataVO.setFileName_b(files_b[j].getName());
                    opsLogDataVO.setFile_a(files_a[i]);
                    opsLogDataVO.setFile_b(files_b[i]);
                    data.add(opsLogDataVO);
                    break;
                }
            }


        }

        //构建第二页面
        Stage stage=new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/second.fxml"));
        Parent root = (Parent)loader.load();
        //获取第二页面控制器并传参
        SecondController controller = (SecondController)loader.getController();
        controller.opsLogDataVOList = data;
        controller.dir_a = dir1.getText();
        controller.dir_b = dir2.getText();
        controller.init();//控制器初始化

        Scene scene = new Scene(root);
        stage.setTitle("标题位置");
        stage.setWidth(1000);
        stage.setHeight(600);
        stage.setScene(scene);
        stage.show();


    }

}

// 提示框代码
//    Alert alert = new Alert(Alert.AlertType.INFORMATION);
//    Button bu = (Button) event.getSource();
//    bu.getId();
//    alert.setTitle("提示");
//    alert.setHeaderText(bu.getId());
//    alert.setContentText("I have a great message for you!");
//    alert.showAndWait();

// 测试数据
//        ObservableList<OpsLogDataVO> data = FXCollections.observableArrayList(
//            new OpsLogDataVO("文件名a","文件名b","未开始","0","0"),
//            new OpsLogDataVO("文件名a","文件名b","未开始","0","0"),
//            new OpsLogDataVO("文件名a","文件名b","未开始","0","0"),
//            new OpsLogDataVO("文件名a","文件名b","未开始","0","0"),
//            new OpsLogDataVO("文件名a","文件名b","未开始","0","0"),
//            new OpsLogDataVO("文件名a","文件名b","未开始","0","0"),
//            new OpsLogDataVO("文件名a","文件名b","未开始","0","0"),
//            new OpsLogDataVO("文件名a","文件名b","未开始","0","0"),
//            new OpsLogDataVO("文件名a","文件名b","未开始","0","0"),
//            new OpsLogDataVO("文件名a","文件名b","未开始","0","0"),
//            new OpsLogDataVO("文件名a","文件名b","未开始","0","0"),
//            new OpsLogDataVO("文件名a","文件名b","未开始","0","0"),
//            new OpsLogDataVO("文件名a","文件名b","未开始","0","0"),
//            new OpsLogDataVO("文件名a","文件名b","未开始","0","0")
//        );