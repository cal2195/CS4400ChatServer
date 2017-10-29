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
            var lines = readChunk(inputStream) ?: continue
            when {
                lines[0] == "KILL_SERVICE" -> mainServer.shutdown()
                lines[0].startsWith("HELO") -> sendHeloText(lines[0])
                else -> handleChatMessages(lines)
            }
        }
    }

    private fun readChunk(inputStream: BufferedReader): List<String>? {
        var expecting = 1
        var lines = ArrayList<String>()
        while (expecting-- > 0) {
            var line = inputStream.readLine()
            lines.add(line)
            println("[$this -> server] $line")
            if (line.startsWith("JOIN_CHATROOM")) expecting = 3
            else if (line.startsWith("LEAVE_CHATROOM")) expecting = 2
        }
        return lines
    }

    private fun handleChatMessages(messages: List<String>) {
        if (messages.isEmpty()) {
            sendError(1, "Bad Message Sent To Server")
            return
        }

        when {
            messages[0].startsWith("JOIN_CHATROOM") -> joinChatRoom(messages)
            messages[0].startsWith("LEAVE_CHATROOM") -> leaveChatRoom(messages)
            messages[0].startsWith("CHAT") -> sendChatMessage(messages)
            else -> sendError(2, "Unknown Message Sent To Server")
        }
    }

    private fun sendChatMessage(messages: List<String>) {
        var hash = messageListToHashmap(messages)

        val roomRef = hash["CHAT"]
        val joinId = hash["JOIN_ID"]
        val clientName = hash["CLIENT_NAME"]
        val message = hash["MESSAGE"]

        if (roomRef == null || joinId == null || clientName == null || message == null) {
            sendError(1, "Bad Message Sent To Server")
            return
        }

        
    }

    private fun leaveChatRoom(messages: List<String>) {
        var hash = listToHashmap(messages)

        if (hash["LEAVE_CHATROOM"] == null || hash["JOIN_ID"] == null || hash["CLIENT_NAME"] == null) {
            sendError(1, "Bad Message Sent To Server")
            return
        }

        var chatroom = mainServer.chatrooms[hash["LEAVE_CHATROOM"]!!.toInt()]
        chatroom?.clients?.remove(hash["JOIN_ID"]!!.toInt())

        sendLeftChatRoom(hash["LEAVE_CHATROOM"]!!, hash["JOIN_ID"]!!, hash["CLIENT_NAME"]!!)
    }

    private fun joinChatRoom(messages: List<String>) {
        var hash = listToHashmap(messages)

        if (hash["JOIN_CHATROOM"] == null || hash["CLIENT_NAME"] == null) {
            sendError(1, "Bad Message Sent To Server")
            return
        }

        var chatroom: String? = hash["JOIN_CHATROOM"]
        var clientName: String? = hash["CLIENT_NAME"]

        if (chatroom == null || clientName == null) {
            sendError(1, "Bad Message Sent To Server")
            return
        }

        mainServer.joinChatRoom(this, chatroom, clientName)
        sendJoinedChatRoom(chatroom, clientName)
    }

    private fun listToHashmap(lines: List<String>): HashMap<String, String> {
        var hash = HashMap<String, String>()
        lines.map { it.split(":") }
                .forEach { hash.put(it[0].trim(), it[1].trim()) }
        return hash
    }

    private fun messageListToHashmap(lines: List<String>): HashMap<String, String> {
        var hash = HashMap<String, String>()

        var message: String? = null

        for (line in lines) {
            if (message == null) {
                val parts = line.split(":")
                if (parts[0] != "MESSAGE") {
                    hash.put(parts[0], parts[1])
                } else {
                    message = parts[1]
                }
            } else {
                message += line
            }
        }

        hash.put("MESSAGE", message!!)

        return hash
    }

    private fun sendJoinedChatRoom(chatroom: String, clientName: String) {
        val message = "JOINED_CHATROOM: $chatroom\nSERVER_IP: ${mainServer.ip}\nPORT: ${clientSocket.localPort}\nROOM_REF: ${chatroom.hashCode()}\nJOIN_ID: ${clientName.hashCode()}\n"
        sendMessage(message)
        mainServer.chatrooms[chatroom.hashCode()]?.sendMessage(clientName, "$clientName has joined the chat room")
    }

    private fun sendLeftChatRoom(chatroomRef: String, joinId: String, clientName: String) {
        val message = "LEFT_CHATROOM: $chatroomRef\nJOIN_ID: $joinId\n"
        sendMessage(message)
        mainServer.chatrooms[chatroomRef.toInt()]?.sendMessage(clientName, "$clientName has left the chat room")
    }

    private fun sendError(errorCode: Int, errorMessage: String) {
        val message = "ERROR_CODE: $errorCode\nERROR_MESSAGE: $errorMessage\n"
        sendMessage(message)
    }

    override fun toString(): String {
        return "${clientSocket.remoteSocketAddress}:${clientSocket.port}"
    }

    fun sendMessage(message: String) {
        println("[server -> $this] $message")
        outputStream.print(message)
        outputStream.flush()
    }

    private fun sendHeloText(line: String) {
        outputStream.print("$line\nIP:" + mainServer.ip + "\nPort:" + clientSocket.localPort + "\nStudentID:14310822\n")
        outputStream.flush()
    }
}
