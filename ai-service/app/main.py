from contextlib import asynccontextmanager
from typing import Literal

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

from app.embeddings import MAX_TEXT_CHARS, MAX_TEXTS, state


@asynccontextmanager
async def lifespan(_: FastAPI):
    state.configure()
    yield


app = FastAPI(title="EIS AI Service", version="0.2.0", lifespan=lifespan)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}


class EmbedRequest(BaseModel):
    texts: list[str] = Field(min_length=1, max_length=MAX_TEXTS)
    kind: Literal["query", "passage"] = "passage"


@app.get("/embed/info")
def embed_info() -> dict[str, object]:
    """REQ-PRT-003: which embedding model is loaded."""
    return state.info()


@app.post("/embed")
def embed(request: EmbedRequest) -> dict[str, object]:
    """REQ-PRT-003: turn texts into vectors (one per text, L2-normalised)."""
    embedder = state.embedder
    if embedder is None:
        raise HTTPException(status_code=503, detail=state.message or "Embeddings are not available.")
    texts = [t[:MAX_TEXT_CHARS] for t in request.texts]
    return {
        "provider": embedder.provider,
        "model": embedder.model,
        "dimension": embedder.dimension,
        "vectors": embedder.embed(texts, request.kind),
    }
