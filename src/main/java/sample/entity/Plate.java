package sample.entity;

import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.util.ArrayList;

public class Plate {

    private String ContainerName;//实验室编号
    private String PlateID;//实验室编号
    private String ReagentType;//试剂
    private String WellSize;//孔径
    private String SampleDate;//取样日期
    private String SamplePerson;//取样人
    private String area;//区域

    private ArrayList<Well> wellList;//孔位列表
    public ObservableList<Node> circleObservableList;//孔位图形列表;
    public int curIndex = 3;//业务上的起始孔位序号

    public final static String [] A_H = {"A","B","C","D","E","F","G","H"};
    public final static String [] _Twelve = {"01","02","03","04","05","06","07","08","09","10","11","12"};
    public final static int EndWell = 95;//结束孔位序号，从0开始


    public String getContainerName() {
        return ContainerName;
    }

    public void setContainerName(String containerName) {
        ContainerName = containerName;
    }

    public String getPlateID() {
        return PlateID;
    }

    public void setPlateID(String plateID) {
        PlateID = plateID;
    }

    public String getReagentType() {
        return ReagentType;
    }

    public void setReagentType(String reagentType) {
        ReagentType = reagentType;
    }

    public String getWellSize() {
        return WellSize;
    }

    public void setWellSize(String wellSize) {
        WellSize = wellSize;
    }

    public String getSampleDate() {
        return SampleDate;
    }

    public void setSampleDate(String sampleDate) {
        SampleDate = sampleDate;
    }

    public String getSamplePerson() {
        return SamplePerson;
    }

    public void setSamplePerson(String samplePerson) {
        SamplePerson = samplePerson;
    }

    public ArrayList<Well> getWellList() {
        return wellList;
    }

    public void setWellList(ArrayList<Well> wellList) {
        this.wellList = wellList;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    /**
     * 孔位数据初始化
     * @param reagentType 试剂
     * @return
     */
    public ArrayList<Well> initWells(String reagentType){

        wellList = new ArrayList<Well>();

        for(int i = 0;i <_Twelve.length ; i++) {
            for (int j = 0; j < A_H.length; j++) {

                String _num = A_H[j] + _Twelve[i];
                Well well = new Well();
                well.setWellNum(_num);
                wellList.add(well);
            }
        }

        //每个板共96个孔位,第一孔位A1=CK 、第三孔位C1=ladder、第九十六孔位H12=ladder 为固定值
        wellList.get(0).setSampleName("CK");
        if(reagentType.equals("typer-29")){
            wellList.get(1).setSampleName("9948");
        }else if(reagentType.equals("typer-21")){
            wellList.get(1).setSampleName("9947");
        }
        wellList.get(2).setSampleName("Ladder");
        wellList.get(95).setSampleName("Ladder");

        return wellList;
    }

    // 孔位数据输出
    public void P_wellList(){

        for (Well w : wellList) {
            System.out.println(w.toString());
        }
        System.out.println("孔位总数："+wellList.size());
    }
}
