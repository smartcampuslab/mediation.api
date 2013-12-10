package eu.trentorise.smartcampus.mediation.util;

public class MediationConstant {

	public static final String ADD_COMMENT = "/rest/comment/app/";
	public static final String GET_COMMENT = "/rest/comment/data/";
	public static final String GET_KEYWORD(String app){
		return "/rest/key/"+app+"/all";
	}

}
