import React, { useState, useEffect, useContext } from 'react'
import { Box, Button, Chip, Divider, Grid, Paper, Typography, makeStyles } from '@material-ui/core'
import axios from 'axios'
import { API_URL } from '../environment'
import { AuthContext } from '../Providers/AuthProvider'
import { useNotification } from '../hooks/notify'
import Carousel from 'react-material-ui-carousel'
import { useHistory } from "react-router-dom"
import Leaderboard from '../components/Leadeboard'

const useStyles = makeStyles(() => ({
    root: {
        height: "calc(100vh - 7rem)"
    },
    chip: {
        margin: 5
    },
    interests: {
        height: "15rem"
    },
    topics: {
    },
    interestsBtn: {
        textAlign: "center"
    },
    sidebar: {
        padding: "2rem 0 0 0"
    },
    checkButton: {
        position: "absolute",
        display: "block",
        bottom: 30,
        right: 0,
        zIndex: 2,
        margin: 5,
        padding: 5
    },
    image: {
        width: "100%"
    },
    courseTitle: {
        margin: 0,
        padding: "2px 10px"
    },
    paper: {
        boxShadow: "none"
    },
}))

const HomePage = () => {
    const classes = useStyles()
    const [topics, setTopics] = useState([])
    const [interests, setInsterests] = useState([])
    const [recommendations, setRecommendations] = useState({})
    const [scores, setScores] = useState([[],[]])
    const { isLoggedIn, getTokens } = useContext(AuthContext)
    const history = useHistory()
    const notify = useNotification()

    const fetchData = async () => {
        try {
            
            axios(`${API_URL}/leaderboard`).then(response => {
                const leadeboardData = response.data.list
                leadeboardData.sort((a,b) => a.progress > b.progress ? -1 : 1)
                const orderedScores = [leadeboardData.map(a => a.progress), leadeboardData.map(a => a.student)]
                setScores(orderedScores)
            })

            if(isLoggedIn){
                const headers = {
                    headers: {
                        Authorization: `Bearer ${getTokens().access_token}`,
                        "Id-Token": `${getTokens().id_token}`,
                    }
                }

                axios(`${API_URL}/recommendations`, headers).then(response => {
                    const data = response.data
                    const keys = Object.keys(data)
                    const randomObject = Math.floor(Math.random()*keys.length)
                    const courses = data[keys[randomObject]]
                    setRecommendations(courses)
                })

                const topicsResponse = await axios(`${API_URL}/topics`, headers)
                const topics = topicsResponse.data.map(topic => topic.name)
                setTopics(topics)

                const interestsResponse = await axios(`${API_URL}/students/interests`, headers)
                const interests = interestsResponse.data.map(interest => interest.name)
                setInsterests(interests)
            }
            else {
                axios(`${API_URL}/visitors/recommendations`).then(response => {
                    setRecommendations(response.data.discover)
                })
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
            if(isLoggedIn){
                await axios.post(
                    `${API_URL}/students/interests`,
                    { "topics": interests },
                    {
                        headers: {
                            Authorization: `Bearer ${getTokens().access_token}`,
                            "Id-Token": `${getTokens().id_token}`,
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

    const checkCourse = (courseName) => {
        history.push(`/courses?q=${courseName}`)
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

    const Item = (props) => {
        return (
            <Paper className={classes.paper}>
                <h2 className={classes.courseTitle}>{props.item.name}</h2>
                <img alt={props.item.name} src={props.item.preview} className={classes.image}></img>

                <Button className={classes.checkButton} variant="contained" color="secondary" onClick={() => checkCourse(props.item.name)}>
                    Check it out!
                </Button>
            </Paper>
        )
    }

    useEffect(() => {
        fetchData()
    }, [isLoggedIn])

    return (
        <Grid container className={classes.root}>
            <Grid container item xs={6} justify="center" className={classes.sidebar}>
                <Grid item xs={6}>
                {!isLoggedIn ? 
                <>
                    <Typography variant="h6" align="center">Discover new courses about {recommendations?.topic}</Typography>
                    <Divider />
                    <Carousel>
                        {
                            recommendations?.courses && recommendations?.courses.map( (item, i) => <Item key={i} item={item} /> )
                        }
                    </Carousel>
                </> :
                <>
                    <Typography variant="h6" align="center">Discover new courses about {recommendations?.topic}</Typography>
                    <Divider />
                    <Carousel>
                        {
                            recommendations?.courses && recommendations?.courses.map( (item, i) => <Item key={i} item={item} /> )
                        }
                    </Carousel>
                </>
                }
                </Grid>
            </Grid>
            {isLoggedIn ?
            <Grid container item xs={6} className={classes.sidebar} justify="center">
                <Divider orientation="vertical" />
                <Grid item xs={2}></Grid>
                <Grid item xs={4}>
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
                </Grid>
            </Grid> :
            <>
            </>
            }
            <Leaderboard scores={scores}></Leaderboard>
        </Grid>
    )
}

export default HomePage