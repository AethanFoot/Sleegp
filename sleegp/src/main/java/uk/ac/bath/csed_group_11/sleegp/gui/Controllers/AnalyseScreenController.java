package uk.ac.bath.csed_group_11.sleegp.gui.Controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import uk.ac.bath.csed_group_11.sleegp.gui.Experiment.ExperimentManager;
import uk.ac.bath.csed_group_11.sleegp.gui.Utilities.Resource;
import uk.ac.bath.csed_group_11.sleegp.gui.Utilities.SceneUtils;
import uk.ac.bath.csed_group_11.sleegp.logic.Classification.ClassificationUtils;
import uk.ac.bath.csed_group_11.sleegp.logic.Classification.Plot;
import uk.ac.bath.csed_group_11.sleegp.logic.data.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * To be written by: Aethan and Xander
 */
public class AnalyseScreenController implements Initializable {
    @FXML
    AnchorPane mainPane;

    @FXML
    private ComboBox<String> processedCombo;
    private ObservableList<String> processedComboData = FXCollections.observableArrayList();

    @FXML
    TableView<TableData> processedTable;

    TableColumn<TableData, String> dateColumn;
    TableColumn<TableData, Double> percentageColumn;
    TableColumn<TableData, Double> timeColumn;

    @FXML
    BarChart<String, Number> barChart;

