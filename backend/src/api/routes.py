from fastapi import APIRouter, HTTPException
from pydantic import BaseModel

from backend.src.core.base.engine import AbstractMatchEngine
from backend.src.core.factory import create_engine

router = APIRouter(prefix="/api/match", tags=["match"])


    _current_engine: AbstractMatchEngine | None = None
    
    