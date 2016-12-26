package gui;

import api.IConnectionHelper;
import app.ConnectionHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.*;

public class GameGUIController {

    private final Button opponentGameFields[][] = new Button[10][10];
    private final Button playerGameFields[][] = new Button[10][10];
    // GameBoard properties (square dimension)
    private final int SIZE = 10;
    private final int FIELD_SIZE = 30;
    @FXML private GridPane grid_opponent;
    @FXML private GridPane grid_player;
    @FXML private TextArea logContainer;
    @FXML private Text player_name;
    @FXML private Text opponent_name;
    @FXML private Text info;
    @FXML private TextField messageEdit;
    @FXML private VBox vbox;
    @FXML private Text chatDescription;
    //Connection vars
    private IConnectionHelper connectionHelper;
    private Configuration configuration;
    private int port;
    private String host;
    private boolean gameEnded = false;



    @FXML
    public void initialize() {
        buildFields(grid_opponent);
        buildFields(grid_player);
        this.configuration = Configuration.getInstance();
        this.port = configuration.getPort();
        this.host = configuration.getHost();
        this.connectionHelper = ConnectionHelper.getInstance();
        this.vbox.setPrefWidth(FIELD_SIZE*SIZE);
        this.chatDescription.setWrappingWidth(FIELD_SIZE*SIZE);
        this.info.setWrappingWidth(FIELD_SIZE*SIZE);

        // wherever you assign event handlers...
        messageEdit.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
                    String message = messageEdit.getText();

                    System.out.println("mess = " + message);
                    // clear text
                    messageEdit.setText("");
                    // add text to textarea
                    logContainer.appendText(player_name.getText() + " -> " + message +"\n");
                    connectionHelper.send("message ::" + message);
                }
            }
        });
    }

    public TextArea getLogContainer() {
        return logContainer;
    }

    private void buildFields(GridPane grid) {

        for (int col = 0; col < 10; col++) {
            for (int row = 0; row < 10; row++) {
                Button button = new Button();
                button.setText(" ");
                button.getStyleClass().add("field");
                button.setMinSize(FIELD_SIZE, FIELD_SIZE);

                if (grid == grid_opponent) {
                    button.getStyleClass().add("opponent");
                    button.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            Button field = (Button) event.getSource();
                            int fieldSize = (int)field.getBoundsInLocal().getWidth();
                            int x = (int)field.getLayoutX();
                            int y = (int)field.getLayoutY();

                            int col = x/fieldSize;
                            int row = y/fieldSize;

                            if (!gameEnded) {
                                shootOpponent(row, col);
                            }

                            System.out.println("row = " + row + ", col = " + col);
                        }
                    });
                    setField(col, row, button, opponentGameFields);
                } else {
                    button.getStyleClass().add("player");
                    setField(col, row, button, playerGameFields);
                }

                grid.add(button, col, row);
            }
        }
    }

    public void setPlayerName(String name) {
        player_name.setText("You : " + name);
    }

    public void setOpponent(String name) {
        grid_opponent.getStyleClass().add("has-joined");
        opponent_name.setText("Your opponent : "  + name);
    }

    public void inform(String message) {
        this.info.setText(message);
    }

    public void appendMessage(String msg) {
        logContainer.appendText(opponent_name.getText() + " -> " + msg +"\n");
    }

    public void disableFields() {
        gameEnded = true;
    }

    public void deserializeBoats(String boats) {
        String[] cases = boats.split("-");

        for (String c : cases) {
            String[] split = c.split(",");
            int col = Integer.parseInt(split[0]);
            int row = Integer.parseInt(split[1]);
            getField(row, col, playerGameFields).getStyleClass().add("boat");
        }
    }

    private void shootOpponent(int row, int col) {
        String cmd = "shoot " + row + "," + col;
        connectionHelper.send(host, port, cmd);
    }

    public void updateFieldUI(int row, int col, String state, boolean isOpponent) {
        Button[][] playerFields = (isOpponent) ? playerGameFields : opponentGameFields;
        Button field = getField(row, col, playerFields);
        switch (state) {
            case "missed":
                field.getStyleClass().add("missed");
                break;
            case "hit":
                field.getStyleClass().add("hit");
                break;
            }
    }

    public void drawSunkBoat(String orientation, String boatSize, String cases, boolean opponentHit) {
        String[] fields = cases.split("-");
        int size = Integer.parseInt(boatSize);
        int counter = 1;
        Button [][] gamefield = opponentHit ? playerGameFields : opponentGameFields;

        for (String field : fields) {
            String[] split = field.split(",");
            int row = Integer.parseInt(split[0]);
            int col = Integer.parseInt(split[1]);
            System.out.println("row,col = " + row+","+col);
            Button b = getField(row, col, gamefield);
            b.getStyleClass().add("sunk");

            if (orientation.equals("vertical")) {
                b.getStyleClass().add("vertical");
            } else {
                b.getStyleClass().add("horizontal");
            }
            if (counter == 1) {
                b.getStyleClass().add("first");
            } else if (counter == size) {
                b.getStyleClass().add("last");
            }

            counter ++;
        }



        System.out.println("drawboat");
    }

    private void setField(int x, int y, Button obj, Button[][] gameFields) { gameFields[y][x] = obj; }

    private Button getField(int x, int y, Button[][] gameFields)
    {
        return gameFields[x][y];
    }
}
