package fr.devsta.jfxtest;

import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

public class ResultImage {
	private float distance;
	private String filePath;
	private Button openButton;

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Button getOpenButton() {
		return openButton;
	}

	public void setOpenButton(Button openButton) {
		this.openButton = openButton;
	}

	public ResultImage(double distance, String filePath) {
		this.distance = (float) distance;
		this.filePath = filePath;
		this.openButton = new Button("Afficher");
		this.openButton.setAlignment(Pos.CENTER);
		this.openButton.setOnMouseClicked((MouseEvent event) -> {
			Viewer2D.exec(ImageLoader.exec(filePath));
		});
	}

}
