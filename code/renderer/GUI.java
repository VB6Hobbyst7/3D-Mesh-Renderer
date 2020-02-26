package renderer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.*;
import javax.swing.border.Border;

/**
 * A simple GUI, similar to the one in assignments 1 and 2, that you can base
 * your renderer off. It is abstract, and there are three methods you need to
 * implement: onLoad, onKeyPress, and render. There is a method to get the
 * ambient light level set by the sliders. You are free to use this class as-is,
 * modify it, or ignore it completely.
 * 
 * @author tony
 */
public abstract class GUI {

	/**
	 * Is called when the user has successfully selected a model file to load,
	 * and is passed a File representing that file.
	 */
	protected abstract void onLoad(File file);

	/**
	 * Is called every time the user presses a key. This can be used for moving
	 * the camera around. It is passed a KeyEvent object, whose methods of
	 * interest are getKeyChar() and getKeyCode().
	 */
	protected abstract void onKeyPress(KeyEvent ev);

	/**
	 * Is called every time the drawing canvas is drawn. This should return a
	 * BufferedImage that is your render of the scene.
	 */
	protected abstract BufferedImage render();

	/**
	 * Forces a redraw of the drawing canvas. This is called for you, so you
	 * don't need to call this unless you modify this GUI.
	 */
	public void redraw() {
		frame.repaint();
	}

	/**
	 * Returns the values of the three sliders used for setting the ambient
	 * light of the scene. The returned array in the form [R, G, B] where each
	 * value is between 0 and 255.
	 */
	public int[] getAmbientLight() {
		return new int[] { red.getValue(), green.getValue(), blue.getValue() };
	}

	// CUSTOM METHODS ADDED BY ME

	/**
	 * Returns the values of the custom light coordinates selected by the user
	 * Divides the number by 10 as a bar between -1 to 1 only returns ints, while I want float values
	 * @return
	 */
	public float[] getLightCoords() { return new float[] { xyzPos[0].getValue() / 10f, xyzPos[1].getValue() / 10f, xyzPos[2].getValue() / 10f }; }

	/**
	 * Returns the custom light used for highlighting the scene
	 * @return
	 */
	public int[] getCustomLight() {
		return new int[] { redCust.getValue(), greenCust.getValue(), blueCust.getValue() };
	}

	/**
	 * Removes all lights added
	 */
	protected abstract void removeAllLight();

	/**
	 * Removes the last light added
	 */
	protected abstract void removePrevLight();

	/**
	 * Adds a random light
	 */
	protected abstract void addRandLight();

	/**
	 * Adds a custom light
	 */
	protected abstract void addCustomLight();

	public static final int CANVAS_WIDTH = 600;
	public static final int CANVAS_HEIGHT = 600;

	// --------------------------------------------------------------------
	// Everything below here is Swing-related and, while it's worth
	// understanding, you don't need to look any further to finish the
	// assignment up to and including completion.
	// --------------------------------------------------------------------

	private JFrame frame;
	private final JSlider red = new JSlider(JSlider.HORIZONTAL, 0, 255, 128);
	private final JSlider green = new JSlider(JSlider.HORIZONTAL, 0, 255, 128);
	private final JSlider blue = new JSlider(JSlider.HORIZONTAL, 0, 255, 128);

