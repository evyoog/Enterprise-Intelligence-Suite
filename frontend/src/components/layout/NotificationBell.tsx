import { useEffect, useState } from 'react'
import {
  Badge, Box, Button, Checkbox, CircularProgress, FormControlLabel,
  IconButton, Popover, Tooltip, Typography,
} from '@mui/material'
import { Bell, Settings } from 'lucide-react'
import { ApiError } from '../../api/client'
import {
  notificationsApi, type Notification, type NotificationCategory, type NotificationPreferences,
} from '../../api/notificationsApi'

const CATEGORY_LABELS: Record<NotificationCategory, string> = {
  SECURITY: 'Security',
  SUBSCRIPTION: 'Subscriptions',
  ORGANIZATION: 'Organization',
  PRIVILEGED_ACCESS: 'Privileged access',
  SYSTEM: 'System',
}
const ALL_CATEGORIES = Object.keys(CATEGORY_LABELS) as NotificationCategory[]

const SEVERITY_COLOR: Record<Notification['severity'], string> = {
  INFO: '#3b82f6',
  WARNING: '#d97706',
  CRITICAL: '#dc2626',
}

function timeAgo(iso: string): string {
  const seconds = Math.floor((Date.now() - new Date(iso).getTime()) / 1000)
  if (seconds < 60) return 'just now'
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) return `${minutes}m ago`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}h ago`
  return `${Math.floor(hours / 24)}d ago`
}

/** The notification center's own trigger + panel — lives in the navbar,
 * shown only for authenticated users (SiteNavbar already gates its own
 * "My Products"/"Admin Panel" links the same way). */
export function NotificationBell() {
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null)
  const [unreadCount, setUnreadCount] = useState(0)
  const [notifications, setNotifications] = useState<Notification[] | null>(null)
  const [showPreferences, setShowPreferences] = useState(false)
  const [preferences, setPreferences] = useState<NotificationPreferences | null>(null)
  const [error, setError] = useState<string | null>(null)

  const refresh = () => {
    notificationsApi.list()
      .then((summary) => { setUnreadCount(summary.unreadCount); setNotifications(summary.notifications) })
      .catch(() => {})
  }

  useEffect(() => {
    refresh()
    const interval = setInterval(refresh, 60_000)
    return () => clearInterval(interval)
  }, [])

  const open = Boolean(anchorEl)

  const handleOpen = (e: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(e.currentTarget)
    setShowPreferences(false)
    refresh()
  }

  const handleClose = () => setAnchorEl(null)

  const openPreferences = () => {
    setShowPreferences(true)
    notificationsApi.getPreferences().then(setPreferences).catch(() => {})
  }

  const toggleCategory = (category: NotificationCategory) => {
    if (!preferences) return
    const next = preferences.emailDisabledCategories.includes(category)
      ? preferences.emailDisabledCategories.filter((c) => c !== category)
      : [...preferences.emailDisabledCategories, category]
    setPreferences({ emailDisabledCategories: next })
    notificationsApi.updatePreferences({ emailDisabledCategories: next }).catch(() => {})
  }

  const handleNotificationClick = (notification: Notification) => {
    if (notification.read) return
    notificationsApi.markRead(notification.id)
      .then(refresh)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not update this notification.'))
  }

  const handleMarkAllRead = () => {
    notificationsApi.markAllRead().then(refresh).catch(() => {})
  }

  return (
    <>
      <Tooltip title="Notifications">
        <IconButton
          onClick={handleOpen}
          size="small"
          sx={{ color: 'inherit' }}
          aria-label={unreadCount > 0 ? `Notifications, ${unreadCount} unread` : 'Notifications'}
        >
          <Badge badgeContent={unreadCount} color="error" max={99}>
            <Bell size={19} />
          </Badge>
        </IconButton>
      </Tooltip>

      <Popover
        open={open}
        anchorEl={anchorEl}
        onClose={handleClose}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Box sx={{ width: 360, maxHeight: 480, display: 'flex', flexDirection: 'column' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', p: 1.5, borderBottom: '1px solid', borderColor: 'divider' }}>
            <Typography sx={{ fontWeight: 700 }}>
              {showPreferences ? 'Notification preferences' : 'Notifications'}
            </Typography>
            <Box sx={{ display: 'flex', gap: 0.5 }}>
              {!showPreferences && unreadCount > 0 && (
                <Button size="small" onClick={handleMarkAllRead}>Mark all read</Button>
              )}
              <Tooltip title={showPreferences ? 'Back to notifications' : 'Preferences'}>
                <IconButton
                  size="small"
                  aria-label={showPreferences ? 'Back to notifications' : 'Notification preferences'}
                  onClick={() => showPreferences ? setShowPreferences(false) : openPreferences()}
                >
                  <Settings size={16} />
                </IconButton>
              </Tooltip>
            </Box>
          </Box>

          {error && <Typography color="error" role="alert" sx={{ p: 1.5, fontSize: 13 }}>{error}</Typography>}

          {showPreferences ? (
            <Box sx={{ p: 1.5, overflowY: 'auto' }}>
              <Typography variant="caption" sx={{ color: 'text.secondary', display: 'block', mb: 1 }}>
                Uncheck a category to stop receiving its emails. You'll still see it here in the notification center.
              </Typography>
              {!preferences && <CircularProgress size={20} />}
              {preferences && ALL_CATEGORIES.map((category) => (
                <FormControlLabel
                  key={category}
                  sx={{ display: 'flex', m: 0 }}
                  control={
                    <Checkbox
                      size="small"
                      checked={!preferences.emailDisabledCategories.includes(category)}
                      onChange={() => toggleCategory(category)}
                    />
                  }
                  label={`Email me about ${CATEGORY_LABELS[category]}`}
                />
              ))}
            </Box>
          ) : (
            <Box sx={{ overflowY: 'auto' }}>
              {notifications === null && (
                <Box sx={{ display: 'flex', justifyContent: 'center', py: 3 }}><CircularProgress size={22} /></Box>
              )}
              {notifications?.length === 0 && (
                <Typography sx={{ color: 'text.secondary', p: 2, fontSize: 13 }}>
                  Nothing yet — you're all caught up.
                </Typography>
              )}
              {notifications?.map((notification) => (
                <Box
                  key={notification.id}
                  onClick={() => handleNotificationClick(notification)}
                  sx={{
                    display: 'flex', gap: 1, p: 1.5, cursor: notification.read ? 'default' : 'pointer',
                    bgcolor: notification.read ? 'transparent' : 'action.hover',
                    borderBottom: '1px solid', borderColor: 'divider',
                    '&:hover': { bgcolor: 'action.hover' },
                  }}
                >
                  <Box sx={{ width: 8, height: 8, borderRadius: '50%', mt: 0.75, flexShrink: 0, bgcolor: SEVERITY_COLOR[notification.severity], opacity: notification.read ? 0.25 : 1 }} />
                  <Box sx={{ minWidth: 0 }}>
                    <Typography sx={{ fontWeight: notification.read ? 500 : 700, fontSize: 14 }}>
                      {notification.title}
                    </Typography>
                    <Typography sx={{ fontSize: 13, color: 'text.secondary' }}>
                      {notification.message}
                    </Typography>
                    <Typography variant="caption" sx={{ color: 'text.disabled' }}>
                      {CATEGORY_LABELS[notification.category]} · {timeAgo(notification.createdAt)}
                    </Typography>
                  </Box>
                </Box>
              ))}
            </Box>
          )}
        </Box>
      </Popover>
    </>
  )
}
