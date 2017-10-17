class ChatRoom(val name: String) {
    var clients = HashMap<Int, ClientThread>()
}