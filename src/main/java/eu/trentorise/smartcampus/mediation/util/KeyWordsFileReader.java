package eu.trentorise.smartcampus.mediation.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.cxf.helpers.FileUtils;

import eu.trentorise.smartcampus.moderatorservice.model.KeyWord;

public final class KeyWordsFileReader {

	public static final String RESOURCE_PATH = "/src/main/resources/bad-words/";
	public static final String FILE_NAME = "keywords";
	public static final String ENCODE = "utf-8";
	public URL url;

	public KeyWordsFileReader(URL url) {
		this.url = url;
	}

	public List<String> getListFromFile() {
		List<String> listFile = new ArrayList<String>();

		File file;
		FileInputStream fip = null;
		BufferedReader reader = null;

		try {

			file = new File(url.toURI());

			// if file doesnt exists
			if (!file.exists()) {
				return null;
			}

			fip = new FileInputStream(file);

			reader = new BufferedReader(new InputStreamReader(fip));
			String line = null;
			while ((line = reader.readLine()) != null) {
				listFile.add(line);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				reader.close();
				fip.close();
			} catch (Exception ex) {
			}
		}

		return listFile;
	}

	public boolean setListToFile(Collection<KeyWord> keywords) {

		BufferedWriter writer = null;
		File file;
		FileOutputStream fop = null;

		try {

			String workingDir = System.getProperty("user.dir");

			file = new File(url.toURI());

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			fop = new FileOutputStream(file);

			// OutputStream input = this.getClass().
			// getResourceAsStream("/bad-words/"+FILE_NAME);
			//
			//
			//
			//
			// writer = new BufferedWriter(new OutputStreamWriter(
			// new FileOutputStream(RESOURCE_PATH+FILE_NAME), ENCODE));

			fop.write("".getBytes());

			for (KeyWord k : keywords) {

				fop.write((k.getKeyword() + "\n").getBytes());
			}

		} catch (IOException ex) {
			return false;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				fop.close();
			} catch (Exception ex) {
			}
		}

		return true;
	}

	public boolean deleteKeywords() {

		File file;
		FileOutputStream fop = null;
		BufferedWriter writer = null;

		try {

			file = new File(url.toURI());

			fop = new FileOutputStream(file);

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			fop.write("".getBytes());
			fop.flush();

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				fop.close();
			} catch (Exception ex) {
			}
		}

		return true;
	}

}
