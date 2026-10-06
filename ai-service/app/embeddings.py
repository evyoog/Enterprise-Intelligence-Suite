"""Text embeddings for semantic search (REQ-PRT-003, C70).

Providers, chosen with environment variables from config/secrets.env:

* ``sentence-transformers`` — an open-source multilingual model (English and
  Spanish) run in this service. ``EMBEDDING_MODEL`` names the model (a
  Hugging Face model id or a local path). Which model is used is Not
  specified (C70); the backend's index expects 384-dimensional vectors.
  Needs ``pip install -r requirements-embeddings.txt``.
* ``stub`` — a deterministic word-hashing embedder for tests and local
  development without a model. It only finds texts that share words or word
  beginnings; it does not understand meaning. Never use it in production.

``EMBEDDING_PROVIDER`` empty: ``sentence-transformers`` when ``EMBEDDING_MODEL``
is set, otherwise embeddings are disabled (``/embed`` answers 503 and the
platform searches by keyword only).
"""

from __future__ import annotations

import hashlib
import math
import os
import re
import threading
import unicodedata
from typing import Protocol

STUB_DIMENSION = 384
MAX_TEXTS = 256
MAX_TEXT_CHARS = 8000
_WORD = re.compile(r"[^\W_]+", re.UNICODE)


class Embedder(Protocol):
    provider: str
    model: str
    dimension: int

    def embed(self, texts: list[str], kind: str) -> list[list[float]]: ...


def fold(text: str) -> str:
    decomposed = unicodedata.normalize("NFD", text)
    return "".join(c for c in decomposed if not unicodedata.combining(c)).lower()


class StubEmbedder:
    """Feature hashing of words and word beginnings, L2-normalised."""

    provider = "stub"
    model = "stub-hashing-v1"
    dimension = STUB_DIMENSION

    def embed(self, texts: list[str], kind: str) -> list[list[float]]:
        return [self._one(text) for text in texts]

    def _one(self, text: str) -> list[float]:
        vector = [0.0] * self.dimension
        for word in _WORD.findall(fold(text)):
            for feature, weight in ((word, 1.0), (word[:5], 0.6)):
                digest = hashlib.sha256(feature.encode("utf-8")).digest()
                index = int.from_bytes(digest[:4], "big") % self.dimension
                sign = 1.0 if digest[4] & 1 else -1.0
                vector[index] += sign * weight
        norm = math.sqrt(sum(v * v for v in vector))
        return [v / norm for v in vector] if norm else vector


class SentenceTransformerEmbedder:
    provider = "sentence-transformers"

    def __init__(self, model_name: str) -> None:
        from sentence_transformers import SentenceTransformer  # imported only when used

        self.model = model_name
        self._model = SentenceTransformer(model_name)
        self.dimension = int(self._model.get_sentence_embedding_dimension())
        self._query_prefix = os.environ.get("EMBEDDING_QUERY_PREFIX", "")
        self._passage_prefix = os.environ.get("EMBEDDING_PASSAGE_PREFIX", "")

    def embed(self, texts: list[str], kind: str) -> list[list[float]]:
        prefix = self._query_prefix if kind == "query" else self._passage_prefix
        vectors = self._model.encode([prefix + t for t in texts], normalize_embeddings=True)
        return [[float(x) for x in v] for v in vectors]


class EmbeddingState:
    """Holds the configured embedder; a real model loads on a background thread."""

    def __init__(self) -> None:
        self._lock = threading.Lock()
        self.embedder: Embedder | None = None
        self.message: str | None = None
        self.provider: str | None = None
        self.model: str | None = None

    def configure(self) -> None:
        provider = os.environ.get("EMBEDDING_PROVIDER", "").strip().lower()
        model = os.environ.get("EMBEDDING_MODEL", "").strip()
        if not provider:
            provider = "sentence-transformers" if model else ""
        self.provider = provider or None
        self.model = model or None
        self.embedder = None
        if not provider:
            self.message = "No embedding model configured (EMBEDDING_PROVIDER / EMBEDDING_MODEL)."
        elif provider == "stub":
            self.embedder = StubEmbedder()
            self.model = StubEmbedder.model
            self.message = None
        elif provider == "sentence-transformers":
            if not model:
                self.message = "EMBEDDING_MODEL is not set."
            else:
                self.message = "Loading model."
                threading.Thread(target=self._load, args=(model,), daemon=True).start()
        else:
            self.message = f"Unknown EMBEDDING_PROVIDER: {provider}"

    def _load(self, model: str) -> None:
        try:
            embedder = SentenceTransformerEmbedder(model)
        except Exception as error:  # noqa: BLE001 - reported through /embed/info
            with self._lock:
                self.message = f"Model could not be loaded: {error}"
            return
        with self._lock:
            self.embedder = embedder
            self.message = None

    def info(self) -> dict[str, object]:
        embedder = self.embedder
        return {
            "enabled": embedder is not None,
            "provider": embedder.provider if embedder else self.provider,
            "model": embedder.model if embedder else self.model,
            "dimension": embedder.dimension if embedder else None,
            "message": self.message,
        }


state = EmbeddingState()
