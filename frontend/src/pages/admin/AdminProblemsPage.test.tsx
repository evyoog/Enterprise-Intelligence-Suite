import '../../i18n'
import { fireEvent, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import type { Problem, ProblemDetail } from '../../api/problemsApi'
import type { AdminIncident } from '../../api/serviceStatusApi'
import { renderWithProviders } from '../../test/renderWithProviders'
import { AdminProblemsPage } from './AdminProblemsPage'

const list = vi.fn()
const get = vi.fn()
const create = vi.fn()
const update = vi.fn()
const linkIncident = vi.fn()
const unlinkIncident = vi.fn()
const addAction = vi.fn()
const updateAction = vi.fn()
const deleteAction = vi.fn()
const incidents = vi.fn()

vi.mock('../../api/problemsApi', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../api/problemsApi')>()
  return {
    ...actual,
    problemsApi: {
      list: () => list(),
      get: (id: number) => get(id),
      create: (b: unknown) => create(b),
      update: (id: number, b: unknown) => update(id, b),
      linkIncident: (id: number, i: number) => linkIncident(id, i),
      unlinkIncident: (id: number, i: number) => unlinkIncident(id, i),
      addAction: (id: number, b: unknown) => addAction(id, b),
      updateAction: (id: number, a: number, b: unknown) => updateAction(id, a, b),
      deleteAction: (id: number, a: number) => deleteAction(id, a),
    },
  }
})
vi.mock('../../api/serviceStatusApi', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../../api/serviceStatusApi')>()
  return { ...actual, adminServiceStatusApi: { incidents: () => incidents() } }
})

const problem = (over: Partial<Problem> = {}): Problem => ({
  id: 1, title: 'Auth cache poisoning', description: 'Stale tokens', status: 'OPEN', rootCause: null, incidentCount: 1, actionCount: 1, openActionCount: 1,
  createdAt: '2026-10-01T08:00:00Z', ...over,
})
const detail = (over: Partial<Problem> = {}, extra: Partial<ProblemDetail> = {}): ProblemDetail => ({
  problem: problem(over),
  incidents: [{ id: 10, title: 'Login errors', productName: 'Valam.ai', phase: 'INVESTIGATING', severity: 'MAJOR', startedAt: '2026-10-01T08:00:00Z' }],
  actions: [{ id: 100, description: 'Shorten the cache TTL', owner: 'Ravi', dueOn: '2026-11-01', done: false }],
  ...extra,
})
const incident = (id: number, title: string, problemId: number | null): AdminIncident => ({
  id, productId: 1, productName: 'Thiran.ai', title, message: 'm', startedAt: '2026-10-01T08:00:00Z', endedAt: null, open: true, severity: 'MINOR', phase: 'LOGGED',
  problemId, problemTitle: null, tickets: [],
})

