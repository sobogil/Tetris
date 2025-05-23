package com.example.fxtest;

import com.example.fxtest.brick.Block;
import com.example.fxtest.brick.Brick;
import com.example.fxtest.brick.Item;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

import static com.example.fxtest.Drawing.displayNextBrick;


//시간이 좀 많이 지나고 브릭 스폰 위치가 이미 쌓여있는 board 블록과 겹치면? >> Board 늘려서 스폰 위치 따로 빼거나 스폰 자체를 바꿔야될듯
public class GameBoard2Controller implements Initializable {

    Brick currentBrick;
    Brick nextBrick;
    Brick currentBrick2;
    Brick nextBrick2;

    BrickController brickController;
    BrickController brickController2;

    GameBoard1 gameBoard = new GameBoard1();
    GameBoard1 gameBoard2 = new GameBoard1();

    Timeline timeline;
    Timeline timeline2;
    Timeline gameTimer;
    Timeline updateTimer;

    RandomGenerator rg = new RandomGenerator();


    @FXML
    public GridPane boardView; //컨트롤View 매핑
    @FXML
    public GridPane boardView2; //컨트롤View 매핑

    @FXML
    private Label scoreLabel;
    @FXML
    private Label scoreLabel2;
    @FXML
    private Label timeLabel;
    @FXML
    private GridPane nextBrickView;
    @FXML
    private GridPane nextBrickView2;
    @FXML
    private GridPane attackBoardView;
    @FXML
    private GridPane attackBoardView2;


    public static double cellWidth = 20;
    public static double cellHeight = 20;
    private int remainingTime=60;


    //SettingModel에서 받아와야 되는 것들
    boolean colorBlindness=true; //색맹모드

    //난이도, 아이템 모드 확인
    public static int difficulty; //난이도

    public static boolean itemMode; //이거 setter로 받아야됨

    public static boolean timeMode;//시간 제한 모드 확인

    //타임라인 그 시간으로 시작
    void startTimeLine(double x,int player, Timeline timeline){
        timeline.stop();
        System.out.println("startTimeLine실행, double x : " + x);
        setTime(x,player,timeline);
        timeline.setCycleCount(Timeline.INDEFINITE); // 무한 반복 설정
        timeline.play(); // Timeline 시작
    }



