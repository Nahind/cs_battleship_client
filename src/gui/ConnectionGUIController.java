package gui;

import app.ConnectionHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Configuration;

import java.io.IOException;

public class ConnectionGUIController {

    @FXML private Text error;
    @FXML private TextField user_name;
    @FXML private Button start;
    @FXML private TextField host;
    @FXML private TextField port;

    private final int SCENE_HEIGHT = 650;
    private final int SCENE_WIDTH  = 650;
    private Configuration config;

    @FXML protected void handleSubmitButtonAction(ActionEvent event)
            throws IOException {

        if (user_name.getText().isEmpty()) {
            error.setText("Enter a valid user name");
        } else if(host.getText().isEmpty()) {
            error.setText("Enter a valid host address");
        } else if(port.getText().isEmpty()) {
            error.setText("Enter a valid port number");
        }
        else {
            Stage stage;
            Parent gameRoot;
            stage = (Stage) start.getScene().getWindow();

            Scene gameScene = initGameGUIScene();

            stage.setScene(gameScene);
            stage.show();
        }
    }

    private Scene initGameGUIScene() throws IOException  {
        String playerName = user_name.getText();
        config = Configuration.getInstance();

        config.setHost(host.getText());
        config.setPort(Integer.parseInt(port.getText()));
        FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("gameGUI.fxml"));
        Parent gameRoot = (Parent)gameLoader.load();

        // Attach the connection helper to the controller
        // Give reference of the log container to the connection helper
        GameGUIController gameGUIController = gameLoader.getController();
        ConnectionHelper connectionHelper = ConnectionHelper.getInstance();
        connectionHelper.setGUIController(gameGUIController);

        // Connect to the server
        connectionHelper.connect(config.getHost(), config.getPort());
        connectionHelper.send(config.getHost(), config.getPort(), "connect " + playerName);

        //create a new scene with root and set the stage
        Scene gameScene = new Scene(gameRoot, SCENE_WIDTH, SCENE_HEIGHT);
        String css = this.getClass().getResource("/assets/battleship.css").toExternalForm();
        gameScene.getStylesheets().add(css);

        return gameScene;
    }




}
