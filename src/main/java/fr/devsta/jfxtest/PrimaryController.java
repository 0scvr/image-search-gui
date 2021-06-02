package fr.devsta.jfxtest;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class PrimaryController {

  @FXML
  private RadioButton rgbRadio, hsvRadio;
  @FXML
  private ImageView selectedImage;
  @FXML
  private Label filenameLabel;

  private String imageFile;

  public void pickImage(ActionEvent actionEvent) throws MalformedURLException {

    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select Image File");
    fileChooser.getExtensionFilters()
        .addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
    File selectedFile = fileChooser.showOpenDialog(filenameLabel.getScene().getWindow());

    if (selectedFile != null) {

      imageFile = selectedFile.toURI().toURL().toString();

      Image image = new Image(imageFile);
      filenameLabel.setText(selectedFile.getName());
      // System.out.println(selectedFile.getAbsolutePath());
      selectedImage.setImage(image);
    } else {
      filenameLabel.setText("Image file selection cancelled.");
    }

  }

  private void switchToSecondary() throws IOException {
    App.setRoot("secondary");
  }


  public void getSelectedMode(ActionEvent evt) {
    // tg.getSelectedToggle()
    if (rgbRadio.isSelected()) {
      System.out.println(rgbRadio.getText());
    }

    if (hsvRadio.isSelected()) {
      System.out.println(hsvRadio.getText());
    }
  }
}
