package eu.trentorise.smartcampus.mediation.engine;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import eu.trentorise.smartcampus.mediation.util.KeyWordsFileReader;
import eu.trentorise.smartcampus.moderatorservice.model.ContentToModeratorService;
import eu.trentorise.smartcampus.moderatorservice.model.KeyWord;
import eu.trentorise.smartcampus.moderatorservice.model.State;
import eu.trentorise.smartcampus.moderatoservice.ModeratorService;
import eu.trentorise.smartcampus.moderatoservice.exception.ModeratorServiceException;

public class MediationParserImpl {

	private String webappname;
	private String urlServermediation;
	private ModeratorService serModeratorService;
	private KeyWordsFileReader keyWordsReader;
	private URL url;

	private static final Logger logger = Logger
			.getLogger(MediationParserImpl.class);

	public MediationParserImpl() {

	}

	public MediationParserImpl(String urlServermediation, String webappname, URL urlResource) {
		super();
		this.webappname = webappname;
		this.urlServermediation = urlServermediation;
		serModeratorService = new ModeratorService(urlServermediation);
		this.url = urlResource;
	}

	public boolean localValidationComment(String testoentity, String identity,
			Long userid, String token) throws SecurityException,
			ModeratorServiceException {

		ContentToModeratorService messageToMediationService = new ContentToModeratorService(
				webappname, identity, testoentity, String.valueOf(userid));
		List<String> x = getNotApprovedWordDictionary();

		boolean isApproved = true;

		long after = 0;
		long before = 0;
		long diff = 0;
		int i = 0;
		
		while ((i < x.size()) && isApproved) {

			before = System.currentTimeMillis();
			String keyword = x.get(i);
			isApproved = (testoentity.toLowerCase().indexOf(keyword) == -1);
			i++;
			if (!isApproved) {
				messageToMediationService.setKeywordApproved(isApproved);
				messageToMediationService.setNote("[Blocked by = "
						+ keyword + "]");
				messageToMediationService.setManualApproved(State.NOT_REQUEST);
				addCommentToMediationService(messageToMediationService, token);
				after = System.currentTimeMillis();
				diff = after - before;
				logger.info("Time parsing = " + diff + " millisec");
				return isApproved;
			}
		}

		after = System.currentTimeMillis();
		diff = after - before;
		logger.info("Time parsing = " + diff + " millisec");
		messageToMediationService.setKeywordApproved(isApproved);
		messageToMediationService.setManualApproved(State.NOT_REQUEST);
		addCommentToMediationService(messageToMediationService, token);

		return isApproved;

	}

	public boolean remoteValidationComment(String testoentity, String identity,
			Long userid, String token) throws SecurityException,
			ModeratorServiceException {

		ContentToModeratorService messageToMediationService = new ContentToModeratorService(
				webappname, identity, testoentity, String.valueOf(userid));

		boolean isApproved = true;

		messageToMediationService.setKeywordApproved(isApproved);
		addCommentToMediationService(messageToMediationService, token);

		return isApproved;

	}

	private void addCommentToMediationService(
			ContentToModeratorService messageToMediationService, String token)
			throws SecurityException, ModeratorServiceException {

		serModeratorService.addContentToManualFilterByApp(token, webappname,
				messageToMediationService);

	}

	// new Version con credintial
	public Map<String, Boolean> updateComment(long fromData, long toData,
			String token) {

		try {

			logger.info("client auth token: " + token);
			List<ContentToModeratorService> listMessgae = serModeratorService
					.getContentByDateWindow(token, webappname, fromData, toData);

			Map<String, Boolean> returnMap = new HashMap<String, Boolean>();

			Boolean resultApprove = false;
			Iterator<ContentToModeratorService> index = listMessgae.iterator();

			while (index.hasNext()) {
				ContentToModeratorService object = index.next();

				resultApprove = object.isKeywordApproved();
				State valueBool = object.getManualApproved();

				// if both of filters are true message moderation was positive
				resultApprove = resultApprove
						&& (valueBool.compareTo(State.APPROVED) == 0
								|| valueBool.compareTo(State.WAITING) == 0 || valueBool
								.compareTo(State.NOT_REQUEST) == 0); // object.getBoolean("parseApproved")&&

				returnMap.put(object.getObjectId(), resultApprove);
			}

			return returnMap;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


	public boolean resetKeyWords() {
		

		KeyWordsFileReader keyReader = new KeyWordsFileReader(url);

		boolean operationDelete = keyReader.deleteKeywords();

		return operationDelete;
	}

	
	// inizializzo le keyword caricandole dal db del moderatore in file locale
	public boolean initKeywords(String token) throws URISyntaxException {
		
		try {

			Collection<KeyWord> KeyWordList;
				KeyWordList = serModeratorService
						.getAllKeywordFilterContent(token, webappname);
		

			logger.info("key list size " + KeyWordList.size());
			
			KeyWordsFileReader keyReader = new KeyWordsFileReader(url);

			keyReader.setListToFile(KeyWordList);

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (ModeratorServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		


		return true;
	}

	// restituisce la lista delle parole da filtrare
	public List<String> getNotApprovedWordDictionary() {
		List<String> keywordsListOnFile = new ArrayList<String>();

		keyWordsReader = new KeyWordsFileReader(url);
		keywordsListOnFile = null;
		keywordsListOnFile = keyWordsReader.getListFromFile();

		return keywordsListOnFile;
	}

	public String getWebappname() {
		return webappname;
	}

	public void setWebappname(String webappname) {
		this.webappname = webappname;
	}

	public String getUrlServermediation() {
		return urlServermediation;
	}

	public void setUrlServermediation(String urlServermediation) {
		this.urlServermediation = urlServermediation;
		serModeratorService = new ModeratorService(urlServermediation);
	}

}
