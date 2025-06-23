package com.programmersdiary;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PictureBlender {
	
	private final List<int[]> pictures;
	private int width;
	private int height;
	private int currentPictureIndex;
	private final int[] currentPixels;
	private int transitionCounter;
	
	public PictureBlender(File[] files) {
		pictures = new ArrayList<>();
		for (File file : files) load(file);
		currentPictureIndex = 0;
		currentPixels = Arrays.copyOf(pictures.get(0), width * height);
		transitionCounter = 0;
	}

	private void load(File file) {
		try {
			BufferedImage image = ImageIO.read(file);
			width = image.getWidth();
			height = image.getHeight();
			int[] pixels = new int[width * height];
			image.getRGB(0, 0, width, height, pixels, 0, width);
			pictures.add(pixels);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void transition(double transitionWeight) {
		if (currentPictureIndex >= (pictures.size() - 1)) {
			currentPictureIndex = 0;
			transitionCounter = 0;
		}
		
		transitionCounter++;
		if (transitionCounter >= transitionWeight) {
			switchToNextImage();
			return;
		}

		progressTransition(transitionWeight);
	}

	private void switchToNextImage() {
		currentPictureIndex++;
		if (currentPictureIndex >= pictures.size()) {
			currentPictureIndex = 0;
		}
		transitionCounter = 0;
		System.arraycopy(pictures.get(currentPictureIndex), 0, currentPixels, 0, currentPixels.length);
	}

		private void progressTransition(double transitionWeight) {
		int nextIndex = (currentPictureIndex + 1) % pictures.size();
		double transitionProgress = (double) transitionCounter / transitionWeight;

		for(int i = 0; i < currentPixels.length; i++) {
			Color currentColour = new Color(pictures.get(currentPictureIndex)[i], true);
			Color nextColour = new Color(pictures.get(nextIndex)[i], true);

			int fa = (int) (currentColour.getAlpha() + (nextColour.getAlpha() - currentColour.getAlpha()) * transitionProgress);
			int fb = (int) (currentColour.getBlue() + (nextColour.getBlue() - currentColour.getBlue()) * transitionProgress);
			int fg = (int) (currentColour.getGreen() + (nextColour.getGreen() - currentColour.getGreen()) * transitionProgress);
			int fr = (int) (currentColour.getRed() + (nextColour.getRed() - currentColour.getRed()) * transitionProgress);

			fa = Math.max(0, Math.min(255, fa));
			fr = Math.max(0, Math.min(255, fr));
			fg = Math.max(0, Math.min(255, fg));
			fb = Math.max(0, Math.min(255, fb));

			currentPixels[i] = new Color(fr, fg, fb, fa).getRGB();
		}
	}

	public int[] currentPixels() {
		return currentPixels;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
