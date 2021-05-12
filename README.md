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

### Leader boards

`Leader Board` is the functionallity that allow us to have a board with the ranking of top students that uses `X-Mentor`. Top students are those who has more watching time using the platform. To accomplish that, we need to separate two functionallities:

* Register the student progress
* Getting the board

Following is a diagram that shows how the `Student Progress Registration Flow` is implemented:  

![Alt text](diagrams/student-progress-registration.png?raw=true "Student Progress Registration Flow")

First, the `x-mentor` microservices receives the request. Then, it publishes the `Student Progress Registration` Domain Event, which ends up as en element inside `student-progress-registered stream` (which is a `Redis Stream`). `Redis Gears` listen to elements pushed to the stream an perform a sink of this data into our time series database to be available for future calculations. 

![Alt text](diagrams/leader-board.png?raw=true "Leader Board Flow")

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