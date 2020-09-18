package sample.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import sample.entity.OpsLogDataVO;
import sample.utils.FileUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RecheckController {

    Stage stage;//全局对象
    @FXML private Pane cPane;//表格面板1
    @FXML private TextField dir1; //路径A
    @FXML private TextField dir2; //路径B
    @FXML private ScrollPane scrollPane_1;//滚动面板1
    @FXML private ScrollPane scrollPane_2;//滚动面板2
    File[] files_a ; //路径A文件列表
    File[] files_b ; //路径B文件列表

    ObservableList<ExcelDataVO> excelDataVOList = FXCollections.observableArrayList(); //导出数据
    private static Logger logger= Logger.getLogger(RecheckController.class);//日志

    /**
     * 初始化方法
     */
    public void init(){
        stage =(Stage) cPane.getScene().getWindow();
    }

    /**
     * 设置路径A、B按钮动作
     * @param event
     */
    @FXML
    protected void setDirAction(ActionEvent event) {

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

        //检查路径配置情况、检查路径内文件数量
        if(dir1.getText().equals("")&&dir2.getText().equals("")){
            alertInfo("请先配置路径A、路径B !"); return;
        }else if(files_a.length==0&&files_b.length==0){
            alertInfo("请重新选择非空路径"); return;
        }

        excelDataVOList.clear();

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
                    List<String> strs_a = matcherStrFromCODIS(files_a[i]);
                    List<String> strs_b = matcherStrFromExcel(files_b[j]);
                    opsLogDataVO.setFile_a_quantity(strs_a.size());
                    opsLogDataVO.setFile_b_quantity(strs_b.size());
                    //路径B文件的数据未求差集前的大小
                    int strs_b_size = strs_b.size();
                    //求出差集，路径B的文件数据将减少
                    strs_b.removeAll(strs_a);
                    //将比对结果数据装入ExcelData中用于导出
                    for(String sample_name :strs_b){
                        String plateID = FileUtil.getPrefix(opsLogDataVO.getFileName_a(),false);
                        excelDataVOList.add(new ExcelDataVO(plateID,sample_name));
                    }
                    opsLogDataVO.setHitQuantity(strs_b_size - strs_b.size());
                    opsLogDataVO.setMissQuantity(strs_b.size());

                    data.add(opsLogDataVO);
                }
            }
        }
        initTable(data);
    }

    /**
     * 创建表格
     * @param opsLogDataVOList
     */
    private void initTable( ObservableList<OpsLogDataVO> opsLogDataVOList){

        if(cPane.getChildren().size()!=0)
        cPane.getChildren().remove(0);

        TableView table = new TableView();

        TableColumn fileName_aCol = new TableColumn("路径A文件名");
        fileName_aCol.setCellValueFactory(new PropertyValueFactory<>("fileName_a"));

        TableColumn file_a_quantityCol = new TableColumn("路径A文件数据量");
        file_a_quantityCol.setCellValueFactory(new PropertyValueFactory<>("file_a_quantity"));

        TableColumn fileName_bCol = new TableColumn("路径B文件名");
        fileName_bCol.setCellValueFactory(new PropertyValueFactory<>("fileName_b"));

        TableColumn file_b_quantityCol = new TableColumn("路径B文件数据量");
        file_b_quantityCol.setCellValueFactory(new PropertyValueFactory<>("file_b_quantity"));

        TableColumn hitQuantityCol = new TableColumn("相同数量");
        hitQuantityCol.setCellValueFactory(new PropertyValueFactory<>("hitQuantity"));

        TableColumn missQuantityCol = new TableColumn("不同数量");
        missQuantityCol.setCellValueFactory(new PropertyValueFactory<>("missQuantity"));

        table.setItems(opsLogDataVOList);
        table.getColumns().addAll(fileName_aCol,file_a_quantityCol, fileName_bCol,
                                    file_b_quantityCol,hitQuantityCol ,missQuantityCol);

        table.setMinHeight(cPane.getHeight());
        table.setMinWidth(cPane.getWidth());
        cPane.getChildren().add(table);
    }

    /**
     * 导出比对数据
     * 分两种，一种从已有复检文件开始，另一种从新文件开始。
     * @throws Exception
     */
    @FXML
    public void exportExcelAction() throws Exception {

        /** 判断列表是否有数据 */
        if(excelDataVOList.size()==0){
            this.alertInfo("暂无数据可以导出");
            return;
        }

        /** 提示选择导出方式 */
        Alert _alert = new Alert(Alert.AlertType.NONE,"是否衔接【已有文件】继续导出？"
                ,new ButtonType("创建【新的文件】", ButtonBar.ButtonData.NO)
                ,new ButtonType("衔接【已有文件】", ButtonBar.ButtonData.YES));
        Optional<ButtonType> _buttonType = _alert.showAndWait();

        if(_buttonType.get().getButtonData().equals(ButtonBar.ButtonData.YES)){

            //选择继续导出的文件
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel files (*.xls)", "*.xls");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setTitle("Open Resource File");
            File f = fileChooser.showOpenDialog(stage);
            //获取指定文件名、路径
            String fileName = FileUtil.getPrefix(f.getName(),false);
            String filePath = f.getParent();
            //判断文件文件名是否正确
            if(nextFileName(fileName).equals("err")) {
                alertInfo("请选择正确的文件");
                return;
            }

            /** 首先用数据填充已有文件剩余孔位 */

            //获取已有文件中92个孔位里可填写的第一个孔位
            int idx = getIdxOfWorkBook(f);
            //用数据将剩余孔位补齐
            int impSize = impDataToWorkBook(f,excelDataVOList,idx);
            if(impSize == -1) return;

            /** 截取补齐第一个文件的剩余数据并生成新文件 */
            if(excelDataVOList.size()>(92-idx)){
                //剩余数据
                List<ExcelDataVO> otherExcelDataVoList = excelDataVOList.subList(impSize,excelDataVOList.size());

                //计算生成文件数量
                int forTimes = getNumOfFilesByDataSize(excelDataVOList.size());

                //将剩余数据输出文档
                for(int i =0 ; i<forTimes ;i++){
                    InputStream in = this.getClass().getResourceAsStream("/ExpFileTemplate/"+"复检模板.xls");
                    Workbook workbook = new HSSFWorkbook(in);
                    fileName =nextFileName(fileName);
                    if(i!=forTimes-1) {
                        impDataToWorkBook(otherExcelDataVoList.subList(i * 92, 92),workbook,fileName);
                    }else{
                        impDataToWorkBook(otherExcelDataVoList.subList(i * 92, otherExcelDataVoList.size()), workbook,fileName);
                    }
                    FileOutputStream fos = new FileOutputStream(filePath  + "\\" + fileName + ".xls");
                    workbook.write(fos);
                    workbook.close();
                    fos.close();
                }
            }
        }else{
            //获取默认文件名前缀
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("");
            dialog.setHeaderText("请设置板号（文件名）");
            dialog.setContentText("板号（文件名）：");
            Optional result = dialog.showAndWait();
            String fileName = "";
            if (result.isPresent()) {
                fileName= (String)result.get();
                if(nextFileName(fileName).equals("err")) {
                    alertInfo("请输入正确的文件名");
                    return;
                }
            }else{
                alertInfo("请输入的文件名为空，请重新操作");
            }

            //获取导出选择路径
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("选择路径");
            File f = directoryChooser.showDialog(stage);
            if(f==null) return;
            String filePath = f.getPath();

            //计算生成文件数量
            int forTimes = getNumOfFilesByDataSize(excelDataVOList.size());

            for(int i =0 ; i<forTimes ;i++){
                InputStream in = this.getClass().getResourceAsStream("/ExpFileTemplate/"+"复检模板.xls");
                Workbook workbook = new HSSFWorkbook(in);
                if(i != 0 ) fileName =nextFileName(fileName);
                if(i!=forTimes-1) {
                    impDataToWorkBook(excelDataVOList.subList(i * 92, 92),workbook,fileName);
                }else{
                    impDataToWorkBook(excelDataVOList.subList(i * 92, excelDataVOList.size()), workbook,fileName);
                }
                FileOutputStream fos = new FileOutputStream(filePath  + "\\" + fileName + ".xls");
                workbook.write(fos);
                workbook.close();
                fos.close();
            }

        }
        //导出完成提示
        alertInfo("导出完成");

    }

    /**
     * 通过当前文件名获取下一文件名
     * 例如：JLHN20-1C 或 JLSGAT-R-9
     * 文件名最后一个横杠后的数字需要改变，若9增加为10。
     * 然后返回新的文件名
     * 若文件名不合法则返回err
     * @param fileName
     * @return
     */
    private static String nextFileName(String fileName){

        if(fileName == null || fileName.equals("")){
            return "err";
        }
        //判断文件名最后是否为数字
        char lastchar = fileName.charAt(fileName.length()-1);

        String numStr = "";
        if(Character.isDigit(lastchar)){
             numStr = fileName.substring(fileName.lastIndexOf("-")+1,fileName.length());
        }else {
             numStr = fileName.substring(fileName.lastIndexOf("-")+1,fileName.length()-1);
        }
        //判断提取的是否为数字
        if(!numStr.equals("")) {

            for (char c : numStr.toCharArray()) {
                if (!Character.isDigit(c)) {
                    return "err";
                }
            }
        }else{
            return "err";
        }

        int num = Integer.valueOf(numStr) + 1;
        String newFileName = fileName.replace("-"+numStr,"-"+String.valueOf(num));

        return newFileName;
    }



    /**
     * 读取已有数据的“复检模板”，查看从哪个位置开始没有值，
     * 该方法返回值不是实际excel模板中的位置，而是模板中92个可填写数据位置的顺序号。
     * @return
     */
    public int getIdxOfWorkBook(File file) throws Exception{

        FileInputStream in = new FileInputStream(file);
        Workbook workbook = new HSSFWorkbook(in);

        int idx = 0;
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 0; i < 92; i++) {

            Cell c ;
            if (i < 45) {
                Row row = sheet.getRow(i + 5);
                c = row.getCell(1);
            } else {
                Row row = sheet.getRow((i - 48) + 5);
                c = row.getCell(5);
            }

            CellType type = c.getCellType();
            if(type==CellType.NUMERIC&&c.getNumericCellValue()==0){
                idx = i; break;
            }else if(type==CellType.STRING&&c.getStringCellValue().equals("")){
                idx = i; break;
            }else if(type==CellType.BLANK){
                idx = i; break;
            }
        }
        in.close();
        workbook.close();
        return idx;
    }


    /**
     * 将比对的数据放入已有的模板文件中
     * @param file 文件
     * @param lists 数据
     * @param startIdx 在“复检模板”中可以插入数据的起始位置
     * @throws Exception
     */

    public int impDataToWorkBook(File file,List<ExcelDataVO> lists,int startIdx) throws Exception{

        int impSize = 0;

        FileInputStream in = new FileInputStream(file);
        Workbook workbook = new HSSFWorkbook(in);
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 0; i < lists.size(); i++) {

            if(i>=92-startIdx){
                break;
            }

            ExcelDataVO excelDataVO = lists.get(i);
            String Name_a = excelDataVO.getName_a();
            String Name_b = excelDataVO.getName_b();

            if (i < 45 && startIdx <45) {
                Row row = sheet.getRow(i + 5 +startIdx);
                row.getCell(1).setCellValue(Name_b);
                row.getCell(2).setCellValue(Name_a);
            } else if(i < 45 && startIdx >45){
                Row row = sheet.getRow((i - 48) + 5 +startIdx);
                row.getCell(5).setCellValue(Name_b);
                row.getCell(6).setCellValue(Name_a);
            } else {
                Row row = sheet.getRow((i - 48) + 5);
                row.getCell(5).setCellValue(Name_b);
                row.getCell(6).setCellValue(Name_a);
            }

            impSize++;
        }
        in.close();

        /* 数据写入 */
        FileOutputStream fos =null;
        try{
            fos = new FileOutputStream(file);
            workbook.write(fos);
        }catch (Exception e){
            alertInfo("请关闭当前文件后再操作");
            return -1;
        }finally {
            workbook.close();
            if(fos!=null) fos.close();
        }

        return impSize;
    }

    /**
     * 将导出输入输出到新建的模板文件中
     * @param lists
     * @param workbook
     * @param plateID 板号
     * @throws Exception
     */
    public void impDataToWorkBook(List<ExcelDataVO> lists ,Workbook workbook,String plateID) throws Exception{

        Sheet sheet = workbook.getSheetAt(0);

        //设置板号
        Row row_0 = sheet.getRow(0);
        Cell cell_1 =row_0.getCell(1);
        if(cell_1.getCellType()== CellType.BLANK){
            cell_1.setCellValue(plateID);
        }

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

    /**
     * 根据数据量判断导出文件的数量
     * @param dataSize
     * @return
     */
    private  static int getNumOfFilesByDataSize(int dataSize){

        int NumOfFiles = 0;

        if(dataSize<92){
            NumOfFiles=1;

        }else if(dataSize>92){
            if(dataSize%92!=0){
                NumOfFiles = dataSize/92 +1;
            }else if(dataSize%92==0){
                NumOfFiles = dataSize/92;
            }
        }
        return NumOfFiles;
    }


    /**
     * 提示弹出框
     * @param
     */
    private void alertInfo (String msg){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(msg);
        alert.showAndWait();
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
           e.printStackTrace();
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

    public class ExcelDataVO {


        private String name_a;
        private String name_b;

        public ExcelDataVO(String name_a, String name_b) {
            this.name_a = name_a;
            this.name_b = name_b;
        }

        public String getName_a() {
            return name_a;
        }

        public void setName_a(String name_a) {
            this.name_a = name_a;
        }

        public String getName_b() {
            return name_b;
        }

        public void setName_b(String name_b) {
            this.name_b = name_b;
        }
    }
}

