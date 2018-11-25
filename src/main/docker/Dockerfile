FROM openjdk:jre-alpine
MAINTAINER Rijk J.C. van Haaften <R.J.C.vanHaaften@gmail.com>
VOLUME /tmp
ARG JAR
ADD maven/$JAR /opt/app.jar
ENTRYPOINT ["java", "-jar", "/opt/app.jar"]
EXPOSE 8080
# docker run -p 8080:8080 -i tehilim/psalmproject:0.1.0
# On windows, make sure to run this as administrator to allow binding the port and
# access using ip-address (not localhost!), eg. http://10.0.75.1:8080/BAG/1234
