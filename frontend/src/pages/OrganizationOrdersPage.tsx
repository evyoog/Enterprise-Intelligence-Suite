import { ClipboardList as PHClipboardList } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Alert, Box, Button, Chip, FormControl, InputLabel, MenuItem,
  Paper, Select, type SelectChangeEvent, TextField, Typography,
} from '@mui/material'
import { ApiError } from '../api/client'
import { ordersApi, type Order, type OrderStatus } from '../api/ordersApi'
import { productsApi, type Product } from '../api/productsApi'
import { PageHeader } from '../components/layout/PageHeader'

/**
 * "/organization/orders" — 09 Order & Provisioning Management (sprint
 * 2027.1.1), organization purchasing only (an individual customer's
 * self-serve subscribe flow stays on MyProductsPage/MySubscriptionsPage,
 * unaffected). Any member sees the request form and their own orders;
 * an ORG_ADMIN (MANAGE_ORDERS) also sees pending approvals below — the
 * backend, not this page, is what actually enforces who may approve.
 */
export function OrganizationOrdersPage() {
  const { t } = useTranslation()
  const [products, setProducts] = useState<Product[]>([])
  const [selectedProductId, setSelectedProductId] = useState<number | ''>('')
  const [selectedPlanId, setSelectedPlanId] = useState<number | ''>('')
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState<string | null>(null)

  const [myOrders, setMyOrders] = useState<Order[] | null>(null)
  const [pendingOrders, setPendingOrders] = useState<Order[] | null>(null)
  const [isOrgAdmin, setIsOrgAdmin] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)
  const [busyId, setBusyId] = useState<number | null>(null)
  const [notes, setNotes] = useState<Record<number, string>>({})

  const load = () => {
    productsApi.list().then(setProducts).catch(() => {})
    ordersApi.myOrders().then(setMyOrders).catch(() => setMyOrders([]))
    ordersApi.pendingOrders()
      .then((orders) => { setIsOrgAdmin(true); setPendingOrders(orders) })
      .catch(() => { setIsOrgAdmin(false); setPendingOrders(null) })
  }

  useEffect(load, [])

  const selectedProduct = products.find((p) => p.id === selectedProductId)

  const submit = async () => {
    if (selectedProductId === '') return
    setSubmitting(true)
    setFormError(null)
    try {
      await ordersApi.submit(selectedProductId, selectedPlanId === '' ? null : selectedPlanId)
      setSelectedProductId('')
      setSelectedPlanId('')
      load()
    } catch (e) {
      setFormError(e instanceof ApiError ? e.message : 'Could not submit this request.')
    } finally {
      setSubmitting(false)
    }
  }

  const decide = async (order: Order, approve: boolean) => {
    setBusyId(order.id)
    setActionError(null)
    try {
      const updated = approve ? await ordersApi.approve(order.id, notes[order.id]) : await ordersApi.reject(order.id, notes[order.id])
      setPendingOrders((prev) => prev?.filter((o) => o.id !== updated.id) ?? prev)
    } catch (e) {
      setActionError(e instanceof ApiError ? e.message : 'That action could not be completed.')
    } finally {
      setBusyId(null)
    }
  }

  const cancel = async (order: Order) => {
    setBusyId(order.id)
    setActionError(null)
    try {
      const updated = await ordersApi.cancel(order.id)
      setMyOrders((prev) => prev?.map((o) => (o.id === updated.id ? updated : o)) ?? prev)
    } catch (e) {
      setActionError(e instanceof ApiError ? e.message : 'That action could not be completed.')
    } finally {
      setBusyId(null)
    }
  }

  return (
    <Box>
      <PageHeader icon={PHClipboardList} accent="amber" area="organization" title={t('orders.title')} subtitle={t('orders.subtitle')} />

      {actionError && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setActionError(null)}>{actionError}</Alert>}

      <Paper variant="outlined" sx={{ p: 2.5, mb: 3 }}>
        <Typography sx={{ fontWeight: 700, mb: 1.5 }}>{t('orders.requestForm.title')}</Typography>
        {formError && <Alert severity="error" sx={{ mb: 2 }}>{formError}</Alert>}
        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'flex-start' }}>
          <FormControl sx={{ minWidth: 220 }}>
            <InputLabel id="order-product-label">{t('orders.requestForm.product')}</InputLabel>
            <Select
              labelId="order-product-label"
              label={t('orders.requestForm.product')}
              value={selectedProductId}
              onChange={(e: SelectChangeEvent<number | ''>) => { setSelectedProductId(e.target.value === '' ? '' : Number(e.target.value)); setSelectedPlanId('') }}
            >
              {products.map((product) => (
                <MenuItem key={product.id} value={product.id}>{product.name}</MenuItem>
              ))}
            </Select>
          </FormControl>
          {selectedProduct && selectedProduct.plans.length > 0 && (
            <FormControl sx={{ minWidth: 220 }}>
              <InputLabel id="order-plan-label">{t('orders.requestForm.plan')}</InputLabel>
              <Select
                labelId="order-plan-label"
                label={t('orders.requestForm.plan')}
                value={selectedPlanId}
                onChange={(e: SelectChangeEvent<number | ''>) => setSelectedPlanId(e.target.value === '' ? '' : Number(e.target.value))}
              >
                <MenuItem value="">{t('orders.requestForm.noPlan')}</MenuItem>
                {selectedProduct.plans.map((plan) => (
                  <MenuItem key={plan.id} value={plan.id}>{plan.name}</MenuItem>
                ))}
              </Select>
            </FormControl>
          )}
          <Button variant="contained" disabled={selectedProductId === '' || submitting} onClick={submit}>
            {t('orders.requestForm.submit')}
          </Button>
        </Box>
      </Paper>

      {isOrgAdmin && (
        <Box sx={{ mb: 3 }}>
          <Typography variant="h6" component="h5" sx={{ fontWeight: 700, mb: 1.5 }}>{t('orders.pending.title')}</Typography>
          {pendingOrders && pendingOrders.length === 0 && (
            <Typography sx={{ color: 'text.secondary' }}>{t('orders.pending.none')}</Typography>
          )}
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
            {pendingOrders?.map((order) => (
              <Paper key={order.id} variant="outlined" sx={{ p: 2, display: 'flex', flexDirection: 'column', gap: 1 }}>
                <Typography sx={{ fontWeight: 700 }}>{order.productName}{order.planName ? ` — ${order.planName}` : ''}</Typography>
                <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('orders.requestedBy', { name: order.requestedByName })}</Typography>
                <TextField
                  size="small"
                  label={t('orders.pending.note')}
                  value={notes[order.id] ?? ''}
                  onChange={(e) => setNotes((prev) => ({ ...prev, [order.id]: e.target.value }))}
                />
                <Box sx={{ display: 'flex', gap: 1 }}>
                  <Button size="small" variant="contained" disabled={busyId === order.id} onClick={() => decide(order, true)}>
                    {t('orders.pending.approve')}
                  </Button>
                  <Button size="small" color="error" variant="outlined" disabled={busyId === order.id} onClick={() => decide(order, false)}>
                    {t('orders.pending.reject')}
                  </Button>
                </Box>
              </Paper>
            ))}
          </Box>
        </Box>
      )}

      <Box>
        <Typography variant="h6" component="h5" sx={{ fontWeight: 700, mb: 1.5 }}>{t('orders.myOrders')}</Typography>
        {myOrders && myOrders.length === 0 && (
          <Typography sx={{ color: 'text.secondary' }}>{t('orders.noOrders')}</Typography>
        )}
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
          {myOrders?.map((order) => (
            <Paper key={order.id} variant="outlined" sx={{ p: 2, display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 1 }}>
              <Box>
                <Typography sx={{ fontWeight: 700 }}>{order.productName}{order.planName ? ` — ${order.planName}` : ''}</Typography>
                {order.decidedByName && (
                  <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('orders.decidedBy', { name: order.decidedByName })}</Typography>
                )}
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <StatusChip status={order.status} />
                {order.status === 'SUBMITTED' && (
                  <Button size="small" disabled={busyId === order.id} onClick={() => cancel(order)}>{t('orders.cancel')}</Button>
                )}
              </Box>
            </Paper>
          ))}
        </Box>
      </Box>
    </Box>
  )
}

const STATUS_COLOR: Record<OrderStatus, 'success' | 'error' | 'warning' | 'default'> = {
  APPROVED: 'success',
  REJECTED: 'error',
  SUBMITTED: 'warning',
  CANCELLED: 'default',
}

function StatusChip({ status }: { status: OrderStatus }) {
  const { t } = useTranslation()
  return <Chip size="small" color={STATUS_COLOR[status]} label={t(`orders.status.${status}`)} />
}
