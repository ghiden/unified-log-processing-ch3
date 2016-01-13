# Unified Log Processing Chapter 3 in Clojure

This is a rewrite of the code example in Chapter 3 of Unified Log Processing.

## Preparation

Install zookeeper and kafka

```bash
brew install zookeeper
brew install kafka
```

Download MaxMind geo-IP database
```bash
curl -O "http://geolite.maxmind.com/download/geoip/database/GeoLiteCity.dat.gz"
gunzip GeoLiteCity.dat.gz
mv GeoLiteCity.dat resources
```

## Run

Start zookeeper
```bash
zkServer start [full path]/config/zookeeper.properties
```

Start kafka
```bash
kafka-server-start.sh config/server.properties
```

Create topics
```bash
kafka-topics.sh --create --topic raw-events --zookeeper localhost:2181 --replication-factor 1 --partitions 1
kafka-topics.sh --create --topic enriched-events --zookeeper localhost:2181 --replication-factor 1 --partitions 1
kafka-topics.sh --create --topic bad-events --zookeeper localhost:2181 --replication-factor 1 --partitions 1
```

Open event consumers in different terminals
```bash
kafka-console-consumer.sh --topic enriched-events --from-beginning --zookeeper localhost:2181
```

```bash
kafka-console-consumer.sh --topic bad-events --from-beginning --zookeeper localhost:2181
```

Start app
```bash
lein run
```

Open raw event producer
```bash
kafka-console-producer.sh --topic raw-events --broker-list localhost:9092
```

Enter events to the raw event producer
```json
{"event": "SHOPPER_VIEWED_PRODUCT", "shopper": { "id": "123", "name": "Jane", "ipAddress": "70.46.123.145" }, "product": { "sku": "aapl-001", "name": "iPad" }, "timestamp": "2015-07-03T12:01:35Z" }
{"event": "SHOPPER_VIEWED_PRODUCT", "shopper": { "id": "456", "name": "Mo", "ipAddress": "89.92.213.32" }, "product": { "sku": "sony-072", "name":"Widescreen TV" }, "timestamp": "2015-07-03T12:03:45Z" }
{"event": "SHOPPER_VIEWED_PRODUCT", "shopper": { "id": "789", "name": "Justin", "ipAddress": "97.107.137.164" }, "product": { "sku": "ms-003", "name": "XBox One" }, "timestamp": "2015-07-03T12:05:05Z" }
not json
{ "event": "SHOPPER_VIEWED_PRODUCT", "shopper": { "id": "458", "name": "Mo", "ipAddress": "not an ip address" }, "product": { "sku": "sony-072", "name": "Widescreen TV" }, "timestamp": "2015-07-03T12:03:45Z" }
{ "event": "SHOPPER_VIEWED_PRODUCT", "shopper": {}, "timestamp": "2015-07-03T12:05:05Z" }
```

You should be able to see 3 events in enriched-events and 3 other ones in bad-events.
