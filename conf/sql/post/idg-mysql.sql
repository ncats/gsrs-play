ALTER TABLE ix_core_xref
ADD INDEX xref_refid_index (refid ASC),
ADD INDEX xref_kind_index (kind ASC);

ALTER TABLE ix_core_value
ADD INDEX value_label_index (label ASC),
ADD INDEX value_term_index (term ASC);

ALTER TABLE ix_core_predicate
ADD INDEX predicate_index (predicate ASC);

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
