package org.triski.faster.mybatis.generator.main;

import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.triski.faster.commons.utils.CamelCaseUtils;
import org.triski.faster.commons.utils.PackageUtils;
import org.triski.faster.commons.utils.PropertiesUtils;
import org.triski.faster.commons.utils.YamlUtils;
import org.triski.faster.mybatis.annotation.Column;
import org.triski.faster.mybatis.annotation.Table;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.*;

/**
 * @author triski
 * @date 2018/12/28
 */
public class TableGenerator {

    /* 默认的配置文件名 */
    private String mybatisXML = "mybatis-generate.xml";
    private String configProperties = "generator.properties";

    /* 自定义 mybatis 配置文件 */
    private boolean custom = true;

    private Properties properties;
    private Map<String, String> tableMap;
    private List<String> scripts = new ArrayList<>();

    /****************************** 配置接口 *******************************/

    public TableGenerator() {
        RuntimeException exception = new RuntimeException();
        String name = exception.getStackTrace()[1].getClassName();
        tableMap = scanInit(name.substring(0, name.lastIndexOf(".")));
    }

    public TableGenerator(Class clazz) {
        String packageToScan = clazz.getPackage().getName();
        tableMap = scanInit(packageToScan);
    }

    public TableGenerator(String packageToScan) {
        tableMap = scanInit(packageToScan);
    }

    public void loadProperties(String path) {
        properties = path.endsWith("properties") ?
                PropertiesUtils.getProperties(path) : YamlUtils.getProperties(path);
    }

    public void setMybatisXml(String xml) {
        this.mybatisXML = xml;
    }

    /****************************** 对外开放接口 *******************************/

    /* 正向工程一号入口：非强制生成表 */
    public void forward() {
        forward(false);
    }

