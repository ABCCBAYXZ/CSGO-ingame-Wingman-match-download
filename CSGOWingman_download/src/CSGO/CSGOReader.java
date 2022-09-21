package CSGO;

import java.io.BufferedReader;
import java.io.IOException;

public class CSGOReader {
	private BufferedReader reader;

	public CSGOReader(BufferedReader _reader) {
		reader = _reader;
	}

	public String read() throws IOException {
		
		String line = "";
		
		if((line = reader.readLine()) != null) 
			return line;
		
		
		return null;
	}
	
	public boolean isDownload(String line) {
		if(line.equals("!download ")) {
				return true;
			}
		return false;
	}
	
	public boolean isPlay(String line) {
		if(line.equals("!play ")) {
				return true;
			}
		return false;
	}
	
	public boolean isStop(String line) {
		if(line.equals("!stop ")) {
			return true;
		}
	return false;
	}
	
	public void close() {
		try {
			reader.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public BufferedReader getReader() {
		return reader;
	}

	public void setReader(BufferedReader reader) {
		this.reader = reader;
	}

	

	

}
