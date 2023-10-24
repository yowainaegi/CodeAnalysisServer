package com.cas.entity;

import lombok.Data;

@Data
public class APIEntity {
    private String apiPath;
    private ControllerNode controllerNode;
}
