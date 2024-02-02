package com.blumeglobal.tests.model.request;

import com.blumeglobal.tests.controller.PathController;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;


@Getter
@Setter
public class PathRequest {

    private Path excelPath;
    private Path projectPath;
}
