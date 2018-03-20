package hayen.ui;

import hayen.event.ColorChangeEvent;
import hayen.event.ColorChangeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Hayen on 16-02-01.</br>
 * Color selector component, allowing the user to choose the hue and saturation on a chromatic circle and the value and alpha are selected using two slider on the side of the circle
 * @see hayen.event.ColorChangeListener
 * @see hayen.event.ColorChangeEvent
 */
public class ColorWheel extends JComponent{

	private static final long serialVersionUI = 58478378884L;

	private static final int[] mask = {	0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000};
	private static final int shift_red = 16;
	private static final int shift_green = 8;
	private static final int shift_blue = 0;
	private static final int shift_alpha = 24;
	private static final int NOTHING_SELECTED = 0;
	private static final int COLOR_SELECTED = 1;
	private static final int VALUE_SELECTED = 2;
	private static final int ALPHA_SELECTED = 3;
	private static final int BORDER = 4;

	private float hue, sat, b;
	private int a;
	private int cursorRadius = 5, cursorSliderRadius = 10;
	private int wheelRadius, widthSlider = 10, borderSize = 1, radius;
	private double theta, angleA, angleB, delta;
	private BufferedImage wheel;
	private Integer bufferedColor = null;
	private Set<ColorChangeListener> listeners;

	/**
	 * Creates a new ColorWheel
	 */
	public ColorWheel(){
		hue = 0;
		sat = 0;
		b = 1;
		a = 255;
		MouseAdapter listener = new WheelMouseListener(this);
		addMouseListener(listener);
		addMouseMotionListener(listener);
		listeners = new LinkedHashSet<ColorChangeListener>();
		setSliderAngle(Math.toRadians(15));
	}

