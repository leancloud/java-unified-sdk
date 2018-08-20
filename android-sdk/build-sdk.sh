#! /bin/bash

set -e
export PATH=$PATH:/usr/local/bin

if [ $# != 1 ] ; then
	echo "Usage: $0 Version"
	echo "e.g: $0 1.0.0"
	exit 1
fi
version=$1
echo "Building sdk $version..."

sed -i '' "s/VERSION_NAME=.*/VERSION_NAME=$version/" gradle.properties
sed -i '' "s/include ':storage-sample-app'//" settings.gradle
sed -i '' "s/include ':realtime-sample-app'//" settings.gradle

./gradlew clean assemble

releaseDir="build/release-$version"
rm -rf "$releaseDir"
mkdir "$releaseDir"
mkdir -p "$releaseDir/storage-android/libs"
mkdir -p "$releaseDir/realtime-android/libs"

cp storage-android/build/libs/storage-android*.jar $releaseDir/
cp realtime-android/build/libs/realtime-android*.jar $releaseDir/
cp -rf libs/* $releaseDir/

echo "Build sdk $version done!"
