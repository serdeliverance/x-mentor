import React, { createContext, useState } from "react"

const AuthContext = createContext()

const AuthProvider = ({ children }) => {
  const getTokens = () => {
    return JSON.parse(localStorage.getItem("token"))
  }
  
  const [isLoggedIn, setIsLoggedIn] = useState(getTokens() ? true : false)

  const login = (data) => {
    localStorage.setItem("token", JSON.stringify(data))
    setIsLoggedIn(true)
  }

  const logout = () => {
    localStorage.removeItem("token")
    setIsLoggedIn(false)
  }

  return (
    <AuthContext.Provider value={{ getTokens, login, logout, isLoggedIn }}>
      {children}
    </AuthContext.Provider>
  )
}

export { AuthProvider, AuthContext }
