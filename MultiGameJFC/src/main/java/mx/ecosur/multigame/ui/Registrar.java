/**
 * 
 */
package mx.ecosur.multigame.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import mx.ecosur.multigame.Color;
import mx.ecosur.multigame.GameType;
import mx.ecosur.multigame.InvalidRegistrationException;
import mx.ecosur.multigame.ejb.RegistrarRemote;
import mx.ecosur.multigame.ejb.SharedBoardRemote;
import mx.ecosur.multigame.ejb.entity.GamePlayer;
import mx.ecosur.multigame.ejb.entity.Player;

/**
 * @author awater
 *
 */
public class Registrar extends JFrame {
	
	public Registrar () {
		this.initialize();
	}
	
	void initialize () {
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 400;
        int height = 150;
            
        this.getContentPane().setLayout(new FlowLayout());
        
        JPanel panel = new JPanel();
        JLabel label = new JLabel ("Would you like to play a game?");
        panel.add(label, BorderLayout.BEFORE_FIRST_LINE);
        
        this.getContentPane().add(panel, BorderLayout.AFTER_LINE_ENDS);
    
        label = new JLabel ("Name:");
        JTextField field = new JTextField (12);
        JSplitPane splitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, true, label, field);
        this.getContentPane().add(splitPane, BorderLayout.AFTER_LAST_LINE);
    
        label = new JLabel ("Game:");
        JComboBox list = new JComboBox ();
        list.addItem("Checkers");
        list.addItem ("Pente");
        
        splitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, true, label, list);
        this.getContentPane().add(splitPane, BorderLayout.AFTER_LAST_LINE);
        
        panel = new JPanel();
        JButton play = new JButton ();
        play.setText("Play");
        play.addActionListener(new RegisterListener(this, field, list));
        play.setActionCommand("Play");
        panel.add(play);

        this.getContentPane().add(panel, BorderLayout.SOUTH);
        
        Dimension preferredSize = new Dimension();
        preferredSize.width = width;
        preferredSize.height = height;
        
        int x = (int) dim.getWidth() / 2 - width /2;
        int y = (int) dim.getHeight () /2 - height /2;
        
        this.setLocation(x, y);  
        this.setPreferredSize(preferredSize);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	class RegisterListener implements ActionListener {
		
		private JFrame enclosedFrame;
		private JTextField textField;
		private JComboBox comboBox;
		private GameType gameType;
		private SharedBoardRemote sharedBoard;
		
		public RegisterListener (JFrame frame, JTextField textField, 
				JComboBox comboBox) 
		{
			this.enclosedFrame = frame;
			this.textField = textField;
			this.comboBox = comboBox;
		    this.gameType = GameType.valueOf(((String)
	        		comboBox.getSelectedItem()).toUpperCase());	
		}
		

		public void actionPerformed(ActionEvent event) {
	        try {
	        	if (event.getActionCommand() == "Play") {
	        		GamePlayer player = register();
	        		GameFrame game = new GameFrame (
	        				"Multi-Game", sharedBoard, player);
	        		game.pack();
	        		game.setVisible(true);
	        		enclosedFrame.setVisible(false);
	        	} 
				
			} catch (NamingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidRegistrationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private GamePlayer register() throws NamingException, 
		InvalidRegistrationException, RemoteException 
		{
		    this.gameType = GameType.valueOf(((String)
	        		comboBox.getSelectedItem()).toUpperCase());
			InitialContext ic = new InitialContext();
			
	        /* Register the user */
	        RegistrarRemote registrar = (RegistrarRemote) ic.lookup(
    			"mx.ecosur.multigame.ejb.RegistrarRemote");
	        
			/* Get the shared board */
	        sharedBoard = (SharedBoardRemote) ic.lookup(
	        		"mx.ecosur.multigame.ejb.SharedBoardRemote");
	        sharedBoard.locateSharedBoard(this.gameType);
	        
			String name = this.textField.getText();
			Player player = registrar.locatePlayer(name);
			GamePlayer ret = registrar.registerPlayer(player, Color.BLACK, 
					sharedBoard.getGameType());
	        return ret;
		}
	}
	
	public static void main (String[] args) {
		System.out.println ("Starting Registrar");
		Registrar r = new Registrar();
		r.pack();
		r.setVisible(true);
	}
}
