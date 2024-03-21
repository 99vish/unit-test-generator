package com.blumeglobal.tests.cache;

import com.blumeglobal.tests.util.JavaParserUtil;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.nio.file.Path;
import java.util.*;

public class Cache {

    public static final  Map<String, ClassOrInterfaceDeclaration> classOrInterfaceDeclarationMap = new HashMap<>();
    public static final  Map<ClassOrInterfaceDeclaration,Path> classOrInterfaceDeclarationToPathMap = new HashMap<>();

    @Getter
    public static final List<Map<String,List<String>>> classOrInterfaceToMethodsMapList = new ArrayList<>();

    @Getter
    public static  List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = new ArrayList<>();
    private final static Map<ClassOrInterfaceDeclaration,ClassOrInterfaceDeclaration> classOrInterfaceDeclarationToEntityMap = new HashMap<>();

    public static void cacheClassOrInterfaceDeclarations(String moduleRootPath) {

        classOrInterfaceDeclarations = JavaParserUtil.getClassOrInterfaceDeclarations(moduleRootPath,classOrInterfaceDeclarationToPathMap,classOrInterfaceDeclarationMap);
        for(ClassOrInterfaceDeclaration classOrInterfaceDeclaration: classOrInterfaceDeclarations) {
            Map<String,List<String>> map = new HashMap<>();
            map.put(classOrInterfaceDeclaration.getNameAsString(),getMethodDeclarationsByClassName(classOrInterfaceDeclaration.getNameAsString()));
            classOrInterfaceToMethodsMapList.add(map);
        }
    }

    public static ClassOrInterfaceDeclaration getClassOrInterfaceDeclarationByClassName(String className) {

        return classOrInterfaceDeclarationMap.get(className);
    }


    public static List<String> getMethodDeclarationsByClassName(String className){
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = classOrInterfaceDeclarationMap.get(className);
        List<String> methods = new ArrayList<>();
        for(MethodDeclaration methodDeclaration: classOrInterfaceDeclaration.getMethods()) {
            methods.add(methodDeclaration.getNameAsString());
        }
        return methods;
    }

    public static MethodDeclaration getMethodDeclaration(String className, String methodName){
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = classOrInterfaceDeclarationMap.get(className);
        List<MethodDeclaration> methodsByName = classOrInterfaceDeclaration.getMethodsByName(methodName);
        if(CollectionUtils.isNotEmpty(methodsByName)){
            return methodsByName.get(0);
        }
        return null;
    }

    public static Path getPathFromClassDeclaration(ClassOrInterfaceDeclaration classOrInterfaceDeclaration){
        return classOrInterfaceDeclarationToPathMap.get(classOrInterfaceDeclaration);
    }

    public static String getPackageFromClassDeclarationNode(Node node) {
        String packageName = null;
        if (node.findCompilationUnit().isPresent()) {
            packageName = node.findCompilationUnit().get().getPackageDeclaration().get().getNameAsString();
        }
        return packageName;
    }

    public static List<String> getImportsFromClassDeclarationNode(Node node) {
        List<String> imports = new ArrayList<>();
        if (node.findCompilationUnit().isPresent()) {
            List<ImportDeclaration> importDeclarations = node.findCompilationUnit().get().getImports();
            for (ImportDeclaration importDeclaration : importDeclarations) {
                imports.add(importDeclaration.toString());
            }
        }
        return imports;
    }


}

