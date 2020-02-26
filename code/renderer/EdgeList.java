package renderer;

/**
 * EdgeList should store the data for the edge list of a single polygon in your
 * scene. A few method stubs have been provided so that it can be tested, but
 * you'll need to fill in all the details.
 *
 * You'll probably want to add some setters as well as getters or, for example,
 * an addRow(y, xLeft, xRight, zLeft, zRight) method.
 */
public class EdgeList {

	private int startY, endY;	 // the ints for the start and end points of the EdgeList
	private float[][] edgeList;	 // the EdgeList saved as a float array
	private int linearDist;		 // the size of the float array above

	/**
	 * Constructor for the EdgeList of a Polygon
	 *
	 * @param startY start position
	 * @param endY end position
	 */
	public EdgeList(int startY, int endY) {
		// TODO fill this in.

		this.startY = startY;
		this.endY = endY;
		linearDist = endY-startY+1; // Got an outOfBoundsException, added in this +1

		// 0 is xLeft, 1 is zLeft, 2 is xRight, 3 is zRight
		edgeList = new float[4][linearDist];

	}

	/**
	 * Returns the startY
	 */
	public int getStartY() {
		// TODO fill this in.
		return this.startY;
	}

	/**
	 * Returns the endY
	 */
	public int getEndY() {
		// TODO fill this in.
		return this.endY;
	}

	/**
	 * Returns the leftX
	 */
	public float getLeftX(int y) {
		// TODO fill this in.
		return edgeList[0][y-startY];
	}

	/**
	 * Returns the rightX
	 */
	public float getRightX(int y) {
		// TODO fill this in.
		return edgeList[2][y-startY];
	}

	/**
	 * Returns the leftZ
	 */
	public float getLeftZ(int y) {
		// TODO fill this in.
		return edgeList[1][y-startY];
	}

	/**
	 * Returns the rightZ
	 */
	public float getRightZ(int y) {
		// TODO fill this in.
		return edgeList[3][y-startY];
	}

	/**
	 * Sets the left elements in the float array
	 *
	 * @param y
	 * 		The index in the float array
	 * @param xL
	 * 		The value to be inserted in the xLeft position
	 * @param zL
	 * 		The value to be inserted in the zLeft position
	 */
	public void setLefts(int y, float xL, float zL) {
		// TODO fill this in.
		// just in case the y position is out of bounds
		if (y-startY < 0 || y-startY >= linearDist) return;

		edgeList[0][y-startY] = xL;
		edgeList[1][y-startY] = zL;
	}

	/**
	 * Sets the right elements in the float array
	 *
	 * @param y
	 * 		The index in the float array
	 * @param xR
	 * 		The value to be inserted in the xRight position
	 * @param zR
	 * 		The value to be inserted in the zRight position
	 */
	public void setRights(int y, float xR, float zR) {
		// TODO fill this in.
		// just in case the y position is out of bounds
		if (y-startY < 0 || y-startY >= linearDist) return;

		edgeList[2][y-startY] = xR;
		edgeList[3][y-startY] = zR;
	}

}

// code for comp261 assignments
