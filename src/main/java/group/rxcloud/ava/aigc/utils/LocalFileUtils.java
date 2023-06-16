package group.rxcloud.ava.aigc.utils;

import org.jetbrains.annotations.NotNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class LocalFileUtils {

    public static void main(String[] args) throws IOException {
        LocalFileUtils.saveFile("https://www.baidu.com/img/bd_logo1.png");
    }

    public static final String FILE_PATH = "./";

    @NotNull
    private static String getDefaultFilePath() {
        return FILE_PATH + System.currentTimeMillis();
    }

    public static String saveFile(String url) throws IOException {
        return saveFile(url, getDefaultFilePath());
    }

    public static String saveFile(String url, String filePath) throws IOException {
        // 从URL获取输入流
        URL urlO = new URL(url);
        InputStream inputStream = urlO.openStream();

        return saveFile(inputStream, filePath);
    }

    public static String saveFile(InputStream inputStream) throws IOException {
        return saveFile(inputStream, getDefaultFilePath());
    }

    public static String saveFile(InputStream inputStream, String filePath) throws IOException {
        // 创建输出流
        FileOutputStream outputStream = new FileOutputStream(filePath);

        // 将输入流中的数据写入输出流
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        // 关闭输入流和输出流
        inputStream.close();
        outputStream.close();

        return filePath;
    }
}
