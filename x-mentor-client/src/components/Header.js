import React, { useState } from 'react';
import { Button, Link, Badge, InputBase, Typography, IconButton, Toolbar, AppBar, fade, makeStyles } from '@material-ui/core';
import SearchIcon from '@material-ui/icons/Search';
import AccountCircle from '@material-ui/icons/AccountCircle';
import MailIcon from '@material-ui/icons/Mail';
import NotificationsIcon from '@material-ui/icons/Notifications';
import { useHistory } from "react-router-dom";
import LoginModal from './LoginModal';

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
  },
  inputInput: {
    padding: theme.spacing(1, 1, 1, 0),
    // vertical padding + font size from searchIcon
    paddingLeft: `calc(1em + ${theme.spacing(4)}px)`,
    transition: theme.transitions.create('width'),
    width: '100%',
    [theme.breakpoints.up('md')]: {
      width: '50ch',
    },
  },
  link: {
    padding: theme.spacing(1, 4, 1, 0),
    '&:hover': {
        textDecoration: "none"
    }
  }
}));

export default function Header() {
  const classes = useStyles();
  const history = useHistory();
  const [loggedIn, setLoggedIn] = useState(false);
  const [openLogin, setOpenLogin] = useState(false);

  const keyPress = (e) => {
    if(e.keyCode === 13){
        history.push("/courses")
    }
  }

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
                    inputProps={{ 'aria-label': 'search' }}
                    onChange={() => console.log("change")}
                    onKeyDown={keyPress}
                />
            </div>
            <div className={classes.grow} />
            {loggedIn ?
            <div>
                <Link className={classes.link} component="button" onClick={() => history.push("/my/courses")} style={{"padding": "8px 24px 8px 0px"}} color="inherit">My Courses</Link>
                <IconButton aria-label="show 4 new mails" color="inherit">
                    <Badge badgeContent={4} color="secondary">
                        <MailIcon />
                    </Badge>
                </IconButton>
                <IconButton aria-label="show 17 new notifications" color="inherit">
                    <Badge badgeContent={17} color="secondary">
                        <NotificationsIcon />
                    </Badge>
                </IconButton>
                <IconButton
                    edge="end"
                    aria-label="account of current user"
                    aria-controls='primary-search-account-menu'
                    aria-haspopup="true"
                    color="inherit"
                >
                    <AccountCircle />
                </IconButton>
            </div>
            :
            <div>
                <Link className={classes.link} component="button" onClick={() => history.push("/my/courses")} style={{"padding": "8px 32px 8px 0px"}} color="inherit">My Courses</Link>
                <Button variant="outlined" color="inherit" onClick={() => setOpenLogin(true)}>Login</Button>
                <LoginModal open={openLogin} setOpen={setOpenLogin}></LoginModal>
            </div>
        }
        </Toolbar>
      </AppBar>
    </div>
  );
}