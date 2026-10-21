import unittest
from backend.src.core.sports.tennis.engine import TennisEngine
from backend.src.core.base.engine import MatchStatus


class TestTennisEngine(unittest.TestCase):
    
    def setUp(self) -> None:
        """Вызывается перед каждым тестом."""