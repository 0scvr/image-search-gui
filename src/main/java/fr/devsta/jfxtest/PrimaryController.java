package fr.devsta.jfxtest;

import java.io.File;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class PrimaryController {

  @FXML
  private ImageView selectedImage;
  @FXML
  private RadioButton rgbRadio, hsvRadio;
  @FXML
  private Label filenameLabel;


  private File file;
  private FileChooser fileChooser = new FileChooser();

  Image img = new Image(getClass().getResourceAsStream("012.jpg"));

  private void switchToSecondary() throws IOException {
    App.setRoot("secondary");
  }

  public void selectFile(ActionEvent evt) {
    System.out.println("button clicked");
    fileChooser.setSelectedExtensionFilter(new ExtensionFilter("Images", "jpg"));
    file = fileChooser.showOpenDialog(filenameLabel.getScene().getWindow());

    if (file != null) {
      filenameLabel.setText(file.getAbsolutePath());
      selectedImage.setImage(new Image(file.getAbsolutePath()));
    }
  }

  public void getSelectedMode(ActionEvent evt) {
    if (rgbRadio.isSelected()) {
      System.out.println(rgbRadio.getText());
    }

    if (hsvRadio.isSelected()) {
      System.out.println(hsvRadio.getText());
    }
  }
}
