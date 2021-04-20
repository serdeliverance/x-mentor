import React, { useEffect, useState } from 'react'
import { makeStyles } from '@material-ui/core/styles'
import { Button, Card, CardActionArea, CardActions, CardContent, CardMedia, Grid, Typography } from '@material-ui/core'
import axios from 'axios'
import Pagination from '@material-ui/lab/Pagination';

const useStyles = makeStyles((theme) => ({
  root: {
    padding: theme.spacing(10),
  },
  grid: {
    display: 'flex',
    justifyContent: 'space-between',
    backgroundColor: theme.palette.background.paper,
  },
  media: {
    height: 140,
  },
  tile: {
    padding: theme.spacing(2),
    width: "33%"
  },
  description: {
    maxWidth: '30ch',
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
}))

export default function CourseListPage() {
  const classes = useStyles()
  const [courses, setCourses] = useState([])
  const [page, setPage] = useState(0)
  const [total, setTotal] = useState(10)

  const handleChange = (event, value) => {
    setPage(value);
  };

  useEffect(() => {
    const fetchData = async () => {
      const result = await axios(
          `http://localhost:9000/courses?page=${page}`,
        )
        setCourses(result.data)
        // setTotal(result.data)
      }
    fetchData()
  }, [])
  
  return (
    <div className={classes.root}>
    <div className={classes.pagination}>
      <Pagination count={total} shape="rounded" onChange={handleChange} />
    </div>
    <Grid container classes={{ root: classes.grid }}>
      {courses.map((course) => (
          <Grid item className={classes.tile} key={course.id}>
              <Card className={classes.card}>
                  <CardActionArea>
                      <CardContent>
                          <Typography gutterBottom variant="h6" className={classes.title}>
                            {course.title}
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
                    <Button size="small" color="primary">
                      Share
                    </Button>
                    <Button size="small" color="primary">
                      Learn More
                    </Button>
                  </CardActions>
              </Card>
          </Grid>
      ))}
  </Grid>
  </div>

  );
}