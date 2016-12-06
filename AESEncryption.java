package aesencryption;
import static aesencryption.AESEncryption.main;
import java.util.*;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.logging.Logger.global;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import java.util.Timer;
import java.util.TimerTask;


/**
 *
 * @author Michaela Barnett
 */
public class AESEncryption {
   
    
// public static void encrytion(tobedecrypted,password){
  //   
 //}
  //public static void encrytion(tobedecrypted,password){
     
 //}
    
    private static SecretKeySpec secretKey ;
    private static byte[] key ;
    private static String decryptedString;
    private static String encryptedString;
    private static String passClass;
    public static void setKey(String myKey){
        
   
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
          //System.out.println(key.length);  we can change alot of things about the encryptin like the key length 
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // use only first 128 bit
         //   System.out.println(key.length); //lenght
          //  System.out.println(new String(key,"UTF-8")); //the UTF
            secretKey = new SecretKeySpec(key, "AES");
            
            
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static String getDecryptedString() {
        return decryptedString;
    }
    public static void setDecryptedString(String decryptedString) {
        AESEncryption.decryptedString = decryptedString;
    }
    public static String getEncryptedString() {
        return encryptedString;
    }
    public static void setEncryptedString(String encryptedString) {
        AESEncryption.encryptedString = encryptedString;
    }
    public static String encrypt(String strToEncrypt)
    {
        try
        {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
         
            setEncryptedString(Base64.encodeBase64String(cipher.doFinal(strToEncrypt.getBytes("UTF-8"))));
        
        }
        catch (Exception e)
        {
           
            System.out.println("Error while encrypting: "+e.toString());
        }
        return null;
    }
    public static String decrypt(String strToDecrypt)
    {
        try
        {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
           
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            setDecryptedString(new String(cipher.doFinal(Base64.decodeBase64(strToDecrypt))));
            
        }
        catch (Exception e)
        {
         
            System.out.println("Error while decrypting: "+e.toString());
        }
        return null;
    }
     
  
    //acts as password from other method
    
   public static void main(String args[]){
                //button click here
                //after user enters password they get 5 secs to open files.
                final String strToEncrypt = "User Password"; //get the user password via user imput --java.util.scanner or via database
                final String strPssword = "encryptor key";
                
                System.out.println("Decrypted : " + AESEncryption.getDecryptedString());
                
                AESEncryption.setKey(strPssword);
                //timer 5 sec 
                try {
			//sleep 5 seconds
			Thread.sleep(5000);
                
                 AESEncryption.encrypt(strToEncrypt.trim());
                //then the ebcryption begings
                System.out.println("String to Encrypt: " + strToEncrypt); 
                System.out.println("Encrypted: " + AESEncryption.getEncryptedString());
                } catch (InterruptedException e) {
			e.printStackTrace();
		}
                //prompt for another 5 secs
        
                final String strToDecrypt =  AESEncryption.getEncryptedString();
                AESEncryption.decrypt(strToDecrypt.trim());
                
               
            //    System.out.println("String To Decrypt : " + strToDecrypt);
                System.out.println("Decrypted : " + AESEncryption.getDecryptedString());
                }
      
}