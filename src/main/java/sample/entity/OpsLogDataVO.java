package sample.entity;

import java.io.File;

/**
 * 比对日志
 */
public class OpsLogDataVO {

    private String fileName_a; //文件A文件名
    private String fileName_b; //文件B文件名
    private int hitQuantity; //比对命中数量
    private int missQuantity; //未比对命中数量
    private int file_a_quantity;//文件A提取数据量
    private int file_b_quantity;//文件B提取数据量
    private File file_a;
    private File file_b;
    //文件A 提取编号数组
    //文件B 提取编号数组

    public OpsLogDataVO() {
        this.hitQuantity = 0;
        this.missQuantity = 0;
        file_a_quantity = 0;
        file_b_quantity = 0;
    }

    public String getFileName_a() {
        return fileName_a;
    }

    public void setFileName_a(String fileName_a) {
        this.fileName_a = fileName_a;
    }

    public String getFileName_b() {
        return fileName_b;
    }

    public void setFileName_b(String fileName_b) {
        this.fileName_b = fileName_b;
    }

    public int getHitQuantity() {
        return hitQuantity;
    }

    public void setHitQuantity(int hitQuantity) {
        this.hitQuantity = hitQuantity;
    }

    public int getMissQuantity() {
        return missQuantity;
    }

    public void setMissQuantity(int missQuantity) {
        this.missQuantity = missQuantity;
    }

    public int getFile_a_quantity() {
        return file_a_quantity;
    }

    public void setFile_a_quantity(int file_a_quantity) {
        this.file_a_quantity = file_a_quantity;
    }

    public int getFile_b_quantity() {
        return file_b_quantity;
    }

    public void setFile_b_quantity(int file_b_quantity) {
        this.file_b_quantity = file_b_quantity;
    }

    public File getFile_a() {
        return file_a;
    }

    public void setFile_a(File file_a) {
        this.file_a = file_a;
    }

    public File getFile_b() {
        return file_b;
    }

    public void setFile_b(File file_b) {
        this.file_b = file_b;
    }
}
