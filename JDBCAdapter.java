 

import java.io.*;
import java.sql.*;
import java.util.*;
// import com.microsoft.jdbc.sqlserver.SQLServerDriver;
import java.io.*;
import java.sql.*;
import java.util.*;
//import oracle.jdbc.driver.OracleDriver;
 

 

public class JDBCAdapter {
    public Connection connection = null;
    Statement statement = null;
    public ResultSet resultSet = null;
    public ResultSetMetaData metaData = null;
    DatabaseMetaData dmd = null;
    StringBuffer errormsg = new StringBuffer(150);
    boolean debug = true;
    String dburl = null;
    public String[] columnNames = new String[0];
    public String[][] rows = null;
    public boolean[] colIsNum = new boolean[0];
    public int[] colSizes = new int[0];
    public int[] colNullable = new int[0];
    public String[] datatypes = new String[0];
    private int numberOfColumns = 0;
    int numberOfRows = 0;
    PrintStream ps = null;
    PrintWriter pw = null;
    
    char output;
    public String dbms = "access";
    public String status = "broken";
    public int cursor = 0;
    public boolean needMetaInfo = false;
    public String server;
    public static TreeSet<String> dburls = new TreeSet<String>();
    byte mode = 0;
    
     
    public static int maxrows(String format) {
        if (format.equals("Table") || format.equals("LognForm")) {
            return 150;
        }
        return 65535;
    }
    static public void main1(String [] args)
    {
      String d = "sun.jdbc.odbc.JdbcOdbcDriver";
       String s = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=C:/project/gfc/dbFileFolder/tlm.mdb";
        String u = "";
        String p = "";
        JDBCAdapter adapter = new JDBCAdapter(s,d,u,p );
        adapter.executeQuery("select * from AppUser");
        adapter.close();
    }
    public static void main(String[] args) {
        String user = "";
        String pass = "";
        String server = "jdbc:derby://localhost:1527/sample";
        user = "app";
        pass = "tomcat";
        String driver = "org.apache.derby.jdbc.ClientDriver";
        String sql = "SELECT objective FROM Course";
        JDBCAdapter.testit(server, driver, user, pass, sql );
    }

    static void testit2(String server, String driver, String user, String pass ) {
        JDBCAdapter adapter = new JDBCAdapter(server, driver, user, pass, System.out );
        String sql0 = "CREATE TABLE Student(id VARCHAR(10), name VARCHAR(30))";
        String sql1 = "INSERT INTO Student(id) VALUES('D10012345');";
        String sql = "SELECT * FROM Student";
        int x = adapter.executeUpdate(sql1);
        
        x = adapter.executeQuery(sql);
        
        adapter.print();
        adapter.println(adapter.error());
        adapter.close();
    }

    public String dbname() {
        String tt = this.url();
        if (this.dbms.equals("sqlserver")) {
            int k = tt.indexOf("DATABASENAME");
            return tt.substring(k + 13).replaceFirst(";.*", "");
        }
        if (this.dbms.equals("mysql") || this.dbms.equals("access") || this.dbms.equals("h2") || this.dbms.equals("postgres")) {
            return tt.replaceFirst("(.*)[:|/]([a-z|A-Z|0-9]+$)", "$2");
        }
        if (this.dbms.equals("derby")) {
            return tt.replaceFirst(".*/([a-z|A-Z|0-9]+);.*", "$1");
        }
        return tt;
    }

    static void testit(String server, String driver, String user, String pass, String sql ) {
        JDBCAdapter adapter = new JDBCAdapter(server, driver, user, pass, System.out );
        String t = adapter.dbms;
        String tt = adapter.keyFields("AppUser");
        int n = -1;
        try {
            n = adapter.executeQuery(sql);
            String[] ft = new String[3];
           
        }
        catch (Exception e) {
            // empty catch block
        }
        
        adapter.println(adapter.error());
        adapter.print();
        adapter.close();
    }

    public String error() {
        return  (this.errormsg.toString());
    }

