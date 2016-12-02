
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;
import java.sql.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
 

import javax.swing.filechooser.FileFilter;



class InternalFrameEvent0 implements InternalFrameListener {

    protected void createListenedToWindow() {
        JInternalFrame listenedToWindow = new JInternalFrame("Event Generator",
                true, //resizable
                true, //closable
                true, //maximizable
                true); //iconifiable
        listenedToWindow.setDefaultCloseOperation(
                WindowConstants.DISPOSE_ON_CLOSE);
    }
    JInternalFrame it = null;

    public void internalFrameClosing(InternalFrameEvent e) {
        it = e.getInternalFrame();
        Encryptor.parent.remove(it);
    }

    public void internalFrameClosed(InternalFrameEvent e) {
        if (it.getTitle().contains("All Course Sessions")) {
          
        }

    }

    public void internalFrameOpened(InternalFrameEvent e) {
    }

    public void internalFrameIconified(InternalFrameEvent e) {
    }

    public void internalFrameDeiconified(InternalFrameEvent e) {
    }

    public void internalFrameActivated(InternalFrameEvent e) {
    }

    public void internalFrameDeactivated(InternalFrameEvent e) {
    }

    void displayMessage(String prefix, InternalFrameEvent e) {
    }

    public void actionPerformed(ActionEvent e) {
    }
}

class FileTypeFilter extends FileFilter {

    private String extension[];
    private String description;

