package fr.devsta.jfxtest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.TreeMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class PrimaryController implements Initializable {

  @FXML
  private ListView<String> resultsListView;
  @FXML
  private ImageView selectedImage;
  @FXML
  private Label filenameLabel;
  @FXML
  private ToggleGroup searchMode;
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


  /**
   * Creates a connection to the specified SQLite database and returns it.
   * 
   * @param pathToDatabase path to .db file
   * @return connection to SQLite database
   * @throws SQLException
   */
  public static Connection getDatabase(String pathToDatabase) throws SQLException {
    // /Users/oscar/Documents/LPIOT/ImageJava/imagesearch/images.db
    String url = "jdbc:sqlite:" + pathToDatabase;
    Connection conn = DriverManager.getConnection(url);
    System.out.println("Connection to SQLite database has been established.");
    return conn;
  }


  public void startSearch(ActionEvent evt) {
    RadioButton selectedRadioButton = (RadioButton) searchMode.getSelectedToggle();

    if (selectedRadioButton.getText() == "RGB") {
      System.out.println("RGB is selected");
    } else {
      System.out.println("HSV is selected");
    }

    String imgPath = imageFile.split("file:")[1];
    System.out.println(imgPath);

    try {
      Connection connection = getDatabase(getClass().getResource("images.db").getPath());
      fr.unistra.pelican.Image testImage = ImageLoader.exec(imgPath);
      // // addImagesToDatabase("/Users/oscar/Documents/LPIOT/ImageJava/imagesearch/images",
      // // connection);
      //
      TreeMap<Double, String> images =
          findSimilarImages(testImage, connection, selectedRadioButton.getText());

      for (Entry<Double, String> entry : images.entrySet()) {
        System.out.println(entry.getKey() + " : " + entry.getValue());
      }


      Image IMAGE_RUBY =
          new Image("https://upload.wikimedia.org/wikipedia/commons/f/f1/Ruby_logo_64x64.png");
      Image IMAGE_VISTA = new Image("http://antaki.ca/bloom/img/windows_64x64.png");

      Image[] listOfImages = {IMAGE_RUBY, IMAGE_VISTA};

      ObservableList<String> items = FXCollections.observableArrayList("RUBY", "VISTA");
      resultsListView.setItems(items);

      resultsListView.setCellFactory(param -> new ListCell<String>() {
        private ImageView imageView = new ImageView();

        @Override
        public void updateItem(String name, boolean empty) {
          super.updateItem(name, empty);
          if (empty) {
            setText(null);
            setGraphic(null);
          } else {
            if (name.equals("RUBY"))
              imageView.setImage(listOfImages[0]);
            else if (name.equals("VISTA"))
              imageView.setImage(listOfImages[1]);
            setText(name);
            setGraphic(imageView);
          }
        }
      });
    } catch (SQLException e) {
      System.err.println("Some error happenned");
    }
  }

  /**
   * Returns the 10 most similar images in the database.
   * 
   * @param testImage base image to compare to
   * @param conn SQLite database connection
   * @return
   * @throws SQLException
   */
  public static TreeMap<Double, String> findSimilarImages(fr.unistra.pelican.Image testImage,
      Connection conn, String mode) throws SQLException {

    TreeMap<Double, String> images = new TreeMap<>();
    Gson gson = new GsonBuilder().create();
    double[][] optimizedHistogram = null;

    // Step 1: Apply filters to request image & get optimized histogram
    fr.unistra.pelican.Image newRequestImage = Utils.applyMedianFilter(testImage);

    switch (mode) {
      case "RGB":
        double[][] requestHistogram = Utils.getRgbHistogram(newRequestImage);
        double[][] discreteHistogram = Utils.getDiscreteHistogram(requestHistogram);
        optimizedHistogram = Utils.getNormalisedRgbHistogram(discreteHistogram,
            (newRequestImage.getXDim() * newRequestImage.getYDim()));
        break;
      case "HSV":
        double[][] requestHistogramHsv = Utils.getHsvHistogram(newRequestImage);
        double[][] discreteHistogramHsv = Utils.getDiscreteHsvHistogram(requestHistogramHsv);
        optimizedHistogram = Utils.getNormalisedRgbHistogram(discreteHistogramHsv,
            (newRequestImage.getXDim() * newRequestImage.getYDim()));
        break;
    }


    // Step 2: Fetch images from the database & calculate their similarity with the request image
    String sql = "SELECT * FROM images";
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(sql);


    switch (mode) {
      case "RGB":
        while (rs.next()) {
          double[][] resultHistogram = gson.fromJson(rs.getString("histogram"), double[][].class);
          double similarityRatio = Utils.getRgbSimilarity(optimizedHistogram, resultHistogram);
          images.put(similarityRatio, rs.getString("filename"));
        }
        break;
      case "HSV":
        while (rs.next()) {
          double[][] resultHistogram =
              gson.fromJson(rs.getString("hsvHistogram"), double[][].class);
          double similarityRatio = Utils.getRgbSimilarity(optimizedHistogram, resultHistogram);
          images.put(similarityRatio, rs.getString("filename"));
        }
        break;
    }


    // TODO: return on first 10 entries
    return images;
  }


  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // TODO Auto-generated method stub

  }

}
