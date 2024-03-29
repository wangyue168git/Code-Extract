package com.wangyue.code.extract;

import com.wangyue.code.extract.config.SystemEnvironmentConfig;
import com.wangyue.code.extract.utils.BufferedRandomAccessFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author wesleywang
 * @Description:
 * @date 2020/11/12
 */
@Slf4j
@Component
public class CodeExtract {

    @Autowired
    private SystemEnvironmentConfig systemEnvironmentConfig;
    int j  = 0;

    @PostConstruct
    public void extractCode() throws IOException {
        File file = new File(systemEnvironmentConfig.getOutputFile());
        if (file.delete()) {
            file.createNewFile();
        }
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
//        BufferedRandomAccessFile writer = new BufferedRandomAccessFile(file, "rw");
        findFile(systemEnvironmentConfig.getFilepath(), writer);
        log.info("项目总行数 ==" + j);
        writer.close();
        System.exit(1);
    }


    public void findFile(String path, BufferedWriter newFile) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            List<File> fileList = Arrays.asList(files);
            fileList.sort(Comparator.comparing(File::getName));
            if (!fileList.isEmpty()) {
                for (File file2 : fileList) {
                    if (file2.isDirectory()) {
                        System.out.println("directory file:" + file2.getAbsolutePath());
                        if (file2.getName().equals("test") || file2.getName().equals(".git")
                                || file2.getName().equals(".gradle")) {
                            continue;
                        }
                        findFile(file2.getAbsolutePath(), newFile);
                    } else {
                        if (file2.getName().endsWith("."+ systemEnvironmentConfig.getFiletype())) {
                            System.out.println("code file:" + file2.getAbsolutePath());
                            handle(file2, newFile);
                        }
                    }
                }
            }
        } else {
            System.out.println("file not exist!");
        }
    }

    private void handle(File file2, BufferedWriter newFile) throws IOException {
        int i = 1;
        StringBuilder builder = new StringBuilder();
        BufferedRandomAccessFile reader = new BufferedRandomAccessFile(file2, "r");
        String str;
        while ((str = reader.readLine()) != null) {
            if (str.equals("") || str.equals("\n") || str.equals("\r\n")
                    || str.matches("^\\s*/\\*{1,2}[\\s\\S]*?")
                    || str.matches("^\\s*//[\\s\\S]*?")
                    || str.matches("^\\s*\\n\n")
                    || str.matches("^\\s*\\*[\\s\\S]*?")) {
                continue;
            }
            i++;
            j++;
            builder.append(str).append("\n");
        }
        String firstLine = null;
        if (systemEnvironmentConfig.getFiletype().equals("java") && file2.getAbsolutePath().contains("main/java")) {
            firstLine =
                    file2.getAbsolutePath().split("main/java")[1] + "\t" + i + "行" + "\n";
        }
        if (systemEnvironmentConfig.getFiletype().equals("sol")){
            firstLine = file2.getName() +"\t"+ i + "行" + "\n";
        }
        String string = firstLine + builder.toString();
        newFile.write(string);
        newFile.flush();// 清空缓冲区
    }
}
