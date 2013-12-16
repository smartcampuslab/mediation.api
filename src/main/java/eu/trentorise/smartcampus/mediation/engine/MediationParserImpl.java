package eu.trentorise.smartcampus.mediation.engine;

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

import eu.trentorise.smartcampus.moderatorservice.model.KeyWord;
import eu.trentorise.smartcampus.moderatorservice.model.MessageToMediationService;
import eu.trentorise.smartcampus.moderatorservice.model.Stato;
import eu.trentorise.smartcampus.moderatoservice.ModeratorService;
import eu.trentorise.smartcampus.moderatoservice.exception.ModeratorServiceException;

public class MediationParserImpl extends JdbcTemplate {

	private static final String CREATE_TABLE_KeyWord = "CREATE TABLE IF NOT EXISTS `KeyWord` ( `id`  varchar(250) NOT NULL,`keyword` varchar(45) DEFAULT NULL,`timeupdate` BIGINT DEFAULT 0,  PRIMARY KEY (`id`))";
	private static final String DELETE_FROM_KeyWord = "delete from KeyWord";
	private static final String DEFAULT_KEY_SELECT_STATEMENT = "select id,keyword,timeupdate from KeyWord ";
	private static final String DEFAULT_KEY_TIME_STATEMENT = "SELECT timeupdate FROM KeyWord ORDER BY timeupdate DESC limit 1 ";

	private String selectKeySql = DEFAULT_KEY_SELECT_STATEMENT;
	private String sqlLongKeyTime = DEFAULT_KEY_TIME_STATEMENT;

	private EmbeddedDatabase dataSource;
	private String webappname;
	private ModeratorService serModeratorService;

	private static final Logger logger = Logger
			.getLogger(MediationParserImpl.class);

	public MediationParserImpl() {

	}

	public MediationParserImpl(String urlServermediation, String webappname) {
		super();
		this.dataSource = new EmbeddedDatabaseBuilder().addScript(
				CREATE_TABLE_KeyWord).build();
		this.webappname = webappname;
		super.setDataSource(dataSource);
		serModeratorService = new ModeratorService(urlServermediation);

	}

	public boolean localValidationComment(String testoentity, int identity,
			Long userid, String token) throws SecurityException,
			ModeratorServiceException {

		MessageToMediationService messageToMediationService = new MessageToMediationService(
				webappname, identity, testoentity, String.valueOf(userid));
		Collection<KeyWord> x = loadAppDictionary();
		Iterator<KeyWord> index = x.iterator();

		boolean isApproved = true;

		long after = 0;
		long before = 0;
		long diff = 0;

		while (index.hasNext() && isApproved) {

			before = System.currentTimeMillis();
			KeyWord test = index.next();
			isApproved = (testoentity.indexOf(test.getKeyword()) == -1);
			if (!isApproved) {
				messageToMediationService.setParseApproved(isApproved);
				messageToMediationService.setNote("[Blocked by = "
						+ test.getKeyword() + "]");
				messageToMediationService
						.setMediationApproved(Stato.NOT_REQUEST);
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
		messageToMediationService.setParseApproved(isApproved);
		messageToMediationService.setMediationApproved(Stato.NOT_REQUEST);
		addCommentToMediationService(messageToMediationService, token);

		return isApproved;

	}

	public boolean remoteValidationComment(String testoentity, int identity,
			Long userid, String token) throws SecurityException,
			ModeratorServiceException {

		MessageToMediationService messageToMediationService = new MessageToMediationService(
				webappname, identity, testoentity, String.valueOf(userid));

		boolean isApproved = true;

		messageToMediationService.setParseApproved(isApproved);
		addCommentToMediationService(messageToMediationService, token);

		return isApproved;

	}

	public boolean updateKeyWord(String token) throws ModeratorServiceException {

		try {

			Collection<KeyWord> KeyWordList = serModeratorService
					.getAllKeywordFilterContent(token, webappname);

			Iterator<KeyWord> index = KeyWordList.iterator();

			logger.info("key list size " + KeyWordList.size());

			execute(DELETE_FROM_KeyWord);

			while (index.hasNext()) {
				KeyWord key = index.next();
				execute("INSERT INTO KeyWord VALUES (\"" + key.getId()
						+ "\",\"" + key.getKeyword() + "\","
						+ key.getTimeupdate() + ")");
			}

			return true;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}

	private void addCommentToMediationService(
			MessageToMediationService messageToMediationService, String token)
			throws SecurityException, ModeratorServiceException {

		serModeratorService.addContentToManualFilterByApp(token, webappname,
				messageToMediationService);

	}

	// new Version con credintial
	public Map<String, Boolean> updateComment(long fromData, long toData,
			String token) {

		try {

			List<MessageToMediationService> listMessgae = serModeratorService
					.getContentByDateWindow(token, webappname, fromData, toData);

			Map<String, Boolean> returnMap = new HashMap<String, Boolean>();

			Boolean resultApprove = false;
			Iterator<MessageToMediationService> index = listMessgae.iterator();

			while (index.hasNext()) {
				MessageToMediationService object = index.next();

				resultApprove = object.isParseApproved();
				Stato valueBool = object.getMediationApproved();

				// if both of filters are true message moderation was positive
				resultApprove = resultApprove
						&& (valueBool.compareTo(Stato.APPROVED) == 0
								|| valueBool.compareTo(Stato.WAITING) == 0 || valueBool
								.compareTo(Stato.NOT_REQUEST) == 0); // object.getBoolean("parseApproved")&&

				returnMap.put(String.valueOf(object.getEntityId()),
						resultApprove);
			}

			return returnMap;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public List<KeyWord> loadAppDictionary() {
		try {
			List<KeyWord> listKeys = query(selectKeySql,
					new BeanPropertyRowMapper<KeyWord>(KeyWord.class));
			return listKeys;

		} catch (Exception x) {
			x.printStackTrace();
			return new ArrayList<KeyWord>();
		}
	}

	public String getWebappname() {
		return webappname;
	}

	public void setWebappname(String webappname) {
		this.webappname = webappname;
	}

}
