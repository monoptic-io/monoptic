FROM flink:1.18
LABEL org.opencontainers.image.source=https://github.com/monoptic-io/monoptic
LABEL org.opencontainers.image.description="Monoptic runtime for Apache Flink"
LABEL org.opencontainers.image.licenses=MIT
ADD ./monoptic-flink-runtime/build/libs/monoptic-flink-runtime-all.jar /opt/flink/lib
