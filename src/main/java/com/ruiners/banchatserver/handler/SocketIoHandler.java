package com.ruiners.banchatserver.handler;

import com.google.gson.Gson;
import com.ruiners.banchatserver.config.Config;
import com.ruiners.banchatserver.model.Client;
import com.ruiners.banchatserver.model.Message;
import io.socket.engineio.server.EngineIoServer;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SocketIoHandler {
    private final EngineIoServer serverEngine = new EngineIoServer();
    private final Map<Long, List<Client>> clients = new HashMap<>();
    private final Gson gson = new Gson();

    SocketIoHandler() {
        SocketIoServer serverSocket = new SocketIoServer(serverEngine);
        SocketIoNamespace namespace = serverSocket.namespace("/");
        clients.put(Config.DEFAULT_ROOM, new ArrayList<>());

        namespace.on("connection", socket -> {
            Client client = new Client(Config.DEFAULT_ROOM, (SocketIoSocket) socket[0]);
            clients.get(Config.DEFAULT_ROOM).add(client);

            client.getSocket().on("chat message", args -> {
                Message message = gson.fromJson((String) args[0], Message.class);
                DatabaseHandler.insertMessage(message);

                for (Client participant : clients.get(message.getRoom()))
                    participant.getSocket().send("chat message", args);

                System.out.println(client.getSocket().getId() + " to room " + message.getRoom() + " >> " + message.getMessage());
            });

            client.getSocket().on("enter room", room -> {
                client.setRoom((long) room[0]);
                System.out.println("client " + client.getSocket().getId() + " entered room " + (long) room[0]);
            });

            client.getSocket().send("last messages", gson.toJson(DatabaseHandler.getMessages(Config.DEFAULT_ROOM)));

            System.out.println("New connection " + client.getSocket().getId());
        });
    }

    @RequestMapping(value = "/socket.io/", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
    public void httpHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
        serverEngine.handleRequest(request, response);
    }

}
