package fr.devsta.jfxtest;

import java.util.Arrays;
import java.util.HashMap;
import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;

public final class Utils {

  /**
   * Applies a median filter to the image and returns the result in a new image.
   * 
   * @param in an image (grayscale or RGB)
   */
  public static Image applyMedianFilter(Image in) {
    int width = in.getXDim();
    int height = in.getYDim();
    int bandCount = in.getBDim();
    Image newImg = new ByteImage(width, height, 1, 1, bandCount);

    for (int x = 1; x < width - 2; x++) {
      for (int y = 1; y < height - 2; y++) {
        for (int band = 0; band < bandCount; band++) {
          int v = in.getPixelXYBByte(x, y, band);
          int vX1 = in.getPixelXYBByte(x + 1, y, band);
          int vXY1 = in.getPixelXYBByte(x + 1, y + 1, band);
          int vXY_1 = in.getPixelXYBByte(x - 1, y - 1, band);
          int vX_1 = in.getPixelXYBByte(x - 1, y, band);
          int vY_1 = in.getPixelXYBByte(x, y - 1, band);
          int vX_1Y1 = in.getPixelXYBByte(x - 1, y + 1, band);
          int vX1Y_1 = in.getPixelXYBByte(x + 1, y - 1, band);
          int vY1 = in.getPixelXYBByte(x, y + 1, band);

          // Get the median value of the 8 pixels
          int[] pixels = new int[] {v, vX1, vXY1, vXY_1, vX_1, vY_1, vX_1Y1, vX1Y_1, vY1};
          Arrays.sort(pixels);
          newImg.setPixelXYBByte(x, y, band, pixels[4]);
        }

      }
    }

    return newImg;
  }


  /**
   * Returns the histogram of an RGB image.
   * 
   * @param img an RGB image
   * @return the colored histogram of the image
   */
  public static double[][] getRgbHistogram(Image img) {
    int width = img.getXDim();
    int height = img.getYDim();

    // Initialize an empty histogram
    double[][] histogram = new double[256][3];
    for (int i = 0; i < histogram.length; i++) {
      histogram[i][0] = histogram[i][1] = histogram[i][2] = 0.0;
    }

    // Build the RGB histogram
    for (int x = 0; x < width - 1; x++) {
      for (int y = 0; y < height - 1; y++) {
        int r = img.getPixelXYBByte(x, y, 0);
        int g = img.getPixelXYBByte(x, y, 1);
        int b = img.getPixelXYBByte(x, y, 2);
        histogram[r][0] += 1;
        histogram[g][1] += 1;
        histogram[b][2] += 1;
      }
    }

    return histogram;
  }


  /**
   * Returns a discrete histogram.
   * 
   * @param histogram Histogram of an RGB image
   * @return a new discrete histogram
   */
  public static double[][] getDiscreteHistogram(double[][] histogram) {
    double[][] discreteHistogram = new double[8][3];

    double sumR = 0;
    double sumG = 0;
    double sumB = 0;

    for (int i = 0; i < discreteHistogram.length; i++) {
      sumR = 0;
      sumG = 0;
      sumB = 0;

      for (int j = 0; j < 32 + i * 32; j++) {
        sumR += histogram[j][0];
        sumG += histogram[j][1];
        sumB += histogram[j][2];
      }

      discreteHistogram[i][0] = sumR / 32;
      discreteHistogram[i][1] = sumG / 32;
      discreteHistogram[i][2] = sumB / 32;
    }
    return discreteHistogram;
  }


  public static double[][] getDiscreteHsvHistogram(double[][] histogram) {
    double[][] discreteHistogram = new double[10][3];

    double sumH = 0;
    double sumS = 0;
    double sumV = 0;

    for (int i = 0; i < discreteHistogram.length; i++) {
      sumH = sumS = sumV = 0;

      for (int j = 0; j < 10 + i * 10; j++) {
        sumH += histogram[j][0];
        sumS += histogram[j][1];
        sumV += histogram[j][2];
      }

      discreteHistogram[i][0] = sumH / 10;
      discreteHistogram[i][1] = sumS / 10;
      discreteHistogram[i][2] = sumV / 10;
    }
    return discreteHistogram;
  }


  /**
   * Returns a normalised histogram from an RGB histogram.
   * 
   * @param hist a histogram of an RGB image
   * @param pixelCount the number of pixels in the image
   * @return the normalised histogram
   */
  public static double[][] getNormalisedRgbHistogram(double[][] hist, int pixelCount) {
    double[][] histogram = new double[hist.length][3];

    for (int i = 0; i < hist.length; i++) {
      histogram[i][0] = hist[i][0] / pixelCount;
      histogram[i][1] = hist[i][1] / pixelCount;
      histogram[i][2] = hist[i][2] / pixelCount;
    }

    return histogram;
  }

