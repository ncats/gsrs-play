ALTER TABLE ix_core_xref
ADD INDEX xref_refid_index (refid ASC),
ADD INDEX xref_kind_index (kind ASC),
add index refid_kind_index (refid asc, kind asc);

ALTER TABLE ix_core_value
ADD INDEX value_label_index (label ASC),
ADD INDEX value_term_index (term ASC),
add index label_term_index (label asc, term asc),
add index sha1_index (sha1 asc),
add index intval_index (intval asc),
add index numval_index (numval asc),
add index lval_index (lval asc),
add index rval_index (rval asc)
;

ALTER TABLE ix_core_predicate
ADD INDEX predicate_index (predicate ASC),
add index subject_pred_index (subject_id asc, predicate asc)
;

alter table ix_idg_disease
add index name_index (name asc)
;

ALTER TABLE ix_idg_target
ADD INDEX target_family_index (idg_family ASC),
ADD INDEX target_tdl_index (idg_tdl ASC),
ADD INDEX target_novelty_index (novelty ASC),
ADD INDEX target_antibody_index (antibody_count ASC),
ADD INDEX target_monoclonal_index (monoclonal_count ASC),
ADD INDEX target_pubmed_index (pubmed_count ASC),
ADD INDEX target_patent_index (patent_count ASC),
ADD INDEX target_grant_index (grant_count ASC),
ADD INDEX target_cost_index (grant_total_cost ASC),
ADD INDEX target_r01_index (r01count ASC)
;

ALTER TABLE ix_idg_harmonogram
ADD INDEX harm_uniprot_index (uniprot_id ASC),
ADD INDEX harm_symbol_index (symbol ASC),
ADD INDEX harm_source_index (data_source asc),
add index harm_type_index (data_type asc),
add index harm_group_index (attr_group asc),
add index harm_attr_type_index (attr_type asc),
add index harm_idgfam_index (idgfamily asc),
add index harm_tdl_index (tdl asc),
add index harm_cdf_index (cdf asc)
;

alter table ix_idg_tinx
add index uniprot_index (uniprot_id asc),
add index doid_index (doid asc),
add index importance_index (importance asc),
add index novelty_index (disease_novelty asc)
;
