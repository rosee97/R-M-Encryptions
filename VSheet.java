 

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.sql.*;

public class VSheet extends JInternalFrame {

    public String tablename;
    public int NUMROWS;
    public int numRows;
    private int extraline = 6;
    public int numCols;
    public String[] fields;
    public String[] datatypes;
    public String[] defaultvalues;
    public boolean[] notnull;
    public String[] keys = null;
    private String keystr = "";

    public String wherekey[];
    public int numRecords = 0;
    public Object[][] data;
    TableModel dataModel;
    int rowinediting = 0;
    int colinediting = 0;
    JTable table = null;
    final String filename = "myclass";
    JInternalFrame opener = null;
    private int W = 0;
    JDBCAdapter adapter = Encryptor.adapter;

    String pad(String s, int mx) {
        for (int i = s.length(); i < mx; i++) {
            s += ' ';
        }
        return s;
    }

    class FrameListener extends WindowAdapter {

        public void windowClosing(WindowEvent e) {
            if (0 != save()) ;
            System.exit(0);
        }
    }

    int save() {
        if (keys == null) {
            return 0;
        }

        int n1 = 0;
        for (int i = 0; i < NUMROWS; i++) {
            if (readTextAt(i, 0).equals("")) {
                continue;
            }
            String sql = "";
            String vk = "";
            for (int j = 0; j < numCols - 1; j++) {
                String q = "'";
                sql += ",";
                boolean iskey = keystr.contains("," + fields[j].toLowerCase() + ",");
                if (iskey) {
                    vk += "AND " + fields[j] + "=";
                }
                if (datatypes[j].indexOf("varchar") < 0) {
                    q = "";
                    if (iskey) {
                        vk += readTextAt(i, j).replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\");
                    }
                } else if (iskey) {
                    vk += "'" + readTextAt(i, j).replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\") + "'";
                }

                if (readTextAt(i, j).trim().equals("")) {
                    if (q.equals("")) {
                        sql += "NULL";
                    } else {
                        sql += "''";
                    }
                } else {
                    sql += q + readTextAt(i, j).replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\") + q;
                }

            }
            sql = "INSERT INTO " + tablename + " values (" + sql.substring(1) + ")";

            try {

                int nn = adapter.executeUpdate(sql);
                Encryptor.println(sql + nn + adapter.error());
                if (nn > 0) {
                    n1 += nn;
                    wherekey[i] = vk.replaceFirst("AND", "WHERE");
                }

            } catch (Exception e1) {
            }

            sql = "";
            for (int j = 0; j < numCols - 1; j++) {
                String x = readTextAt(i, j).replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\");
                if (!x.equals("")) {
                    String q = "'";
                    if (datatypes[j].indexOf("varchar") < 0) {
                        q = "";
                    }
                    if (q.equals("") && readTextAt(i, j).trim().equals("")) {
                        sql += fields[j] + "=NULL";
                    } else {
                        sql += fields[j] + "=" + q + x + q;
                    }
                    sql += ",";

                }

            }
            String password = readTextAt(i,2);
            if (!sql.equals("")) {
                sql = "UPDATE " + tablename + " SET " + sql.replaceFirst("Password='[^']*'", "Password='" + Encryption.encrypt(password) + "'") + wherekey[i];
            }
            if (!sql.equals("")) {
                try {
                    Encryptor.println(sql);
                    int nn = adapter.executeUpdate(sql);
                    Encryptor.println(sql + nn + adapter.error());
                } catch (Exception e1) {
                }
            }
        }
        return 0;
    }

    void deleter() {
        if (keys == null) {
            return;
        }
        int n1 = 0;
        for (int i = 0; i < NUMROWS; i++) {
            if (((Boolean) (data[i][numCols - 1])).booleanValue() == false) {
                continue;
            }

            String sql = "DELETE FROM " + tablename + " " + wherekey[i];
            try {
                int nn = adapter.executeUpdate(sql);
                if (nn > 0) {
                    n1 += nn;
                    for (int j = 0; j < numCols; j++) {
                        if (datatypes[j].indexOf("integer") >= 0) {
                            table.setValueAt(new Integer(-1), i, j);
                        } else if (datatypes[j].indexOf("long") >= 0) {
                            table.setValueAt(new Long(-1), i, j);
                        } else if (datatypes[j].indexOf("boolean") >= 0) {
                            table.setValueAt(new Boolean(false), i, j);
                        } else if (datatypes[j].indexOf("varchar") >= 0) {
                            table.setValueAt("", i, j);
                        }
                    }
                }
            } catch (Exception e1) {
            }
        }
    }

