package mx.ecosur.multigame.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import mx.ecosur.multigame.Cell;
import mx.ecosur.multigame.GameGrid;
import mx.ecosur.multigame.GameState;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.ejb.SharedBoardRemote;
import mx.ecosur.multigame.ejb.entity.Game;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Player;

public class GameFrame extends JFrame {

	/* The type of game being played */
	GameType gameType;
	
	/* A handle to the shared board instance used by all players of
	 * the game.
	 */
	SharedBoardRemote sharedBoard;

	/*
	 * The local player 
	 */
	GamePlayer player;

	public GameFrame(String title, SharedBoardRemote board, GamePlayer player) {
		super();
		this.setTitle(title);
		this.gameType = board.getGameType();
		this.sharedBoard = board;
		this.player = player;
		this.initialize();
	}

	void initialize() {
		/* Check and see if the game already has a player,
		 * if so, set the GameState to BEGIN.
		 */
		Game game = this.sharedBoard.getGame();
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		JDesktopPane layers = new JDesktopPane();
		this.setLayeredPane(layers);

		JSplitPane split;
		JInternalFrame frame = generateInfoFrame(dim);
		layers.add(frame);

		/* chat panel */
		frame = generateChatFrame(dim);
		layers.add(frame);

		/* MenuBar */
		this.setJMenuBar(createMenuBar());

		/* the main panel */
		frame = generateGameFrame(dim);
		layers.add(frame);

		/* setup the panel for display */
		Dimension preferredSize = new Dimension((int) (dim.getWidth()),
				(int) (dim.getHeight()));
		this.setPreferredSize(preferredSize);
		this.setLocation(0, 0);
	}

	private JInternalFrame generateGameFrame(Dimension dim) {
		JInternalFrame frame;
		GamePanel main = null;
		
		switch (gameType) {
			case CHECKERS:
				main = new GamePanel(this.gameType, 8, 8);
				break;
			case PENTE:
				main = new GamePanel(this.gameType, 19, 19);
				break;
			default:
				//fall through
		}
			
		frame = new JInternalFrame();
		frame.getContentPane().add(main);
		frame.setLocation(3, 20);

		int height = (int) (dim.getWidth()) / 2;
		int width = (int) (dim.getWidth()) / 2;
		frame.setSize(width, height);

		frame.setTitle(this.gameType.name());
		frame.setVisible(true);
		frame.setClosable(true);
		frame.setMaximizable(true);
		frame.setIconifiable(true);
		frame.setAutoscrolls(true);
		return frame;
	}

	private JInternalFrame generateChatFrame(Dimension dim) {
		JSplitPane split;
		JInternalFrame frame;
		JPanel chat = new JPanel();
		JTextArea textArea = new JTextArea(10,40);
		textArea.setLineWrap(true);
		textArea.setEditable(true);
		textArea.setText(new String());
		JScrollPane scrollPane = new JScrollPane(textArea);

		textArea = new JTextArea(10,20);
		textArea.setBackground(Color.DARK_GRAY);
		textArea.setForeground(Color.GREEN);
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.setText(String.format("%s: Hi, cool game.\nLuis:  Si, es bueno.", 
				player.getPlayer().getName()));
		JScrollPane readerPane = new JScrollPane (textArea);
		readerPane.add(new JButton());
		
		split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, readerPane,
				scrollPane);
		chat.add(split);
		

