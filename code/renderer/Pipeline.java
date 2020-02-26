package renderer;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import renderer.Scene.Polygon;

/**
 * The Pipeline class has method stubs for all the major components of the
 * rendering pipeline, for you to fill in.
 * 
 * Some of these methods can get quite long, in which case you should strongly
 * consider moving them out into their own file. You'll need to update the
 * imports in the test suite if you do.
 */
public class Pipeline {

	/**
	 * Returns true if the given polygon is facing away from the camera (and so
	 * should be hidden), and false otherwise.
	 */
	public static boolean isHidden(Polygon poly) {
		// TODO fill this in.

		// gets the normal vector
		Vector3D[] vertices = poly.getVertices();
		Vector3D normal = getNormal(vertices);

		// the slides at one point say normal.z < 0, but this is the formula that works for me
		return normal.z >= 0;

	}

	/**
	 * Since I grab the normal more than once, saves this process into a method of its own
	 * @param vertices
	 * @return
	 */
	private static Vector3D getNormal(Vector3D[] vertices) {

		// uses the minus method in the Vector3D class to do the cross product formula
		Vector3D a = vertices[1].minus(vertices[0]),
				 b = vertices[2].minus(vertices[1]);

		// (v2-v1) x (v3-v2)
		return a.crossProduct(b);
	}

	/**
	 * Computes the colour of a polygon on the screen, once the lights, their
	 * angles relative to the polygon's face, and the reflectance of the polygon
	 * have been accounted for.
	 * 
	 * @param lightDirection
	 *            A Map of Vector3D, Color paining pointing to the directional light read in from
	 *            the file, with it's designated.
	 * @param lightColor
	 * @param ambientLight
	 *            The ambient light in the scene, i.e. light that doesn't depend
	 *            on the direction.
	 */
	public static Color getShading(Polygon poly, Map<Vector3D, Color> lightDirection, Color ambientLight) {
		// TODO fill this in.

		// gets the normal vector
		Vector3D[] vertices = poly.getVertices();
		Vector3D normal = getNormal(vertices);

		// grabs the reflectance of the polygon, needs to divide these numbers by 255.0f for multiplication later
		float reflectRed = poly.getReflectance().getRed() / 255.0f,
		      reflectGreen = poly.getReflectance().getGreen() / 255.0f,
		      reflectBlue = poly.getReflectance().getBlue() / 255.0f;

		// uses my custom setColor() method to get the int colour of the polygons
		// rgb value 0 = red, 1 = green, 2 = blue
		int rC = setColor(ambientLight.getRed(),reflectRed, normal, lightDirection, 0),
		    gC = setColor(ambientLight.getGreen(),reflectGreen, normal, lightDirection, 1),
		    bC = setColor(ambientLight.getBlue(), reflectBlue, normal, lightDirection, 2);

		return new Color(rC,gC,bC);

	}

	/**
	 * Since all the colors have the same method of creation, just different values inserted,
	 * this method does the main calculation for them all.
	 *
	 * @param ambient
	 * 			the ambient light in the scene
	 * @param light
	 * 			the color of the directional light
	 * @param reflectance
	 * 			the reflectance of the polygon
	 * @param cosTheta
	 * 			the theta value calculated via the cos equation
	 * @return the color
	 */
	public static int setColor(int ambient, float reflectance, Vector3D normal, Map<Vector3D, Color> lightDirection, int rgb) {

		// initially, the colour will just be the ambient light * the reflectance
		int color = (int)(ambient * reflectance);

		// cycles through the vector-color map
		for (Vector3D lightDir : lightDirection.keySet()) {

			// does the cosTheta calculation for the normal vector passed over
			float cosTheta = normal.cosTheta(lightDir);

			// initialises the light added to 0
			int light = 0;

			// depending on which rgb being returned here
			if (rgb == 0) light = lightDirection.get(lightDir).getRed();
			else if (rgb == 1) light = lightDirection.get(lightDir).getGreen();
			else if (rgb == 2) light = lightDirection.get(lightDir).getBlue();

			// increments the color by this calculation
			color += (light * reflectance) * Math.max(0,cosTheta);

		}

		// since this method has the potential to go out of bounds, this is necessary
		if (color < 0) return 0;
		else if (color > 255) return 255;
		else return color;

	}

