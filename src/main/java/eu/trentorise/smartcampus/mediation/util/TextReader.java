package eu.trentorise.smartcampus.mediation.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class TextReader {

	private ArrayList<String> listBadWords;

	private final static String FILE_IT = "it";
	private final static String FILE_EN = "en";
	private final static String FILE_DE = "de";
	private final static String FILE_ES = "es";
	private final static String FILE_FR = "fr";
	private final static String FILE_JA = "ja";
	private final static String FILE_NL = "nl";
	private final static String FILE_PT = "pt";
	private final static String FILE_RU = "ru";
	private final static String FILE_ZH = "zh";

	private final static int FILES_NUMBER = 10;

	public TextReader() {
		// TODO Auto-generated constructor stub
	}

	public ArrayList<String> getListFromFiles() {

		listBadWords = new ArrayList<String>();
		
		for (int i = 0; i < FILES_NUMBER; i++) {
			
			switch (i) {

			case 0:
				listBadWords.addAll(buildListFromFile(FILE_IT));
				break;
			case 1:
				listBadWords.addAll(buildListFromFile(FILE_EN));
				break;
			case 2:
				listBadWords.addAll(buildListFromFile(FILE_DE));
				break;
			case 3:
				listBadWords.addAll(buildListFromFile(FILE_ES));
				break;
			case 4:
				listBadWords.addAll(buildListFromFile(FILE_FR));
				break;
			case 5:
				listBadWords.addAll(buildListFromFile(FILE_JA));
				break;
			case 6:
				listBadWords.addAll(buildListFromFile(FILE_NL));
				break;
			case 7:
				listBadWords.addAll(buildListFromFile(FILE_PT));
				break;
			case 8:
				listBadWords.addAll(buildListFromFile(FILE_RU));
				break;
			case 9:
				listBadWords.addAll(buildListFromFile(FILE_ZH));
				break;
			}
		}

		return listBadWords;

	}

	
	
	public ArrayList<String> buildListFromFile(String file) {
		ArrayList<String> listFile = new ArrayList<String>();

		URL url=this.getClass().getResource("/");
		String filePath=url.getPath()+file;
		InputStream input = this.getClass().getResourceAsStream("/bad-words/"+file);

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
