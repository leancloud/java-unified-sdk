#! /bin/sh

set -e

echo "test NorthChina Production..."

export APP_ID=ohqhxu3mgoj2eyj6ed02yliytmbes3mwhha8ylnc215h0bgk
export APP_KEY=6j8fuggqkbc5m86b8mp4pf2no170i5m7vmax5iypmi72wldc
export APP_REGION=NorthChina
export API_HOST=

cd core && mvn clean test

echo "test NorthChina Staging..."

export APP_ID=ohqhxu3mgoj2eyj6ed02yliytmbes3mwhha8ylnc215h0bgk
export APP_KEY=6j8fuggqkbc5m86b8mp4pf2no170i5m7vmax5iypmi72wldc
export APP_REGION=NorthChina
export API_HOST=https://cn-stg1.leancloud.cn

#cd core && mvn clean test

echo "test EastChina Production..."

export APP_ID=qwTQb5S80beMUMGg3xtHsEka-9Nh9j0Va
export APP_KEY=MAdcyBOm2vGB9Cr36xhPlqDA
export APP_REGION=EastChina
export API_HOST=

cd core && mvn clean test

echo "test NorthAmerica Production..."

export APP_ID=QvNM6AG2khJtBQo6WRMWqfLV-gzGzoHsz
export APP_KEY=be2YmUduiuEnCB2VR9bLRnnV
export APP_REGION=NorthAmerica
export API_HOST=

cd core && mvn clean test

