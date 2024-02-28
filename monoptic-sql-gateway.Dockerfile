FROM eclipse-temurin:18
EXPOSE 8765
WORKDIR /opt/monoptic-sql-gateway
ADD ./monoptic-sql-gateway/build/distributions/monoptic-sql-gateway.tar /opt/
ENTRYPOINT ["./bin/monoptic-sql-gateway", "-p", "8765", "-u", "jdbc:monoptic:"]
