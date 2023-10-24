package com.cas.entity;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;
import lombok.Data;

@Data
public class ServiceNode {
    private String serviceVariableName;
    private String serviceMethodName;
    private Type serviceInterfaceType;
    private Type serviceImplClassType;
    private ClassOrInterfaceDeclaration serviceInterfaceDeclaration;
    private ClassOrInterfaceDeclaration serviceImplClassDeclaration;
    private MethodDeclaration serviceMethod;
    private MethodDeclaration serviceImplMethod;
}
