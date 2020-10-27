FROM centos:8 AS builder
RUN dnf -y install dejavu-sans-fonts dejavu-serif-fonts fontconfig git java-1.8.0-openjdk-devel && dnf clean all && fc-cache -f
COPY . /tmp/build
WORKDIR /tmp/build
RUN ./activator clean
RUN ./activator -Dconfig.file=modules/ginas/conf/ginas.conf ginas/dist
WORKDIR /opt
RUN jar xf /tmp/build/modules/ginas/target/universal/ginas-*.zip && mv /opt/ginas-* /opt/g-srs
WORKDIR /opt/g-srs
RUN mv /tmp/build/modules/ginas/conf /opt/g-srs/conf
RUN mkdir -p ginas.ix exports logs conf/sql conf/sql/init conf/sql/load conf/sql/post conf/sql/test conf/evolutions/default
RUN sed -i "s/localhost/db/g" conf/ginas-mysql.conf
RUN sed -i "s/localhost:5433/db:5432/g" conf/ginas-postgres.conf
RUN sed -i "s/#evolutionplugin=disabled/evolutionplugin=disabled/g" conf/ginas-*.conf

FROM centos:8
RUN dnf -y install dejavu-sans-fonts dejavu-serif-fonts fontconfig java-1.8.0-openjdk-headless && dnf clean all && fc-cache -f
COPY --from=builder /opt /opt
COPY entrypoint.sh /entrypoint.sh
VOLUME ["/opt/g-srs/ginas.ix", "/opt/g-srs/logs", "/opt/g-srs/exports"]
EXPOSE 9000
ENTRYPOINT ["/entrypoint.sh"]
WORKDIR /opt/g-srs
CMD ["./bin/ginas"]
