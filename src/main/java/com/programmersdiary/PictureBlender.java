package com.programmersdiary;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
		List<BufferedImage> loadedImages = loadAllImages(files);
		setDimensions(loadedImages);
		processAllImages(loadedImages);
		currentPixels = new int[width * height];
		initializeCurrentState();
	}

	private List<BufferedImage> loadAllImages(File[] files) {
		List<BufferedImage> loadedImages = new ArrayList<>();
		for (File file : files) {
			BufferedImage image = loadSingleImage(file);
			if (image != null) {
				loadedImages.add(image);
			}
		}
		return loadedImages;
	}

	private BufferedImage loadSingleImage(File file) {
		try {
			return ImageIO.read(file);
		} catch(IOException e) {
			System.err.println("Failed to load image: " + file.getName());
			e.printStackTrace();
			return null;
		}
	}

	private void setDimensions(List<BufferedImage> images) {
		int maxWidth = 0;
		int maxHeight = 0;
		for (BufferedImage image : images) {
			maxWidth = Math.max(maxWidth, image.getWidth());
			maxHeight = Math.max(maxHeight, image.getHeight());
		}
		width = maxWidth;
		height = maxHeight;
	}

	private void processAllImages(List<BufferedImage> images) {
		for (BufferedImage image : images) {
			resizeAndAdd(image);
		}
	}

	private void initializeCurrentState() {
		currentPictureIndex = 0;
		System.arraycopy(pictures.get(0), 0, currentPixels, 0, width * height);
		transitionCounter = 0;
	}

	private void resizeAndAdd(BufferedImage originalImage) {
		BufferedImage resizedImage = createResizedCanvas();
		Graphics2D g2d = setupGraphics(resizedImage);
		drawImageCentered(g2d, originalImage);
		g2d.dispose();
		addPixelsToCollection(resizedImage);
	}

	private BufferedImage createResizedCanvas() {
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}

	private Graphics2D setupGraphics(BufferedImage image) {
		Graphics2D g2d = image.createGraphics();
		setRenderingHints(g2d);
		fillBackground(g2d);
		return g2d;
	}

	private void setRenderingHints(Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	private void fillBackground(Graphics2D g2d) {
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, width, height);
	}

	private void drawImageCentered(Graphics2D g2d, BufferedImage originalImage) {
		int x = (width - originalImage.getWidth()) / 2;
		int y = (height - originalImage.getHeight()) / 2;
		g2d.drawImage(originalImage, x, y, null);
	}

	private void addPixelsToCollection(BufferedImage image) {
		int[] pixels = new int[width * height];
		image.getRGB(0, 0, width, height, pixels, 0, width);
		pictures.add(pixels);
	}
	
	public void transition(double transitionWeight) {
		if (pictures.isEmpty()) return;
		
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
