import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Update by internetroot on 2014-09-06.
 */
public class SqliteSQLGenerator {

    private static Connection conn = null;
    private static Statement sm = null;
    private static String insert = "INSERT INTO";//插入sql
    private static String delete = "DELETE FROM"; //删除sql
    private static String values = "VALUES";//values关键字
    private static String where = "WHERE"; //where关键字
    private static List<String> tableList = new ArrayList<String>();//全局存放表名列表
    private static List<String> insertList = new ArrayList<String>();//INSERT INTO 列表
    private static List<String> deleteList = new ArrayList<>(); // DELETE列表
//    private static List<String> apiCode = new ArrayList<>(); // api_code值列表
    private static String insertFilePath;//绝对路径 导出insert语句数据的文件
    private static String deleteFilePath; // delete语句sql文件

    public static String generateTableDataSQL(String sql, String[] params) {
        return null;
    }

    public static String executeSelectSQLFile(String file, String[] params, String domain) throws Exception {
        List<String> listSQL = new ArrayList<String>();
        connectSQL("com.mysql.jdbc.Driver", "jdbc:mysql://132.122.1.94:19001/sps_cfg?useUnicode=true&characterEncoding=UTF-8", "sps_cfg", "Ab123456!@#");//连接数据库
        listSQL = createSQL(file);//创建查询语句
        executeSQL(conn, sm, listSQL, tableList, domain);//执行sql并拼装
        createInsertFile();//创建insertSql文件
        createDeleteFile();
        return null;
    }

    /**
     * 拼装查询语句
     *
     * @return 返回select集合
     */
    private static List<String> createSQL(String file) throws Exception {
        List<String> listSQL = new ArrayList<String>();
        BufferedReader br = null;
        InputStreamReader fr = null;
        InputStream is = null;

        int i;//tableName表名的第一个字符位置
        int k;//tableName表名单最后一个字符的位置;  from tableName
        String tableName;

        try {
//            is = SqliteSQLGenerator.class.getResourceAsStream(file);
//            fr = new InputStreamReader(is);

            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String rec = null;//一行
            while ((rec = br.readLine()) != null) {
//                rec = rec.toLowerCase();
                if(("").equals(rec.trim())){
                    continue;
                }
                rec = rec.replaceAll("\\s{2,}", " ");
                i = rec.indexOf("from ", 1) + 5;
                if(i == 4){
                    i = rec.indexOf("FROM ", 1) + 5;
                }
                k = rec.indexOf(" ", i);
                if (k == -1) {
                    k = rec.length();
                }
                tableName = rec.substring(i, k);
                tableList.add(tableName);
                //获取所有查询语句
                listSQL.add(rec.toString());
            }

        } catch (Exception e){
            throw e;
        } finally {
            if (br != null) {
                br.close();
            }
            if (fr != null) {
                fr.close();
            }
            if (is != null) {
                is.close();
            }
        }
        return listSQL;
    }

