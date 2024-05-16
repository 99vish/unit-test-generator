package com.blumeglobal.tests.model.output;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Argument {

    private String name;

    private String value;

    private String dataType;

    private Boolean hasAnnotation = false;

    private String annotationType = "";

    private String pathToJsonFile;

    private Boolean isApiRequest = false ;

    private int argumentOrder;

}
