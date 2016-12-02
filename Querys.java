 

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.sql.*;


public class Querys extends JInternalFrame 
{
   public String tablename;
   public int NUMROWS ;
   public int numRows ;
    private int extraline = 6;
   public int numCols ;
   public String[] fields ;
   public String[] datatypes ;
   public String[] defaultvalues;
   public boolean[] notnull;
   public String[] keys = null;
   private String keystr="";
    
   public String wherekey[];
   public int numRecords = 0;
   public Object[][] data;
   TableModel dataModel;
   int rowinediting = 0;
   int colinediting = 0;
   JTable table = null;
   
   JButton stopbut, setsch;
   JInternalFrame opener = null;
   JDBCAdapter adapter = Encryptor.adapter;
   private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    
   String pad(String s, int mx)
   {
      for (int i=s.length(); i < mx; i++)
         s += ' ';
      return s;
   }
   class FrameListener extends WindowAdapter
   {
    public void windowClosing(WindowEvent e)
    {
       if (0!=save()) ;
       System.exit(0);
    }
   }
  
   int save()
   {
       if (keys==null) return 0;
       
        int n1 = 0;
            for (int i = 0; i < NUMROWS ; i++) 
            {
                 if ( readTextAt(i, 0).equals("")) continue;   
                 String sql = "";
                 String vk = "";
                 for (int j = 0; j < numCols - 1; j++) 
                 {
                     String q = "'";
                     sql += ",";
                     boolean iskey = keystr.contains("," + fields[j].toLowerCase() +",");
                     if (iskey) vk+=  " AND " + fields[j]  + "=";
                     if (datatypes[j].indexOf("varchar") < 0) 
                     {
                         q = "";
                         if (iskey) 
                             vk +=   readTextAt(i, j).replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\") ;
                     }
                     else
                     {
                         if (iskey) 
                             vk += "'" + readTextAt(i, j).replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\") + "'";
                     }
                     
                     if (readTextAt(i, j).trim().equals("")) 
                     {
                         if (q.equals("")) 
                         {
                             sql += "NULL";
                         } 
                         else 
                         {
                             sql += "''";
                         }
                     } 
                     else 
                     {
                         sql += q + readTextAt(i, j).replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\") + q;
                     }
                      
                 }
                 sql = "INSERT INTO " + tablename + " values (" + sql.substring(1) + ")";
                 
                 try 
                 {
                     
                     int nn = adapter.executeUpdate(sql);
                     Encryptor.println(sql + nn+ adapter.error());
                     if (nn > 0) 
                     {
                         n1 += nn;
                         wherekey[i] = vk.replaceFirst("AND", " WHERE");
                     }

                 } 
                 catch (Exception e1) 
                 {
                 }

                 sql = "";
                 for (int j = 0; j < numCols - 1; j++) {
                     String x = readTextAt(i, j).replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\");
                     if (!x.equals("")) 
                     {
                         String q = "'";
                         if (datatypes[j].indexOf("varchar") < 0) 
                         {
                             q = "";
                         }
                         if (q.equals("") && readTextAt(i, j).trim().equals("")) 
                         {
                             sql += fields[j] + "=NULL";
                         } 
                         else 
                         {
                             sql += fields[j] + "=" + q + x + q;
                         }
                         sql += ",";

                     }

                 }
                 if (!sql.equals("")) 
                 {
                     sql = "UPDATE " + tablename + " SET " + sql.replaceFirst(",$", "") + wherekey[i];
                 }
                 if (!sql.equals("")) 
                 {
                     try 
                     {
                         Encryptor.println(sql);
                         int nn = adapter.executeUpdate(sql);
                         Encryptor.println(sql + nn+ adapter.error());
                     } catch (Exception e1) 
                     {
                     }
                 }
             }
              
        return 0;
   }
  
