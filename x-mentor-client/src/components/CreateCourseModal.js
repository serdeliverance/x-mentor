import React, { useState } from 'react'
import Button from '@material-ui/core/Button'
import TextField from '@material-ui/core/TextField'
import Dialog from '@material-ui/core/Dialog'
import DialogActions from '@material-ui/core/DialogActions'
import DialogContent from '@material-ui/core/DialogContent'
import DialogTitle from '@material-ui/core/DialogTitle'

export default function CreateCourseModal({open, setOpen}) {

  const handleLogin = () => {
    setOpen(false)
  }

  const handleCancel = () => {
    setOpen(false)
  }

  return (
    <Dialog open={open} onClose={handleCancel} aria-labelledby="form-dialog-title">
        <DialogTitle id="form-dialog-title">Course</DialogTitle>
        <DialogContent>
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
                id="content"
                label="Content"
                type="text"
                fullWidth
            />
            <TextField
                autoFocus
                margin="dense"
                id="preview"
                label="Preview"
                type="file"
                accept="image/*"
                fullWidth
            />
        </DialogContent>
        <DialogActions>
            <Button onClick={handleCancel} color="primary">
                Cancel
            </Button>
            <Button onClick={handleLogin} color="primary">
                Login
            </Button>
        </DialogActions>
    </Dialog>
  )
}