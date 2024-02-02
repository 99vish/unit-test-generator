package com.blumeglobal.tests.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JavaParserUtil {

    public static List<ClassOrInterfaceDeclaration> getClassOrInterfaceDeclarations(String directoryPath) {
        List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = new ArrayList<>();
        JavaParser javaParser = new JavaParser();
        List<File> allJavaFiles = new ArrayList<>();
        getAllJavaFiles(new File(directoryPath), allJavaFiles);
        if (CollectionUtils.isNotEmpty(allJavaFiles)) {
            for (File javaFile : allJavaFiles) {
                try {
                    ParseResult<CompilationUnit> parseResult = javaParser.parse(javaFile);
                    if (parseResult.isSuccessful()) {
                        Optional<CompilationUnit> result = parseResult.getResult();
                        if (result.isPresent()) {
                            CompilationUnit compilationUnit = result.get();
                            List<ClassOrInterfaceDeclaration> compilationUnitAll = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
                            classOrInterfaceDeclarations.addAll(compilationUnitAll);
                        }
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return classOrInterfaceDeclarations;
    }


    public static List<ClassOrInterfaceDeclaration> getClassOrInterfaceDeclarations(String directoryPath, Map<ClassOrInterfaceDeclaration, Path>classDeclarationToPathMap, Map<String,ClassOrInterfaceDeclaration> classOrInterfaceDeclarationMap) {
        List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = new ArrayList<>();
        JavaParser javaParser = new JavaParser();
        List<File> allJavaFiles = new ArrayList<>();
        getAllJavaFiles(new File(directoryPath), allJavaFiles);
        if (CollectionUtils.isNotEmpty(allJavaFiles)) {
            for (File javaFile : allJavaFiles) {
                try {
                    ParseResult<CompilationUnit> parseResult = javaParser.parse(javaFile);
                    if (parseResult.isSuccessful()) {
                        Optional<CompilationUnit> result = parseResult.getResult();
                        if (result.isPresent()) {
                            CompilationUnit compilationUnit = result.get();
                            List<ClassOrInterfaceDeclaration> compilationUnitAll = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
                            for(ClassOrInterfaceDeclaration classOrInterfaceDeclaration:compilationUnitAll){
                                classDeclarationToPathMap.put(classOrInterfaceDeclaration,javaFile.toPath());
                                classOrInterfaceDeclarationMap.put(classOrInterfaceDeclaration.getNameAsString(),classOrInterfaceDeclaration);
                            }
                            classOrInterfaceDeclarations.addAll(compilationUnitAll);
                        }
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return classOrInterfaceDeclarations;
    }

    private static void getAllJavaFiles(File directory, List<File> javaFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                } else if (file.isDirectory()) {
                    getAllJavaFiles(file, javaFiles);
                }
            }
        }
    }

    public static List<ClassOrInterfaceDeclaration> getClassOrInterfaceDeclarationByType(List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations, String type) {
        List<ClassOrInterfaceDeclaration> declarations = new ArrayList<>();
        for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarations) {
            List<AnnotationExpr> annotations = classOrInterfaceDeclaration.getAnnotations();
            if (CollectionUtils.isNotEmpty(annotations)) {
                for (AnnotationExpr annotation : annotations) {
                    if (annotation instanceof MarkerAnnotationExpr) {
                        MarkerAnnotationExpr markerAnnotation = (MarkerAnnotationExpr) annotation;
                        if (markerAnnotation.getNameAsString().equals(type)) {
                            declarations.add(classOrInterfaceDeclaration);
                        }
                    }
                }
            }
        }
        return declarations;
    }
}
