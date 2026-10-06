import math

from fastapi.testclient import TestClient

from app.embeddings import STUB_DIMENSION, StubEmbedder, state
from app.main import app


def _client(monkeypatch, provider: str | None) -> TestClient:
    monkeypatch.delenv("EMBEDDING_MODEL", raising=False)
    if provider is None:
        monkeypatch.delenv("EMBEDDING_PROVIDER", raising=False)
    else:
        monkeypatch.setenv("EMBEDDING_PROVIDER", provider)
    return TestClient(app)


def test_disabled_without_configuration(monkeypatch) -> None:
    with _client(monkeypatch, None) as client:
        info = client.get("/embed/info").json()
        assert info["enabled"] is False
        response = client.post("/embed", json={"texts": ["hello"]})
        assert response.status_code == 503


def test_stub_returns_normalised_vectors(monkeypatch) -> None:
    with _client(monkeypatch, "stub") as client:
        info = client.get("/embed/info").json()
        assert info == {"enabled": True, "provider": "stub", "model": "stub-hashing-v1",
                        "dimension": STUB_DIMENSION, "message": None}
        body = client.post("/embed", json={"texts": ["Pagar una factura", "pay an invoice"], "kind": "query"}).json()
        assert body["dimension"] == STUB_DIMENSION
        assert len(body["vectors"]) == 2
        for vector in body["vectors"]:
            assert len(vector) == STUB_DIMENSION
            assert math.isclose(math.sqrt(sum(v * v for v in vector)), 1.0, rel_tol=1e-6)


def test_stub_is_deterministic_and_accent_insensitive() -> None:
    stub = StubEmbedder()
    a, b = stub.embed(["Facturación mensual", "facturacion MENSUAL"], "passage")
    assert a == b


def test_stub_similar_texts_score_higher() -> None:
    stub = StubEmbedder()
    query, near, far = stub.embed(["invoice payment", "pay your invoices online", "reset your password"], "query")
    dot = lambda x, y: sum(p * q for p, q in zip(x, y))  # noqa: E731
    assert dot(query, near) > dot(query, far)


def test_rejects_empty_and_unknown_provider(monkeypatch) -> None:
    with _client(monkeypatch, "stub") as client:
        assert client.post("/embed", json={"texts": []}).status_code == 422
    with _client(monkeypatch, "nonsense") as client:
        assert client.get("/embed/info").json()["enabled"] is False
    state.configure()
