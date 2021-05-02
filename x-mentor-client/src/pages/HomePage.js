import React, { useState, useEffect, useContext } from 'react'
import { makeStyles } from '@material-ui/core/styles'
import { Box, Button, Chip, Grid, Typography } from '@material-ui/core'
import axios from 'axios'
import { API_URL } from '../environment'
import { AuthContext } from '../Providers/AuthProvider'

const useStyles = makeStyles(() => ({
    root: {
        height: "100%"
    },
    chip: {
        margin: 5
    },
    interests: {
        height: "10rem"
    },
    topics: {
    },
    interestsBtn: {
        textAlign: "center"
    }
}))

const HomePage = () => {
    const classes = useStyles()
    const [topics, setTopics] = useState([])
    const [interests, setInsterests] = useState([])
    const authContext = useContext(AuthContext)

    const fetchData = async () => {
        try{
            if(authContext.getTokens()){
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

    const handleDelete = (interestToRemove) => {
        setInsterests(interests.filter(interest => interest !== interestToRemove))
    }
    
    const handleClick = (topic) => {
        setInsterests([...interests, topic])
    }

    const handleInterestsSubmit = async () => {
        try{
            if(authContext.getTokens()){
                const response = await axios.post(
                    `${API_URL}/interests`,
                    interests,
                    {
                        headers: {
                          Authorization: `Bearer ${authContext.getTokens().access_token}`,
                          "Id-Token": `${authContext.getTokens().id_token}`,
                        }
                    }
                )
                console.log(response)
            }
        }
        catch(error){
            console.log(error)          
        }
    }

    useEffect(() => {
        fetchData()
    }, [])

    return (
        <div className={classes.root}>
            <Grid container>
                <Grid item xs={10}>
                    <Typography variant="h4">Welcome to X-Mentor</Typography>
                </Grid>
                {authContext.getTokens() && <Grid item xs={2}>
                    <Typography variant="h4" align="center">Interests</Typography>
                    <Box className={classes.interests}>
                        {interests.length === 0 ? <Typography variant="body">You have no interests set</Typography> :
                        interests.map(interest => (
                            <Chip label={interest} className={classes.chip} onDelete={() => handleDelete(interest)} color="primary"/>
                        ))}
                    </Box>

                    <Box className={classes.topics} mb={3}>
                        {topics.map(topic => (
                            <Chip label={topic.name} className={classes.chip} clickable onClick={() => handleClick(topic.name)}/>
                        ))}
                    </Box>
                    
                    <Box className={classes.interestsBtn}>
                        <Button variant="contained" color="primary" onClick={handleInterestsSubmit}>Save Changes</Button>
                    </Box>
                </Grid>}
            </Grid>
        </div>
    )
}

export default HomePage