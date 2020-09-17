package sample.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import sample.entity.Plate;
import sample.entity.Well;
import sample.utils.ExpExcelByTemplate;
import sample.utils.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Optional;

/**
 * #业务流程
 *     一个板有96个孔位，排列方式为8行，12列的矩阵。行标由字母A到H标识，列标由数字1到12标识。
 * 孔位名为数字加字母的组合，例如：A1、B2 等。
 *     第一孔位A1=CK 、第三孔位C1=ladder、第九十六孔位H12=ladder 为固定值，
 * 第二个孔位需要先选择试剂类型，当选typer-29 默认为9948，若选typer-21 默认为9947 。
 *     打板过程为用扫码枪对样本的条形码进行扫描，并将扫描的值填入对应的孔位，每个板扫描的起始位置是D1，
 * 扫描的顺序是D1>E1>H1...A2>B2,扫描过程要忽略固定值（1，2，3，96 孔位），结束位置为G12.
 *
 * #程序设计
 *     Plate 类定义为“板”。Well 类定义为“孔位” ，板除了基础信息还包含“孔位列表”。
 *     (Plate 对象的创建应该在填写完基础信息并点击“开始采集”按钮后发生）
 *     孔位的图形列表也保存在了Plate中
 *
 * #流程设计：
 *     1.填写基本信息
 *     2.点击开始采集，效验必填项，通过后开始采集，打开扫码框，关闭基础信息输入框，并把焦点放在扫描框位置
 *     3.读取条码数据，数据满足9位长度要求。由输入框显示已采集数据
 *     4.记录数据，改变页面状态，鼠标点击黄色可以修改当前数据
 *     5.生成“上样表和分析表”
 *
 * #页面设计
 *     该页码的基础格局是VBOX ，第一行是基础信息 ，第二行为“板”的列标题，第三行是插入HBOX
 *  HBOX的中第一列为“板”的的行标题
 *        1 2 3 4 5 6 7 8 9 10 11 12
 *      A
 *      B
 *      C
 *      D
 *      E
 *      F
 *      G
 *      H
 *  ----------------------------------
 *  #页面颜色：
 *      孔位默认颜色：
 *      孔位固定值颜色：WELL_RED
 *      当前孔位颜色：WELL_YELLOW
 *      已扫描孔孔位颜色：WELL_GREEN
 *      “板”的列标题颜色：
 *      “板”的行标题颜色：
 *
 */
public class SampleController {

    /** 页面元素 */
    @FXML
    private VBox vbox;//布局元素：根元素
    @FXML
    private HBox hbox;//布局元素：板位置布局元素
    @FXML
    private Pane p_title;//布局元素：板列头布局元素
    @FXML
    private TextField PlateID;//板号
    @FXML
    private ChoiceBox ReagentType;//试剂类型
    @FXML
    private TextField WellSize;//孔径
    @FXML
    private DatePicker SampleDate;//取样日期
    @FXML
    private TextField SamplePerson;//取样人
    @FXML
    private TextField Area;//区域
    @FXML
    private TextField ScanInput;//扫描输入框
    @FXML
    private Button StartButton;//开始按钮
    @FXML
    private Button ExpButton;//导出按钮
    @FXML
    private Button NextPlate;//下一板按钮

    /** 配色 */
    Color WELL_RED = Color.web("#f1403c");
    Color WELL_GREEN = Color.web("#3cb034");
    Color WELL_YELLOW = Color.web("#FFCE3E");
    Color WELL_DEFAULT = Color.web("#dbebff");

    /** 数据 */
    Plate plate;//“板”数据
    EventHandler<KeyEvent> ScanInputEnterListener;

    private static Logger logger= Logger.getLogger(SampleController.class);


    /** 初始化"打板"数据 */
    public void init() {
        plate = new Plate();
    }

