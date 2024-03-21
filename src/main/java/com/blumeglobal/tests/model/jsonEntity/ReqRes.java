package com.blumeglobal.tests.model.jsonEntity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ReqRes {

    private List<Map<String, Object>> requestData;
    private Map<String, Object> responseData;

    public ReqRes(List<Map<String, Object>> requestData, Map<String, Object> responseData) {
        this.requestData = requestData;
        this.responseData = responseData;
    }
}
