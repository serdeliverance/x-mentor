import React, { useState, useEffect } from 'react'
import { makeStyles } from '@material-ui/core/styles'
import { Button, Chip, Grid, Typography } from '@material-ui/core'
import axios from 'axios'
import { API_URL } from '../environment'

const useStyles = makeStyles((theme) => ({
    root: {
        height: "100%"
    }

}))

const HomePage = () => {
    const classes = useStyles()
    const [topics, setTopics] = useState([])
    const [interests, setInsterests] = useState([])

    const fetchData = async () => {
        try{
            if(localStorage.getItem("token")){
                const topics = await axios(
                    `${API_URL}/topics`,
                    {
                        headers: {
                          Authorization: `Bearer ${JSON.parse(localStorage.getItem("token"))["access_token"]}`,
                          "Id-Token": `${JSON.parse(localStorage.getItem("token"))["id_token"]}`,
                        }
                    }
                )
                console.log(topics.data)
                setTopics(topics.data)

                const interests = await axios(
                    `${API_URL}/interests`,
                    {
                        headers: {
                          Authorization: `Bearer ${JSON.parse(localStorage.getItem("token"))["access_token"]}`,
                          "Id-Token": `${JSON.parse(localStorage.getItem("token"))["id_token"]}`,
                        }
                    }
                )
                console.log(interests.data)
                setInsterests(interests.data)
            }
        }
        catch(error){
            console.log(error)          
        }
    }

    useEffect(() => {
        fetchData()
    }, [])

    const handleDelete = () => {
        console.info('You clicked the delete icon.');
      };
    
      const handleClick = () => {
        console.info('You clicked the Chip.');
      };

    return (
        <div className={classes.root}>
            <Grid container>
                <Grid item xs={9}>
                    <Typography variant="h4">Welcome to X-Mentor</Typography>
                </Grid>
                {localStorage.getItem("token") && <Grid item xs={3}>
                    <Typography variant="h4" align="center">Interests</Typography>
                    {interests.map(intereset => (
                        <Chip label={intereset} onDelete={handleDelete} color="primary"/>
                    ))}
                    {topics.map(topic => (
                        <Chip label={topic.name} component="a" href="#chip" clickable onClick={handleClick}/>
                    ))}
                    <Button variant="contained" color="primary">Save Changes</Button>
                </Grid>}
            </Grid>
        </div>
    )
}

export default HomePage