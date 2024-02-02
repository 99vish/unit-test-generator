package com.blumeglobal.tests.model.output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Argument {

    private String name;

    private String value;

    private String dataType;

    private boolean requestBody;

    private String entityType;

    private String pathToJsonFile;



}