    //타임라인 시간 설정 메서드
    void setTime(double x, int player, Timeline timeline) {
        if(timeline==null){
            if(player==1){
                this.timeline = new Timeline(
                        new KeyFrame(Duration.seconds(x), event -> {

                            minute10(); //x초만큼의 속도
                        }));
            }
            else{
                this.timeline2 = new Timeline(
                        new KeyFrame(Duration.seconds(x), event -> {

                            minute10_2(); //x초만큼의 속도
                        }));
            }
        }
        else{
            timeline.getKeyFrames().clear(); // 기존 키프레임 삭제
            if (player == 1) {
                this.timeline = timeline;
                this.timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(x), event -> {
                    minute10(); // x초만큼의 속도
                }));
            } else {
                this.timeline2 = timeline;
                this.timeline2.getKeyFrames().add(new KeyFrame(Duration.seconds(x), event -> {
                    minute10_2(); // x초만큼의 속도
                }));
            }
        }

    }


    //더 이상 못내려갈때 Brick 행렬에 고정 , 노말 블록이면 1 , 아이템 블록이면 그 아이템 숫자 고정
    public void fixed(GameBoard1 gameBoard,Brick currentBrick){
        System.out.println(" _______________currentBrick" + currentBrick);
        if(currentBrick==null){
            return;
        }
        if(currentBrick.getBlockList()==null){
            System.out.println("getBlockList가 null임");
        }
        else{
            System.out.println("null 아님");
        }
        for(Block block : currentBrick.getBlockList())
            gameBoard.board[block.getX()][block.getY()]=block.getItem().getNum();
    }



    //뷰 바꿔서 겜 시작이 아닌 그 뷰에서 그대로 게임 (재)시작때 초기화
    void init(){
        gameBoard=new GameBoard1();
        gameBoard2=new GameBoard1();

        //currentBrick=rg.genarateNormal(0,colorBlindness,gameBoard); //일단 이지로, 여기서 모드 받아와야됨.
        //currentBrick2=rg.genarateNormal(0,colorBlindness, gameBoard2);
        nextBrick=rg.genarateNormal(difficulty,colorBlindness, gameBoard);
        nextBrick2=rg.genarateNormal(difficulty,colorBlindness, gameBoard2);

        colorBlindness=getColorBliness();
        System.out.println("초기화완료");

    }

    void destroy(){
        timeline.stop();
        timeline2.stop();
        boardView.setOnKeyPressed(null);
        boardView2.setOnKeyPressed(null);
        if (boardView.getScene() == null || boardView2.getScene() == null) {
            // Scene이 아직 설정되지 않은 경우
            return;
        }
        Platform.runLater(() -> {
            Optional<ButtonType> result;
            ButtonType homeButtonType = new ButtonType("홈으로");
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("게임 종료");

            if (gameBoard.gameOver ) {
                alert.setHeaderText("PLAYER 2 WIN");
            } else if (gameBoard2.gameOver) {
                alert.setHeaderText("PLAYER 1 WIN");
            } else if(gameBoard.getScore()<gameBoard2.getScore()){
                alert.setHeaderText("PLAYER 2 WIN");
            }else if(gameBoard.getScore()>gameBoard2.getScore()){
                alert.setHeaderText("PLAYER 1 WIN");
            }else{
                alert.setHeaderText("DRAW");
            }

            alert.getButtonTypes().setAll(homeButtonType);

            result = alert.showAndWait();

            if (result.isPresent() && result.get().equals(homeButtonType)) {
                try {
                    Stage stage = (Stage) boardView.getScene().getWindow();
                    SettingModel.init();

                    int width = SettingModel.getWidth();
                    int height = SettingModel.getHeight();

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Start.fxml"));
                    Parent root = loader.load();
                    Scene scene = boardView.getScene();
                    scene.setRoot(root);
                    stage.setTitle("Start Page");
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace(); // 예외 처리
                }
            }
        });
        System.out.println("초기화완료");
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        boardView.setFocusTraversable(true);
        boardView2.setFocusTraversable(true);
        //difficulty, 색맹여부 받아오는 ,
        colorBlindness=getColorBliness();



        init();


        currentBrick=rg.genarateNormal(difficulty,colorBlindness,gameBoard); //일단 이지로, 여기서 모드 받아와야됨.
        //currentBrick= new BrickI(0,4,Color.GREEN );
        //currentBrick = new BrickO(0,4,Color.SKYBLUE);
        currentBrick2=rg.genarateNormal(difficulty,colorBlindness, gameBoard2);

        nextBrick=rg.genarateNormal(difficulty,colorBlindness, gameBoard);
        //nextBrick=new BrickI(0,4,Color.GREEN );
        //nextBrick = new BrickO(0,4,Color.SKYBLUE);
        nextBrick2=rg.genarateNormal(difficulty,colorBlindness, gameBoard2);

        brickController = new BrickController(SettingModel.getRotate1(),SettingModel.getMoveL1(), SettingModel.getMoveR1(),SettingModel.getMoveD1(),SettingModel.getHardDrop1()); //키 값 전부 field에 세팅
        brickController2 = new BrickController(SettingModel.getRotate2(),SettingModel.getMoveL2(), SettingModel.getMoveR2(),SettingModel.getMoveD2(),SettingModel.getHardDrop2());
        // GridPane에 키 이벤트 핸들러 등록

        //보드2도 등록해야됨

        Platform.runLater(() -> {
            Scene scene = boardView.getScene();
            if (scene != null) {
                setupKeyListener(scene);
            } else {
                boardView.sceneProperty().addListener((obs, oldScene, newScene) -> setupKeyListener(newScene));
            }
        });


        gameBoard.scoreProperty().addListener((obs, oldScore, newScore) -> {
            if (newScore.intValue() > oldScore.intValue()) {
                updateScoreLabel(scoreLabel,gameBoard);
            }
        });

        gameBoard2.scoreProperty().addListener((obs, oldScore, newScore) -> {
            if (newScore.intValue() > oldScore.intValue()) {
                updateScoreLabel(scoreLabel2,gameBoard2);
            }
        });

        //여기서 CellWidth 바꾸기
        //change()함수 실행
        try {
            change(boardView);
            change(boardView2);
            attackchange(attackBoardView);
            attackchange(attackBoardView2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        
        displayNextBrick(nextBrick,nextBrickView,cellWidth);
        displayNextBrick(nextBrick2,nextBrickView2,cellWidth);



        setTime(1.0,1,timeline);
        setTime(1.0,2,timeline2);
        timeline.setCycleCount(Timeline.INDEFINITE); // 무한 반복 설정
        timeline.play(); // Timeline 시작
        timeline2.setCycleCount(Timeline.INDEFINITE); // 무한 반복 설정
        timeline2.play(); // Timeline 시작
        if(timeMode) {
            gameTimer = new Timeline(new KeyFrame(Duration.seconds(60), event -> destroy()));
            gameTimer.setCycleCount(1); // 단 한 번 실행
            gameTimer.play(); // 타임라인 시작
            startTimer();
        }else {
            timeLabel.setVisible(false);
        }
    }

    private void startTimer() {
        remainingTime = 60; // 남은 시간을 초기화
        timeLabel.setText("Time: " + remainingTime); // 초기 시간 표시

        updateTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (remainingTime > 0) {
                remainingTime--; // 1초 감소
                timeLabel.setText("Time: " + remainingTime); // 시간을 표시할 라벨 업데이트
            }
        }));
        updateTimer.setCycleCount(Timeline.INDEFINITE); // 무한 반복
        updateTimer.play();
    }

    //Score 컨트롤 혹은 deleteLine 컨트롤이 일정 int값 이상이면 속도 빨라짐(1초 > 0.8초 > 0.5초). 속성감시 이벤트 리스너
    //이 이벤트 리스너는 값에 맞게 현재 호출되는 정기 실행 함수를 멈추고 매 0.8 혹은 0.5마다 호출되는 함수를 여기서 호출한다.

    private void setupKeyListener(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (!gameBoard.whileGame || currentBrick == null) {
                // 게임이 시작되지 않았거나 currentBrick이 초기화되지 않은 경우 입력 무시
                event.consume();
                return;
            }
            if (isLeftPlayerControl(event)) {
                regiBrickEvent(brickController,currentBrick,boardView,gameBoard,nextBrickView,timeline,1,event);
            }
            else if (isRightPlayerControl(event)){
                regiBrickEvent(brickController2,currentBrick2,boardView2,gameBoard2,nextBrickView2,timeline2,2,event);
            }
            event.consume(); // Ensure the event is not propagated further unnecessarily
        });
    }

    private boolean isLeftPlayerControl(KeyEvent event) {
        for (int i =0; i<5; i++){
            if(event.getCode() ==  getKeyCodeFromString(SettingModel.keyVal.get(i)) || event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.BACK_SPACE){
                return true;
            }
        }
        return false;
    }

    private boolean isRightPlayerControl(KeyEvent event) {
        for (int i =5; i<10; i++){
            if(event.getCode() ==  getKeyCodeFromString(SettingModel.keyVal.get(i)) || event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.BACK_SPACE){
                return true;
            }
        }
        return false;
    }

    private KeyCode getKeyCodeFromString(String keyString) {
        try {
            Field field = KeyCode.class.getField(keyString);
            return (KeyCode) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }


    //어느 한계 선 이상(1번째 행에 1이 하나라도 들어오면) 이 되면 끝인 리스너, GameOverFlag=true로



    //매 1초마다 호출되는 함수
    //더 이상 내려갈 수 없는지 확인 + 없다면 거기 위치에 Brick 박고 nextBrick을 currentBrick으로 넘기고 nextBrick 랜덤으로 뽑아오고
    //키 입력 시 위치 변경
    //줄 꽉차면 없애고 그 윗줄 내리고
    //어느 한계 선 이상이 되면 끝인지 매초 확인하고 맞으면 종료
    //아이템은 총 2가지 케이스 >> (1) 떨어지면 바로 작동 (2) 줄 삭제가 되어야 작동
    private void minute10(){ //currentBrick , boardView, attackBoardBiew , gameBoard, nextBrickView, timeLine , gameBoard2 , attackBoardView2
        if(!gameBoard.gameOver) {
            Drawing.colorErase(currentBrick, boardView);
            Drawing.updateBoardView(attackBoardView, gameBoard.attackBoard,cellWidth);
            if (!gameBoard.whileGame) {
                gameBoard.downScore = 1;
                System.out.println("겜 자체 시작");
                //nextBrick을 currentBrick으로 옮김. + 색칠 + 이벤트 장착
                sponBrick(gameBoard, boardView, nextBrickView, 1);

                //게임 중으로 바꿈
                gameBoard.whileGame = true;


                //테스트
                //printMatrix();

            } else {
                if (!currentBrick.canMoveDown()/*!canMoveDown()*/) { //더 못내려가면
                    System.out.println("!currentBrick.canMoveDown()");
                    //그 위치에 색칠
                    //colorFill();

                    gameBoard.turnEnd = true;
                    nextBrickView.setVisible(true);


                    Drawing.colorFill(currentBrick, boardView,cellWidth);
                    System.out.println("*************Current 색칠 colorFill************");
                    fixed(gameBoard, currentBrick);
                    System.out.println("*************Current 보드 fixed************");

                    //착지시(아이템) , 살포시 안착했을때
                    Item.turnEndDoItem(currentBrick, gameBoard, boardView); //아이템

                    System.out.println("*************보드1************");
                    printMatrix(gameBoard);


                    //먼저 삭제되는 로우 가져와서 거기에 아이템 있는지 확인(아이템)
                    List<Integer> removedRows = gameBoard.getRemovedRows(); //삭제 전에 우선 삭제되는 라인 먼저 확인
                    for (int line : removedRows) {
                        System.out.printf("줄삭제 : " + line + " ");
                    }
                    System.out.println();

                    gameBoard.attackLine(removedRows, currentBrick, gameBoard2);
                    //보드 전부 0
                    checkAndDoItem6(removedRows, gameBoard, boardView);


                    Drawing.updateBoardView(removedRows, boardView, gameBoard.board); //gui 여기서 삭제
                    Drawing.updateBoardView(attackBoardView2, gameBoard2.attackBoard,cellWidth);
                    System.out.println("*************GUI 업뎃************");


                    gameBoard.removeFullRows(); //배열에서 삭제 후 점수 업뎃
                    //NPE조심
                    Drawing.animeRow(removedRows, boardView,cellWidth);
                    System.out.println("줄 삭제 이후");
                    System.out.println("************삭제된 줄 ***********************"+gameBoard.deleteLine);
                    printMatrix(gameBoard);



                    Item.sponDoItem(currentBrick, gameBoard, nextBrickView);

                    gameBoard.turnEnd = false;
                    //nextBrick을 currentBrick으로 옮김. + 색칠 + 이벤트 장착
                    System.out.println(currentBrick + "== 스폰 전 Current Brick");
                    gameBoard.attackStart();
                    Drawing.attackUpdateBoardView(gameBoard.myAttackBoardToList(), boardView,cellWidth);
                    gameBoard.attackBoardClean();
                    Drawing.updateBoardView(attackBoardView, gameBoard.attackBoard,cellWidth);
                    sponBrick(gameBoard, boardView, nextBrickView, 1);
                    System.out.println("*************Block 새로 스폰************" + currentBrick + "== 스폰 후 CurrentBrick");
                    chageTime(gameBoard,1,timeline);

                    System.out.println("겜은 안끝났지만 내려갈 곳 없어서 블록 스폰" + gameBoard.blockSpon);


                } else { //내려갈수 있으면
                    //지우고 moveD() 호출하고 색칠하기
                    Drawing.colorErase(currentBrick, boardView);
                    //System.out.println("---------------");
                    currentBrick.moveD();
                    //printBlock(currentBrick);
                    Drawing.colorFill(currentBrick, boardView,cellWidth);

                    //테스트
                    //printMatrix();
                }
                if (gameBoard.turnEnd == true) {
                    Item.turnEndDoItem(currentBrick, gameBoard, boardView); //아이템 , 하드드롭했을때
                    gameBoard.turnEnd = false;

                }
            }
        }
    }

        private void minute10_2(){
        if(!gameBoard2.gameOver) {
            Drawing.colorErase(currentBrick2, boardView2);
            Drawing.updateBoardView(attackBoardView2, gameBoard2.attackBoard,cellWidth);

            if (!gameBoard2.whileGame) {
                gameBoard2.downScore = 1;
                System.out.println("겜 자체 시작");
                //nextBrick을 currentBrick으로 옮김. + 색칠 + 이벤트 장착
                sponBrick(gameBoard2, boardView2, nextBrickView2, 2);

                //게임 중으로 바꿈
                gameBoard2.whileGame = true;


                //테스트
                //printMatrix();

            } else {
                if (!currentBrick2.canMoveDown()/*!canMoveDown()*/) { //더 못내려가면
                    //그 위치에 색칠
                    //colorFill();

                    gameBoard2.turnEnd = true;
                    nextBrickView2.setVisible(true);


                    Drawing.colorFill(currentBrick2, boardView2,cellWidth);
                    fixed(gameBoard2, currentBrick2);
                    //아이템 기능을 빼고 아무슨아이템이냐 받고 호출
                    //Block Item
                    System.out.println("!currentBrick.canMoveDown()");

                    //착지시(아이템) , 살포시 안착했을때
                    Item.turnEndDoItem(currentBrick2, gameBoard2, boardView2); //아이템

                    System.out.println("*********************보드2**********************");
                    printMatrix(gameBoard2);

                    //먼저 삭제되는 로우 가져와서 거기에 아이템 있는지 확인(아이템)
                    List<Integer> removedRows = gameBoard2.getRemovedRows(); //삭제 전에 우선 삭제되는 라인 먼저 확인
                    gameBoard2.attackLine(removedRows, currentBrick2, gameBoard);


                    //보드 전부 0
                    checkAndDoItem6(removedRows, gameBoard2, boardView2);

                    //NPE조심
                    Drawing.updateBoardView(removedRows, boardView2, gameBoard2.board); //gui 여기서 삭제
                    Drawing.updateBoardView(attackBoardView, gameBoard.attackBoard,cellWidth);
                    gameBoard2.removeFullRows(); //배열에서 삭제 후 점수 업뎃
                    Drawing.animeRow(removedRows, boardView2,cellWidth);

                    Item.sponDoItem(currentBrick2, gameBoard2, nextBrickView2);

                    gameBoard2.turnEnd = false;
                    //nextBrick을 currentBrick으로 옮김. + 색칠 + 이벤트 장착
                    gameBoard2.attackStart();
                    Drawing.attackUpdateBoardView(gameBoard2.myAttackBoardToList(), boardView2,cellWidth);
                    gameBoard2.attackBoardClean();
                    Drawing.updateBoardView(attackBoardView2, gameBoard2.attackBoard,cellWidth);
                    sponBrick(gameBoard2, boardView2, nextBrickView2, 2);
                    chageTime(gameBoard2,2,timeline2);
                } else {
                    //지우고 moveD() 호출하고 색칠하기
                    Drawing.colorErase(currentBrick2, boardView2);
                    //System.out.println("겜은 안끝났고 내려갈 곳도 있고");
                    currentBrick2.moveD();
                    Drawing.colorFill(currentBrick2, boardView2,cellWidth);

                }
                if (gameBoard2.turnEnd == true) {

                    Item.turnEndDoItem(currentBrick2, gameBoard2, boardView2); //아이템 , 하드드롭했을때
                    gameBoard2.turnEnd = false;


                }
            }
        }
    }

    private void chageTime(GameBoard1 gameBoard,int player,Timeline timeline) {
        if((gameBoard.deleteLine>=10 && gameBoard.deleteLine!=0) || gameBoard.blockSpon%20 ==0) {
            if(gameBoard.blockSpon%20==0){
                gameBoard.blockSpon=0;
            }
            else{
                gameBoard.deleteLine=0;
            }
            gameBoard.downScore++;
            System.out.println("changeTime 실행");
            startTimeLine(changeSpeed(difficulty, gameBoard), player,timeline);
        }
    }

    public double changeSpeed(int difficulty, GameBoard1 gameBoard){
        if(gameBoard.speed<=0.1){
            return 0.1;
        }
        if(difficulty==0){ //이지
            gameBoard.speed=gameBoard.speed-0.08;
            System.out.println("difficulty = " + difficulty);
            System.out.println("gameBoard.speed = " + gameBoard.speed);
            return gameBoard.speed;
        }
        else if(difficulty==1){ //노말
            gameBoard.speed= gameBoard.speed-0.1;
            System.out.println("difficulty = " + difficulty);
            System.out.println("gameBoard.speed = " + gameBoard.speed);
            return gameBoard.speed;
        }
        else{ //하드
            gameBoard.speed= gameBoard.speed-0.12;
            System.out.println("difficulty = " + difficulty);
            System.out.println("gameBoard.speed = " + gameBoard.speed);
            return gameBoard.speed;
        }
    }


    //아이템6 실행
    private void checkAndDoItem6(List<Integer> removedRows,GameBoard1 gameBoard,GridPane boardView) {
        boolean flag=false;
        for(int fullRow : removedRows){
            for(int i = 0; i< GameBoard1.WIDTH; i++){
                if(gameBoard.board[fullRow][i]==6){
                    flag=true;
                }
            }
            if(flag==true){
                break;
            }
        }
        if(flag==true){
            for (int[] row : gameBoard.board) {
                Arrays.fill(row, 0);
            }
            Drawing.updateBoardView(boardView, gameBoard.board,cellWidth);
            Drawing.animeNuclear(boardView,cellWidth);
        }
    }

    private void sponBrick(GameBoard1 gameBoard , GridPane gridPane, GridPane nextBrickView,int n) { //n이 1이면 currentBrick 1에, n이 2이면 currentBrick 2에
        //nextBrick을 currentBrick으로 옮김.
        if(n==1) {
            currentBrick = nextBrick;
        }
        else {
            currentBrick2 = nextBrick2;
        }
        //nextBrick 랜덤 뽑아와서 세팅
        if(n==1) {
            if (gameBoard.deleteLine >= 10  && gameBoard.deleteLine != 0 && itemMode == true) {
                nextBrick = rg.generateItem(difficulty, colorBlindness, gameBoard);
                //gameBoard.deleteLine = 0;
                gameBoard.blockSpon++;
            } else {
                nextBrick = rg.genarateNormal(difficulty, colorBlindness, gameBoard);
                //nextBrick=rg.generateItem(difficulty,colorBlindness,gameBoard);
                gameBoard.blockSpon++;
            }
        }

        if(n==2) {
            if (gameBoard.deleteLine >= 10  && gameBoard.deleteLine != 0 && itemMode == true) {
                nextBrick2 = rg.generateItem(difficulty, colorBlindness, gameBoard);
                //gameBoard.deleteLine = 0;
                gameBoard.blockSpon++;
            } else {
                nextBrick2 = rg.genarateNormal(difficulty, colorBlindness, gameBoard);
                //nextBrick2 =rg.generateItem(difficulty,colorBlindness,gameBoard);
                gameBoard.blockSpon++;
            }

        }

        //nextBrick=new BrickZ(0,4,Color.GREEN );
        //nextBrick = new BrickO(0,4,Color.SKYBLUE);

        if(n==1) {
            //currentBrick 색칠하고
            Drawing.colorFill(currentBrick, gridPane,cellWidth);
            displayNextBrick(nextBrick, nextBrickView,cellWidth);
            //이벤트 장착
            if (isBrickColliding(currentBrick, gameBoard)) {
                // If there's a collision, handle game over logic here.
                fixed(gameBoard, currentBrick);
                System.out.println("Game over due to overlapping spawn.");
                destroy(); // This method should effectively end the game and clean up.
                return; // Return early to avoid further processing.
            }
        }
        else{
            Drawing.colorFill(currentBrick2, gridPane,cellWidth);
            displayNextBrick(nextBrick2, nextBrickView,cellWidth);
            if (isBrickColliding(currentBrick2, gameBoard)) {
                // If there's a collision, handle game over logic here.
                fixed(gameBoard, currentBrick2);
                System.out.println("Game over due to overlapping spawn.");
                destroy(); // This method should effectively end the game and clean up.
                return; // Return early to avoid further processing.
            }
        }


    }



    private boolean isBrickColliding(Brick brick, GameBoard1 gameBoard) {
        for (Block block : brick.getBlockList()) {
            int x = block.getX();
            int y = block.getY();
            if (gameBoard.board[x][y] != 0) {
                gameBoard.gameOver=true;
                gameBoard.whileGame=true;
                // 블록이 보드의 셀과 겹칠 경우 충돌로 판단
                return true;
            }
        }
        return false; // 충돌하지 않음
    }

    //2차원배열 출력 테스트함수
    public void printMatrix(GameBoard1 gameBoard){
        for(int i = 0; i< GameBoard1.HEIGHT; i++){
            for(int j = 0; j< GameBoard1.WIDTH; j++){
                System.out.printf("%d ", gameBoard.board[i][j]);
            }
            System.out.println();
        }
        System.out.println("--------END-------");
    }


    //현재 브릭 x , y 모든 좌표 출력
    public void printBlock(Brick currentBrick){
        for(Block block : currentBrick.getBlockList()){
            System.out.println("x값: " + block.getX() + " y 값: "+block.getY());
        }
    }





    //무게추 같은 경우에는 로테이트 함수 구현은 해놔야됨. 바디는 냅두고
    private void regiBrickEvent(BrickController brickController, Brick currentBrick,GridPane boardView,GameBoard1 gameBoard,GridPane nextBrickView, Timeline timeline,int player, KeyEvent event) {
            Drawing.colorErase(currentBrick,boardView);
            String keyValue = event.getCode().toString();
            if (event.getCode() == KeyCode.ESCAPE) {
                gameBoard.pause = !gameBoard.pause;
                if(gameBoard.pause) {
                    boardView.setOpacity(0);
                    nextBrickView.setOpacity(0);
                    timeline.stop();
                    if (timeMode){
                        gameTimer.pause();
                        updateTimer.pause();
                    }
                } else {
                    boardView.setOpacity(1);
                    nextBrickView.setOpacity(1);
                    timeline.play();
                    if (timeMode){
                        gameTimer.play();
                        updateTimer.play();
                    }
                }
            } else if (event.getCode() == KeyCode.BACK_SPACE) {
                // 백스페이스 키가 눌렸을 때의 동작 (게임 종료)
                Stage stage = (Stage) boardView.getScene().getWindow();
                timeline.stop(); // 타임라인 애니메이션을 정지합니다.
                stage.close(); // 현재 스테이지를 닫습니다.
            }
            if (!gameBoard.pause) {
                if (keyValue.equals(brickController.getMOVER()) || keyValue.toLowerCase().equals(brickController.getMOVER())) {
                    // 오른쪽 이동 키가 눌렸을 때의 동작
                    System.out.println("Right key pressed");
                    brickController.moveR(currentBrick);
                    printBlock(currentBrick);
                    Drawing.colorFill(currentBrick,boardView,cellWidth);
                } else if (keyValue.equals(brickController.getMOVEL()) || keyValue.toLowerCase().equals(brickController.getMOVEL())) {
                    // 왼쪽 이동 키가 눌렸을 때의 동작
                    System.out.println("Left key pressed");
                    brickController.moveL(currentBrick);
                    printBlock(currentBrick);
                    Drawing.colorFill(currentBrick,boardView,cellWidth);
                } else if (keyValue.equals(brickController.getMOVED()) || keyValue.toLowerCase().equals(brickController.getMOVED())) {
                    // 아래 이동 키가 눌렸을 때의 동작
                    brickController.moveD(currentBrick);
                    printBlock(currentBrick);
                    Drawing.removeEmptyCells(boardView, gameBoard.board);
                    Drawing.colorFill(currentBrick,boardView,cellWidth);
                } else if (keyValue.equals(brickController.getROTATE()) || keyValue.toLowerCase().equals(brickController.getROTATE())) {
                    // 회전 키가 눌렸을 때의 동작
                    System.out.println("Rotate key pressed");
                    brickController.rotate(currentBrick);
                    printBlock(currentBrick);
                    Drawing.colorFill(currentBrick,boardView,cellWidth);
                } else if(keyValue.equals(brickController.getSTRAIGHT()) || keyValue.toLowerCase().equals(brickController.getSTRAIGHT())) {
                    //여기는 수직떨구기
                    System.out.println("---------------------------------수직 떨구기 누름");
                    brickController.straightD(currentBrick);
                    if (isHardDropGameOver(currentBrick)) {
                        Drawing.colorFill(currentBrick,boardView,cellWidth);
                        fixed(gameBoard,currentBrick);
                        System.out.println("수직떨구기");
                        destroy();
                    } else {
                        //수직 떨구고 timeline을 간격 없이 바로 새로 시작해야돼서
                        timeline.stop();
                        System.out.println("---------------------------------정지");
                        gameBoard.turnEnd = true;
                        //시작부터 바로 Space바
                        gameBoard.whileGame=true;
                        Drawing.colorFill(currentBrick, boardView,cellWidth);
                        //떨구고 바로 블록 뽑아옴
                        if(player==1) {
                            Drawing.removeEmptyCells(boardView, gameBoard.board);
                            minute10();
                        }
                        else {
                            Drawing.removeEmptyCells(boardView2, gameBoard2.board);
                            minute10_2();

                        }
                        timeline.play();
                        if(gameBoard.gameOver){
                            Drawing.colorFill(currentBrick, boardView,cellWidth);
                        }
                        System.out.println("---------------------------------재게");
                        System.out.println("수직떨구기");
                    }
                }
                event.consume();
                if (gameBoard.whileGame == true) {
                    //Drawing.colorFill(currentBrick,boardView);
                }//색칠하고
            }

    }


    //보드 해상도 change함수
    public void change(GridPane boardView) throws IOException {
        // 해상도에 따라 칸의 크기를 동적으로 조정
        SettingModel.init();

        int width = SettingModel.getWidth();
        int height = SettingModel.getHeight();

        int numRows = 20; // 행의 수
        int numCols = 10; // 열의 수

        //해상도 바꾸고 싶으면 여기를 바꾼다.
        cellWidth = height / 30;
        cellHeight = height / 30;

        boardView.getColumnConstraints().clear();
        boardView.getRowConstraints().clear();

        for (int i = 0; i < numCols; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setMinWidth(cellWidth);
            colConstraints.setPrefWidth(cellWidth);
            colConstraints.setMaxWidth(cellWidth);
            boardView.getColumnConstraints().add(colConstraints);
        }

        for (int i = 0; i < numRows; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(cellHeight);
            rowConstraints.setPrefHeight(cellHeight);
            rowConstraints.setMaxHeight(cellHeight);
            boardView.getRowConstraints().add(rowConstraints);
        }
    }

    public void attackchange(GridPane boardView) throws IOException {
        // 해상도에 따라 칸의 크기를 동적으로 조정
        SettingModel.init();

        int width = SettingModel.getWidth();
        int height = SettingModel.getHeight();

        int numRows = 10; // 행의 수
        int numCols = 10; // 열의 수

        //해상도 바꾸고 싶으면 여기를 바꾼다.
        cellWidth = height / 30;
        cellHeight = height / 30;
        double val = cellWidth/2;
        boardView.getColumnConstraints().clear();
        boardView.getRowConstraints().clear();

        for (int i = 0; i < numCols; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setMinWidth(val);
            colConstraints.setPrefWidth(val);
            colConstraints.setMaxWidth(val);
            boardView.getColumnConstraints().add(colConstraints);
        }

        for (int i = 0; i < numRows; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(val);
            rowConstraints.setPrefHeight(val);
            rowConstraints.setMaxHeight(val);
            boardView.getRowConstraints().add(rowConstraints);
        }
    }

    public void updateScoreLabel(Label scoreLabel,GameBoard1 gameBoard) {
        scoreLabel.setText("Score: " +Integer.toString(gameBoard.getScore()));
    }

    //hardDrop 시에 게임오버 판단
    public boolean isHardDropGameOver(Brick currentBrick) {
        boolean flag = false;
        List<Block> blockList = currentBrick.getBlockList();
        for (Block block : blockList) {
            if (block.getX() < 1) {
                flag = true;
            }
        }
        return flag;
    }



    //Difficulty2Controller에서 setOption으로 호출해서 ItemMode , difficulty 넘겨줘야함.
    public static void setOptions(int difficulty, boolean timeMode, boolean itemMode) {
        GameBoard2Controller.difficulty = difficulty;
        GameBoard2Controller.timeMode = timeMode;
        GameBoard2Controller.itemMode = itemMode;
        // 여기서부터 게임을 시작할 수 있음
        System.out.println("난이도" + difficulty);
        System.out.println("시간제한모드" + timeMode);
        System.out.println("아이템모드" + itemMode);
    }





    //프로퍼티에서 색맹모드 여부 가져오기
    public boolean getColorBliness(){
        //setting.properties에서 값 가져와서 MOVE에 넣기
        // Properties 객체 생성
        /*Properties settingProperties = new Properties();
        try {
            // setting.properties 파일 로드
            FileInputStream in = new FileInputStream("src/main/resources/setting.properties");
            settingProperties.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String temp = settingProperties.getProperty("colorBlindness");*/
        int num = SettingModel.getColorBlindnessVal();
        if(num==0){
            return false;
        }
        else{
            return true;
        }
    }
}