  /**
   * Retuns the Euclidean distance between two histograms.
   * 
   * @param histo1 (must be same length as histo2)
   * @param histo2 (must be same length as histo1)
   * @return Euclidean distance
   */
  public static double getRgbSimilarity(double[][] histo1, double[][] histo2) {
    double r = 0;
    double g = 0;
    double b = 0;
    for (int i = 0; i < histo2.length; i++) {
      r += Math.pow((histo1[i][0] - histo2[i][0]), 2);
      g += Math.pow((histo1[i][1] - histo2[i][1]), 2);
      b += Math.pow((histo1[i][2] - histo2[i][2]), 2);
    }
    return Math.sqrt(r) + Math.sqrt(g) + Math.sqrt(b);
  }


  public static double[][] getHsvHistogram(Image img) {
    int width = img.getXDim();
    int height = img.getYDim();

    // Initialize an empty histogram
    double[][] histogram = new double[101][3];
    for (int i = 0; i < histogram.length; i++) {
      histogram[i][0] = histogram[i][1] = histogram[i][2] = 0.0;
    }

    // Build the HSV histogram
    for (int x = 0; x < width - 1; x++) {
      for (int y = 0; y < height - 1; y++) {

        int r = img.getPixelXYBByte(x, y, 0);
        int g = img.getPixelXYBByte(x, y, 1);
        int b = img.getPixelXYBByte(x, y, 2);
        HashMap<String, Double> hsv = rgbToHsv(r, g, b);

        // H
        histogram[(int) Math.round(hsv.get("H") * 100)][0] += 1;
        // S
        histogram[(int) Math.round(hsv.get("S") * 100)][1] += 1;
        // V
        histogram[(int) Math.round(hsv.get("V") * 100)][2] += 1;
      }
    }

    return histogram;
  }


  /**
   * Converts a given RGB color to HSV equivalent.
   * 
   * @param r amount of red (0-255)
   * @param g amount of green (0-255)
   * @param b amount of blue (0-255)
   * @return (0 <= H <= 360; 0 <= S,V <= 1)
   */
  public static HashMap<String, Double> rgbToHsv(double r, double g, double b) {
    double maxColor = Math.max(Math.max(r, g), b);
    double minColor = Math.min(Math.min(r, g), b);
    double diff = maxColor - minColor;

    HashMap<String, Double> hsv = new HashMap<>(3);

    // Hue
    if (diff == 0) {
      // h = 0;
      hsv.put("H", 0.0);
    } else if (r == maxColor) {
      // h = (6 + (g - b) / diff) % 6;
      // hsv.put("H", (double) Math.round(((((6 + (g - b) / diff) % 6) / 6) * 360)));
      hsv.put("H", ((((6 + (g - b) / diff) % 6) / 6)));
    } else if (g == maxColor) {
      // h = 2 + (b - r) / diff;
      // hsv.put("H", (double) Math.round(((2 + (b - r) / diff) / 6) * 360));
      hsv.put("H", ((2 + (b - r) / diff) / 6));
    } else if (b == maxColor) {
      // h = 4 + (r - g) / diff;
      // hsv.put("H", (double) Math.round(((4 + (r - g) / diff) / 6) * 360));
      hsv.put("H", ((4 + (r - g) / diff) / 6));

    } else {
      // h = 0;
      hsv.put("H", 0.0);
    }


    // Saturation
    if (maxColor != 0) {
      // s = diff / maxColor;
      hsv.put("S", diff / maxColor);
    } else {
      // s = 0;
      hsv.put("S", 0.0);
    }

    // Value
    // v = maxColor / 255.;
    hsv.put("V", maxColor / 255.0);

    return hsv;

  }

  // TODO: Grayscale

  /**
   * Returns the histogram of a grayscale image.
   * 
   * @param img a grayscale image
   * @return the histogram of the image
   */
  public static double[] getHistogram(Image img) {
    int width = img.getXDim();
    int height = img.getYDim();

    // Initialize an empty histogram
    double[] histogram = new double[256];
    for (int i = 0; i < histogram.length; i++) {
      histogram[i] = 0.0;
    }

    // Build the histogram
    for (int x = 0; x < width - 1; x++) {
      for (int y = 0; y < height - 1; y++) {
        int v = img.getPixelXYBByte(x, y, 0);
        histogram[v] += 1;
      }
    }

    return histogram;
  }
}
