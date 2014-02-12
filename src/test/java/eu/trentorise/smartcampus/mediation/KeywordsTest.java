package eu.trentorise.smartcampus.mediation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import eu.trentorise.smartcampus.mediation.engine.MediationParserImpl;
import eu.trentorise.smartcampus.mediation.util.KeyWordsFileReader;



public class KeywordsTest {
	
	private String token;
	private MediationParserImpl mediationParser;
	private KeyWordsFileReader fileReader;
	private URL url;

	@Before
	public void init() throws MalformedURLException {
		token = Constants.CLIENT_AUTH_TOKEN;
		url = new URL(Constants.URL_FILE);
		mediationParser = new MediationParserImpl(Constants.URL_MEDIATION_SERVICE, Constants.WEBAPP_NAME, url);
		fileReader = new KeyWordsFileReader(url);
	}

	@Test
	public void keywords() throws Exception {
		
		// init keywords
		boolean operation = mediationParser.initKeywords(token);
		Assert.assertTrue(operation);
		System.out.println("Init keywords: "+operation);
		
		
	
		// get keywords from file
		List<String> words = mediationParser.getNotApprovedWordDictionary();
		Assert.assertNotNull(words);
		
		
		// reset keywords
		operation = mediationParser.resetKeyWords();
		Assert.assertTrue(operation);
		System.out.println("reset keywords: "+operation);
	}

	
	
	
}
