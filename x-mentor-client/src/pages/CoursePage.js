import React, { useEffect, useState } from 'react'
import { makeStyles } from '@material-ui/core/styles'
import { useParams } from "react-router-dom"
import axios from 'axios'
import { API_URL } from '../environment'
import { Grid, Typography } from '@material-ui/core'

const styles = makeStyles(() => ({
    root: {
        display: "flex",
        flexDirection: "column",
        alignItems: "center"
    },
    contentContainer: {
        height: "30rem",
        textAlign: "center",
        background: "gray",
    },
    content: {
        height: "100%",
        width: "100%"
    },
    title: {
        padding: "1rem 5rem"
    },
    about: {
        padding: "2rem 5rem"
    }
}))

const CoursePage = () => {
    const classes = styles()
    const { id } = useParams()
    const [alert, setAlert] = useState({
        open: false,
        severity: "",
        message: ""
    })
    const [course, setCourse] = useState({})
    
    const fetchData = async () => {
        try{
            const response = await axios(
                `${API_URL}/courses/${id}`,
                {
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem("token")["access_token"]}`,
                        "Id-Token": `${JSON.parse(localStorage.getItem("token"))["id_token"]}`,
                        "Content-Type": "application/json"
                    }
                }
            )
            setCourse(response.data)
        }
        catch(error){
            console.error(error)
            setAlert({open: true, severity: "error", message: "There was an retreiving the course"})
        }
    }

    function Content() {
        if(course.content){
            if(course.content.startsWith("data:image")) return <img alt="content" className={classes.content} src={`${course.content}`}></img>
            else if(course.content.startsWith("data:video")) return <video alt="content" className={classes.content} src={`${course.content}`} controls></video>
            else return <iframe title="Content" className={classes.content} src={`${course.content}`}></iframe>
        }
        else{
            return <img alt="preview" className={classes.content} src={`${course.preview}`}></img>
        }
    }


    useEffect(() => {
        fetchData()
    }, [])
    
    return (
        <div className={classes.root}>
            <Typography variant="h5" className={classes.title}><strong>{course.title}</strong></Typography>
            <Grid container>
                <Grid item xs={2}></Grid>
                <Grid item xs={8} className={classes.contentContainer}>
                    <Content />
                </Grid>
                <Grid item xs={2}></Grid>
                <Grid item xs={2}></Grid>
                <Grid item xs={8} className={classes.about}>
                    <Typography variant="h6"><strong>About</strong></Typography>
                    <Typography>{course.description}</Typography>
                </Grid>
            </Grid>
        </div>
    )
}

export default CoursePage