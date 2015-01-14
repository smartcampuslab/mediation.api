package eu.trentorise.smartcampus.mediation.model;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import eu.trentorise.smartcampus.moderatorservice.model.State;


@MappedSuperclass
public class CommentBaseEntity {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@Column(name = "TESTO")
	private String testo;
	
	@Column(name = "APPROVED")
	@Enumerated(EnumType.STRING) 
	private State approved;
	
	@Column(name = "LASTTIME")
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
	public String getTesto() {
		return testo;
	}
	public void setTesto(String testo) {
		this.testo = testo;
	}
	/**
	 * @return the approved
	 */
	public State getApproved() {
		return approved;
	}
	/**
	 * @param approved the approved to set
	 */
	public void setApproved(State approved) {
		this.approved = approved;
	}
}
