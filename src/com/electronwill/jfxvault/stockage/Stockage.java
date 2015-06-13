/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.electronwill.jfxvault.stockage;

import java.lang.reflect.Field;

/**
 * Classe utilitaire pour stocker et récupérer les notes de façon sécurisée.
 *
 * @author TheElectronWill
 */
public abstract class Stockage {

    public abstract void renommer(String ancienNom, String nouveauNom);

    public abstract String[] listerNotes();

    public abstract void enregistrer(String nom, String contenu);

    public abstract void supprimer(String nom);

    public abstract String lire(String nom);

    public abstract void definirMdp(String mdp);

    public abstract boolean deverouiller(String mdp);

    public abstract boolean premiereFois();

    /**
     * Fait "disparaître" le contenu de la chaîne de caractères.
     *
     * @param mdp
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.NoSuchFieldException
     */
    public final void securiser(String mdp) throws IllegalAccessException, NoSuchFieldException {
//    private final char value[];
        Field value = String.class.getDeclaredField("value");
        value.setAccessible(true);
        char[] chars = (char[]) value.get(mdp);
        for (int i = 0; i < chars.length; i++) {
            chars[i] = 0;
        }
        value.set(mdp, new char[0]);
    }

}
