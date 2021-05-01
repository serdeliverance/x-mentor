import React, { useState } from 'react'
import { Button, Dialog, TextField, DialogActions, DialogContent, Snackbar, DialogTitle, Tooltip, makeStyles } from '@material-ui/core'
import axios from 'axios'
import { API_URL } from '../environment'
import MuiAlert from '@material-ui/lab/Alert'
import HelpIcon from '@material-ui/icons/Help'

function Alert(props) {
  return <MuiAlert elevation={6} variant="filled" {...props} />
}

const useStyles = makeStyles(() => ({
  title: {
    textAlign: "center"
  },
  tooltip: {
    maxWidth: "42ch"
  }
}))

export default function LoginModal({settings, setSettings, setLoggedIn}) {
  const classes = useStyles()

  const [loginForm, setLoginForm] = useState({
    username: "",
    password: ""
  })
  const [alert, setAlert] = useState({
    open: false,
    severity: "",
    message: ""
  })

  const keyPress = (e) => {
    if(e.keyCode === 13 && loginForm.username && loginForm.password){
      handleAuth()
    }
  }

  const handleAuth = async () => {
    try{
      const response = await axios.post(
        `${API_URL}${settings.endpoint}`,
        loginForm
      )
      localStorage.setItem("token", JSON.stringify(response.data))
      setLoggedIn(true)
      setSettings({...settings, open: false})
    }
    catch (error){
      console.error(error)
      setAlert({open: true, severity: "error", message: "There was an error"})
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
    <>
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
    <Snackbar open={alert.open} autoHideDuration={6000} onClose={() => setAlert({...alert, open: false})}>
      <Alert onClose={() => setAlert({...alert, open: false})} severity={alert.severity}>
          {alert.message}
      </Alert>
    </Snackbar>
    </>
  )
}