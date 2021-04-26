import React, { useEffect, useState, useRef } from 'react'
import { makeStyles } from '@material-ui/core/styles'
import { Button, Card, CardActionArea, CardActions, CardContent, CardMedia, Grid, Typography } from '@material-ui/core'
import axios from 'axios'
import Pagination from '@material-ui/lab/Pagination'
import { useLocation } from "react-router-dom"
import { API_URL } from '../environment'

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
      float: 'right'
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

function useQuery() {
  return new URLSearchParams(useLocation().search);
}

export default function CourseListPage() {
  const classes = useStyles()
  const query = useQuery()
  const prevQueryParam = useRef()
  const [courses, setCourses] = useState([])
  const [page, setPage] = useState(1)
  const [total, setTotal] = useState(10)
  const [currentCourse, setCurrentCourse] = useState()

  const handleChange = (event, value) => setPage(value)

  const fetchData = async () => {
    const response = await axios(
      `${API_URL}/courses?q=${query.get('q')}&page=${page}`,
    )
    setCourses(response.data.courses)
    setTotal(Math.round(response.data.total / 6))
  }

  const enroll = async (courseId) => {
    console.log(courseId)
    const response = await axios.post(
      `${API_URL}/courses/${courseId}/enroll`,
    )
    console.log(response)
  }

  const startCourse = (courseId) => {
    console.log("Start course: " + courseId)
  }

  // TODO Arreglar
  useEffect(() => {
    if(query.get('q') !== prevQueryParam.current){
      fetchData()
      setPage(1)
      prevQueryParam.current = query.get('q')
    }
  }, [query])
  
  useEffect(() => {
    if(query.get('q') === prevQueryParam.current){
      fetchData()
    }
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
                  <CardActionArea onClick={(e) => startCourse(e.target.closest(".MuiCard-root").id)}>
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
                    <Button color="primary" className={classes.enroll} onClick={(e) => startCourse(e.target.closest(".MuiCard-root").id)}>
                      Start Course
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
  );
}