		frame = new JInternalFrame();
		frame.setLayout(new FlowLayout());
		frame.setTitle("Messaging");
		frame.getContentPane().add(chat);
		frame.setVisible(true);
		frame.pack();
		frame.setResizable(false);
		frame.setIconifiable(true);
		frame.setLocation((int) (dim.getWidth()) - frame.getWidth(), 300);
		return frame;
	}

	private JInternalFrame generateInfoFrame(Dimension dim) {
		/* info panel */
		JPanel info = new JPanel(new GridLayout (4,1));
		JLabel label = new JLabel ("Name:");
		JLabel name = new JLabel (player.getPlayer().getName());
		JSplitPane split = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, label, 
				name);
		info.add(split);
		
		label = new JLabel ("Color:");
		JLabel tokenLabel = new JLabel (player.getColor().name());
		split = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, label, tokenLabel);
		info.add(split);
		
		label = new JLabel ("Join time:");
		String date = String.format("%ta %<tb %<td %<tY %<tT", new Date(player.getPlayer().getLastRegistration()));
		JLabel lastReg = new JLabel (date);
		split = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, label, lastReg);
		info.add(split);
		
		label = new JLabel ("Wins:");
		JLabel wins = new JLabel ("" + player.getPlayer().getWins());
		split = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, label, wins);
		info.add(split);
		
		label = new JLabel ("Turn:");
		JLabel turn = new JLabel ("" + player.isTurn());
		split = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, label, turn);
		info.add(split);
		
		label = new JLabel ("State:");
		JLabel state = new JLabel (sharedBoard.getState().toString());
		split = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, label, state);
		info.add(split);		
		
		label = new JLabel ("Players:");
		/** TODO:  Make this label respond to change events */
		JLabel players = new JLabel (playerList  (sharedBoard.getPlayers()));
		split = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, label, players);
		info.add(split);

		JInternalFrame frame = new JInternalFrame();
		frame.setTitle("Information");
		frame.getContentPane().add(info);
		frame.pack();
		frame.setLocation((int) (dim.getWidth()) - frame.getWidth(), 50);
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setIconifiable(true);
		return frame;
	}

	private String playerList (List<GamePlayer> list) {
		StringBuffer buf = new StringBuffer();
		for (GamePlayer p : list) {
			if (p.equals(player))
				continue;
			buf.append (p.getPlayer().getName());
			buf.append (" ");
		}
		
		return buf.toString().trim();
	}

	private JMenuBar createMenuBar() {
		JMenuBar bar = new JMenuBar();
        
        JMenu menu = new JMenu ("Game");
        JMenuItem item = new JMenuItem ("Switch");
        menu.add(item);
        item = new JMenuItem ("Quit");
        menu.add(item);
        bar.add (menu);
        
        menu = new JMenu ("Help");
        item = new JMenuItem ("Checkers");
        menu.add(item);        
        item = new JMenuItem ("Pente");
        menu.add(item);
        bar.add(menu);
        
        return bar;	
	
	}

	class GamePanel extends JPanel {

		int rows, columns;
		GameType game;

		GamePanel(GameType game_type, int rows, int columns) {
			this.rows = rows;
			this.columns = columns;
			this.game = game_type;
			this.initialize();
		}

		void initialize() {
	        Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
	        GridLayout layout = new GridLayout(this.rows, this.columns);
	        GameGrid grid = sharedBoard.getGameGrid();
	        this.setLayout(layout);
	        for (int row = 0; row < this.rows; row++) {
	            for (int column = 0; column < this.columns; column++) {
	                /* Check for an existing Cell, if there, create an icon
	                 * and add it to the label 
	                 */
	                Cell candidate = grid.getLocation(row, column);
	                if (candidate != null) {
	                	Token token = new Token(candidate.getColor());
		                token.setBackground(getColor (row, column));
		                token.setBorder(border);
		                token.addMouseListener(new GridListener());
	                	this.add(token);
	                } else {
		                JPanel squarePanel = new JPanel();
		                squarePanel.setBackground(getColor (row, column));
		                squarePanel.setBorder(border);
		                this.add(squarePanel);
	                }
	            }
	        }	
		}

		Color getColor (int row, int column) {
	    	Color color = null;
	    	
	    	switch (game) {
	    		case CHECKERS:
	    			color = Color.LIGHT_GRAY;
		            if ((row + column) % 2 == 0)
		            	color =color.darker().darker();
		            else
		            	color = color.brighter();
	    			break;
	    		case PENTE:
	    			color = Color.YELLOW;
		            color = color.darker().darker();
	    			break;
	    	}
	            
	        return color;
	    }
		
		
	}
	
	class GridListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent event) {
			System.out.println ("Mouse click at: " + event.getPoint());
			JPanel panel = (JPanel) event.getComponent();
			
			if (panel.getComponentCount() == 0) {
				Token token = new Token(player.getColor());
				panel.add(token, 0);
			} else {
				System.out.println ("Token set already!");
			}
		}
	}
}
