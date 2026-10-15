from fastapi import WebSocket


async def websocket_endpoint(websocket: WebSocket):
    """Простейший WebSocket-эндпоинт для проверки соединения"""
    await websocket.accept()
    await websocket.send_json({"message": "connected"})
    
    while True:
        data = await websocket.receive_text()
        await websocket.send_json({"echo": data})