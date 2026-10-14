import type { ConfigurationStatus, ConnectionStatus } from '../../api/paymentGatewayApi'

type ChipColor = 'default' | 'success' | 'warning' | 'error' | 'info'

/** C98: how each status is coloured. Configured, connected, active and default are four separate facts and are never merged into one. */
export const configurationColor = (s: ConfigurationStatus): ChipColor =>
  s === 'CONFIGURED' ? 'success' : s === 'CONFIGURATION_INCOMPLETE' ? 'warning' : 'default'

export const connectionColor = (s: ConnectionStatus): ChipColor =>
  s === 'CONNECTION_SUCCESSFUL' ? 'success'
    : s === 'AUTHENTICATION_FAILED' || s === 'TEST_FAILED' || s === 'UNSUPPORTED_CONFIGURATION' ? 'error'
      : s === 'PROVIDER_UNAVAILABLE' ? 'warning' : 'default'