    public String dbInfo() {
        try {
            DatabaseMetaData dm = this.connection.getMetaData();
            this.dbms = dm.getDatabaseProductName().toLowerCase();
            return dm.getDatabaseProductName() + "\n" + dm.getDriverVersion() + "\n" + dm.getURL() + "\n" + dm.getUserName();
        }
        catch (Exception e) {
            return e.toString();
        }
    }

    public JDBCAdapter(PrintWriter out) {
        String server = "jdbc:oracle:thin:@167.21.180.26:1521:oracledb";
        String user = "scott";
        String pass = "tiger";
        String driver = "oracle";
        server = "jdbc:odbc:public";
        driver = "sun.jdbc.odbc.JdbcOdbcDriver";
        this.pw = out;
        this.output = 119;
        this.init(server, driver, user, pass);
       
    }

    void print(String str) {
        if (this.debug) {
            if (this.output == 'w') {
                this.pw.print(str);
            } else if (this.output == 's') {
                this.ps.print(str);
            }
        } else {
            this.errormsg.append(str);
        }
    }

    void println(String str) {
        if (this.debug) {
            if (this.output == 'w') {
                this.pw.println(str);
            } else if (this.output == 's') {
                this.ps.println(str);
            }
        } else {
            this.errormsg.append(str + "\n");
        }
    }

     

    public JDBCAdapter(String url, String driverName, String user, String passwd ) {
        
        this.debug = false;
        if (url == null || driverName == null) {
            this.errormsg.append("DB url or driver is null");
        } else {
            this.init(url, driverName, user, passwd);
        }
    }

    public JDBCAdapter(String url, String driverName, String user, String passwd, PrintWriter out ) {
        this.pw = out;
        this.output = 119;
       
        this.init(url, driverName, user, passwd);
    }

    public JDBCAdapter(String url, String driverName, String user, String passwd, PrintStream out ) {
        this.ps = out;
        this.output = 115;
       
        this.init(url, driverName, user, passwd);
    }

    public JDBCAdapter() {
        this.debug = false;
    }

    public void init(String url, String driverName, String user, String passwd) {
        this.server = url;
        if (this.debug) {
            this.println("start");
        }
        if (driverName == null) {
            this.println("driver is null");
            return;
        }
        if (url == null) {
            this.println("db server is null");
            return;
        }
        if (user == null) {
            this.println("user is null");
            user = "";
        }
        this.dburl = url.replaceFirst(".*/([^/]*)$", "$1");
        try {
            if (driverName.indexOf("oracle") >= 0) {
               // DriverManager.registerDriver((Driver)new OracleDriver());
            } else if (driverName.indexOf("microsoft") >= 0) {
              //  DriverManager.registerDriver((Driver)new SQLServerDriver());
            } else if (driverName.indexOf("odbc") >= 0){
                Class.forName(driverName);
            }  else {
                Class.forName(driverName).newInstance();
            }
            if (this.debug) {
                this.println("Opening db connection");
            }
            if (driverName.indexOf("microsoft") >= 0) {
                this.connection = DriverManager.getConnection(url + ";User=" + user + ";Password=" + passwd);
            } else if (driverName.indexOf("derby") >= 0) {
                Properties properties = new Properties();
                this.connection = DriverManager.getConnection(url + ";create=true", properties);
            } else {
                this.connection = DriverManager.getConnection(url, user, passwd);
              
            }
            if (this.connection != null) {
                this.statement = this.connection.createStatement();
                DatabaseMetaData dm = this.connection.getMetaData();
                
                this.dbms = dm.getDatabaseProductName().toLowerCase();
                if (this.dbms.indexOf("server") >= 0) 
                {
                    this.dbms = "sqlserver";
                }  else if (this.dbms.indexOf("h2") >= 0) 
                {
                    this.dbms = "h2";
                } 
                else if (this.dbms.indexOf("postgres") >= 0) 
                {
                    this.dbms = "postgres";
                }
                else if (this.dbms.indexOf("oracle") >= 0) 
                {
                    this.dbms = "oracle";
                } else if (this.dbms.indexOf("mysql") >= 0) {
                    this.dbms = "mysql";
                } else if (this.dbms.indexOf("access") >= 0) {
                    this.dbms = "access";
                }
                else if (this.dbms.indexOf("derby") >= 0) 
                {
                    this.dbms = "derby";
                } 
                this.status = "open";
            } else if (this.debug) {
                this.println("invalid");
            }
        }
        catch (NoClassDefFoundError e)
        {
             this.println("Driver class not exist");
        }
        catch (ClassNotFoundException e)
        {
             this.println("Driver class not exist");
        }
        catch (SQLException e) {
            this.println(  driverName + " or " + e.toString());
        }
        catch (Exception e) {
            this.println(  driverName + " or " + e.toString());
        }
         
    }