	/**
	 * Set the <em>value</em> parameter of the color on the color wheel to a new value.</br>
	 * The <em>value</em> parameter represent the brightness of the color
	 * @param value : the new value
	 */
	public void setValue(float value){
		b = clamp(0, value, 1);
		wheel = null;
		bufferedColor = null;
		fireColorChangeEvent();
	}
	/**
	 * Set the <em>value</em> parameter of the color on the color wheel to a new value.</br>
	 * The <em>value</em> parameter represent the brightness of the color
	 * @param value : the new value
	 */
	public void setValue(double value){ setValue((float)value); }
	/**
	 * Set the <em>alpha</em> parameter of the color to a new value.</br>
	 * The <em>alpha</em> parameter represent the transparency of the color.
	 * @param alpha : the new alpha value
	 */
	public void setAlpha(int alpha){
		a = clamp(0, alpha, 255);
		bufferedColor = null;
		fireColorChangeEvent();
	}
	/**
	 * Allow to set the <em>red</em> value of the color to a new value
	 * @param red : the new <em>red</em> value [0, 255]
	 */
	public void setRed(int red){
		int rgb[] = new int[3]; HSBtoRGB(hue, sat, b, rgb);
		rgb[0] = clamp(0, red, 255);
		float[] hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], null);
		hue = hsb[0]; sat = hsb[1]; b = hsb[2];
		bufferedColor = null;
		fireColorChangeEvent();
	}
	/**
	 * Allow to set the <em>green</em> value of the color to a new value
	 * @param green : the new <em>green</em> value [0, 255]
	 */
	public void setGreen(int green){
		int rgb[] = new int[3]; HSBtoRGB(hue, sat, b, rgb);
		rgb[1] = clamp(0, green, 255);
		float[] hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], null);
		hue = hsb[0]; sat = hsb[1]; b = hsb[2];
		bufferedColor = null;
		fireColorChangeEvent();
	}
	/**
	 * Allow to set the <em>blue</em> value of the color to a new value
	 * @param blue : the new <em>blue</em> value [0, 255]
	 */
	public void setBlue(int blue){
		int rgb[] = new int[3]; HSBtoRGB(hue, sat, b, rgb);
		rgb[2] = clamp(0, blue, 255);
		float[] hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], null);
		hue = hsb[0]; sat = hsb[1]; b = hsb[2];
		bufferedColor = null;
		fireColorChangeEvent();
	}
	/**
	 * Set the <em>hue</em> parameter of the color on the color wheel to a new value.</br>
	 * The <em>hue</em> parameter represent a pure color on the spectrum
	 * @param h : the new hue of the color
	 */
	public void setHue(double h){ setHue((float)h); }
	/**
	 * Set the <em>hue</em> parameter of the color on the color wheel to a new value.</br>
	 * The <em>hue</em> parameter represent a pure color on the spectrum
	 * @param h : the new hue of the color
	 */
	public void setHue(float h){
		bufferedColor = null;
		hue = clamp(0, h, 1);
		fireColorChangeEvent();
	}
	/**
	 * Set the <em>saturation</em> parameter of the color on the color wheel to a new value.</br>
	 * The <em>saturation</em> of a color is equivalent to it's <em>chroma</em>, but it is scaled to always fit between 0 and 1.</br>
	 * It represent the "pureness" of the color
	 * @param saturation : the new saturation of the color
	 */
	public void setSaturation(float saturation){
		bufferedColor = null;
		sat = clamp(0, saturation, 1);
		fireColorChangeEvent();
	}
	/**
	 * Set the <em>saturation</em> parameter of the color on the color wheel to a new value.</br>
	 * The <em>saturation</em> of a color is equivalent to it's <em>chroma</em>, but it is scaled to always fit between 0 and 1.</br>
	 * It represent the "pureness" of the color
	 * @param saturation : the new saturation of the color
	 */
	public void setSaturation(double saturation){ setSaturation((float)saturation); }
	/**
	 * Change the color selected by the wheel
	 * @param red : the new <em>red</em> value [0, 255]
	 * @param green : the new <em>green</em> value [0, 255]
	 * @param blue : the new <em>blue</em> value [0, 255]
	 * @param alpha : the new alpha value
	 */
	public void setColor(int red, int green, int blue, int alpha){
		int rgb[] = new int[3]; HSBtoRGB(hue, sat, b, rgb);
		rgb[0] = clamp(0, red, 255);
		rgb[1] = clamp(0, green, 255);
		rgb[2] = clamp(0, blue, 255);
		a = clamp(0, alpha, 255);
		float[] hsb = Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], null);
		hue = hsb[0]; sat = hsb[1]; b = hsb[2];
		bufferedColor = null;
		fireColorChangeEvent();
	}
	public void setColor(int red, int green, int blue) { setColor(red, green, blue, a); }
	public void setColor(Color color){ setColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()); }
	public void setColor(float hue, float saturation, float value, int alpha){
		this.hue = clamp(0, hue, 1);
		this.sat = clamp(0, saturation, 1);
		this.b = clamp(0, value, 1);
		this.a = clamp(0, alpha, 255);
		bufferedColor = null;
		fireColorChangeEvent();
	}
	public void setColor(float hue, float saturation, float value){ setColor(hue, saturation, value, a); }
	/**
	 * Allow to change the angle between the vertical axis of the circle and the extremities of the <em>alpha</em> and <em>value</em> sliders.
	 * @param theta : the new angle
	 */
	public void setSliderAngle(double theta){
		this.theta = theta;
		angleA = Math.PI/2 + theta;
		angleB = Math.PI/2 - theta;
		delta = 2*(Math.PI - angleA);
		wheel = null;
	}
	/**
	 * Allow to change the size (in pixel) of the border of the circle
	 * @param size : the new size of the border
	 */
	public void setBorderSize(int size){
		updateRadius(getWidth(), widthSlider, size);
		wheel = null;
	}
	/**
	 * Allow to change the width of the <em>alpha</em> and <em>value</em> sliders
	 * @param width : the new width of the sliders
	 */
	public void setSliderWidth(int width){
		widthSlider = width;
		wheel = null;
	}

	/**
	 * Return the color identified by the wheel in the HSB format
	 * @return a float array containing the hue, saturation and value
	 */
	public float[] getColorHSV(){
		return new float[]{hue, sat, b};
	}
	/**
	 * Return a <code>Java.awt.Color</code> object representing the color selected on this <code>ColorWheel</code>
	 * @return the color Selected on this <code>ColorWheel</code>
	 */
	public Color getColor(){
		if (bufferedColor == null)
			bufferedColor = generateBufferedColor(hue, sat, b, a);
		return new Color(bufferedColor, true);
	}
	/**
	 * Return the selected color in the hex RGB format.</br>
	 * bits 1-8 is blue, 7-16 is green, 17-24 is red and 25-32 is alpha
	 * @return the selected color in the hex format
	 */
	public int getColorRGB(){
		if (bufferedColor == null)
			bufferedColor = generateBufferedColor(hue, sat, b, a);
		return bufferedColor;
	}
	/**
	 * Return the red value of the selected color
	 * @return the red value ([0, 255])
	 */
	public int getRed(){ return (bufferedColor & mask[0]) >>> shift_red; }
	/**
	 * Return the green value of the selected color
	 * @return the green value ([0, 255])
	 */
	public int getGreen(){ return (bufferedColor & mask[1]) >>> shift_green; }
	/**
	 * Return the blue value of the selected color
	 * @return the blue value ([0, 255])
	 */
	public int getBlue(){ return (bufferedColor & mask[2]) >>> shift_blue; }
	/**
	 * Return the alpha value of the selected color
	 * @return the alpha value ([0, 255])
	 */
	public int getAlpha(){ return (bufferedColor & mask[3]) >>> shift_alpha; }
	/**
	 * Return the <em>hue</em> of the selected color
	 * @return the <em>hue</em> ([0, 1])
	 */
	public double getHue(){ return hue; }
	/**
	 * Return the <em>saturation</em> of the selected color
	 * @return the <em>saturation</em> ([0, 1])
	 */
	public double getSaturation(){ return sat; }
	/**
	 * Return the <em>value</em> of the selected color
	 * @return the <em>value</em> ([0, 1])
	 */
	public double getValue(){ return b; }
	/**
	 * Return the interior angle formed by the vertical axis of the circle and the extremities of the <em>alpha</em> and the <em>value</em> sliders.
	 * @return The interior angle formed by the vertical axis and the sliders
	 */
	public double getSliderAngle(){ return theta; }
	/**
	 * Return the size, or width, of the border of the wheel
	 * @return the size of the border of the wheel
	 */
	public double getBorderSize(){ return borderSize; }

	@Override
	public void paint(Graphics g){
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (wheel == null) {
			updateRadius(Math.min(getWidth(), getHeight()), widthSlider, borderSize);
			wheel = generateWheel();
		}

		g2.drawImage(wheel, null, this);

		int sliderRad = wheelRadius + borderSize + widthSlider/2;
		double[] xyColor = getCoordinate(hue, sat, wheelRadius);
		double[] xyValue = getCoordinate(computeAngleFromAlpha(b, delta), sliderRad); xyValue[0] *= -1;
		double[] xyAlpha = getCoordinate(computeAngleFromAlpha(a/255d, delta), sliderRad);

		g2.setColor(Color.lightGray);
		g2.fillOval((int)((xyAlpha[0] + radius ) - cursorSliderRadius), (int)((xyAlpha[1] + radius ) - cursorSliderRadius), cursorSliderRadius*2, cursorSliderRadius*2);
		g2.fillOval((int)((xyValue[0] + radius ) - cursorSliderRadius), (int)((xyValue[1] + radius ) - cursorSliderRadius), cursorSliderRadius*2, cursorSliderRadius*2);
		g2.setColor(Color.gray);
		g2.fillOval((int)((xyAlpha[0] + radius ) - cursorSliderRadius/2), (int)((xyAlpha[1] + radius ) - cursorSliderRadius/2), cursorSliderRadius, cursorSliderRadius);
		g2.fillOval((int)((xyValue[0] + radius ) - cursorSliderRadius/2), (int)((xyValue[1] + radius ) - cursorSliderRadius/2), cursorSliderRadius, cursorSliderRadius);
		g2.setColor(getCursorColor(b));
		g2.drawOval((int)((xyAlpha[0] + radius ) - cursorSliderRadius), (int)((xyAlpha[1] + radius ) - cursorSliderRadius), cursorSliderRadius*2, cursorSliderRadius*2);
		g2.drawOval((int)((xyValue[0] + radius ) - cursorSliderRadius), (int)((xyValue[1] + radius ) - cursorSliderRadius), cursorSliderRadius*2, cursorSliderRadius*2);
		g2.drawOval((int)((xyColor[0] + radius ) - cursorRadius), (int)((xyColor[1] + radius ) - cursorRadius), cursorRadius*2, cursorRadius*2);
	}
	@Override
	public void setSize(Dimension s){
		int diam = (int)Math.min(s.getHeight(), s.getWidth());
		updateRadius(diam, widthSlider, borderSize);
		super.setSize(s);
		wheel = null;
	}
	@Override
	public void setSize(int w, int h){
		int diam = Math.min(w, h);
		updateRadius(diam, widthSlider, borderSize);
		super.setSize(w, h);
		wheel = generateWheel();
	}
	@Override
	public void setBounds(Rectangle r){
		int diam = (int)Math.min(r.getHeight(), r.getWidth());
		updateRadius(diam, widthSlider, borderSize);
		super.setBounds(r);
		wheel = generateWheel();
	}
	@Override
	public void setBounds(int x, int y, int w, int h){
		int diam = Math.min(w, h);
		updateRadius(diam, widthSlider, borderSize);
		super.setBounds(x, y, w, h);
		wheel = generateWheel();
	}

	/**
	 * Send a <code>ColorChangeEvent</code> to all registered listeners.</br>
	 * A <code>ColorChangeEvent</code> is fired when the color selected on this <code>ColorWheel</code> changes
	 */
	private void fireColorChangeEvent(){
		ColorChangeEvent e = new ColorChangeEvent(this, getColor());
		Iterator<ColorChangeListener> it = listeners.iterator();
		while(it.hasNext())
			it.next().colorChanged(e);
	}
	/**
	 * Register a <code>ColorChangeListener</code> to this <code>ColorWheel</code></br>
	 * A <code>ColorChangeEvent</code> is fired every time the color selected by this <code>ColorWheel</code> changes
	 * @param l : the <code>ColorChangeListener</code> to register
	 */
	public void addColorChangeListener(ColorChangeListener l){
		listeners.add(l);
	}
	/**
	 * Remove the desired <code>ColorChangeListener</code> so that it will not receive <code>ColorChangeEvent</code> fired by this <code>ColorWheel</code>
	 * @param l
	 */
	public void removeColorChangeListener(ColorChangeListener l){
		listeners.remove(l);
	}

	/**
	 * Compute the color in the rgb hex format
	 * @param hue : the <em>hue</em> of the color
	 * @param sat : the <em>saturation</em> of the color
	 * @param val : the <em>value</em> of the color
	 * @param a : the transparency of the color
	 * @return the color int the rgb hex format
	 */
	private static int generateBufferedColor(double hue, double sat, double val, int a){
		int rgb = Color.HSBtoRGB((float)hue, (float)sat, (float)val) & ~mask[3];
		return (a << shift_alpha) + rgb;
	}
	/**
	 * Generate the <code>BufferedImage</code> representing the wheel
	 * @return the wheel!
	 */
	private BufferedImage generateWheel(){
			if (wheel != null)
				wheel.flush();

			int width = Math.min(getWidth(), getHeight());
			int Radius = width/2;
			wheel = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);

			int[] selections = {0, 0, 0, 0, 0};

			int x, y, nx, ny;
			int sqrMagnitude;
			double magnitude, alpha;
			for (x = 0; x < width; x++) {
				for (y = 0; y < width; y++) {
					nx = x - Radius;
					ny = Radius - y;
					sqrMagnitude = nx*nx + ny*ny;
					magnitude = Math.sqrt(sqrMagnitude);
					alpha = Math.atan2(ny, nx);

					int sel = getSelection(sqrMagnitude, wheelRadius, widthSlider, 1, angleA, angleB, alpha);
					selections[sel]++;

					switch (sel){
						case COLOR_SELECTED: // color
							double hue = alpha / Math.PI / 2;
							double saturation = magnitude / wheelRadius;
							wheel.setRGB(x, y, Color.HSBtoRGB((float) hue, (float) saturation, b));
							break;
						case VALUE_SELECTED: // brightness
							int n = (int)(255*computeValue(alpha, angleA, delta))%255;
							wheel.setRGB(x, y, new Color(n, n, n).getRGB());
							break;
						case ALPHA_SELECTED: // alpha
							wheel.setRGB(x, y, new Color(0, 0, 0, (int) (computeAlpha(alpha, angleB, delta) * 255)).getRGB());
							break;
						case BORDER: // border of the wheel
							wheel.setRGB(x, y, 0xFF303030);
							break;
						default: // nothing
							wheel.setRGB(x, y, 0x00000000);
							break;
					}
				}
			}

		return wheel;
	}

	/**
	 * Update the radius of the chromatic circle to accomodate the other thingies
	 * @param width : the width of the component (visible part, aka smallest dimension)
	 * @param slider : the width of the sliders
	 * @param border : the width of the border
	 */
	private void updateRadius(int width, int slider, int border){
		radius = width/2;
		widthSlider = slider;
		borderSize = border;
		wheelRadius = width/2 - slider - border*2;
	}

	/**
	 * Private listener that takes care of the mouse event for this component
	 */
	private static class WheelMouseListener extends MouseAdapter{

		private int selection = NOTHING_SELECTED;
		private ColorWheel wheel;
		private WheelMouseListener(ColorWheel w){ wheel = w; }

		@Override
		public void mousePressed(MouseEvent e){
			int nx = e.getX() - (wheel.radius);
			int ny = wheel.radius - e.getY();
			int sqrMagnitude = nx*nx + ny*ny;
			double alpha = Math.atan2(ny, nx);
			selection = getSelection(sqrMagnitude, wheel.wheelRadius, wheel.widthSlider, wheel.borderSize, wheel.angleA, wheel.angleB, alpha);
			selection = selection == BORDER ? NOTHING_SELECTED : selection;
			changeColor(sqrMagnitude, alpha);
		}
		@Override
		public void mouseReleased(MouseEvent e){
			selection = NOTHING_SELECTED;
		}
		@Override
		public void mouseDragged(MouseEvent e){
			int nx = e.getX() - wheel.radius;
			int ny = wheel.radius - e.getY();
			changeColor(nx*nx + ny*ny, Math.atan2(ny, nx));
		}
		private void changeColor(int sqrMagnitude,double alpha){
			switch (selection){
				case COLOR_SELECTED:
					wheel.hue = (float)(alpha / Math.PI / 2);
					wheel.sat = (float)(Math.sqrt(sqrMagnitude) / wheel.wheelRadius);
					if (wheel.sat > 1)
						wheel.sat = 1;
					wheel.bufferedColor = null;
					wheel.fireColorChangeEvent();
					break;
				case ALPHA_SELECTED:
					wheel.setAlpha((int)(255*computeAlpha(alpha, wheel.angleB, wheel.delta)));
					break;
				case VALUE_SELECTED:
					wheel.setValue((float)computeValue(alpha, wheel.angleA, wheel.delta));
				default:
					break;
			}
			if (selection != NOTHING_SELECTED)
				wheel.repaint();
		}
	}
	/**
	 * Compute and return the xy coordinates, centered on the center of the wheel, of the color cursor
	 * @param hue : the <em>hue</em> of the selected color
	 * @param saturation : the <em>saturation</em> of the selected color
	 * @param rad : the radius of the wheel
	 * @return the cartesian coordinate of the color cursor
	 */
	private static double[] getCoordinate(float hue, float saturation, int rad){
		double[] coords = new double[2];
		coords[0] = Math.cos(2*Math.PI*hue)*saturation*rad;
		coords[1] = -Math.sin(2*Math.PI*hue)*saturation*rad;
		return coords;
	}
	/**
	 * compute the cartesian coordinate equivalent of a polar coordinate
	 * @param angle : the angle of the coordinate
	 * @param rad : the distance relative to the center of the coordinate
	 * @return the cartesian coordinate
	 */
	private static double[] getCoordinate(double angle, int rad){
		return new double[]{Math.cos(angle)*rad, -Math.sin(angle)*rad};
	}
	/**
	 * Compute and return what part of the wheel a point is in
	 * @param sqrMagnitude : the square magnitude of the position vector of the point
	 * @param wheelRad : the radius of the chromatic circle
	 * @param widthSlider : the width of the slider
	 * @param borderWidth : the width of the border
	 * @param thetaA : the angle at which the <em>value</em> slider start
	 * @param thetaB : the angle at which the <em>alpha</em> slider start
	 * @param alpha : the angle relative to the positive x axis of the point's vector
	 * @return the selected zone
	 */
	private static int getSelection(int sqrMagnitude, int wheelRad, int widthSlider, int borderWidth, double thetaA, double thetaB, double alpha){
		int rad = wheelRad;
		alpha = Math.abs(alpha);

		if (sqrMagnitude <= rad*rad) // color
			return COLOR_SELECTED;
		rad += borderWidth;
		if (sqrMagnitude <= rad*rad)
			return BORDER;
		rad += widthSlider;
		if (sqrMagnitude <= rad*rad) {
			if (alpha >= thetaA)
				return VALUE_SELECTED;
			if (alpha <= thetaB)
				return ALPHA_SELECTED;
			return NOTHING_SELECTED;
		}
		rad += borderWidth;
		if (sqrMagnitude <= rad*rad && (alpha >= thetaA || alpha <= thetaB))
			return BORDER;
		return NOTHING_SELECTED;
	}
	/**
	 * Compute the <em>value</em> selected on the slider
	 * @param alpha : the angle of the cursor
	 * @param theta : the angle at which the <em>alpha</em> slider starts
	 * @param delta : the angle that the slider occupies
	 * @return the <em>value</em>
	 */
	private static double computeValue(double alpha, double theta, double delta){
		if (alpha > 0 && alpha <= theta)
			return 1;
		if (alpha < 0 && alpha >= -theta)
			return 0;
		return (alpha <= 0 ? Math.abs(alpha + theta) : Math.PI*2 - Math.abs(alpha + theta)) / delta;
	}
	/**
	 * Compute the <em>alpha</em> selected on the slider
	 * @param alpha : the angle of the cursor
	 * @param theta : the angle at which the <em>alpha</em> slider starts
	 * @param delta : the angle that the slider occupies
	 * @return the <em>alpha</em>
	 */
	private static double computeAlpha(double alpha, double theta, double delta){ return (alpha + theta)/delta; }
	/**
	 * Inverse of <code>computeAlpha</code>.</br>
	 * Compute the angle of the cursor based on the <em>alpha</em> parameter
	 * @param alpha : the <em>alpha</em> parameter
	 * @param delta : the "width" of the slider, in radians
	 * @return the angle, in radians, of the cursor
	 */
	private static double computeAngleFromAlpha(double alpha, double delta){ return (alpha-0.5)*delta; }
	/**
	 * Return the color the color cursor should have to be visible, based on the <em>value</em>
	 * @param b : the <em>value</em> of the selected color
	 * @return the color the color cursor should use
	 */
	private static Color getCursorColor(float b){ return b < 0.5 ? Color.white : Color.black; }
	/**
	 * Clamp <code>val</code> between <code>min</code> and <code>max</code>
	 * @param min : the minimal value
	 * @param val : the value to clamp
	 * @param max : the maximal value
	 * @return the clamped value
	 */
	private static int clamp(int min, int val, int max){
		if (val < min)
			return min;
		if (val > max)
			return max;
		return val;
	}
	/**
	 * Clamp <code>val</code> between <code>min</code> and <code>max</code>
	 * @param min : the minimal value
	 * @param val : the value to clamp
	 * @param max : the maximal value
	 * @return the clamped value
	 */
	private static float clamp(float min, float val, float max){
		if (val < min)
			return min;
		if (val > max)
			return max;
		return val;
	}
	/**
	 * Return the rgb equivalent of a hsb color
	 * @param hue : the <em>hue</em> of the color
	 * @param saturation : the <em>saturation</em> of the color
	 * @param value : the <em>value</em> of the color
	 * @param rgb : a int array to store the rgb value
	 * @return a hex representation of the rgb color
	 */
	public static int HSBtoRGB(double hue, double saturation, double value, int[] rgb){
		double C = saturation * value;
		double Hprime = hue*6;
		double X = C*(1 - Math.abs(Hprime%2 - 1));
		double m = value - C;

		double r1, g1, b1;
		{
			if 		(0 <= Hprime && Hprime < 1) { r1 = C; g1 = X; b1 = 0; }
			else if (1 <= Hprime && Hprime < 2) { r1 = X; g1 = C; b1 = 0; }
			else if (2 <= Hprime && Hprime < 3) { r1 = 0; g1 = C; b1 = X; }
			else if (3 <= Hprime && Hprime < 4) { r1 = 0; g1 = X; b1 = C; }
			else if (4 <= Hprime && Hprime < 5) { r1 = X; g1 = 0; b1 = C; }
			else if (5 <= Hprime && Hprime < 6) { r1 = C; g1 = 0; b1 = X; }
			else { r1 = 0; g1 = 0; b1 = 0; }
		}

		int r = (int)((r1 + m)*255);
		int g = (int)((g1 + m)*255);
		int b = (int)((b1 + m)*255);
		if (rgb != null && rgb.length >= 3){
			rgb[0] = r;
			rgb[1] = g;
			rgb[2] = b;
		}
		int hex = 0xFF000000 + (r << 16) + (g << 8) + b;
		return hex;
	}
}
