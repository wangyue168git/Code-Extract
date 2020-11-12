package com.webank.code.extract;

import com.webank.code.extract.config.SystemEnvironmentConfig;
import com.webank.code.extract.utils.BufferedRandomAccessFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

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

    @PostConstruct
    public void extractCode() throws IOException {
        File file = new File("code.txt");
        if (file.delete()) {
            file.createNewFile();
        }
        BufferedRandomAccessFile writer = new BufferedRandomAccessFile(file, "rw");
        findJavaFile(systemEnvironmentConfig.getFilepath() ,writer);
        writer.close();
        System.exit(1);
    }

    public void findJavaFile(String path, BufferedRandomAccessFile newFile) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (null != files) {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        System.out.println("directory file:" + file2.getAbsolutePath());
                        findJavaFile(file2.getAbsolutePath(), newFile);
                    } else {
                        if (file2.getName().endsWith(".java")) {
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

    private void handle(File file2, BufferedRandomAccessFile newFile) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(file2.toURI()), StandardCharsets.UTF_8)) {
            stream.forEach(str -> {
                if (str == null || str.equals("")) {
                    return;
                }
                builder.append(str).append("\n");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        String string = builder.toString();
        string = string.replaceAll("/\\*{1,2}[\\s\\S]*?\\*/\n","");
        string = string.replaceAll("//[\\s\\S]*?\\n","");
        string = string.replaceAll("^\\s*\\n\n","");
        newFile.seek(file2.length());
        newFile.writeBytes(string);
    }
}
