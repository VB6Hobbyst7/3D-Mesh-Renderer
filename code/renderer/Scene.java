package renderer;

import java.awt.Color;
import java.util.*;

/**
 * The Scene class is where we store data about a 3D model and light source
 * inside our renderer. It also contains a static inner class that represents one
 * single polygon.
 * 
 * Method stubs have been provided, but you'll need to fill them in.
 * 
 * If you were to implement more fancy rendering, e.g. Phong shading, you'd want
 * to store more information in this class.
 */
public class Scene {

	// Collections to store the polygons and lights
	private List<Polygon> polygonList;
	private Map<Vector3D, Color> lightList;

	/**
	 * Default constructor of a scene
	 * @param polygons
	 * @param lights
	 */
	public Scene(List<Polygon> polygons, Map<Vector3D, Color> lights) {
          // TODO fill this in.
		this.polygonList = polygons;
		this.lightList = lights;
	}

	/**
	 * Returns the lights
	 * @return
	 */
	public Map<Vector3D, Color> getLights() {
          // TODO fill this in.
          return lightList;
	}

	/**
	 * Returns the polygons
	 * @return
	 */
	public List<Polygon> getPolygons() {
          // TODO fill this in.
          return polygonList;
	}

	/**
	 * Clears the map of lights
	 */
	public void clearLight() {
		lightList.clear();
	}

	/**
	 * Uses a stack to find the inputted order of the lights, then removes the last one
	 */
	public void removeLastLight() {

		// creates the temporary stack to store the lights inside
		Stack<Vector3D> findLast = new Stack<>();
		for (Vector3D v : lightList.keySet())
			findLast.push(v);

		// findLast.pop() would be the last light inserted
		lightList.remove(findLast.pop());
	}

	/**
	 * Regardless of where the light is random or custom, this just puts the data
	 * into the Vector3D-Color map
	 * @param v
	 * @param c
	 */
	public void addLight(Vector3D v, Color c) {
		lightList.put(v, c);
	}

	/**
	 * Polygon stores data about a single polygon in a scene, keeping track of
	 * (at least!) its three vertices and its reflectance.
         *
         * This class has been done for you.
	 */
	public static class Polygon {
		Vector3D[] vertices;
		Color reflectance;

		/**
		 * @param points
		 *            An array of floats with 9 elements, corresponding to the
		 *            (x,y,z) coordinates of the three vertices that make up
		 *            this polygon. If the three vertices are A, B, C then the
		 *            array should be [A_x, A_y, A_z, B_x, B_y, B_z, C_x, C_y,
		 *            C_z].
		 * @param color
		 *            An array of three ints corresponding to the RGB values of
		 *            the polygon, i.e. [r, g, b] where all values are between 0
		 *            and 255.
		 */
		public Polygon(float[] points, int[] color) {
			this.vertices = new Vector3D[3];

			float x, y, z;
			for (int i = 0; i < 3; i++) {
				x = points[i * 3];
				y = points[i * 3 + 1];
				z = points[i * 3 + 2];
				this.vertices[i] = new Vector3D(x, y, z);
			}

			int r = color[0];
			int g = color[1];
			int b = color[2];
			this.reflectance = new Color(r, g, b);
		}

		/**
		 * An alternative constructor that directly takes three Vector3D objects
		 * and a Color object.
		 */
		public Polygon(Vector3D a, Vector3D b, Vector3D c, Color color) {
			this.vertices = new Vector3D[] { a, b, c };
			this.reflectance = color;
		}

		public Vector3D[] getVertices() {
			return vertices;
		}

		public Color getReflectance() {
			return reflectance;
		}

		@Override
		public String toString() {
			String str = "polygon:";

			for (Vector3D p : vertices)
				str += "\n  " + p.toString();

			str += "\n  " + reflectance.toString();

			return str;
		}
	}
}

// code for COMP261 assignments
