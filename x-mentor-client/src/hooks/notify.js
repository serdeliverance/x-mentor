import { useContext } from "react"
import { NotificationsContext } from "../Providers/NotificationsProvider"

export function useNotification() {
  const { notifyUser } = useContext(NotificationsContext)
  return (message, severity) => notifyUser(message, severity)
}
