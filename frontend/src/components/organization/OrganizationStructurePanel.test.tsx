import '../../i18n'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { renderWithProviders } from '../../test/renderWithProviders'
import { OrganizationStructurePanel } from './OrganizationStructurePanel'

const tree = vi.fn()
const detail = vi.fn()
const create = vi.fn()
const move = vi.fn()
const remove = vi.fn()
const importCsv = vi.fn()
const history = vi.fn()
const listUsers = vi.fn()

vi.mock('../../api/orgHierarchyApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/orgHierarchyApi')>('../../api/orgHierarchyApi')
  return {
    ...actual,
    orgHierarchyApi: {
      tree: () => tree(),
      detail: (id: number) => detail(id),
      history: (id: number) => history(id),
      create: (parentId: number, input: unknown) => create(parentId, input),
      update: vi.fn(),
      move: (id: number, parent: number) => move(id, parent),
      remove: (id: number) => remove(id),
      placeMember: vi.fn(),
      removeMember: vi.fn(),
      importCsv: (file: File) => importCsv(file),
      levels: vi.fn(),
      saveLevels: vi.fn(),
    },
  }
})
vi.mock('../../api/registrationApi', async () => {
  const actual = await vi.importActual<typeof import('../../api/registrationApi')>('../../api/registrationApi')
  return { ...actual, registrationApi: { ...actual.registrationApi, listMyOrgUsers: () => listUsers() } }
})

const levels = [
  { type: 'ORGANIZATION', label: 'Organization', rank: 0 },
  { type: 'DIVISION', label: 'Division', rank: 1 },
  { type: 'DEPARTMENT', label: 'Department', rank: 3 },
]
const node = (id: number, parentId: number | null, name: string, type: string, extra = {}) => ({
  id, parentId, name, type, code: null, description: null, sortOrder: 0, active: true,
  childCount: 0, memberCount: 0, createdAt: '2026-10-07T00:00:00Z', updatedAt: '2026-10-07T00:00:00Z', ...extra,
})
const nodes = [
  node(1, null, 'Acme', 'ORGANIZATION', { childCount: 2 }),
  node(2, 1, 'Engineering', 'DIVISION', { childCount: 1, code: 'ENG' }),
  node(3, 1, 'Sales', 'DIVISION'),
  node(4, 2, 'Platform', 'DEPARTMENT'),
]

describe('OrganizationStructurePanel', () => {
  beforeEach(() => {
    for (const m of [tree, detail, create, move, remove, importCsv, history, listUsers]) m.mockReset()
    tree.mockResolvedValue({ levels, nodes })
    detail.mockImplementation(async (id: number) => ({ node: nodes.find((n) => n.id === id), path: ['Acme'], members: [] }))
    history.mockResolvedValue([])
    listUsers.mockResolvedValue([])
  })

  it('shows the org chart cards, opens the top levels, collapses and expands a branch and has no accessibility violations', async () => {
    const { container } = renderWithProviders(<OrganizationStructurePanel />)
    expect(await screen.findByRole('article', { name: 'Acme' })).toBeInTheDocument()
    expect(screen.getByRole('article', { name: 'Engineering' })).toBeInTheDocument()
    expect(screen.getByRole('article', { name: 'Platform' })).toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Collapse Engineering' }))
    expect(screen.queryByRole('article', { name: 'Platform' })).not.toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: 'Expand Engineering' }))
    expect(await screen.findByRole('article', { name: 'Platform' })).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('opens the details panel from a card', async () => {
    renderWithProviders(<OrganizationStructurePanel />)
    await userEvent.setup().click(await screen.findByRole('button', { name: 'Details: Engineering' }))
    expect(await screen.findByRole('heading', { name: 'Engineering' })).toBeInTheDocument()
    expect(detail).toHaveBeenCalledWith(2)
  })

  it('search highlights matches and dims the rest', async () => {
    renderWithProviders(<OrganizationStructurePanel />)
    const user = userEvent.setup()
    await user.type(await screen.findByLabelText('Search nodes'), 'platform')
    expect(await screen.findByRole('article', { name: 'Platform' })).toHaveAttribute('data-match', 'true')
    expect(screen.getByRole('article', { name: 'Sales' })).toHaveAttribute('data-match', 'false')
  })

  it('Add node is enabled without a selection and adds under the root; the backend reason is shown on failure', async () => {
    create.mockRejectedValueOnce(new ApiError(409, 'A node named Ops already exists under this parent'))
    renderWithProviders(<OrganizationStructurePanel />)
    const user = userEvent.setup()
    await screen.findByRole('article', { name: 'Acme' })
    const add = screen.getByRole('button', { name: 'Add node' })
    expect(add).toBeEnabled()
    await user.click(add)
    await user.type(await screen.findByLabelText(/^Name/), 'Ops')
    await user.click(screen.getByRole('button', { name: 'Save' }))
    expect(await screen.findByText(/already exists under this parent/)).toBeInTheDocument()
    expect(create).toHaveBeenCalledWith(1, expect.objectContaining({ name: 'Ops', type: 'DIVISION' }))
  })

  it('the root card offers no move, deactivate or delete; other cards do', async () => {
    renderWithProviders(<OrganizationStructurePanel />)
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: 'Actions for Acme' }))
    expect(await screen.findByRole('menuitem', { name: 'Edit' })).toBeInTheDocument()
    expect(screen.queryByRole('menuitem', { name: 'Delete' })).not.toBeInTheDocument()
    expect(screen.queryByRole('menuitem', { name: 'Move' })).not.toBeInTheDocument()
    await user.keyboard('{Escape}')
    await user.click(screen.getByRole('button', { name: 'Actions for Sales' }))
    expect(await screen.findByRole('menuitem', { name: 'Delete' })).toBeInTheDocument()
  })

  it('imports a CSV and lists failed rows', async () => {
    importCsv.mockResolvedValue({ created: 2, failed: 1, errors: [{ row: 4, message: 'Parent not found: X' }] })
    renderWithProviders(<OrganizationStructurePanel />)
    const user = userEvent.setup()
    await screen.findByRole('article', { name: 'Acme' })
    await user.click(screen.getByRole('button', { name: 'Import CSV' }))
    expect(await screen.findByText(/name,type,code,description,parentName/)).toBeInTheDocument()
    expect(screen.getByText(/Blank puts the node directly under the root/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Download sample CSV' })).toBeInTheDocument()
    const input = await screen.findByLabelText('CSV file')
    await user.upload(input, new File(['name,type\nA,Division'], 'nodes.csv', { type: 'text/csv' }))
    await user.click(screen.getByRole('button', { name: 'Import' }))
    expect(await screen.findByText('2 created, 1 failed.')).toBeInTheDocument()
    expect(screen.getByText('Row 4: Parent not found: X')).toBeInTheDocument()
  })

  it('shows a retry when the structure cannot be loaded', async () => {
    tree.mockRejectedValueOnce(new ApiError(403, 'You do not have permission to do this'))
    renderWithProviders(<OrganizationStructurePanel />)
    expect(await screen.findByText(/could not be loaded/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Retry' })).toBeInTheDocument()
  })
})
