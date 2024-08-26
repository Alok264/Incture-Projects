package com.imo.workorder.operationTime.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "Workorder_Dms")
@Data
public class WorkOrderDms {
	@Id
	@Column(name = "Id")
	private String Id;

	@Column(name = "DocumentName", nullable = false)
	private String documentName;

	@Column(name = "DocumentType", nullable = false)
	private String documentType;

	@Column(name = "FolderId")
	private String folderId;

	@Column(name = "RepoName")
	private String repoName;
}
