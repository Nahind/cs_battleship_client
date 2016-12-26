package app;

import gui.ConnectionGUIController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.*;

import java.util.ArrayList;

public class SocketClientApplication extends javafx.application.Application {

    private final int SCENE_HEIGHT = 650;
    private final int SCENE_WIDTH  = 650;

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader connectionLoader = new FXMLLoader(getClass().getResource("/gui/connectionGUI.fxml"));
        Parent connectionRoot = (Parent)connectionLoader.load();
        ConnectionGUIController connectionGUIController = connectionLoader.getController();

        Scene connectionScene = new Scene(connectionRoot, SCENE_WIDTH, SCENE_HEIGHT);

        primaryStage.setTitle("Battleship 101");
        primaryStage.setScene(connectionScene);
        primaryStage.show();
    }


    public static void main(String[] args) {

        launch(args);
    }

    /*private static void printDamier(Damier d) {
        String empty = "[ ]";
        String missed  = "[-]";
        String boat  = "[0]";
        String hit   = "[X]";
        String damier = "";
        String design;
        ArrayList<Field> fields = d.getFields();

        for (int i = 0; i < fields.size() ; i ++) {
            design = empty;
            Field currentField = fields.get(i);

            if (currentField.isBoat()) {
                design = boat;
            }

            switch (currentField.getState()) {
                case empty:
                    break;
                case missed:
                    design = missed;
                    break;
                case hit:
                    design = hit;
                    break;
            }

            if (i % d.getSize() == 0) {
                damier += "\n" + design;
            } else {
                damier += design;
            }
        }

        System.out.print(damier);
    }*/
}
