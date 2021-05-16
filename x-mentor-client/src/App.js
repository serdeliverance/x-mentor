import React, { useContext } from "react";
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
import { AuthContext, AuthProvider } from "./Providers/AuthProvider";

export default function App() {
  return (
    <AuthProvider>
      <Router>
        <NotificationsProvider>
          <Header/>
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
        <Footer />
      </Router>
    </AuthProvider>
  )
}

function PrivateRoute({ children, ...rest }) {
  const { isLoggedIn } = useContext(AuthContext)
  return (
    <Route
      {...rest}
      render={({ location }) =>
        isLoggedIn ? (
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