	private static final Dimension DRAWING_SIZE = new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT);
	private static final Dimension CONTROLS_SIZE = new Dimension(150, 600);

	private static final Font FONT = new Font("Courier", Font.BOLD, 36);

	private final JSlider[] xyzPos = {new JSlider(JSlider.HORIZONTAL, -10, 10, 0),
									  new JSlider(JSlider.HORIZONTAL, -10, 10, 0),
									  new JSlider(JSlider.HORIZONTAL, -10, 10, 0)};

	private final JSlider redCust = new JSlider(JSlider.HORIZONTAL, 0, 255, 128);
	private final JSlider greenCust = new JSlider(JSlider.HORIZONTAL, 0, 255, 128);
	private final JSlider blueCust = new JSlider(JSlider.HORIZONTAL, 0, 255, 128);

	public GUI() {
		initialise();
	}

	@SuppressWarnings("serial")
	private void initialise() {
		// make the frame
		frame = new JFrame();
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.LINE_AXIS));
		frame.setSize(new Dimension(DRAWING_SIZE.width + CONTROLS_SIZE.width, DRAWING_SIZE.height));
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// set up the drawing canvas, hook it into the render() method, and give
		// it a nice default if render() returns null.
		JComponent drawing = new JComponent() {
			protected void paintComponent(Graphics g) {
				BufferedImage image = render();
				if (image == null) {
					g.setColor(Color.WHITE);
					g.fillRect(0, 0, DRAWING_SIZE.width, DRAWING_SIZE.height);
					g.setColor(Color.BLACK);
					g.setFont(FONT);
					g.drawString("IMAGE IS NULL", 50, DRAWING_SIZE.height - 50);
				} else {
					g.drawImage(image, 0, 0, null);
				}
			}
		};
		// fix its size
		drawing.setPreferredSize(DRAWING_SIZE);
		drawing.setMinimumSize(DRAWING_SIZE);
		drawing.setMaximumSize(DRAWING_SIZE);
		drawing.setVisible(true);

		// set up the load button
		final JFileChooser fileChooser = new JFileChooser();
		JButton load = new JButton("Load");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				// set up the file chooser
				fileChooser.setCurrentDirectory(new File("."));
				fileChooser.setDialogTitle("Select input file");
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

				// run the file chooser and check the user didn't hit cancel
				if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					onLoad(file);
					redraw();
				}
			}
		});
		// we have to put the button in its own panel to ensure it fills the
		// full width of the control bar.
		JPanel loadpanel = new JPanel(new BorderLayout());
		loadpanel.setMaximumSize(new Dimension(1000, 25));
		loadpanel.setPreferredSize(new Dimension(1000, 25));
		loadpanel.add(load, BorderLayout.CENTER);

		// set up the sliders for ambient light. they were instantiated in
		// the field definition, as for some reason they need to be final to
		// pull the set background trick.
		red.setBackground(new Color(230, 50, 50));
		green.setBackground(new Color(50, 230, 50));
		blue.setBackground(new Color(50, 50, 230));

		JPanel sliderparty = new JPanel();
		sliderparty.setLayout(new BoxLayout(sliderparty, BoxLayout.PAGE_AXIS));
		sliderparty.setBorder(BorderFactory.createTitledBorder("Ambient Light"));

		sliderparty.add(red);
		sliderparty.add(green);
		sliderparty.add(blue);

		// Remove All Light
		JButton remAllLight = new JButton("Remove All Lights");
		remAllLight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				removeAllLight();
				redraw();
			}
		});

		// Remove Light
		JButton removeLight = new JButton("Remove Previous");
		removeLight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				removePrevLight();
				redraw();
			}
		});

		// Random Light
		JButton randLight = new JButton("Add Random Light");
		randLight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				addRandLight();
				redraw();
			}
		});

		// Custom Light
		JButton custLight = new JButton("Add Custom Light");
		custLight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				addCustomLight();
				redraw();
			}
		});

		// a panel for all the light buttons to be stored in
		JPanel lightspanel = new JPanel();
		lightspanel.setMaximumSize(new Dimension(140, 100));
		lightspanel.setLayout(new GridLayout(4,1));
		lightspanel.add(remAllLight , BorderLayout.CENTER);
		lightspanel.add(removeLight , BorderLayout.CENTER);
		lightspanel.add(randLight, BorderLayout.CENTER);
		lightspanel.add(custLight, BorderLayout.CENTER);

		// a panel for all the sliders for custom lights to be stored in
		JPanel positions = new JPanel();
		positions.setLayout(new BoxLayout(positions, BoxLayout.PAGE_AXIS));
		positions.setBorder(BorderFactory.createTitledBorder("    Custom Light Pos   "));

		// uses a for loop since all the sliders are set up the same
		for (int i = 0; i < xyzPos.length; i++) {
			if (i == 0) positions.add(new JLabel("X Value:"));
			else if (i == 1) positions.add(new JLabel("Y Value:"));
			else if (i == 2) positions.add(new JLabel("Z Value:"));

			xyzPos[i].setPaintLabels(true);
			xyzPos[i].setPaintTicks(true);
			xyzPos[i].setMinorTickSpacing(1);
			xyzPos[i].setMajorTickSpacing(5);
			positions.add(xyzPos[i]);
		}


		// does the same trick here, as with the ambient lights
		redCust.setBackground(new Color(230, 50, 50));
		greenCust.setBackground(new Color(50, 230, 50));
		blueCust.setBackground(new Color(50, 50, 230));

		// a panel for adjusting the custom light colours
		JPanel customcolors = new JPanel();
		customcolors.setLayout(new BoxLayout(customcolors, BoxLayout.PAGE_AXIS));
		customcolors.setBorder(BorderFactory.createTitledBorder("  Custom Light Color  "));

		customcolors.add(redCust);
		customcolors.add(greenCust);
		customcolors.add(blueCust);

		// this is not a best-practices way of doing key listening; instead you
		// should use either a KeyListener or an InputMap/ActionMap combo. but
		// this method neatly avoids any focus issues (KeyListener) and requires
		// less effort on your part (ActionMap).
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent ev) {
				if (ev.getID() == KeyEvent.KEY_PRESSED) {
					onKeyPress(ev);
					redraw();
				}
				return true;
			}
		});

		// make the panel on the right, fix its size, give it a border!
		JPanel controls = new JPanel();
		controls.setPreferredSize(CONTROLS_SIZE);
		controls.setMinimumSize(CONTROLS_SIZE);
		controls.setMaximumSize(CONTROLS_SIZE);
		controls.setLayout(new BoxLayout(controls, BoxLayout.PAGE_AXIS));
		Border edge = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		controls.setBorder(edge);

		controls.add(loadpanel);
		controls.add(Box.createRigidArea(new Dimension(0, 15)));
		controls.add(sliderparty);
		// if i were going to add more GUI components, i'd do it here.
		controls.add(Box.createRigidArea(new Dimension(0, 30)));
		controls.add(lightspanel);
		controls.add(Box.createRigidArea(new Dimension(0, 15)));
		controls.add(positions);
		controls.add(Box.createRigidArea(new Dimension(0, 15)));
		controls.add(customcolors);
		controls.add(Box.createVerticalGlue());

		// put it all together.
		frame.add(drawing);
		frame.add(controls);

		frame.pack();
		frame.setVisible(true);
	}
}

// code for comp261 assignments
