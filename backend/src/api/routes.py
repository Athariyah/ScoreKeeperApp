from fastapi import APIRouter, HTTPException
from pydantic import BaseModel

from backend.src.core.base.engine import AbstractMatchEngine
from backend.src.core.factory import create_engine

router = APIRouter(prefix="/api/match", tags=["match"])


_current_engine: AbstractMatchEngine | None = None

class StartMatchRequest(BaseModel):
    sport: str
    player1 : str
    player2 : str
    server_index: int
    
@router.post("/start")
def start_match(request: StartMatchRequest):
    """Начинает новый матч."""
    global _current_engine
    
    try:
        engine = create_engine(request.sport)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    
    engine.start_match(request.player1, request.player2, request.server_index)
    _current_engine = engine
    return _current_engine.get_score()

@router.get("/state")
def get_state():
    """Возвращает текущее состояние матча."""
    if _current_engine is None:
        raise HTTPException(status_code=404, detail="no active match")
    return _current_engine.get_score()