package encryptor;
//need to import libarary
import org.jasypt.util.text.BasicTextEncryptor;
import java.util.Timer;
import java.util.*;
/**
 *
 * @author rose
 */
public class Encryptor {
    //input will be the password gotten from user
    public static void encrypt(){
        String input = "password";
        BasicTextEncryptor cryptor = new BasicTextEncryptor();
        cryptor.setPassword(input);
        for(int x=1; x<input.length(); x++){
        System.out.print("*");   
        }    
    }
    
    public static void main(String[] args) {   
        encrypt();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
          public void run(){
              String input = "password";
              System.out.println(input);
          }
        }, 0,30000);
        //replace "password" with a passed string forom the database..
    }
}
