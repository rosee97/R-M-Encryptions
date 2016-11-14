/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SimpleTableDemo.java requires no other files.
 */
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JToggleButton;
import javax.swing.table.TableCellRenderer;

public class em_db extends JPanel {

  private boolean DEBUG = false;

  public em_db() {
    super(new GridLayout(1, 0));

    String[] columnNames = {"ID",
      "Account Type",
      "User Name",
      "Password",
      "Select to encrypt"};

    Object[][] data = {
      {"1" ,"FaceBook", "rose@97",
       "Roshi_v@97","Show"},
      {"2", "Instagram", "roseeVallurupalli",
       "RoshMani_v","Show"},
      {"3", "Gmail", "MichaelaB@gmail.com",
       "Michaela@B45","Show"}};
   
    final JTable table = new JTable(data, columnNames);
    table.setPreferredScrollableViewportSize(new Dimension(500, 70));
    table.setFillsViewportHeight(true);
    
    table.getColumn("Select to encrypt").setCellRenderer(new ButtonColumn());

    
    if (DEBUG) {
      table.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          printDebugData(table);
        }
      });
    }

    //Create the scroll pane and add the table to it.
    JScrollPane scrollPane = new JScrollPane(table);

    //Add the scroll pane to this panel.
    add(scrollPane);

    JDBCAdapter adapter = new JDBCAdapter("jdbc:h2:~/test1", "org.h2.Driver", "sa", "");
  }

  private void printDebugData(JTable table) {
    int numRows = table.getRowCount();
    int numCols = table.getColumnCount();
    javax.swing.table.TableModel model = table.getModel();

    System.out.println("Value of data: ");
    for (int i = 0; i < numRows; i++) {
      System.out.print("    row " + i + ":");
      for (int j = 0; j < numCols; j++) {
        System.out.print("  " + model.getValueAt(i, j));
      }
      System.out.println();
    }
    System.out.println("--------------------------");
  }

  /**
   * Create the GUI and show it. For thread safety, this method should be
   * invoked from the event-dispatching thread.
   */
  private static void createAndShowGUI() {
    //Create and set up the window.
    JFrame frame;
    frame = new JFrame("EM_Data_Table");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    

    //Create and set up the content pane.
    em_db newContentPane = new em_db();
    newContentPane.setOpaque(true); //content panes must be opaque
    frame.setContentPane(newContentPane);

    //Display the window.
    frame.pack();
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    //Schedule a job for the event-dispatching thread:
    //creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        createAndShowGUI();
      }
    });
  }
}