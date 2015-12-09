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
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class RootController implements Initializable {
    public DatePicker beginDatePicker;
    public ComboBox conditionFilterBox;
    public ComboBox dataFilterBox;
    public ComboBox searchTypeBox;
    public SplitPane rootPane;
    public ProgressBar loadingProgressBar;
    public ComboBox previousDayBox;
    public ComboBox followingDayBox;
    public TextArea logArea;
    public Label currentProgress;
    private ObservableList<String> conditionList;
    private ObservableList<String> dataList;
    private ObservableList<String> searchTypeList;
    private ObservableList<String> dayList;
    // instance variable
    private ApiClient client;
    private InMemoryCache cache = new InMemoryCache();
    private List<StockInfo> selectedStocks;
    private List<List<StockInfo>> relatedStocks;
    private FileChooser fileChooser = new FileChooser();
    private ExcelFactory excelFactory = new ExcelFactory();
    private DataReader dataReader = new DataReader();
    private List<StockInfo> existingStocks = new ArrayList<>();

    public RootController() {
        conditionList = FXCollections.observableArrayList(
                "选择", "涨停", "其他没想好"
        );
        dataList = FXCollections.observableArrayList(
                "全选", "最高价", "最低价", "开盘价", "收盘价", "交易量"
        );
        searchTypeList = FXCollections.observableArrayList(
                "所有股票"
        );
        dayList = FXCollections.observableArrayList(
                "0日", "1日", "2日", "3日", "4日", "5日", "6日", "7日", "8日", "9日", "10日"
        );
        // set delegate as self
        client = new ApiClient(this);
    }

    public void confirmStockId(Event event) {
        getLimitedUpStocksOn(beginDatePicker.getValue().toString());
    }

    public void filterStock(ActionEvent actionEvent) {
        relatedStocks = new ArrayList<>();
        int previousDays = previousDayBox.getSelectionModel().getSelectedIndex();
        int followingDays = followingDayBox.getSelectionModel().getSelectedIndex();
        System.out.println(selectedStocks);
        for (StockInfo stock : selectedStocks) {
            relatedStocks.add(cache.findStockInfoBtw(stock, previousDays, followingDays));
        }
        log(String.format("获得 %d 涨停股票, 前%d天, 后%d天 相关数据", selectedStocks.size(), previousDays, followingDays));
    }

    public void outputData(ActionEvent actionEvent) {
        log("正在导出数据");
        System.out.println(selectedStocks);
        DirectoryChooser directoryChooser = new DirectoryChooser();
        fileChooser.setTitle("Save Excel File");
        File fileLocation = directoryChooser.showDialog(this.logArea.getScene().getWindow());
        excelFactory.createExcelFrom(relatedStocks, previousDayBox.getSelectionModel().getSelectedIndex());
        try {
            String title = relatedStocks.get(0).get(previousDayBox.getSelectionModel().getSelectedIndex()).getTradeDate();
            excelFactory.saveFileTo(fileLocation.getAbsolutePath(), title + "涨停股票数据统计.xlsx");
            log("成功保存excel");
        } catch (Exception e) {
            log(e.toString());
            log("无法保存: 把这段复制下来发给我");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initUI();
    }


    private void getLimitedUpStocksOn(String tradeDate) {
        selectedStocks = cache.findLimitUpStocksOn(tradeDate);
        if (selectedStocks.size() == 0) {
            log("本日无涨停股票");
        } else {
            log(String.format("已经获得 %s 全部涨停股票数据, 共有 %d 只股票", tradeDate, selectedStocks.size()));
        }
        System.out.println(selectedStocks);
    }

    private void initUI() {
        beginDatePicker.setEditable(false);
        beginDatePicker.setPromptText("输入日期");

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
                loadingProgressBar.setProgress(-1d);
                break;
            case SEARCH_FINISHED:
                loadingProgressBar.setProgress(0d);
                break;
            case SEARCHED_ALL_STOCKS:
                break;
            case SEARCHED_MULTI_STOCKS:
                break;
            default:
        }
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
        Platform.runLater(() -> {
            logArea.appendText(logInfo);
            logArea.appendText("\n");
            System.out.println(logInfo);
        });
    }

    public void clearAll(ActionEvent actionEvent) {
        logArea.clear();
        log("清空记录");
    }

    public void selectSearchType(ActionEvent actionEvent) {
        int selectedIndex = searchTypeBox.getSelectionModel().getSelectedIndex();
        if (selectedIndex == 1) {
            updateUI(UIStatus.SEARCHED_SINGLE_STOCK);
        } else if (selectedIndex == 0) {
            updateUI(UIStatus.SEARCHED_ALL_STOCKS);
        }
    }

    public void loadCache(ActionEvent actionEvent) {
        fileChooser.setTitle("Open Resource File");
        File location = fileChooser.showOpenDialog(this.logArea.getScene().getWindow());
        if (location == null) return;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(
                    new FileInputStream(location));
            cache = (InMemoryCache) objectInputStream.readObject();
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

    public void initCache(ActionEvent actionEvent) {
        client = new ApiClient(this);
        log(String.format("正在获得数据..."));
        client.readDataFromAPI("/api/market/getMktEqud.json?field=&tradeDate=20151208", 10, 5, s -> {
            if (s != null) {
                log("正在处理数据...");
                existingStocks = dataReader.readStocksFrom(s);
                if (existingStocks != null) {
                    log("成功初始化缓存");
                } else {
                    log("无法初始化");
                }
            } else {
                log("无法获得数据");
            }
        });
    }

    public void setProgress(String progress) {
        Platform.runLater(() -> currentProgress.setText(progress));
    }

    private int progress;
    int interval = 100;
    private ExecutorService thread = Executors.newSingleThreadExecutor();

    public void buildCache(ActionEvent actionEvent) {
        client = new ApiClient(this);
        log(String.format("正在获得数据..."));
        setProgress("0");
        thread.submit(() -> {
            client.readMultiDataFromAPI(existingStocks);
            log("完成 " + progress + "/" + existingStocks.size());
        });

        System.out.println(existingStocks);
    }

}
