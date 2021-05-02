import React, { useEffect, useState } from 'react'
import { makeStyles } from '@material-ui/core/styles'
import { Button, Card, CardActionArea, CardActions, CardContent, CardMedia, Grid, Typography, Snackbar } from '@material-ui/core'
import axios from 'axios'
import Pagination from '@material-ui/lab/Pagination'
import { API_URL } from '../environment'
import MuiAlert from '@material-ui/lab/Alert'
import { useHistory } from "react-router-dom";

function Alert(props) {
  return <MuiAlert elevation={6} variant="filled" {...props} />
}

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
  const [alert, setAlert] = useState({
    open: false,
    severity: "",
    message: ""
  })

  const handleChange = (event, value) => setPage(value)

  const fetchData = async () => {
    try{
      const response = await axios(
        `${API_URL}/student/courses?page=${page}`,
        {
          headers: {
            Authorization: `Bearer ${JSON.parse(localStorage.getItem("token"))["access_token"]}`,
            "Id-Token": `${JSON.parse(localStorage.getItem("token"))["id_token"]}`,
          }
        }
      )
      setCourses(response.data.courses)
      setTotal(Math.ceil(response.data.total / 6))
    }
    catch(error){
      const status = error.response.status
      if(status === 401) {
        setAlert({open: true, severity: "warning", message: "Session Expired"})
        localStorage.removeItem("token")
        history.push("/")
      }
      else {
        setAlert({open: true, severity: "error", message: "There was an error retreiving courses"})
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
    <>
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
  <Snackbar open={alert.open} autoHideDuration={6000} onClose={() => setAlert({...alert, open: false})}>
      <Alert onClose={() => setAlert({...alert, open: false})} severity={alert.severity}>
          {alert.message}
      </Alert>
  </Snackbar>
  </>
  );
}