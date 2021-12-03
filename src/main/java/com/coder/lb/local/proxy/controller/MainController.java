package com.coder.lb.local.proxy.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;

/**
 * @author zhuhf
 */
public class MainController {
    private int count = 0;
    public Label welcomeText;

    public void onHelloButtonClick(ActionEvent actionEvent) {
        welcomeText.setText("点了" + (count++) + "次了");
    }
}
