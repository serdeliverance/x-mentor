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

* Login/Logout
* Sign Up
* Interest
* Student Recommendation
* Recommendation System
* Course Enrollment
* Student Progress Registration
* Leaderboard

## Architecture, Data Model and Domain Events

The following picture gives a high level overview of the system architecture:

![Alt text](diagrams/x-mentor-arch.png?raw=true "Architecture")

Our data model is expressed through nodes and relations using `Redis Graph`. The model is very simple: just `Student`, `Course` and `Topic` entities expressing different kind of relations between each other.

![Alt text](diagrams/graph-model.png?raw=true "Graph model")

`X-Mentor` follows an `Event Driven Architecture` approach in which the following `Domain Events` are considered:

* `student-enrolled`
* `student-interested`
* `student-interest-lost`
* `course-created`
* `course-rated`
* `course-recommended`
* `student-progress-registered`

## How it works?

### Recommendation System

In order to implement a `Recommendation System` that suggest users different kind courses to take, we decided to rely on the power of `Redis Graph`. Searching for relations between nodes in the graph database give us an easy way to implement different king of recommendation strategies.

#### Enrolled Recommendation Strategy

1. Random select a course the student is enrolled in

```
TODO comandooooooo
``` 

2. Get the topic of the course

```
TODO comandoooooo
```

3. Look for students enrolled to the same course

```
TODO comandoooooo
```

4. Look for courses of the same topic when those students are enrolled

```
TODO comandoooooo
```

5. Recommend those courses.

#### Interest Recommendation Strategy

1. Random select a student interest

```
TODO comandoooooo
```

2. Look for students that are enrolled to course of that topic

```
TODO comandoooooo
```

3. Look for other courses of the same topic we students are enrolled in


4. Return the recommended courses (having into account those which the student isn't already enrolled)

#### Discover Recommendation Strategy

1. Get all topics

```
TODO comandoooooo
```

2. Get student interest topics

```
TODO comandoooooo
```

3. Get topics the user is enrolled in

```
TODO comandoooooo
```

4. Get a topic the user is neither interesting nor enrolled

5. Get courses of that topic and recomend them

```
TODO comandoooooo
```

### Student Progress Registration

This functionallity allow us to track the time the user spend in the platform watching courses. That info is then used to implement the LeaderBoard.

![Alt text](diagrams/student-progress-registration.png?raw=true "Student Progress Registration Flow")

`x-mentor` microservices receives the request. Then, it publishes the `Student Progress Registration Domain Event`, which ends up as en element inside `student-progress-registered stream` (which is a `Redis Stream`) via the following command:

```
TODO comandooooooooo
```

`Redis Gears` listen to elements pushed to the stream and then sinks this data into `Redis TimeSeries` using the following command:

```
TODO comandoooooooo
```

### Leaderboard

`Leaderboard` is the functionallity that allow us to have a board with the ranking of top students that uses `X-Mentor`. Top students are those who has more watching time using the platform. To accomplish that, we need to separate two functionallities:

* Register the student progress
* Getting the board

![Alt text](diagrams/leader-board.png?raw=true "Leader Board Flow")

When the user request for the leaderboard data, we first look at `Redis` for the time series keys

```
TODO comandoooooooo
```

For each key, we use `Redis TimeSeries` to get the range of
samples in a time window of three months performing sum aggregation. 

```
TODO comandooooooo
```

That way we can get the accumulated watching hour of every student. After that we select the top 5 based on that metric and retrieve the board.

## How to run it locally?

### Prerequisites

### Local installation