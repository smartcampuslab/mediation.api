package eu.trentorise.smartcampus.mediation.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import eu.trentorise.smartcampus.mediation.model.CommentBaseEntity;
import eu.trentorise.smartcampus.mediation.model.KeyWordPersistent;
import eu.trentorise.smartcampus.mediation.model.MessageToMediationService;
import eu.trentorise.smartcampus.mediation.model.Stato;
import eu.trentorise.smartcampus.mediation.util.MediationConstant;
import eu.trentorise.smartcampus.network.RemoteConnector;
import eu.trentorise.smartcampus.network.RemoteException;

public class MediationParserImpl extends JdbcTemplate {

	private static final String CREATE_TABLE_KEYWORDPERSISTENT = "CREATE TABLE IF NOT EXISTS `keywordpersistent` ( `id`  varchar(250) NOT NULL,`keyword` varchar(45) DEFAULT NULL,`timeupdate` BIGINT DEFAULT 0,  PRIMARY KEY (`id`))";
	private static final String DELETE_FROM_KEYWORDPERSISTENT = "delete from keywordpersistent";
	private static final String DEFAULT_KEY_SELECT_STATEMENT = "select id,keyword,timeupdate from keywordpersistent ";
	private static final String DEFAULT_KEY_TIME_STATEMENT = "SELECT timeupdate FROM keywordpersistent ORDER BY timeupdate DESC limit 1 ";

	private String selectKeySql = DEFAULT_KEY_SELECT_STATEMENT;
	private String sqlLongKeyTime = DEFAULT_KEY_TIME_STATEMENT;

	private DataSource dataSource;
	private String urlServermediation;
	private String webappname;

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

	public boolean localValidationComment(String testoentity, int identity,
			Long userid, String token) {

		MessageToMediationService messageToMediationService = new MessageToMediationService(
				webappname, identity, testoentity, String.valueOf(userid));
		Collection<KeyWordPersistent> x = loadAppDictionary();
		Iterator<KeyWordPersistent> index = x.iterator();

		boolean isApproved = true;

		long after = 0;
		long before = 0;
		long diff = 0;

		while (index.hasNext() && isApproved) {

			before = System.currentTimeMillis();
			KeyWordPersistent test = index.next();
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
			Long userid, String token) {

		MessageToMediationService messageToMediationService = new MessageToMediationService(
				webappname, identity, testoentity, String.valueOf(userid));

		boolean isApproved = true;

		messageToMediationService.setParseApproved(isApproved);
		addCommentToMediationService(messageToMediationService, token);

		return isApproved;

	}

	public boolean updateKeyWord(String token) {

		try {
			long lastTime = getLastKeyWordTime();

			logger.debug(urlServermediation
					+ MediationConstant.GET_KEYWORD(webappname));
			String response = RemoteConnector.getJSON(urlServermediation,
					MediationConstant.GET_KEYWORD(webappname), token);

			Collection<KeyWordPersistent> keyWordPersistentList = KeyWordPersistent
					.valueOfList(response);
			Iterator<KeyWordPersistent> index = keyWordPersistentList
					.iterator();

			logger.info("key list size " + keyWordPersistentList.size());

			execute(DELETE_FROM_KEYWORDPERSISTENT);

			while (index.hasNext()) {
				KeyWordPersistent key = index.next();
				execute("INSERT INTO keywordpersistent VALUES (\""
						+ key.getId() + "\",\"" + key.getKeyword() + "\","
						+ key.getTimeupdate() + ")");
			}

			return true;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}

	private long getLastKeyWordTime() {
		try {
			return queryForLong(sqlLongKeyTime);
		} catch (Exception x) {

			logger.warn("Empty key table,reload all");
			execute(CREATE_TABLE_KEYWORDPERSISTENT);
			return 0;
		}
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

	public Collection<CommentBaseEntity> updateCommentToMediationService(Collection<CommentBaseEntity> list,String token) {

		try {
			logger.debug(urlServermediation + MediationConstant.GET_COMMENT);
			String response = RemoteConnector.getJSON(urlServermediation,
					MediationConstant.GET_COMMENT + System.currentTimeMillis()
							+ "/" + webappname, token);
			Map<String, Boolean> returnMap = new HashMap<String, Boolean>();

			Boolean resultApprove=false;
			JSONArray array = new JSONArray(response);
			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);

				resultApprove=object.getBoolean("parseApproved");
				String valueBool=object.getString("mediatioApproved");
				
				// if both of filters are true message moderation was positive
				resultApprove = resultApprove &&  (valueBool.compareTo(
								Stato.APPROVED.toString()) == 0 || valueBool.compareTo(
										Stato.WAITING.toString()) == 0 || valueBool.compareTo(
												Stato.NOT_REQUEST.toString()) == 0 ); //object.getBoolean("parseApproved")&&
	
				returnMap.put(object.getString("entityId"), resultApprove);
			}
			
			
			for(CommentBaseEntity commentBaseEntity : list){
				if(returnMap.containsKey(commentBaseEntity.getId())){
					commentBaseEntity.setApproved(returnMap.get(commentBaseEntity.getId()));
				}
				
			}
			

			return list;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	//new Version con credintial
	public Collection<CommentBaseEntity> updateCommentToMediationServiceClientCredential(Collection<CommentBaseEntity> list,String token) {

		try {
			logger.debug(urlServermediation + MediationConstant.GET_COMMENT);
			String response = RemoteConnector.getJSON(urlServermediation,
					MediationConstant.GET_COMMENT + System.currentTimeMillis()
							+ "/" + webappname, token);
			Map<String, Boolean> returnMap = new HashMap<String, Boolean>();

			Boolean resultApprove=false;
			JSONArray array = new JSONArray(response);
			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);

				resultApprove=object.getBoolean("parseApproved");
				String valueBool=object.getString("mediatioApproved");
				
				// if both of filters are true message moderation was positive
				resultApprove = resultApprove &&  (valueBool.compareTo(
								Stato.APPROVED.toString()) == 0 || valueBool.compareTo(
										Stato.WAITING.toString()) == 0 || valueBool.compareTo(
												Stato.NOT_REQUEST.toString()) == 0 ); //object.getBoolean("parseApproved")&&
	
				returnMap.put(object.getString("entityId"), resultApprove);
			}
			
			
			for(CommentBaseEntity commentBaseEntity : list){
				if(returnMap.containsKey(commentBaseEntity.getId())){
					commentBaseEntity.setApproved(returnMap.get(commentBaseEntity.getId()));
				}
				
			}
			

			return list;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public List<KeyWordPersistent> loadAppDictionary() {
		try {
			List<KeyWordPersistent> listKeys = query(selectKeySql,
					new BeanPropertyRowMapper<KeyWordPersistent>(
							KeyWordPersistent.class));
			return listKeys;

		} catch (Exception x) {
			x.printStackTrace();
			return new ArrayList<KeyWordPersistent>();
		}
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