    public void profile() {
        this.println(this.error());
        this.println("user:" + this.userName());
        this.println("url: " + this.url());
        this.println("driver: " + this.driverName());
        String[] tn = this.tableList();
        if (tn != null) {
            for (int i = tn.length - 1; i >= 0; --i) {
                this.println(tn[i]);
            }
        }
        this.println("");
    }

    public void print() {
        int i;
        int nr = this.getRowCount();
        int nc = this.getColumnCount();
        int[] width = new int[nc];
        for (i = 0; i < nc; ++i) {
            try {
                width[i] = this.metaData.getColumnDisplaySize(i + 1);
                continue;
            }
            catch (Exception e) {
                // empty catch block
            }
        }
        for (i = 0; i < nr; ++i) {
            for (int j = 0; j < nc; ++j) {
                String v;
                int k;
                if ((v = this.getValueAt(i, j)) == null) {
                    v = "";
                }
                int w = v.length();
                if (this.colIsNum[j]) {
                    for (k = 0; k < width[j] - w; ++k) {
                        this.print(" ");
                    }
                    this.print(v + " ");
                    continue;
                }
                this.print(v);
                for (k = 0; k < width[j] - w + 1; ++k) {
                    this.print(" ");
                }
            }
            this.println("");
        }
    }

    public boolean transacte1(String[] querys, int n, int m) {
        StringBuffer er = new StringBuffer();
        boolean j = true;
        for (int i = n; i < m; ++i) {
            try {
                if (this.executeUpdate(querys[i]) >= 0) continue;
                j = false;
                er.append(this.error() + "\n");
                continue;
            }
            catch (Exception e) {
                j = false;
                er.append(e.toString());
            }
        }
        this.errormsg.append(er);
        return j;
    }

    public boolean transacte(String[] querys, int n, int m) throws Exception {
        boolean tt = this.connection.getAutoCommit();
        boolean b = false;
        boolean hasstate = true;
        try {
            this.connection.setAutoCommit(false);
            if (this.statement == null) {
                this.statement = this.connection.createStatement();
                hasstate = false;
            }
            for (int i = n; i < m; ++i) {
               boolean kk = this.statement.execute(querys[i]);
                
            }
            this.connection.commit();
            b = true;
        }
        catch (Exception e) {
             
            this.connection.rollback();
        }
        finally {
            this.statement.close();
            this.connection.setAutoCommit(tt);
            if (hasstate) {
                this.statement = this.connection.createStatement();
            }
        }
        return b;
    }

    public int executeUpdate(String query) {
        return  executeUpdate( query, true);
    }
    
