import java.net.ServerSocket

class MainServer(private val port: Int) {

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
}

fun main(args: Array<String>) {
    var mainServer = MainServer(2195)
    mainServer.listen()
}
