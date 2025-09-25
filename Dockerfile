FROM amazoncorretto:21
COPY target/*.jar app.jar
RUN yum install -y tzdata && \
    rm /etc/localtime && \
    ln -snf /usr/share/zoneinfo/Europe/Moscow /etc/localtime && \
    echo "Europe/Moscow" > /etc/timezone
ENTRYPOINT ["java","-jar","/app.jar"]