   void deleter()
   {
        if (keys==null) return;
            int n1=0;
            for (int i=0; i < NUMROWS ; i++)
             { 
                 if ( ((Boolean)(data[i][numCols-1])).booleanValue() == false) continue;
                
                 String sql  = "DELETE FROM "+  tablename  +" " + wherekey[i];
                 Encryptor.println(sql);
                 try
                 { 
                     int  nn = adapter.executeUpdate(sql);
                     if (nn>0)
                     { 
                         n1+=nn;  
                         for (int j = 0; j < numCols ; j++) 
                         {
                             if(datatypes[j].indexOf("integer")>=0) 
                             {
                                 table.setValueAt(new Integer(-1),i,j);
                             }
                             else if(datatypes[j].indexOf("long")>=0) 
                             {
                                 table.setValueAt(new Long(-1),i,j);
                             }
                             else if(datatypes[j].indexOf("boolean")>=0) 
                             {
                                 table.setValueAt(new Boolean(false),i,j);
                             }
                             else if(datatypes[j].indexOf("varchar")>=0) 
                             {
                                 table.setValueAt("",i,j);
                             }
                         }
                     }
                 }catch(Exception e1){}
             }
   }
 
  public void addRow(Object [] record)
  {
      DefaultTableModel model = (DefaultTableModel) table.getModel();
      
      model.addRow(record);
  }   
  int []direct =null;
  void sortby(int col)
  {
       
      int n = NUMROWS-extraline;
      if (direct == null) direct = new int[numCols+1];
      if (direct[col] == 0)direct[col]=1;
      else direct[col]= - direct[col];
      if (col==numCols-1)
      {
          Boolean b = new Boolean(false);
          if (direct[col] == -1)
              b = new Boolean(true);
          for (int i=0; i < n; i++)
             table.setValueAt(b, i, col);
          return;
      }
      Integer rows[] = new Integer[n];
      for (int i=0; i < n; i++)
         rows[i] = new Integer(i); 
      Arrays.sort(rows, new RowComparator5(col, this));	
      int [] a = new int[n];
      for (int i=0; i < n; i++)
      {
          a[i] = rows[i].intValue();
          Encryptor.println(a[i]);
      }
      
      Object[][] newd = new Object[n][];
      for (int i=0; i < n; i++)
      {
           newd[i]  = new Object[data[i].length]; 
          for (int j=0; j < data[i].length; j++)
              newd[i][j] = data[i][j];
      }
      for (int i=0; i < n; i++)
      {
           for (int j=0; j < numCols; j++)
            table.setValueAt(newd[a[i]][j], i, j); 
      }
      
  }
  
