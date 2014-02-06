package eu.trentorise.smartcampus.mediation.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class KeyWordsFileReader {
	
	public static final String RESOURCE_PATH = "/bad-words/";
	public static final String FILE_NAME = "keywords";

	public KeyWordsFileReader() {
		// TODO Auto-generated constructor stub
	}
	
	public List<String> getListFromFile() {
		List<String> listFile = new ArrayList<String>();

		URL url=this.getClass().getResource("/");
		String filePath=url.getPath()+FILE_NAME;
		InputStream input = this.getClass().getResourceAsStream(RESOURCE_PATH+FILE_NAME);

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					input));
			String line = null;
			while ((line = reader.readLine()) != null) {
				listFile.add(line);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return listFile;
	}
	
	
}
