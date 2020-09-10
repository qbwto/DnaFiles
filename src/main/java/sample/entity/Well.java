package sample.entity;



public class Well {


    private String WellNum;//孔位号
    private String SampleName; //实验室编号，即扫码样本获取的编号
    public final static int SampleNameLength = 9;//实验室编号长度为9


    public String getWellNum() {
        return WellNum;
    }

    public String getSampleName() {
        return SampleName;
    }

    public void setWellNum(String wellNum) {
        WellNum = wellNum;
    }

    public void setSampleName(String sampleName) {
        SampleName = sampleName;
    }

    @Override
    public String toString() {
        return "Well{" +
                "WellNum='" + WellNum + '\'' +
                ", SampleName='" + SampleName + '\'' +
                '}';
    }
}
