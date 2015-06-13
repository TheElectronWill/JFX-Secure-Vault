/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.electronwill.jfxvault;

import com.electronwill.jfxvault.stockage.Stockage;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author TheElectronWill
 */
public class MainClass extends Application {

    public static Stage STAGE;
    public static Stockage STOCKAGE;
    public static Logger log = Logger.getLogger("JPass");

    static {
        log.setUseParentHandlers(false);
    }

    @Override
    public void start(Stage stage) throws Exception {
        STAGE = stage;
        stage.setTitle("JFX Secure Vault");
        stage.setResizable(false);
        Parent promptRoot = FXMLLoader.load(getClass().getResource("ui/PasswordPrompt.fxml"));
        Scene promptScene = new Scene(promptRoot);
        stage.setScene(promptScene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
