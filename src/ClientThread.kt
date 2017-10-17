import java.io.*
import java.net.Socket

class ClientThread(var clientSocket: Socket, var mainServer: MainServer) : Runnable {

    private var inputStream: BufferedReader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
    private var outputStream: PrintWriter = PrintWriter(clientSocket.getOutputStream())

    override fun run() {
        println("Starting listener for client $this")
        listen()
    }

    private fun listen() {
        while (true) {
            var line = inputStream.readLine() ?: break
            println("$this: $line")
            when {
                line == "KILL_SERVICE" -> mainServer.shutdown()
                line.startsWith("HELO") -> sendHeloText(line)
                else -> handleChatMessages(line.split(":"))
            }
        }
    }

    private fun handleChatMessages(messages: List<String>) {
        if (messages.size > 1) {
            sendError(1, "Bad Message Sent To Server")
            return
        }

        when (messages[0]) {
            "JOIN_CHATROOM" -> joinChatRoom(messages)
            else -> sendError(2, "Unknown Message Sent To Server")
        }
    }

    private fun joinChatRoom(messages: List<String>) {
        if (messages.size < 8) {
            sendError(1, "Bad Message Sent To Server")
            return
        }

        var chatroom: String? = null
        var clientName: String? = null
        for ((index, value) in messages.withIndex()) {
            if (value.trim() == "JOIN_CHATROOM")
                chatroom = messages[index + 1].trim()
            if (value.trim() == "CLIENT_NAME")
                clientName = messages[index + 1].trim()
        }

        if (chatroom == null || clientName == null) {
            sendError(1, "Bad Message Sent To Server")
            return
        }

        mainServer.joinChatRoom(this, chatroom, clientName)
        sendJoinedChatRoom(chatroom, clientName)
    }

    private fun sendJoinedChatRoom(chatroom: String, clientName: String) {
        val message = "JOINED_CHATROOM: $chatroom\nSERVER_IP: ${clientSocket.localSocketAddress}\nPORT: ${clientSocket.localPort}\nROOM_REF: ${chatroom.hashCode()}\nJOIN_ID: ${clientName.hashCode()}\n"
        sendMessage(message)
    }

    private fun sendError(errorCode: Int, errorMessage: String) {
        val message = "ERROR_CODE: $errorCode\nERROR_MESSAGE: $errorMessage\n"
        sendMessage(message)
    }

    override fun toString(): String {
        return "${clientSocket.remoteSocketAddress}:${clientSocket.port}"
    }

    fun sendMessage(message: String) {
        outputStream.print(message)
        outputStream.flush()
    }

    private fun sendHeloText(line: String) {
        outputStream.print("$line\nIP:" + clientSocket.localSocketAddress.toString() + "\nPort:" + clientSocket.localPort + "\nStudentID:14310822\n")
        outputStream.flush()
    }
}
