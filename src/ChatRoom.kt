class ChatRoom(val name: String) {
    var clients = HashMap<Int, ClientThread>()

    fun sendMessage(message: String, clientName: String) {
        val message = "CHAT: ${name.hashCode()}\nCLIENT_NAME: $clientName\nMESSAGE: $message\n"
        for (client in clients) {
            client.value.sendMessage(message)
        }
    }
}