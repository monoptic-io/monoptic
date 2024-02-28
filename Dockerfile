FROM eclipse-temurin:18
WORKDIR /opt/monoptic-jdbc-cli
ADD ./monoptic-jdbc-cli/build/distributions/monoptic-jdbc-cli.tar /opt/
COPY ./etc/* /opt/etc/
ENTRYPOINT ["./bin/monoptic-jdbc-cli"]
