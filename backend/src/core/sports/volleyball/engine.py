from typing import Any
from backend.src.core.base.engine import AbstractMatchEngine, MatchStatus


class VolleyballEngine(AbstractMatchEngine):
    
    SETS_TO_WIN = 2
    TIEBREAK_SET_NUMBER = 3
    REGULAR_SET_POINTS = 25
    TIEBREAK_SET_POINTS = 15
    
    def __init__(self) -> None:
        super().__init__("volleyball")
        self.current_set = 1
        self.current_set_points = [0, 0]
        self.sets_won = [0, 0]

    def add_point(self, player_index: int) -> None:
        self.current_set_points[player_index] += 1
        
    def start_match(self, player1: str, player2: str, server_index: int) -> None:
        self.status = MatchStatus.IN_PROGRESS
        self.current_server = server_index
        self.current_set = 1
        self.current_set_points = [0, 0]
        self.sets_won = [0, 0]
        self.winner = None
        self.players = (player1, player2)
        
    def get_score(self) -> dict[str, Any]:
        ...
    
    def _get_set_max(self) -> int:
        ...
    
    def _is_set_won_by(self, player_index: int) -> bool:
        ...
        
    