    public FileTypeFilter(String extension, String description) {
        this.extension = extension.split(",");
        this.description = description;
    }

    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }
        for (int j = 0; j < extension.length; j++) {
            if (file.getName().toLowerCase().endsWith(extension[j].toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public String getDescription() {
        return description + String.format(" (*%s)", extension);
    }
}

public class Encryptor extends JFrame {

    final public static String driver = "org.h2.Driver";
    public static String server = "jdbc:h2:~/test1";
    public static String dbuser = "sa";
    public static String dbpassword = "";
    public static String installFolder = "";
    public static String dataFolder = "";
    public static String filepath = "";
    public static int height = 25;
    public static String csname;
    
    protected java.util.Timer timer;
    protected long lasttime = -1;
    protected String lastcsname = "";
    protected String lastroom;
    protected long nexttime = -1;
    protected String nextcsname = "";
    protected String nextroom;
    JMenu[] menus = null;
    JMenuItem[][] menuItems = null;
    public static JDesktopPane theDesktop;
    //public static JDesktopPane theDesktop;
    JMenuBar bar = null; // create menu bar

    String tmp;
    String allsearchs = ",";
    String initfolder = "C:\\Users\\zhong\\OneDrive\\Pictures\\Saved pictures";
    static public JDBCAdapter adapter = null;

    class MenuClick implements ActionListener {

        Encryptor fa;

        public MenuClick(Encryptor fa) {
            this.fa = fa;
        }

        public void actionPerformed(ActionEvent e) {
            String name = e.getActionCommand();
            if(name.equals("Open")) Open();
            else if(name.equals("Save")) Save(); 
            else if(name.equals("Delete")) Delete();
            else if(name.equals("Information")) Information();
            else if(name.equals("Set Password")) SetPassword(); 
            else if(name.equals("About")) About();
            else if(name.equals("Reference")) Reference();
            /* 
            {
                try {
                    Class<?> c = Class.forName("Encryptor");
                    java.lang.reflect.Method method = c.getDeclaredMethod(name.replaceAll(" ", ""), null);
                    method.invoke(fa, null);
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(fa.theDesktop, name + "() is not implemented");
                }
            }*/
        }
    }
    MenuClick listener = new MenuClick(this);

    void makemenu() {
        String menustr[] = new String[]{
            "File:Open,Save,Delete,Import",
            "Database:Information,Set Password",
            "Help:About,Reference"
        };
        bar = new JMenuBar();

        menus = new JMenu[menustr.length];
        menuItems = new JMenuItem[menustr.length][];
        for (int i = 0; i < menustr.length; i++) {
            menus[i] = new JMenu(menustr[i].replaceFirst(":.*", ""));
            String[] x = menustr[i].replaceFirst(".*:", "").split(",");
            menuItems[i] = new JMenuItem[x.length];
            for (int j = 0; j < x.length; j++) {
                menuItems[i][j] = new JMenuItem(x[j]);
                menus[i].add(menuItems[i][j]);
                menuItems[i][j].addActionListener(listener);
            }
            bar.add(menus[i]);
        }
    }

    public void Open() {
         JFileChooser fc = new JFileChooser(initfolder);
        FileFilter docFilter = new FileTypeFilter(".db", "H2 database file");
        fc.setFileFilter(docFilter);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.showOpenDialog(theDesktop);
        File selFile = fc.getSelectedFile();
        
        filepath = selFile.getAbsolutePath(); 
   
        int j = filepath.lastIndexOf(File.separator);
        initfolder = filepath.substring(0, j);
        server = "jdbc:h2:file:" + filepath.replaceFirst("\\.h2\\.db$", "");
        dataFolder = initfolder;
        Encryptor.println(dataFolder);
        Encryptor.println(server);
        adapter = new JDBCAdapter(server, driver, dbuser, dbpassword);
      
        needlogin("main");
        setTitle("Encryptor: " + filepath.replace('/', File.separatorChar).replace('\\', File.separatorChar));
    }

    public void Save() {

         
        try {
            ((VSheet)  getSelectedFrame()).save();
        } catch (Exception e) {
        }
        
    }

    public void SaveAll() {
        JInternalFrame fs[] = getAllFrames();
        for (int i = 0; i < fs.length; i++) {
           
            try {
                ((VSheet) fs[i]).save();
            } catch (Exception e) {
            }
            
        }

    }

    public void Delete() {
        try {
           
            ((VSheet)  getSelectedFrame()).deleter();

        } catch (Exception e) {
        }
    }

    public void Import() {
        JFileChooser fc = new JFileChooser(initfolder);
        FileFilter docFilter = new FileTypeFilter(".db", "H2 database file");
        fc.setFileFilter(docFilter);

        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.showOpenDialog(theDesktop);
        File selFile = fc.getSelectedFile();
        if (selFile == null) {
            return;
        }
        String filepath1 = selFile.getAbsolutePath();
        int j = filepath1.lastIndexOf(File.separator);
        initfolder = filepath1.substring(0, j);

        String server = "jdbc:h2:file:" + filepath1.replaceFirst("\\.h2\\.db$", "");
        JDBCAdapter ad = new JDBCAdapter(server, driver, dbuser, dbpassword);
        if (ad.equals("")) {
            String[] tbls = new String[]{"CourseSession", "Roster", "Absence", "Detection", "Mismatch", "Querys"};
            for (int i = 0; i < 6; i++) {
                ad.copyto(adapter, tbls[i]);
            }
            ad.close();

        } else {
            
            needlogin("import");
        }
    }

     

     
     
 
    public void Information() {
        String str = "DBMS: h2\n";
        str += "File path: " + filepath + "\n";
        str += "File size: " + (new File(filepath)).length();
        JOptionPane.showMessageDialog(this, str);
    }

     
    public void SetPassword() {
        needlogin("set");
    }

    public Encryptor(String dbfile) {
        super("Encryptor:" + dbfile);
        makemenu();
        setJMenuBar(bar);
        theDesktop = new JDesktopPane();  
        
       // theDesktop = new ScrollDesktop();
        theDesktop.setLocation(new Point(0, 0));
        add(theDesktop);
        addWindowListener(new FrameListener());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 720);
        setVisible(true);
        setTitle("Encryptor: " + dbfile.replace('/', File.separatorChar).replace('\\', File.separatorChar));
        if (false)
        try{
            log = new FileWriter("log.txt",true);
            log.append("log\n");
        }catch(Exception e){}
        
    }

    String[] sessiontime(String room, char goingon) {
        int n = adapter.executeQuery("select csname, schedule from CourseSession WHERE classroom='" + room + "'");
        if (n < 1) {
            return null;
        }
        Vector<String> allslots = new Vector<String>();
        Hashtable<String, String> time2session = new Hashtable<String, String>(2);
        String alltimes = "";
        for (int i = 0; i < n; i++) {
            String csname = adapter.getValueAt(i, 0);
            String slots = adapter.getValueAt(i, 1);
            String times = toMinute(slots);
            if (times == null) {
                JOptionPane.showMessageDialog(this, err);
                continue;
            }
            String[] tmp = times.replaceFirst(",$", "").split(",");
            for (int j = 0; j < tmp.length; j++) {
                time2session.put(tmp[j], csname);
            }
            alltimes += times;

        }
        //so far   20003,32200,33330,12220,
        //          cs1, cs1, cs3,   cs4

        java.util.Date now = new java.util.Date();
        int timePassedThisWeek = now.getDay() * 24 * 60 + now.getHours() * 60 + now.getMinutes();

        String[] minutearr = alltimes.replaceFirst(",$", "").split(",");
        if (goingon == 'y') {
            for (int k = 0; k < minutearr.length; k++) {
                String[] two = minutearr[k].split(":");
                int a = Integer.parseInt(two[0]);
                int b = Integer.parseInt(two[1]);
                if (timePassedThisWeek < b && timePassedThisWeek >= a) {
                    return new String[]{two[0], time2session.get(minutearr[k])};
                }
            }
            return null;
        }
        int minindex = 0, min = Integer.MAX_VALUE - 1;
        for (int k = 0; k < minutearr.length; k++) {
            String[] two = minutearr[k].split(":");
            int a = Integer.parseInt(two[0]);
            int b = Integer.parseInt(two[1]);
            int diff = a - timePassedThisWeek;
            if (diff <= 0) {
                diff += 7 * 24 * 60;
            }
            if (diff < min) {
                min = diff;
                minindex = k;
            }
        }
        return new String[]{"" + min, time2session.get(minutearr[minindex])};
    }
    public static String err = "";

    public static String toMinute(String slot) // MWF 11:00am - 11:50 
    {
        err = "";
        int k = slot.indexOf(",");
        if (k > 0) {
            String x = toMinute(slot.substring(0, k));
            if (x == null) {
                return null;
            }
            String y = toMinute(slot.substring(k + 1));
            if (y == null) {
                return null;
            }
            return x + y;
        }
        String letters = slot.replaceAll(" ", "").toUpperCase().replaceFirst("^([A-Z]+).*", "$1");
        String digits[] = slot.replaceFirst("^[^0-9]+([0-9][0-9]?)[ ]*:[ ]*([0-9][0-9]?)[^0-9]+([0-9][0-9]?)[ ]*:[ ]*([0-9][0-9]?).*", "$1:$2:$3:$4").split(":");
        String apm[] = slot.replaceFirst("^[^0-9]+[0-9][0-9]?[ ]*:[ ]*[0-9][0-9]?([^0-9]+)[0-9][0-9]?[ ]*:[ ]*[0-9][0-9]?(.*)", " $1: $2").toLowerCase().split(":");

        String ans = "";
        for (int i = 0; i < letters.length(); i++) {
            char c = letters.charAt(i);
            int x = c == 'M' ? 1 : c == 'T' ? 2 : c == 'W' ? 3 : c == 'R' ? 4 : c == 'F' ? 5 : c == 'S' ? 6 : c == 'U' ? 0 : 7;

            if (digits.length != 4) {
                err = "Invalid time format:" + slot + ". Use [UMTWRFS]hh:mm[am|pm]-hh:mm[am|pm]";
                return null;
            }
            if (x == 7) {
                err = "Invalid letter for day of a week:" + c;
                return null;
            }
            x *= 24 * 60;
            int y = x;
            int t = Integer.parseInt(digits[0].replaceFirst("^0", ""));
            if (t > 23 || t > 12 && apm[0].contains("pm") || t < 0) {
                err = "Invalid time format:" + slot + ". Use [UMTWRFS]hh:mm[am|pm]-hh:mm[am|pm]";
                return null;
            }
            x += t * 60;

            t = Integer.parseInt(digits[1].replaceFirst("^0", ""));
            if (t >= 60) {
                err = "Invalid time format:" + slot + ". Use [UMTWRFS]hh:mm[am|pm]-hh:mm[am|pm]";
                return null;
            }
            x += t;
            if (apm[0].contains("pm")) {
                x += 12 * 60;
            }
            t = Integer.parseInt(digits[2].replaceFirst("^0", ""));
            if (t > 23 || t > 12 && apm[0].contains("pm") || t < 0) {
                err = "Invalid time format:" + slot + ". Use [UMTWRFS]hh:mm[am|pm]-hh:mm[am|pm]";
                return null;
            }
            y += t * 60;
            t = Integer.parseInt(digits[3].replaceFirst("^0", ""));
            if (t >= 60) {
                err = "Invalid time format:" + slot + ". Use [UMTWRFS]hh:mm[am|pm]-hh:mm[am|pm]";
                return null;
            }
            y += t;
            if (apm[1].contains("pm")) {
                y += 12 * 60;
            }
            ans += x + ":" + y + ",";
        }
        return ans;    // 121203,34343,343435
    }

     

     

    
 

    class FrameListener extends WindowAdapter {

        public void windowClosing(WindowEvent e) {
            adapter.close();
            if (log!=null)
            try{ log.close();}catch(Exception e1){}
            System.exit(0);
        }
    }

    void getSession() {
        
        JInternalFrame ji =  getSelectedFrame();
         
        String sn = "";
        if (ji!=null)
            sn = ji.getClass().getName();
       
         
       
    }

     
 
    void needlogin(String whichdb0) {
        Login lg;
        if (whichdb0.equals("set")) {
            whichdb = "set";
            lg = new Login(this, null, null, dbuser, null);

        } else {
            whichdb = whichdb0;
            lg = new Login(this, server, driver, dbuser, dbpassword);

        }
        addTheFrame(lg);
    }

     

     
    public static Hashtable<JInternalFrame, JInternalFrame> parent = new Hashtable<JInternalFrame, JInternalFrame>();

    static public JInternalFrame[] getAllFrames() {
        int n = parent.size();
        int j = 0;
        JInternalFrame[] x = new JInternalFrame[n];
        for (Enumeration e = parent.keys(); e.hasMoreElements();) {
            x[j++] = (JInternalFrame) e.nextElement();
        }
        return x;
    }
    static JInternalFrame getSelectedFrame()
    {
        for (Enumeration e = ffs.keys(); e.hasMoreElements();)
        {
            JInternalFrame  f = (JInternalFrame)e.nextElement();
            if (ffs.get(f).equals("1"))
                return f;
        }
        return null;
    }
    static InternalFrameEvent0 yy = new InternalFrameEvent0();
    static public Hashtable<JInternalFrame,String> ffs = new Hashtable<JInternalFrame,String>();
    public static void addTheFrame(JInternalFrame newFrame) {
        JInternalFrame f =  getSelectedFrame();
        newFrame.pack();
        theDesktop.add(newFrame );
        newFrame.setVisible(true);
        newFrame.addInternalFrameListener(yy);
        if (f!=null)
        ffs.put(f, "0" );
        ffs.put(newFrame, "1" );
        newFrame.addFocusListener(new FocusListener(){
        public void focusLost(FocusEvent e){
            JInternalFrame f =  (JInternalFrame)e.getSource();
            ffs.put(f,"0");
        } 
        public void focusGained(FocusEvent e){
          JInternalFrame f =  (JInternalFrame)e.getSource();
          ffs.put(f,"1"); 
        }}
        );
        try {
            newFrame.setSelected(true);
            newFrame.setResizable(true);
            newFrame.setVisible(true);
            parent.put(newFrame, f);
        } catch (Exception e) {
        }
    }

    public static void showmsg(JInternalFrame t, Object obj) {
        JOptionPane.showMessageDialog(t.getDesktopPane(), obj);
    }

    public static void closetitle(String nm) {
        JInternalFrame fs[] = getAllFrames();
        for (int i = 0; i < fs.length; i++) {
            if (fs[i].getTitle().equals(nm)) {
                theDesktop.remove(i);
                parent.remove(fs[i]);
            }
        }

    }

    public static int compare(Object data[][], String[] datatypes, Integer r1, Integer r2, int col, int direct) {
        Object obj = data[r1.intValue()][col];
        if (obj == null) {
            return -1;
        }
        try {
            if (((String) obj).equals("")) {
                return -1;
            }
        } catch (Exception e) {
        }
        if (datatypes[col].indexOf("long") >= 0) {

            Long L1 = (Long) data[r1.intValue()][col];
            Long L2 = (Long) data[r2.intValue()][col];
            long i1 = L1.longValue();
            long i2 = L2.longValue();
            if (i1 == i2) {
                return direct * (r1.intValue() - r2.intValue());
            }
            return direct * ((int) Math.round(i1 - i2));
        } else if (datatypes[col].indexOf("double") >= 0) {
            double i1 = ((Double) (data[r1.intValue()][col])).doubleValue();
            double i2 = ((Double) (data[r2.intValue()][col])).doubleValue();
            if (i1 == i2) {
                return direct * (r1.intValue() - r2.intValue());
            }
            return direct * ((int) Math.round(i1 - i2));
        } else if (datatypes[col].indexOf("float") >= 0) {
            float i1 = ((Float) (data[r1.intValue()][col])).floatValue();
            float i2 = ((Float) (data[r2.intValue()][col])).floatValue();
            if (i1 == i2) {
                return direct * (r1.intValue() - r2.intValue());
            }
            return direct * ((int) Math.round(i1 - i2));
        } else if (datatypes[col].indexOf("integer") >= 0) {
            int i1 = ((Integer) (data[r1.intValue()][col])).intValue();
            int i2 = ((Integer) (data[r2.intValue()][col])).intValue();
            if (i1 == i2) {
                return direct * (r1.intValue() - r2.intValue());
            }
            return direct * ((int) Math.round(i1 - i2));
        } else if (datatypes[col].indexOf("blob") >= 0) {
            return direct * (r1.intValue() - r2.intValue());

        } else {
            if (data[r1.intValue()][col]==null) return -direct;
            else if (data[r2.intValue()][col]==null) return  direct;
            String s1 =   (data[r1.intValue()][col].toString());
            String s2 =   (data[r2.intValue()][col].toString());
            return direct * (s1.compareToIgnoreCase(s2));
        }
    }

    String whichdb = "main";

    public void changepassword(String user, String pass) {
        adapter.executeUpdate("ALTER USER " + user + " SET PASSWORD'" + pass.replaceAll("'", "''") + "'");
        Encryptor.println(adapter.error());
        dbpassword = pass;
    }

    public boolean login(String server1, String driver1, String user, String pass) {
        JDBCAdapter adapter1 = new JDBCAdapter(server1, driver1, user, pass);
        if (adapter.error().equals("")) {
            if (whichdb.equals("main")) {
                dbuser = user;
                dbpassword = pass;
            
                adapter = adapter1;
            } else {
                String[] tbls = new String[]{"CourseSession", "Roster", "Absence", "Detection", "Mismatch", "Querys"};
                for (int i = 0; i < 6; i++) {
                    adapter1.copyto(adapter, tbls[i]);
                }
                adapter1.close();
            }
            return true;
        }
        return false;
    }

    public static void main(String args[]) {
        String file = null;
        installFolder = System.getProperty("user.dir").replace('\\', '/').replaceFirst("/$", "");
        String dbfile = "";
        if (args.length > 0 && args[0].endsWith("h2.db")) {
            filepath = args[0];
            dbfile = args[0];
            server = "jdbc:h2:file:" + args[0].replaceFirst(".h2.db$", "").replace('\\', '/');
            int j = args[0].lastIndexOf(File.separator);
            dataFolder = args[0].substring(0, j);
            Encryptor.println(dataFolder);
            Encryptor.println(server);
        } else {
            dataFolder = System.getProperty("user.dir").replace('\\', '/').replaceFirst("/$", "");
            Encryptor.println(dataFolder);
            server = "jdbc:h2:file:" + dataFolder + "/passwords";
            dbfile = dataFolder + File.separator + "passwords";
            Encryptor.println(server);
        }
        adapter = new JDBCAdapter(server, driver, dbuser, dbpassword);
        adapter.needMetaInfo = true;
        Encryptor.println(adapter.error());
        new Encryptor(dbfile);
        VSheet vs = new VSheet("create table Secrets(Account varchar(30), Username varchar(30), Password varchar(30), PRIMARY KEY(Account,Username))");
        addTheFrame(vs);
        

    } // end mai
    
    public void About()
    {
        String str = "            Encryptor  \n\nAuthor: Zhongyan Lin\n               Delaware State University\nVeriosn: 1.0\nLicense:  GNU General Public License";
        JOptionPane.showMessageDialog(this, str);
    }
    
    public void Reference()
    {
        String str = "The implementation of this software has used the folloing libraries:\n1. H2 database: http://www.h2database.com\n2. Face detection:  http://www.opencv.org\n3. Face Recognition: http://vismod.media.mit.edu/vismod/demos/facerec/\n4. Webcam capture: http://webcam-capture.sarxos.pl/\n5: JScrollPane: http://jscroll.sourceforge.net/";
        JOptionPane.showMessageDialog(this, str);
    }
    static FileWriter log = null;
    static public void print(String x)
    {
        if(log==null) System.out.print(x); else
        try{ log.append(x); }catch(Exception e){}
    }
    static public void println(String x)
    {
        if(log==null) System.out.println(x); else
        try{ log.append(x + "\n"); }catch(Exception e){}
    }
     static public void print(int x)
    {
        if(log==null) System.out.print(x); else
        try{ log.append(""+x); }catch(Exception e){}
    }
    static public void println(int x)
    {
        if(log==null) System.out.println(x); else
        try{ log.append(x + "\n"); }catch(Exception e){}
    }
     static public void print(long x)
    {
        if(log==null) System.out.print(x); else
        try{ log.append(""+x); }catch(Exception e){}
    }
    static public void println(long x)
    {
        if(log==null) System.out.println(x); else
        try{ log.append(x + "\n"); }catch(Exception e){}
    }
}
