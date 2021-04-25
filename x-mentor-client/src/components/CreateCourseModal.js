import React, { useState } from 'react'
import { Button, makeStyles, TextField, Dialog, DialogActions, DialogContent, DialogTitle, Switch, Box } from '@material-ui/core'
import axios from 'axios'

const useStyles = makeStyles(() => ({
    preview: {
        height: "12rem"
    },
    content: {
        height: "12rem"
    },
    paper: {
        minWidth: 600
    }
}));
  

export default function CreateCourseModal({open, setOpen}) {
    const classes = useStyles()
    const [preview, setPreview] = useState("")
    const [content, setContent] = useState("")
    const [isContentUrl, setIsContentUrl] = useState(false)

    const handleCreate = async () => {
        setOpen(false)
        const result = await axios.post(
            `http://localhost:9000/courses`
        )
    }

    const handleClose = () => {
        setOpen(false)
        setPreview("")
        setContent("")
        setIsContentUrl(false)
    }

    const handlePreview = (event) => {
        const file = event.target.files[0]
        if(file){
            const base64 = toBase64(file).then(image => {
                setPreview(image)
            })
        }
    }

    const handleContent = (event) => {
        if(isContentUrl){
            const video = event.target.value
            if(video.match("youtube.com")){
                const videoId = youtube_parser(video)
                setContent(`https://www.youtube.com/embed/${videoId}`)
            }
        }
        else {
            const file = event.target.files[0]
            if(file){
                const base64 = toBase64(file).then(image => {
                    setContent(image)
                })
            }
        }
    }

    function youtube_parser(url){
        var regExp = /^.*((youtu.be\/)|(v\/)|(\/u\/\w\/)|(embed\/)|(watch\?))\??v?=?([^#&?]*).*/;
        var match = url.match(regExp);
        return (match&&match[7].length==11)? match[7] : false;
    }

    const toBase64 = file => new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => resolve(reader.result);
        reader.onerror = error => reject(error);
    });

    return (
        <Dialog open={open} onClose={handleClose} classes={{paperWidthSm: classes.paper}} aria-labelledby="form-dialog-title">
            <DialogTitle id="form-dialog-title">Course</DialogTitle>
            <DialogContent dividers>
                <TextField
                    autoFocus
                    margin="dense"
                    id="title"
                    label="Title"
                    type="text"
                    fullWidth
                    inputProps={{ maxLength: 50 }}
                />
                <TextField
                    autoFocus
                    margin="dense"
                    id="description"
                    label="Description"
                    type="text"
                    multiline
                    fullWidth
                    inputProps={{ maxLength: 256 }}
                />
                <TextField
                    autoFocus
                    margin="dense"
                    id="preview"
                    label="Preview"
                    type="file"
                    accept="image/*"
                    autoComplete="off"
                    tabIndex="-1"
                    onChange={handlePreview}
                    fullWidth
                />
                {preview !== "" && <img alt="preview" className={classes.preview} src={`${preview}`}></img>}
                <Box display="flex" alignItems="center">
                    {
                        isContentUrl ?
                            <TextField
                                autoFocus
                                margin="dense"
                                id="content"
                                label="Content"
                                type="text"
                                placeholder="Youtube video"
                                onChange={handleContent}
                                fullWidth
                            /> : 
                        <>
                        <TextField
                            autoFocus
                            margin="dense"
                            id="content"
                            label="Content"
                            type="file"
                            accept="image/*"
                            autoComplete="off"
                            tabIndex="-1"
                            onChange={handleContent}
                            fullWidth
                        />
                        </>
                    }
                    <Switch
                        checked={isContentUrl}
                        onChange={() => {
                            setIsContentUrl(!isContentUrl)
                            setContent("")
                        }}
                        name="contentSwitch"
                        inputProps={{ 'aria-label': 'secondary checkbox' }}
                    />
                </Box>
                {content && content.startsWith("data:image") && !isContentUrl && <img alt="content" className={classes.content} src={`${content}`}></img>}
                {content && content.startsWith("data:video") && !isContentUrl && <video alt="content" className={classes.content} src={`${content}`} controls></video>}
                {content && isContentUrl && <iframe className={classes.content} src={`${content}`}></iframe>}
            </DialogContent>
            <DialogActions>
                <Button onClick={handleClose} color="primary">
                    Cancel
                </Button>
                <Button onClick={handleCreate} color="primary">
                    Create
                </Button>
            </DialogActions>
        </Dialog>
    )
}