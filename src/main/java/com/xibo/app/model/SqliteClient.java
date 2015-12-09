package com.xibo.app.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangx on 06/12/2015.
 */
public class SQLiteClient {
    public final String DATABASE_NAME = "jdbc:sqlite:market.db";
    public static final String TBL_EXISTING_STOCKS = "TBL_EXISTING_STOCKS";
    public static final String TBL_ALL_STOCKS = "TBL_ALL_STOCKS";
    private Connection connection;

    public SQLiteClient() {
        try {
            Class.forName("org.sqlite.JDBC");
            // will create db if not exist
            connection = DriverManager.getConnection(DATABASE_NAME);
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void createTableForStockMarket() {
        try (Statement statement = connection.createStatement();) {

            String sql = "CREATE TABLE IF NOT EXISTS " + TBL_EXISTING_STOCKS +
                    "(ticker CHAR(10) PRIMARY KEY NOT NULL," +
                    " secID           TEXT    NOT NULL, " +
                    " secShortName    TEXT    NOT NULL)";
            statement.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTableForAllStocks() {
        try (Statement statement = connection.createStatement();) {

            String sql = "CREATE TABLE IF NOT EXISTS " + TBL_ALL_STOCKS +
                    "(ticker          CHAR(10)NOT NULL," +
                    " tradeDate       TEXT    NOT NULL, " +
                    " secID           TEXT    NOT NULL, " +
                    " secShortName    TEXT    NOT NULL, " +
                    " closePrice      REAL, " +
                    " highestPrice    REAL, " +
                    " lowestPrice     REAL, " +
                    " openPrice       REAL, " +
                    " preClosePrice   REAL, " +
                    " dealAmount      INTEGER, " +
                    " PRIMARY KEY (ticker, tradeDate)) ";
            statement.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertValues(String ticker, String secID, String secShortName) {
        String sql = "INSERT INTO " + TBL_EXISTING_STOCKS + " VALUES (?,?,?);";
        try (PreparedStatement statement = connection.prepareStatement(sql);) {
            statement.setString(1, ticker);
            statement.setString(2, secID);
            statement.setString(3, secShortName);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void insertValues(StockInfo stock) {
        String sql = "INSERT OR IGNORE INTO " + TBL_ALL_STOCKS + " VALUES (?,?,?,?,?,?,?,?,?,?);";
        try (PreparedStatement statement = connection.prepareStatement(sql);) {
            statement.setString(1, stock.getTicker());
            statement.setString(2, stock.getTradeDate());
            statement.setString(3, stock.getSecID());
            statement.setString(4, stock.getSecShortName());
            statement.setDouble(5, stock.getClosePrice());
            statement.setDouble(6, stock.getHighestPrice());
            statement.setDouble(7, stock.getLowestPrice());
            statement.setDouble(8, stock.getOpenPrice());
            statement.setDouble(9, stock.getPreClosePrice());
            statement.setInt(10, stock.getDealAmount());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<StockInfo> getStocksBy(String ticker) {
        String sql = "SELECT * FROM " + TBL_ALL_STOCKS + " WHERE Ticker = ?;";
        List<StockInfo> stocks = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);) {
            statement.setString(1, ticker);

            ResultSet result = statement.executeQuery();
            while (result.next()) {
                stocks.add(new StockInfo(
                        result.getString(1),
                        result.getString(2),
                        result.getString(3),
                        result.getString(4),
                        result.getDouble(5),
                        result.getDouble(6),
                        result.getDouble(7),
                        result.getDouble(8),
                        result.getDouble(9),
                        result.getInt(10)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stocks;
    }

    public void deleteRows(String ticker) {
        String sql = "DELETE FROM " + TBL_EXISTING_STOCKS + " WHERE ticker = ?;";
        try (PreparedStatement statement = connection.prepareStatement(sql);) {
            statement.setString(1, ticker);
            statement.executeUpdate();
            System.out.println(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAllRows() {
        String sql = "DELETE FROM " + TBL_EXISTING_STOCKS + ";";
        try (PreparedStatement statement = connection.prepareStatement(sql);) {
            statement.executeUpdate();
            System.out.println(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteAllStocks() {
        String sql = "DELETE FROM " + TBL_ALL_STOCKS + ";";
        try (PreparedStatement statement = connection.prepareStatement(sql);) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTable(String tableName) {
        String sql = "DROP TABLE IF EXISTS " + tableName + ";";
        try (PreparedStatement statement = connection.prepareStatement(sql);) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<StockInfo> getStocksBy(int row) {
        List<StockInfo> existingStocks = getExistingStocks();
        List<StockInfo> stocks = new ArrayList<>();

        String sql = "SELECT * FROM " + TBL_ALL_STOCKS + " WHERE Ticker = ?;";
        try (PreparedStatement statement = connection.prepareStatement(sql);) {

            ResultSet result = statement.executeQuery();
            while (result.next()) {
                stocks.add(new StockInfo(
                        result.getString(1),
                        result.getString(2),
                        result.getString(3),
                        result.getString(4),
                        result.getDouble(5),
                        result.getDouble(6),
                        result.getDouble(7),
                        result.getDouble(8),
                        result.getDouble(9),
                        result.getInt(10)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stocks;
    }

    public List<StockInfo> getExistingStocks() {
        String sql = "SELECT * FROM " + TBL_EXISTING_STOCKS + ";";
        List<StockInfo> stocks = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);) {

            ResultSet result = statement.executeQuery();
            while (result.next()) {
                StockInfo stock = new StockInfo();
                stock.setTicker(result.getString(1));
                stock.setSecID(result.getString(2));
                stock.setSecShortName(result.getString(3));
                stocks.add(stock);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stocks;
    }

}