import React, { useState, createContext } from "react"
import { Snackbar } from "@material-ui/core"
import { default as Alert } from "@material-ui/lab/Alert"

const NotificationsContext = createContext({
  notifyUser: (message, severity) => undefined,
})

const NotificationsProvider = ({ children }) => {
  const [open, setOpen] = useState(false)
  const [message, setMessage] = useState("")
  const [severity, setSeverity] = useState("success")

  function notifyUser(message, severity) {
    setMessage(message)
    setSeverity(severity)
    setOpen(true)
  }

  return (
    <NotificationsContext.Provider value={{ notifyUser }}>
      {children}
      <Snackbar
        anchorOrigin={{
          vertical: "bottom",
          horizontal: "center",
        }}
        open={open}
        autoHideDuration={60000}
        onClose={() => setOpen(false)}
        message={message}
      >
        <Alert
          severity={severity}
          onClose={() => setOpen(false)}
          variant="filled"
        >
          {message}
        </Alert>
      </Snackbar>
    </NotificationsContext.Provider>
  )
}

export { NotificationsProvider, NotificationsContext }
