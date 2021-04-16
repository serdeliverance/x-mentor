-- Users schema

-- !Ups

DROP TABLE IF EXISTS "rating";

CREATE TABLE IF NOT EXISTS "rating" (
  rating_id BIGSERIAL,
  user_id INT NOT NULL,
  course_id INT NOT NULL,
  stars INT NOT NULL,
  PRIMARY KEY(rating_id)
);

DROP TABLE IF EXISTS "topic";

CREATE TABLE IF NOT EXISTS "topic" (
  topic_id BIGSERIAL,
  name VARCHAR(55) NOT NULL,
  description VARCHAR(255),
  PRIMARY KEY(topic_id)
);

DROP TABLE IF EXISTS "course";

CREATE TABLE IF NOT EXISTS "course" (
  course_id BIGSERIAL,
  name VARCHAR(55) NOT NULL,
  topic_id INT NOT NULL,
  PRIMARY KEY(course_id)
);

DROP TABLE IF EXISTS "content";

CREATE TABLE IF NOT EXISTS "content" (
  content_id BIGSERIAL,
  content VARCHAR(255) NOT NULL,
  PRIMARY KEY(content_id)
);

DROP TABLE IF EXISTS "recommendation";

CREATE TABLE IF NOT EXISTS "recommendation" (
  recommendation_id BIGSERIAL,
  user_id INT NOT NULL,
  topic_id INT,
  course_id INT,
  PRIMARY KEY(recommendation_id)
);