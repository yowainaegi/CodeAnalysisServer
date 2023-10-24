package com.cas.utils;

import com.cas.entity.APIEntity;
import com.cas.entity.ControllerNode;
import com.cas.entity.ServiceNode;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnalysisUtil {
    public static void AnalysisAPIEntity(List<File> javaFileList) throws FileNotFoundException {
        List<APIEntity> apiEntityList = new ArrayList<>();

        // 初步获取所有的controllerNode节点
        for (File javaFile : javaFileList) {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);

            // 获取文件中所有的类或接口
            List<ClassOrInterfaceDeclaration> cidList = cu.findAll(ClassOrInterfaceDeclaration.class);
            for (ClassOrInterfaceDeclaration cid : cidList) {
                // 获取类或接口中所有的类注解
                for (AnnotationExpr classAnnotation : cid.getAnnotations()) {
                    // 确定位一个controller类
                    if (classAnnotation.getNameAsString().toUpperCase(Locale.ROOT).contains("controller".toUpperCase(Locale.ROOT))) {
                        // 找出controllerNode节点
                        List<APIEntity> apiEntities = AnalysisControllerFile(cid);
                        apiEntityList.addAll(apiEntities);
                    }
                }
            }
        }

        // 完善所有serviceNode节点
        for (APIEntity apiEntity : apiEntityList) {
            AnalysisServiceFile(apiEntity.getControllerNode().getServiceNodeList(), javaFileList);
        }

        System.out.println(apiEntityList);

    }

    private static List<APIEntity> AnalysisControllerFile(ClassOrInterfaceDeclaration controllerDeclaration) {
        List<APIEntity> apiEntityList = new ArrayList<>();
        for (AnnotationExpr classAnnotation : controllerDeclaration.getAnnotations()) {
            // autowired注入的类变量集合
            List<FieldDeclaration> autowiredClassFieldDeclarationList = new ArrayList<>();
            // 寻找出所有autowired注入的变量
            List<FieldDeclaration> classFieldDeclarationList = controllerDeclaration.findAll(FieldDeclaration.class);
            for (FieldDeclaration fieldDeclaration : classFieldDeclarationList) {
                for (AnnotationExpr fieldAnnotation : fieldDeclaration.getAnnotations()) {
                    if(fieldAnnotation.getNameAsString().toUpperCase(Locale.ROOT).endsWith("Autowired".toUpperCase(Locale.ROOT))) {
                        autowiredClassFieldDeclarationList.add(fieldDeclaration);
                    }
                }
            }

            // 类注解中的requestMapping，api路径前缀
            String apiPathPrefix = "";
            if (classAnnotation.getNameAsString().toUpperCase(Locale.ROOT).endsWith("Mapping".toUpperCase(Locale.ROOT))) {
                apiPathPrefix = AnalysisAnnotation(classAnnotation);
                for (MethodDeclaration method : controllerDeclaration.getMethods()) {
                    for (AnnotationExpr methodAnnotation : method.getAnnotations()) {
                        String apiPathSuffix = "";
                        if(methodAnnotation.getNameAsString().toUpperCase(Locale.ROOT).endsWith("Mapping".toUpperCase(Locale.ROOT))) {
                            apiPathSuffix = AnalysisAnnotation(methodAnnotation);
                            // 组装出apiEntity
                            APIEntity apiEntity = new APIEntity();
                            apiEntity.setApiPath(apiPathPrefix + apiPathSuffix);
                            // controller节点
                            ControllerNode controllerNode = new ControllerNode();
                            controllerNode.setControllerClassDeclaration(controllerDeclaration);
                            controllerNode.setControllerMethod(method);
                            apiEntity.setControllerNode(controllerNode);

                            // serverNode节点，存在autowired自动注入变量，放入
                            if(!autowiredClassFieldDeclarationList.isEmpty()) {
                                List<ServiceNode> serviceNodeList = new ArrayList<>();
                                // 寻找是否使用了@Autowired注入的类变量
                                for (FieldDeclaration acfd : autowiredClassFieldDeclarationList) {
                                    if(method.getBody().toString().contains(acfd.getVariables().get(0).getNameAsString())) {
                                        ServiceNode serviceNode = new ServiceNode();
                                        serviceNode.setServiceInterfaceType(acfd.getVariables().get(0).getType());
                                        serviceNode.setServiceVariableName(acfd.getVariables().get(0).getNameAsString());
                                        method.accept(new VoidVisitorAdapter<Void>() {
                                            @Override
                                            public void visit(MethodCallExpr methodCall, Void arg) {
                                                super.visit(methodCall, arg);
                                                if(methodCall.getScope().isPresent()) {
                                                    String className = methodCall.getScope().get().toString();
                                                    if(className.equals(serviceNode.getServiceVariableName())) {
                                                        String methodName = methodCall.getName().getIdentifier();
                                                        serviceNode.setServiceMethodName(methodName);
                                                    }
                                                }
                                            }
                                        }, null);
                                        serviceNodeList.add(serviceNode);
                                        controllerNode.setServiceNodeList(serviceNodeList);
                                    }
                                }
                            }
                            apiEntityList.add(apiEntity);
                        }
                    }
                }
            }
        }
        return apiEntityList;
    }

    private static void AnalysisServiceFile(List<ServiceNode> serviceNodeList, List<File> javaFileList) throws FileNotFoundException {
        for (File javaFile : javaFileList) {
            CompilationUnit cu = StaticJavaParser.parse(javaFile);
            for (ClassOrInterfaceDeclaration classDeclaration : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                for (AnnotationExpr classAnnotation : classDeclaration.getAnnotations()) {
                    if(classAnnotation.getNameAsString().toUpperCase(Locale.ROOT).contains("Service".toUpperCase(Locale.ROOT))) {
                        for (ClassOrInterfaceType implementedType : classDeclaration.getImplementedTypes()) {
                            for (ServiceNode serviceNode : serviceNodeList) {
                                if(implementedType.equals(serviceNode.getServiceInterfaceType())) {
                                    serviceNode.setServiceImplClassDeclaration(classDeclaration);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static String AnalysisAnnotation(AnnotationExpr annotationExpr) {
        if (annotationExpr.isNormalAnnotationExpr()) {
            NormalAnnotationExpr normalAnnotationExpr = annotationExpr.asNormalAnnotationExpr();
            if (normalAnnotationExpr.getPairs().size() == 1) {
                return normalAnnotationExpr.getPairs().get(0).getValue().toString().replace("\"", "");
            } else {
                for (MemberValuePair pair : normalAnnotationExpr.getPairs()) {
                    if (pair.getName().toString().contains("value")) {
                        return pair.getValue().toString();
                    }
                }
            }
        } else if (annotationExpr.isMarkerAnnotationExpr()) {
            MarkerAnnotationExpr markerAnnotationExpr = annotationExpr.asMarkerAnnotationExpr();
//            System.out.println(markerAnnotationExpr);
            return null;
        }
        return null;
    }

}
