import '../i18n'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { axe } from 'jest-axe'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../api/client'
import { renderWithProviders } from '../test/renderWithProviders'
import { OrganizationStructurePage } from './OrganizationStructurePage'

const tree = vi.fn()
const detail = vi.fn()
const create = vi.fn()
const move = vi.fn()
const remove = vi.fn()
const importCsv = vi.fn()
const history = vi.fn()
const listUsers = vi.fn()

vi.mock('../api/orgHierarchyApi', async () => {
  const actual = await vi.importActual<typeof import('../api/orgHierarchyApi')>('../api/orgHierarchyApi')
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
vi.mock('../api/registrationApi', async () => {
  const actual = await vi.importActual<typeof import('../api/registrationApi')>('../api/registrationApi')
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

describe('OrganizationStructurePage', () => {
  beforeEach(() => {
    for (const m of [tree, detail, create, move, remove, importCsv, history, listUsers]) m.mockReset()
    tree.mockResolvedValue({ levels, nodes })
    detail.mockImplementation(async (id: number) => ({ node: nodes.find((n) => n.id === id), path: ['Acme'], members: [] }))
    history.mockResolvedValue([])
    listUsers.mockResolvedValue([])
  })

  it('shows the tree, expands a node and has no accessibility violations', async () => {
    const { container } = renderWithProviders(<OrganizationStructurePage />)
    const treeEl = await screen.findByRole('tree', { name: 'Organization structure' })
    expect(within(treeEl).getByText('Acme')).toBeInTheDocument()
    expect(within(treeEl).getByText('Engineering')).toBeInTheDocument()
    expect(within(treeEl).queryByText('Platform')).not.toBeInTheDocument()
    await userEvent.setup().click(screen.getByRole('button', { name: /^Engineering/ }))
    expect(await screen.findByRole('heading', { name: 'Engineering' })).toBeInTheDocument()
    expect(await axe(container)).toHaveNoViolations()
  })

  it('search shows matches with their parents', async () => {
    renderWithProviders(<OrganizationStructurePage />)
    const user = userEvent.setup()
    await user.type(await screen.findByLabelText('Search nodes'), 'platform')
    const treeEl = screen.getByRole('tree')
    expect(within(treeEl).getByText('Platform')).toBeInTheDocument()
    expect(within(treeEl).getByText('Engineering')).toBeInTheDocument()
    expect(within(treeEl).queryByText('Sales')).not.toBeInTheDocument()
  })

  it('creates a child under the selected node and shows the backend reason on failure', async () => {
    create.mockRejectedValueOnce(new ApiError(409, 'A node named Ops already exists under this parent'))
    renderWithProviders(<OrganizationStructurePage />)
    const user = userEvent.setup()
    await screen.findByRole('tree')
    await user.click(screen.getByRole('button', { name: 'Add node' }))
    await user.type(await screen.findByLabelText(/^Name/), 'Ops')
    await user.click(screen.getByRole('button', { name: 'Save' }))
    expect(await screen.findByText(/already exists under this parent/)).toBeInTheDocument()
    expect(create).toHaveBeenCalledWith(1, expect.objectContaining({ name: 'Ops', type: 'DIVISION' }))
  })

  it('does not offer move or delete for the root', async () => {
    renderWithProviders(<OrganizationStructurePage />)
    await screen.findByRole('tree')
    expect(await screen.findByRole('button', { name: 'Edit' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Delete' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Move' })).not.toBeInTheDocument()
  })

  it('imports a CSV and lists failed rows', async () => {
    importCsv.mockResolvedValue({ created: 2, failed: 1, errors: [{ row: 4, message: 'Parent not found: X' }] })
    renderWithProviders(<OrganizationStructurePage />)
    const user = userEvent.setup()
    await screen.findByRole('tree')
    await user.click(screen.getByRole('button', { name: 'Import CSV' }))
    const input = await screen.findByLabelText('CSV file')
    await user.upload(input, new File(['name,type\nA,Division'], 'nodes.csv', { type: 'text/csv' }))
    await user.click(screen.getByRole('button', { name: 'Import' }))
    expect(await screen.findByText('2 created, 1 failed.')).toBeInTheDocument()
    expect(screen.getByText('Row 4: Parent not found: X')).toBeInTheDocument()
  })

  it('shows a retry when the structure cannot be loaded', async () => {
    tree.mockRejectedValueOnce(new ApiError(403, 'You do not have permission to do this'))
    renderWithProviders(<OrganizationStructurePage />)
    expect(await screen.findByText(/could not be loaded/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Retry' })).toBeInTheDocument()
  })
})
