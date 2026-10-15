from fastapi import WebSocket


async def websocket_endpoint(websocket: Websocket):
    """Простейший WebSocket-эндпоинт для проверки соединения"""
    