package hayen.event;

import java.awt.*;

/**
 * Created by Hayen on 16-02-05.
 * Event launched by a <code>ColorWheel</code> when it's selected color change
 * @see hayen.ui.ColorWheel
 * @see hayen.event.ColorChangeListener
 */
public class ColorChangeEvent extends AWTEvent{
	public final Color color;
	public ColorChangeEvent(Component source, Color c){
		super(source, AWTEvent.RESERVED_ID_MAX + 3);
		color = c;
	}
}