    public int executeUpdate(String query, boolean delattach) {
        int i;
        if (this.dbms.equals("mysql")) {
            query = query.replaceAll("\\\\", "\\\\\\\\");
        } else if (this.dbms.equals("h2")) {
            query = query.replaceAll(" mod ", " % ");
        }
        query = query.replaceFirst("^[ |\t|\n|\r]+", "").replaceFirst("[ |\t|\n|\r]+", " ");
        int j = 0;
        int N = query.length();
        if (N == 0) 
        {
            return 0;
        }
        i = query.indexOf(" ");
        if (i == -1)
        {
           
            return 0;
        }
        String act = query.substring(0,i).toLowerCase();
        boolean mody = true;
        String tablename = null;
        String att[] = null;
        i++;
        j = i;
        for (; i < N && query.charAt(i) != ' ' && query.charAt(i) != '\t' && query.charAt(i) != '\n' && query.charAt(i) != '\r'; ++i) { }
        String maybetbn = query.substring(j, i);
        if (act.indexOf("update") == 0) 
        {
            tablename = maybetbn;
        } 
        else 
        {
          
            maybetbn = maybetbn.toLowerCase().trim();
            if (!maybetbn.equals("table") && !maybetbn.equals("into")  && !maybetbn.equals("from") && !maybetbn.equals("database") ) 
            {
               
                this.println("Error: Incorrect Query:" + act + " " + tablename);
                return -1;
            }
         
            String tmp = query.substring(i+1);
            tmp = tmp.replaceFirst("^[ |\t|\n|\r]+", "");
            tmp = tmp.replaceFirst("[ |\t|\n|\r]+", " ");
          
            i = tmp.indexOf(" ");
            if (i == -1) i = tmp.length();
         
            tablename = tmp.substring(0, i).toLowerCase();
           
            if (delattach && act.indexOf("delete") == 0)
            {
                String querya = query.replaceFirst("^[D|d][E|e][L|e][E|e][T|t][E|e]", "SELECT attach ");

                int na = executeQuery(querya);

                if (na > 0)
                {
                    att = new String[na];
                    for (int k=0; k < na; k++)
                    {
                        att[k] = getValueAt(0,k);
                    }
                }
            }    
        }
        
        if (tablename.equals("") && query.indexOf("SET") != 0) 
        {
            this.println("Error: Query:'"  +query + "' is wrong, because it has no table");
            return -1;
        }
        this.errormsg.setLength(0);
        if (this.connection == null || this.statement == null) {
           
            return -1;
        }
        this.numberOfColumns = 0;
        int nrows = -1;
        String tb = this.dburl + "," + tablename;
        
        while (dburls.contains(tb)) 
        {
            Thread.yield();
        }
        grabHandle(tb, true);
        try {
            
            nrows = this.statement.executeUpdate(query);
            
            
        }
        catch (SQLException ex) {
            
            this.println("Error: " + ex.toString());
             
        }
        
        grabHandle(tb, false);
       
        return nrows;
    }

    static synchronized void grabHandle(String url, boolean get) {
        if (get) {
            dburls.add(url);
        } else {
            dburls.remove(url);
        }
    }

    public int executeQuery(String query) {
         mode = 0;
        String ss;
        if (this.dbms.equals("mysql")) {
            query = query.replaceAll("\\\\", "\\\\\\\\");
        } else if (this.dbms.equals("h2")) {
            query = query.replaceAll(" mod ", " % ");
        }
        this.errormsg.setLength(0);
        this.cursor = 0;
        this.numberOfColumns = 0;
        this.numberOfRows = 0;
        if (this.connection == null || this.statement == null) {
           
            return -1;
        }
        if (query == null || query.length() < 6) {
            this.println("wrong query:" + query);
            return -1;
        }
        if (!(ss = query.substring(0, 6).toLowerCase()).equals("select")) {
            this.println("no select");
            return -1;
        }
        try {
            this.numberOfColumns = 0;
            this.numberOfRows = 0;
            this.resultSet = this.statement.executeQuery(  query);
            this.metaData = this.resultSet.getMetaData();
            if (this.metaData == null) {
                return -1;
            }
            this.numberOfColumns = this.metaData.getColumnCount();
        }
        catch (SQLException ex) {
            this.println("Error:");
            this.println(ex.toString());
            return -1;
        }
        boolean hasnext = false;
        do {
            hasnext = false;
            try {
                hasnext = this.resultSet.next();
            }
            catch (Exception ex) {
                break;
            }
            if (!hasnext) break;
            if (this.rows == null) {
                this.rows = new String[10][];
            }
            if (this.rows.length == this.numberOfRows) {
                String[][] bigone = new String[this.numberOfRows + 10][];
                for (int t = 0; t < this.numberOfRows; ++t) {
                    bigone[t] = this.rows[t];
                }
                this.rows = bigone;
            }
            this.rows[this.numberOfRows] = new String[this.numberOfColumns];
            for (int i = 1; i <= this.numberOfColumns; ++i) {
                try {
                    this.rows[this.numberOfRows][i - 1] = this.resultSet.getString(i);
                    continue;
                }
                catch (Exception ex) {
                    // empty catch block
                }
            }
            ++this.numberOfRows;
        } while (true);
        if (this.needMetaInfo) {
            this.metainfo();
        }
        return this.numberOfRows;
    }

