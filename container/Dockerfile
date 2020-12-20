FROM fedora:33

WORKDIR /plugin
COPY entrypoint.sh .

RUN chmod +x entrypoint.sh && \
    dnf install -y git java-1.8.0-openjdk-devel wget && \
    mkdir /buildtools /output && \
    cd /buildtools && \
    wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar && \
    java -jar BuildTools.jar --rev latest && \
    ln -s /buildtools/spigot-*.jar /buildtools/spigot.jar && \
    ls /buildtools/spigot-*.jar | sed -r 's/^.*\/spigot-(.*)\.jar/\1/g' > /output/spigot_version

ENTRYPOINT ["./entrypoint.sh"]
