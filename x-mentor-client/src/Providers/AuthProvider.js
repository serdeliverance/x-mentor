import React, { createContext } from "react"

const AuthContext = createContext({
  getTokens: () => undefined,
  setTokens: (tokens) => undefined
})

const AuthProvider = ({ children }) => {

  function getTokens() {
    return JSON.parse(localStorage.getItem("token"))
  }

  function setTokens(authTokens) {
    localStorage.setItem("token", authTokens)
  }

  return (
    <AuthContext.Provider value={{ getTokens, setTokens }}>
      {children}
    </AuthContext.Provider>
  )
}

export { AuthProvider, AuthContext }
