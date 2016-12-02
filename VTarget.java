 
 
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.sql.*;
import java.io.*;
import java.util.concurrent.Semaphore;
import java.util.regex.*;

public class VTarget extends  JInternalFrame 
{

    String name = "job";
    String[] names = {"Target", "RegEx", "FiniteSet", "Required", "Datatyp"};
    int numCols = 5;
    public int widths[] = {1, 5, 5, 1, 1};
    
    String keys[] = null;
    int N = 6;

    JDBCAdapter adapter = Encryptor.adapter;
    String tablesig = "";
    public void getdata() 
    {
        String sql0 = "Create table Target( searchname varchar(20), target  varchar(20),  regex  text, finiteset text, required varchar(1),datatyp  varchar(20), num int, primary key(searchname, target))";
        adapter.executeUpdate(sql0);
        adapter.executeUpdate("delete FROM Target where target='' OR target is null");
        String sql2 = "SELECT   target as Target, regex as RegEx, finiteset as FiniteSet, required as Required, datatyp as DataType FROM Target WHERE searchname='" + name + "' order by num";
        adapter.needMetaInfo = true;
        N = adapter.executeQuery(sql2);
        if (N == -1) 
            N=6; 
        else N++;
        keys = new String[N];
        for (int i=0; i < N-1; i++)
        {
            keys[i] = adapter.getValueAt(i,0);
            tablesig += adapter.getValueAt(i,0) + "-" + adapter.getValueAt(i,4) + ";";
        }
    }

    GridLayout grids;
    JPanel  panel;
    public JButton btnchoser, btnsave, btnstart, btnrewind, btnredo, btnstop;
    JTextArea[][] textareas;

    String path = "/Users/IMHil/Downloads/us.sitesucker.maz.sitesucker/www.dice.com/jobs";
    Thread process;
    public String state = "initial";
    public Semaphore lock = new Semaphore(0);
    void addrow(int r)
    {
        textareas[r] = new JTextArea[numCols];
        for (int c = 0; c < numCols; c++) 
        {
            GridBagConstraints gbc = makeGbc(c, r+1);
            textareas[r][c] = new  JTextArea(6, widths[c]*6 );
            if (r < N) 
            {
                textareas[r][c].setText(adapter.getValueAt(r, c));
            }
            if (false && (c == 0 || c == 3 || c==4))
            {
                JScrollPane scrollPane = new JScrollPane( textareas[r][c]);
                panel.add(scrollPane,gbc);
            }
            else
            { 
                panel.add(textareas[r][c],gbc);
                textareas[r][c].setWrapStyleWord(true);
            }
        }
    }
    void growrow()
    {
        int r = ++N;
        addrow(r); 
    }
    public VTarget(String nm) 
    {
        super(nm, true, true, true, true);
        name = nm;
        getdata();
         
        GridBagLayout grids = new GridBagLayout();
        panel = new javax.swing.JPanel();
        //panel.setBounds(0, 0, 600, 600);
        panel.setLayout(grids);
        
        textareas = new JTextArea[N + 10][];
        for (int c = 0; c < numCols; c++) 
        {
            GridBagConstraints gbc = makeGbc(c, 0);
            JLabel lab = new JLabel(names[c]);
            //lab.setSize(80, 30);
            panel.add(lab, gbc);
        }    
        for (int r = 0; r < N + 1; r++) 
        {
            addrow(r);
        }
        
        JScrollPane scrollPane = new javax.swing.JScrollPane();
        Container content = getContentPane();
        GridBagLayout gridbag = new GridBagLayout();
        content.setLayout(gridbag);
        GridBagConstraints z = new GridBagConstraints();
        
        z.weightx = 0;
        z.gridx = 0;
        z.gridy = 0;
        z.gridwidth=1;
        z.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(scrollPane, z);
        content.add(panel);
        JPanel buttonson = new JPanel();
        btnsave = new JButton("Save");
        btnsave.setSize(80, 25);
        buttonson.add(btnsave);
        btnchoser = new JButton("Choose");
        btnchoser.setSize(80, 25);
        buttonson.add(btnchoser);
        btnrewind = new JButton("Rewind");
        btnrewind.setSize(80, 25);
        buttonson.add(btnrewind);
        btnredo = new JButton("Redo");
        btnredo.setSize(80, 25);
        buttonson.add(btnredo);
        btnstart = new JButton("Start");
        btnstart.setSize(80, 25);
        buttonson.add(btnstart);
        btnstop = new JButton("Stop");
        btnstop.setSize(80, 25);
        buttonson.add(btnstop);
        z.weightx = 0;
        z.gridx = 0;
        z.gridy = 1;
        z.gridwidth=1;
        z.fill = GridBagConstraints.BOTH;
        gridbag.setConstraints(buttonson, z);
        content.add(buttonson);
        ButtonListener actionlistener = new ButtonListener(this);
        btnchoser.addActionListener(actionlistener);
        btnstart.addActionListener(actionlistener);
        btnsave.addActionListener(actionlistener);
        btnrewind.addActionListener(actionlistener);
        btnredo.addActionListener(actionlistener);
        btnstop.addActionListener(actionlistener);
        setSize(540, 650);
        setVisible(true);
    }

    public String readTextAt(int i, int j) 
    {
        String s = (String) (textareas[i][j].getText());
        if (j == 0 || j==3  )
        {
            return s.replaceAll("\\W","");
        }
        return s;
    }

    String visited = "E:";
    public boolean stopit = false;

    class ButtonListener implements ActionListener {

