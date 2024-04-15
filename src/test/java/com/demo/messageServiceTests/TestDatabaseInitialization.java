package com.demo.messageServiceTests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class TestDatabaseInitialization {
    /**
     * 用来执行测试之前对数据库的更改
     * @param path_to_your_sql_file  测试对应的sql预处理语句
     * @throws Exception
     */
    public void initializeDatabase(String path_to_your_sql_file) throws Exception {
        // 1. 连接数据库
        String url = "jdbc:mysql://localhost:3306/demo_db?serverTimezone=UTC";
        String username = "root";
        String password = "123456";
        Connection connection = DriverManager.getConnection(url, username, password);

        // 2. 读取 SQL 文件内容
        StringBuilder sqlContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path_to_your_sql_file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sqlContent.append(line);
            }
        }

        // 3. 执行 SQL 语句
        String[] sqlStatements = sqlContent.toString().split(";");
        Statement statement = connection.createStatement();
        for (String sqlStatement : sqlStatements) {
            statement.addBatch(sqlStatement);
        }
        statement.executeBatch();

        // 关闭连接
        statement.close();
        connection.close();
    }
}