    public void metainfo() 
    {
        try 
        {
            this.columnNames = new String[this.numberOfColumns];
            this.colIsNum = new boolean[this.numberOfColumns];
            this.colSizes = new int[this.numberOfColumns];
            this.colNullable = new int[this.numberOfColumns];
            this.datatypes = new String[this.numberOfColumns];
            for (int column = 0; column < this.numberOfColumns; ++column) 
            {
                this.columnNames[column] = this.metaData.getColumnLabel(column + 1);
                this.colIsNum[column] = this.isnum(column);
                this.colSizes[column] = this.metaData.getColumnDisplaySize(column + 1);
                this.colNullable[column] = this.metaData.isNullable(column + 1);
                this.datatypes[column] = this.metaData.getColumnTypeName(column + 1);
            }
        }
        catch (Exception e) 
        {
            this.println("" + e);
        }
    }

    public boolean executeQuery2(String query, boolean mt) {
        if (this.dbms.equals("mysql")) {
            query = query.replaceAll("\\\\", "\\\\\\\\");
        } else if (this.dbms.equals("h2")) {
            query = query.replaceAll(" mod ", " % ");
        }
        mode = 2;
        this.errormsg.setLength(0);
        try 
        {
            this.resultSet = this.statement.executeQuery(query);
            if (mt) 
            {
                this.metaData = this.resultSet.getMetaData();
                this.numberOfColumns = this.metaData.getColumnCount();
                metainfo();
            }
            this.cursor = -1;
            end = false;
            return true;
        }
        catch (SQLException ex) 
        {
            this.println("Error:*****************");
            this.println(ex.toString());
            return false;
        }
    }
    
    public String tocsv()
    {
        int m = getColumnCount();
        int i = 0;
        String s = "";
        while(true) 
        {
            for(int j = 0; j < m; j++)
            {
                String tv =  getValueAt(i, j);
                if (cursor < 0)
                    break;
                else if (j==0 && !s.equals(""))
                    s  += "\n";
                if (tv!=null)
                {
                    if (this.isnum(j)==false)
                    {
                         tv =  '"' + tv.replaceAll("\"","\"\"");
                    }
                    s += tv;
                }
                if(j < m-1)
                {
                    s += ",";
                }
            }
           
            if (cursor >= 0)
                i++;
            else 
                break;
        }
        return s;
    }

    public boolean nextRow() {
        if ( cursor == this.numberOfRows - 1) {
            return false;
        }
        ++this.cursor;
        return true;
    }

    public String getParameter(int i) {
        return this.getValueAt(this.cursor, i);
    }

    public String getParameter(String s) {
        int i;
        for (i = 0; !(i >= this.numberOfColumns || this.columnNames[i].equals(s)); ++i) {
        }
        if (i == this.numberOfColumns) {
            return "";
        }
        return this.getValueAt(0, i);
    }
     
