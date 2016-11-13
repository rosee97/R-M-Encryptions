package encryptor;
import org.jasypt.util.text.*;
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
        Encryptor.encrypt("password");
        //replace "passowrd" with a passed string forom the database..
    }
        

    }
    

