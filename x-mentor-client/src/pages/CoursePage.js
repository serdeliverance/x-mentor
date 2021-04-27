import React, { useState } from 'react'
import TextareaAutosize from '@material-ui/core/TextareaAutosize'
import { makeStyles } from '@material-ui/core/styles';

const styles = makeStyles(() => ({
    dialog: {
        height: "57vh !important",
        margin: "17vh 10vw",
        width: "79vw !important"
    },
}))

const CoursePage = () => {
    const classes = styles()
    const [value, setValue] = useState("")

    
    const handleChange = (event) => setValue(event.target.value)

    const handleBlur = (event) => {
        const { value } = event.target;
        setValue(value)
    }    
    
    return (
        <TextareaAutosize onBlur={handleBlur} className={classes.dialog} id="textArea" rowsMin={30} value={value} onChange={handleChange} />
    )
}

export default CoursePage