    public boolean end = false;
    public boolean rewind()
    {
        boolean b = false;
        try{
           b = this.resultSet.first();
        }catch(Exception e1){}
        if (b) cursor = 0;
        return b;
    }
    public String getValueAt(int aRow, int aColumn) {
        if (mode == 2)
        {
            if (aRow != cursor+1 && aRow != cursor)
            {
                return "After executeQuery2, you have to read data sequentially with row=0, 1, 2 ...";
            }
            if (cursor == -2)
            {
                return null;
            }
            if (aRow == cursor+1) 
            {
                try 
                {
                    if (this.resultSet.next())
                    {
                        cursor++;
                    }
                    else
                    {
                       end = true;
                       cursor= -2; 
                       return null;
                    }
                }
                catch (Exception ex) 
                {
                    cursor = -2;
                }
            }
             
            if (cursor>=0)
            {
                try
                {
                    String x =  this.resultSet.getString(aColumn+1);
                    return x;
                }catch(Exception e){}
            }
            return null;
        }
        else if (mode == 0)
        {
            if (aRow >= this.numberOfRows || aColumn >= this.numberOfColumns) 
            {
                return null;
            }
            return this.rows[aRow][aColumn];
        }
        else
            return null;
    }

    public void close() {
        this.errormsg.setLength(0);
        String m = "resultset";
        try {
            if (this.resultSet != null) {
                this.resultSet.close();
                this.resultSet = null;
            }
            m = "statement";
            if (this.statement != null) {
                this.statement.close();
                this.statement = null;
            }
            m = "connection";
            if (this.connection != null) {
                this.connection.close();
            }
            if (this.debug) {
                this.println("Closed db connection");
            }
            this.connection = null;
            this.statement = null;
            this.dmd = null;
        }
        catch (SQLException e) {
            this.println("Error as Closing db " + m);
        }
    }

    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    public boolean isnum(int column) {
        int type;
        try {
            type = this.metaData.getColumnType(column + 1);
        }
        catch (SQLException e) {
            return false;
        }
        switch (type) {
        case  Types.BIGINT : 
        case  Types.BINARY : 
        case  Types.BIT:
        case  Types.BOOLEAN : case Types.DECIMAL : case  Types.DOUBLE :
        case  Types.FLOAT : case  Types.INTEGER : case  Types.NUMERIC:
        case  Types.REAL: case  Types.SMALLINT : case  Types.TIMESTAMP:
        case  Types.TINYINT: 
        {
                return true;
            }
        }
        return false;
    }

    public boolean isCellEditable(int row, int column) {
        try {
            return this.metaData.isWritable(column + 1);
        }
        catch (SQLException e) {
            return false;
        }
    }

    public int getColumnCount() {
        return this.numberOfColumns;
    }

    public int getRowCount() {
        return this.numberOfRows;
    }

