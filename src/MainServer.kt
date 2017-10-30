import java.net.ServerSocket

class MainServer(var ip: String?, private val port: Int) {

    val chatrooms = HashMap<Int, ChatRoom>()

    fun listen() {
        var serverSocket = ServerSocket(port)
        if (ip == null) {
            ip = serverSocket.inetAddress.hostAddress
        }
        println("Server listening on $ip:$port")
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
    if (args.size == 1) {
        var mainServer = MainServer(null, args[0].toInt())
        mainServer.listen()
        return
    }

    if (args.size == 2) {
        var mainServer = MainServer(args[0], args[1].toInt())
        mainServer.listen()
        return
    }

    var mainServer = MainServer("86.44.163.146", 2195)
    mainServer.listen()
}
