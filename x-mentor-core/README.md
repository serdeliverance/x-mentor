# x-mentor-core

`WIP`

## Stack

* Play Framework
* Akka Streams
* Redis Graph
* Redis pub/sub
* RediStreams
* Redis Gears
* RediSearch
* Redis Json
* Redis IA
* Keycloak
* React

## Main features

* login
* interest
* student recommendation
* recommendation (recommendation sent to the user)
* course enrollment
* course finish
* student progress

* notifications (new courses)

* course CRUD
* student CRUD

* bestsellers (possible case for IA recommendation module)

* tutor chat

## Architecture (first draft)

![Alt text](diagrams/x-mentor-arch.png?raw=true "Architecture")

## Graph model

![Alt text](diagrams/graph-model.png?raw=true "Graph model")

## Domain Events

* `student-enrolled`
* `student-interested`
* `course-completed`
* `course-created`
* `course-updated`
* `course-rated`
* `course-recommended`
* `teacher-registered`

## Graph relations

* `interested_in`

`(student) -[:interested_in]-> (topic)`

* `has`

`(topic) -[:has]-> (course)`

* `studying`

`(student) -[:studying_or_has_finished]-> (course)`

* `rates`

`(student) -[:rates]-> (course)`

maybe tag relation with `recommends` if rate > 3

* `teaches`

`(student) -[:teaches]-> (course)`

## Redis Keys

* `courses:*:levels:*:contents:*:resources:*`
* `users:*`
* `topics:*:courses:*`