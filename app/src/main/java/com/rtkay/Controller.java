package com.rtkay;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

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
    private byte[] graphDef;
    private List<String> labels;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            graphDef = getClass().getResourceAsStream("tensorflow/tensorflow_inception_graph.pb").readAllBytes();
            System.out.println(graphDef);
            labels = copyLinesToList("tensorflow/imagenet_comp_graph_label_strings.txt");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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
        fileChooser.getExtensionFilters()
                .addAll(new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.jpeg"));
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
        byte[] imageBytes = null;
        try {
            imageBytes = Files.readAllBytes(selectedImage.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (Tensor image = Tensor.create(imageBytes)) {
            float[] labelProbabilities = executeInceptionGraph(graphDef, image);
            int bestLabelIndex = maxIndex(labelProbabilities);
            txtResults.setText("");
            txtResults.setText(String.format("BEST MATCH: %s (%.2f%% likely)", labels.get(bestLabelIndex),
                    labelProbabilities[bestLabelIndex] * 100f));
            System.out.println(String.format("BEST MATCH: %s (%.2f%% likely)", labels.get(bestLabelIndex),
                    labelProbabilities[bestLabelIndex] * 100f));
        }
    }

    private int maxIndex(float[] probabilities) {
        int best = 0;
        for (int i = 1; i < probabilities.length; ++i) {
            if (probabilities[i] > probabilities[best]) {
                best = i;
            }
        }
        return best;
    }

    private float[] executeInceptionGraph(byte[] graphDef2, Tensor image) {
        try (Graph g = new Graph()) {
            g.importGraphDef(graphDef);
            try (Session s = new Session(g);
                    Tensor result = s.runner().feed("DecodeJpeg/contents", image).fetch("softmax").run().get(0)) {
                final long[] rshape = result.shape();
                if (result.numDimensions() != 2 || rshape[0] != 1) {
                    throw new RuntimeException(String.format(
                            "Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s",
                            Arrays.toString(rshape)));
                }
                int nlabels = (int) rshape[1];
                float[][] here = new float[1][nlabels];
                float[] probabilities = ((float[][]) result.copyTo(here))[0];
                return probabilities;
            }
        }
    }

    /* stores a model of our known data into an array list */
    private List<String> copyLinesToList(String path) throws IOException {
        URL url = getClass().getResource(path);
        File file = new File(url.getFile());
        try {
            return Files.readAllLines(file.toPath(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.err.println("Failed to read [" + path + "]: " + e.getMessage());
            System.exit(0);
        }
        return null;
    }

}
