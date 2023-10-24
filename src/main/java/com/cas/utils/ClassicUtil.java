package com.cas.utils;

import com.cas.entity.ControllerNode;

import java.io.File;
import java.util.List;

public class ClassicUtil {
    public static void classicProjectFiles(List<File> javaFileList, List<File> xmlFileList, File file) {
        if (file.isDirectory() && !file.getName().contains("target"))
            for (File listFile : file.listFiles()) {
                classicProjectFiles(javaFileList, xmlFileList, listFile);
            }
        else if (file.getName().endsWith(".java"))
            javaFileList.add(file);
        else if (file.getName().endsWith(".xml"))
            xmlFileList.add(file);
    }
}
