package com.blumeglobal.tests.cache;

import com.blumeglobal.tests.util.JavaParserUtil;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.apache.commons.collections4.CollectionUtils;

import java.nio.file.Path;
import java.util.*;

public class Cache {

    private static  Map<String, ClassOrInterfaceDeclaration> classOrInterfaceDeclarationMap = new HashMap<>();

    private static  Map<ClassOrInterfaceDeclaration,Path> classOrInterfaceDeclarationToPathMap = new HashMap<>();

    public void cacheClassOrInterfaceDeclarations(String moduleRootPath) {

        //List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = JavaParserUtil.getClassOrInterfaceDeclarations(moduleRootPath);
        List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = JavaParserUtil.getClassOrInterfaceDeclarations(moduleRootPath,classOrInterfaceDeclarationToPathMap,classOrInterfaceDeclarationMap);
//        for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarations) {
//            classOrInterfaceDeclarationMap.put(classOrInterfaceDeclaration.getNameAsString(), classOrInterfaceDeclaration);
//        }


    }

    public static ClassOrInterfaceDeclaration getClassOrInterfaceDeclarationByClassName(String className) {
        return classOrInterfaceDeclarationMap.get(className);
    }

    public static Collection<ClassOrInterfaceDeclaration> getClassOrInterfaceDeclarations() {
        return classOrInterfaceDeclarationMap.values();
    }

    public static List<MethodDeclaration> getMethodDeclarationsByClassName(String className){
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = classOrInterfaceDeclarationMap.get(className);
        return classOrInterfaceDeclaration.getMethods();
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

