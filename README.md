# X-Mentor - Where Heroes Learn

X-Mentor is an e-Learning platform which not only tries to connect students and teachers, but also ideas, emotions and knowledge. We want people to progress and learn how to use their powers, because everyone has something to teach and everyone has something to learn, but everyone has a power and remember, with great power comes great responsability.

## Screenshots

![Home Page](https://github.com/serdeliverance/x-mentor/blob/develop/screenshots/HomePage.png?raw=true)

![Course List Page](https://github.com/serdeliverance/x-mentor/blob/develop/screenshots/CourseListPage.png?raw=true)

![Course Page](https://github.com/serdeliverance/x-mentor/blob/develop/screenshots/CoursePage.png?raw=true)

## Table of Contents

- [X-Mentor - Where Heroes Learn](#x-mentor---where-heroes-learn)
  * [Screenshots](#screenshots)
  * [Stack](#stack)
  * [Main features](#main-features)
  * [Architecture, Data Model and Domain Events](#architecture--data-model-and-domain-events)
  * [How it works](#how-it-works)
    + [Login](#login)
    + [Sign Up](#sign-up)
    + [Course Creation](#course-creation)
    + [Course Enrollment](#course-enrollment)
    + [Course Review (Rating)](#course-review--rating-)
    + [Courses Search](#courses-search)
      - [All](#all)
      - [By ID](#by-id)
      - [By Student](#by-student)
    + [Student's Interests](#student-s-interests)
    + [Course Recommendation System](#course-recommendation-system)
      - [Enrolled Recommendation Strategy](#enrolled-recommendation-strategy)
      - [Interest Recommendation Strategy](#interest-recommendation-strategy)
      - [Discover Recommendation Strategy](#discover-recommendation-strategy)
      - [How the graph data is accessed](#how-the-graph-data-is-accessed)
    + [Student Progress Registration](#student-progress-registration)
    + [Leaderboard](#leaderboard)
  * [How to run it locally? Run the *docker-compose.yml*](#how-to-run-it-locally--run-the--docker-composeyml-)
    + [Prerequisites](#prerequisites)
    + [Start](#start)

## Stack

* Scala/Play Framework/Akka Streams
* React
* Redis Graph
* RediStreams
* Redis Blooms
* Redis Gears
* RediSearch
* Redis JSON
* Redis TimeSeries
* Keycloak

## Main features

* Login
* Sign Up
* Course Creation
* Course Enrollment
* Course Review
* Course Search
* Course Recommendation System
* Student's Interests
* Student Progress Registration
* Leaderboard
* Real Time Course Creation Notifications

## Architecture, Data Model and Domain Events

The following picture gives a high level overview of the system architecture:

![Architecture](https://github.com/serdeliverance/x-mentor/blob/develop/diagrams/x-mentor-arch.png?raw=true)

Our data model is expressed through nodes and relations using `Redis Graph`. The model is very simple: just `Student`, `Course` and `Topic` entities expressing different kind of relations between each other.

![Graph model](https://github.com/serdeliverance/x-mentor/blob/develop/diagrams/graph-model.png?raw=true)

`X-Mentor` follows an `Event Driven Architecture` approach in which the following `Domain Events` are considered:

* `student-enrolled`
* `student-interested`
* `student-interest-lost`
* `course-created`
* `course-rated`
* `course-recommended`
* `student-progress-registered`

## How it works

### Login

Starts the authentication process against Keycloak

1. Verifies if user's username already exists in `users` bloom filter
2. Gets auth token

* Verifies if username already exists in `users` bloom filter
```
BF.EXISTS users '${student.username}'
``` 

![Login](https://github.com/serdeliverance/x-mentor/blob/develop/diagrams/login.png?raw=true)

### Sign Up

1. Registering user against Keycloak
2. Adds user's username to `users` bloom filter
3. Creates user in redisGraph
4. Add student's timeseries key (needed for registering student progress)

![Sign Up](https://github.com/serdeliverance/x-mentor/blob/develop/diagrams/sign-up.png?raw=true)

* Adds username to `users` bloom filter

```
BF.ADD users '${student.username}'
```

* Creates student into the graph

```
GRAPH.QUERY xmentor "CREATE (:Student {username: '${student.username}', email: '${student.email}'})"
```

* Creates student progress timeseries key

```
TS.CREATE studentprogress:${username} RETENTION 0 LABELS student ${username}
```

### Course Creation

Creates a course which is going to be stored as a JSON in redisJSON

1. Gets the last course id from redis key `course-last-index`
2. Increases course id key in 1
3. Stores course as JSON in redisJSON
4. Adds course id to `courses` bloom filter
5. Creates course in the graph
6. Publishes `course-created` event which sends notifications by Server Sent Event to the frontend

![Course Creation](https://github.com/serdeliverance/x-mentor/blob/develop/diagrams/course-creation.png?raw=true)

* Gets the last course id from redis key `course-last-index`
```
GET course-last-index
``` 

* Increases course id key in 1
```
INCR course-last-index
``` 

* Stores course as JSON in redisJSON
```
JSON.SET course:${course.id} . '${course.asJson}'
``` 

* Adds course id to `courses` bloom filter
```
BF.ADD coourses '${course.id}'
``` 

* Creates course in the graph
```
GRAPH.QUERY xmentor "CREATE (:Course {name: '${course.title}', id: '${course.id.get}', preview: '${course.preview}'})"
``` 

* Publishes `course-created` event which sends notifications by Server Sent Event to the frontend
```
XADD course-created $timestamp title ${course.title} topic ${course.topic}
``` 
    
### Course Enrollment

Enrolls a student in a specific course

1. Verifies if a student exists in `users` bloom filter
2. Gets course as JSON from redisJSON
3. Creates studying relation between the student and the course in redisGraph

![Course Enrollment](https://github.com/serdeliverance/x-mentor/blob/develop/diagrams/course-enrollment.png?raw=true)

* Verifies if a student exists in `users` bloom filter
```
BF.EXISTS users ${student.username}
``` 

* Gets course as JSON from redisJSON
```
JSON.GET course:${course.id}
``` 

* Creates studying relation between the student and the course in redisGraph
```
GRAPH.QUERY xmentor "MATCH (s:Student), (c:Course) WHERE s.username = '${studying.student}' AND c.name = '${studying.course}' CREATE (s)-[:studying]->(c)"
``` 

### Course Review (Rating)

It is the functionallity that allows a student to rate a course. For that purpose, it do the following:

1. Verifies if a studying relation exists between the student and the course
2. Verifies that a rates relation does not exists between the student and the course
3. Creates the rate realation (see the diagramn below) in the graph.
4. Publish event `course-rated` stream

The following diagram shows the interaction with `Redis Graph` and `Redis Streams`

![Course Review](https://github.com/serdeliverance/x-mentor/blob/develop/diagrams/course-review.png?raw=true)

The commands are used:

* Get courses by student

```
GRAPH.QUERY xmentor "MATCH (student)-[:studying]->(course) where student.username = '$student' RETURN course"
``` 

* Get courses rated by user

```
GRAPH.QUERY xmentor "MATCH (student)-[:rates]->(course) where student.username ='$student' RETURN course"
```

* Create rates relation in the graph

```
GRAPH.QUERY xmentor "MATCH (s:Student), (c:Course) WHERE s.username = '${rating.student}' AND c.name = '${rating.course}' CREATE (s)-[:rates {rating:${rating.stars}}]->(c)"
```

* Publish event to `course-rated` stream

```
XADD course-rated $timestamp student $student_username course $course starts $stars
```

### Course Search

#### All
Retrieves courses by query from redisJSON with rediSearch

```
FT.SEARCH courses-idx ${query}*
``` 

#### By ID

```
BF.EXISTS courses ${course.id}

JSON.GET course:${course.id}
``` 

#### By Student

```
GRAPH.QUERY xmentor "MATCH (student)-[:studying]->(course) where student.username = '$student' RETURN course"

FT.SEARCH courses-idx ${course.title}
```

### Student's Interests

1. Gets all interested relations from redisGraph
2. Gets difference between already existed relations and new ones (it allow us to separate new interests from existing ones and also to identify lost of interest)
3. Creates new interested relations into redisGraph
4. Removes interested relations that don't apply anymore
5. Publishes to `student-interest-lost` and `student-interested` stream

The following diagram shows the interaction with `Redis Graph` and `Redis Streams`

![Interests Flow](https://github.com/serdeliverance/x-mentor/blob/develop/diagrams/interests.png?raw=true)

* Get all student's interests

```
GRAPH.QUERY xmentor "MATCH (student)-[:interested]->(topic) WHERE student.username ='$student' RETURN topic"
```


* Create interest relation

```
GRAPH.QUERY xmentor "MATCH (s:Student), (t:Topic) WHERE s.username = '${interest.student}' AND t.name = '${interest.topic}' CREATE (s)-[:interested]->(t)"
```

* Delete interest relation

```
GRAPH.QUERY xmentor "MATCH (student)-[interest:interested]->(topic) WHERE student.username='${interest.student}' and topic.name='${interest.topic}' DELETE interest"
```

* Publishing to `student-interested` stream

```
XADD student-interested $timestamp student ${student.username} topic $topic
```

* Publishing to `student-interest-lost` stream

```
XADD student-interest-lost $timestamp student ${student.username} topic $topic
```

### Course Recommendation System

In order to implement a `Course Recommendation System` that suggest users different kind courses to take, we decided to rely on the power of `Redis Graph`. Searching for relations between nodes in the graph database give us an easy way to implement different king of recommendation strategies.

#### Enrolled Recommendation Strategy

1. Random select a course the student is enrolled in
2. Get the topic of the course
3. Look for students enrolled to the same course
4. Look for courses of the same topic when those students are enrolled
5. Recommend those courses.

#### Interest Recommendation Strategy

1. Random select a student's interest
2. Look for students that are enrolled to course of that topic
3. Look for other courses of the same topic we students are enrolled in
4. Return the recommended courses (having into account those which the student isn't already enrolled)

#### Discover Recommendation Strategy

1. Get all topics
2. Get student's interest topics
3. Get topics the user is enrolled in
4. Get a topic the user is neither interesting nor enrolled
5. Get courses of that topic and recomend them

#### How the graph data is accessed

* All student's courses

```
GRAPH.QUERY xmentor "MATCH (student)-[:studying]->(course) where student.username = '$student' RETURN course"
```

* Get all topics

```
GRAPH.QUERY xmentor "MATCH (topic:Topic) RETURN topic"
```


* Get topic by course

```
GRAPH.QUERY xmentor "MATCH (topic:Topic)-[:has]->(course:Course) WHERE course.name = '$course' RETURN topic"
```

* Get students that are enrolled in (`studying` relation) a course

```
GRAPH.QUERY xmentor "MATCH (student)-[:studying]->(course) WHERE course.name = '$course' RETURN student"
```

* Get courses by topic

```
GRAPH.QUERY xmentor "MATCH (topic)-[:has]->(course) WHERE topic.name = '${topic.name}' RETURN course"
```

* Get student's interests

```
GRAPH.QUERY xmentor "MATCH (student)-[:interested]->(topic) WHERE student.username ='$student' RETURN topic"
```

* Get courses the student is enrolled in by topic

```
GRAPH.QUERY xmentor "MATCH (student)-[:studying]->(course), (topic)-[:has]->(course) where student.username = '${student.username}' and topic.name = '${topic.name}' RETURN course"
```

* Get topics the user is enrolled in

```
GRAPH.QUERY xmentor "MATCH (student)-[:studying]->(course), (topic)-[:has]->(course) WHERE student.username = '${student.username}' RETURN topic"
```

### Student Progress Registration

This functionallity allow us to track the time the user spend in the platform watching courses. That info is then used to implement the Leaderboard.

![Student Progress Registration Flow](https://github.com/serdeliverance/x-mentor/blob/develop/diagrams/student-progress-registration.png?raw=true)

`x-mentor-core` receives the request. Then, it publishes the `Student Progress Registration Domain Event`, which ends up as an element inside `student-progress-registered stream` (which is a `Redis Stream`) via the following command:

```
XADD student-progress-registered $timestamp student $student_username duration $duration
```

`Redis Gears` listen to elements pushed to the stream and then sinks this data into `Redis TimeSeries` using the following command:

```
TS.ADD studentprogress:$student_username $timestamp $duration RETENTION 0 LABELS student $student_username
```

### Leaderboard

`Leaderboard` is the functionality that allow us to have a board with the ranking of top students that uses `X-Mentor`. Top students are those who have more watching time using the platform. To accomplish that, we need to separate two functionallities:

* Register the student progress
* Getting the board data

![Leader Board Flow](https://github.com/serdeliverance/x-mentor/blob/develop/diagrams/leader-board.png?raw=true)

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

That way we can get the accumulated watching time of every student. After that, we select the top 5 highest accumulated watching time and retrive that information to visualize the board.

## How to run it locally? Run the *docker-compose.yml*

### Prerequisites
* Docker Engine and Docker Compose

### Start

```
docker-compose up
```

Wait until Keycloak and x-mentor-core are ready, then go to http://localhost:3000. Welcome to X-Mentor!