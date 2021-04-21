import React from 'react'
import { Typography, makeStyles, AppBar, Container, Toolbar } from '@material-ui/core'


const useStyles = makeStyles((theme) => ({
  appBar: {
    top: 'auto',
    bottom: 0,
  },
}));

export default function Footer() {
  const classes = useStyles()

  return (
    <AppBar className={classes.appBar} component="footer" color="primary">
      <Container maxWidth="md">
        <Toolbar>
          <Typography variant="body1" color="inherit">
            © 2021 X-Mentor
          </Typography>
        </Toolbar>
      </Container>
    </AppBar>
  )
}