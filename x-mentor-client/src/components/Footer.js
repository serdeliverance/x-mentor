import React from 'react'
import { Typography, makeStyles } from '@material-ui/core'


const useStyles = makeStyles(() => ({
  footer: {
    bottom: 0,
    position: "fixed",
    width: "100%",
    height: "2rem",
    padding: "0.5rem 0px",
    color: "#fff",
    backgroundColor: "#3f51b5",
    display: "flex",
    alignItems: "center",
    justifyContent: "center"
  },
}));

export default function Footer() {
  const classes = useStyles()

  return (
    <div className={classes.footer} component="footer" color="primary">
        <Typography variant="body1" color="inherit">
          &copy; 2021 X-Mentor
        </Typography>
    </div>
  )
}