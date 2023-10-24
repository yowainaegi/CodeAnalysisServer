package com.cas.entity;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import lombok.Data;

import java.io.File;

@Data
public class MapperNode {
    private ClassOrInterfaceDeclaration mapperInterfaceDeclaration;
    private File xml;
}
