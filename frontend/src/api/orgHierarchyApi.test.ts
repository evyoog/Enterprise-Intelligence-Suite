import { describe, expect, it, vi } from 'vitest'

const apiRequest = vi.fn()
vi.mock('./client', () => ({ apiRequest: (...a: unknown[]) => apiRequest(...a) }))

import { orgHierarchyApi } from './orgHierarchyApi'

describe('orgHierarchyApi.tree', () => {
  it('turns the root parentId that the server omits (null fields are not serialized) into null', async () => {
    apiRequest.mockResolvedValue({ levels: [], nodes: [{ id: 1, name: 'Acme' }, { id: 2, parentId: 1, name: 'Eng' }] })
    const tree = await orgHierarchyApi.tree()
    expect(tree.nodes[0].parentId).toBeNull()
    expect(tree.nodes[1].parentId).toBe(1)
  })
})
