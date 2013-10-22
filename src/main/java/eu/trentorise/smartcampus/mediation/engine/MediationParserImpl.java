package eu.trentorise.smartcampus.mediation.engine;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import eu.trentorise.smartcampus.mediation.model.KeyWordPersistent;
import eu.trentorise.smartcampus.mediation.model.MessageToMediationService;
import eu.trentorise.smartcampus.mediation.util.MediationConstant;
import eu.trentorise.smartcampus.mediation.util.TextReader;
import eu.trentorise.smartcampus.network.RemoteConnector;
import eu.trentorise.smartcampus.network.RemoteException;
import eu.trentorise.smartcampus.social.model.User;

public class MediationParserImpl extends JdbcTemplate {
	
	private static final String DEFAULT_KEY_SELECT_STATEMENT = "select * from keywordpersistent ";

	
	private String selectKeySql = DEFAULT_KEY_SELECT_STATEMENT;
	
	private DataSource dataSource;
	private String urlServermediation;
	private String webappname;
	private String client_token = "bearer blabla";

	private static final Logger logger = Logger
			.getLogger(MediationParserImpl.class);

	public MediationParserImpl() {
	}

	public MediationParserImpl(DataSource dataSource,
			String urlServermediation, String webappname) {
		super(dataSource);
		this.dataSource = dataSource;
		this.setUrlServermediation(urlServermediation);
		this.webappname = webappname;
	}

	public boolean fastValidateComment(String testoentity, int identity,
			Long userid, String token) {

		MessageToMediationService messageToMediationService = new MessageToMediationService(
				webappname, identity, testoentity, String.valueOf(userid));
		Collection<KeyWordPersistent> x = loadAppDictionary(); //getNotApprovedWordDictionary();
		Iterator<KeyWordPersistent> index = x.iterator();

		boolean isApproved = true;

		long after = 0;
		long before = 0;
		long diff = 0;

		while (index.hasNext() && isApproved) {

			before = System.currentTimeMillis();
			KeyWordPersistent test = index.next();
			isApproved = (testoentity.indexOf(test.getKey()) == -1);
			if (!isApproved) {
				messageToMediationService.setParseApproved(isApproved);
				messageToMediationService
						.setNote("[Blocked by = " + test + "]");
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
		addCommentToMediationService(messageToMediationService, token);

		return isApproved;

	}

	public boolean remoteValidateComment(String testoentity, int identity,
			Long userid, String token) {

		MessageToMediationService messageToMediationService = new MessageToMediationService(
				webappname, identity, testoentity, String.valueOf(userid));

		boolean isApproved = true;

		messageToMediationService.setParseApproved(isApproved);
		addCommentToMediationService(messageToMediationService, token);

		return isApproved;

	}

	private List<KeyWordPersistent> updateKeyWord(String token, long data) {

		try {
			logger.debug(urlServermediation + MediationConstant.ADD_COMMENT);
			String response = RemoteConnector.getJSON(urlServermediation,
					MediationConstant.GET_KEYWORD + data, token);
			
			List<KeyWordPersistent> keyWordPersistentList=KeyWordPersistent.valueOfList(response);
			
			return keyWordPersistentList;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	private void addCommentToMediationService(
			MessageToMediationService messageToMediationService, String token) {

		try {
			logger.debug(urlServermediation + MediationConstant.ADD_COMMENT);
			RemoteConnector.postJSON(urlServermediation,
					MediationConstant.ADD_COMMENT,
					messageToMediationService.ToJson(), token);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// restituisce la lista delle parole da filtrare
	private Collection<String> getNotApprovedWordDictionary() {
		Collection<String> stringColl = new ArrayList<String>();

		TextReader readerBW = new TextReader();
		stringColl = readerBW.getListFromFiles();

		return stringColl;
	}

	public List<KeyWordPersistent> loadAppDictionary() {
		
		return queryForList(selectKeySql, null, null, KeyWordPersistent.class);
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getUrlServermediation() {
		return urlServermediation;
	}

	public void setUrlServermediation(String urlServermediation) {
		this.urlServermediation = urlServermediation;
	}

	public String getWebappname() {
		return webappname;
	}

	public void setWebappname(String webappname) {
		this.webappname = webappname;
	}

}
