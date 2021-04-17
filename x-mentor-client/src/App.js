import React from "react";
import {
  BrowserRouter as Router,
  Switch,
  Route
} from "react-router-dom";
import Home from './pages/HomePage'
import CoursePage from './pages/CoursePage'
import CourseListPage from './pages/CourseListPage'
import MyCoursesPage from './pages/MyCoursesPage'
import Header from "./components/Header";

export default function App() {
  return (
    <>
    <Router>
      <Header />
      <Switch>
        <Route path="/course">
          <CoursePage />
        </Route>
        <Route path="/courses">
          <CourseListPage />
        </Route>
        <Route path="/my/courses">
          <MyCoursesPage />
        </Route>
        <Route path="/">
          <Home />
        </Route>
      </Switch>
    </Router>
    </>
  );
}
