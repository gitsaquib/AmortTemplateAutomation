package nbcu.compass.amorttemplate.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.sikuli.basics.Settings;
import org.sikuli.script.Finder;
import org.sikuli.script.Image;
import org.sikuli.script.Match;

import io.appium.java_client.windows.WindowsDriver;

public class SikuliImageRecognitionUtil {
	
	
	public static void main(String args[]) {
		double[] iconCoords = null;
		int screenWidth = 1366;
		int screenHeight = 728;
		File screenshot = new File("C:\\Users\\mohammed.saquib\\Downloads\\images\\test.png");
		BufferedImage fullscreen;
		try {
			fullscreen = resizeImage(ImageIO.read(screenshot), screenWidth, screenHeight);
			File assistiveTouchIcon = new File("C:\\Users\\mohammed.saquib\\Downloads\\images\\iconDark.png");
			BufferedImage icon = resizeImage(ImageIO.read(assistiveTouchIcon), 19, 18);
			iconCoords = findSubImageBySikuli(fullscreen, icon);
			if(null == iconCoords) {
				iconCoords = findSubimage(toGreyScale(fullscreen), toGreyScale(icon));
			}
			if(null != iconCoords) {
				iconCoords[0] += (icon.getWidth() / 2); 
				iconCoords[1] += (icon.getHeight() / 2);
			}
			System.out.println(iconCoords[0] + " - " + iconCoords[1]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static void clickImage(WindowsDriver appSession) throws Exception {
		double[] iconCoords = null;
		String iconPath = System.getProperty("user.dir") + File.separator + "images" + File.separator;
		Dimension screen = appSession.manage().window().getSize();
		int screenWidth = screen.getWidth();
		int screenHeight = screen.getHeight();
		
		
		File screenshot = ((TakesScreenshot) appSession).getScreenshotAs(OutputType.FILE);
		BufferedImage fullscreen = resizeImage(ImageIO.read(screenshot), screenWidth, screenHeight);

		File assistiveTouchIcon = new File(iconPath + "iconDark.png");
		BufferedImage icon = resizeImage(ImageIO.read(assistiveTouchIcon), 76 * (screenWidth / 768), 76 * (screenWidth / 768));

		iconCoords = findSubImageBySikuli(fullscreen, icon);

		if(null == iconCoords) {
			iconCoords = findSubimage(toGreyScale(fullscreen), toGreyScale(icon));
		}
		
		if(null != iconCoords) {
			iconCoords[0] += (icon.getWidth() / 2); 
			iconCoords[1] += (icon.getHeight() / 2);
		}
		System.out.println(iconCoords[0] + " - " + iconCoords[1]);
		
		tapAt(appSession, (int) iconCoords[0], (int) iconCoords[1]);
	}
	
	@SuppressWarnings("rawtypes")
	public static void tapAt(WindowsDriver appSession, int tapPointX, int tapPointY) {
		Actions vActions = new Actions(appSession);
		vActions.moveByOffset(-170, 0);
		vActions.doubleClick();
		Action vClickAction = vActions.build();
		vClickAction.perform();
	}
	
	public static BufferedImage toGreyScale(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int p = img.getRGB(x, y);

				int a = (p >> 24) & 0xff;
				int r = (p >> 16) & 0xff;
				int g = (p >> 8) & 0xff;
				int b = p & 0xff;

				int avg = (r + g + b) / 3;

				p = (a << 24) | (avg << 16) | (avg << 8) | avg;

				img.setRGB(x, y, p);
			}
		}
		return img;
	}
	
	public static double[] findSubImageBySikuli(BufferedImage img1, BufferedImage img2, double... MinSimilarity) {
		double[] iconCoords = null;
		Finder screen = new Finder(img1);
		double defaultMinSimilarity = Settings.MinSimilarity;
		if (MinSimilarity.length > 0)
			Settings.MinSimilarity = MinSimilarity[0];
		System.out.println(screen.find(new Image(img2)));
		if (screen.hasNext()) {
			Match match = screen.next();
			iconCoords = new double[] { match.getX(), match.getY(), Settings.MinSimilarity };
		}
		screen.destroy();
		Settings.MinSimilarity = defaultMinSimilarity;
		return iconCoords;
	}
	
	public static double[] findSubimage(BufferedImage im1, BufferedImage im2) throws IOException {
		File fullscreen = new File("C:\\Users\\mohammed.saquib\\Downloads\\fullscreen.png");
	    ImageIO.write(im1, "png", fullscreen);
	    
	    File icon = new File("C:\\Users\\mohammed.saquib\\Downloads\\icon.png");
	    ImageIO.write(im2, "png", icon);
	    
		int w1 = im1.getWidth();
		int h1 = im1.getHeight();
		int w2 = im2.getWidth();
		int h2 = im2.getHeight();
		assert (w2 <= w1 && h2 <= h1);
		int bestX = 0;
		int bestY = 0;
		double lowestDiff = Double.POSITIVE_INFINITY;
		for (int x = 0; x < w1 - w2; x++) {
			double comp = compareImages(im1.getSubimage(x, 0, w2, h2), im2);
			if (comp < lowestDiff) {
				bestX = x;
				bestY = 0;
				lowestDiff = comp;
			}
		}
		for (int x = 0; x < w1 - w2; x++) {
			double comp = compareImages(im1.getSubimage(x, h1 - h2, w2, h2), im2);
			if (comp < lowestDiff) {
				bestX = x;
				bestY = h1 - h2;
				lowestDiff = comp;
			}
		}
		for (int y = h2; y < h1 - h2; y++) {
			double comp = compareImages(im1.getSubimage(0, y, w2, h2), im2);
			if (comp < lowestDiff) {
				bestX = 0;
				bestY = y;
				lowestDiff = comp;
			}
		}
		for (int y = h2; y < h1 - h2; y++) {
			double comp = compareImages(im1.getSubimage(w1 - w2, y, w2, h2), im2);
			if (comp < lowestDiff) {
				bestX = w1 - w2;
				bestY = y;
				lowestDiff = comp;
			}
		}

		return new double[] { bestX, bestY, lowestDiff };
	}
	
	public static double compareImages(BufferedImage im1, BufferedImage im2) {
		assert (im1.getHeight() == im2.getHeight() && im1.getWidth() == im2.getWidth());
		double variation = 0.0;
		for (int x = 0; x < im1.getWidth(); x++) {
			for (int y = 0; y < im1.getHeight(); y++) {
				variation += compareARGB(im1.getRGB(x, y), im2.getRGB(x, y)) / Math.sqrt(3);
			}
		}
		return variation / (im1.getWidth() * im1.getHeight());
	}
	
	public static double compareARGB(int rgb1, int rgb2) {
		double r1 = ((rgb1 >> 16) & 0xFF) / 255.0;
		double r2 = ((rgb2 >> 16) & 0xFF) / 255.0;
		double g1 = ((rgb1 >> 8) & 0xFF) / 255.0;
		double g2 = ((rgb2 >> 8) & 0xFF) / 255.0;
		double b1 = (rgb1 & 0xFF) / 255.0;
		double b2 = (rgb2 & 0xFF) / 255.0;
		double a1 = ((rgb1 >> 24) & 0xFF) / 255.0;
		double a2 = ((rgb2 >> 24) & 0xFF) / 255.0;
		return a1 * a2 * Math.sqrt((r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2));
	}
	
	private static BufferedImage resizeImage(BufferedImage originalImage, int IMG_WIDTH, int IMG_HEIGHT) {
		int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
		BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
		Graphics2D g = resizedImage.createGraphics();
		
		g.setComposite(AlphaComposite.Src);
	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    
		g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
		g.dispose();
		System.out.println("Resized Image: "+resizedImage);
		return resizedImage;
	}
}
