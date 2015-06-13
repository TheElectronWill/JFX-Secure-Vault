/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.electronwill.jfxvault.ui;

import com.electronwill.jfxvault.MainClass;
import com.electronwill.jfxvault.stockage.StockageFichier;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javax.crypto.NoSuchPaddingException;

/**
 * FXML Controller class
 *
 * @author TheElectronWill
 */
public class PasswordPromptController implements Initializable {

    @FXML
    public PasswordField pwf;

    @FXML
    public Label incorrect;

    public static Scene MAIN_SCENE;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            MainClass.STOCKAGE = new StockageFichier();
            if (MainClass.STOCKAGE.premiereFois()) {//L'application est lancée pour la première fois
                incorrect.setText("Choisissez un mot de passe");
                incorrect.setVisible(true);
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            Logger.getLogger(PasswordPromptController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void ok(ActionEvent e) {
        if (MainClass.STOCKAGE.premiereFois()) {
            MainClass.STOCKAGE.definirMdp(pwf.getText());
        } else if (!MainClass.STOCKAGE.deverouiller(pwf.getText())) {
            incorrect.setVisible(true);
            return;
        }
        try {
            Parent mainRoot = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
            MAIN_SCENE = new Scene(mainRoot);
            MainClass.STAGE.setScene(MAIN_SCENE);
            MAIN_SCENE.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                MainWindowController.INSTANCE.largeurChange(oldValue.doubleValue(), newValue.doubleValue());
            });
            MAIN_SCENE.heightProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                MainWindowController.INSTANCE.hauteurChange(oldValue.doubleValue(), newValue.doubleValue());
            });
        } catch (Exception ex) {
            Logger.getLogger(PasswordPromptController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