        public ButtonListener(VTarget vt) 
        {
            this.vt = vt;
        }
        VTarget vt;

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("Choose")) 
            {
                String filename = ".";
                JFileChooser fc = new JFileChooser(new File(visited));
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.showOpenDialog(getDesktopPane().getTopLevelAncestor());
                File selFile = fc.getSelectedFile();
                if (selFile!=null)
                {
                path = selFile.getAbsolutePath();
                int k = path.lastIndexOf(File.separator);
                visited = path.substring(0, k);
                vt.setTitle(name + ":" + path);
                }
            } 
            else if (command.equals("Start")) 
            {
                vt.state = "started";
                String fs = "", dt="";
                for (int i=0; i < N+1; i++)
                {
                    if (!readTextAt(i,0).equals("")){
                    fs +=  readTextAt(i,0) + ",";
                    dt +=  readTextAt(i,4) +  ","  ;}
                }
                fs += "filepath";
                dt += "varchar(200)";
                //VSheet vs =  new VSheet(name ,fs.split(","), dt.split(","),  new String[]{"filepath"} );
                VSheet vs = new VSheet("CREATE TABLE job(location varchar(51),skills varchar(300),position varchar(50),salary int,filepath varchar(200),PRIMARY KEY(filepath)");
                Encryptor.addTheFrame(vs);

            } else if (command.equals("Pause")) {
                vt.state = "paused";
                vt.btnstart.setText("Resume");
            } else if (command.equals("Resume")) {
                vt.state = "started";
                vt.lock.release();
                vt.btnstart.setText("Pause");
            } else if (command.equals("Stop")) {
                vt.state = "paused";
                vt.stopit = true;
                vt.btnstart.setText("Start");
            } else if (command.equals("Save") ) {

                String sql;
                String tablesig1 = "";
                boolean needrefresh = false; 
                for (int i = 0; i < N+1; i++) 
                {
                    if (readTextAt(i,0).equals(""))
                    tablesig1 += readTextAt(i,0) + "-" + readTextAt(i,4) + ";";
                    try 
                    {
                        Pattern.compile((String) (readTextAt(i, 1)));
                    }
                    catch (java.util.regex.PatternSyntaxException exception) 
                    {
                        JOptionPane.showMessageDialog(vt, (String) ((String) (readTextAt(i, 1))) + " is invalid regular expression");
                        continue;
                    }
                    textareas[i][2].setText(readTextAt(i, 2).replaceAll("[\n|\r|\t]", " ").replaceAll("[ ]+", " ").replaceAll("[ ]*,[ ]*", ","));

                    sql = "";
                    
                    for (int j = 0; j < numCols; j++) 
                    {

                        sql += "'" + readTextAt(i, j).replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\") + "',";
                        
                    }
                    sql = "INSERT INTO Target values ('" + name.replaceAll("'", "''") + "'," + sql +   (i + 1) + ")";
                   

                    if (adapter.executeUpdate(sql) == 1) 
                        needrefresh = true;
                    Encryptor.println(adapter.error() + sql);

                    if (i < keys.length && keys[i] != null) 
                    {
                        sql = "";
                        for (int j = 0; j < numCols; j++) {
                            sql += readTextAt(i, j);
                        }
                        if (sql.trim().equals("")) {
                            sql = "DELETE FROM Target WHERE target='" + keys[i] + "' and searchname='" + name.replaceAll("'", "''") + "'";
                        } else {
                            sql = "";
                            for (int j = 0; j < numCols; j++) 
                            {
                                String x = readTextAt(i, j);//readTextAt(i,j).replaceAll("'", "''");

                                //if (!x.equals(""))
                                {
                                    sql += names[j] + "='" + x.replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\") + "',";
                                    
                                }
                            }
                            String nm = keys[i];
                            if (!sql.equals("")) 
                            {
                                sql = "UPDATE Target SET " + sql.replaceFirst(",$", "") + " WHERE searchname='" + name.replaceAll("'", "''") + "' AND target='" + nm.replaceAll("'", "''") + "'";
                            }
                            keys[i] = readTextAt(i, 0);
                        }
                        adapter.executeUpdate(sql);
                        Encryptor.println(adapter.error() + sql);
                    }

                }
                adapter.needMetaInfo = true;
                if (!tablesig.equals("tablesig1")) 
                {
                    adapter.executeUpdate("DROP TABLE " + name);
                    tablesig = tablesig1;
                    String sql0 = "CREATE TABLE " + name + "(";
                    for (int i = 0; i < N; i++) 
                    {
                        if (!readTextAt(i,0).equals(""))
                        sql0 += readTextAt(i,0) + " " + readTextAt(i,4) + ",";
                        Encryptor.println(sql0);
                    }
                    sql0 += "filepath varchar(200),PRIMARY KEY(filepath))";
                    adapter.executeUpdate(sql0);
                    Encryptor.println(adapter.error());
                }
                if (needrefresh) growrow(); 
            }
        }
    }
    private GridBagConstraints makeGbc(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.gridx = x;
        gbc.gridy = y;
        if (x==0 || x==3 ||x==4)
            gbc.weightx = 0;
        else
            gbc.weightx = 0.5;
        gbc.weighty = 1.0/(1+N);
        gbc.insets = new Insets(1, 1, 1, 1);
        gbc.anchor = (x == 0) ? GridBagConstraints.LINE_START : GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.BOTH;
        return gbc;
    }
    public static void main2(String[] args) {
        VTarget frame = new VTarget("job");
        // frame.addWindowListener(new WindowAdapter() {
        //   public void windowClosing(WindowEvent e) {
        //     System.exit(0);
        //   }
        // });
    }

    class FrameListener extends WindowAdapter {

        public void windowClosing(WindowEvent e) {
            if (0 != save()) ;
            System.exit(0);
        }
    }

    int save() {
        return 0;
    }
}
   
 
