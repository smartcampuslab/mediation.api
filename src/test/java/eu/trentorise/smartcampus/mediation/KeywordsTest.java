package eu.trentorise.smartcampus.mediation;

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

	@Before
	public void init() {
		token = Constants.CLIENT_AUTH_TOKEN;
		mediationParser = new MediationParserImpl(Constants.URL_MEDIATION_SERVICE, Constants.WEBAPP_NAME);
		fileReader = new KeyWordsFileReader();
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
