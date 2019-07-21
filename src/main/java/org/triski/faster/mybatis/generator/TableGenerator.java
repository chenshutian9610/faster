package org.triski.faster.mybatis.generator;

import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.triski.faster.commons.exception.FasterException;
import org.triski.faster.commons.utils.CamelCaseUtils;
import org.triski.faster.commons.utils.ClasspathUtils;
import org.triski.faster.commons.utils.PackageUtils;
import org.triski.faster.commons.utils.PropertiesUtils;
import org.triski.faster.mybatis.annotation.Column;
import org.triski.faster.mybatis.annotation.Table;

import java.io.File;
import java.io.IOException;
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

    private static final Logger logger = LoggerFactory.getLogger(TableGenerator.class);

    private static final String MYBATIS_GENERATOR_CONFIG = "mybatis/generatorConfig.xml";
    private static Set<String> KEYWORD;

    private Properties properties;
    private Map<String, String> class2table;
    private List<String> scripts;

    {
        // 读取配置文件
        if (initProperties("faster.properties") == false) {
            if (initProperties("faster.yml") == false) {
                if (initProperties("faster.yaml") == false) {
                    if (initProperties("application.properties") == false) {
                        if (initProperties("application.yml") == false) {
                            if (initProperties("application.yaml") == false) {
                                throw new FasterException("classpath don't exists any configure file!");
                            }
                        }
                    }
                }
            }
        }
        // 初始化 KEYWORD
        KEYWORD = initKeyword();
    }

    public TableGenerator() {
        RuntimeException exception = new RuntimeException();
        String name = exception.getStackTrace()[1].getClassName();
        class2table = init(name.substring(0, name.lastIndexOf(".")));
    }

    public TableGenerator(Class clazz) {
        String packageToScan = clazz.getPackage().getName();
        class2table = init(packageToScan);
    }

    public TableGenerator(String packageToScan) {
        class2table = init(packageToScan);
    }

    public void forward() {
        forward(false);
    }

    /**
     * 正向工程
     *
     * @param force 是否强制生成表
     */
    public void forward(boolean force) {
        try {
            if (scripts.size() == 0) {
                logger.warn("no table to generate");
                return;
            }

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

    public void reverse() {
        reverse(MYBATIS_GENERATOR_CONFIG);
    }

    /**
     * 逆向工程
     *
     * @param classpath 逆向工程配置文件位置, 默认为 mybatis/generatorConfig.xml
     */
    public void reverse(String classpath) {
        if (class2table.size() == 0) {
            logger.warn("class2table's size is 0");
            return;
        }
        try {
            // 获取逆向工程配置文件的流
            InputStream in = null;
            if (Objects.equals(MYBATIS_GENERATOR_CONFIG, classpath)) {
                MybatisXmlUtils.config(properties);
                in = MybatisXmlUtils.deal(classpath);
            } else {
                in = ClasspathUtils.getResourcesAsStream(classpath);
            }
            // 逆向工程开始
            List<String> warnings = new ArrayList<String>();
            Configuration config = new ConfigurationParser(warnings).parseConfiguration(in);
            DefaultShellCallback callback = new DefaultShellCallback(true);
            MyBatisGenerator generator = new MyBatisGenerator(config, callback, warnings);
//            generator.setXmlMerge(false);
            generator.generate(toTableConfigurations(class2table));

            in.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /****************************** 内部处理 *******************************/

    /** 初始化配置文件, 成功返回 true, 否则 false */
    private boolean initProperties(String filename) {
        File config = ClasspathUtils.getFile(filename);
        if (config.exists()) {
            properties = PropertiesUtils.getProperties(config);
            return true;
        }
        logger.debug("classpath don't exists '{}'", filename);
        return false;
    }

    /** 初始化 class2table 和 scripts */
    private Map<String, String> init(String packageToScan) {
        if (packageToScan == null) {
            throw new FasterException("invalid package: {}", packageToScan);
        }
        scripts = new ArrayList<>();

        List<Class> clazzList = PackageUtils.scan(packageToScan);
        Map<String, String> class2table = new HashMap<>();
        for (Class clazz : clazzList) {
            String name = clazz.getSimpleName();
            List<String> columnDefinitions = new ArrayList<>();
            StringBuilder ddl = new StringBuilder();
            Field[] fields = clazz.getDeclaredFields();
            Table table = (Table) clazz.getAnnotation(Table.class);
            if (table == null) continue;
            String tableName = table.name().length() == 0 ? CamelCaseUtils.toUnderline(name) : table.name();
            for (Field field : fields) {
                Column column = (Column) field.getAnnotation(Column.class);
                if (KEYWORD.contains(field.getName().toUpperCase()))
                    System.err.println(String.format("%s#%s 是关键字或保留字, 可能导致创建表不成功", name, field.getName()));
                columnDefinitions.add(getColumnDefinition(field, column).trim());
            }
            System.out.println();

            ddl.append(String.format("CREATE TABLE %s (\n", tableName));
            ddl.append(String.join(",\n", columnDefinitions));
            ddl.append(String.format("\n) %s COMMENT = '%s';", table.meta(), table.comment()));

            scripts.add(String.format("DROP TABLE IF EXISTS %s;", tableName));
            scripts.add(new String(ddl));


            class2table.put(name, tableName);
        }
        return class2table;
    }

    /** 读取 mysql.keyword.txt，获取 mysql 关键字 (5.7) */
    private static Set<String> initKeyword() {
        try (InputStream in = ClasspathUtils.getResourcesAsStream("mybatis/mysql.keyword.txt")) {
            Scanner scanner = new Scanner(in, "utf-8");
            Set<String> keyWords = new HashSet<>(666);
            while (scanner.hasNext()) {
                keyWords.add(scanner.next());
            }
            return keyWords;
        } catch (IOException e) {
            throw new FasterException(e);
        }
    }

    /** @example id varchar(40) not null primary key */
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

    private List<TableConfiguration> toTableConfigurations(Map<String, String> class2table) {
        List<TableConfiguration> tableConfigurations = new ArrayList<>();
        Context context = new Context(ModelType.CONDITIONAL);
        for (Map.Entry<String, String> entry : class2table.entrySet()) {
            TableConfiguration tc = new TableConfiguration(context);
            tc.setDomainObjectName(entry.getKey());
            tc.setMapperName(entry.getKey() + "Mapper");
            tc.setTableName(entry.getValue());
            tableConfigurations.add(tc);
        }
        return tableConfigurations;
    }
}