    @FXML
    LineChart<Number, Number> lineChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!ExperimentManager.isExperimentMode()) {
            setupTable();
            listenForTableWidthChange();
        }
        addToLastWeek();
        comboBoxSetup();
        listenForComboAction();
    }

    public void setupTable() {
        Platform.runLater(()->{
            double inlWidth = processedTable.getScene().getWindow().widthProperty().getValue();

            dateColumn = new TableColumn<>("Date");
            dateColumn.setCellValueFactory(param -> {
                SimpleObjectProperty<String> property = new SimpleObjectProperty<>();
                property.setValue(param.getValue().getDate());
                return property;
            });
            dateColumn.setPrefWidth(inlWidth * (4.0 / 15.0));
            dateColumn.setMinWidth(100);

            percentageColumn = new TableColumn<>("Percentage Slept");
            percentageColumn.setCellValueFactory(param -> {
                SimpleObjectProperty<Double> property = new SimpleObjectProperty<>();
                property.setValue(param.getValue().getPercentage());
                return property;
            });
            percentageColumn.setPrefWidth(inlWidth * (2.0 / 5.0));
            percentageColumn.setMinWidth(150);

            timeColumn = new TableColumn<>("Time Slept");
            timeColumn.setCellValueFactory(param -> {
                SimpleObjectProperty<Double> property = new SimpleObjectProperty<>();
                property.setValue(param.getValue().getTime());
                return property;
            });
            timeColumn.setPrefWidth(inlWidth * (1.0 / 3.0));
            timeColumn.setMinWidth(125);

            processedTable.getColumns().addAll(dateColumn, percentageColumn, timeColumn);
        });
    }

    public void addToLastWeek() {
        try {
            User user = User.loadUserFromFile(new File("test2.usr"));
            double percentage = calculatePercentageSlept(user.get(0).getProcessedData());
            double timeSlept = calculateTimeSlept(percentage,
                user.get(0).getProcessedData().get(user.get(0).getProcessedData().size() - 1).getTimeElapsed());
            if (!ExperimentManager.isExperimentMode()) {
                Platform.runLater(() -> {
                    ObservableList<TableData> data = FXCollections.observableArrayList(
                        new TableData(user.get(0).getRawData().getEpoch(0).getTimeStamp().replace('.', ' '), percentage, timeSlept)
//                        new TableData(user.get(0).getRawData().getEpoch(1).getTimeStamp().replace('.', ' '), 94, 104),
//                        new TableData(user.get(0).getRawData().getEpoch(2).getTimeStamp().replace('.', ' '), 95, 90),
//                        new TableData(user.get(0).getRawData().getEpoch(3).getTimeStamp().replace('.', ' '), 67, 95),
//                        new TableData(user.get(0).getRawData().getEpoch(4).getTimeStamp().replace('.', ' '), 20, 98)
                    );

                    processedTable.getItems().addAll(data);
                    System.out.println("Table");
                    processedTable.refresh();
                });
            } else {
                XYChart.Series<String, Number> barSeries = new XYChart.Series<>();

                barSeries.getData().add(new XYChart.Data<>(user.get(0).getRawData().getEpoch(0).getTimeStamp(), 8));
                barSeries.getData().add(new XYChart.Data<>(user.get(0).getRawData().getEpoch(1).getTimeStamp(), 9));
                barSeries.getData().add(new XYChart.Data<>(user.get(0).getRawData().getEpoch(2).getTimeStamp(), 3));
                barSeries.getData().add(new XYChart.Data<>(user.get(0).getRawData().getEpoch(3).getTimeStamp(), 5));

                Platform.runLater(() -> {
                    barChart.getData().add(barSeries);
                });

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void listenForTableWidthChange() {
        Platform.runLater(() -> {
            processedTable.getScene().getWindow().widthProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue,
                                    Number newValue) {
                    double width = newValue.doubleValue();
                    Platform.runLater(() -> {
                        dateColumn.setPrefWidth(width * (4.0 / 15.0));
                        percentageColumn.setPrefWidth(width * (2.0 / 5.0));
                        timeColumn.setPrefWidth(width * (1.0 / 3.0));
                    });
                }
            });
        });
    }

    public void comboBoxSetup() {
        try {
            User user = User.loadUserFromFile(Resource.getFileFromResource("test2.usr"));
            processedComboData.add(user.get(0).getRawData().getEpoch(0).getTimeStamp());
        }  catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        processedCombo.setItems(processedComboData);
    }

    public void listenForComboAction() {
        processedCombo.setOnAction((event) -> {
            String selected = processedCombo.getSelectionModel().getSelectedItem();
            System.out.println("ComboBox Action (selected: " + selected + ")");
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            XYChart.Series<Number, Number> seriesPercent = new XYChart.Series<>();
            try {
                User user = User.loadUserFromFile(new File("test2.usr"));
                DataCouple couple = null;
                for (int i = 0; i < user.size(); i++) {
                    if (user.get(i).getRawData().getEpoch(0).getTimeStamp().equals(selected)) {
                        couple = user.get(i);
                    }
                }

                for (int i = 0; i < user.get(0).getProcessedData().size(); i += 10) {
                    Plot plot = couple.getProcessedData().get(i);
                    series.getData().add(new XYChart.Data<>(plot.getTimeElapsed(),
                        plot.getLevel()));
                }
                seriesPercent.getData().add(new XYChart.Data<>(0, 60));
                seriesPercent.getData().add(new XYChart.Data<>(user.get(0).getProcessedData().get(user.get(0).getProcessedData().size() - 1).getTimeElapsed(),
                    60));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            lineChart.getData().addAll(series, seriesPercent);
        });
    }

    public double calculatePercentageSlept(ProcessedDataContainer container) {
        int awake = 0;
        int asleep = 0;

        for(Plot p : container) {
            if (p.getLevel() > 60.0) {
                awake += 1;
            } else {
                asleep += 1;
            }
        }

        return 100 * ((double)asleep / (double)(awake + asleep));
    }

    public double calculateTimeSlept(double percentage, double totalTime) {
        double milliseconds = (percentage / 100) * totalTime;
        return milliseconds / (1000 * 60 * 60);
    }

    public void back() {
        SceneUtils.setView((Stage) processedCombo.getScene().getWindow(), "HomeScreen.fxml");
    }

    public void process() {
       new Thread(()->{

        try {
            User user = User.loadUserFromFile(new File("test2.usr"));
            ProcessedDataContainer processedDataContainer;
            for (int i = 0; i < user.size(); i++) {
                if (user.get(i).getProcessedData() == null) {
                    processedDataContainer = ClassificationUtils.convertData(user.get(i).getRawData());
                    System.out.println("PC created");
                    user.add(new DataCouple(user.get(i).getRawData(), processedDataContainer));
                    System.out.println("Couple added");
                }
            }

            //Saving to file
            user.saveToFile(new File("test2.usr"));
            System.out.println("usr saved");


        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Unable to load container from file: " + e.toString());
            return;
        }

        }).start();
    }

    public class TableData {
        private final SimpleStringProperty date;
        private final SimpleDoubleProperty percentage;
        private final SimpleDoubleProperty time;

        private TableData(String date, double percentage, double time) {
            this.date = new SimpleStringProperty(date);
            this.percentage = new SimpleDoubleProperty(percentage);
            this.time = new SimpleDoubleProperty(time);
        }

        public String getDate() {
            return date.get();
        }

        public SimpleStringProperty dateProperty() {
            return date;
        }

        public void setDate(String date) {
            this.date.set(date);
        }

        public double getPercentage() {
            return percentage.get();
        }

        public SimpleDoubleProperty percentageProperty() {
            return percentage;
        }

        public void setPercentage(double percentage) {
            this.percentage.set(percentage);
        }

        public double getTime() {
            return time.get();
        }

        public SimpleDoubleProperty timeProperty() {
            return time;
        }

        public void setTime(double time) {
            this.time.set(time);
        }
    }
}
