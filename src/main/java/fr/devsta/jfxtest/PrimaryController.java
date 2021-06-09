package fr.devsta.jfxtest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.TreeMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class PrimaryController implements Initializable {
	@FXML
	private TableView<ResultImage> resultTable;
	@FXML
	private TableColumn<ResultImage, Float> distanceColumn;
	@FXML
	private TableColumn<ResultImage, String> nameColumn;
	@FXML
	private TableColumn<ResultImage, String> openColumn;

	@FXML
	private ImageView selectedImage;

	@FXML
	private Label filenameLabel;

	@FXML
	private ToggleGroup searchMode;

	private String imageFile;

	private Connection dbConnection;

	@FXML
	private Label databasePathLabel;

	public void pickImage(ActionEvent actionEvent) throws MalformedURLException {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choisir une image");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
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
			fr.unistra.pelican.Image testImage = ImageLoader.exec(imgPath);

			TreeMap<Double, String> images = findSimilarImages(testImage, selectedRadioButton.getText());

			for (Entry<Double, String> entry : images.entrySet()) {
				resultTable.getItems().add(new ResultImage(entry.getKey(), entry.getValue()));
			}
		} catch (SQLException e) {
			System.err.println("Some error happenned");
		}
	}

	/**
	 * Trouve les 10 images les plus similaires dans la base de données.
	 * 
	 * @param testImage
	 * @param mode      Méthode de recherche ("RGB" ou "HSV")
	 * @return
	 * @throws SQLException
	 */
	public TreeMap<Double, String> findSimilarImages(fr.unistra.pelican.Image testImage, String mode)
			throws SQLException {

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

		// Step 2: Fetch images from the database & calculate their similarity.
		String sql = "SELECT * FROM images";
		ResultSet rs = dbConnection.createStatement().executeQuery(sql);

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
				double[][] resultHistogram = gson.fromJson(rs.getString("hsvHistogram"), double[][].class);
				double similarityRatio = Utils.getRgbSimilarity(optimizedHistogram, resultHistogram);
				images.put(similarityRatio, rs.getString("filename"));
			}
			break;
		}

		int count = 0;
		TreeMap<Double, String> top10 = new TreeMap<Double, String>();
		for (Entry<Double, String> entry : images.entrySet()) {
			if (count >= 10)
				break;

			top10.put(entry.getKey(), entry.getValue());
			count++;
		}
		return top10;
	}

	/**
	 * Affiche une fenêtre pour sélectionner des images et les insère dans la base
	 * de données.
	 * 
	 * @param evt
	 */
	public void addImagesInDatabase(ActionEvent evt) {
		String sql = "INSERT INTO images(filename,histogram,hsvHistogram,grayscale) VALUES(?,?,?,?)";
		Gson gson = new GsonBuilder().create();

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choisir des images");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images", "*.jpg"));
		List<File> selectedFiles = fileChooser.showOpenMultipleDialog(filenameLabel.getScene().getWindow());

		if (selectedFiles != null && selectedFiles.size() > 0) {
			for (File file : selectedFiles) {
				fr.unistra.pelican.Image img = ImageLoader.exec(file.getAbsolutePath());

				// Apply filters to every image & get its optimized histogram
				fr.unistra.pelican.Image newRequestImage = Utils.applyMedianFilter(img);
				double[][] requestHistogram = Utils.getRgbHistogram(newRequestImage);
				double[][] discreteHistogram = Utils.getDiscreteHistogram(requestHistogram);
				double[][] optimizedHistogram = Utils.getNormalisedRgbHistogram(discreteHistogram,
						(newRequestImage.getXDim() * newRequestImage.getYDim()));

				// HSV version
				double[][] requestHistogramHsv = Utils.getHsvHistogram(newRequestImage);
				double[][] discreteHistogramHsv = Utils.getDiscreteHsvHistogram(requestHistogramHsv);
				double[][] optimizedHistogramHsv = Utils.getNormalisedRgbHistogram(discreteHistogramHsv,
						(newRequestImage.getXDim() * newRequestImage.getYDim()));

				String stringHistogram = gson.toJson(optimizedHistogram);
				String stringHsvHistogram = gson.toJson(optimizedHistogramHsv);

				try {
					PreparedStatement pstmt = dbConnection.prepareStatement(sql);
					pstmt.setString(1, file.getAbsolutePath());
					pstmt.setString(2, stringHistogram);
					pstmt.setString(3, stringHsvHistogram);
					pstmt.setInt(4, 0);
					pstmt.executeUpdate();
					System.out.println(file.getName() + " a été ajoutée à la base d'images.");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("Erreur! Aucune image n'a été sélectionnée.");
		}
	}

	/**
	 * Vide la base de données utilisée.
	 * 
	 * @param evt
	 */
	public void emptyDatabase(ActionEvent evt) {
		try {
			dbConnection.createStatement().executeUpdate("DELETE FROM images");
			System.out.println("La base de données a été vidée.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Affiche une fenêtre pour choisir une base de données et tente de se connecter
	 * à celle-ci.
	 * 
	 * @param evt
	 */
	public void setDatabase(ActionEvent evt) {
		try {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Choisir une base de données");
			fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Base de données SQLite", "*.db"));
			File selectedFile = fileChooser.showOpenDialog(filenameLabel.getScene().getWindow());

			if (selectedFile != null) {

				try {
					dbConnection = Utils.getDatabase(selectedFile.toURI().toURL().toString());
					System.out.println("La base de données sélectionnée a bien été enregistrée.");
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

				databasePathLabel.setText(selectedFile.getAbsolutePath());
			} else {
				databasePathLabel.setText("");
				dbConnection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Initialize connection to default database
		try {
			String defaultDatabasePath = getClass().getResource("images.db").getPath();

			dbConnection = Utils.getDatabase(defaultDatabasePath);
			databasePathLabel.setText(defaultDatabasePath);

			openColumn.setCellValueFactory(new PropertyValueFactory<>("openButton"));
			nameColumn.setCellValueFactory(new PropertyValueFactory<>("filePath"));
			distanceColumn.setCellValueFactory(new PropertyValueFactory<>("distance"));

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
