rm -rf logs/application.log pharos.ix/h2/ pharos.ix/structure/ pharos.ix/text/ && ( echo "drop schema ix_idg; create schema ix_idg" | mysql -uroot )