    /**
     * 创建deletesql.txt并导出数据
     */
    private static void createDeleteFile() {
        File file = new File(deleteFilePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("创建delete文件名失败！！");
                e.printStackTrace();
            }
        }
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            if (deleteList.size() > 0) {
                for (int i = 0; i < deleteList.size(); i++) {
                    bw.append(deleteList.get(i));
                    bw.append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建insertsql.txt并导出数据
     */
    private static void createInsertFile() {
        File file = new File(insertFilePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("创建insert文件名失败！！");
                e.printStackTrace();
            }
        }
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            if (insertList.size() > 0) {
                for (int i = 0; i < insertList.size(); i++) {
                    bw.append(insertList.get(i));
                    bw.append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 连接数据库 创建statement对象
     *
     * @param driver
     * @param url
     * @param UserName
     * @param Password
     */
    public static void connectSQL(String driver, String url, String UserName, String Password) {
        try {
            Class.forName(driver).newInstance();
            conn = DriverManager.getConnection(url, UserName, Password);
            sm = conn.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行sql并返回插入sql
     * @param conn
     * @param sm
     * @param listSQL
     */
    public static void executeSQL(Connection conn, Statement sm, List listSQL, List listTable, String domain) throws SQLException {
        List<String> insertSQL = new ArrayList<String>();
        ResultSet rs = null;
        try {
            rs = getColumnNameAndColumeValue(sm, listSQL, listTable, rs, domain);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            rs.close();
            sm.close();
            conn.close();
        }
    }

    /**
     * 获取列名和列值
     *
     * @param sm
     * @param listSQL
     * @param rs
     * @return
     * @throws java.sql.SQLException
     */
    private static ResultSet getColumnNameAndColumeValue(Statement sm,
                                                         List listSQL, List ListTable, ResultSet rs, String domain) throws SQLException {
        for (int j = 0; j < listSQL.size(); j++) {
            String sql = String.valueOf(listSQL.get(j));
            rs = sm.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            while (rs.next()) {
                StringBuffer ColumnName = new StringBuffer();
                StringBuffer ColumnValue = new StringBuffer();
                String apiCode = "";
                String ownerType = "";
                String cfsId = "";
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);

                    String colName = rsmd.getColumnName(i);
                    ColumnName.append(colName + ",");
                    if (Types.CHAR == rsmd.getColumnType(i) || Types.VARCHAR == rsmd.getColumnType(i)
                            || Types.LONGVARCHAR == rsmd.getColumnType(i)) {
                        if (value == null) {
                            ColumnValue.append("null,");
                        } else {
                            ColumnValue.append("'").append(value).append("',");
                        }
                    } else if (Types.SMALLINT == rsmd.getColumnType(i) || Types.INTEGER == rsmd.getColumnType(i)
                            || Types.BIGINT == rsmd.getColumnType(i) || Types.FLOAT == rsmd.getColumnType(i)
                            || Types.DOUBLE == rsmd.getColumnType(i) || Types.NUMERIC == rsmd.getColumnType(i)
                            || Types.DECIMAL == rsmd.getColumnType(i)) {
                        if (value == null) {
                            ColumnValue.append("null,");
                        } else {
                            ColumnValue.append(value).append(",");
                        }
                    } else if (Types.DATE == rsmd.getColumnType(i) || Types.TIME == rsmd.getColumnType(i)
                            || Types.TIMESTAMP == rsmd.getColumnType(i)) {
                        if (value == null) {
                            ColumnValue.append("null,");
                        } else {
                            ColumnValue.append("'").append(value).append("',");
                        }
                    } else {
                        if (value == null) {
                            ColumnValue.append("null,");
                        } else {
                            ColumnValue.append(value).append(",");
                        }
                    }

                    //api维度,
                    if(("4").equals(domain)){
                        if(colName.equals("api_code")) {
                            apiCode = "'" + value + "'";
                        }
                        if(colName.equals("owner_type")){
                            ownerType = value;
                        }
                        if(colName.equals("owner_obj_id")){
                            cfsId = value;
                        }
                    }
                }
                if(("4").equals(domain)){
                    exoprtObj(ownerType, cfsId);
                }
                //System.out.println(ColumnName.toString());
                //System.out.println(ColumnValue.toString());
                ColumnValue.deleteCharAt(ColumnValue.length() - 1); // 去掉最后一个字符”,“


                deleteSQL(ListTable.get(j).toString(), domain, apiCode);
                insertSQL(ListTable.get(j).toString(), ColumnName, ColumnValue);
            }
        }
        return rs;
    }

    /**
     * 导出对象相关信息
     * @param ownerType
     * @param cfsId
     */
    private static void exoprtObj(String ownerType, String cfsId){
        StringBuffer sql = new StringBuffer();
        String tableName = "";
        switch(ownerType){
            case Constant.OBJType.CFS:
                tableName = Constant.TableName.CFS;
                break;
            case Constant.OBJType.RFS:
                tableName = Constant.TableName.RFS;
                break;
            case Constant.OBJType.RES:
                tableName = Constant.TableName.RES;
                break;
            default:
                System.out.println("对象类型ownerType获取不正确" + ownerType);
                return;
        }
        sql.append("SELECT * FROM ");
        sql.append(tableName).append(" ");
        sql.append(where).append(" ").append("cfs_id = ").append(cfsId); //TODO 使用:cfsId

    }

    /**
     * 拼装insertsql 放到全局list里面
     *
     * @param ColumnName
     * @param ColumnValue
     */
    private static void insertSQL(String TableName, StringBuffer ColumnName,
                                  StringBuffer ColumnValue) {
        StringBuffer insertSQL = new StringBuffer();
        insertSQL.append(insert).append(" ").append(TableName).append("(").append(ColumnName.toString())
                .append(")").append(values).append("(").append(ColumnValue.toString()).append(");");
        insertList.add(insertSQL.toString());
        System.out.println(insertSQL.toString());
    }

    /**
     * 拼装deleteSQL 放到全局list里面
     *
     * @param TableName
     * @param domain
     */
    private static void deleteSQL(String TableName, String domain, String condition) {
        StringBuffer deleteSQL = new StringBuffer();
        if(("4").equals(domain)) {
            deleteSQL.append(delete).append(" ").append(TableName).append(" ").append(where).append(" ")
                    .append("api_code =").append(condition).append(";");
            deleteList.add(deleteSQL.toString());
            System.out.println(deleteSQL.toString());
        }
    }

    public static void main(String[] args) throws Exception {
        // 使用Class.getResourceAsStream读取文件时用相对路径
//        String file2 = "export_sqlite_data_select.txt";

//        String file2 = "D:\\code\\insert\\src\\main\\resources\\export_sqlite_data_select.txt";
//        insertFilePath = "D:\\code\\insert\\src\\main\\resources\\insertApi.sql";
//        deleteFilePath = "D:\\code\\insert\\src\\main\\resources\\deleteApi.sql";

        String file2 = args[0];
        insertFilePath = args[1];
//        String domain = args[2];
        executeSelectSQLFile(file2, null, "4");

//        String str = "SELECT * FROM  cf_api_info  aaaa   aaadw      asdqw1";
//        str = str.replaceAll("\\s{2,}", " ");
//        System.out.println(str);
    }
}