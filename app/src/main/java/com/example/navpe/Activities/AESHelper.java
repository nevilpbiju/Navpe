package com.example.navpe.Activities;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AESHelper {

    private static final int ITERATION_COUNT = 1000;
    private static final int KEY_LENGTH = 256;
    private static final String PBKDF2_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int PKCS5_SALT_LENGTH = 32;
    private static final String DELIMITER = "]";
    private static final SecureRandom random = new SecureRandom();

    public static String encrypt(String plaintext, String password) {
        byte[] salt  = generateSalt();
        SecretKey key = deriveKey(password, salt);
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            byte[] iv = generateIv(cipher.getBlockSize());
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
            byte[] cipherText = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return String.format("%s%s%s%s%s", toBase64(salt), DELIMITER, toBase64(iv), DELIMITER, toBase64(cipherText));

        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
    public static String decrypt(String ciphertext, String password) {
        String[] fields = ciphertext.split(DELIMITER);
        if(fields.length != 3) {
            throw new IllegalArgumentException("Invalid encrypted text format");
        }
        byte[] salt        = fromBase64(fields[0]);
        byte[] iv          = fromBase64(fields[1]);
        byte[] cipherBytes = fromBase64(fields[2]);
        SecretKey key = deriveKey(password, salt);

        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
            byte[] plaintext = cipher.doFinal(cipherBytes);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
    public static Key getSecureRandomKey(String cipher) {
        byte[] secureRandomKeyBytes = new byte[KEY_LENGTH / 8];
        SecureRandom random = new SecureRandom();
        random.nextBytes(secureRandomKeyBytes);
        return new SecretKeySpec(secureRandomKeyBytes, cipher);
    }
    private static byte[] generateSalt() {
        byte[] b = new byte[PKCS5_SALT_LENGTH];
        random.nextBytes(b);
        return b;
    }
    private static byte[] generateIv(int length) {
        byte[] b = new byte[length];
        random.nextBytes(b);
        return b;
    }
    private static SecretKey deriveKey(String password, byte[] salt) {
        try {
            KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBKDF2_DERIVATION_ALGORITHM);
            byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
    private static String toBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
    private static byte[] fromBase64(String base64) {
        return Base64.decode(base64, Base64.NO_WRAP);
    }
    private static void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles())) {
                DeleteRecursive(child);
            }
        }
        //noinspection ResultOfMethodCallIgnored
        fileOrDirectory.delete();
    }
    public static Bitmap decrypt(Context context) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPref",MODE_PRIVATE);
        // on below line creating a file for getting photo directory.
        File photoDir = context.getExternalFilesDir(Environment.DIRECTORY_DCIM);
        File file = new File(photoDir + File.separator + "encryptedFaceData.jpeg");
        // on below line creating input stream for file with file path.
        FileInputStream fis = new FileInputStream(file.getPath());
        // on below line creating a file for decrypted image.
        File decFile = new File(photoDir, "decryptedFaceData.jpeg");
        if(decFile.exists()){
            DeleteRecursive(decFile);
        }
        byte[] salt = Base64.decode(sharedPreferences.getString("salt",null), Base64.DEFAULT);
        SecretKey key = deriveKey("A%D*G-KaPdSgVkYp3s6v9y/B?E(H+MbQ", salt);
        // on below line creating an file output stream for decrypted image.
//        SecretKey key = (SecretKey) getSecureRandomKey("AES");
        FileOutputStream fos = new FileOutputStream(decFile.getPath());
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        //Decode the string into correct(byte) format
        byte[] iv = Base64.decode(sharedPreferences.getString("IV",null), Base64.DEFAULT);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        // on below line creating a variable for cipher input stream.
        CipherInputStream cis = new CipherInputStream(fis, cipher);
        // on below line creating a variable b.
        int b;
        byte[] d = new byte[8];
        while ((b = cis.read(d)) != -1) {
            fos.write(d, 0, b);
        }
        // on below line flushing our fos, closing fos and closing cis.
        fos.flush(); fos.close();
        cis.close();
        // on below line creating an image file from decrypted image file path.
        File imgFile = new File(decFile.getPath());
        if (imgFile.exists()) {
            // creating bitmap for image and displaying that bitmap in our image view.
            return BitmapFactory.decodeFile(imgFile.getPath());
        }
        return null;
    }
    // on below line creating a method to encrypt an image.
    public static void encrypt(Context context, byte[] path) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        byte[] salt  = generateSalt();
        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPref",MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("salt", Base64.encodeToString(salt, Base64.DEFAULT));
        edit.apply();

        SecretKey key = deriveKey("A%D*G-KaPdSgVkYp3s6v9y/B?E(H+MbQ", salt);
        // on below line creating a variable for file input stream
        InputStream fis = new ByteArrayInputStream(path);
//        FileInputStream fis = new FileInputStream(path);
        // on below line creating a variable for file
        File photoDir = context.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DCIM);
        // on below line creating a file for encrypted file.
        File file = new File(photoDir, "encryptedFaceData.jpeg");
        if(file.exists()){
            DeleteRecursive(file);
        }
        // on below line creating a variable for file output stream.
        FileOutputStream fos = new FileOutputStream(file.getPath());
        // on below line creating a variable for secret key.
        // creating a variable for secret key and passing our secret key and algorithm for encryption.
//        SecretKeySpec sks = new SecretKeySpec("A%D*G-KaPdSgVkYp3s6v9y/B?E(H+MbQ".getBytes(), "AES");
        // on below line creating a variable for cipher and initializing it
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

        SecureRandom r = new SecureRandom(); // should be the best PRNG
        byte[] bytes = new byte[16];
        r.nextBytes(bytes);
        IvParameterSpec ivSpec = new IvParameterSpec(bytes);

        edit.putString("IV",  Base64.encodeToString(bytes, Base64.DEFAULT)).apply();
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        // on below line initializing cipher and specifying decrypt mode to encrypt.
//        cipher.init(Cipher.ENCRYPT_MODE, sks);
        // on below line creating cos
        CipherOutputStream cos = new CipherOutputStream(fos, cipher);
        int b;
        byte[] d = new byte[8];
        while ((b = fis.read(d)) != -1) {
            cos.write(d, 0, b);
        }
        // on below line closing our cos and fis.
        cos.flush();     cos.close();
        fis.close();
    }
}