    public synchronized String keyFields(String tn) {
        if (this.connection == null) {
            return null;
        }
        try {
            if (this.dmd == null) {
                this.dmd = this.connection.getMetaData();
            }
            ResultSet rs = this.dmd.getPrimaryKeys(null, null, tn);
            String fields = "";
            String str = "";
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                if (!fields.equals("")) {
                    fields = fields + ",";
                }
                fields = fields + columnName;
            }
            return fields;
        }
        catch (Exception e) {
            return "";
        }
    }

    public synchronized String[] tableList() {
        if (this.connection == null) {
            return null;
        }
        try {
            StringBuffer tblnames = new StringBuffer("");
            if (this.dmd == null) {
                this.dmd = this.connection.getMetaData();
            }
            boolean n = false;
            String[] ts = new String[]{"TABLE"};
            ResultSet rs2 = this.dmd.getTables(null, null, "%", ts);
            int k = 0;
            while (rs2.next()) {
                String tt;
                if ((tt = rs2.getString(3)) == null || tt.equals("")) continue;
                if (k > 0) {
                    tblnames.append(",");
                }
                tblnames.append(tt);
                ++k;
            }
            return tblnames.toString().split(",");
        }
        catch (Exception e) {
            this.println(e.toString());
            return null;
        }
    }

    public String userName() {
        try {
            return this.connection.getMetaData().getUserName();
        }
        catch (Exception e) {
            return "unknown";
        }
    }

    public String url() {
        try {
            return this.connection.getMetaData().getURL();
        }
        catch (Exception e) {
            return "unknown";
        }
    }

    public String driverName() {
        try {
            return this.connection.getMetaData().getDriverName();
        }
        catch (Exception e) {
            return "unknown";
        }
    }

    public synchronized ResultSetMetaData tableMeta(String tablename) {
        if (this.connection == null) {
            return null;
        }
        try {
            this.resultSet = this.statement.executeQuery("SELECT * from " + tablename + " where 0 = 1");
            return this.resultSet.getMetaData();
        }
        catch (SQLException ex) {
            
            return null;
        }
    }

    public String tabledef(String table, String targetdbms) {
        boolean trans = false;
        int sindex = -1;
        int tindex = -1;
       
        String keyfields = this.keyFields(table);
        String ans = "CREATE TABLE " + table + "(\n";
        int kk = 0;
        try {
            this.resultSet = this.statement.executeQuery("SELECT * from " + table + " where 0 = 1");
            this.metaData = this.resultSet.getMetaData();
            kk = this.metaData.getColumnCount();
            if (kk < 0) {
                return "";
            }
            for (int i = 0; i < kk; ++i) {
                ans = ans + this.metaData.getColumnName(i + 1) + " ";
                ans = !trans ? ans + this.metaData.getColumnTypeName(i + 1) : ans +  this.metaData.getColumnTypeName(i + 1) ;
                int ll = this.metaData.getColumnDisplaySize(i + 1);
                if (!(ll >= 10000 || this.isnum(i))) {
                    ans = ans + "(" + ll + ")";
                }
                if (this.metaData.isNullable(i + 1) != 1) {
                    ans = ans + " NOT NULL";
                }
                ans = i < kk - 1 ? ans + "," : (!keyfields.equals("") ? ans + ",PRIMARY KEY (" + keyfields + ")\n)" : ans + ")");
            }
            return ans;
        }
        catch (Exception e) {
            return "";
        }
    }

    public synchronized String[] fieldList(String tablename) {
        try {
            this.resultSet = this.statement.executeQuery("SELECT * from " + tablename + " where 0 = 1");
            this.metaData = this.resultSet.getMetaData();
            if (this.metaData != null) {
                int kk = this.metaData.getColumnCount();
                String[] a = new String[kk];
                for (int i = 0; i < kk; ++i) {
                    a[i] = this.metaData.getColumnName(i + 1);
                }
                return a;
            }
        }
        catch (Exception e) {
            // empty catch block
        }
        return null;
    }

    private void copy2(String[] x, String[] y) {
        for (int i = 0; i < y.length; ++i) {
            x[i] = y[i];
        }
    }
    
    public int copyto(JDBCAdapter ada, String tbn)
    {
        executeQuery2("select * from " + tbn, true);
        int i = 0;
        int n=0;
        while (true)
        {
        if (i==0) 
        {
           try{ if (resultSet.first() == false) break; }catch(Exception e){break;}
        }
        else
        {
            try{ if (resultSet.next() == false) break; }catch(Exception e){break;}
        }
       
        String cmd = "INSERT INTO " + tbn + " values(";
        for (int j=0; j < numberOfColumns; j++)
        {
            cmd += "?";
            if (j < numberOfColumns-1) 
                cmd += ",";
        }
        cmd += ")";
        
        try{
           
        PreparedStatement pst = ada.connection.prepareStatement(cmd);
        for (int j=1; j <= numberOfColumns; j++)
        {
            if (datatypes[j-1].toLowerCase().equals("integer"))
            {
                pst.setInt(j, resultSet.getInt(j));
            }
            else if (datatypes[j-1].toLowerCase().equals("long"))
            {
                pst.setLong(j, resultSet.getLong(j));
            }
            else if (datatypes[j-1].toLowerCase().equals("double"))
            {
                pst.setDouble(j, resultSet.getDouble(j));
            }
            else if (datatypes[j-1].toLowerCase().equals("float"))
            {
                pst.setFloat(j, resultSet.getFloat(j));
            }
            if (datatypes[j-1].toLowerCase().equals("blob"))
            {
                pst.setBlob(j, resultSet.getBlob(j));
            }
            else 
            {
                pst.setString(j, resultSet.getString(j));
            }
        }
         if (pst.executeUpdate()==1) n++;
        }catch(Exception e){}
        i++;
    }
    return n;
    }
  
}
