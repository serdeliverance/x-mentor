import React, { useState, useEffect, useContext } from 'react'
import { makeStyles } from '@material-ui/core/styles'
import { Button, Chip, Grid, Typography } from '@material-ui/core'
import axios from 'axios'
import { API_URL } from '../environment'
import { AuthContext } from '../Providers/AuthProvider'

const useStyles = makeStyles(() => ({
    root: {
        height: "100%"
    }

}))

const HomePage = () => {
    const classes = useStyles()
    const [topics, setTopics] = useState([])
    const [interests, setInsterests] = useState([])
    const authContext = useContext(AuthContext)

    const fetchData = async () => {
        try{
            if(localStorage.getItem("token")){
                const topics = await axios(
                    `${API_URL}/topics`,
                    {
                        headers: {
                          Authorization: `Bearer ${authContext.getTokens().access_token}`,
                          "Id-Token": `${authContext.getTokens().id_token}`,
                        }
                    }
                )
                console.log(topics.data)
                setTopics(topics.data)

                const interests = await axios(
                    `${API_URL}/interests`,
                    {
                        headers: {
                          Authorization: `Bearer ${authContext.getTokens().access_token}`,
                          "Id-Token": `${authContext.getTokens().id_token}`,
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
    }
    
    const handleClick = () => {
        console.info('You clicked the Chip.')
    }

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