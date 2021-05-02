import React, { useEffect, useState, useRef } from 'react'
import { makeStyles } from '@material-ui/core/styles'
import { Box, Button, Card, CardActionArea, CardActions, CardContent, CardMedia, Grid, Tooltip, Typography, Badge, Snackbar } from '@material-ui/core'
import axios from 'axios'
import Pagination from '@material-ui/lab/Pagination'
import { useLocation } from "react-router-dom"
import EmojiEventsIcon from '@material-ui/icons/EmojiEvents'
import Rating from '@material-ui/lab/Rating'
import CourseModal from '../components/CourseModal';
import { API_URL } from '../environment'
import MuiAlert from '@material-ui/lab/Alert'

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
    width: "30%",
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
    fontWeight: 'bold',
    WebkitLineClamp: 1,
    WebkitBoxOrient: 'vertical',
    overflow: 'hidden',
    display: '-webkit-box'
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
    padding: 10,
    marginLeft: 10
  },
  content: {
    padding: "16px 16px 0"
  },
  topic: {
    justifyContent: "center",
    marginBottom: 10
  },
  topicBadge: {
    right: "auto"
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
  const [openCourseModal, setOpenCourseModal] = useState(false)
  const [currentCourse, setCurrentCourse] = useState()
  const [alert, setAlert] = useState({
    open: false,
    severity: "",
    message: ""
  })

  const handleChange = (event, value) => setPage(value)

  const fetchData = async () => {
    const response = await axios(
      `${API_URL}/courses?q=${query.get('q')}&page=${page}`,
    )
    setCourses(response.data.courses)
    setTotal(Math.ceil(response.data.total / 6))
  }

  const enroll = async (courseId) => {
    try{
      const response = await axios.post(
        `${API_URL}/courses/${courseId}/enroll`,
        {},
        {
          headers: {
            Authorization: `Bearer ${JSON.parse(localStorage.getItem("token"))["access_token"]}`,
            "Id-Token": `${JSON.parse(localStorage.getItem("token"))["id_token"]}`,
          }
        }
      )
      setAlert({open: true, severity: "success", message: "Enroll successfully"})
    }
    catch(error){
      const status = error.response.status
      if(status === 401)
        setAlert({open: true, severity: "error", message: "You need to sign in to enroll on a course"})
    }
  }

  const handleCourseModal = (course) => {
    setCurrentCourse(course)
    setOpenCourseModal(true)
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
                <CardActionArea onClick={() => handleCourseModal(course)}>
                    <CardContent className={classes.content}>
                      <Badge classes={{root: classes.topic, badge: classes.topicBadge}} badgeContent={course.topic} color="secondary"></Badge>
                        <Typography gutterBottom variant="h6" className={classes.title}>
                          {course.title}
                          {course.rating >= 4 ? 
                            <Tooltip placement="top" title="Top Course">
                              <EmojiEventsIcon className={classes.star} />
                            </Tooltip> : <></>}
                        </Typography>

                        <Typography variant="body2" color="textSecondary" component="p" className={classes.description}>
                          {course.description}
                        </Typography>
                        <CardMedia
                          className={classes.media}
                          image={course.preview}
                          title={course.title}
                        />
                    </CardContent>
                </CardActionArea>
                <CardActions className={classes.actions}>
                  <Button color="primary" className={classes.enroll} onClick={(e) => enroll(e.target.closest(".MuiCard-root").id)}>
                    Enroll
                  </Button>
                  <Tooltip title={course.rating} placement="left">
                    <Box component="fieldset" pb={0.2} borderColor="transparent">
                        <Rating name="read-only" value={course.rating} precision={0.5} readOnly />
                    </Box>
                  </Tooltip>
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
  <CourseModal course={currentCourse} open={openCourseModal} setOpen={setOpenCourseModal} />
  <Snackbar open={alert.open} autoHideDuration={6000} onClose={() => setAlert({...alert, open: false})}>
      <Alert onClose={() => setAlert({...alert, open: false})} severity={alert.severity}>
          {alert.message}
      </Alert>
  </Snackbar>
  </>
  );
}