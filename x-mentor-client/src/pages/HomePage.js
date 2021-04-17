import React, { useState, useEffect } from 'react'
import { makeStyles } from '@material-ui/core/styles'
import CreateCourseModal from '../components/CreateCourseModal'
import AddIcon from '@material-ui/icons/Add'
import { Button, Grid } from '@material-ui/core'

const useStyles = makeStyles((theme) => ({
    button: {
        margin: theme.spacing(2)
    },
}))

const HomePage = () => {
    const classes = useStyles()
    const [openCourseModal, setOpenCourseModal] = useState(false)


    return (
        <>
            <Grid
                container
                direction="row"
                justify="flex-end"
                alignItems="center"
            >
                <Button
                    variant="contained"
                    color="primary"
                    className={classes.button}
                    startIcon={<AddIcon />}
                    onClick={() => setOpenCourseModal(true)}
                >
                    Create Course
                </Button>
            </Grid>
            
            <CreateCourseModal open={openCourseModal} setOpen={setOpenCourseModal}></CreateCourseModal>
        </>
    )
}

export default HomePage