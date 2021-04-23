import React, { useEffect, useState, useRef } from 'react'
import { makeStyles } from '@material-ui/core/styles'
import { Button, Card, CardActionArea, CardActions, CardContent, CardMedia, Grid, Tooltip, Typography } from '@material-ui/core'
import axios from 'axios'
import Pagination from '@material-ui/lab/Pagination'
import { useLocation } from "react-router-dom"
import StarRateIcon from '@material-ui/icons/StarRate'

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
    height: 180,
    padding: "45px 0",
    margin: "20px 0 0"
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

  const handleChange = (event, value) => setPage(value)

  const fetchData = async () => {
    const result = await axios(
      `http://localhost:9000/courses?q=${query.get('q')}&page=${page}`,
    )
    setCourses(result.data.courses)
    setTotal(Math.round(result.data.total / 6))
  }

  const enroll = async (courseId) => {
    console.log(courseId)
    /*const result = await axios(
      `http://localhost:9000/${courseId}/enroll`,
    )*/
  }

  const showDescription = () => {
    console.log("Description")
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
                    <CardActionArea onClick={(e) => showDescription(e.target.closest(".MuiCard-root").id)}>
                        <CardContent>
                            <Typography gutterBottom variant="h6" className={classes.title}>
                              {course.title}
                              {course.rating > 3 ? 
                                <Tooltip placement="top" title="Top Course">
                                  <StarRateIcon className={classes.star} />
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
                    <CardActions>
                      <Button size="small" color="primary" onClick={enroll}>
                        Enroll
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