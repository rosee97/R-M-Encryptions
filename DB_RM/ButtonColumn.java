package encryptor;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author rose
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import org.jasypt.util.text.BasicTextEncryptor;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.TableCellRenderer;

class ButtonColumn extends JButton implements TableCellRenderer {

  public ButtonColumn(JToggleButton jb) {
    setOpaque(true);
    if(jb.isSelected())
      {
          jb.setText("Show");
          //Encryptor.encrypt(table.getColumn("Password"));
      }
      else
      {
          jb.setText("Don't Show");
      //Decryptor.decrypt(table.getColumn("Password"));
      }  
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column) {
    if (isSelected) {
      setForeground(table.getSelectionForeground());
      setBackground(table.getSelectionBackground());
    } else {
      setForeground(table.getForeground());
      setBackground(UIManager.getColor("Button.background"));
    }
    setText((value == null) ? "" : value.toString());
    return this;
  }
  
  
  public static void main(String[] args) {
    
  }
}