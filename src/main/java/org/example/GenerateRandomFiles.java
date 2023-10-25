package org.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
//此类用于创建   大小为1-5M 格式为JPG   peng  wav  文件名从0到99的文件
public class GenerateRandomFiles {

    private static final String FILE_PATH = "M:\\files1\\";
    //win本地路径
    private static final int NUM_FILES = 500;//文件的数量
    private static final int MIN_SIZE = 1; // 最小文件大小（MB）
    private static final int MAX_SIZE = 5; // 最大文件大小（MB）
    
    public static void main(String[] args) {
        File directory = new File(FILE_PATH);
        // 如果目录不存在，创建目录
        if (!directory.exists()) {
            directory.mkdirs(); // 创建多级目录
        }
        Random random = new Random();

        for (int i = 0; i < NUM_FILES; i++) {
            String fileName = FILE_PATH + i + getFileExtension(random.nextInt(7));
            int fileSize = random.nextInt(MAX_SIZE - MIN_SIZE + 1) + MIN_SIZE; // 随机生成文件大小

            generateRandomFile(fileName, fileSize);
        }

        System.out.println("Files generated successfully.");
    }
    private static void generateRandomFile(String fileName, int fileSize) {
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            byte[] data = new byte[fileSize * 1024 * 1024]; // 文件大小转换为字节数

            // 随机填充文件内容
            new Random().nextBytes(data);

            // 写入文件
            outputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFileExtension(int fileType) {
        switch (fileType) {
            case 0:
                return ".wav";
            case 1:
                return ".mp3";
            case 2:
                return ".jpg";
            case 3:
                return ".png";
            case 4:
                return ".pdf";
            case 5:
                return ".xls";
            case 6:
                 return ".doc";
            default:
                return ".txt"; // 默认为.txt格式
        }
    }
}
