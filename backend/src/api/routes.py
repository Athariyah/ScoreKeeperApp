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
    
class AddPointRequest(BaseModel):
    player_index: int
    
class SetScoreRequest(BaseModel):
    sets: list[int]
    sets_won: list[int]
    current_set: int
    server: int
    status: str
    winner: int | None
    
    
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

@router.post("/point")
def add_point(request: AddPointRequest):
    """Добавление очка игроку"""
    if _current_engine is None:
        raise HTTPException(status_code=404, detail="No active match")
    
    if _current_engine.is_match_over():
        raise HTTPException(status_code=409, detail="Match is already finished")
    
    if request.player_index not in (0, 1):
        raise HTTPException(status_code=400, detail="player_index must be 0 or 1")
    
    _current_engine.add_point(request.player_index)
    return _current_engine.get_score()

@router.post("/reset")
def reset_match():
    """Сброс состояния матча."""
    if _current_engine is None:
        raise HTTPException(status_code=404, detail="No active match")
    _current_engine.reset()
    return _current_engine.get_score()

@router.post("/force-end")
def force_end():
    """Принудительное завершение матча."""    
    if _current_engine is None:
        raise HTTPException(status_code=404, detail="No active match")
    
    if _current_engine.is_match_over():
        raise HTTPException(status_code=409, detail="Match is already finished")
    
    _current_engine.force_end()
    return _current_engine.get_score()

@router.post("/remove-point")
def remove_point():
    """Убирает последнее начисленное очко."""
    if _current_engine is None:
        raise HTTPException(status_code=404, detail="No active match")
    
    if _current_engine.is_match_over():
        raise HTTPException(status_code=409, detail="Match is already finished")
    
    _current_engine.remove_point()
    return _current_engine.get_score()

@router.post("/set-score")
def set_score(request: SetScoreRequest):
    """Полностью перезаписывает состояние матча."""
    if _current_engine is None:
        raise HTTPException(status_code=404, detail="No active match")
    
    _current_engine.set_score(request.model_dump())
    return _current_engine.get_score()