    /* 正向工程二号入口：强制生成表 */
    public void forward(boolean force) {
        try {
            if (scripts.size() == 0) return;

            if (properties == null)
                loadProperties(configProperties);

            String driver = properties.getProperty("jdbc.driver");
            if (driver == null) driver = "com.mysql.jdbc.Driver";

            Class.forName(driver);
            String url = properties.getProperty("jdbc.url");
            String username = properties.getProperty("jdbc.username");
            String password = properties.getProperty("jdbc.password");
            Connection connection = DriverManager.getConnection(url, username, password);
            Statement statement = connection.createStatement();
            for (String script : scripts) {
                if (!force && script.startsWith("DROP"))
                    continue;
                statement.execute(script);
                System.out.println(script + "\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* 逆向工程一号入口 */
    public void reverse() {
        custom = false;
        reverse(mybatisXML);
    }

    /* 逆向工程二号入口 */
    public void reverse(String mybatisConfig) {
        if (tableMap.size() == 0)
            return;

        if (properties == null)
            loadProperties(configProperties);

        try {
            /* 如果是自定义配置文件，则不能使用 ${variable:defaultValue} 这种格式（mybatis 本身不支持）*/
            InputStream in = null;
            if (custom) {
                in = this.getClass().getClassLoader().getResourceAsStream(mybatisConfig);
            } else {
                MybatisXmlUtils.config(properties);
                in = MybatisXmlUtils.deal(mybatisConfig);
            }

            List<String> warnings = new ArrayList<String>();
            Configuration config = new ConfigurationParser(warnings).parseConfiguration(in);
            DefaultShellCallback callback = new DefaultShellCallback(true);

            /* 复制自 org.mybatis.generator.api.MyBatisGenerator，扩展了 xml 融合和表生成策略 */
            MyBatisGeneratorExtension generator = new MyBatisGeneratorExtension(config, callback, warnings);
            generator.setXmlMerge(false);
            generator.generate(getTableConfigurations(tableMap));

            in.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /****************************** 内部处理 *******************************/

    /* 初始化 tableMap 和 scripts */
    private Map<String, String> scanInit(String packageToScan) {
        if (packageToScan == null)
            throw new RuntimeException(String.format("包路径（%s）无效！", packageToScan));

        Field[] fields;
        Table table;
        Column column;
        String tableName;
        Set<String> keyWords = getKeyWords();
        List<Class> clazzList = PackageUtils.scan(packageToScan);
        Map<String, String> tableMap = new HashMap<>();
        for (Class clazz : clazzList) {
            String name = clazz.getSimpleName();
            List<String> columnDefinitions = new ArrayList<>();
            StringBuilder ddl = new StringBuilder();
            fields = clazz.getDeclaredFields();
            table = (Table) clazz.getAnnotation(Table.class);
            if (table == null) continue;
            tableName = table.name().length() == 0 ? CamelCaseUtils.toUnderline(name) : table.name();
            for (Field field : fields) {
                column = (Column) field.getAnnotation(Column.class);
                if (keyWords.contains(field.getName().toUpperCase()))
                    System.err.println(String.format("%s#%s 是关键字或保留字, 可能导致创建表不成功", name, field.getName()));
                columnDefinitions.add(getColumnDefinition(field, column).trim());
            }
            System.out.println();

            if (table.generate()) {
                ddl.append(String.format("CREATE TABLE %s (\n", tableName));
                ddl.append(String.join(",\n", columnDefinitions));
                ddl.append(String.format("\n) %s COMMENT = '%s';", table.meta(), table.comment()));

                scripts.add(String.format("DROP TABLE IF EXISTS %s;", tableName));
                scripts.add(new String(ddl));
            }

            tableMap.put(name, tableName);
        }
        return tableMap;
    }

    /* 读取 MySQL_KEY_WORD，获取 mysql 关键字（5.7） */
    private Set<String> getKeyWords() {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("MySQL_KEY_WORD");
        Scanner scanner = new Scanner(in, "utf-8");
        Set<String> keyWords = new HashSet<>(666);
        while (scanner.hasNext())
            keyWords.add(scanner.next());
        return keyWords;
    }

    /* 获取字段定义信息 */
    private String getColumnDefinition(Field field, Column column) {
        String name = field.getName();
        String type = field.getType().getSimpleName();
        String defaultValue = "", comment = "", id = "", unique = "";
        int length = 40;

        if (column != null) {
            defaultValue = column.defaultValue();
            length = column.length();
            if (column.comment().length() != 0)
                comment = String.format("COMMENT '%s'", column.comment());
            if (column.id())
                id = "PRIMARY KEY" + (column.autoIncrement() ? " AUTO_INCREMENT" : "");
            if (column.unique())
                unique = "UNIQUE KEY";
        }

        switch (type.toLowerCase()) {
            case "long":
                type = "BIGINT";
                defaultValue = String.format("DEFAULT '%s'", defaultValue.length() == 0 ? "0" : defaultValue);
                break;
            case "int":
            case "integer":
                type = "INTEGER";
                defaultValue = String.format("DEFAULT '%s'", defaultValue.length() == 0 ? "0" : defaultValue);
                break;
            case "byte":
                type = "TINYINT";
                defaultValue = String.format("DEFAULT '%s'", defaultValue.length() == 0 ? "0" : defaultValue);
                break;
            case "boolean":
                type = "BIT";
                defaultValue = String.format("DEFAULT b'%s'",
                        "true".equals(defaultValue) || "1".equals(defaultValue) ? "1" : "0");
                break;
            case "string":
                type = String.format("VARCHAR(%d)", length);
                defaultValue = String.format("DEFAULT '%s'", defaultValue);
                break;
            case "date":
                type = "DATETIME";
                break;
            case "char":
            case "character":
                type = "CHAR(1)";
                defaultValue = String.format("DEFAULT '%s'", defaultValue);
                break;
            case "bigdecimal":
                type = "DECIMAL(19,2)";
                defaultValue = String.format("DEFAULT '%s'", defaultValue.length() == 0 ? "0" : defaultValue);
        }

        if (id.length() != 0 || unique.length() != 0)
            defaultValue = "";

        return String.format("\t%s %s %s %s %s %s", name, type, defaultValue, unique, id, comment);
    }

    /* 辅助方法, 用于获取 TableConfiguration 列表 */
    private List<TableConfiguration> getTableConfigurations(Map<String, String> map) {
        List<TableConfiguration> tableConfigurations = new ArrayList<>();
        Context context = new Context(ModelType.CONDITIONAL);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            TableConfiguration tc = new TableConfiguration(context);
            tc.setDomainObjectName(entry.getKey());
            tc.setMapperName(entry.getKey() + "Mapper");
            tc.setTableName(entry.getValue());
            tableConfigurations.add(tc);
        }
        return tableConfigurations;
    }
}
