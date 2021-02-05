package com.rtkay;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import org.tensorflow.Graph;
import org.tensorflow.Session;

import javax.imageio.ImageIO;
import com.jfoenix.controls.JFXButton;

import org.tensorflow.Tensor;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

public class Controller implements Initializable {
    @FXML
    private JFXButton btnUploadImage, btnIdentify;

    @FXML
    private ImageView imgPreview;

    @FXML
    private Label txtResults;
    private File selectedImage = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnUploadImage.setOnMouseClicked(event -> {
            showFileUploadDialog();
        });
        btnIdentify.setOnMouseClicked(event -> {
            identifyImage();
        });
    }

    private void showFileUploadDialog() {
        Stage stage = (Stage) imgPreview.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        selectedImage = selectedFile;
        if (selectedFile != null) {
            Image image = new Image(selectedFile.toURI().toString(), 417, // requested width
                    449, // requested height
                    true, // preserve ratio
                    true, // smooth rescaling
                    true // load in background
            );
            imgPreview.setImage(image);
        }
    }

    private void identifyImage() {

    }

}
