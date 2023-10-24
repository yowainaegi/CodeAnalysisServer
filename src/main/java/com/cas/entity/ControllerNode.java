package com.cas.entity;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;
import lombok.Data;

import java.util.List;

@Data
public class ControllerNode {
    private Type type;
    private ClassOrInterfaceDeclaration controllerClassDeclaration;
    private MethodDeclaration controllerMethod;
    private List<ServiceNode> serviceNodeList;
}
