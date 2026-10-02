from abc import ABC, abstractmethod
from enum import Enum, auto
class MatchStatus(Enum):
    NOT_STARTED = auto()
    IN_PROGRESS = auto()
    FINISHED = auto()

class AbstractMatchEngine(ABC):

    def __init__(self, sport_name: str) -> None:
        self.status = MatchStatus.NOT_STARTED
        self.sport_name = sport_name
        self.current_server = 0
        self.winner = None
        self.players = ("", "")
        
    @abstractmethod
    def add_point(self, player_index: int) -> None:
        ...
