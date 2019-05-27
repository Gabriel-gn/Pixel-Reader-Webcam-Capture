/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javafxapplication13_textrecognitionscreen;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
//import javafx.scene.shape.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.AnchorPane;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import javafx.scene.layout.StackPane;
import testresourcebundle.ImagePane;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.stage.DirectoryChooser;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 *
 * @author Gabriel
 */
public class FXMLDocumentController implements Initializable {

    private double xFinalRect;
    private double yFinalRect;
    private javafx.scene.shape.Rectangle rectCrop;
    private double xInicialRect;
    private double yInicialRect;
    private Image image1;
    private boolean recording = false;
    private ScheduledExecutorService exec;
    private int rectw;
    private int recth;
    private int initXPoint;
    private int initYPoint;
    private int r = 0;
    private int g = 0;
    private int b = 0;
    private int r2 = 0;
    private int g2 = 0;
    private int b2 = 0;
    private int count = 0;
    private File selectedDirectory;
    private Webcam webcam;

    @FXML
    private ImagePane imagePane;
    @FXML
    private AnchorPane anchorPane;
    @FXML
    private StackPane stackPane;
    @FXML
    private Label lbl_folder;
    @FXML
    private Label lbl_xi;
    @FXML
    private Label lbl_yi;
    @FXML
    private Label lbl_w;
    @FXML
    private Label lbl_h;
    @FXML
    private ImagePane imagePanePreview;
    @FXML
    private Button btn_capture;
    @FXML
    private ComboBox<Webcam> comboBox_webcam;
    @FXML
    private ComboBox<WebcamResolution> comboBox_webcamRes;
    @FXML
    private AnchorPane anchorPaneWebcam;
    @FXML
    private CheckBox checkBox_captureSnapshot;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // TODO
            initScreenShot();
        } catch (AWTException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        initComboBox();
        initWebcamPanel();

    }

    private void initScreenShot() throws AWTException {
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage capture;
        capture = new Robot().createScreenCapture(screenRect);
        image1 = SwingFXUtils.toFXImage(capture, null);
        imagePane.imageView.setImage(image1);
    }

    private void initComboBox() {
        comboBox_webcam.setItems(FXCollections.observableArrayList(Webcam.getWebcams()));

        comboBox_webcam.valueProperty().addListener((obs, oldValue, newValue) -> {
            webcam = newValue;
//            webcam.open();
            System.out.println("Webcam: " + newValue.getName());
            initWebcamPanel();
        });
        comboBox_webcam.getSelectionModel().selectFirst();

        comboBox_webcamRes.setItems(FXCollections.observableArrayList(Arrays.asList(WebcamResolution.values())));
        comboBox_webcamRes.valueProperty().addListener((obs, oldValue, newValue) -> {
            webcam.close();
            webcam.setViewSize(newValue.getSize());
            System.out.println("Dimensões: " + newValue.getSize());
            initWebcamPanel();
            webcam.open();
        });
        comboBox_webcamRes.getSelectionModel().selectFirst();
    }

    private void initWebcamPanel() {
        if (!anchorPaneWebcam.getChildren().isEmpty()) {
            anchorPaneWebcam.getChildren().remove(0, anchorPaneWebcam.getChildren().size());
        }

        WebcamPanel panel = new WebcamPanel(webcam);
//        panel.setFPSDisplayed(true);
//        panel.setDisplayDebugInfo(true);
//        panel.setImageSizeDisplayed(true);
//        panel.setMirrored(true);

        SwingNode swingNode = new SwingNode();
        swingNode.setContent(panel);

        AnchorPane.setTopAnchor(swingNode, 0.0);
        AnchorPane.setBottomAnchor(swingNode, 0.0);
        AnchorPane.setLeftAnchor(swingNode, 0.0);
        AnchorPane.setRightAnchor(swingNode, 0.0);

        anchorPaneWebcam.getChildren().add(swingNode);
    }

    private void captureScreenRect(int xi, int yi, int w, int h, boolean makeFile) throws AWTException {
        Robot robot = new Robot();
        int x = xi;
        int y = yi;
        int width = w;
        int height = h;
        Rectangle area = new Rectangle(x, y, width, height);
        BufferedImage capture = robot.createScreenCapture(area);
        imagePanePreview.imageView.setImage(SwingFXUtils.toFXImage(capture, null));
        if (makeFile) {
            saveSnapshotToFolder(capture);
        }
    }

    @FXML
    private void handleDraggedCrop(javafx.scene.input.MouseEvent event) {
        xFinalRect = event.getX();
        yFinalRect = event.getY();

        if (xFinalRect >= anchorPane.getWidth()) {
            xFinalRect = anchorPane.getWidth();
        } else if (xFinalRect <= 0) {

            xFinalRect = 0;
        }

        if (yFinalRect >= anchorPane.getHeight()) {
            yFinalRect = anchorPane.getHeight();
        } else if (yFinalRect <= 0) {
            yFinalRect = 0;
        }

        rectCrop.setX(xInicialRect);
        rectCrop.setY(yInicialRect);
        rectCrop.setWidth(xFinalRect - xInicialRect);
        rectCrop.setHeight(yFinalRect - yInicialRect);

        if (rectCrop.getWidth() < 0) {
            rectCrop.setWidth(-rectCrop.getWidth());
            rectCrop.setX(rectCrop.getX() - rectCrop.getWidth());
        }

        if (rectCrop.getHeight() < 0) {
            rectCrop.setHeight(-rectCrop.getHeight());
            rectCrop.setY(rectCrop.getY() - rectCrop.getHeight());
        }

    }

    @FXML
    private void handlePressedCrop(javafx.scene.input.MouseEvent event) {
        //if (btn_Cortar.isSelected()) {

        xInicialRect = event.getX();
        yInicialRect = event.getY();

        double width = anchorPane.getWidth();
        double height = anchorPane.getHeight();

        anchorPane.setPrefSize(width, height);
        anchorPane.setMinSize(width, height);
        anchorPane.setMaxSize(width, height);

        if (rectCrop != null) {
            anchorPane.getChildren().remove(rectCrop);
        }

        rectCrop = new javafx.scene.shape.Rectangle();
        //rectCrop.getStyleClass().add("editToolsCropSelection");
        anchorPane.getChildren().add(rectCrop);
        rectCrop.setOpacity(0.35);

        //}
    }

    @FXML
    private void handleRealeasedCrop(javafx.scene.input.MouseEvent event) throws AWTException {

        //if (btn_Cortar.isSelected()) {
        anchorPane.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        anchorPane.setMinSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        anchorPane.setMaxSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        //}
        //System.out.println("rectCrop largura: " + rectCrop.getWidth());
        //System.out.println("rectCrop altura: " + rectCrop.getHeight());

        //DAQUI PRA BAIXO PEGA A IMAGEM E FAZ O CROP DA PARTE SELECIONADA
        Bounds bounds = rectCrop.getBoundsInParent();
        //System.out.println(bounds);

        double rectMinX = bounds.getMinX();
        double rectMinY = bounds.getMinY();

        //System.out.println("escala: " + escala);
        //COMEÇO DA LEITURA DOS PIXELS
        PixelReader reader = image1.getPixelReader();

        int rectWidth = (int) rectCrop.getWidth();
        int rectHeight = (int) rectCrop.getHeight();
        Bounds rectCropBounds = rectCrop.localToParent(rectCrop.getBoundsInLocal());

        initXPoint = (int) (rectCropBounds.getMinX());
        initYPoint = (int) (rectCropBounds.getMinY());
        rectw = rectWidth;
        recth = rectHeight;

        getPixelColor(initXPoint, initYPoint);

        //System.out.println(writer.getPixelFormat().getType()); //BYTE_BGRA_PRE
        lbl_w.setText("" + rectWidth);
        lbl_h.setText("" + rectHeight);
        lbl_xi.setText("" + rectCropBounds.getMinX());
        lbl_yi.setText("" + rectCropBounds.getMinY());

        captureScreenRect(initXPoint, initYPoint, rectw, recth, false);

//        System.out.println("ssWidth: " + imagePane.imageView.getFitWidth());
//        System.out.println("ssHeight: " + imagePane.imageView.getFitHeight());
//        System.out.println("rectCropBoundsX: " + rectCropBounds.getMinX());
//        System.out.println("rectCropBoundsY: " + rectCropBounds.getMinY());
//        System.out.println("-----------------------------------------------------------------");
    }

    @FXML
    private void handlePrint(ActionEvent event) {
        try {
            // TODO
            initScreenShot();
        } catch (AWTException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getPixelColor(int xi, int yi) throws AWTException {
        Robot robot = new Robot();
        Color color = robot.getPixelColor(xi, yi);

        r = color.getRed();
        g = color.getGreen();
        b = color.getBlue();

//        System.out.println("Red   = " + color.getRed());
//        System.out.println("Green = " + color.getGreen());
//        System.out.println("Blue  = " + color.getBlue());
    }

    @FXML
    private void handleChooseFolder(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        selectedDirectory = directoryChooser.showDialog(((Scene) (anchorPane.getScene())).getWindow());

        if (selectedDirectory == null) {
            lbl_folder.setText("Nenhuma pasta selecionada");
        } else {
            lbl_folder.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void handleStart(ActionEvent event) {
        if (!recording) {
            exec = Executors.newSingleThreadScheduledExecutor();
            exec.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        getPixelColor(initXPoint, initYPoint);
                    } catch (AWTException ex) {
                        Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (r != r2 || g != g2 || b != b2) {
                        r2 = r;
                        g2 = g;
                        b2 = b;
                        try {
                            captureScreenRect(initXPoint, initYPoint, rectw, recth, true);
                            takeWebcamCapture();
                        } catch (AWTException ex) {
                            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        count++;
                        System.out.println("CAPTURADO");
                    }
                }
            }, 0, 1, TimeUnit.SECONDS);
            btn_capture.setText("Capturando");
            recording = true;
        } else {
            exec.shutdown();
            btn_capture.setText("Iniciar Captura");
            recording = false;
        }
    }

    private void saveSnapshotToFolder(BufferedImage bImage) {
        if (checkBox_captureSnapshot.isSelected()) {
            File outputFile = new File(selectedDirectory.getAbsolutePath() + "/snap(" + count + ").png");
//        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
            try {
                ImageIO.write(bImage, "png", outputFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void takeWebcamCapture() throws IOException {
        // get default webcam and open it
//        webcam.open();

        // get image
        BufferedImage image = webcam.getImage();

        // save image to PNG file
        ImageIO.write(image, "PNG", new File(selectedDirectory.getAbsolutePath() + "/capture(" + count + ").png"));
    }

}