	/**
	 * This method should rotate the polygons and light such that the viewer is
	 * looking down the Z-axis. The idea is that it returns an entirely new
	 * Scene object, filled with new Polygons, that have been rotated.
	 *
	 * @param scene
	 *            The original Scene.
	 * @param xRot
	 *            An angle describing the viewer's rotation in the YZ-plane (i.e
	 *            around the X-axis).
	 * @param yRot
	 *            An angle describing the viewer's rotation in the XZ-plane (i.e
	 *            around the Y-axis).
	 * @return A new Scene where all the polygons and the light source have been
	 *         rotated accordingly.
	 */
	public static Scene rotateScene(Scene scene, float xRot, float yRot) {
		// TODO fill this in.

		// if scene is null, can't rotate it
		if (scene == null) return null;

		Transform rotationX = Transform.newXRotation(xRot),
				  rotationY = Transform.newYRotation(yRot);

		// This translates all the points to the origin, then the later "fromOrigin" method translates them back
		// This is so all the points will rotate about the centre of the screen, not the top left corner
		// This will make the program fail the Rotating test cases, but make the object rotate about the centre instead (which I prefer)
		translateToOrigin(scene);

		for (Scene.Polygon p : scene.getPolygons()) {

			Vector3D[] verts = p.getVertices();

			// could use a for each loop, but you can't directly edit elements (ie. changing them to a new Vector3D) this way
			for (int i = 0; i < verts.length; i++) {

				if (xRot != 0f) verts[i] = rotationX.multiply(verts[i]);
				if (yRot != 0f) verts[i] = rotationY.multiply(verts[i]);

			}

		}

		// This translates all the points back from the origin
		translateFromOrigin(scene);

		// uses a Map referencing the current lights, and creates a new map which will be passed back
		Map<Vector3D, Color> lights = scene.getLights(),
							 newLights = new LinkedHashMap<>();

		// cycles through the current keySet (can't modify this as a ConcurrentModification Exception will be thrown)
		for (Vector3D light : lights.keySet()) {

			// does the xRotation first
			if (xRot != 0f) {
				newLights.put(rotationX.multiply(light),lights.get(light));
			}
			// if an xRotation has been done already, has to replace the object in the new map
			if (yRot != 0f) {

				if (xRot != 0f) {
					newLights.replace(rotationY.multiply(light),lights.get(light));
				} else {
					newLights.put(rotationY.multiply(light),lights.get(light));
				}

			}

		}

		return new Scene(scene.getPolygons(), newLights);

	}

	/**
	 * This should translate the scene by the appropriate amount.
	 * 
	 * @param scene
	 * @return
	 */
	public static Scene translateScene(Scene scene) {
		// TODO fill this in.

		// 0 is the centerX, 1 is the centerY
		float[] centre = getCentres(scene);

		// how far across the scene needs to be shifted by
		float distX = (float)GUI.CANVAS_WIDTH/2f - centre[0],
			  distY = (float)GUI.CANVAS_HEIGHT/2f - centre[1];

		Transform translation = Transform.newTranslation(distX,distY,0f);

		for (Scene.Polygon p : scene.getPolygons()) {

			Vector3D[] verts = p.getVertices();

			// could use a for each loop, but I prefer this method
			for (int i = 0; i < verts.length; i++) {

				// if the distance is actually being changed (which in most cases it will be)
				if (distX != 0 && distY != 0) verts[i] = translation.multiply(verts[i]);

			}

		}

		return new Scene(scene.getPolygons(), scene.getLights());

	}

