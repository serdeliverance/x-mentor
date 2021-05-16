import React, { useState, useContext, useEffect } from 'react'
import { Button, Link, Badge, InputBase, Typography, IconButton, Toolbar, AppBar, fade, makeStyles, Popover, Grid, Divider } from '@material-ui/core'
import SearchIcon from '@material-ui/icons/Search'
import AccountCircle from '@material-ui/icons/AccountCircle'
import NotificationsIcon from '@material-ui/icons/Notifications'
import { useHistory } from "react-router-dom"
import LoginModal from './LoginModal'
import AddIcon from '@material-ui/icons/Add'
import Tooltip from '@material-ui/core/Tooltip'
import CreateCourseModal from './CreateCourseModal'
import { AuthContext } from '../Providers/AuthProvider'
import { API_URL } from '../environment'

const useStyles = makeStyles((theme) => ({
  grow: {
    flexGrow: 1,
  },
  title: {
    display: 'none',
    cursor: 'pointer',
    [theme.breakpoints.up('sm')]: {
      display: 'block',
    },
  },
  search: {
    position: 'relative',
    borderRadius: theme.shape.borderRadius,
    backgroundColor: fade(theme.palette.common.white, 0.15),
    '&:hover': {
      backgroundColor: fade(theme.palette.common.white, 0.25),
    },
    marginRight: theme.spacing(2),
    marginLeft: 0,
    width: '40%',
    [theme.breakpoints.up('sm')]: {
      marginLeft: theme.spacing(3),
    },
  },
  searchIcon: {
    padding: theme.spacing(0, 2),
    height: '100%',
    position: 'absolute',
    pointerEvents: 'none',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  inputRoot: {
    color: 'inherit',
    width: '100%'
  },
  inputInput: {
    padding: theme.spacing(1, 1, 1, 0),
    paddingLeft: `calc(1em + ${theme.spacing(4)}px)`,
    transition: theme.transitions.create('width'),
  },
  link: {
    padding: theme.spacing(1, 4, 1, 0),
    '&:hover': {
        textDecoration: "none"
    }
  },
  button: {
    margin: theme.spacing(1, 2, 1, 0)
  },
  errorBorder: {
    border: "red solid 1px",
    borderRadius: "4px"
  },
  loginBtn: {
    marginRight: "2rem"
  },
  emptyNotification: {
    padding: theme.spacing(2),
    fontSize: 14
  },
  notification: {
    padding: theme.spacing(2, 0, 2, 2),
    width: "100%",
    fontSize: 14
  },
  notificationPreview:{
    width: "3rem",
    height: "3rem",
    borderRadius: "2rem",
    objectFit: "contain",
    border: "#d0d0d0 1px solid",
    backgroundColor: "white"
  },
  notificationDate: {
    fontSize: 10,
    padding: theme.spacing(0, 2, 0.5, 0),
  },
  notificationContainer: {
    width: 360
  },
  notificationButton: {
    textTransform: "none",
    textAlign: "left"
  }
}));

export default function Header() {
  const classes = useStyles()
  const history = useHistory()
  const [openCourseModal, setOpenCourseModal] = useState(false)
  const [searchValue, setSearchValue] = useState("")
  const [notifications, setNotifications] = useState([])
  const { isLoggedIn, logout } = useContext(AuthContext)
  const [authSettings, setAuthSettings] = useState({
    mode: "",
    open: false,
    endpoint: "",
    title: ""
  })
  const [anchorEl, setAnchorEl] = React.useState(null);

  const handleClick = (event) => setAnchorEl(event.currentTarget)

  const handleClose = () => {
    setAnchorEl(null)
    const updatedNotifications = [...notifications]
    updatedNotifications.forEach(notification => notification.read = true)
    setNotifications(updatedNotifications)
  }
  const showNotifications = Boolean(anchorEl)

  const keyPress = (e) => {
    const value = e.target.value
    if(e.keyCode === 13){
      history.push(`/courses?q=${value}`)
      setSearchValue("")
    }
  }

  const handleLogout = () => logout()

  const handleLogin = () => {
    setAuthSettings({
      mode: "login",
      open: true,
      endpoint: "/login",
      title: "Login"
    })
  }

  const handleSignup = () => {
    setAuthSettings({
      mode: "signup",
      open: true,
      endpoint: "/signup",
      title: "Sign Up"
    })
  }

  const checkCourse = (courseName) => {
    history.push(`/courses?q=${courseName}`)
  }

  useEffect(() => {
    if(isLoggedIn){
      const sse = new EventSource(`${API_URL}/notifications`,
      { withCredentials: true });
      function getRealtimeData(event) {
        const data = event.data
        if(data){
          setNotifications(prevState => [JSON.parse(data), ...prevState])
        }
      }
      sse.onmessage = e => getRealtimeData(e)
      sse.onerror = (error) => {
        console.log(error)        
        sse.close()
      }
      return () => {
        sse.close()
      }
    }
  }, [isLoggedIn])

  return (
    <div className={classes.grow}>
      <AppBar position="static">
        <Toolbar>
            <Typography variant="h6" className={classes.title} onClick={() => history.push("/")} noWrap>
                X-Mentor
            </Typography>
            <div className={classes.search}>
                <div className={classes.searchIcon}>
                    <SearchIcon />
                </div>
                <InputBase
                  placeholder="Search…"
                  classes={{
                      root: classes.inputRoot,
                      input: classes.inputInput,
                  }}
                  value={searchValue}
                  inputProps={{ 'aria-label': 'search' }}
                  onChange={e => setSearchValue(e.target.value)}
                  onKeyDown={keyPress}
                />
            </div>
            <div className={classes.grow} />
            {isLoggedIn ?
            <>
            <div>
              <IconButton aria-label="show new notifications" color="inherit" onClick={handleClick}>
                <Badge badgeContent={notifications.filter(notification => !notification.read).length} color="secondary">
                  <NotificationsIcon />
                </Badge>
              </IconButton>
              <Popover
                id="notifications"
                open={showNotifications}
                anchorEl={anchorEl}
                onClose={handleClose}
                anchorOrigin={{
                  vertical: 'bottom',
                  horizontal: 'center',
                }}
                transformOrigin={{
                  vertical: 'top',
                  horizontal: 'center',
                }}
              >
                {notifications.length === 0 ?
                  <Typography className={classes.emptyNotification}>No New Notifications</Typography>
                :
                notifications.map(notification => (
                  <>
                  <Grid container justify="center" className={classes.notificationContainer}>
                    <Button className={classes.notificationButton} color="inherit" onClick={() => checkCourse(notification.title)}>
                      <Grid container item xs={2} justify="flex-end" alignItems="center">
                        <img alt="preview" className={classes.notificationPreview} src={`${notification.preview}`}></img>
                      </Grid>
                      <Grid container item xs={10} justify="flex-end">
                        <Typography className={classes.notification}>New Course Created: {notification.title}</Typography>
                        <Typography className={classes.notificationDate}>{new Date(notification.createdAt).toUTCString()}</Typography>
                      </Grid>
                    </Button>
                  </Grid>
                  <Divider />
                  </>
                ))}
              </Popover>
              <Tooltip title="Create Course" arrow>
                <IconButton
                    color="inherit"
                    aria-label="create"
                    className={classes.button}
                    onClick={() => setOpenCourseModal(true)}>
                  <AddIcon />
                </IconButton>
              </Tooltip>
              <Link className={classes.link} component="button" onClick={() => history.push("/my/courses")} style={{"padding": "8px 24px 8px 0px"}} color="inherit">My Courses</Link>
              <Tooltip title="Logout" arrow>
                <IconButton
                    edge="end"
                    aria-label="account of current user"
                    aria-controls='primary-search-account-menu'
                    aria-haspopup="true"
                    color="inherit"
                    onClick={() => handleLogout()}
                >
                  <AccountCircle />
                </IconButton>
              </Tooltip>
            </div>
            <CreateCourseModal open={openCourseModal} setOpen={setOpenCourseModal}></CreateCourseModal>
            </>
            :
            <>
            <div>
                <Button variant="outlined" color="inherit" onClick={handleLogin} className={classes.loginBtn}>Login</Button>
                <Button variant="outlined" color="inherit" onClick={handleSignup}>Sign Up</Button>
                <LoginModal settings={authSettings} setSettings={setAuthSettings}></LoginModal>
            </div>
            </>
        }
        </Toolbar>
      </AppBar>
    </div>
  )
}