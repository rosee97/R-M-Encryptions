package encryptor;
//package decryptor;
//need to import libarary
import org.jasypt.util.text.BasicTextEncryptor;
//import java.util.*;
/**
 *
 * @author Michaela Barnett
 */
public class Encryptor {
    //input will be the password gotten from user
    
    public static void encrypt(String input){
        BasicTextEncryptor cryptor = new BasicTextEncryptor();
        cryptor.setPassword(input);
        for(int x=1; x<input.length(); x++){
        System.out.print("*");   
    }    
    }
    
    public static void main(String[] args) {
      //if /*(when user clicks show button)*/{
        Encryptor.encrypt("password");
        Double startTime = (System.currentTimeMillis()/1000000000.0); 
        if (startTime >= 40){
          //Decryptor.decrypt("password");
        } 
      //}  
        
        //replace "passowrd" with a passed string forom the database..
    }
        

    }
