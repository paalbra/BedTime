#!/usr/bin/bash

if [[ -z $1 ]]; then
  echo "Usage: $0 <repo-uri>"
  exit 1
fi

REPO_URI="$1"

git clone "$REPO_URI" repo && \
cd repo && \
mkdir build && \
javac -d build -classpath /buildtools/spigot.jar src/paalbra/BedTime/*.java && \
cp *.yml build && \
jar -cvf build/BedTime.jar -C build . && \
cp build/BedTime.jar /output  && \
cat plugin.yml | grep -oP '(?<=^version: ).*' > /output/bedtime_version && \
cd .. && \
rm -rf repo

echo "Spigot version: $(cat /output/spigot_version)"
echo "BedTime version: $(cat /output/bedtime_version)"
echo "BedTime: /output/BedTime.jar"