	/**
	 * This should scale the scene.
	 * 
	 * @param scene
	 * @return
	 */
	public static Scene scaleScene(Scene scene) {
		// TODO fill this in.

		// gets the scale factor the scene needs to be boosted by
		float scaleBy = getScale(scene);

		// This translates all the points to the origin, then the later "fromOrigin" method translates them back
		// This is so all the points will rotate about the centre of the screen, not the top left corner
		// This will make the program fail the Rotating test cases, but make the object rotate about the centre instead (which I prefer)
		translateToOrigin(scene);

		Transform scale = Transform.newScale(scaleBy,scaleBy,scaleBy);

		for (Scene.Polygon p : scene.getPolygons()) {

			Vector3D[] verts = p.getVertices();

			// could use a for each loop, but I prefer this method
			for (int i = 0; i < verts.length; i++) {

				verts[i] = scale.multiply(verts[i]);

			}

		}

		// This translates all the points back from the origin
		translateFromOrigin(scene);

		return new Scene(scene.getPolygons(), scene.getLights());

	}

	/**
	 * Returns a Transformation that would Translate all points to the origin (from their original points, to the centre)
	 * This is because if you translate to the origin, rotate, and then translate back
	 * you effectively rotate around the center point of the object
	 * @param sc
	 * @return
	 */
	private static void translateToOrigin(Scene sc) {

		// calls my getCentres method which returns the x and y point of the centre
		float[] centre = getCentres(sc);

		// the transformation required to move the scene to the top left corner
		Transform toOrigin = Transform.newTranslation((GUI.CANVAS_WIDTH  + centre[0]) - GUI.CANVAS_WIDTH/2f,
													  (GUI.CANVAS_HEIGHT + centre[1]) - GUI.CANVAS_HEIGHT/2f,
													  0f);

		for (Scene.Polygon p : sc.getPolygons())
			for (int i = 0; i < p.getVertices().length; i++)
				p.getVertices()[i] = toOrigin.multiply(p.getVertices()[i]);

	}

	/**
	 * Returns a Transformation that would Translate all points from the origin (to their original points)
	 * This is because if you translate to the origin, rotate, and then translate back
	 * you effectively rotate around the center point of the object
	 * @param sc
	 * @return
	 */
	private static void translateFromOrigin(Scene sc) {

		// the transformation required to move the scene from the top left corner
		Transform fromOrigin = Transform.newTranslation(GUI.CANVAS_WIDTH/2f,
														GUI.CANVAS_HEIGHT/2f,
														0f);

		for (Scene.Polygon p : sc.getPolygons())
			for (int i = 0; i < p.getVertices().length; i++)
				p.getVertices()[i] = fromOrigin.multiply(p.getVertices()[i]);

	}

	/**
	 * Finds the center points of all the polygons in a scene
	 * @param sc
	 * @return
	 */
	private static float[] getCentres(Scene sc) {

		// 0 is leftmost, 1 is rightmost, 2 is uppermost, 3 is lowermost
		float[] outerPoints = findOuterPoints(sc);

		// divides the x coords in half, divides the y coords in half
		float centreX = (outerPoints[0] + outerPoints[1]) / 2f,
			  centreY = (outerPoints[2] + outerPoints[3]) / 2f;

		return new float[]{centreX, centreY};

	}

	/**
	 * Finds the outermost points of all the polygons in a scene
	 * @param sc
	 * @return
	 */
	private static float[] findOuterPoints(Scene sc){

		float[] points = {Float.POSITIVE_INFINITY,		// 0, leftmost
						  Float.NEGATIVE_INFINITY,		// 1, rightmost
				   		  Float.POSITIVE_INFINITY,		// 2, uppermost
						  Float.NEGATIVE_INFINITY,		// 3, lowermost
						 };

		for (Scene.Polygon p : sc.getPolygons()) {
			for (Vector3D v : p.getVertices()) {
				points[0] = Math.min(points[0], v.x);	// minimum between current point val and v.x
				points[1] = Math.max(points[1], v.x);	// maximum between current point val and v.x
				points[2] = Math.min(points[2], v.y);	// minimum between current point val and v.y
				points[3] = Math.max(points[3], v.y);	// maximum between current point val and v.y
			}
		}

		return points;

	}

