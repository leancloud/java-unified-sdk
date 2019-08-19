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
#sed -i '' "s/SDK_VERSION = .*;/SDK_VERSION = \"$version\";/" leancloud-push-lite/src/main/java/cn/leancloud/push/lite/AVOSCloud.java
sed -i '' "s/include ':storage-sample-app'//" settings.gradle
sed -i '' "s/include ':realtime-sample-app'//" settings.gradle
sed -i '' "s/include ':push_lite_demo'//" settings.gradle

./gradlew clean assemble uploadArchives

releaseDir="build/release-$version"
mkdir -p "$releaseDir"

cp storage-android/build/libs/storage-android-*.jar $releaseDir/
cp realtime-android/build/libs/realtime-android-*.jar $releaseDir/
cp mixpush-android/build/libs/mixpush-android-*.jar $releaseDir/
cp leancloud-fcm/build/libs/leancloud-fcm-*.jar $releaseDir/
cp leancloud-push-lite/build/libs/leancloud-push-lite-*.jar $releaseDir/
cp -rf libs/* $releaseDir/
cp -rf mixpush-android/libs/* $releaseDir/

echo "Build sdk $version done!"
