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
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class GUI
{
	static Color clr = new Color( 122, 114, 0 );

	private static final long autosave_time = TimeUnit.MINUTES.toNanos(5); //if you pause between typing for at least this long after making changes, the document will autosave

	GameFrame win;
	JTextPane ta;
	JPanel infoPanel;
	File saveFile;
	JLabel lbl_wordCount;
	JLabel lbl_lastSave;
	volatile long nsLastModifiedDocument; //the value of System.nanoTime when you last modified the document
	volatile boolean dirtyFile; //have changes been made since the last save?
	volatile boolean lastSaveAutoSave; //if this is true, then the last save was an autosave
	boolean wordCountTextSelected; //the last time we ran word count, was text selected?

	public GUI(String filename)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		Dimension textAreaSize = new Dimension();
		textAreaSize.width = (int)(screenSize.width * .65);
		textAreaSize.height = screenSize.height;

		Dimension topSpacerSize = new Dimension();
		topSpacerSize.width = screenSize.width;
		topSpacerSize.height = 32;

		Dimension sideSpacerSize = new Dimension();
		sideSpacerSize.width = (screenSize.width - textAreaSize.width) / 2;
		sideSpacerSize.height = screenSize.height;

		saveFile = new File(filename);

		ta = createTextArea();
		ta.setPreferredSize( textAreaSize );
		infoPanel = createInfoPanel(screenSize.width);

		lbl_wordCount = new JLabel("0 words");
		lbl_lastSave = new JLabel();
		updateLastSave();

		{
			infoPanel.add( new JLabel( "Esc to quit, CTRL+S to save" ) );
			infoPanel.add( new JLabel( " | " ) );
			infoPanel.add( lbl_wordCount );
			infoPanel.add( new JLabel( " | " ) );
			infoPanel.add( new JLabel( "\"" + saveFile.getName() + "\"" ) );
			infoPanel.add( lbl_lastSave );
		}

		JScrollPane scrollPane = new JScrollPane( ta );
		scrollPane.setBorder( null );
		scrollPane.getVerticalScrollBar().setUI( new CustomScrollBarUI() );

		win = new GameFrame();
		win.setLayout( new BorderLayout() );

		win.add( createSpacerPanel( topSpacerSize ), BorderLayout.PAGE_START );
		win.add( createSpacerPanel( sideSpacerSize ), BorderLayout.LINE_START );
		win.add( createSpacerPanel( sideSpacerSize ), BorderLayout.LINE_END );
		win.add( scrollPane, BorderLayout.CENTER );
		win.add( infoPanel, BorderLayout.PAGE_END );

		win.setTitle("MiniWriter");
		win.setVisible(true);
		win.pack();

		win.getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke( "ctrl S" ), "save" );
		win.getRootPane().getActionMap().put( "save", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveFile(false);
			}
		} );

		GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		dev.setFullScreenWindow( win );

		if( saveFile.isFile() )
			loadFile();

		ta.getDocument().addDocumentListener( new DocumentListener() {
			public void insertUpdate(DocumentEvent e) { handle(); }
			public void removeUpdate(DocumentEvent e) { handle(); }
			public void changedUpdate(DocumentEvent e) { handle(); }

			private void handle()
			{
				updateWordCount();
				dirtyFile = true;
				nsLastModifiedDocument = System.nanoTime();
			}
		} );

		ta.addCaretListener( new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				if( e.getDot() != e.getMark() ) //text has been selected
					updateWordCount();
				else if( e.getDot() == e.getMark() && wordCountTextSelected) //we have unselected text
					updateWordCount();
			}
		});
	}

	private void loadFile()
	{
		try( FileInputStream fis = new FileInputStream( saveFile );
		     InputStreamReader isr = new InputStreamReader( fis );
		     BufferedReader br = new BufferedReader(isr) )
		{
			while(true)
			{
				String line = br.readLine();
				if( line == null )
					break;

				try
				{
					ta.getDocument().insertString( ta.getDocument().getLength(), line+ "\n", null );
				}
				catch( BadLocationException e ) {}
			}

			br.close();

			ta.setCaretPosition( ta.getDocument().getLength() );
			updateWordCount();
		}
		catch(IOException e) {}
	}

	private void saveFile(boolean autosave)
	{
		try( FileOutputStream fos = new FileOutputStream( saveFile );
		     OutputStreamWriter osw = new OutputStreamWriter( fos, "utf8" );
		     BufferedWriter bw = new BufferedWriter( osw ) )
		{
			ta.write(bw);
			bw.close();
			updateLastSave();
			dirtyFile = false;
			lastSaveAutoSave = autosave;
		}
		catch(IOException e) {}
	}

	public boolean quit()
	{
		return win.escHit() || win.closeRequested();
	}

	public void update()
	{
		updateLastSave();

		if( System.nanoTime() - nsLastModifiedDocument >= autosave_time )
			saveFile(true);
	}

	public void dispose()
	{
		saveFile(true);

		GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

		if( dev.getFullScreenWindow() == win )
			dev.setFullScreenWindow( null );

		win.dispose();
	}

	private JPanel createSpacerPanel(Dimension preferredSize)
	{
		JPanel p = new JPanel();
		p.setBackground( Color.black );
		p.setPreferredSize( preferredSize );
		return p;
	}

	private JTextPane createTextArea()
	{
		final Font font = new Font("Dialog", 0, 16);
		JTextPane ta = new JTextPane();
		ta.setFont( font );
		ta.setCaretColor( clr );
		ta.setForeground( clr );
		ta.setBackground( Color.black );
		//ta.setDisabledTextColor( Color.red );
		//ta.setSelectedTextColor( clr );
		//ta.setSelectionColor( Color.green );

		MutableAttributeSet set = new SimpleAttributeSet();
		StyleConstants.setLineSpacing( set, 1.5f );

		ta.setParagraphAttributes( set, false );
		//ta.setTabSize( 4 );
		//ta.setLineWrap( true );
		//ta.setWrapStyleWord( true );

		//This caret code was taken from: http://stackoverflow.com/a/11809374
		ta.setCaret(new DefaultCaret() {
			public void paint(Graphics g) {

				JTextComponent comp = getComponent();
				if (comp == null)
					return;

				Rectangle r;
				try {
					r = comp.modelToView(getDot());
					if (r == null)
						return;
				} catch (BadLocationException e) {
					return;
				}
				r.height = font.getSize(); //this value changes the caret size
				if (isVisible())
					g.fillRect(r.x, r.y, 1, r.height);
			}
		});

		return ta;
	}

	private JPanel createInfoPanel(int wid)
	{
		JPanel p = new JPanel();

		p.setFont( new Font( "Dialog", 0, 16 ) );
		p.setPreferredSize( new Dimension( wid, (int) (p.getFont().getSize() * 1.5) ) );

		p.setBackground( Color.gray );
		p.setForeground( Color.black );

		p.setLayout( new FlowLayout( FlowLayout.LEFT ) );

		return p;
	}

	private void updateWordCount()
	{
		try
		{
			wordCountTextSelected = (ta.getSelectedText() != null);
		}
		catch( IllegalArgumentException e )
		{
			//This exception will be thrown when selected text is deleted
			wordCountTextSelected = false;
		}

		String str = ( wordCountTextSelected ? ta.getSelectedText() : ta.getText() );
		int count = countWords( str );
		if( count == 1 )
			lbl_wordCount.setText( "1 word" );
		else
			lbl_wordCount.setText( count+ " words" );
	}

	private int countWords(String str)
	{
		if( !str.equals("") )
			return str.split("\\s+").length; //TODO there's probably a faster and more accurate way to do this

		return 0;
	}

	private void updateLastSave()
	{
		if( saveFile.isFile() )
		{
			Date date = new Date( saveFile.lastModified() );

			long sec = ChronoUnit.SECONDS.between( date.toInstant(), Instant.now() );
			long min = ChronoUnit.MINUTES.between( date.toInstant(), Instant.now() );

			StringBuffer str = new StringBuffer();

			if( dirtyFile )
				str.append("* ");

			if( lastSaveAutoSave )
				str.append("(Last autosaved: ");
			else
				str.append("(Last saved: ");

			if( sec < 5 )
				str.append("a few moments ago, ");
			else if( sec < 60 )
				str.append(sec+ " second(s) ago, ");
			else
				str.append(min+ " minute(s) ago, ");

			str.append("at " +date.toString()+ ")");
			lbl_lastSave.setText( str.toString() );
		}
		else
		{
			lbl_lastSave.setText( "(Last saved: never)" );
		}
	}
}
