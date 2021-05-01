import React from "react";
import {
  BrowserRouter as Router,
  Switch,
  Route,
  Redirect
} from "react-router-dom";
import Home from './pages/HomePage'
import CoursePage from './pages/CoursePage'
import CourseListPage from './pages/CourseListPage'
import MyCoursesPage from './pages/MyCoursesPage'
import Header from "./components/Header";
import Footer from "./components/Footer";

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
        <PrivateRoute path="/my/courses">
          <MyCoursesPage />
        </PrivateRoute>
        <Route path="/">
          <Home />
        </Route>
      </Switch>
      <Footer />
    </Router>
    </>
  );
}

function PrivateRoute({ children, ...rest }) {
  return (
    <Route
      {...rest}
      render={({ location }) =>
        localStorage.getItem("token") ? (
          children
        ) : (
          <Redirect
            to={{
              pathname: "/",
              state: { from: location }
            }}
          />
        )
      }
    />
  );
}
