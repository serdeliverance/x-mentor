import React from 'react'
import { makeStyles } from '@material-ui/core/styles'
import { Typography } from '@material-ui/core'

const useStyles = makeStyles((theme) => ({
    button: {
        margin: theme.spacing(2)
    },
}))

const HomePage = () => {
    const classes = useStyles()

    return (
        <>
            <Typography variant="h4">Welcome to X-Mentor</Typography>
        </>
    )
}

export default HomePage