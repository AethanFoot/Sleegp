package uk.ac.bath.csed_group_11.sleegp.gui.Controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import uk.ac.bath.csed_group_11.sleegp.gui.Utilities.SceneUtils;
import uk.ac.bath.csed_group_11.sleegp.logic.Classification.ClassificationUtils;
import uk.ac.bath.csed_group_11.sleegp.logic.data.DataCouple;
import uk.ac.bath.csed_group_11.sleegp.logic.data.EpochContainer;
import uk.ac.bath.csed_group_11.sleegp.logic.data.ProcessedDataContainer;
import uk.ac.bath.csed_group_11.sleegp.logic.data.User;

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
    private ComboBox<EpochContainer> processedCombo;
    private ObservableList<EpochContainer> processedComboData = FXCollections.observableArrayList();

    @FXML
    TableView<String> processedTable;

    TableColumn<String, String> dateColumn;
    TableColumn<String, String> percentageColumn;
    TableColumn<String, String> timeColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dateColumn.setCellValueFactory(param -> {
            SimpleObjectProperty<String> property = new SimpleObjectProperty<>();
            property.setValue(param.toString());
            System.out.println("eivueaouv");
            return property;
        });

        percentageColumn.setCellValueFactory(param -> {
            SimpleObjectProperty<String> property = new SimpleObjectProperty<>();
            property.setValue(param.toString());
            return property;
        });

        timeColumn.setCellValueFactory(param -> {
            SimpleObjectProperty<String> property = new SimpleObjectProperty<>();
            property.setValue(param.toString());
            return property;
        });


        try {
            processedComboData.add(EpochContainer.loadContainerFromFile(new File("/home/aethan/CSED" +
                "/resources/test-data/3 Hour (Fixed).ec")));
        }  catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        processedCombo.setItems(processedComboData);

        processedCombo.setOnAction((event) -> {
            EpochContainer epochSelected = processedCombo.getSelectionModel().getSelectedItem();
            System.out.println("ComboBox Action (selected: " + epochSelected + ")");
        });



        Platform.runLater(()->{
            processedTable.getItems().add(";siudvgwoyegw");
            System.out.println("Table");
            processedTable.refresh();
        });
    }

    public void back() {
        SceneUtils.setView((Stage) processedCombo.getScene().getWindow(), "HomeScreen.fxml");
    }

   public void process() {
       new Thread(()->{

        try {


            User user = new User();

            EpochContainer ec;
            ec = EpochContainer.loadContainerFromFile(new File("/home/aethan/CSED" +
                "/resources/test-data/3 Hour (Fixed).ec"));
            System.out.println("EC created");

            ProcessedDataContainer processedDataContainer = ClassificationUtils.convertData(ec);
            System.out.println("PC created");

            user.add(new DataCouple(ec, processedDataContainer));
            System.out.println("Couple added");
            //Saving to file
            processedDataContainer.saveToFile(new File("/home/aethan/CSED" +
                "/resources/test-data/3 Hour test.sd"));
            System.out.println("PC saved");
            user.saveToFile(new File("test1.usr"));
            System.out.println("usr saved");

            User user2 = User.loadUserFromFile(new File("test1.usr"));

            Platform.runLater(()->{
                System.out.println(user2.get(0).getRawData().getEpoch(0).getTimeStamp());
                processedTable.getItems().add(user2.get(0).getRawData().getEpoch(0).getTimeStamp());
                System.out.println("Table");
                processedTable.refresh();
            });



        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Unable to load container from file: " + e.toString());
            return;
        }

        }).start();
    }
}
