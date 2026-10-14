import argparse

import uvicorn


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", type=int, default="8000")
    parser.add_argument("--host", type=int, default="127.0.0.1")
    args = parser.parse_args()
    
    uvicorn.run(
        "backend.src.api.main:app"
        host = args.host,
        post=args.port,
        reload=False,
    )
    

if __name__ == "__main__":
    main()