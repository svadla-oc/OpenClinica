package org.akaza.openclinica.domain.datamap;

// default package
// Generated Aug 8, 2013 11:32:37 AM by Hibernate Tools 3.4.0.CR1

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.akaza.openclinica.domain.DataMapDomainObject;

/**
 * DnItemDataMap generated by hbm2java
 */
@Entity
@Table(name = "dn_item_data_map")

public class DnItemDataMap extends DataMapDomainObject {

	private DnItemDataMapId dnItemDataMapId;
	private ItemData itemData;
	private DiscrepancyNote discrepancyNote;

	public DnItemDataMap() {
	}

	public DnItemDataMap(DnItemDataMapId id) {
		this.dnItemDataMapId = id;
	}

	public DnItemDataMap(DnItemDataMapId id, ItemData itemData,
			DiscrepancyNote discrepancyNote) {
		this.dnItemDataMapId = id;
		this.itemData = itemData;
		this.discrepancyNote = discrepancyNote;
	}

	@EmbeddedId
	@AttributeOverrides({
			@AttributeOverride(name = "itemDataId", column = @Column(name = "item_data_id")),
			@AttributeOverride(name = "discrepancyNoteId", column = @Column(name = "discrepancy_note_id")),
			@AttributeOverride(name = "columnName", column = @Column(name = "column_name")),
			@AttributeOverride(name = "studySubjectId", column = @Column(name = "study_subject_id")) })
	public DnItemDataMapId getDnItemDataMapId() {
		return this.dnItemDataMapId;
	}

	public void setDnItemDataMapId(DnItemDataMapId id) {
		this.dnItemDataMapId = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_data_id", insertable = false, updatable = false)
	public ItemData getItemData() {
		return this.itemData;
	}

	public void setItemData(ItemData itemData) {
		this.itemData = itemData;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "discrepancy_note_id", insertable = false, updatable = false)
	
	public DiscrepancyNote getDiscrepancyNote() {
		return this.discrepancyNote;
	}

	public void setDiscrepancyNote(DiscrepancyNote discrepancyNote) {
		this.discrepancyNote = discrepancyNote;
	}

	

}
