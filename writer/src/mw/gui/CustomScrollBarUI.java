/*
The MIT License (MIT)

Copyright (c) 2014 Warren S

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package mw.gui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

//Based off of: http://stackoverflow.com/a/8209911
class CustomScrollBarUI extends BasicScrollBarUI
{
	protected JButton createDecreaseButton(int orientation)
	{
		return new BasicArrowButton(orientation,
				GUI.clr,
				Color.black,
				Color.black,
				Color.white);
	}

	protected JButton createIncreaseButton(int orientation)
	{
		return new BasicArrowButton(orientation,
				GUI.clr,
				Color.black,
				Color.black,
				Color.white);
	}

	protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds)
	{
		g.setColor( Color.black );
		g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
	}

	protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds)
	{
		if(thumbBounds.isEmpty() || !scrollbar.isEnabled())
			return;

		g.setColor( GUI.clr );
		g.fillRect( thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height );
	}
}
