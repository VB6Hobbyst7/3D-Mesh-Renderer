package renderer;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.Pipe;
import java.util.*;
import java.util.Vector;

public class Renderer extends GUI {

	public Scene scene;
	public final float factor = (float) (0.02*Math.PI);
	private boolean scaled = false;

	@Override
	protected void onLoad(File file) {
		// TODO fill this in.

		// I use this to see if I've scaled my object yet, as before it would shrink and grow to the size of the screen
		scaled = false;

		/*
		 * This method should parse the given file into a Scene object, which
		 * you store and use to render an image.
		 */
		try{

			// Declares the list of Polygons and the map of Lights, is a linkedHashMap so it is order by insertion
			ArrayList<Scene.Polygon> polygonList = new ArrayList<>();
			Map<Vector3D, Color> lightList = new LinkedHashMap<>();

			BufferedReader br = new BufferedReader(new FileReader(file));
			String line; String[] splitLine; // a variable for the line and split line is used to read and separate the data, respectively

			// since the number of polygons is on one line by itself, turns that line into an integer
			int numOfTris = strToInt(br.readLine());

			for (int i = 0; i < numOfTris; i++) {

				line = br.readLine();
				splitLine = line.split(","); // uses a , as a delimiter to read the data in its distinct columns

				// the colours in the txt files are split by the first three integers
				int cols[] = strsToInts(splitLine[0],splitLine[1],splitLine[2]);

				float[] coords = new float[9];
				for (int j = 0; j < 3; j++) {
					coords[j*3] = strToFloat(splitLine[j*3+3]);		// gets 0, 3, and 6 from splitLine values 3, 6 and 9
					coords[j*3+1] = strToFloat(splitLine[j*3+4]);	// gets 1, 4, and 7 from splitLine values 4, 7 and 10
					coords[j*3+2] = strToFloat(splitLine[j*3+5]);	// gets 2, 5, and 8 from splitLine values 5, 8 and 11
				}

				polygonList.add(new Scene.Polygon(coords,cols));

			}

			// repeats this as it has to read the last line of the document
			line = br.readLine();
			splitLine = line.split(","); // uses a , as a delimiter to read the data in its distinct columns

			// grabs the light vector that the file has by default, with an initial colour of white
			float[] vect = strsToFloats(splitLine[0],splitLine[1],splitLine[2]);
			Color color = new Color(255,255,255);

			lightList.put(new Vector3D(vect[0],vect[1],vect[2]), color);

			br.close();

			// initialises the scene here that is referenced to throughout the render class
			scene = new Scene(polygonList, lightList);

		} catch(IOException e){
			System.out.println("File for polygons was unable to be read properly.");
		}
	}

	/**
	 * Takes a String and returns it as an integer.
	 */
	private static int strToInt(String s) {
		return Integer.parseInt(s);
	}

	/**
	 * Takes a String and returns it as a float.
	 */
	private static float strToFloat(String s) {
		return Float.parseFloat(s);
	}

	/**
	 * Takes three Strings (x, y, z) and returns them as a float array
	 * Used primarily for making Vector3D Objects, and calls the strToFloat class too
	 * @param x is the x coord
	 * @param y is the y coord
	 * @param z is the z coord
	 */
	private static float[] strsToFloats(String x, String y, String z) {
		return new float[]{strToFloat(x),strToFloat(y),strToFloat(z)};
	}


	/**
	 * Takes three Strings (r, g, b) and returns them as an integer array
	 * Used primarily for making Color objects, and calls the strToInt class too
	 * @param r
	 * @param g
	 * @param b
	 */
	private static int[] strsToInts(String r, String g, String b) {
		return new int[]{strToInt(r),strToInt(g),strToInt(b)};
	}

	/**
	 * Method is called upon a KeyEvent being triggered in the GUI class
	 * Used for rotating the object on screen
	 * @param ev
	 */
	@Override
	protected void onKeyPress(KeyEvent ev) {
		// TODO fill this in.

		// If the scene is null, no need to check for keys
		if (scene == null) return;

		/*
		 * This method should be used to rotate the user's viewpoint.
		 */
		// left rotation
		if (ev.getKeyCode() == KeyEvent.VK_LEFT
				|| Character.toUpperCase(ev.getKeyChar()) == 'A')
			scene = Pipeline.rotateScene(scene, 0, -factor);

		// right rotation
		else if (ev.getKeyCode() == KeyEvent.VK_RIGHT
				|| Character.toUpperCase(ev.getKeyChar()) == 'D')
			scene = Pipeline.rotateScene(scene, 0, factor);

		// downward rotation
		else if (ev.getKeyCode() == KeyEvent.VK_DOWN
				|| Character.toUpperCase(ev.getKeyChar()) == 'S')
			scene = Pipeline.rotateScene(scene, -factor, 0);

		// upward rotation
		else if (ev.getKeyCode() == KeyEvent.VK_UP
				|| Character.toUpperCase(ev.getKeyChar()) == 'W')
			scene = Pipeline.rotateScene(scene, factor, 0);

	}

