package hayen.event;

/**
 * Created by Hayen on 16-02-05.</br>
 * Interface that any classes wishing to receive event from a <code>ColorWheel</code> have to implement
 * @see hayen.ui.ColorWheel
 * @see hayen.event.ColorChangeEvent
 */
public interface ColorChangeListener {
	/**
	 * Called when a <code>ColorChangeEvent</code> is fired
	 * @param e : the <code>ColorChangeEvent</code>
	 */
	public void colorChanged(ColorChangeEvent e);
}
