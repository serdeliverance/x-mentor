import React, { useEffect, useState, useRef, useContext } from 'react'
import { makeStyles } from '@material-ui/core/styles'
import { Box, Button, Card, CardActionArea, CardActions, CardContent, CardMedia, Grid, Tooltip, Typography, Badge } from '@material-ui/core'
import axios from 'axios'
import Pagination from '@material-ui/lab/Pagination'
import { useLocation } from "react-router-dom"
import EmojiEventsIcon from '@material-ui/icons/EmojiEvents'
import Rating from '@material-ui/lab/Rating'
import CourseModal from '../components/CourseModal'
import { API_URL } from '../environment'
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
    padding: theme.spacing(5),
    width: "30%",
  },
  description: {
    WebkitLineClamp: 3,
    WebkitBoxOrient: 'vertical',
    overflow: 'hidden',
    display: '-webkit-box',
    minHeight: "4em"
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
  },
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
  const notify = useNotification()
  const { getTokens } = useContext(AuthContext)

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
      await axios.post(
        `${API_URL}/courses/${courseId}/enroll`,
        {},
        {
          headers: {
            Authorization: `Bearer ${getTokens().access_token}`,
            "Id-Token": `${getTokens().id_token}`,
          }
        }
      )
      notify("Enrolled successfully", "success")
    }
    catch(error){
      const status = error.response.status
      if(status === 401)
        notify("You need to sign in to enroll on a course", "error")
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
            <Card id={course.id}>
                <CardActionArea onClick={() => handleCourseModal(course)}>
                    <CardContent className={classes.content}>
                      <Badge classes={{root: classes.topic, badge: classes.topicBadge}} badgeContent={course.topic} color="secondary"></Badge>
                        <Grid container>
                          <Grid item xs={11}>
                            <Typography gutterBottom variant="h6" className={classes.title}>
                              {course.title}
                            </Typography>
                          </Grid>
                          {course.rating >= 4 ? 
                            <Grid item xs={1}>
                              <Tooltip placement="top" title="Top Course">
                                <EmojiEventsIcon className={classes.star} />
                              </Tooltip>
                            </Grid>: <></>}
                        </Grid>

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
                  <Tooltip title={course.rating} className={classes.ratingTooltip} placement="left">
                    <Box component="fieldset" pb={0.2} borderColor="transparent">
                        <Rating name="read-only" value={course.rating} readOnly />
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
  </>
  );
}