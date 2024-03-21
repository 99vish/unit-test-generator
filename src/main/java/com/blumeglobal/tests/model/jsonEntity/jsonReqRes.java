package com.blumeglobal.tests.model.jsonEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class jsonReqRes {

    private String requestJson;
    private String reponseJson;

    public jsonReqRes(String requestJson, String reponseJson) {
        this.requestJson = requestJson;
        this.reponseJson = reponseJson;
    }
}
