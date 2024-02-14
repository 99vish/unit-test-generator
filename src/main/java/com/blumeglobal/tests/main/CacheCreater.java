package com.blumeglobal.tests.main;

import com.blumeglobal.tests.cache.Cache;
import com.blumeglobal.tests.cache.InputTestCasesCache;
import com.blumeglobal.tests.controller.PathController;

import java.nio.file.Path;

public class CacheCreater {

    public static void createCache() {


        Path projectPath = PathController.getProjectPath();
        Path excelPath = PathController.getExcelPath();
        Path interfacesPath = PathController.getInterfacesPath();


        Cache.cacheClassOrInterfaceDeclarations(projectPath.toString());//883
        Cache.cacheClassOrInterfaceDeclarations(interfacesPath.toString());//400

        InputTestCasesCache.cacheInputTestCases(excelPath);

    }

}
