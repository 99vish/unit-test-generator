package com.blumeglobal.tests.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathGeneratorUtil {

    public static Path getTestFolderPath(Path path, String className){

        int mainIndex = getKeyWordIndex("main",path);
        Path testFolderPath = path.getRoot().resolve(path.subpath(0, mainIndex + 1))
                .resolveSibling("test")
                .resolve(path.subpath(mainIndex + 1, path.getNameCount()-1));

        Path filePath = testFolderPath.resolve(className + "Test.java");

        if (!Files.exists(testFolderPath)) {
            try {
                Files.createDirectories(testFolderPath);
                Files.createFile(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return filePath;
        //return path.getRoot().resolve(path.subpath(0,mainIndex+1)).resolveSibling("test").resolve(path.subpath(mainIndex+1, path.getNameCount())).resolveSibling(className+"Test.java");
    }

    public static Path getPathForUtilCLassGeneration(Path path,String className){

        int mainIndex = getKeyWordIndex("main",path);
        int blumeGlobalIndex = getKeyWordIndex("blumeglobal",path);

        return path.getRoot().resolve(path.subpath(0,mainIndex+1)).resolveSibling("test").resolve(path.subpath(mainIndex+1,blumeGlobalIndex+1)).resolve(className);
    }

    public static Path getPathForJsonGeneration(Path path,String className,String methodName){

        int mainIndex = getKeyWordIndex("main",path);
        return path.getRoot().resolve(path.subpath(0,mainIndex+1)).resolveSibling("test").resolve("resources").resolve(className).resolve(methodName);
    }


    private static int getKeyWordIndex(String word,Path path){
        int index= -1;
        for (int i=0;i<path.getNameCount();i++){
            if(word.equals(path.getName(i).toString())){
                String s =path.getName(i).toString();
                index = i;
                break;
            }
        }
        return index;
    }
}
