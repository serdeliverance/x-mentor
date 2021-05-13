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
* Notifications
* FullText Search

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

2. Get the topic of the course

3. Look for students enrolled to the same course

4. Look for courses of the same topic when those students are enrolled

5. Recommend those courses.

#### Interest Recommendation Strategy

1. Random select a student interest

2. Look for students that are enrolled to course of that topic

3. Look for other courses of the same topic we students are enrolled in

4. Return the recommended courses (having into account those which the student isn't already enrolled)

#### Discover Recommendation Strategy

1. Get all topics

2. Get student interest topics

3. Get topics the user is enrolled in

4. Get a topic the user is neither interesting nor enrolled

5. Get courses of that topic and recomend them

#### How the graph data is accessed


1. All student's courses

```
TODO comandooooooo
``` 

2. Get topic by course

```
TODO comandoooooo
```

3. Get students that are enrolled (`studying`) a course

```
TODO comandoooooo
```

4. Get courses by topic

```
TODO comandoooooo
```

5. Get student's interests

```
TODO comandoooooo
```

6. Get courses the user is enrolled in by topic

```
TODO comandoooooo
```

7. Get all topics

```
TODO comandoooooo
```

8. Get student interest topics

```
TODO comandoooooo
```

9. Get topics the user is enrolled in

```
TODO comandoooooo
```

### Student Progress Registration

This functionallity allow us to track the time the user spend in the platform watching courses. That info is then used to implement the LeaderBoard.

![Alt text](diagrams/student-progress-registration.png?raw=true "Student Progress Registration Flow")

`x-mentor` microservices receives the request. Then, it publishes the `Student Progress Registration Domain Event`, which ends up as en element inside `student-progress-registered stream` (which is a `Redis Stream`) via the following command:

```
XADD student-progress-registered $timestamp student $student_username duration $duration
```

`Redis Gears` listen to elements pushed to the stream and then sinks this data into `Redis TimeSeries` using the following command:

```
TS.ADD studentprogress:$student_username $timestamp $duration RETENTION 0 LABELS student $student_username
```

### Leaderboard

`Leaderboard` is the functionallity that allow us to have a board with the ranking of top students that uses `X-Mentor`. Top students are those who has more watching time using the platform. To accomplish that, we need to separate two functionallities:

* Register the student progress
* Getting the board

![Alt text](diagrams/leader-board.png?raw=true "Leader Board Flow")

When the user request for the leaderboard data, we first look at `Redis` for the time series keys

```
LRANGE student-progress-list 0 -1		// to retrieve all the list elements
```

For each key, we use `Redis TimeSeries` to get the range of samples in a time window of three months performing sum aggregation. 

```
TS.RANGE $student_key $thee_months_back_timestamp $timestamp AGGREGATION sum 1000
```

where:

	* `student_key ` is the student's time series key. For example: `studentprogress:codi.sipes` is the time series key for student `codi.sipes`.
	* `three_months_back_timestamp` is a `Unix Timestamp` with represents a point in time three months back than `timestamp` (in order to have a time window of three months).
	* `timestamp` the current timestamp (in `Unix Timestamp` format).
	* We perform sum aggregation of the sample values in that time windows using a `Time Bucket` of 1000 milliseconds.

That way we can get the accumulated watching hour of every student. After that we select the highest top 5 accumulated watching hours and retrive that information to visualize the board.

## How to run it locally?

### Prerequisites

### Local installation