    /**
     * 【开始采集】按钮绑定事件
     * @throws Exception
     */
    @FXML
    public void startSample(ActionEvent event) throws Exception {

        /* 校验基础数据后并保存*/
        if (PlateID.getText().length() == 0) {
            alertInfo ("板号");
            return;
        } else if (ReagentType.getValue()==null) {
            alertInfo ("试剂");
            return;
        } else if (WellSize.getText().length() == 0) {
            alertInfo ("孔径");
            return;
        } else if (SampleDate.getValue()==null) {
            alertInfo ("取样日期");
            return;
        } else if (SamplePerson.getText().length() ==0) {
            alertInfo ("取样人");
            return;
        }else if (Area.getText().length() ==0) {
            alertInfo ("区域");
            return;
        }

        plate.setPlateID(PlateID.getText());
        plate.setReagentType(ReagentType.getValue().toString());
        plate.setWellSize(WellSize.getText());
        plate.setSampleDate(SampleDate.getValue().toString());
        plate.setSamplePerson(SamplePerson.getText());
        plate.setArea(Area.getText());

        /*修改页面功能*/
        PlateID.setDisable(true);//禁止【板号】
        ReagentType.setDisable(true);//禁止【试剂类型】
        WellSize.setDisable(true);//禁止【孔径】
        SampleDate.setDisable(true);//禁止【取样日期】
        SamplePerson.setDisable(true);//禁止【取样人】
        Area.setDisable(true);//禁止【区域】
        StartButton.setDisable(true);//禁止【开始按钮】
        ScanInput.setDisable(false); //解禁【条码】
        ScanInput.requestFocus();//将焦点放在【条码】输入框
        ExpButton.setDisable(false); //解禁【导出数据】
        NextPlate.setDisable(false);//解禁【下一版】
        ((Stage) vbox.getScene().getWindow()).setMaximized(true);//全屏

        //初始化“板”的孔位【数据】
        plate.initWells(ReagentType.getValue().toString());

        //初始化“板”的【图形】
        initTable();

        //条码框【回车】监听事件
        ScanInputEnterListener = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    //获取条码框当前值
                    String newValue = ((TextField)keyEvent.getTarget()).getText();
                    //检验数字位数
                    if(newValue.length()!=Well.SampleNameLength){
                        ScanInput.clear();  return;
                    }
                    //检验是否重复扫描
                    for(Well w :plate.getWellList()){
                       if(w.getSampleName()!=null&&(w.getSampleName()).equals(newValue)){
                           Alert _alert = new Alert(Alert.AlertType.NONE,"重复条码！是否确定保存？"
                                   ,new ButtonType("取消", ButtonBar.ButtonData.NO)
                                   ,new ButtonType("确定", ButtonBar.ButtonData.YES));
                           //方法用途是：将在对话框消失以前不会执行之后的代码
                           Optional<ButtonType> _buttonType = _alert.showAndWait();
                           //清空条码框
                           ScanInput.clear();
                           //接受用户决策
                           if(_buttonType.get().getButtonData().equals(ButtonBar.ButtonData.YES)){
                               try{
                                   nextWell(newValue);
                                   return;
                               }catch (Exception e){
                                   e.printStackTrace();
                               }
                           }else{
                               return;
                           }
                       }
                    }
                    //若检测不是重复条码则清空条码框并保存
                    ScanInput.clear();
                    nextWell(newValue);
                }
            }
        };

        //条码框【回车】监听事件
        ScanInput.setOnKeyPressed(ScanInputEnterListener);
    }


    /**
     * 触发下一个未完成的孔位
     * @param curVal
     */

    public void nextWell(String curVal) {

        //保存当前扫描条码值
        (plate.getWellList().get(plate.curIndex)).setSampleName(curVal);

        //改变当前孔位显示状态
        Label label = (Label)plate.circleObservableList.get(plate.curIndex);
        label.setText(curVal);
        ((Circle)label.getGraphic()).setFill(WELL_GREEN);//将当前已扫描孔位颜色变为绿色
        System.out.println("保存数据:" + curVal + "到" + plate.curIndex);

        //黄色孔位移动到下一个非完成孔位状态
        if (plate.curIndex < Plate.EndWell - 1) {

            for(int i = 0 ; i<plate.getWellList().size();i++){
                if(plate.getWellList().get(i).getSampleName()==null){
                    plate.curIndex = i;
                    Label L1 = (Label)plate.circleObservableList.get(plate.curIndex);
                    ((Circle)L1.getGraphic()).setFill(WELL_YELLOW);
                    break;
                }
            }
        }
        //到达孔位G12（即业务上最后一个可以扫描的孔位）后，弹出提示。
        else if (plate.curIndex == Plate.EndWell - 1) {

            int count = 0;
            for(Well w: plate.getWellList()){
                if(w.getSampleName()==null) count++;
            }
            String msg = "当前到达【G12】孔位，"+"未完成孔位共:"+count+"个。\n"+"是否要生成文件？";
            alertLastWell(msg);
        }

    }

    /**
     * 初始化“打板”图形
     */
    private void initTable() {

        Group colTitle = new Group();//“板”的列标题，内容为1至12 ，位于VBox的第二列
        Group FirstCol = new Group();//“板”的行标题，内容为A到H，属于第0列 ，位于 HBox 的第一列
        Group AllWells = new Group();//“板”的孔位矩阵 ，位于 HBox 的第二列

        /* 根据屏幕设置整体孔位大小、字体大小 */
        int labelHeigthAndWidth = 70;//决定孔位大小参数 默认适配屏幕1366 * 768
        if((Screen.getPrimary().getBounds()).getHeight()==1080.0){
            labelHeigthAndWidth=100;
        }

        /* “板”的列标题（要多出一节给字母列）*/
        for (int i = 0; i < Plate._Twelve.length + 1; i++) {
            Label label = new Label(i == 0 ? "" : Plate._Twelve[i - 1]);
            label.setLayoutX(i * labelHeigthAndWidth);
            label.setLayoutY(labelHeigthAndWidth/2);
            label.setMinHeight(labelHeigthAndWidth/3);
            label.setMinWidth(labelHeigthAndWidth);
            label.setAlignment(Pos.CENTER);
            colTitle.getChildren().add(label);
        }

        /* “板”的行标题 */
        for (int i = 0; i < Plate.A_H.length; i++) {
            Label label = new Label(Plate.A_H[i]);
            label.setLayoutX(labelHeigthAndWidth/2);
            label.setLayoutY(i * labelHeigthAndWidth);
            label.setMinHeight(labelHeigthAndWidth);
            label.setMinWidth(labelHeigthAndWidth);
            label.setAlignment(Pos.CENTER);
            // label.setStyle("-fx-background-color:#a6f5ff");
            FirstCol.getChildren().add(label);
        }

        /* “板”的孔位矩阵 */
        int radius = labelHeigthAndWidth/2-2;//孔位半径
        int circleNum = 0;//孔位图形编号

        //8 行每行12个孔位 共96孔位 生成孔位板的顺序为填写顺序，A1>B1>C1
        for (int i = 0; i < 12; i++) {
            int centerX = i * labelHeigthAndWidth;

            for (int j = 0; j < 8; j++) {
                int centerY = j * labelHeigthAndWidth;

                Circle circle = new Circle(radius);
                circle.setFill(WELL_DEFAULT);
                circle.setStroke(Color.DARKGREY);
                circle.setStrokeWidth(2);
                circle.setCursor(Cursor.HAND);
                circle.setId(String.valueOf(circleNum++));

                Label label  = new Label();
                label.setTextFill(Color.web("#ffffff"));
                label.setGraphic(circle);
                label.setLayoutX(centerX);
                label.setLayoutY(centerY);
                label.setContentDisplay(ContentDisplay.CENTER);

                /*
                 * 鼠标点击图形事件逻辑
                 * 获取当前图形ID，当前ID值与板的索引值相同（plate.curIndex）
                 * 第1、2、3、95号固定位即红色图形是不可以点击的，可以点击绿色或灰色图形
                 * 板上至多有一个黄色图形
                 * 点击的新点位是绿色，则表明该点位已经有扫描值，修改时可以变为黄色，移走后需要变回绿色
                 */
                circle.setOnMousePressed(event -> {

                    Circle curCircle = (Circle) event.getTarget();// 【新点位】当前触发图形
                    Circle OldCircle = (Circle)((Label)plate.circleObservableList.get(plate.curIndex)).getGraphic();//【旧点位】图形
                    int curCircleId = Integer.parseInt(curCircle.getId());//当前触发图形ID

                    //除固定点位外，需要改变界面相关状态
                    if (curCircleId != 0 && curCircleId != 1 && curCircleId != 2 && curCircleId != 95) {
                        //鼠标小手弯曲
                        circle.setCursor(Cursor.CLOSED_HAND);
                        //【新点位】除了红色之外，灰色和绿色都可以修改成黄色
                        curCircle.setFill(WELL_YELLOW);
                        //【旧点位】对应的sampleName中有值的话就变回绿色
                        if (plate.getWellList().get(plate.curIndex).getSampleName() != null) {
                            //当旧点位和新点位为同一点位时，不改变旧点位颜色
                            if (curCircleId != plate.curIndex) OldCircle.setFill(WELL_GREEN);
                        } else {
                            //当旧点位和新点位为同一点位时，不改变旧点位颜色
                            if (curCircleId != plate.curIndex) OldCircle.setFill(WELL_DEFAULT);
                        }
                        //切换当前点位索引值
                        plate.curIndex = Integer.parseInt(circle.getId());
                    }
                });

                //鼠标点击释放
                circle.setOnMouseReleased(event -> {
                    //鼠标小手弯曲伸直
                    circle.setCursor(Cursor.HAND);
                });

                AllWells.getChildren().add(label);
            }
        }

        //初始化部分颜色和数据
        plate.circleObservableList = AllWells.getChildren();
        ((Circle)((Label) plate.circleObservableList.get(0)).getGraphic()).setFill(WELL_RED);
        ((Circle)((Label) plate.circleObservableList.get(1)).getGraphic()).setFill(WELL_RED);
        ((Circle)((Label) plate.circleObservableList.get(2)).getGraphic()).setFill(WELL_RED);
        ((Circle)((Label) plate.circleObservableList.get(3)).getGraphic()).setFill(WELL_YELLOW);
        ((Circle)((Label) plate.circleObservableList.get(95)).getGraphic()).setFill(WELL_RED);
        ((Label) plate.circleObservableList.get(0)).setText(plate.getWellList().get(0).getSampleName());
        ((Label) plate.circleObservableList.get(1)).setText(plate.getWellList().get(1).getSampleName());
        ((Label) plate.circleObservableList.get(2)).setText(plate.getWellList().get(2).getSampleName());
        ((Label) plate.circleObservableList.get(95)).setText(plate.getWellList().get(95).getSampleName());

        p_title.getChildren().add(colTitle);
        p_title.setStyle("-fx-background-color: #ffffff");
        hbox.getChildren().add(FirstCol);
        hbox.getChildren().add(AllWells);
        hbox.setStyle("-fx-background-color: #ffffff");

    }

    /**
     * 导出数据测试
     * 导出需要数据、模板、文件名、路径参数
     * 后期可能会增加模板和解析方法
     * @param event
     * @throws Exception
     */
    @FXML
    public void expFiles(ActionEvent event) throws Exception {

        Stage stage = (Stage) vbox.getScene().getWindow();

        //路径选择器
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择路径");
        File f = directoryChooser.showDialog(stage);

        //文件列表
        if(f==null){ return; }//未选择路径

        /* 生成分析表 */
        ExpExcelByTemplate expExcelByFX = new ExpExcelByTemplate("分析模板.xls");
        expExcelByFX.impDataToWorkBook(plate);
        Workbook wb_fx = expExcelByFX.getWorkbook();
        FileOutputStream fos = new FileOutputStream(f.getAbsolutePath()+"/"+plate.getPlateID()+".xls");
        wb_fx.write(fos);
        wb_fx.close();
        fos.close();

        /* 生成上样表 */
        ExpExcelByTemplate expExcelByQY = new ExpExcelByTemplate("取样模板.xls");
        expExcelByQY.impDataToWorkBook(plate);
        Workbook wb_qy = expExcelByQY.getWorkbook();
        FileUtil.exlToText(f.getAbsolutePath()+"/"+plate.getPlateID()+".txt",wb_qy);
        wb_qy.close();

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("文件已生成到："+f.getAbsolutePath());
        alert.showAndWait();
    }

    /**
     * 提示基础信息必填项
     * @param
     */
    private void alertInfo (String label){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("基础信息【"+label+"】不能为空！");
        alert.showAndWait();
    }

    /**
     * G12（结束孔位）孔位提示信息
     * @param conText
     */
    private void alertLastWell(String conText) {

        Alert _alert = new Alert(Alert.AlertType.NONE
                        ,conText
                        ,new ButtonType("取消", ButtonBar.ButtonData.NO)
                        ,new ButtonType("确定", ButtonBar.ButtonData.YES));
        //设置窗口的标题
        _alert.setTitle("提醒");
        // 方法用途是：将在对话框消失以前不会执行之后的代码
        Optional<ButtonType> _buttonType = _alert.showAndWait();
        //根据点击结果返回
        if(_buttonType.get().getButtonData().equals(ButtonBar.ButtonData.YES)){
            try{
                this.expFiles(null);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 下一板功能按钮绑定事件
     * @param event
     * @throws Exception
     */
    @FXML
    public void nextPlate(ActionEvent event) throws Exception {

        int count = 0;

        for(Well w: plate.getWellList()){
            if(w.getSampleName()==null) count++;
        }

        if(count!=0){
            String msg = "未完成孔位共:"+count+"个。"+"确认开始下一板操作？\n（【导出数据】可将未完成的数据导出）";

            Alert _alert = new Alert(Alert.AlertType.NONE,msg
                    ,new ButtonType("取消", ButtonBar.ButtonData.NO)
                    ,new ButtonType("确定", ButtonBar.ButtonData.YES));
            //方法用途是：将在对话框消失以前不会执行之后的代码
            Optional<ButtonType> _buttonType = _alert.showAndWait();
            //清空条码框
            ScanInput.clear();
            //接受用户决策
            if(_buttonType.get().getButtonData().equals(ButtonBar.ButtonData.YES)){
                //提示完成
            }else{
                return;
            }
        }

        /*修改页面功能*/
        PlateID.setDisable(false);//开放【板号】
        PlateID.clear();//开放【板号】
        ReagentType.setDisable(false);//开放【试剂类型】
        WellSize.setDisable(false);//开放【孔径】
        SampleDate.setDisable(false);//开放【取样日期】
        SamplePerson.setDisable(false);//开放【取样人】
        Area.setDisable(false);//开放【区域】
        StartButton.setDisable(false);//开放【开始按钮】
        ScanInput.setDisable(true); //禁止【条码】
        ExpButton.setDisable(true); //禁止【导出数据】
        NextPlate.setDisable(true);//禁止【下一板】

        /* 图形清空 */
        hbox.getChildren().remove(0);
        hbox.getChildren().remove(0);
        p_title.getChildren().remove(0);

        /* 初始化数据 */
        plate = new Plate();

        logger.info("完成一次打板");

    }
}

