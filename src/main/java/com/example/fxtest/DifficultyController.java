package com.example.fxtest;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Properties;

//import static com.example.fxtest.Main.loadProperties;

public class DifficultyController {
    @FXML
    private RadioButton easyRadioButton;

    @FXML
    private RadioButton normalRadioButton;

    @FXML
    private RadioButton hardRadioButton;

    private ToggleGroup toggleGroup;

    @FXML
    private Button itemButton;
    @FXML
    private Button normalButton;
    @FXML
    private Button backButton;
    boolean itemMode;

    @FXML
    private void initialize() {
        // 라디오 버튼을 그룹으로 묶기
        toggleGroup = new ToggleGroup();
        easyRadioButton.setToggleGroup(toggleGroup);
        normalRadioButton.setToggleGroup(toggleGroup);
        hardRadioButton.setToggleGroup(toggleGroup);
    }

    @FXML
    private void startItemMode(ActionEvent event) throws IOException {
        itemMode = true;
        startGame(event);// 아이템 모드는 여기서 설정
    }
    @FXML
    private void startNormalMode(ActionEvent event) throws IOException {
        itemMode = false;
        startGame(event);
        // 아이템 모드는 여기서 설정
    }

    private void startGame(ActionEvent event) throws IOException {
        int difficulty;
        if (easyRadioButton.isSelected()) {
            difficulty = 0;
        } else if (normalRadioButton.isSelected()) {
            difficulty = 1;
        } else {
            difficulty = 2;
        }

        // 난이도와 아이템 모드 설정
        GameBoardController.setOptions(difficulty, itemMode);
        // 현재 스테이지(창) 닫기
        gameMode();
    }
    @FXML
    private void gameMode() throws IOException {
        System.out.println("스코어보드 버튼이 클릭되었습니다. 스코어보드를 보여줍니다.");
        // 여기에 스코어보드 화면을 보여주는 로직 추가
        Stage stage = (Stage) itemButton.getScene().getWindow();

        SettingModel.init();

        int width = SettingModel.getWidth();
        int height = SettingModel.getHeight();


        FXMLLoader loader = new FXMLLoader(getClass().getResource("GameBoard.fxml"));
        Parent root = loader.load();
        Scene scene = itemButton.getScene(); // 현재 Scene을 가져옵니다.
        scene.setRoot(root); // 현재 Scene의 root를 새로운 root로 설정합니다.
        stage.setTitle("Score Page");
        stage.setWidth(width); // 현재 Stage의 너비를 설정합니다.
        stage.setHeight(height); // 현재 Stage의 높이를 설정합니다.
        stage.show();
    }
    @FXML
    private void goBack(ActionEvent event) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();

        SettingModel.init();

        int width = SettingModel.getWidth();
        int height = SettingModel.getHeight();


        FXMLLoader loader = new FXMLLoader(getClass().getResource("Start.fxml"));
        Parent root = loader.load();
        Scene scene = backButton.getScene(); // 현재 Scene을 가져옵니다.
        scene.setRoot(root); // 현재 Scene의 root를 새로운 root로 설정합니다.
        stage.setTitle("Start Page");
        stage.setWidth(width); // 현재 Stage의 너비를 설정합니다.
        stage.setHeight(height); // 현재 Stage의 높이를 설정합니다.
        stage.show();
    }
}