  public Querys( String  SQL)
  {
      super(" ", true, true, true, true );
      adapter = Encryptor.adapter;
      if ( SQL.toLowerCase().startsWith("create table"))
      {
      
      String [] y =  SQL.replaceFirst("[^\\(]+\\(","").replaceFirst("(?i),[ |\n|\r|\t]*PRIMARY[ |\n|\r|\t]KEY.*","").split(",");
      int numCols1 =  y.length;
      String fields0[] = new String[y.length];
      String [] datatypes0 = new String[y.length];
      defaultvalues  = new String[y.length];
      notnull = new boolean[y.length+1]; 
      String z[] =  SQL.replaceAll("[ |\t|\n|\r]+"," ").trim().split(" ");
      String tablename0 = z[2].replaceFirst("\\(.*", "");
      for (int i=0; i < y.length; i++)
      {
          defaultvalues[i] = y[i].replaceFirst("^[^']+'", "").replaceFirst("'[^']*$", "").replaceAll("''", "'");
          if (defaultvalues[i].equals(y[i]))
          {
              defaultvalues[i] = "";
          }
          y[i] = y[i].replaceAll("'[^']*'","").replaceAll("[ |\t|\n|\t]+"," ").trim();
          String yi = y[i].replaceFirst("(?i)not null", "");
          notnull[i] =  ( !yi.equals(y[i]) );
          
          fields0[i] = y[i].replaceFirst(" .*$", "");
          int k = fields0[i].length();
          if (k < y[i].length())
          {  
              y[i] = y[i].substring(k).trim();
              datatypes0[i] = y[i].replaceFirst(" .*$", "");
              k = datatypes0[i].length();
              if (k < y[i].length())
              {
                  y[i] = y[i].substring(k).trim();
                  k = y[i].indexOf("(");
                  if (k>=0)
                  {
                     int j = y[i].indexOf(")",k);
                     datatypes0[i]  = "varchar" + y[i].substring(k, j+1);
                  }
              }
              
              
              
          }
      }
      String x =  SQL.replaceFirst("(?i).*,[ |\n|\r|\t]*PRIMARY[ |\n|\r|\t]KEY","");
      int k = x.indexOf("(");
      int j = -1; if (k>=0) j = x.indexOf(")",k);
      String key0[] ;
      if (j>-1) key0  = x.substring(k+1, j).split("[ |\n|\r|\t]*,[ |\n|\r|\t]*");
      else key0 = new String[]{"key"};
      maketable(  tablename0, fields0  ,  datatypes0 ,    key0   );
      setTitle(tablename0);
      }
      else
      {
          selectSQL = SQL;
          int n = adapter.executeQuery(SQL);
         
          String [] datatypes0 = new String[adapter.getColumnCount()];
          
          for (int i=0; i < adapter.getColumnCount(); i++)
          {
              try{datatypes0[i] = adapter.metaData.getColumnTypeName(i + 1).toLowerCase();}catch(Exception e)
              {
                 datatypes0[i] = "varchar(20)";
              }
          }
          maketable("query",adapter.columnNames ,  datatypes0 , null);
          setTitle(SQL.substring(0,SQL.length()>50?50:SQL.length()));
      }
  }
  String selectSQL = null; 
  public Querys( String tablename, String fields[], String datatypes[],  String keys[] )
  {      
       super(tablename, true, true, true, true );
       maketable(tablename,  fields ,   datatypes ,    keys  );
  }
  String pt(String s[])
  {
      String str = "";
      if (str!=null)
      for (int i=0; i < s.length; i++)
        str += "  " + s[i];  
      return str;
  }
  private void  maketable( String tablename, String fields[], String datatypes[],  String keys[] )
  {
       Encryptor.println(pt(fields));
       Encryptor.println(pt(datatypes));
       Encryptor.println(pt(keys));
       if (fields.length!=datatypes.length)
       {
           Encryptor.println("fields.length!=datatypes.length");
       }
       this.numCols = fields.length  + 1;
       this.tablename= tablename;
       this.datatypes = new String[numCols];
       this.fields = new String[numCols];
       if (keys!=null){
       this.keys = new String[keys.length]; 
       for (int i=0; i < keys.length; i++)
       {
           this.keys[i] = keys[i];
       }
       }
       final String colnames [] = new String[numCols];
       final String datp[] = new String[numCols];
       for (int i=0; i < numCols-1; i++)
       {
           this.datatypes[i] = datatypes[i].toLowerCase();
           this.fields[i] = fields[i].replaceAll(" ","");
           colnames[i] = fields[i];
           datp[i] = this.datatypes[i];
       }
       datp[numCols-1] = this.datatypes[numCols-1] = "boolean";
       this.fields[numCols-1] = "Mark"; 
        
       final int widths[] = new int[numCols];
       colnames[0] = "Name";
       colnames[1] = "SQL Statement";
       colnames[2] = "Mark";
       this.fields[0] = "qname";
       this.fields[1] = "stmt"; 
       this.fields[2] = "mark";
      
       for (int i=0; i < numCols   ; i++) 
       {
           widths[i] = 50; 
           if (this.datatypes[i].contains("varchar"))
              widths[i] = 7*Integer.parseInt(this.datatypes[i].replaceAll("[^0-9]",""));
           if (widths[i] > 200) widths[i]=200;
           if (widths[i] < 70) widths[i] = 70;
           Encryptor.println(widths[i]);
       }
       
       final long t = System.currentTimeMillis();
       final  String randt = ("" + (t%24) + ":" + (t%60));
       data = mkdata();  
         
        dataModel = new AbstractTableModel() 
       {
            public int getColumnCount() { return numCols ; }
            public int getRowCount() { if (data==null) return 0; return data.length;}
            public Object getValueAt(int row, int col) 
            {
                   return  data[row][col]; 
            }

            public String getColumnName(int column) 
            { 
                if (column < numCols) 
                    return colnames[column]; 
                else return "";
            }
            public Class getColumnClass(int col) {return getValueAt(0,col).getClass();}
            public boolean isCellEditable(int row, int col) {return col==2 || col==0;}
            public void setValueAt(Object aValue, int row, int column) 
            {
                data[row][column] = aValue;
                fireTableCellUpdated(row, column);
                fireTableDataChanged();
            }
        };

        final String tips[] = fields;
         
        table  = new JTable(dataModel)
        {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) 
            {
                Component c = super.prepareRenderer(renderer, row, column);
                if (column >=1) 
                    c.setForeground(Color.blue);
                else 
                    c.setForeground(Color.black);
                return c;
            }
            protected JTableHeader createDefaultTableHeader() 
            {
               return new JTableHeader(columnModel) 
               {
                 public String getToolTipText(MouseEvent e) 
                 {
                    String tip = null;
                    java.awt.Point p = e.getPoint();
                    int index = columnModel.getColumnIndexAtX(p.x);
                    int realIndex =   columnModel.getColumn(index).getModelIndex();
                    return tips[realIndex];
                 }
               };
               
            }
            
            public Class getColumnClass(int column) 
            {
                 if (datp[column].contains("varchar")  )
                     return String.class;
                 else  if (datp[column].equals("integer") )
                      return Integer.class;
                  else  if (datp[column].equals("long")  )
                      return Long.class;
                 else  if (datp[column].equals("double") )
                      return Double.class; 
                  else  if ( column == numCols-1 )
                       return Boolean.class;
                  else 
                      return String.class; 
            }
               
        };
       
