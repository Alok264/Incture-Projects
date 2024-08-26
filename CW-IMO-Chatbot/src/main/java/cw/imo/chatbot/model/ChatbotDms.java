package cw.imo.chatbot.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "chatbot_dms")
public class ChatbotDms {

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "documentName", nullable = false)
	private String documentName;

	@Column(name = "documentType", nullable = false)
	private String documentType;

	@Column(name = "folderId", nullable = true)
	private String folderId;

	@Column(name = "repoName", nullable = true)
	private String repoName;
}
