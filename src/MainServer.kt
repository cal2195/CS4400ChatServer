import java.net.ServerSocket

class MainServer(private val port: Int) {

    val chatrooms = HashMap<Int, ChatRoom>()

    fun listen() {
        var serverSocket = ServerSocket(port)
        println("Server listening ton port $port")
        while (true) {
            println("Waiting for new client")
            var clientSocket = serverSocket.accept()
            println("Client accepted!")

            var clientThread = Thread(ClientThread(clientSocket, this))
            clientThread.start()
        }
    }

    fun shutdown() {
        System.exit(0)
    }

    fun joinChatRoom(clientThread: ClientThread, chatroomName: String, clientName: String) {
        var chatroom = chatrooms[chatroomName.hashCode()]

        if (chatroom == null) {
            chatroom = ChatRoom(chatroomName)
            chatrooms.put(chatroomName.hashCode(), chatroom)
        }

        chatroom.clients.put(clientName.hashCode(), clientThread)
    }
}

fun main(args: Array<String>) {
    var mainServer = MainServer(2195)
    mainServer.listen()
}
