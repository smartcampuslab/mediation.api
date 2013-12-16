package eu.trentorise.smartcampus.mediation.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;


@MappedSuperclass
public class CommentBaseEntity {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@Column(name = "TESTO")
	private String testo;
	
	@Column(name = "APPROVED")
	private boolean approved;
	
	@Column(name = "APPROVED")
	private long timestamp;
	
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
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
