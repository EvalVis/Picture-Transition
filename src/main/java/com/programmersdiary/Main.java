package com.programmersdiary;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class Main extends Canvas implements Runnable{
	
	private static final long serialVersionUID = 1L;

    public static int scale = 1;
	
	private Thread thread;
	private JFrame frame;
	private boolean running;
	
	private Screen screen;
	
	private BufferedImage image;
	private int[] pixels;
	public static int transitionWeight = 60;
	private PictureBlender pictureBlender;
	
	private JPanel controlPanel;
	private JButton selectFolderButton;
	private JLabel statusLabel;
	private JSpinner speedSpinner;
    private boolean hostInitialized = false;
	
	
	public synchronized void start() {
		running = true;
		thread = new Thread(this, "Main");
		thread.start();
	}
	
	public synchronized void stop() {
		running = false;
		try {
			thread.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public Main() {
		setupUI();
	}
	
	private void setupUI() {
		frame = new JFrame("Picture Transition");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		
		controlPanel = new JPanel(new FlowLayout());
		selectFolderButton = new JButton("Select Folder");
		statusLabel = new JLabel("Please select a folder containing images");
        JLabel speedLabel = new JLabel("Speed (seconds):");
		speedSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
		
		selectFolderButton.addActionListener(e -> selectFolder());
		
		speedSpinner.addChangeListener(e -> updateSpeed());
		
		controlPanel.add(selectFolderButton);
		controlPanel.add(statusLabel);
		controlPanel.add(speedLabel);
		controlPanel.add(speedSpinner);
		
		frame.add(controlPanel, BorderLayout.NORTH);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private void selectFolder() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setDialogTitle("Select folder containing images");
		
		int result = fileChooser.showOpenDialog(frame);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFolder = fileChooser.getSelectedFile();
			File[] imageFiles = selectedFolder.listFiles((dir, name) -> {
				String lower = name.toLowerCase();
				return lower.endsWith(".png") || lower.endsWith(".jpg") || 
				       lower.endsWith(".jpeg") || lower.endsWith(".gif") || lower.endsWith(".bmp");
			});
			
			if (imageFiles != null && imageFiles.length > 0) {
				initializeHost(imageFiles);
				statusLabel.setText("Loaded " + imageFiles.length + " images");
				selectFolderButton.setText("Change Folder");
			} else {
				statusLabel.setText("No image files found in selected folder");
			}
		}
	}
	
	private void updateSpeed() {
		int seconds = (Integer) speedSpinner.getValue();
		transitionWeight = seconds * 60;
	}
	
	private void initializeHost(File[] imageFiles) {
		pictureBlender = new PictureBlender(imageFiles);
		int width = pictureBlender.getWidth();
		int height = pictureBlender.getHeight();
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		screen = new Screen(width, height, pictureBlender);
		
		Dimension size = new Dimension(width * scale, height * scale);
		setPreferredSize(size);
		
		frame.remove(controlPanel);
		frame.add(this, BorderLayout.CENTER);
		frame.add(controlPanel, BorderLayout.NORTH);
		frame.pack();
		
		updateSpeed();
		hostInitialized = true;
		start();
	}

	public static void main(String[] args) {
		new Main();
	}
	

	public void run() {
		long lastTime = System.nanoTime();
		final double ns = 1000000000.0 / 60.0;
		double delta = 0;
		while(running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while(delta >= 1) {
				update();
				delta--;
			}
			render();
		}
		stop();
	}
	
	private void render() {
		if (!hostInitialized) return;
		
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(3);
			return;
		}
		screen.clear();
		screen.render();
        System.arraycopy(screen.pixels, 0, pixels, 0, pixels.length);
		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
		g.dispose();
		bs.show();
	}
	
	private void update() {
		if (!hostInitialized) return;
		pictureBlender.transition(transitionWeight);
	}

}
