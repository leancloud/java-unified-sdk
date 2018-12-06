#!/bin/bash

set -e

export PATH=$PATH:/usr/local/bin

if  [ $# != 1 ]; then
	echo "Usage: $0 VERSION"
	echo " e.g.: $0 1.0.0"
	exit 1;
fi
version=$1
sed -i '' "s/SDK_VERSION = .*;/SDK_VERSION = \"$version\";/" core/src/main/java/cn/leancloud/core/AppConfiguration.java

git add core/src/main/java/cn/leancloud/core/AppConfiguration.java

git ci -m "version bump"

git push origin master

mvn clean

mvn --batch-mode  release:clean release:prepare release:perform