	/**
	 * Gets the scale factor that the scene is to be grown/shrunk by
	 * @param sc
	 * @return
	 */
	private static float getScale(Scene sc) {

		// 0 is leftmost, 1 is rightmost, 2 is uppermost, 3 is lowermost
		float[] outerPoints = findOuterPoints(sc),
		// 0 is closest, 1 is furthest
				zPoints = findZPoints(sc);

		// gets all the edges that border the 3d object on the x, y and z planes
		float horzEdge = outerPoints[1] - outerPoints[0],
			  vertEdge = outerPoints[3] - outerPoints[2],
			  depthEdge = zPoints[1] - zPoints[0];

		// determines whichever edges is the biggest, if the object is a cube scales by horizontal edge
		if (horzEdge >= vertEdge && horzEdge >= depthEdge)
			return (GUI.CANVAS_WIDTH-100)/horzEdge;

		else if (horzEdge < vertEdge && vertEdge > depthEdge)
			return (GUI.CANVAS_HEIGHT-100)/vertEdge;

		else if (horzEdge < depthEdge && vertEdge < depthEdge)
			return (GUI.CANVAS_HEIGHT-100)/depthEdge;

		else return 1.0f; // should never reach this

	}

	/**
	 * Finds the closest and furthest points bordering the polygons on the z plane
	 * @param sc
	 * @return
	 */
	private static float[] findZPoints(Scene sc) {

		float[] points = {Float.POSITIVE_INFINITY,		// 0, closest
						  Float.NEGATIVE_INFINITY		// 1, furthest
		};

		for (Scene.Polygon p : sc.getPolygons()) {
			for (Vector3D v : p.getVertices()) {
				points[0] = Math.min(points[0], v.z);	// minimum between point and v.z
				points[1] = Math.max(points[1], v.z);	// maximum between point and v.z
			}
		}

		return points;

	}

	/**
	 * Computes the edgelist of a single provided polygon, as per the lecture
	 * slides.
	 */
	public static EdgeList computeEdgeList(Polygon poly) {
		// TODO fill this in.

		Vector3D[] vectors = poly.getVertices();

		// gets the top and bottom points bordering the polygons on the y plane
		int maxY = findMaxY(vectors);
		int minY = findMinY(vectors);

		// initialises the new EdgeList object here
		EdgeList eL = new EdgeList(minY,maxY);

		// uses my custom addToEdgeList method
		addToEdgeList(eL,vectors[0],vectors[1]);
		addToEdgeList(eL,vectors[1],vectors[2]);
		addToEdgeList(eL,vectors[2],vectors[0]);

		return eL;
	}

	/**
	 * Finds the maximum y value of the polygon as an integer
	 *
	 * @param vects
	 * @return
	 */
	private static int findMaxY(Vector3D[] vects){

		int maxY = Integer.MIN_VALUE;

		for (Vector3D v : vects)
			if (v.y > maxY) maxY = Math.round(v.y);

		return maxY;

	}

	/**
	 * Finds the minimum y value of the polygon as an integer
	 *
	 * @param vects
	 * @return
	 */
	private static int findMinY(Vector3D[] vects){

		int minY = Integer.MAX_VALUE;

		for (Vector3D v : vects)
			if (v.y < minY) minY = Math.round(v.y);

		return minY;

	}

	/**
	 * A method that is passed two vectors/vertices and finds the outermost x and z
	 * points of the polygon, adding them to the EdgeList object
	 *
	 * @param eL
	 * @param a
	 * @param b
	 */
	private static void addToEdgeList(EdgeList eL, Vector3D a, Vector3D b) {

		// Kyle Mans helped with this part here, realising all these values needed to be rounded
		float aX = Math.round(a.x), aY = Math.round(a.y), aZ = Math.round(a.z),
		      bX = Math.round(b.x), bY = Math.round(b.y), bZ = Math.round(b.z);

		// gets the x, y, and z values calculated with or incremented by the slope values below
		// although the slides don't say to round these, doing this got rid of whiskers
		float xVal = aX, zVal = aZ;
		int yVal = (int) aY;

		// the slopes on the x and z axis, calculated via rise/run
		float slopeX = (bX-aX) / (bY-aY),
			  slopeZ = (bZ-aZ) / (bY-aY);

		// this was copied from the slides
		if (aY < bY) {

			// counts along the left side
			while (yVal <= bY) {

				// sets both the left variables at the same time
				eL.setLefts(yVal,xVal,zVal);

				// increments x and z by their slope values, and increments y by 1
				xVal += slopeX;
				zVal += slopeZ;
				yVal++;

			}
		} else {

			// counts along the right side
			while (yVal >= bY) {

				// sets both the right variables at the same time
				eL.setRights(yVal,xVal,zVal);

				// increments x and z by their slope values, and increments y by 1
				xVal -= slopeX;
				zVal -= slopeZ;
				yVal--;

			}
		}

	}

