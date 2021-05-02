# x-mentor-core

`WIP`

## Stack

* Scala/Play Framework/Akka Streams
* React
* Redis Graph
* RediStreams
* Redis Gears
* RediSearch
* Redis Json
* Keycloak

## Main features

* login/logout
* sign up
* interest
* student recommendation
* recommendation (recommendation sent to the user)
* course enrollment
* course finish
* student progress

* bestsellers


## Architecture (first draft)

![Alt text](diagrams/x-mentor-arch.png?raw=true "Architecture")

## Graph model

![Alt text](diagrams/graph-model.png?raw=true "Graph model")

## Domain Events

* `student-enrolled`
* `student-interested`
* `course-created`
* `course-rated`
* `course-recommended`

## Graph relations

* `interested_in`

`(student) -[:interested_in]-> (topic)`

* `has`

`(topic) -[:has]-> (course)`

* `studying`

`(student) -[:studying]-> (course)`

* `rates`

`(student) -[:rates]-> (course)`

## Redis Keys

* `courses:*:levels:*:contents:*:resources:*`
* `users:*`
* `topics:*:courses:*`