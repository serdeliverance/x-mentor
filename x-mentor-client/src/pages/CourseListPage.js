import React, { useState } from 'react'
import { makeStyles } from '@material-ui/core/styles'
import { Button, Card, CardActionArea, CardActions, CardContent, CardMedia, GridList, GridListTile, Typography } from '@material-ui/core'

const useStyles = makeStyles((theme) => ({
  root: {
    display: 'flex',
    flexWrap: 'wrap',
    justifyContent: 'space-around',
    overflow: 'hidden',
    backgroundColor: theme.palette.background.paper
  },
  media: {
    height: 140,
  },
  tile: {
    height: 'auto',
    padding: theme.spacing(2),
    width: theme.spacing(30)
  },
  gridList: {
    padding: theme.spacing(10),
    overflowY: 'hidden'
  },
}))

export default function CourseListPage() {
  const classes = useStyles();
  const [courses, setCourses] = 
    useState([{ title: "Title", img: "https://picsum.photos/200/200?random=1"}, 
        { title: "Title", img: "https://picsum.photos/200/200?random=2"}, 
        { title: "Title", img: "https://picsum.photos/200/200?random=3"}, 
        { title: "Title", img: "https://picsum.photos/200/200?random=4"}
    ])

  return (
    <GridList cellHeight={160} className={classes.gridList} cols={3}>
      {courses.map((tile) => (
          <GridListTile classes={{tile: classes.tile}} key={tile.img} cols={tile.cols || 1}>
              <Card>
                  <CardActionArea>
                      <CardContent>
                          <Typography gutterBottom variant="h5" component="h2">
                            Lizard
                          </Typography>
                          <Typography variant="body2" color="textSecondary" component="p">
                            Lizards are a widespread group of squamate reptiles, with over 6,000 species.
                          </Typography>
                          <CardMedia
                            className={classes.media}
                            image={tile.img}
                            title={tile.title}
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
          </GridListTile>
      ))}
  </GridList>

  );
}