/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.electronwill.jfxvault.ui;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import com.electronwill.jfxvault.MainClass;
import static com.electronwill.jfxvault.MainClass.STOCKAGE;

/**
 * FXML Controller class
 *
 * @author TheElectronWill
 */
public class MainWindowController implements Initializable {

    public static MainWindowController INSTANCE;

    @FXML
    public ListView list;

    @FXML
    public TextField recherche;

    @FXML
    public TabPane tabs;

    @FXML
    public Button ajouter;

    @FXML
    public Button supprimer;

    private ObservableList<String> oList;
    private FilteredList<String> filtre;

    private ContextMenu menu;
    private String nomChoisit;

    private double realTabsX;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        MainClass.STAGE.setResizable(true);
        String[] notes = STOCKAGE.listerNotes();
        oList = FXCollections.observableArrayList(notes);
        filtre = new FilteredList<>(oList, s -> true);
        list.setItems(filtre);
        realTabsX = tabs.getLayoutX();

        MenuItem menuSupprimer = new MenuItem("Supprimer");
        menuSupprimer.setOnAction(e -> {
            menu.hide();
            supprimer(nomChoisit);
        });
        MenuItem menuRenommer = new MenuItem("Renommer");
        menuRenommer.setOnAction(e -> {
            menu.hide();
            renommer(nomChoisit);
        });
        MenuItem menuOuvrir = new MenuItem("Ouvrir");
        menuOuvrir.setOnAction(e -> {
            menu.hide();
            ouvrir(nomChoisit);
        });
        menu = new ContextMenu(menuSupprimer, menuRenommer, menuOuvrir);

