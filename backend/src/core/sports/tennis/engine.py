from typing import Any
from backend.src.core.base.engine import AbstractMatchEngine, MatchStatus


class TennisEngine(AbstractMatchEngine):
    
    SETS_TO_WIN = 2
    GAMES_TO_WIN_SET = 6
    POINTS_TO_WIN_GAME = 4
    TIEBREAK_POINTS = 7
    
    def __init__(self) -> None:
        super().__init__("tennis")
        self.points = [0, 0]
        self.games = [0, 0]
        self.sets_won = [0, 0]

    def start_match(self, player1: str, player2: str, server_index: int) -> None:
        self.players = (player1, player2)
        self.status = MatchStatus.IN_PROGRESS
        self.current_server = server_index
        self.points = [0, 0]
        self.games = [0, 0]
        self.sets_won = [0, 0]
        self.winner = None
        self.scoring_history.clear()

    def add_point(self, player_index: int) -> None:
        ...

    def remove_point(self) -> None:
        ...

    def set_score(self, state: dict[str, Any]) -> None:
        ...

    def get_score(self) -> dict[str, Any]:
        return{
            "sport": self.sport_name,
            "players": list(self.players),
            "status": self.status.name,
            "points": list(self.points),
            "games": list(self.games),
            "sets_won": list(self.sets_won),
            "server": self.current_server,
            "winner": self.winner,
        }