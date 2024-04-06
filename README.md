# Multithreaded HTTP Server
This project aims to develop a multithreaded HTTP server in Java. The server will handle multiple client connections simultaneously, improving performance and scalability.

## Features
- **Multithreaded Architecture:** The server utilizes multithreading to handle concurrent client requests efficiently. Each client connection is processed in a separate thread, allowing multiple clients to interact with the server simultaneously.
- **HTTP Protocol Support:** The server supports the HTTP protocol, allowing clients to send HTTP requests and receive responses. It can handle various HTTP methods such as GET, POST, PUT, DELETE, etc.
- **WebSocket Integration:** WebSocket support is integrated into the server, enabling real-time bidirectional communication between clients and the server. WebSocket messages are handled asynchronously in separate threads.
- **Chat Server Functionality:** The server includes functionality to run a chat application. Clients can join chat rooms, send messages, and receive messages from other users in the same room. Broadcasting mechanisms ensure that messages are relayed to all clients in the room.

- # Usage
- Git clone to your local repository.
  ```
  git clone https://github.com/j2695203/MyHttpServer.git
  ```
