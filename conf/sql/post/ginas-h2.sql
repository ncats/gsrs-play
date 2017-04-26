CREATE INDEX xref_refid_index on ix_core_xref (refid);
CREATE INDEX xref_kind_index on ix_core_xref (kind);
CREATE INDEX value_label_index on ix_core_value (label);
CREATE INDEX value_term_index on ix_core_value (term);
CREATE INDEX sub_approval_index on ix_ginas_substance (approval_id);
CREATE INDEX name_index on ix_ginas_name (name);
CREATE INDEX code_index on ix_ginas_code (code);
CREATE INDEX code_system_index on ix_ginas_code (code_system);
CREATE INDEX code_code_system_index on ix_ginas_code (code,code_system);
CREATE INDEX ref_id_index on ix_ginas_reference (id);
CREATE INDEX interaction_index on ix_ginas_relationship (interaction_type);
CREATE INDEX qualification_index on ix_ginas_relationship (qualification);
CREATE INDEX type_index on ix_ginas_relationship (type);
CREATE INDEX sub_ref_index on ix_ginas_substanceref (refuuid);
CREATE INDEX relate_originate_index on ix_ginas_relationship (originator_uuid);




CREATE VIEW TEST_SUB AS SELECT uuid, approval_id FROM ix_ginas_substance;


CREATE VIEW TEST_SUB3 AS SELECT uuid||'G123' as uuid, approval_id FROM ix_ginas_substance;
