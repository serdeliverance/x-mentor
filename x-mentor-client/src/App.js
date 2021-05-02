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
import { NotificationsProvider } from "./Providers/NotificationsProvider";
import { AuthProvider } from "./Providers/AuthProvider";

export default function App() {
  return (
    <>
    <Router>
      <AuthProvider>
        <NotificationsProvider>
          <Header />
          <Switch>
            <Route path="/courses">
              <CourseListPage />
            </Route>
            <PrivateRoute path="/my/courses">
              <MyCoursesPage />
            </PrivateRoute>
            <PrivateRoute path="/course/:id">
              <CoursePage />
            </PrivateRoute>
            <Route path="/">
              <Home />
            </Route>
          </Switch>
        </NotificationsProvider>
      </AuthProvider>
      <Footer />
    </Router>
    </>
  )
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
  )

}
