package com.xibo.app.controller;

import com.xibo.app.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


public class RootController implements Initializable {
    public DatePicker endDatePicker;
    public DatePicker beginDatePicker;
    public TextField stockIdField;
    public Button confirmButton;
    public ComboBox conditionFilterBox;
    public ComboBox dataFilterBox;
    public ComboBox otherFilterBox;
    public ComboBox searchTypeBox;
    public Button filterButton;
    public Button outputButton;
    public SplitPane rootPane;
    public ProgressBar loadingProgressBar;
    public ComboBox previousDayBox;
    public ComboBox followingDayBox;
    public TextArea logArea;
    private ObservableList<String> conditionList;
    private ObservableList<String> dataList;
    private ObservableList<String> searchTypeList;
    private ObservableList<String> dayList;
    // instance variable
    private Client client;
    private List<StockInfo> tempCache = new LinkedList<>();
    private InMemoryCache cache = new InMemoryCache();
    private List<List<StockInfo>> selectedStocks;
    private String tempJSON;
    private FileChooser fileChooser = new FileChooser();
    private ExcelFactory excelFactory = new ExcelFactory();
    private DataReader dataReader = new DataReader();
    public RootController() {
        conditionList = FXCollections.observableArrayList(
                "选择", "涨停", "其他没想好"
        );
        dataList = FXCollections.observableArrayList(
                "全选", "最高价", "最低价", "开盘价", "收盘价", "交易量"
        );
        searchTypeList = FXCollections.observableArrayList(
                "所有股票", "单一股票"
                );
        dayList = FXCollections.observableArrayList(
                "0日","1日","2日","3日","4日","5日","6日","7日","8日","9日", "10日"
        );
        // set delegate as self
        client = new Client(this);
    }

    public void confirmStockId(Event event) {
        switch (searchTypeBox.getSelectionModel().getSelectedIndex()) {
            case 0:
                fetchAllStocksData();
                break;
            case 1:
                fetchSingleStockData();
                break;
        }
    }

    public void filterStock(ActionEvent actionEvent) {
        selectedStocks = new ArrayList<>();
        int previousDays = previousDayBox.getSelectionModel().getSelectedIndex();
        int followingDays = followingDayBox.getSelectionModel().getSelectedIndex();
        for (StockInfo stock : tempCache) {
            selectedStocks.add(cache.findStockInfoBtw(stock, previousDays, followingDays));
        }
        log(String.format("获得 %d 涨停股票, 前%d天, 后%d天 相关数据", selectedStocks.size(), previousDays, followingDays));
    }

    public void outputData(ActionEvent actionEvent) {
        log("正在导出数据");
        System.out.println(selectedStocks);
        DirectoryChooser directoryChooser = new DirectoryChooser();
        fileChooser.setTitle("Save Excel File");
        File fileLocation = directoryChooser.showDialog(this.logArea.getScene().getWindow());
        excelFactory.createExcelFrom(selectedStocks, previousDayBox.getSelectionModel().getSelectedIndex());
        String title = selectedStocks.get(0).get(previousDayBox.getSelectionModel().getSelectedIndex()).getTradeDate();
        excelFactory.saveFileTo(fileLocation.getAbsolutePath(), title + "涨停股票数据统计.xlsx");
        log("成功保存excel");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initUI();
    }

    private void fetchSingleStockData() {
        sendAlert("该功能已经被停用");
        return;
        /*
        String errorMsg = "";
        LocalDate beginLocalDate = beginDatePicker.getValue();
        LocalDate endLocalDate = endDatePicker.getValue();
        if (stockIdField.getText().isEmpty()) {
            errorMsg = "股票代码不能为空 \n";
        }
        if (beginLocalDate == null) {
            errorMsg += "开始日期不能为空 \n";
        }
        if (endLocalDate == null) {
            errorMsg += "结束日期不能为空 \n";
        }
        if (beginLocalDate != null && endLocalDate != null) {
            if (!endLocalDate.isAfter(beginLocalDate) && !endLocalDate.isEqual(beginLocalDate)) {
                errorMsg += "结束日期必须大于开始日期\n";
            }
            if (endLocalDate.isAfter(LocalDate.now())) {
                errorMsg += "结束日期必须小于今天";
            }
            if (beginLocalDate.isAfter(LocalDate.now())) {
                errorMsg += "开始日期必须小于今天";
            }
        }

        // one year is an acceptable date fetching period
        if (!errorMsg.isEmpty()) {
            sendAlert(errorMsg);
            return;
        }

        updateUI(UIStatus.SEARCH_CONFIRMED);

        String stockId = stockIdField.getText();
        String beginDate = beginLocalDate.toString().replace("-", "");
        String endDate = beginLocalDate.toString().replace("-", "");
        System.out.println("Fetch stock data: stockId :" + stockId + " since: " + beginDate + " to: " + endDate);
        client.readDataFromAPI("/api/market/getMktEqud.json?field=&beginDate=" + beginDate + "&endDate=" + endDate + "&ticker=" + stockId);
        */
    }

