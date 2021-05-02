import React, { useEffect, useState, useContext } from 'react'
import { makeStyles } from '@material-ui/core/styles'
import { Button, Card, CardActionArea, CardActions, CardContent, CardMedia, Grid, Typography } from '@material-ui/core'
import axios from 'axios'
import Pagination from '@material-ui/lab/Pagination'
import { API_URL } from '../environment'
import { useHistory } from "react-router-dom";
import { useNotification } from '../hooks/notify'
import { AuthContext } from '../Providers/AuthProvider'

const useStyles = makeStyles((theme) => ({
  root: {
    padding: `${theme.spacing(8)}px ${theme.spacing(18)}px`,
  },
  grid: {
    display: 'flex',
    justifyContent: 'space-between',
    backgroundColor: theme.palette.background.paper,
  },
  media: {
    height: 165,
    padding: "45px 0",
    margin: "20px 0 0",
    backgroundSize: "auto 100%"
  },
  tile: {
    padding: theme.spacing(4),
    width: "30%"
  },
  description: {
    WebkitLineClamp: 3,
    WebkitBoxOrient: 'vertical',
    overflow: 'hidden',
    display: '-webkit-box'
  },
  card: {
    height: '50vh'
  },
  title: {
    fontWeight: 'bold'
  },
  pagination: {
    '& > *': {
      marginTop: theme.spacing(2),
    },
  },
  star: {
    float: "right",
    position: "relative",
    top: 5,
    color: "gold"
  },
  actions: {
    height: "3rem",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center"
  },
  enroll: {
    padding: 16
  },
  content: {
    padding: "16px 16px 0"
  }
}))

export default function CourseListPage() {
  const classes = useStyles()
  const history = useHistory()
  const [courses, setCourses] = useState([])
  const [page, setPage] = useState(1)
  const [total, setTotal] = useState(10)
  const notify = useNotification()
  const authContext = useContext(AuthContext)

  const handleChange = (event, value) => setPage(value)

  const fetchData = async () => {
    try{
      const response = await axios(
        `${API_URL}/student/courses?page=${page}`,
        {
          headers: {
            Authorization: `Bearer ${authContext.getTokens().access_token}`,
            "Id-Token": `${authContext.getTokens().id_token}`,
          }
        }
      )
      setCourses(response.data.courses)
      setTotal(Math.ceil(response.data.total / 6))
    }
    catch(error){
      const status = error.response && error.response.status
      if(status === 401) {
        notify("Session Expired", "warning")
        localStorage.removeItem("token")
        history.push("/")
      }
      else {
        notify("There was an error retreiving courses", "error")
      }
    }
  }

  const watchCourse = (courseId) => {
    history.push(`/course/${courseId}`)
  }
  
  useEffect(() => {
    fetchData()
  }, [page])

  return (
    <div className={classes.root}>
      {courses.length > 0 ?
      <>
      <div className={classes.pagination}>
        <Pagination count={total} shape="rounded" onChange={handleChange} />
      </div>
      <Grid container classes={{ root: classes.grid }}>
        {courses.map((course) => (
          <Grid item className={classes.tile} key={course.id}>
              <Card className={classes.card} id={course.id}>
                  <CardActionArea onClick={(e) => watchCourse(e.target.closest(".MuiCard-root").id)}>
                      <CardContent className={classes.content}>
                          <Typography gutterBottom variant="h6" className={classes.title}>
                            {course.title}
                          </Typography>
                          <CardMedia
                            className={classes.media}
                            image={course.preview}
                            title={course.title}
                          />
                      </CardContent>
                  </CardActionArea>
                  <CardActions className={classes.actions}>
                    <Button color="primary" className={classes.enroll} onClick={(e) => watchCourse(e.target.closest(".MuiCard-root").id)}>
                      Watch Course
                    </Button>
                  </CardActions>
              </Card>
          </Grid>
        ))}
      </Grid>
      </>
    : <Grid container item classes={{ root: classes.grid }}>
        <Typography gutterBottom variant="h6" className={classes.title}>
          We couldn't find any courses with the specified filter
        </Typography>
      </Grid>
    }
  </div>
  )
}