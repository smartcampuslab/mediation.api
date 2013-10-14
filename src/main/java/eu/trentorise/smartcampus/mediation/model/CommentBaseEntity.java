package eu.trentorise.smartcampus.mediation.model;



public class CommentBaseEntity {
	
	private int id;
	private String testo;
	private boolean approved;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public boolean isApproved() {
		return approved;
	}
	public void setApproved(boolean approved) {
		this.approved = approved;
	}
	public String getTesto() {
		return testo;
	}
	public void setTesto(String testo) {
		this.testo = testo;
	}


}
