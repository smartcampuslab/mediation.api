package eu.trentorise.smartcampus.mediation.engine;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

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

	public MediationParserImpl(String urlServermediation, String webappname, String urlResource) throws MalformedURLException {
		super();
		this.webappname = webappname;
		this.urlServermediation = urlServermediation;
		serModeratorService = new ModeratorService(urlServermediation);
		if (urlResource != null) {
			this.url = new URL(urlResource);
		}
	}

	public boolean localValidationComment(String testoentity, String identity,
			Long userid, String token) throws SecurityException,
			ModeratorServiceException {

		ContentToModeratorService messageToMediationService = new ContentToModeratorService(
				webappname, identity, testoentity, String.valueOf(userid));
		List<String> x = getNotApprovedWordDictionary();

		boolean isApproved = true;

		int i = 0;
		
		while ((i < x.size()) && isApproved) {

			String keyword = x.get(i);
			isApproved = (testoentity.toLowerCase().indexOf(keyword) == -1);
			i++;
			if (!isApproved) {
				messageToMediationService.setKeywordApproved(isApproved);
				messageToMediationService.setNote("[Blocked by = "
						+ keyword + "]");
				messageToMediationService.setManualApproved(State.NOT_REQUEST);
				addCommentToMediationService(messageToMediationService, token);
				return isApproved;
			}
		}

		messageToMediationService.setKeywordApproved(isApproved);
		messageToMediationService.setManualApproved(State.NOT_REQUEST);
		addCommentToMediationService(messageToMediationService, token);

		return isApproved;
	}

	public void remoteValidationCommentWithSubject(String testoentity, String identity,
			Long userid, String subject, String token) throws SecurityException,
			ModeratorServiceException {

		ContentToModeratorService messageToMediationService = new ContentToModeratorService(
				webappname, identity, testoentity, subject, String.valueOf(userid));
		addCommentToMediationService(messageToMediationService, token);
	}
	
	public void remoteValidationComment(String testoentity, String identity,
			Long userid, String token) throws SecurityException,
			ModeratorServiceException {

		ContentToModeratorService messageToMediationService = new ContentToModeratorService(
				webappname, identity, testoentity, String.valueOf(userid));

		addCommentToMediationService(messageToMediationService, token);
	}
	

	private void addCommentToMediationService(
			ContentToModeratorService messageToMediationService, String token)
			throws SecurityException, ModeratorServiceException {

		serModeratorService.addContentToManualFilterByApp(token, webappname,
				messageToMediationService);

	}

	/**
	 * Check the definitive status of the specified entities. 
	 * Also deleted the entity from the moderator service data set.
	 * @param ids
	 * @param token
	 * @return
	 */
	public Map<String, Boolean> updateComment(List<String> ids, String token) {
		Map<String,Boolean> result = new HashMap<String, Boolean>();
		if (ids != null) {
			for (String id : ids) {
				try {
					List<ContentToModeratorService> objects = serModeratorService.getContentByObjectId(token, webappname, id);
					if (objects != null && !objects.isEmpty()) {
						ContentToModeratorService  object = objects.get(0);
						boolean keywordApproved = object.isKeywordApproved();
						State manualApproved  = object.getManualApproved();
						if (keywordApproved && manualApproved.equals(State.APPROVED)) {
							result.put(id, true);
						}
						if (!keywordApproved || manualApproved.equals(State.NOT_APPROVED)) {
							result.put(id, true);
						}
					} else {
						// not found: consider approved
						result.put(id, true);
					}
					if (result.containsKey(id)) {
						serModeratorService.deleteByObjectId(token, webappname, id);
					}
				} catch (Exception e) {
					e.printStackTrace();
					return Collections.emptyMap();
				}
			}
		}
		return result;
	}

	// new Version con credintial
	public Map<String, Boolean> updateComment(long fromData, long toData, String token) {

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

	public static void main(String[] args) throws MalformedURLException {
		MediationParserImpl impl = new MediationParserImpl("https://platform.smartcommunitylab.it/core.moderator","ifame", null);
		Map<String, Boolean> updateComment = impl.updateComment(Arrays.asList("4","5"), "1595665b-3157-4dac-9bda-6ec1abd85938");
		System.err.println(updateComment);
	}
}
