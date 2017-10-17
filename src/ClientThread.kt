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
            when (line) {
                "KILL_SERVICE" -> mainServer.shutdown()
                "HELO text" -> sendHeloText()
            }
        }
    }

    override fun toString() : String {
        return "${clientSocket.remoteSocketAddress}:${clientSocket.port}"
    }

    private fun sendHeloText() {
        outputStream.print("HELO text\nIP:" + clientSocket.localSocketAddress.toString() + "\nPort:" + clientSocket.localPort + "\nStudentID:14310822\n")
        outputStream.flush()
    }
}