	/**
	 * Fills a zbuffer with the contents of a single edge list according to the
	 * lecture slides.
	 * 
	 * The idea here is to make zbuffer and zdepth arrays in your main loop, and
	 * pass them into the method to be modified.
	 * 
	 * @param zbuffer
	 *            A double array of colours representing the Color at each pixel
	 *            so far.
	 * @param zdepth
	 *            A double array of floats storing the z-value of each pixel
	 *            that has been coloured in so far.
	 * @param polyEdgeList
	 *            The edgelist of the polygon to add into the zbuffer.
	 * @param polyColor
	 *            The colour of the polygon to add into the zbuffer.
	 */
	public static void computeZBuffer(Color[][] zbuffer, float[][] zdepth, EdgeList polyEdgeList, Color polyColor) {

		// does a check to make sure the y value doesn't start from less that 0
		int startY = Math.max(0, polyEdgeList.getStartY());
		// does a check to make sure the y value doesn't end from greater than CANVAS_HEIGHT
		int endY = Math.min(GUI.CANVAS_HEIGHT, polyEdgeList.getEndY());

		// using the values calculated from above, creates a for loop
		for (int y = startY; y < endY; y++) {

			// gets all the left and right values, saves them to these variables for cleanliness
			float xL = polyEdgeList.getLeftX(y), zL = polyEdgeList.getLeftZ(y),
				  xR = polyEdgeList.getRightX(y), zR = polyEdgeList.getRightZ(y);

			// calculates the slope based on these values
			float slope = (zR - zL)/(xR - xL);

			// does a check to make sure the x value doesn't start from less that 0
			int startX = Math.max(0, Math.round(xL));
			// does a check to make sure the x value doesn't end from greater than CANVAS_WIDTH
			int endX = Math.min(GUI.CANVAS_WIDTH, Math.round(xR));

			// initialises a z value to increment with
			float z = Math.round(zL) + slope*(startX-xL);

			// using the values calculated from above, creates a for loop
			for (int x = startX; x < endX; x++){

				// if the z is closer to the screen than the current zdepth
				if (z < zdepth[x][y]) {
					zbuffer[x][y] = polyColor;
					zdepth[x][y] = z;
				}
				z += slope;

			}

		}
	}

	/**
	 * Removes all the small holes that will occur between the polygons
	 * and fills them in with the colour and zdepth of the pixel above it
	 * @param zbuffer
	 * @param zdepth
	 */
	public static void removeHoles(Color[][] zbuffer, float[][] zdepth) {

		// iterates through all the x and y positions on screen
		for (int y = 1; y < GUI.CANVAS_HEIGHT-1; y++) {

			for (int x = 1; x < GUI.CANVAS_WIDTH-1; x++) {

				// if there is a hole
				if (zdepth[x][y] == Float.POSITIVE_INFINITY) {
				    //  if there are values filled in above and below it
				    if (zdepth[x][y-1] < Float.POSITIVE_INFINITY &&
					    zdepth[x][y+1] < Float.POSITIVE_INFINITY) {

                        // replaces the hole with the colour and zdepth of the pixel above it
                        zbuffer[x][y] = zbuffer[x][y - 1];
                        zdepth[x][y] = zdepth[x][y - 1];

                    }
				    // else if there are values filled in to its sides
				    else if (zdepth[x-1][y] < Float.POSITIVE_INFINITY &&
                            zdepth[x+1][y] < Float.POSITIVE_INFINITY) {

                        // replaces the hole with the colour and zdepth of the pixel to the left of it
                        zbuffer[x][y] = zbuffer[x-1][y];
                        zdepth[x][y] = zdepth[x-1][y];
                    }

				}

			}

		}

	}

}

// code for comp261 assignments
