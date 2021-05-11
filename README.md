# x-mentor-core

`WIP`

## Stack

* Scala/Play Framework/Akka Streams
* React
* Redis Graph
* RediStreams
* Redis Blooms
* Redis Gears
* RediSearch
* Redis Json
* Redis TimeSeries
* Keycloak

## Main features

* login/logout
* sign up
* interest
* student recommendation
* recommendation system
* course enrollment
* student progress
* leader boards

## Architecture

![Alt text](diagrams/x-mentor-arch.png?raw=true "Architecture")

## Graph model

![Alt text](diagrams/graph-model.png?raw=true "Graph model")

## Domain Events

* `student-enrolled`
* `student-interested`
* `student-interest-lost`
* `course-created`
* `course-rated`
* `course-recommended`
* `student-progress-registered`

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

### Bloom filters

* courses
* users

### Graphs

* xmentor
	- topics
	- users
	- courses 

### Json

* `courses:{n}`

### Keys

* public-key
* course-last-index
* student-progress-list