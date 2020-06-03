FROM maven:3.5-jdk-8-alpine as mvnBuilder
MAINTAINER Ranjith K <ranjith.masthikatte@gmail.com>

WORKDIR /fuseki-server-build
COPY . /fuseki-server-build
RUN rm /fuseki-server-build/Dockerfile
RUN mvn -Drat.skip=true clean install

WORKDIR /fuseki-server-build/apache-jena-fuseki/target/
RUN mkdir apache-jena-jar
RUN find . -name 'apache-jena-fuseki*.tar.gz' -exec mv {} apache-jena-jar/ \;

WORKDIR /fuseki-server-build/apache-jena-fuseki/target/apache-jena-jar
RUN find . -name 'apache-jena-fuseki*.tar.gz' -exec tar -xvf {} --strip 1 \;
RUN mkdir run/
RUN cp /fuseki-server-build/shiro.ini run/

FROM openjdk:8-jre-alpine

WORKDIR /fuseki-server

COPY --from=mvnBuilder /fuseki-server-build/apache-jena-fuseki/target/apache-jena-jar/ .

EXPOSE ${port}
CMD ["java", "-jar", "fuseki-server.jar", "--port", "${port}"]