        INSTANCE = this;
    }

    /*
     ------> +X
     |
     |
     v
     +Y
     */
    public void largeurChange(double ancienneLargeur, double nouvelleLargeur) {
        double ratio = nouvelleLargeur / ancienneLargeur;
        realTabsX *= ratio;
        tabs.setLayoutX(Math.ceil(realTabsX));//Pour que le texte ne soit pas flou il faut que sa position soit un nombre entier
        tabs.setPrefWidth(tabs.getPrefWidth() * ratio);
        list.setLayoutX(list.getLayoutX() * ratio);
        list.setPrefWidth(list.getPrefWidth() * ratio);
        recherche.setLayoutX(recherche.getLayoutX() * ratio);
        recherche.setPrefWidth(list.getPrefWidth());
        double tailleBouton = (list.getPrefWidth() / 2.0);
        ajouter.setLayoutX(ajouter.getLayoutX() * ratio);
        ajouter.setPrefWidth(tailleBouton);
        supprimer.setLayoutX(supprimer.getLayoutX() * ratio);
        supprimer.setPrefWidth(tailleBouton);
        for (Tab tab : tabs.getTabs()) {
            TextArea texte = (TextArea) tab.getContent();
            String police = texte.getFont().getFamily();
            double taille = Math.max(Math.min(20, texte.getFont().getSize() * ratio), 12);
            Font nouvellePolice = Font.font(police, taille);
            texte.setFont(nouvellePolice);
        }
    }

    public void hauteurChange(double ancienneHauteur, double nouvelleHanteur) {
        double ratio = nouvelleHanteur / ancienneHauteur;
        tabs.setLayoutY(tabs.getLayoutY() * ratio);
        tabs.setPrefHeight(tabs.getPrefHeight() * ratio);
        list.setLayoutY(list.getLayoutY() * ratio);
        list.setPrefHeight(list.getPrefHeight() * ratio);
        recherche.setLayoutY(recherche.getLayoutY() * ratio);
        for (Tab tab : tabs.getTabs()) {
            TextArea texte = (TextArea) tab.getContent();
            String police = texte.getFont().getFamily();
            double taille = Math.max(Math.min(20, texte.getFont().getSize() * ratio), 12);
            Font nouvellePolice = Font.font(police, taille);
            texte.setFont(nouvellePolice);
        }
    }

    @FXML
    public void recherche(KeyEvent e) {
        if (e.getCode() == KeyCode.ESCAPE) {
            recherche.clear();
            filtre.setPredicate(s -> true);
            return;
        }
        String texte = recherche.getText().toLowerCase();
        if (texte == null || texte.length() == 0) {
            filtre.setPredicate(s -> true);
        } else {
            filtre.setPredicate(s -> s.toLowerCase().contains(texte));
        }
    }

    @FXML
    public void clicListe(Event e) {
        Object o = list.getSelectionModel().getSelectedItem();
        if (o == null) {
            return;
        }
        String nom = (String) o;
        if (e instanceof MouseEvent) {
            MouseEvent me = (MouseEvent) e;
            switch (me.getButton()) {
                case PRIMARY:
                    ouvrir(nom);
                    break;
                case SECONDARY:
                    nomChoisit = nom;
                    menu.show(list, me.getScreenX(), me.getScreenY());
                    break;
            }
            return;
        }
        ouvrir(nom);

    }

    private void ouvrir(String nom) {
        for (Tab t : tabs.getTabs()) {
            if (t.getText().equals(nom)) {
                return;
            }
        }
        String contenu = STOCKAGE.lire(nom);
        TextArea textArea = new TextArea(contenu);
        Tab tab = new Tab(nom, textArea);
        tab.setClosable(true);
        tab.setOnClosed((value) -> {
            String nouveauContenu = textArea.getText();
            STOCKAGE.enregistrer(nom, nouveauContenu);
            textArea.clear();
        });
        tabs.getTabs().add(tab);

    }

    private void supprimer(String nom) {
        tabs.getTabs().removeIf((t) -> t.getText().equals(nom));
        oList.remove(nom);
        STOCKAGE.supprimer(nom);
    }

    private void renommer(String nomActuel) {
        TextInputDialog dialog = new TextInputDialog(nomActuel);
        dialog.setTitle("Renommage");
        dialog.setContentText("Entrez un nouveau nom:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String nouveauNom = result.get();
            if (nouveauNom.trim().isEmpty()) {
                Alert alerte = new Alert(Alert.AlertType.ERROR);//Java 8u40 et plus
                alerte.setTitle("Erreur");
                alerte.setHeaderText("Nom de la note invalide");
                alerte.setContentText("Vous avez entré un nom vide !");
                alerte.showAndWait();
            } else if (oList.contains(nouveauNom)) {
                Alert alerte = new Alert(Alert.AlertType.ERROR);//Java 8u40 et plus
                alerte.setTitle("Erreur");
                alerte.setContentText("Une note avec le même nom existe déjà !");
                alerte.showAndWait();
            } else if (nouveauNom.contains("/") || nouveauNom.contains("\\")) {
                Alert alerte = new Alert(Alert.AlertType.ERROR);//Java 8u40 et plus
                alerte.setTitle("Erreur");
                alerte.setHeaderText("Nom de la note invalide");
                alerte.setContentText("Les caractères / et \\ ne sont pas autorisés !");
                alerte.showAndWait();
            } else {
                for (Tab tab : tabs.getTabs()) {
                    if (tab.getText().equals(nomActuel)) {
                        tab.setText(nouveauNom);
                        tab.setId(nouveauNom);
                    }
                }
                oList.removeIf(s -> s.equals(nomActuel));
                oList.add(nouveauNom);
                STOCKAGE.renommer(nomActuel, nouveauNom);
            }
        }
    }

    @FXML
    public void clicAjouter(ActionEvent e) {
        TextInputDialog dialog = new TextInputDialog();//Java 8u40 et plus
        dialog.setTitle("Création d'une note sécurisée");
        dialog.setContentText("Entrez un nom:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String nom = result.get();
            if (nom.trim().isEmpty()) {
                Alert alerte = new Alert(Alert.AlertType.ERROR);//Java 8u40 et plus
                alerte.setTitle("Erreur");
                alerte.setHeaderText("Nom de la note invalide");
                alerte.setContentText("Vous avez entré un nom vide !");
                alerte.showAndWait();
            } else if (oList.contains(nom)) {
                Alert alerte = new Alert(Alert.AlertType.ERROR);//Java 8u40 et plus
                alerte.setTitle("Erreur");
                alerte.setContentText("Une note avec le même nom existe déjà !");
                alerte.showAndWait();
            } else if (nom.contains("/") || nom.contains("\\")) {
                Alert alerte = new Alert(Alert.AlertType.ERROR);//Java 8u40 et plus
                alerte.setTitle("Erreur");
                alerte.setHeaderText("Nom de la note invalide");
                alerte.setContentText("Les caractères / et \\ ne sont pas autorisés !");
                alerte.showAndWait();
            } else {
                oList.add(nom);
                STOCKAGE.enregistrer(nom, "");
            }
        }
    }

    @FXML
    public void clicSupprimer(ActionEvent e) {
        Object o = list.getSelectionModel().getSelectedItem();
        if (o == null) {
            return;
        }
        String nom = (String) o;
        supprimer(nom);
    }

}
