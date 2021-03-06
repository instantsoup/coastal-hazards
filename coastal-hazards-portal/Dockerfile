FROM tomcat:8.0-jre8-alpine

ARG doi_network=false
ARG PORTAL_VERSION=1.1.63
ARG POSTGRES_JDBC_VERSION=42.2.4
ENV KEY_STORE_PASSWORD=changeit

RUN apk add --update openssl

RUN if [ "${doi_network}" = true ]; then \
		mkdir -p /usr/lib/ssl/certs/ && \
		/usr/bin/wget -O /usr/lib/ssl/certs/DOIRootCA.crt http://sslhelp.doi.net/docs/DOIRootCA2.cer && \
		ln -sf /usr/lib/ssl/certs/DOIRootCA.crt /usr/lib/ssl/certs/`openssl x509 -hash -noout -in /usr/lib/ssl/certs/DOIRootCA.crt`.0 && \
		echo "\\n\\nca-certificate = /usr/lib/ssl/certs/DOIRootCA.crt" >> /etc/wgetrc; \
	fi

RUN apk add --update --no-cache \
	openssl \
	curl && \
	rm -rf /var/lib/apt/lists/* && \
  rm /var/cache/apk/*

RUN wget -O /usr/local/tomcat/webapps/probe.war "http://central.maven.org/maven2/com/github/psi-probe/psi-probe-web/3.1.0/psi-probe-web-3.1.0.war"

RUN mkdir -p /usr/local/tomcat/ssl

COPY docker/key-store.jks /usr/local/tomcat/ssl/key-store.jks

COPY docker/key-store.jks /usr/local/tomcat/ssl/trust-store.jks

RUN wget -O /usr/local/tomcat/webapps/coastal-hazards-portal.war "https://cida.usgs.gov/maven/service/local/repositories/cida-public-releases/content/gov/usgs/cida/coastalhazards/coastal-hazards-portal/${PORTAL_VERSION}/coastal-hazards-portal-${PORTAL_VERSION}.war"

RUN curl -o /usr/local/tomcat/lib/postgresql.jar https://jdbc.postgresql.org/download/postgresql-$POSTGRES_JDBC_VERSION.jar

COPY docker/tomcat-users.xml /usr/local/tomcat/conf/tomcat-users.xml
COPY docker/server.xml /usr/local/tomcat/conf/server.xml
COPY docker/setenv.sh /usr/local/tomcat/bin/setenv.sh
COPY docker/tomcat-users.xml /usr/local/tomcat/conf/tomcat-users.xml
COPY docker/context.xml /usr/local/tomcat/conf/context.xml

RUN chmod +x /usr/local/tomcat/bin/setenv.sh

RUN rm -rf /usr/local/tomcat/bin/ROOT

RUN rm -rf /usr/local/tomcat/bin/docs

RUN rm -rf /usr/local/tomcat/bin/examples

HEALTHCHECK --interval=10s --timeout=2s \
	CMD curl -s -k "https://localhost:8443/coastal-hazards-portal/diagnostics" | grep -q '<div name="getServletPath">/diagnostics</div>' || exit 1