    public void addRow(Object[] record) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        model.addRow(record);
    }
    int[] direct = null;

    void sortby(int col) {

        int n = NUMROWS - extraline;
        if (direct == null) {
            direct = new int[numCols + 1];
        }
        if (direct[col] == 0) {
            direct[col] = 1;
        } else {
            direct[col] = -direct[col];
        }
        if (col == numCols - 1) {
            Boolean b = new Boolean(false);
            if (direct[col] == -1) {
                b = new Boolean(true);
            }
            for (int i = 0; i < n; i++) {
                table.setValueAt(b, i, col);
            }
            return;
        }
        Integer rows[] = new Integer[n];
        for (int i = 0; i < n; i++) {
            rows[i] = new Integer(i);
        }
        Arrays.sort(rows, new RowComparator(col, this));
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = rows[i].intValue();
            Encryptor.println(a[i]);
        }

        Object[][] newd = new Object[n][];
        for (int i = 0; i < n; i++) {
            newd[i] = new Object[data[i].length];
            for (int j = 0; j < data[i].length; j++) {
                newd[i][j] = data[i][j];

            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < numCols; j++) {
                table.setValueAt(newd[a[i]][j], i, j);
            }

        }

    }

    void fromcreate(String SQL) {
        String[] y = SQL.replaceFirst("[^\\(]+\\(", "").replaceFirst("(?i),[ |\n|\r|\t]*PRIMARY[ |\n|\r|\t]KEY.*", "").split(",");
        int numCols1 = y.length;
        String fields0[] = new String[y.length];
        String[] datatypes0 = new String[y.length];
        defaultvalues = new String[y.length];
        notnull = new boolean[y.length + 1];
        String z[] = SQL.replaceAll("[ |\t|\n|\r]+", " ").trim().split(" ");
        String tablename0 = z[2].replaceFirst("\\(.*", "");
        for (int i = 0; i < y.length; i++) {
            defaultvalues[i] = y[i].replaceFirst("^[^']+'", "").replaceFirst("'[^']*$", "").replaceAll("''", "'");
            if (defaultvalues[i].equals(y[i])) {
                defaultvalues[i] = "";
            }
            y[i] = y[i].replaceAll("'[^']*'", "").replaceAll("[ |\t|\n|\t]+", " ").trim();
            String yi = y[i].replaceFirst("(?i)not null", "");
            notnull[i] = (!yi.equals(y[i]));

            fields0[i] = y[i].replaceFirst(" .*$", "");
            int k = fields0[i].length();
            if (k < y[i].length()) {
                y[i] = y[i].substring(k).trim();
                datatypes0[i] = y[i].replaceFirst(" .*$", "");
                k = datatypes0[i].length();
                if (k < y[i].length()) {
                    y[i] = y[i].substring(k).trim();
                    k = y[i].indexOf("(");
                    if (k >= 0) {
                        int j = y[i].indexOf(")", k);
                        datatypes0[i] = "varchar" + y[i].substring(k, j + 1);
                    }
                }

            }
        }
        String x = SQL.replaceFirst("(?i).*,[ |\n|\r|\t]*PRIMARY[ |\n|\r|\t]KEY", "");
        int k = x.indexOf("(");
        int j = -1;
        if (k >= 0) {
            j = x.indexOf(")", k);
        }
        String key0[];
        if (j > -1) {
            key0 = x.substring(k + 1, j).split("[ |\n|\r|\t]*,[ |\n|\r|\t]*");
        } else {
            key0 = new String[]{"key"};
        }
        maketable(tablename0, fields0, datatypes0, key0);
        setTitle(tablename0);
    }
    String whereclause = null;

    public VSheet(String SQL) {
        super(" ", true, true, true, true);
        adapter = Encryptor.adapter;
        if (SQL.toLowerCase().startsWith("create table")) {
            fromcreate(SQL);
        } else if (SQL.replaceAll("'[^']*'", "").contains("*")) {
            int k = SQL.toLowerCase().replaceAll("\\s", " ").indexOf(" from ");

            tablename = SQL.toLowerCase().replaceAll("\\s", " ").substring(k + 6).replaceFirst(" .*", "").trim();

            String x = adapter.tabledef(tablename, adapter.dbms);
            if (x == null || x.equals("")) {
                JOptionPane.showInternalMessageDialog(this, "Wrong SQL: " + SQL);
            } else {
                whereclause = SQL.replaceFirst("(?i).*(\\swhere\\s.*$)", "$1");
                if (SQL.equals(whereclause)) {
                    whereclause = null;
                }
                fromcreate(x);
            }
        } else {
            selectSQL = SQL;

            adapter.executeQuery2(SQL, true);

            String[] datatypes0 = new String[adapter.getColumnCount()];
            for (int i = 0; i < adapter.getColumnCount(); i++) {
                datatypes0[i] = adapter.datatypes[i];

                if (datatypes0[i].contains("char")) {
                    try {
                        datatypes0[i] += "(" + adapter.metaData.getColumnDisplaySize(i + 1) + ")";
                    } catch (Exception e) {
                    }
                } else if (datatypes0[i].contains("bigint")) {
                    datatypes0[i] = "long";
                } else if (datatypes0[i].contains("blob")) {
                    datatypes0[i] = "blob";
                }
            }
            maketable("query", adapter.columnNames, datatypes0, null);
            setTitle(SQL.substring(0, SQL.length() > 50 ? 50 : SQL.length()));

        }
    }
    String selectSQL = null;

    public VSheet(String tablename, String fields[], String datatypes[], String keys[]) {
        super(tablename, true, true, true, true);
        maketable(tablename, fields, datatypes, keys);
    }

    String pt(String s[]) {
        String str = "";
        if (s != null) {
            for (int i = 0; i < s.length; i++) {
                str += "  " + s[i];
            }
        }
        return str;
    }

    JPanel imgpane = null;
    JTextArea textarea = null;
    JLabel icp = null;

    void showpic(int i, int j) {
        if (!datatypes[j].contains("blob") && textarea != null) {
            textarea.setText(texts.elementAt(i));
        } else if (icp != null && datatypes[j].contains("blob")) {
            icp.setIcon(images.elementAt(i));

        } else if (datatypes[j].contains("blob") && icp == null) {
            icp = new JLabel(images.elementAt(i));
            imgpane.add(icp);
        } else if (!datatypes[j].contains("blob") && textarea == null) {
            textarea = new JTextArea(texts.elementAt(i));
            textarea.setSize(200, NUMROWS * Encryptor.height - 10);
            imgpane.add(textarea);
        }
    }
    String bigColumns = ",";

    private void maketable(String tablename, String fields[], String datatypes[], String keys[]) {
        Encryptor.println(pt(fields));
        Encryptor.println(pt(datatypes));
        Encryptor.println(pt(keys));
        if (fields.length != datatypes.length) {
            Encryptor.println("fields.length!=datatypes.length");
        }
        this.numCols = fields.length + 1;
        this.tablename = tablename;
        this.datatypes = new String[numCols];
        this.fields = new String[numCols];
        if (keys != null) {
            this.keys = new String[keys.length];
            for (int i = 0; i < keys.length; i++) {
                this.keys[i] = keys[i];
            }
        }
        final String colnames[] = new String[numCols];
        final String datp[] = new String[numCols];
        for (int i = 0; i < numCols - 1; i++) {
            this.datatypes[i] = datatypes[i].toLowerCase();
            Encryptor.println("datatypes[" + i + "]=" + datatypes[i]);
            this.fields[i] = fields[i].replaceAll(" ", "");
            colnames[i] = fields[i];
            datp[i] = this.datatypes[i];
        }
        datp[numCols - 1] = this.datatypes[numCols - 1] = "boolean";
        this.fields[numCols - 1] = "Mark";

        final int widths[] = new int[numCols];
        colnames[numCols - 1] = "Mark";
        for (int i = 0; i < numCols; i++) {
            widths[i] = 50;
            if (this.datatypes[i].contains("varchar")) {
                widths[i] = 6 * Integer.parseInt(this.datatypes[i].replaceAll("[^0-9]", ""));
            }
            if (widths[i] > 200) {
                widths[i] = 200;
            }
            if (widths[i] < 70) {
                widths[i] = 70;
            }
            Encryptor.println(widths[i]);
        }

        final long t = System.currentTimeMillis();
        final String randt = ("" + (t % 24) + ":" + (t % 60));
        data = mkdata();
        if (data == null) {
            return;
        }
        dataModel = new AbstractTableModel() {
            public int getColumnCount() {
                return numCols;
            }

            public int getRowCount() {
                if (data == null) {
                    return 0;
                }
                return data.length;
            }

            public Object getValueAt(int row, int col) {
                return data[row][col];
            }

            public String getColumnName(int column) {
                if (column < numCols) {
                    return colnames[column];
                } else {
                    return "";
                }
            }

            public Class getColumnClass(int column) {
                if (datp[column].contains("varchar")) {
                    return String.class;
                } else if (datp[column].equals("integer")) {
                    return Integer.class;
                } else if (datp[column].equals("long") || datp[column].equals("bigint")) {
                    return Long.class;
                } else if (datp[column].equals("double")) {
                    return Double.class;
                } else if (column == numCols - 1) {
                    return Boolean.class;
                } else {
                    return String.class;
                }
            }
            // public Class getColumnClass(int col) {return getValueAt(0,col).getClass();}

            public boolean isCellEditable(int row, int col) {
                return !bigColumns.contains("," + col + ",");
            }

            public void setValueAt(Object aValue, int row, int column) {
                data[row][column] = aValue;
                fireTableCellUpdated(row, column);
                fireTableDataChanged();
            }
        };

        final String tips[] = fields;
        if (data != null) {
            table = new JTable(dataModel) {
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {

                    Component c = super.prepareRenderer(renderer, row, column);
                   // bigColumns = ",2,";
                    if (column == 2) {
                        c.setForeground(Color.blue);
                        
                    } else {
                        c.setForeground(Color.black);
                    }
                    return c;
                }

                protected JTableHeader createDefaultTableHeader() {
                    return new JTableHeader(columnModel) {
                        public String getToolTipText(MouseEvent e) {
                            String tip = null;
                            java.awt.Point p = e.getPoint();
                            int index = columnModel.getColumnIndexAtX(p.x);
                            int realIndex = columnModel.getColumn(index).getModelIndex();
                            return tips[realIndex];
                        }
                    };

                }

            };
        }
        if (table != null) {
            table.getTableHeader().addMouseListener(
                    new MouseAdapter() {
                boolean direct = false;

                public void mouseClicked(MouseEvent e) {
                    int col = table.columnAtPoint(e.getPoint());
                    String name = table.getColumnName(col);
                    Encryptor.println("sort by  " + col + " " + name);
                    sortby(col);
                }
            });
        }

        table.setFont(new Font("Arial", Font.PLAIN, 14));
        TableColumnModel columnModel = table.getColumnModel();

        table.setRowHeight(Encryptor.height);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int wd = 0;
        for (int i = 0; i < numCols; i++) {
            wd += widths[i];
            TableColumn columni = columnModel.getColumn(i);
            columni.setPreferredWidth(widths[i]);
        }
        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.lightGray);
        header.setPreferredSize(new Dimension(wd, Encryptor.height + 3));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollpane = new JScrollPane(table);
        int hh = (NUMROWS + 1) * (  Encryptor.height) + 6;
        
        scrollpane.setPreferredSize(new Dimension(wd, hh));
        scrollpane.setViewportView(table);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row < 0) {
                    return;
                }
                int col = table.getSelectedColumn();
                if(col!=2){
                  //do nothing
                }
            }
        }
        );

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        Container content = getContentPane();
        content.setLayout(gridbag);
        c.fill = GridBagConstraints.VERTICAL;

        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        gridbag.setConstraints(scrollpane, c);

        content.add(scrollpane);

        if (images.size() > 0 || texts.size() > 0) {

            if (texts.size() != 0 && images.size() == 0) {
                W = 200;
            } else if (texts.size() != 0 && images.size() != 0 && W < 200) {
                W = 200;
            }
            imgpane = new JPanel();
            imgpane.setPreferredSize(new Dimension(W, NUMROWS * Encryptor.height));
            c.gridx = 1;
            c.gridy = 0;
            gridbag.setConstraints(imgpane, c);
            content.add(imgpane);
        }

        // Container content = frame.getContentPane();
        if (!tablename.equals("query")) {

            JPanel buttonson = new JPanel();

            JButton choser = new JButton("Save");
            choser.setSize(320, 25);
            buttonson.add(choser);

            JButton update = new JButton("Delete");
            update.setSize(320, 25);
            buttonson.add(update);

            c.gridx = 0;
            c.gridy = 1;
            if (images.size() > 0 || texts.size() > 0) {
                c.gridwidth = 2;
            }
            gridbag.setConstraints(buttonson, c);
            content.add(buttonson);

            ButtonListener actionlistener = new ButtonListener(table);
            choser.addActionListener(actionlistener);

            update.addActionListener(actionlistener);
        }
        setSize(wd + W + 10, 550);
        setVisible(true);
       
    }

    public String readTextAt(int i, int j) {
        if (i >= data.length || j >= data[i].length) {
            return "";
        }
        if (datatypes[j].toLowerCase().indexOf("varchar") >= 0) {
            if (data[i][j] != null) {
                return (String) data[i][j];
            }
            return "";
        } else if (datatypes[j].toLowerCase().indexOf("integer") >= 0) {
            try {
                Integer q = (Integer) (data[i][j]);
                if (q == null) {
                    return "";
                }
                return "" + q.intValue();
            } catch (Exception e) {
                return "";
            }
        } else if (datatypes[j].toLowerCase().indexOf("long") >= 0) {
            try {
                Long q = (Long) (data[i][j]);
                if (q == null) {
                    return "";
                }
                return "" + q.longValue();
            } catch (Exception e) {
                return "";
            }
        } else if (datatypes[j].toLowerCase().indexOf("double") >= 0) {
            try {
                Double q = (Double) (data[i][j]);
                if (q == null) {
                    return "";
                }
                return "" + q.doubleValue();
            } catch (Exception e) {
                return "";
            }
        } else if (datatypes[j].toLowerCase().indexOf("boolean") >= 0) {
            try {
                Boolean q = (Boolean) (data[i][j]);
                if (q == null) {
                    return "";
                }
                return "" + q.booleanValue();
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }

    class ButtonListener implements ActionListener {

        JTable table;

        public ButtonListener(JTable tbl) {
            table = tbl;
        }

        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd.equals("Delete")) {
                deleter();

            } else if (cmd.equals("Save")) {
                save();
            }

        }

    }

    public static void main2(String[] args) {
        // VSheet vf =  new VSheet("Job" ,new String[]{"Location", "Skills", "Position", "Salary"}, new String[]{"varchar(30)","varchar(30)","varchar(30)","integer" } );
        //vf.table.setValueAt("AAAA", 1, 1) ;      
    }

    Vector<ImageIcon> images = null;
    Vector<String> texts = null;

    String makeCreateSQL() {
        String sql = "";
        keystr = ",";
        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                keystr += keys[i].toLowerCase() + ",";
            }
            sql = "CREATE TABLE " + tablename + "(";
            for (int i = 0; i < numCols - 1; i++) {
                sql += fields[i] + " " + datatypes[i] + ",";
            }
            sql += "PRIMARY KEY(" + keystr.substring(1, keystr.length() - 1) + "))";

        }
        return sql;
    }

    void makeKeyString() {
        keystr = ",";
        if (keys != null) {
            for (int i = 0; i < keys.length; i++) {
                keystr += keys[i].toLowerCase() + ",";
            }
        }
    }

    String makeSelectSQL() {
        String sql2 = "SELECT ";
        for (int i = 0; i < numCols - 1; i++) {
            sql2 += fields[i] + ",";
        }
        sql2 = sql2.replaceFirst(",$", "") + " FROM " + tablename;
        return sql2;
    }

    public Object[][] mkdata() {

        String sql0 = makeCreateSQL();
        makeKeyString();
        String sql2 = makeSelectSQL();

        if (selectSQL != null) {
            extraline = 0;
        } else {
            if (true) {
                adapter.executeUpdate(sql0);
            }
            Encryptor.println(sql0);
            if (whereclause != null) {
                sql2 += whereclause;
            }
            adapter.executeQuery2(sql2, true);
        }
        Vector<Object[]> objv = new Vector<Object[]>();
        numCols = adapter.getColumnCount() + 1;
        images = new Vector<ImageIcon>();
        texts = new Vector<String>();
        Vector<String> wherekeyv = new Vector<String>();

        for (int i = 0; true; i++) {
            if (i == 0) {
                try {
                    if (!adapter.resultSet.first()) {
                        break;
                    }
                } catch (Exception e) {
                    break;
                }
            } else {
                try {
                    if (!adapter.resultSet.next()) {
                        break;
                    }
                } catch (Exception e) {
                    break;
                }
            }
            String kv = "";
            Object[] tt = new Object[numCols];
            int nullcount = 0;

            for (int j = 0; j < numCols - 1; j++) {
                boolean iskey = keystr.contains("," + fields[j].toLowerCase() + ",");

                if (iskey) {
                    kv += "AND " + fields[j] + "=";
                }
                if (adapter.datatypes[j].toLowerCase().equals("text")) {
                    String str = null;
                    try {
                        str = adapter.resultSet.getString(j + 1);
                        texts.addElement(str);
                        tt[j] = "t" + (texts.size());

                        bigColumns += j + ",";
                    } catch (Exception e) {
                        nullcount++;
                    }
                } else if (adapter.datatypes[j].toLowerCase().equals("blob")) {

                    try {
                        Blob b = adapter.resultSet.getBlob(j + 1);
                        InputStream is = b.getBinaryStream();
                        byte buf[] = new byte[(int) b.length()];
                        is.read(buf);
                        is.close();
                        ImageIcon x = new ImageIcon(buf);
                        int w = x.getIconWidth();
                        if (w > W) {
                            W = w;
                        }
                        images.addElement(x);
                        tt[j] = "m" + (images.size());
                    } catch (Exception e) {
                        nullcount++;
                    }
                    bigColumns += j + ",";

                } else if (datatypes[j].contains("varchar")) {
                    String str = null;
                    try {
                        str = adapter.resultSet.getString(j + 1);
                        if (iskey) {
                            kv += "'" + str.replaceAll("'", "''") + "'";
                        }

                    } catch (Exception e) {
                        if (iskey) {
                            kv += "NULL";
                        }
                        nullcount++;
                    }
                    tt[j] = str;
                } else if (datatypes[j].equals("double")) {
                    double str = 0.0;
                    try {
                        str = adapter.resultSet.getDouble(j + 1);
                        if (iskey) {
                            kv += "" + str;
                        }

                    } catch (Exception e) {
                        if (iskey) {
                            kv += "NULL";
                        }
                        nullcount++;
                    }
                    tt[j] = new Double(str);
                } else if (datatypes[j].equals("integer")) {
                    int str = 0;
                    try {
                        str = adapter.resultSet.getInt(j + 1);
                        if (iskey) {
                            kv += str;
                        }

                    } catch (Exception e) {
                        if (iskey) {
                            kv += "NULL";
                        }
                        nullcount++;
                    }
                    tt[j] = new Integer(str);
                } else if (datatypes[j].equals("long") || datatypes[j].equals("bigint")) {
                    long str = 0;
                    try {
                        str = adapter.resultSet.getLong(j + 1);
                        if (iskey) {
                            kv += str;
                        }

                    } catch (Exception e) {
                        if (iskey) {
                            kv += "NULL";
                        }
                        nullcount++;
                    }
                    tt[j] = new Long(str);
                }

            }
            tt[numCols - 1] = new Boolean(true);
            objv.addElement(tt);
            wherekeyv.addElement(kv.replaceFirst("AND", " WHERE"));
        }
        NUMROWS = objv.size() + extraline;
        data = new Object[NUMROWS][];
        wherekey = new String[NUMROWS];
        for (int i = 0; i < NUMROWS - extraline; i++) {
            data[i] = objv.elementAt(i);
            wherekey[i] = wherekeyv.elementAt(i);
            Encryptor.println(wherekey[i]);
        }
        for (int i = NUMROWS - extraline; i < NUMROWS; i++) {
            data[i] = new Object[numCols];
            for (int j = 0; j < numCols - 1; j++) {

                if (datatypes[j].indexOf("varchar") >= 0) {
                    data[i][j] = defaultvalues[j];
                } else if (datatypes[j].indexOf("double") >= 0) {
                    double x = 0.0;
                    try {
                        x = Double.parseDouble("" + defaultvalues[j]);
                    } catch (Exception e) {
                    }
                    data[i][j] = new Double(x);
                } else if (datatypes[j].indexOf("integer") >= 0) {
                    int x = 0;
                    try {
                        x = Integer.parseInt("" + defaultvalues[j]);
                    } catch (Exception e) {
                    }
                    data[i][j] = new Integer(x);
                } else if (datatypes[j].indexOf("long") >= 0) {
                    long x = 0;
                    try {
                        x = Long.parseLong("" + defaultvalues[j]);
                    } catch (Exception e) {
                    }
                    data[i][j] = new Long(x);
                }
            }
            data[i][numCols - 1] = new Boolean(false);
        }

        return data;
    }
}

class RowComparator implements Comparator<Integer> {

    int col = 0;
    VSheet vs;

    public RowComparator(int c, VSheet x) {
        col = c;
        vs = x;
    }

    public int compare(Integer r1, Integer r2) {
        return Encryptor.compare(vs.data, vs.datatypes, r1, r2, col, vs.direct[col]);
    }
}
