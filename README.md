# Akka-Http-Kafka Kata

## Task
Implement two microservices which communicate via kafka as messaging and persistance.

The commit service has to have a single endpoint /commit  that takes a POST request in order to commit a booking.

The bookings service has to have also a single endpoint /bookings that handles a GET request  and lists all booked flights of a given user.

## Requirements
* docker
* docker-compose
* sbt 

## Run

Start Kafka in 1st shell
```bash
docker-compose up
```

Start commit service in 2nd shell
```bash
cd commit-service
sbt run
```

Start bookings service in 3rd shell
```bash
cd bookings-service
sbt run
```

Example requests in 4th shell
```bash
# No Content as successful commit
curl -XPOST http://localhost:9000/commit -v -d'{ "userId": "123abc", "persons": [{ "firstname": "Max", "lastname": "Mustermann", "seat": "A5" }], "price": 69.99, "flightnumber": "EZY8124", "provider": "easyjet"}' 

# Bad Request with Error Message
curl -XPOST http://localhost:9000/commit -v -d"{}"

# Internal Server Error because booking failed
curl -XPOST http://localhost:9000/commit -v -d'{ "userId": "123abc", "persons": [{ "firstname": "Alice", "lastname": "Mustermann", "seat": "A5" },{ "firstname": "Bob", "lastname": "Mustermann", "seat": "A6" }, { "firstname": "Charly", "lastname": "Mustermann", "seat": "A7" }], "price": 69.99, "flightnumber": "EZY8124", "provider": "easyjet"}' 

# OK for stored bookings
curl -XPOST http://localhost:9001/bookings?userid=123abc -v

# OK for with empty set for unknown userid
curl -XPOST http://localhost:9001/bookings?userid=unknown -v
```

## Test
Ensure no concurrent running Kafka container because the tests start their own.

Run tests in commit service
```bash
cd commit-service
sbt test
```

Run tests in bookings service
```bash
cd bookings-service
sbt test
```

## Architecture

```
++++++++++++++++++                        +++++++++                       ++++++++++++++++++++
+                +   =================\   +       +   /=================  +                  +
+ Commit-Service +   produces bookings >  + Kafka +  < consumes bookings  + bookings-service + 
+                +   =================/   +       +   \=================  +                  +
++++++++++++++++++                        +++++++++                       ++++++++++++++++++++
                                                                                    |
                                                                                    |
                                                                                 ++++++
                                                                                 + DB +
                                                                                 ++++++
```

When the commit service processed a booking commit it produces kafka messages to store whether the booking commit was successful or not.
* Kafka stores the commits forever and thus is the source of truth persitance layer.
* The bookings service consumes the kafka events and stores only the successful ones in its own persistence (is implemented as in memory cache in this kata but could be any other persistence layer)
* The commit service still can work when bookings-service is down
* The bookings-service still can work if commit-service is down or if connection to kafka is lost
* The bookings-service is eventuelly consistent (If connection to kafka is lost or the newest message isn't processed yet, it still provides all bookings of the past)
* The kafka messages are idempotent. The bookings-service stores no duplicates in the DB and thus can handle the same event twice without negative effects. (Solved in this kata by using a Set)

## Thoughts
* The commit service produces kafka messages for successful and failed commits but the bookings-service just cares about successful messages to show the loose cuopling. (e.g. some kind of reporting service could care about the failures, too)
* The case classes of the commit-service API, kafka messages and bookings-service API each differ by intention to show that each part has its own micro-domain-model. The commit-service API needs a `privider` attribute to know which 3rd party API must be used, but Kafka and bookings-service don't need to care about that. Kafka needs to know about a `successful` flag to distiguish between successes and faiures but the bookings-service just stores successful bookings thus the flag is not necessary.
* In this Kata the DRY principle is violated regarding the case classes of the Kafka messages. In my optionion it is a case by case decision if the case classes should be packaged in a lib or not. If extract it you get DRY on the one hand but create compile-time coupling on the other hand.
* I used play json to de/serialise JSON because I knew it but while implementing the kata I realized I would use `spray-json` within the next service because it integrates better with `akka-http`

## Outlook
* introduce configuration instead of hard coded topic names, ports, ...
* introduce swagger to get always up-to-date documentation of http APIs of commmit-service and bookings-service
* CI: introduce `sbt-native-packager` to create docker images
* CI: after tests for commit-service and bookings-service create snapshots of the docker-containers and use them for local development. Then you get valid fixtures.
* CI: a smoke-test should start commit-service, kafka and bookings-service and do one happy trail (a successfully committed booking should be provided by bookings-service)
* extract kafka producer and consumer code into a lib because it should not be different in different micro services
* in bookings-service: if instead of an in-memory cache a persistent database is used to store the successful bookings the offset of the kafka topic should be stored, too so that after a restart the bookings-service don't need to reprocess all messages again.