	/**
	 * Renders the scene data onto the screen
	 * @return
	 */
	@Override
	protected BufferedImage render() {
		// TODO fill this in.

		// if the scene is null, doesn't need to check anything
		if (scene == null) return null;

		// Declares the bitmap and zdepth, and initialises them in the array
		Color[][] bitmap = new Color[CANVAS_WIDTH][CANVAS_HEIGHT];
		float[][] zdepth = new float[CANVAS_WIDTH][CANVAS_HEIGHT];
		initArrays(bitmap, zdepth);

		// transforms the scene to the centre in another method
		transformScene();

		// grabs the ambientLight from the GUI class
		Color ambLight = new Color(getAmbientLight()[0],getAmbientLight()[1],getAmbientLight()[2]);

		// cycles through all the main checks in the Pipeline class
		for (Scene.Polygon p : scene.getPolygons()) {

			if (!Pipeline.isHidden(p)) {

				// gets the proper shading colour (I changed the variables that were passed over since I use a LinkedHashMap)
				Color col = Pipeline.getShading(p,scene.getLights(),ambLight);
				// declares the edgelist of the polygon being cycled through
				EdgeList eL = Pipeline.computeEdgeList(p);
				// computes the zbuffer of said polygon
				Pipeline.computeZBuffer(bitmap,zdepth,eL,col);

			}
		}

		// after all the polygons have been drawn, fills in any holes that have not been coloured in the for loop above
		Pipeline.removeHoles(bitmap,zdepth);

		// converts the Color array into a bitmap
		return convertBitmapToImage(bitmap);

	}

	/**
	 * Converts a 2D array of Colors to a BufferedImage. Assumes that bitmap is
	 * indexed by column then row and has imageHeight rows and imageWidth
	 * columns. Note that image.setRGB requires x (col) and y (row) are given in
	 * that order.
	 */
	private BufferedImage convertBitmapToImage(Color[][] bitmap) {
		BufferedImage image = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < CANVAS_WIDTH; x++) {
			for (int y = 0; y < CANVAS_HEIGHT; y++) {
				image.setRGB(x, y, bitmap[x][y].getRGB());
			}
		}
		return image;
	}

	/**
	 * Initialises the 2D Arrays used for the bitmap and zdepth
	 * Created purely for a cleaner looking method
	 *
	 * @param bitmap the Color[][] array used for the bitmap render
	 * @param zdepth the float[][] array used for the zdepth of the polygons
	 */
	private void initArrays(Color[][] bitmap, float[][]zdepth) {

		// this for loop initialises all the values in the bitmap array and zdepth
		for (int i = 0; i < CANVAS_HEIGHT; i++) {
			for (int j = 0; j < CANVAS_WIDTH; j++) {

				// creates a grid pattern from the background
				// if a polygon doesn't cover that pixel, it will remain this colour
				if (i % 120 == 0 || j % 120 == 0) bitmap[i][j] = Color.WHITE;
				else bitmap[i][j] = Color.DARK_GRAY;

				// by default, the zdepth should be positive infinity
				// (the furthest back that a polygon could go)
				zdepth[i][j] = Float.POSITIVE_INFINITY;

			}
		}

	}

	/**
	 * Purely for cleanliness in the render method above
	 * Transforms the scene to the center it and scales it if necessary
	 */
	private void transformScene(){

		// if the scene hasn't been scaled yet, scales it to the appropriate size
		if (!scaled) {
			scene = Pipeline.scaleScene(scene);
			scaled = true;
		}
		// translates the scene to the centre
		scene = Pipeline.translateScene(scene);

	}

	/**
	 * Removes all lights in the scene that was added
	 */
	@Override
	protected void removeAllLight() {

		// the only requirements for deleting all the lights
		if (scene != null && !scene.getLights().isEmpty())
			scene.clearLight();

	}

	/**
	 * Removes the last light in the scene that was added
	 */
	@Override
	protected void removePrevLight() {

		// the only requirements for deleting a single light
		if (scene != null && !scene.getLights().isEmpty())
			scene.removeLastLight();

	}

	/**
	 * Adds a random light into the scene at a random x, y, z coordinate
	 * with a random r, g, b color
	 */
	@Override
	protected void addRandLight() {

		if (scene != null) {

			// uses the Random object
			Random rand = new Random();

			// initialises all the floats
			float x = 0.0f, y = 0.0f, z = 0.0f;

			// x, y, z at 0.0 makes the object shade black, this while condition prevents that
			while (Math.abs(x) + Math.abs(y) + Math.abs(z) == 0.0f) {
				// rand.nextFloat() - rand.nextFloat() will return a number between -1 and 1
				x = rand.nextFloat() - rand.nextFloat();
				y = rand.nextFloat() - rand.nextFloat();
				z = rand.nextFloat() - rand.nextFloat();

			}

			// inputs these coordinates into a new Vector
			Vector3D randVector = new Vector3D(x,y,z);

			// picks a random color using rand.nextInt()
			int r = rand.nextInt(255), g = rand.nextInt(255), b = rand.nextInt(255);
			Color randColor = new Color(r,g,b);

			scene.addLight(randVector, randColor);

		}

	}

	/**
	 * Adds a custom light based on the users choices in the GUI
	 */
	@Override
	protected void addCustomLight() {

		if (scene != null) {

			// gets the light coordinates
			float[] coords = getLightCoords();

			// much like the random method, this makes sure that the light coordinates are not 0,0,0
			if (Math.abs(coords[0]) + Math.abs(coords[1]) + Math.abs(coords[2]) > 0.0f) {

				// makes the vector object
				Vector3D vector = new Vector3D(coords[0],coords[1],coords[2]);
				// picks the color
				Color color = new Color(getCustomLight()[0], getCustomLight()[1], getCustomLight()[2]);

				scene.addLight(vector,color);

			}

		}

	}

	public static void main(String[] args) {
		new Renderer();
	}
}

// code for comp261 assignments
