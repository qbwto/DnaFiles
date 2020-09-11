package sample;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sample.controller.SampleController;


/**
 * 主页
 */
public class Main extends Application {

    @Override
    public void init() throws Exception {
        super.init();

    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        primaryStage.setTitle("实验室小工具");
        primaryStage.setScene(new Scene(root, 600, 350));
        primaryStage.show();

    }

    /**
     * 【打板】模块涉及的类：
     * controller.SampleController
     * entity.Plate 、entity.Well
     * utils.ExpExcelByTemplate
     * ExpFileTemplate/*
     * fxml/sample.fxml
     * @param event
     * @throws Exception
     */
    @FXML
    protected void gotoSampleStage(ActionEvent event)throws Exception {

        Stage stage=new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/sample.fxml"));
        Parent root = (Parent)loader.load();
        stage.setScene(new Scene(root, 950, 600));
        SampleController controller = (SampleController)loader.getController();
        controller.init();//调用控制器方法
        stage.setTitle("实验室小工具 | 打板");
        stage.show();


    }

    /**
     * 【复检】模块
     * @param event
     * @throws Exception
     */
    @FXML
    protected void gotoRecheck(ActionEvent event)throws Exception {

        Stage stage=new Stage();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/recheck.fxml"));
        Parent root = (Parent)loader.load();
        stage.setScene(new Scene(root, 1000, 600));
        stage.setTitle("实验室小工具 | 复检");
        stage.show();
    }


    /**
     * 主程序方法
     */
    public static void main(String[] args) {
        launch(args);
    }
}

