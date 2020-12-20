#!/usr/bin/bash

if [[ -z $1 ]]; then
  echo "Usage: $0 <repo-uri>"
  exit 1
fi

REPO_URI="$1"

git clone "$REPO_URI" repo && \
cd repo && \
GIT_SHORTCOMMIT=$(cat .git/refs/heads/master | cut -c 1-7) && \
mkdir build && \
javac -d build -classpath /buildtools/spigot.jar src/paalbra/BedTime/*.java && \
cp *.yml build && \
jar -cvf build/BedTime.jar -C build . && \
SPIGOT_VERSION=$(cat /output/spigot_version) && \
BEDTIME_VERSION=$(cat plugin.yml | grep -oP '(?<=^version: ).*' | tee /output/bedtime_version) && \
BEDTIME_PATH="/output/BedTime-$BEDTIME_VERSION-$GIT_SHORTCOMMIT.jar"
cp build/BedTime.jar "$BEDTIME_PATH" && \
cd .. && \
rm -rf repo && \

echo "Spigot version: $SPIGOT_VERSION"
echo "BedTime version: $BEDTIME_VERSION"
echo "BedTime: $BEDTIME_PATH"