describe('AdminProblemsPage (C101 IC-2)', () => {
  beforeEach(() => {
    for (const m of [list, get, create, update, linkIncident, unlinkIncident, addAction, updateAction, deleteAction, incidents]) m.mockReset()
    list.mockResolvedValue([problem(), problem({ id: 2, title: 'Old DNS issue', status: 'CLOSED', incidentCount: 2, actionCount: 2, openActionCount: 0, rootCause: 'TTL' })])
    incidents.mockResolvedValue([incident(10, 'Login errors', 1), incident(11, 'Slow exports', null), incident(12, 'Timeouts', null)])
    get.mockResolvedValue(detail())
  })

  const open = async (name = 'Open problem Auth cache poisoning') => {
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name }))
    const dialog = await screen.findByRole('dialog', { name: /^Problem:/ })
    await within(dialog).findByText('Shorten the cache TTL')
    return { user, dialog }
  }

  it('lists problems with status, incidents and open/total actions, and has no accessibility violations', async () => {
    const { container } = renderWithProviders(<AdminProblemsPage />)
    const row = (await screen.findByText('Auth cache poisoning')).closest('tr')!
    expect(within(row).getByText('Open')).toBeInTheDocument()
    expect(within(row).getByText('1 open / 1')).toBeInTheDocument()
    const closedRow = screen.getByText('Old DNS issue').closest('tr')!
    expect(within(closedRow).getByText('Closed')).toBeInTheDocument()
    expect(within(closedRow).getByText('0 open / 2')).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('offers a retry when the list cannot be loaded and an empty state when there are no problems', async () => {
    list.mockRejectedValueOnce(new ApiError(500, 'Boom'))
    renderWithProviders(<AdminProblemsPage />)
    expect(await screen.findByText(/Boom/)).toBeInTheDocument()
    list.mockResolvedValue([])
    await userEvent.setup().click(screen.getByRole('button', { name: 'Retry' }))
    expect(await screen.findByText('No problems')).toBeInTheDocument()
  })

  it('creates a problem and links the incidents picked (only those not already in a problem are listed)', async () => {
    create.mockResolvedValue(detail({ title: 'Cache bug' }))
    renderWithProviders(<AdminProblemsPage />)
    const user = userEvent.setup()
    await user.click((await screen.findAllByRole('button', { name: 'New problem' }))[0])
    const dialog = await screen.findByRole('dialog', { name: 'New problem' })
    await waitFor(() => expect(within(dialog).getByRole('checkbox', { name: /Slow exports/ })).toBeInTheDocument())
    expect(within(dialog).queryByRole('checkbox', { name: /Login errors/ })).not.toBeInTheDocument()
    expect(await axe(dialog)).toHaveNoViolations()
    await user.click(within(dialog).getByRole('button', { name: 'Create problem' }))
    expect(within(dialog).getByText('Enter a title.')).toBeInTheDocument()
    expect(create).not.toHaveBeenCalled()
    await user.type(within(dialog).getByRole('textbox', { name: /^Title/ }), 'Cache bug')
    await user.type(within(dialog).getByRole('textbox', { name: 'Description' }), 'Stale tokens')
    await user.click(within(dialog).getByRole('checkbox', { name: /Slow exports/ }))
    await user.click(within(dialog).getByRole('checkbox', { name: /Timeouts/ }))
    await user.click(within(dialog).getByRole('button', { name: 'Create problem' }))
    await waitFor(() => expect(create).toHaveBeenCalledWith({ title: 'Cache bug', description: 'Stale tokens', incidentIds: [11, 12] }))
    expect(await screen.findByText('Problem "Cache bug" created')).toBeInTheDocument()
    expect(list).toHaveBeenCalledTimes(2)
  })

  it('blocks moving on without a root cause, explains why, and allows it once the root cause is written', async () => {
    update.mockResolvedValue(detail({ status: 'ROOT_CAUSE_IDENTIFIED', rootCause: 'Cache TTL too long' }))
    renderWithProviders(<AdminProblemsPage />)
    const { user, dialog } = await open()
    expect(await axe(dialog)).toHaveNoViolations()
    expect(within(dialog).getByText('Cannot move to "Root cause identified": write the root cause first.')).toBeInTheDocument()
    expect(within(dialog).getByText('Cannot move to "Closed": only a resolved problem can be closed.')).toBeInTheDocument()
    await user.click(within(dialog).getByRole('combobox', { name: 'Move to' }))
    expect(await screen.findByRole('option', { name: 'Root cause identified' })).toHaveAttribute('aria-disabled', 'true')
    expect(screen.getByRole('option', { name: 'Resolved' })).toHaveAttribute('aria-disabled', 'true')
    await user.keyboard('{Escape}')

    await user.type(within(dialog).getByRole('textbox', { name: /^Root cause/ }), 'Cache TTL too long')
    expect(within(dialog).queryByText(/write the root cause first/)).not.toBeInTheDocument()
    await user.click(within(dialog).getByRole('combobox', { name: 'Move to' }))
    await user.click(await screen.findByRole('option', { name: 'Root cause identified' }))
    await user.click(within(dialog).getByRole('button', { name: 'Change status' }))
    await waitFor(() => expect(update).toHaveBeenCalledWith(1, { status: 'ROOT_CAUSE_IDENTIFIED', rootCause: 'Cache TTL too long' }))
    expect(await screen.findByText('Status changed to Root cause identified')).toBeInTheDocument()
  }, 15000)

  it('blocks closing a resolved problem while a corrective action is open, then allows it once every action is done', async () => {
    get.mockResolvedValue(detail({ status: 'RESOLVED', rootCause: 'Cache TTL too long' }))
    updateAction.mockResolvedValue(detail({ status: 'RESOLVED', rootCause: 'Cache TTL too long', openActionCount: 0 }, {
      actions: [{ id: 100, description: 'Shorten the cache TTL', owner: 'Ravi', dueOn: '2026-11-01', done: true, doneAt: '2026-10-05T00:00:00Z' }],
    }))
    update.mockResolvedValue(detail({ status: 'CLOSED', rootCause: 'Cache TTL too long', openActionCount: 0 }))
    renderWithProviders(<AdminProblemsPage />)
    const { user, dialog } = await open()
    expect(within(dialog).getByText('Cannot move to "Closed": 1 corrective action is still open.')).toBeInTheDocument()
    await user.click(within(dialog).getByRole('combobox', { name: 'Move to' }))
    expect(await screen.findByRole('option', { name: 'Closed' })).toHaveAttribute('aria-disabled', 'true')
    await user.keyboard('{Escape}')

    await user.click(within(dialog).getByRole('checkbox', { name: 'Done: Shorten the cache TTL' }))
    expect(updateAction).toHaveBeenCalledWith(1, 100, { done: true })
    await waitFor(() => expect(within(dialog).queryByText(/corrective action is still open/)).not.toBeInTheDocument())
    await user.click(within(dialog).getByRole('combobox', { name: 'Move to' }))
    await user.click(await screen.findByRole('option', { name: 'Closed' }))
    await user.click(within(dialog).getByRole('button', { name: 'Change status' }))
    await waitFor(() => expect(update).toHaveBeenCalledWith(1, { status: 'CLOSED', rootCause: 'Cache TTL too long' }))
    // closed is final: the dialog turns read-only
    expect(await within(dialog).findByText(/This problem is closed/)).toBeInTheDocument()
    expect(within(dialog).queryByRole('button', { name: 'Change status' })).not.toBeInTheDocument()
  }, 15000)

  it('shows a closed problem read-only', async () => {
    get.mockResolvedValue(detail({ id: 2, title: 'Old DNS issue', status: 'CLOSED', rootCause: 'TTL', openActionCount: 0, closedAt: '2026-10-03T00:00:00Z' }, {
      actions: [{ id: 5, description: 'Raise TTL', done: true }],
    }))
    renderWithProviders(<AdminProblemsPage />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'View problem Old DNS issue' }))
    const dialog = await screen.findByRole('dialog', { name: /^Problem:/ })
    expect(await within(dialog).findByText(/This problem is closed/)).toBeInTheDocument()
    expect(within(dialog).getByRole('textbox', { name: /^Title/ })).toBeDisabled()
    expect(within(dialog).getByRole('textbox', { name: /^Root cause/ })).toBeDisabled()
    expect(within(dialog).getByRole('checkbox', { name: 'Done: Raise TTL' })).toBeDisabled()
    for (const name of ['Save details', 'Change status', 'Link incident', 'Add action', 'Remove']) {
      expect(within(dialog).queryByRole('button', { name })).not.toBeInTheDocument()
    }
  })

  it('links an incident that is not in a problem yet and removes a linked one', async () => {
    linkIncident.mockResolvedValue(detail({ incidentCount: 2 }, {
      incidents: [
        { id: 10, title: 'Login errors', productName: 'Valam.ai', phase: 'INVESTIGATING', severity: 'MAJOR', startedAt: '2026-10-01T08:00:00Z' },
        { id: 11, title: 'Slow exports', productName: 'Thiran.ai', phase: 'LOGGED', severity: 'MINOR', startedAt: '2026-10-01T08:00:00Z' },
      ],
    }))
    unlinkIncident.mockResolvedValue(detail({ incidentCount: 0 }, { incidents: [] }))
    renderWithProviders(<AdminProblemsPage />)
    const { user, dialog } = await open()
    await user.click(within(dialog).getByRole('combobox', { name: 'Add an incident' }))
    // Login errors is already in this problem, so only the free ones are offered
    expect((await screen.findAllByRole('option')).map((o) => o.textContent)).toEqual(['Slow exports · Thiran.ai', 'Timeouts · Thiran.ai'])
    await user.click(screen.getByRole('option', { name: 'Slow exports · Thiran.ai' }))
    await user.click(within(dialog).getByRole('button', { name: 'Link incident' }))
    expect(linkIncident).toHaveBeenCalledWith(1, 11)
    expect(await within(dialog).findByText('Slow exports')).toBeInTheDocument()
    await user.click(within(dialog).getByRole('button', { name: 'Remove incident Login errors from this problem' }))
    expect(unlinkIncident).toHaveBeenCalledWith(1, 10)
    expect(await within(dialog).findByText('No incidents linked yet.')).toBeInTheDocument()
  }, 15000)

  it('adds, edits and deletes corrective actions, deleting only after confirmation', async () => {
    addAction.mockResolvedValue(detail({ actionCount: 2 }, { actions: [
      { id: 100, description: 'Shorten the cache TTL', owner: 'Ravi', dueOn: '2026-11-01', done: false },
      { id: 101, description: 'Add a canary', owner: 'Mei', dueOn: '2026-12-01', done: false },
    ] }))
    updateAction.mockResolvedValue(detail({}, { actions: [{ id: 100, description: 'Shorten the cache TTL', owner: 'Sam', dueOn: '2026-11-15', done: false }] }))
    deleteAction.mockResolvedValue(detail({ actionCount: 0, openActionCount: 0 }, { actions: [] }))
    renderWithProviders(<AdminProblemsPage />)
    const { user, dialog } = await open()

    await user.type(within(dialog).getByRole('textbox', { name: 'New action' }), 'Add a canary')
    await user.type(within(dialog).getByRole('textbox', { name: 'Owner (optional)' }), 'Mei')
    fireEvent.change(within(dialog).getByLabelText('Due date (optional)'), { target: { value: '2026-12-01' } })
    await user.click(within(dialog).getByRole('button', { name: 'Add action' }))
    expect(addAction).toHaveBeenCalledWith(1, { description: 'Add a canary', owner: 'Mei', dueOn: '2026-12-01' })
    expect(await within(dialog).findByText('Add a canary')).toBeInTheDocument()

    await user.click(within(dialog).getByRole('button', { name: 'Edit action Shorten the cache TTL' }))
    const owner = within(dialog).getByRole('textbox', { name: 'Owner' })
    await user.clear(owner)
    await user.type(owner, 'Sam')
    fireEvent.change(within(dialog).getByLabelText('Due date'), { target: { value: '2026-11-15' } })
    await user.click(within(dialog).getByRole('button', { name: 'Save' }))
    await waitFor(() => expect(updateAction).toHaveBeenCalledWith(1, 100, { description: 'Shorten the cache TTL', owner: 'Sam', dueOn: '2026-11-15' }))

    await user.click(await within(dialog).findByRole('button', { name: 'Delete action Shorten the cache TTL' }))
    const confirm = await screen.findByRole('dialog', { name: 'Delete this action?' })
    await user.click(within(confirm).getByRole('button', { name: 'Cancel' }))
    expect(deleteAction).not.toHaveBeenCalled()
    await waitFor(() => expect(screen.queryByRole('dialog', { name: 'Delete this action?' })).not.toBeInTheDocument())
    await user.click(await within(dialog).findByRole('button', { name: 'Delete action Shorten the cache TTL' }))
    await user.click(within(await screen.findByRole('dialog', { name: 'Delete this action?' })).getByRole('button', { name: 'Delete' }))
    await waitFor(() => expect(deleteAction).toHaveBeenCalledWith(1, 100))
    expect(await within(dialog).findByText('No corrective actions yet.')).toBeInTheDocument()
  }, 25000)

  it('saves the details and shows what the server refuses', async () => {
    update.mockRejectedValueOnce(new ApiError(400, 'A closed problem cannot be changed.'))
    renderWithProviders(<AdminProblemsPage />)
    const { user, dialog } = await open()
    await user.type(within(dialog).getByRole('textbox', { name: /^Root cause/ }), 'Cache')
    await user.click(within(dialog).getByRole('button', { name: 'Save details' }))
    expect(await within(dialog).findByText('A closed problem cannot be changed.')).toBeInTheDocument()
    expect(update).toHaveBeenCalledWith(1, { title: 'Auth cache poisoning', description: 'Stale tokens', rootCause: 'Cache' })
  })
})
