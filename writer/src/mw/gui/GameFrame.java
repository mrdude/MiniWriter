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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class GameFrame extends JFrame
{
	protected static final Cursor blankCursor;
	static {
		BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(0,0), "blank cursor");
	}

	private volatile boolean closeFlag;
	private volatile boolean escHit;

	public GameFrame()
	{
		addWindowListener( new WindowAdapter()
		{
			public void windowClosing(WindowEvent e) { closeFlag = true; }

			public void windowClosed(WindowEvent e) { closeFlag = true; }
		} );

		getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke( "ESCAPE" ), "esc" );
		getRootPane().getActionMap().put( "esc", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				escHit = true;
			}
		} );
	}

	public boolean closeRequested() { return closeFlag; }

	public boolean escHit() { return escHit; }

	/** Hides the window cursor */
	public void hideCursor() { setCursor( blankCursor ); }

	/** Shows the window cursor */
	public void showCursor() { setCursor( Cursor.getDefaultCursor() ); }
}