package CSGO;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Conductor {

	private Telnet connection;
	private CSGOReader csr;
	private CSGOWriter csw;
	private LoginFrame frame;
	private Login login;
	private Download download;

	Conductor() throws IOException, InterruptedException {

		login = new Login();
		setFrame(new LoginFrame(login));
		download = new Download();

	}

	public void connectToCSGO() throws UnknownHostException, IOException, InterruptedException {
		connection = new Telnet();
		if (connection.getSocket() != null) {
			csr = new CSGOReader(new BufferedReader(new InputStreamReader(connection.getSocket().getInputStream())));
			csw = new CSGOWriter(new DataOutputStream(connection.getSocket().getOutputStream()));
		}
	}

	private void skipLines(int i) throws IOException {
		while (i-- > 0)
			getCsr().read();
	}

	// use this to check console output bytes vs your Text bytes useful for .equals
	@SuppressWarnings("unused")
	private void debug(String line, String TextToCheck) {
		System.out.println("line bytes:" + Arrays.toString(line.getBytes()));
		System.out.println("TextToCheck bytes:" + Arrays.toString("Stop ".getBytes()));
	}

	private void close() {
		getCsw().close();
		getConnection().close();
		getCsr().close();

	}

	private void restart() {
		frame.setVisible(false);
		frame.dispose();
		login.setCounter(0);
		frame = new LoginFrame(login);
		frame.openFrame(frame);
		
		
	}
	
	public void setDirectory() {
		download.setDirectory(login.getDirectory());
	}

	public Telnet getConnection() {
		return connection;
	}

	public void setConnection(Telnet connection) {
		this.connection = connection;
	}

	public CSGOReader getCsr() {
		return csr;
	}

	public void setCsr(CSGOReader csr) {
		this.csr = csr;
	}

	public CSGOWriter getCsw() {
		return csw;
	}

	public void setCsw(CSGOWriter csw) {
		this.csw = csw;
	}

	public LoginFrame getFrame() {
		return frame;
	}

	public void setFrame(LoginFrame frame) {
		this.frame = frame;
	}

	public Login getLogin() {
		return login;
	}

	public void setLogin(Login login) {
		this.login = login;
	}

	public Download getDownload() {
		return download;
	}

	public void setDownload(Download download) {
		this.download = download;
	}

	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {

		Conductor conductor = new Conductor();

		while (true) {
			
			if (!conductor.getLogin().cookieWorking()) {
				
				if (conductor.getLogin().isUnableToGetPersonalData()) {
					conductor.getFrame().showMessage("Cookie expired or wrong SteamID... try to login again");
					conductor.getFrame().setLoggedIn(false);
					conductor.restart();
				}else {
				conductor.getFrame().openFrame(conductor.getFrame());
				
				}
				while (!conductor.getFrame().getLoggedIn()) {
					TimeUnit.SECONDS.sleep(1);
				}

			} else {
		
				conductor.getFrame().setVisible(false);
				conductor.getFrame().dispose();
				
				conductor.setDirectory();
				URI uri = new URI("steam://run/730");
				
				if (Desktop.isDesktopSupported()) {
					Desktop.getDesktop().browse(uri);
				}
				break;
			}
		}
		
		conductor.connectToCSGO();
		// opens Telnet connection and reader/writer

		String line = "";

		// writes connected to CSGO console if telnet connection is up
		if (conductor.getCsr() != null)
			conductor.getCsw().writeConsole("Connected!");

		// reads every line of CS:GO Console
		while (conductor.getCsr() != null && (line = conductor.getCsr().read()) != null) {

			if (conductor.getCsr().isDownload(line)) {
				conductor.getDownload().downloadDemo(conductor.getLogin().requestWingman());
				conductor.getCsw().writeConsole("download completed! type \"echo !play\" to play latest wingman demo");
				conductor.skipLines(1);
			}

			if (conductor.getCsr().isPlay(line)) {
				conductor.getCsw().playDemo();
			}

			if (conductor.getCsr().isStop(line)) {
				conductor.getCsw().writeConsole("disconnected");
				conductor.getDownload().deleteDemo();
				conductor.close();
			}

		}
		conductor.getDownload().deleteDemo();
	}

	

}
