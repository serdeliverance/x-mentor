import React, { useState } from 'react'
import { Button, Dialog, TextField, DialogActions, DialogContent, Snackbar, Typography, Box, makeStyles } from '@material-ui/core'
import axios from 'axios'
import { API_URL } from '../environment'
import MuiAlert from '@material-ui/lab/Alert'

function Alert(props) {
  return <MuiAlert elevation={6} variant="filled" {...props} />
}

const useStyles = makeStyles(() => ({
  arrowF: {
    animation: "$rotate 0.7s forwards"
  },

  arrowB: {
    animation: "$rotate 0.7s backwards"
  },

  "@keyframes rotate": {
    "0%": {
      transform: "rotate(0)"
    },
    "100%": {
      transform: "rotate(540deg)"
    }
  }
}))

export default function LoginModal({open, setOpen, setLoggedIn}) {
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
  const [isLogin, setIsLogin] = useState(false)

  const handleAuth = async () => {
    try{
      const endpoint = isLogin ? "/login" : "/signup"
      const response = await axios.post(
        `${API_URL}${endpoint}`,
        loginForm
      )
      console.log(response)
      localStorage.set("token", response)
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
        <Box display="flex" justifyContent="space-around" alignItems="center" mt={2}>
          <Typography>Login</Typography>
          <Typography>Sign Up</Typography>
        </Box>
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
            <Button onClick={handleAuth} color="primary">
                { isLogin ? "Sign up" : "Login" }
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