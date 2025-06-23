package com.programmersdiary;

import java.util.Arrays;

public class Screen {
	
	private final int width;
	private final int height;
	private final PictureBlender pictureBlender;
	public int[] pixels;
	
	public Screen(int width, int height, PictureBlender pictureBlender) {
		this.width = width;
		this.height = height;
        this.pictureBlender = pictureBlender;
        pixels = new int[width * height];
	}
	
	public void clear() {
        Arrays.fill(pixels, 0);
	}
	
	public void render() {
		for(int y = 0; y < height; y++) {
            if (width >= 0) System.arraycopy(pictureBlender.currentPixels(), y * width, pixels, y * width, width);
		}
	}

}