        table.getTableHeader().addMouseListener(
        new MouseAdapter() 
        {
           boolean direct = false;
           public void mouseClicked(MouseEvent e) 
           {
            int col = table.columnAtPoint(e.getPoint());
            String name = table.getColumnName(col);
            Encryptor.println("sort by  " + col + " " + name );
            if (col==5)
            {
               direct = !direct; 
                int n = numRecords;
                if (NUMROWS > n) n = NUMROWS;
               for (int i=0; i < n ; i++)
                   table.setValueAt(new Boolean(direct),i,col);
            }
            else
                sortby(col);
           }
         });
        
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        TableColumnModel columnModel = table.getColumnModel();
        
        table.setRowHeight(Encryptor.height);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        int wd = 0;
        for (int i=0; i < numCols; i++)
        {
           wd += widths[i];
           TableColumn columni = columnModel.getColumn(i);
           columni.setPreferredWidth(widths[i]);
        }
        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.lightGray);
        header.setPreferredSize(new Dimension(wd, Encryptor.height+3));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollpane = new JScrollPane(table);
        scrollpane.setPreferredSize(new Dimension(wd, (NUMROWS+1)*( Encryptor.height)+6));
        scrollpane.setViewportView(table);

        table.addMouseListener(new MouseAdapter()
        {
          public void mouseClicked(MouseEvent e)
          {
             if (rowinediting>=0  )
             {
                 String sql = (String)(jTextArea1.getText());  
                 if (sql!=null && !sql.equals(""))
                 data[rowinediting][3] = sql;
             }
             int row = table.getSelectedRow();
              if (row < 0) return;
             rowinediting = row;
             String qname = (String)data[row][0];
             
             int col = table.getSelectedColumn();
             if (col>0)
             {
                 String sql = (String)data[row][3];
                 jTextArea1.setText(sql);
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
      
      
      jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();

        setTitle("SQL Executor");

        jTextArea1.setColumns(50);
        jTextArea1.setRows(20);
        jScrollPane1.setViewportView(jTextArea1);

        jButton1.setText("Execute");
        
      
      c.fill = GridBagConstraints.VERTICAL;

      c.weightx = 0.5;
      c.gridx = 1;
      c.gridy = 0;
      gridbag.setConstraints(jScrollPane1, c);
      
      content.add(jScrollPane1);
      
      
      // Container content = frame.getContentPane();
       if (!tablename.equals("query"))
       {
      JPanel buttonson = new JPanel();
      
       
       
      JButton choser = new JButton("Save");
      choser.setSize(320, 25);
      buttonson.add(choser);
      
      JButton update = new JButton("Delete");
      update.setSize(320, 25);
      buttonson.add(update);
      
      jButton1.setSize(320, 25);
      buttonson.add(jButton1);
      
      c.gridx = 0;
      c.gridy = 1;
      c.gridwidth = 2;
      gridbag.setConstraints(buttonson, c);
      content.add(buttonson);
       
      
      ButtonListener actionlistener = new ButtonListener(table);
      choser.addActionListener(actionlistener);
      jButton1.addActionListener(actionlistener);
      update.addActionListener(actionlistener);
       }
      setSize(wd +10, 450);
      setVisible(true);
      setTitle("SQL Queries");
      
  }

   String sql = "CREATE TABLE Student(id  varchar(9), lastname varchar(30), age integer, primary key(id))";
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {                                         
       sql  =  jTextArea1.getText().trim();       // TODO add your handling code here:
         
       if (sql!=null && !sql.equals(""))
            data[rowinediting][3] = sql;
       if (sql.toLowerCase().startsWith("create table") )
       {
           if (adapter.executeUpdate(sql) == 0)
           {
               VSheet vs = new VSheet(sql);
               Encryptor.addTheFrame(vs);
           }
           else
           {
              jTextArea1.setText(sql + "\n\n" + adapter.error()); 
           }
       }
       else if ( sql.toLowerCase().startsWith("select "))
       {
           VSheet vs = new VSheet(sql);
           Encryptor.addTheFrame(vs);
       }
       else
       {
           int n = Encryptor.adapter.executeUpdate(sql);
           JOptionPane.showMessageDialog(this, n+ " records affected. " + adapter.error());
       }
    }  
     public String readTextAt(int i, int j)
  {
      if (i >= data.length || j >= data[i].length)
          return "";
      if (datatypes[j].toLowerCase().indexOf("varchar") >= 0)
      {
          if (data[i][3] == null) return "";
          if (j==0 && ((String)data[i][3]).length() > 10 &&(data[i][0]==null || ((String)data[i][0]).equals("")))
          {
              table.setValueAt("Query" + i, i, 0);
              return "Query" + i;
          }
          if (j==1) j=3;
          if (data[i][j]!=null) 
              return (String)data[i][j];
          return "";
      }
      else if (datatypes[j].toLowerCase().indexOf("integer") >= 0  )
      {
          try{
          Integer q = (Integer)(data[i][j]);
          if (q ==null) return "";
          return "" + q.intValue();
          }catch(Exception e){return "";}
      }
      else if ( datatypes[j].toLowerCase().indexOf("long") >= 0  )
      {
          try{
          Long q = (Long)(data[i][j]);
          if (q ==null) return "";
          return "" + q.longValue();
          }catch(Exception e){return "";}
      }
      else if ( datatypes[j].toLowerCase().indexOf("double") >= 0)
      {
          try{
          Double q = (Double)(data[i][j]);
          if (q ==null) return "";
          return "" + q.doubleValue();
          }catch(Exception e){return "";}
      }
      else if ( datatypes[j].toLowerCase().indexOf("boolean") >= 0  )
      {
          try{
          Boolean q = (Boolean)(data[i][j]);
          if (q ==null) return "";
          return "" + q.booleanValue();
          }catch(Exception e){return "";}
      }
      return "";
  }
   
     
    
    class ButtonListener implements ActionListener 
    {
      
      JTable table ; 
      public ButtonListener( JTable tbl) { table = tbl;}
      
      public void actionPerformed(ActionEvent e) 
      {
         String cmd = e.getActionCommand();
          if (cmd.equals("Delete"))
         {
             deleter();
          
         }
         else if (cmd.equals("Save"))
         {
            save();  
         } 
          else if (cmd.equals("Execute"))
         {
             jButton1ActionPerformed(null);  
         } 
          
      }
          
    }

    public static void main2 (String[] args)
    {
     // Querys vf =  new Querys("Job" ,new String[]{"Location", "Skills", "Position", "Salary"}, new String[]{"varchar(30)","varchar(30)","varchar(30)","integer" } );
      //vf.table.setValueAt("AAAA", 1, 1) ;      
    }
    
   
   
  
  public Object[][] mkdata()
  { 
            keystr = "";
            for (int i = 0; i < keys.length; i++) 
            {
                keystr += "," + keys[i] ;
            }
             
            String sql2 = "SELECT qname, stmt FROM " + tablename;
            if (selectSQL!=null)
            {
                NUMROWS = adapter.executeQuery(selectSQL);
                extraline = 0; 
            } 
            else
            {
                NUMROWS = adapter.executeQuery(sql2);
                if (NUMROWS==-1) NUMROWS =  extraline;
                else NUMROWS +=  extraline;
            }
            if (NUMROWS==extraline)
            {
                rowinediting = 0;
            }
            data = new Object[NUMROWS][];
            wherekey = new String[NUMROWS];
            keystr = (keystr + ",").toLowerCase();
             
            for (int i=0 ; i < NUMROWS-extraline ; i++)
            {
                    String kv = "";
                    data[i] = new Object[numCols+1];
                    for (int j = 0; j < numCols-1; j++)
                    {
                        boolean iskey = keystr.contains("," + fields[j].toLowerCase() +",");
                        String str = adapter.getValueAt(i,j);
                        if (iskey)
                            kv += " AND " + fields[j] + "=";
                        if (datatypes[j].indexOf("varchar")>=0)
                        {
                            if (iskey)
                            {
                                kv += "'" + str + "'";
                            }
                            data[i][j] = str;
                        }
                        else if(datatypes[j].indexOf("double")>=0) 
                        {
                            if (iskey)
                            {
                                kv +=   str  ;
                            }
                            double kk = -1;
                            try
                            {
                                kk = Double.parseDouble(str);
                            }
                            catch(Exception e1){}
                            data[i][j] = new Double(kk);
                        }
                        else if(datatypes[j].indexOf("integer")>=0) 
                        {
                            if (iskey)
                            {
                                kv += str  ;
                            }
                            int kk = -1;
                            try
                            {
                                kk = Integer.parseInt(str);
                            }
                            catch(Exception e1){}
                            data[i][j] = new Integer(kk);
                        }    
                        else if(datatypes[j].indexOf("long")>=0) 
                        {
                            if (iskey)
                            {
                                kv +=   str  ;
                            }
                            long kk = -1;
                            try
                            {
                                kk = Long.parseLong(str);
                            }
                            catch(Exception e1){}
                            data[i][j] = new Long(kk);
                        } 
                        
                        
                    }
                     data[i][1] = " >> "; 
                     data[i][3] = adapter.getValueAt(i,1);
                     data[i][numCols-1] = new Boolean(true); 
                     wherekey[i] = kv.replaceFirst("AND", " WHERE");
            }
            for (int i=NUMROWS- extraline ; i < NUMROWS  ; i++)
            {
                    data[i] =  new Object[numCols+1];
                    data[i][0] = "";
                    data[i][1] = " >> ";
                    
                    data[i][numCols-1] = new Boolean(false);
            }
     
     
    return data;
  }
}

class RowComparator5 implements Comparator<Integer>
{
    int col = 0;
    Querys vs;
    public RowComparator5(int c, Querys x){col=c; vs = x;}
    public int compare(Integer r1, Integer r2) 
    {
        return Encryptor.compare(vs.data, vs.datatypes,  r1,  r2,  col, vs.direct[col]);
    }
}

