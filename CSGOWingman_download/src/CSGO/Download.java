package CSGO;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

public class Download {

	private String directory;
	
	public void unzipBZ2() throws FileNotFoundException, IOException {
		var input = new BZip2CompressorInputStream(new BufferedInputStream(new FileInputStream(directory + "\\demo.dem.bz2")));
	    var output = new FileOutputStream(directory + "\\demo.dem");
	    try (input; output) {
	        IOUtils.copy(input, output);
	    }
	    
	    new File(directory + "\\demo.dem.bz2").delete();
	    input.close();
	    output.close();
	}
	
	public void downloadDemo(String line) throws IOException {
		
		File out = new File(directory + "\\demo.dem.bz2");
		URL s = new URL(line.replace("<a target=\"_blank\" href=\"", "").replace("\">", ""));
		HttpURLConnection http = (HttpURLConnection)s.openConnection();
		BufferedInputStream in = new BufferedInputStream(http.getInputStream());
		FileOutputStream fos = new FileOutputStream(out);
		BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
		byte[] buffer = new byte[1024];
	
		int read = 0;
	
		
		while((read = in.read(buffer, 0, 1024)) >= 0) {
			bout.write(buffer, 0, read);
		}
		
		bout.close();
		in.close();
		
		unzipBZ2();
		System.out.println("complete");
	}


	public void deleteDemo() {
		new File(directory + "\\demo.dem").delete();
	}
	
	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

}
