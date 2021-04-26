import React, { useState } from 'react'
import { Button, Dialog, TextField, DialogActions, DialogContent, DialogTitle, Snackbar } from '@material-ui/core'
import axios from 'axios'
import { API_URL } from '../environment'
import MuiAlert from '@material-ui/lab/Alert'

function Alert(props) {
  return <MuiAlert elevation={6} variant="filled" {...props} />;
}

export default function LoginModal({open, setOpen, setLoggedIn}) {
  const [loginForm, setLoginForm] = useState({
    username: "",
    password: ""
  })
  const [alert, setAlert] = useState({
    open: false,
    severity: "",
    message: ""
  })

  const handleLogin = async () => {
    try{
      const response = await axios.post(
        `${API_URL}/login`,
        loginForm
      )
      console.log(response)
      setAlert({open: true, severity: "success", message: "Course created!"})
      setLoggedIn(true)
      setOpen(false)
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
    setOpen(false)
  }

  return (
    <>
    <Dialog open={open} onClose={handleCancel} aria-labelledby="form-dialog-title">
        <DialogTitle id="form-dialog-title">Login</DialogTitle>
        <DialogContent>
            <TextField
                autoFocus
                margin="dense"
                id="username"
                label="Username"
                type="text"
                onChange={handleTextField}
                fullWidth
            />
            <TextField
                autoFocus
                margin="dense"
                id="password"
                label="Password"
                type="password"
                onChange={handleTextField}
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
    <Snackbar open={alert.open} autoHideDuration={6000} onClose={() => setAlert({...alert, open: false})}>
      <Alert onClose={() => setAlert({...alert, open: false})} severity={alert.severity}>
          {alert.message}
      </Alert>
    </Snackbar>
    </>
  )
}