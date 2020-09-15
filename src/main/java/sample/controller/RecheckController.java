package sample.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Workbook;
import sample.entity.ExcelDataVO;
import sample.entity.OpsLogDataVO;
import sample.utils.ExpExcelByTemplate;
import sample.utils.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class RecheckController {

    @FXML private Pane cPane;//表格面板1
    @FXML private TextField dir1; //路径A
    @FXML private TextField dir2; //路径B
    @FXML private ScrollPane scrollPane_1;//滚动面板1
    @FXML private ScrollPane scrollPane_2;//滚动面板2

    File[] files_a ; //路径A文件列表
    File[] files_b ; //路径B文件列表

    ObservableList<ExcelDataVO> excelDataVOList = FXCollections.observableArrayList(); //导出数据

    /**
     * 设置路径A、B按钮动作
     * @param event
     */
    @FXML
    protected void setDirAction(ActionEvent event) {

        Stage stage = (Stage) cPane.getScene().getWindow();

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
     * 开始比对
     * 路径A 用于CODIS
     * 路径B 用于分析表
     *
     * @throws Exception
     */
    @FXML
    public void startBDAction() throws Exception {

        //检查路径配置情况
        if(dir1.getText().equals("")&&dir2.getText().equals("")){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("请先配置路径A、路径B !");
            alert.showAndWait();
            return;
        //检查路径内文件数量
        }else if(files_a.length==0&&files_b.length==0){

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("请重新选择非空路径");
            alert.showAndWait();
            return;
        }

        //比对列表数据
        ObservableList<OpsLogDataVO> data = FXCollections.observableArrayList();

        for(int i =0; i<files_a.length;i++){

            OpsLogDataVO opsLogDataVO =  new OpsLogDataVO();

            for(int j =0; j<files_b.length;j++){

                //获取文件名
                String name_a = files_a[i].getAbsoluteFile().getName();
                String name_b = files_b[j].getAbsoluteFile().getName();

                //若文件不是dat或xls为后缀的文件则舍弃
                if(!FileUtil.getPostfix(name_a,false).equals(".dat")){
                    break;
                }
                if(!FileUtil.getPostfix(name_b,false).equals(".xls")){
                    break;
                }

                //路经A中的文件名前缀和路径B中的文件名前缀相同时
                if(FileUtil.getPrefix(name_a,false).equals(FileUtil.getPrefix(name_b,false))){

                    opsLogDataVO.setFileName_a(files_a[i].getName());
                    opsLogDataVO.setFileName_b(files_b[j].getName());
                    opsLogDataVO.setFile_a(files_a[i]);
                    opsLogDataVO.setFile_b(files_b[j]);


                    //读取路径a中文件数据,读取路径b中文件数据
                    List<String> strs_a = FileUtil.matcherStrFromCODIS(files_a[i]);
                    List<String> strs_b = FileUtil.matcherStrFromExcel(files_b[j]);
                    opsLogDataVO.setFile_a_quantity(strs_a.size());
                    opsLogDataVO.setFile_b_quantity(strs_b.size());
                    //路径B文件的数据未求差集前的大小
                    int strs_b_size = strs_b.size();
                    //求出差集，路径B的文件数据将减少
                    strs_b.removeAll(strs_a);
                    //将比对结果数据装入ExcelData中用于导出
                    excelDataVOList.removeAll();
                    for(String s :strs_b){
                        excelDataVOList.add(new ExcelDataVO(FileUtil.getPrefix(opsLogDataVO.getFileName_a(),false),s));
                    }
                    opsLogDataVO.setHitQuantity(strs_b_size - strs_b.size());
                    opsLogDataVO.setMissQuantity(strs_b.size());

                    data.add(opsLogDataVO);
                }
            }
        }

        this.initTable(data);

    }


    private void initTable( ObservableList<OpsLogDataVO> opsLogDataVOList){

        if(cPane.getChildren().size()!=0)
        cPane.getChildren().remove(0);

        // 创建表格
        TableView table = new TableView();

        TableColumn fileName_aCol = new TableColumn("路径A文件名");
        fileName_aCol.setCellValueFactory(
                new PropertyValueFactory<>("fileName_a"));

        TableColumn file_a_quantityCol = new TableColumn("路径A文件数据量");
        file_a_quantityCol.setCellValueFactory(
                new PropertyValueFactory<>("file_a_quantity"));

        TableColumn fileName_bCol = new TableColumn("路径B文件名");
        fileName_bCol.setCellValueFactory(
                new PropertyValueFactory<>("fileName_b"));

        TableColumn file_b_quantityCol = new TableColumn("路径B文件数据量");
        file_b_quantityCol.setCellValueFactory(
                new PropertyValueFactory<>("file_b_quantity"));


        TableColumn hitQuantityCol = new TableColumn("相同数量");
        hitQuantityCol.setCellValueFactory(
                new PropertyValueFactory<>("hitQuantity"));

        TableColumn missQuantityCol = new TableColumn("不同数量");

        missQuantityCol.setCellValueFactory(
                new PropertyValueFactory<>("missQuantity"));


        table.setItems(opsLogDataVOList);
        table.getColumns()
                .addAll(fileName_aCol,file_a_quantityCol, fileName_bCol,file_b_quantityCol,
                        hitQuantityCol ,missQuantityCol);


        table.setMinHeight(cPane.getHeight());
        table.setMinWidth(cPane.getWidth());


        cPane.getChildren().add(table);
    }

    /**
     * 导出比对数据
     * @throws Exception
     */
    @FXML
    public void exportExcelAction() throws Exception {

        int excelDataVOListSize =  excelDataVOList.size();
//

        //判断列表是否有数据
        if(excelDataVOListSize==0){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("暂无数据可以导出");
            alert.showAndWait();
            return;
        }

        String fileNamePrefix = "\\";

        //获取默认文件名前缀
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("");
        dialog.setHeaderText("设置默认文件名");
        dialog.setContentText("文件名：");
        Optional result = dialog.showAndWait();
        if (result.isPresent()) {
            fileNamePrefix = "\\"+(String)result.get();
        }

        Stage stage = (Stage) cPane.getScene().getWindow();

        //获取路径
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择路径");
        File f = directoryChooser.showDialog(stage);
        String filePath = f.getPath();

        //每个导出文件的数据条数为92条。满了后重新开启一个新文件
        int exportFileNum = excelDataVOListSize/92+1;
        List<List<ExcelDataVO>> exportFiles = new ArrayList<List<ExcelDataVO>>();
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
        /* 生成分析表 */
        int file_idx = 0;
        for(int i = 0 ; i < exportFiles.size();i++) {
            ExpExcelByTemplate expExcelByFJ = new ExpExcelByTemplate("复检模板.xls");
            List<ExcelDataVO> excelDataVOList = exportFiles.get(i);
            expExcelByFJ.impDataToWorkBook(excelDataVOList);
            Workbook wb_fx = expExcelByFJ.getWorkbook();
            FileOutputStream fos = new FileOutputStream(filePath + fileNamePrefix + "_" + file_idx + ".xls");
            wb_fx.write(fos);
            wb_fx.close();
            fos.close();
            file_idx++;
        }

        //导出提醒
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("导出完成");
        alert.showAndWait();


    }
}

