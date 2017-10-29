class ChatRoom(val name: String) {
    var clients = HashMap<Int, ClientThread>()

    fun sendMessage(clientName: String? = "SERVER", message: String) {
        val mess = "CHAT: ${name.hashCode()}\nCLIENT_NAME: $clientName\nMESSAGE: $message\n\n"
        for (client in clients) {
            client.value.sendMessage(mess)
        }
    }

    fun disconnectClient(clientName: String) {
        if (clients.containsKey(clientName.hashCode())) {
            sendMessage(clientName, "$clientName has disconnected")
            clients.remove(clientName.hashCode())
        }
    }
}