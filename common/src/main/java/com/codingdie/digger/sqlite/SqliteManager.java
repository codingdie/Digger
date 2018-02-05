package com.codingdie.digger.sqlite;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.function.Consumer;

/**
 * Created by xupeng on 17-7-25.
 */
public class SqliteManager {

    private SqlSessionFactory sqlSessionFactory;
    private String dbPath = "sqlite.db";
    private static SqliteManager manager;

    public static synchronized SqliteManager getInstance() {
        if (manager == null) {
            manager = new SqliteManager();
        }
        return manager;
    }

    public SqliteManager() {
        String filePath = "storage" + File.separator + dbPath;
        if (!new File(filePath).exists()) {
            initDB(dbPath);
        }
        DataSource dataSource = new UnpooledDataSource("org.sqlite.JDBC", "jdbc:sqlite:" + filePath, "", "");
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }


    private void initDB(String dbPath) {
        Connection connection = null;
        Statement statement = null;
        try {
            DataSource dataSource = new UnpooledDataSource("org.sqlite.JDBC", "jdbc:sqlite:" + "storage" + File.separator + "test.db", "", "");
            connection = dataSource.getConnection();
            String sql = "CREATE TABLE TIEBA_SPIDER " +
                    "(ID INT PRIMARY KEY    NOT NULL," +
                    " tiebaName    TEXT    NOT NULL, " +
                    " config  TEXT    NOT NULL" +
                    " status  int    NOT NULL";
            statement = connection.createStatement();
            statement.executeUpdate(sql);
            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (statement != null)
                    statement.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                if (connection != null)
                    connection.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public <T> void excute(Class<T> tClass, Consumer<T> consumer) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        T mapper = sqlSession.getMapper(tClass);
        consumer.accept(mapper);
        sqlSession.close();
    }

}