    private void fetchAllStocksData() {
        String errorMsg = "";
        LocalDate beginLocalDate = beginDatePicker.getValue();
        if (beginLocalDate == null) {
            errorMsg += "开始日期不能为空 \n";
        }
        // one year is an acceptable date fetching period
        if (!errorMsg.isEmpty()) {
            sendAlert(errorMsg);
            return;
        }
        updateUI(UIStatus.SEARCH_CONFIRMED);
        String beginDate = beginDatePicker.getValue().toString().replace("-", "");
        System.out.println("Fetch all stock data in: " + beginDate);
        client.readDataFromAPI("/api/market/getMktEqud.json?field=&tradeDate=" + beginDate, (s)-> {

            if (s != null) {
                log(String.format("正在处理数据..."));
                List<StockInfo> result = dataReader.readStocksFrom(s);
                if (result == null) {
                    log("无法获得数据");

                }
                else findLimitUpsInStocksFrom(result);
            }
        });
        log(String.format("正在获取 %s 全部股票数据", beginDate));
    }

    private void findLimitUpsInStocksFrom(List<StockInfo> stocks) {
        tempCache = stocks.stream().filter(s->s.isLimitUp()).collect(Collectors.toList());
        String tradeDate = stocks.get(0).getTradeDate();
        log(String.format("已经获得 %s 全部涨停股票数据, 共有 %d 只股票", tradeDate, tempCache.size()));
        client.readMultiDataFromAPI(tempCache);
    }

    private void initUI() {
        beginDatePicker.setEditable(false);
        beginDatePicker.setPromptText("输入日期");
        endDatePicker.setDisable(true);

        setupComboBox(conditionFilterBox, conditionList);
        setupComboBox(dataFilterBox, dataList);
        setupComboBox(searchTypeBox, searchTypeList);
        setupComboBox(previousDayBox, dayList);
        setupComboBox(followingDayBox, dayList);

        logArea.setEditable(false);
        logArea.setWrapText(true);
        log("Application started ...");
    }

    public void updateUI(UIStatus status) {
        switch (status) {
            case SEARCH_CONFIRMED:
                confirmButton.setDisable(true);
                loadingProgressBar.setProgress(-1d);
                break;
            case SEARCH_FINISHED:
                confirmButton.setDisable(false);
                endDatePicker.setDisable(false);
                loadingProgressBar.setProgress(0d);
                break;
            case SEARCHED_ALL_STOCKS:
                break;
            case SEARCHED_SINGLE_STOCK:
                storeStockDayInfoToCache();
                break;
            case SEARCHED_MULTI_STOCKS:
                break;
            default:
        }
    }

    private void storeStockDayInfoToCache() {
        List<StockInfo> stockInfoList = dataReader.readStocksFrom(tempJSON);
        StockInfo sampleStock = stockInfoList.get(stockInfoList.size() - 1);
        cache.store(stockInfoList);
        log(String.format("获得 %s 历史数据, 共 %d 条", sampleStock.getSecShortName(), stockInfoList.size()));
    }

    public void sendAlert(String msg) {
        // send error message and return
        Alert errorAlert = new Alert(Alert.AlertType.WARNING);
        errorAlert.setTitle("参数错误");
        errorAlert.setContentText(msg);
        errorAlert.setHeaderText(null);
        errorAlert.show();
    }

    public void log(String logInfo) {
        logArea.appendText(logInfo);
        logArea.appendText("\n");
    }

    public void clearAll(ActionEvent actionEvent) {
        logArea.clear();
        log("清空记录");
        tempCache.clear();
        log("清空全部缓存");
    }

    public void selectSearchType(ActionEvent actionEvent) {
        int selectedIndex = searchTypeBox.getSelectionModel().getSelectedIndex();
        if (selectedIndex == 1) {
            updateUI(UIStatus.SEARCHED_SINGLE_STOCK);
        } else if (selectedIndex == 0) {
            updateUI(UIStatus.SEARCHED_ALL_STOCKS);
        }
    }

    public void setTempJSON(String tempJSON) {
        this.tempJSON = tempJSON;
    }

    public String getTempJSON() {
        return tempJSON;
    }

    public void loadCache(ActionEvent actionEvent) {
        fileChooser.setTitle("Open Resource File");
        File location = fileChooser.showOpenDialog(this.logArea.getScene().getWindow());
        if (location == null) return;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(
                    new FileInputStream(location));
            cache = (InMemoryCache)objectInputStream.readObject();
            log("成功读取缓存数据");
            System.out.println(cache);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void saveCache(ActionEvent actionEvent) {
        fileChooser.setTitle("Save Resource File");
        File fileLocation = fileChooser.showSaveDialog(this.logArea.getScene().getWindow());
        if (fileLocation == null) return;
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                    new FileOutputStream(fileLocation));
            objectOutputStream.writeObject(cache);
            objectOutputStream.flush();
            objectOutputStream.close();
            log("成功保存缓存数据");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public InMemoryCache getCache() {
        return cache;
    }

    public void setupComboBox(ComboBox box, ObservableList list) {
        box.getItems().addAll(list);
        box.getSelectionModel().select(0);
    }
}
