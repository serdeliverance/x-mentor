# x-mentor-core

`WIP`

## Screenshots

`TODO`

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
* leaderboard

## Architecture, Data Model and Domain Events

The following picture gives a high level overview of the system architecture:

![Alt text](diagrams/x-mentor-arch.png?raw=true "Architecture")

Our data model is expressed through nodes and relations using `Redis Graph`. The model is very simple: just `Student`, `Course` and `Topic` entities expressing different kind of relations between each other:

![Alt text](diagrams/graph-model.png?raw=true "Graph model")

It is important to mention `X-Mentor` was implemented following an `Event Driven Architecture` approach in which the following `Domain Events` are considered:

* `student-enrolled`
* `student-interested`
* `student-interest-lost`
* `course-created`
* `course-rated`
* `course-recommended`
* `student-progress-registered`

## How Redis Modules helped us to implement some core features

### Recommendation System

In order to implement a `Recommendation System` that suggest users different kind courses to take, we decided to rely on the power of `Redis Graph`. Searching for relations between nodes in the graph database give us an easy way to implement different king of recommendation strategies.

`TODO image`

### Leader boards

`Leader Board` is the functionallity that allow us to have a board with the ranking of top students that uses `X-Mentor`. Top students are those who has more watching time using the platform. To accomplish that, we need to separate two functionallities:

* Register the student progress
* Getting the board

Following is a diagram that shows how the `Student Progress Registration Flow` is implemented:  

![Alt text](diagrams/student-progress-registration.png?raw=true "Student Progress Registration Flow")

First, the `x-mentor` microservices receives the request. Then, it publishes the `Student Progress Registration Domain Event`, which ends up as en element inside `student-progress-registered stream` (which is a `Redis Stream`). `Redis Gears` listen to elements pushed to the stream and then sinks this data into `Redis TimeSeries` database to be available for further calculations.

![Alt text](diagrams/leader-board.png?raw=true "Leader Board Flow")

When the user request for the leader board data, we first look at `Redis` for the time series keys. For each key, we use `Redis TimeSeries` to get the range of
samples in a time window of three months performing sum aggregation. That way we can get the accumulated watching hour of every student. After that we select the top 5 based on that metric and retrieve the board.

## How it works?

### 1. How the data is stored?

### 2. How the data is accessed?

## How to run it locally?

### Prerequisites


### Local installation