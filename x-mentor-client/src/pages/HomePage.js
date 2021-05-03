import React, { useState, useEffect, useContext } from 'react'
import { makeStyles } from '@material-ui/core/styles'
import { Box, Button, Chip, Divider, Grid, Typography } from '@material-ui/core'
import axios from 'axios'
import { API_URL } from '../environment'
import { AuthContext } from '../Providers/AuthProvider'
import { useNotification } from '../hooks/notify'

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
    },
    sidebar: {
        padding: "2rem 4rem"
    }
}))

const HomePage = () => {
    const classes = useStyles()
    const [topics, setTopics] = useState([])
    const [interests, setInsterests] = useState([])
    const [recommendations, setRecommendations] = useState([])
    const authContext = useContext(AuthContext)
    const notify = useNotification()

    const fetchData = async () => {
        try{
            const recommendationsResponse = axios(
                `${API_URL}/recommendations`,
                {
                    headers: {
                      Authorization: `Bearer ${authContext.getTokens().access_token}`,
                      "Id-Token": `${authContext.getTokens().id_token}`,
                    }
                }
            ).then(response => {
                console.log(response.data)
                setRecommendations(response.data)
            })
            console.log("Continue")

            if(authContext.getTokens()){
                const topicsResponse = await axios(
                    `${API_URL}/topics`,
                    {
                        headers: {
                          Authorization: `Bearer ${authContext.getTokens().access_token}`,
                          "Id-Token": `${authContext.getTokens().id_token}`,
                        }
                    }
                )
                const topics = topicsResponse.data.map(topic => topic.name)
                setTopics(topics)

                const interestsResponse = await axios(
                    `${API_URL}/interests`,
                    {
                        headers: {
                          Authorization: `Bearer ${authContext.getTokens().access_token}`,
                          "Id-Token": `${authContext.getTokens().id_token}`,
                        }
                    }
                )
                const interests = interestsResponse.data.map(interest => interest.name)
                setInsterests(interests)
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
        if(!interests.includes(topic))
            setInsterests([...interests, topic])
    }

    const handleInterestsSubmit = async () => {
        try{
            if(authContext.getTokens()){
                const response = await axios.post(
                    `${API_URL}/interests`,
                    { "topics": interests },
                    {
                        headers: {
                          Authorization: `Bearer ${authContext.getTokens().access_token}`,
                          "Id-Token": `${authContext.getTokens().id_token}`,
                        }
                    }
                )
                notify("Interests saved!", "success")
            }
        }
        catch(error){
            console.log(error)          
        }
    }

    const InterestsComponent = () => {
        if(interests.length === 0) {
            return (
                <Box className={classes.interests} display="flex" justifyContent="center" alignItems="center">
                    <Typography variant="body1">You don't have any interests listed</Typography>
                </Box>
            )
        }
        else {
            return (
                <Box className={classes.interests} textAlign="center">
                    {interests.map(interest => (
                        <Chip key={interest} label={interest} className={classes.chip} onDelete={() => handleDelete(interest)} color="primary"/>
                    ))}
                </Box>
            )
        }
    }

    useEffect(() => {
        fetchData()
    }, [])

    return (
        <div className={classes.root}>
            <Grid container>
                <Grid item xs={9}>
                    <Typography variant="h4">Welcome to X-Mentor</Typography>
                </Grid>
                {authContext.getTokens() && 
                <Grid item xs={3} className={classes.sidebar}>
                    <Typography variant="h6" align="center">Tell us what you're interested in</Typography>
                    <Divider />
                    <InterestsComponent />
                    <Divider />
                    <Box className={classes.topics} mb={3} textAlign="center">
                        {topics.map(topic => (
                            <Chip key={topic} label={topic} className={classes.chip} disabled={interests.includes(topic) ? true : false} onClick={() => handleClick(topic)}/>
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