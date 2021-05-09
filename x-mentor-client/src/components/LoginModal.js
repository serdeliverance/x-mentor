import React, { useState, useContext } from 'react'
import { Button, Dialog, TextField, DialogActions, DialogContent, DialogTitle, Tooltip, makeStyles } from '@material-ui/core'
import axios from 'axios'
import { API_URL } from '../environment'
import HelpIcon from '@material-ui/icons/Help'
import { useNotification } from '../hooks/notify'
import { AuthContext } from '../Providers/AuthProvider'

const useStyles = makeStyles(() => ({
  title: {
    textAlign: "center"
  },
  tooltip: {
    maxWidth: "42ch"
  }
}))

export default function LoginModal({settings, setSettings}) {
  const classes = useStyles()
  const notify = useNotification()
  const { login } = useContext(AuthContext)

  const [loginForm, setLoginForm] = useState({
    username: "",
    password: ""
  })

  const keyPress = (e) => {
    if(e.keyCode === 13 && loginForm.username && loginForm.password){
      handleAuth()
    }
  }

  const handleAuth = async () => {
    if(loginForm.username && loginForm.password){
      try{
        const response = await axios.post(
          `${API_URL}${settings.endpoint}`,
          loginForm
        )
        login(response.data)
        setSettings({...settings, open: false})
      }
      catch (error){
        console.error(error)
        notify("There was an error", "error")
      }
    }
  }

  const handleTextField = (event) => {
    setLoginForm({
        ...loginForm,
        [event.target.id]: event.target.value
    })
  }

  const handleCancel = () => {
    setSettings({...settings, open: false})
  }

  return (
    <Dialog open={settings.open} onClose={handleCancel} aria-labelledby="form-dialog-title">
        <DialogTitle className={classes.title}>
          {settings.title}
          {settings.mode === "login" && <Tooltip fontSize="small" classes={{ tooltip: classes.tooltip }} placement="right"
            title="Psst... you can create an user or use this one username: codi.sipes	 / password: codi.sipes	">
            <HelpIcon/>
          </Tooltip>}
        </DialogTitle>
        <DialogContent>
            <TextField
                autoFocus
                margin="dense"
                id="username"
                label="Username"
                type="text"
                onChange={handleTextField}
                onKeyDown={keyPress}
                fullWidth
            />
            <TextField
                margin="dense"
                id="password"
                label="Password"
                type="password"
                onChange={handleTextField}
                onKeyDown={keyPress}
                fullWidth
            />
        </DialogContent>
        <DialogActions>
            <Button onClick={handleCancel} color="primary">
                Cancel
            </Button>
            <Button onClick={handleAuth} color="primary">
                { settings.title }
            </Button>
        </DialogActions>
    </Dialog>
  )
}