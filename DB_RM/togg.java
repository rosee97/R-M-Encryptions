/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package encryptor;

/**
 *
 * @author rose
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class myframe extends JFrame implements ItemListener
{

JToggleButton jb;
JTextField t1;
myframe()
{

setTitle("JTOGGLE BUTTON");
setLayout(new FlowLayout());
setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//t1 = new JTextField(15);
jb = new JToggleButton("Show");
jb.setPreferredSize(new Dimension(150,50));
jb.setBackground(Color.cyan);
add(jb);
//add(t1);
jb.addItemListener(this);
setSize(700, 200);
setVisible(true);
} 
public void itemStateChanged(ItemEvent eve)
{   
if(jb.isSelected())
{
    jb.setText("Show");
//t1.setEnabled(false);
}
else
{
    jb.setText("Don't Show");
//t1.setEnabled(true);
}
}  
}

public class togg
{

public static void main(String[] args) {

myframe x = new myframe();
}
}