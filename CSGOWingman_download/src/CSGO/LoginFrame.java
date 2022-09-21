package CSGO;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class LoginFrame extends JFrame implements ActionListener,  DocumentListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Container container = getContentPane();
	JLabel userLabel = new JLabel("USERNAME");
	JLabel passwordLabel = new JLabel("PASSWORD");
	JLabel sourceLabel = new JLabel("CSGO DIRECTORY");
	JLabel steamIDLabel = new JLabel("STEAM ID");
	
	JButton directoryButton = new JButton("...");
	JTextField userTextField = new JTextField();
	JTextField steamIDField = new JTextField();
	JButton save = new JButton("save");
	JTextField directory = new JTextField();
	 JFileChooser chooser;
	JTextField emailField = new JTextField();
	JTextField twofacField = new JTextField();
	JPasswordField passwordField = new JPasswordField();
	JButton loginButton = new JButton("LOGIN");
	JButton resetButton = new JButton("RESET");
	JCheckBox showPassword = new JCheckBox("Show Password");
	JLabel background = new JLabel(new ImageIcon(getClass().getClassLoader().getResource("pic.jpg")));

	boolean loggedIn;
	
	Login login;
	
	
	LoginFrame(Login _login) {
		login = _login;
		
		ToolTipManager.sharedInstance().setInitialDelay(100);
		
		setSaveDataText();
		setVisibility();
		setToolTiptext();
		setLayoutManager();
		setLocationAndSize();
		setColor();
		addComponentsToContainer();
		addActionEvent();

	}
	
	public void setSaveDataText() {
		
		directory.setText(login.getDirectory());
		steamIDField.setText(login.getSteamID());
		
	}
	
	public void setVisibility() {
		directory.setEnabled(false);
		loginButton.setEnabled(false);
	}
	
	public void setToolTiptext() {
		steamIDField.setToolTipText("the number in your profile URL (17 digits)");
		directoryButton.setToolTipText("example: D:\\SteamLibrary\\steamapps\\common\\Counter-Strike Global Offensive\\csgo");
	}
	
	public void setLayoutManager() {
		container.setLayout(null);
	}

	public void setColor() {
		userLabel.setForeground(Color.WHITE);
		passwordLabel.setForeground(Color.WHITE);
		sourceLabel.setForeground(Color.WHITE);
		steamIDLabel.setForeground(Color.WHITE);
	}

	public void setLocationAndSize() {
		steamIDLabel.setBounds(50, 150, 100, 30);
		sourceLabel.setBounds(40, 80, 110, 30);
		userLabel.setBounds(50, 220, 100, 30);
		passwordLabel.setBounds(50, 290, 100, 30);
		directoryButton.setBounds(310, 80, 30, 30);
		directory.setBounds(150, 80, 150, 30);
		steamIDField.setBounds(150, 150, 150, 30);
		userTextField.setBounds(150, 220, 150, 30);
		passwordField.setBounds(150, 290, 150, 30);
		showPassword.setBounds(150, 320, 150, 30);
		loginButton.setBounds(50, 370, 100, 30);
		resetButton.setBounds(200, 370, 100, 30);
		background.setBounds(0, 0, 1000, 1000);
	}

	public void addComponentsToContainer() {
		container.add(background);
		background.add(directoryButton);
		background.add(steamIDLabel);
		background.add(sourceLabel);
		background.add(directory);
		background.add(steamIDField);
		background.add(userLabel);
		background.add(passwordLabel);
		background.add(userTextField);
		background.add(passwordField);
		background.add(showPassword);
		background.add(loginButton);
		background.add(resetButton);
	}

	public void addActionEvent() {
		passwordField.getDocument().addDocumentListener(this);
		userTextField.getDocument().addDocumentListener(this);
		directory.getDocument().addDocumentListener(this);
		steamIDField.getDocument().addDocumentListener(this);
		directoryButton.addActionListener(this);
		loginButton.addActionListener(this);
		resetButton.addActionListener(this);
		showPassword.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Coding Part of LOGIN button
		
		if(e.getSource() == directoryButton) {
			System.out.println("sui");
			chooser = new JFileChooser(); 
		    chooser.setCurrentDirectory(new java.io.File("."));
		    chooser.setDialogTitle("CSGO directory");
		    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    //
		    // disable the "All files" option.
		    //
		    chooser.setAcceptAllFileFilterUsed(false);
		    //    
		    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
		       directory.setText(chooser.getSelectedFile().toString()); 
		       directory.setToolTipText(chooser.getSelectedFile().toString());
		       login.setDirectory(chooser.getSelectedFile().toString());
		      }
		     }
		
		if(e.getSource() == save) {
			
			login.setSteamID(steamIDField.getText());
			System.out.println("saved:" + steamIDField.getText());
			loggedIn = true;
		}
		
		
		if (e.getSource() == loginButton) {
			login.setUsername(userTextField.getText());
			login.setPassword(passwordField.getText());
			login.setEmailauth(emailField.getText());
			login.setTwofactorcode(twofacField.getText());
			login.setSteamID(steamIDField.getText());
			try {
				login.encryptPassword();
			} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
					| IllegalBlockSizeException | BadPaddingException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				String respond = login.requestLogin();
				JOptionPane.showMessageDialog(null, respond);

				if (respond.equals("enter Steam Guard code")) {

					loadGuardOverlay();

				} else if (respond.equals("enter Email code")) {

					loadEmailOverlay();

				} else if (respond.equals("Logged in!")) {
					loggedIn = true;
					// close window... start csgo

				}

			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

		}
		// Coding Part of RESET button
		if (e.getSource() == resetButton) {
			userTextField.setText("");
			passwordField.setText("");
			steamIDField.setText("");
			directory.setText("");
		}
		// Coding Part of showPassword JCheckBox
		if (e.getSource() == showPassword) {
			if (showPassword.isSelected()) {
				passwordField.setEchoChar((char) 0);
			} else {
				passwordField.setEchoChar('*');
			}

		}
	}

	public void removeUpdate(DocumentEvent e) {
		 checkFields();
     }

     public void insertUpdate(DocumentEvent e) {
    	 checkFields();
     }

     public void changedUpdate(DocumentEvent e) {
    	 checkFields();
     }
	
	public void checkFields() {
  
        if(steamIDField.getText().length() == 17 && userTextField.getText().length() > 0
        		&& directory.getText().contains("\\csgo") && passwordField.getText().length() > 0)
            loginButton.setEnabled(true);
        else
            loginButton.setEnabled(false);
    }
	
	public void loadSteamIDOverlay() {

		container.removeAll();
		background.removeAll();
		this.repaint();
		background.repaint();

		container.add(background);
		save.addActionListener(this);
		save.setBounds(50, 370, 100, 30);
		background.add(save);
		steamIDField.setBounds(150, 290, 150, 30);
		background.add(steamIDField);
		passwordLabel.setText("STEAM ID");
		background.add(passwordLabel);

	}
	
	private void loadEmailOverlay() {

		container.removeAll();
		background.removeAll();
		this.repaint();
		background.repaint();

		container.add(background);
		background.add(loginButton);
		emailField.setBounds(150, 290, 150, 30);
		background.add(emailField);
		passwordLabel.setText("EMAIL CODE");
		background.add(passwordLabel);

	}

	private void loadGuardOverlay() {

		container.removeAll();
		background.removeAll();
		this.repaint();
		background.repaint();

		container.add(background);
		background.add(loginButton);
		twofacField.setBounds(150, 290, 150, 30);
		background.add(twofacField);
		passwordLabel.setText("GUARD CODE");
		background.add(passwordLabel);

	}

	public void openFrame(LoginFrame frame) {
		frame.setTitle("Steam Login");
		frame.setVisible(true);
		frame.setBounds(10, 10, 370, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		
	}

	public boolean getLoggedIn() {
		return loggedIn;
	}

	public void showMessage(String string) {
		JOptionPane.showMessageDialog(null, string);
	}

	public void setLoggedIn(boolean b) {
		loggedIn = b;
		
	}


		

}
