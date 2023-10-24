package com.cas.controller;

import com.cas.utils.AnalysisUtil;
import com.cas.utils.ClassicUtil;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/demo")
public class DemoController {

    @GetMapping("")
    public String test() {
        return "project working ...";
    }

    @GetMapping("testJavaFile")
    public String testJavaFile() throws FileNotFoundException {
        List<File> javaFileList = new ArrayList<>();
        List<File> xmlFileList = new ArrayList<>();
        String javaFilepath = "C:\\Users\\sijie.dai\\work\\firstlife\\code\\workspace_qolead\\next\\api";
        ClassicUtil.classicProjectFiles(javaFileList, xmlFileList, new File(javaFilepath));

        AnalysisUtil.AnalysisAPIEntity(javaFileList);
        return null;
    }


    private String getApiClass(File file) throws FileNotFoundException {
        String result = null;

        try {
            CompilationUnit cu = StaticJavaParser.parse(file);
            for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                for (AnnotationExpr annotation : classOrInterfaceDeclaration.getAnnotations()) {
                    // 判断其是否含有@Controller/@restController注解
                    if("@Controller".equals(annotation.toString())
                            || "@RestController".equals(annotation.toString())) {
                        result = classOrInterfaceDeclaration.getNameAsString();
                    }
                }
            }
        } catch (ParseProblemException e) {
            System.err.println(file.getName());
        }
        return result;
    }

    private void recursionGetFileObject(List<File> files, File file) {
        if(file.isDirectory()) {
            for (File listFile : file.listFiles()) {
                recursionGetFileObject(files, listFile);
            }
        } else {
            // 只选取java文件
            if(file.getName().endsWith(".java"))
                files.add(file);
        }
    }
}
