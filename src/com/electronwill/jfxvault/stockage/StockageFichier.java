/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.electronwill.jfxvault.stockage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author TheElectronWill
 */
public final class StockageFichier extends Stockage {

    private final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
    private final File homeDir = new File(System.getProperty("user.home"));
    /**
     * Répertoire contenant les fichiers et dossiers de JPass.
     */
    private final File appDir;
    /**
     * Répertoire contenant les notes sécurisées, toutes encryptées avec AES-256.
     */
    private final File secureDir;
    /**
     * Fichier contenant le hash (SHA-256) du mot de passe. Permet de vérifier que l'utilisateur
     * entre le bon.
     */
    private final File mainFile;
    /**
     * Utilitaire de chiffrement.
     */
    private final Cipher cipher;
    /**
     * Clé secrète de chiffrement.
     */
    private volatile SecretKey key;

    public StockageFichier() throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("linux")) {
            appDir = new File(homeDir, ".local/share/jpass");
        } else if (os.contains("win")) {
            appDir = new File(System.getenv("APPDATA"), "JPass");
        } else {//OS X
            appDir = new File(homeDir, "Library/Application Support/JPass");
        }
        secureDir = new File(appDir, "storage");
        secureDir.mkdirs();
        mainFile = new File(appDir, ".passwd");
    }

    @Override
    public void definirMdp(String mdp) {
        try {
            mainFile.createNewFile();
            byte[] passwd = mdp.getBytes(StandardCharsets.UTF_8);
            byte[] hash = sha256.digest(passwd);
            byte[] hashHash = sha256.digest(hash);
            try (FileOutputStream out = new FileOutputStream(mainFile, false)) {
                out.write(hashHash);
            }
            key = new SecretKeySpec(hash, 0, 16, "AES");//128 bits = 16 octets
        } catch (IOException ex) {
            Logger.getLogger(StockageFichier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String[] listerNotes() {
        File[] notes = secureDir.listFiles();
        String[] noms = new String[notes.length];
        for (int i = 0; i < notes.length; i++) {
            File note = notes[i];
            noms[i] = note.getName();
        }
        return noms;
    }

    @Override
    public boolean premiereFois() {
        return !mainFile.exists() || mainFile.length() != 32;
    }

    @Override
    public boolean deverouiller(String mdp) {
        try {
            byte[] passwd = mdp.getBytes(StandardCharsets.UTF_8);
            byte[] hash = sha256.digest(passwd);
            boolean ok = verifier(hash);
            if (!ok) {
                return false;
            }
            key = new SecretKeySpec(hash, 0, 16, "AES");//128 bits = 16 octets
        } catch (Exception ex) {
            Logger.getLogger(StockageFichier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    @Override
    public void renommer(String ancienNom, String nouveauNom) {
        try {
            File f = new File(secureDir, ancienNom);
            File f2 = new File(secureDir, nouveauNom);
            Files.copy(f.toPath(), f2.toPath());
            f.delete();
        } catch (IOException ex) {
            Logger.getLogger(StockageFichier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void supprimer(String nom) {
        File f = new File(secureDir, nom);
        f.delete();
    }

    /**
     * Vérifie que le hash donné correspond a celui stocké dans {@link #mainFile}.
     *
     * @param hash
     * @return
     * @throws IOException
     */
    public boolean verifier(byte[] hash) throws IOException {
        byte[] hashHash = sha256.digest(hash);
        byte[] trueHash = lire(mainFile);
        for (int i = 0; i < trueHash.length; i++) {
            byte tb = trueHash[i];
            byte hb = hashHash[i];
            if (tb != hb) {
                return false;
            }
        }
        return true;
    }

    private void print(byte[] bytes) {
        for (byte b : bytes) {
            System.out.print(b);
            System.out.print(",");
        }
        System.out.println();
    }

    private void print(String nom, byte[] bytes) {
        System.out.print(nom);
        System.out.print(": ");
        for (int b : bytes) {
            System.out.print(b);
            System.out.print(",");
        }
        System.out.println();
    }

    @Override
    public void enregistrer(String nom, String contenu) {
        try {
            File f = new File(secureDir, nom);
            if (contenu.isEmpty()) {
                f.createNewFile();
                return;
            }
            byte[] bytes = contenu.getBytes(StandardCharsets.UTF_8);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(bytes);
            try (FileOutputStream out = new FileOutputStream(f, false)) {
                out.write(encrypted);
            }
        } catch (Exception ex) {
            Logger.getLogger(StockageFichier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Lit le contenu d'un fichier.
     *
     * @param f
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public byte[] lire(File f) throws FileNotFoundException, IOException {
        FileInputStream input = new FileInputStream(f);
        ByteArrayOutputStream output = new ByteArrayOutputStream(input.available());
        byte[] buff = new byte[1024];
        while (input.available() > 0) {
            int read = input.read(buff);
            output.write(buff, 0, read);
        }
        return output.toByteArray();
    }

    @Override
    public String lire(String nom) {
        File f = new File(secureDir, nom);
        if (f.length() == 0) {
            return "";
        }
        try {
            byte[] encrypted = lire(f);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            Logger.getLogger(StockageFichier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
