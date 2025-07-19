package com.example.wschat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit


// WebSocketViewModel is a ViewModel class. ViewModels are designed to hold and manage UI-related data
// in a lifecycle-conscious way. This means the data inside the ViewModel survives configuration changes
// (like screen rotations), preventing data loss and unnecessary re-fetching.
// It also acts as an intermediary between the UI (Composables) and the WebSocket logic.
class WebsocketViewModel : ViewModel() {

    // Define the URL for our WebSocket server.
    // For this simple example, we're using a public "echo" WebSocket server.
    // An echo server simply sends back any message it receives.
    // 'wss://' indicates a secure WebSocket connection (recommended for production).
    // For your own backend, this would be something like "wss://your-backend.com/chat".
    private val webSocketUrl = "wss://echo.websocket.events"

    // Initialize the OkHttpClient. This client will be used to create and manage the WebSocket connection.
    // We set readTimeout and writeTimeout to 0 to disable them for WebSockets.
    // WebSockets are long-lived connections, so we don't want them to time out due to inactivity.
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .writeTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    // _connectionState is a MutableStateFlow. (Renamed from _connectionStatus as per your provided code)
    // MutableStateFlow is a mutable (changeable) state holder that emits updates to its collectors.
    // It's ideal for UI state that needs to be observed and updated, like the connection status.
    // It's initialized with "Disconnected".
    private val _connectionState = MutableStateFlow("Disconnected")
    // connectionState is an immutable (read-only) StateFlow.
    // We expose this read-only version to the UI to prevent external classes from directly modifying
    // the internal state, promoting better encapsulation.
    val connectedState: StateFlow<String> = _connectionState

    // _listOfMessages is a MutableStateFlow that holds a list of strings, representing our chat messages. (Renamed from _messages)
    // This list will be displayed in the UI. It's initialized as an empty list.
    private val _listOfMessages = MutableStateFlow<List<String>>(emptyList())
    // listOfMessages is the immutable StateFlow exposed to the UI for displaying the chat history.
    val listOFMessages: StateFlow<List<String>> = _listOfMessages

    // webSocket is a nullable WebSocket instance.
    // It will hold the active WebSocket connection object once it's established.
    // It's nullable because initially, there is no active connection.
    private var webSocket: WebSocket? = null

    // SimpleWebSocketListener is an inner class that extends OkHttp's WebSocketListener.
    // This class defines how our application reacts to various events that occur on the WebSocket connection.
    private inner class SimpleWebSocketListener() : WebSocketListener() {

        // onOpen is called when the WebSocket connection has been successfully established.
        // This is the moment your "open line" is ready for bi-directional communication.
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            // Launch a coroutine within the ViewModel's scope to update the UI state.
            // Using viewModelScope.launch ensures that this operation is tied to the ViewModel's lifecycle
            // and happens on a background thread, preventing UI freezes.
            viewModelScope.launch {
                _connectionState.value = "Connected" // Update the connection status to "Connected".
                 addMessage("--- Connected to WebSocket ---")// Add a system message to the chat history.
            }
        }
        // onMessage is called when a text message is received from the WebSocket server.
        // This is how your app gets real-time updates or chat messages from others.
        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            viewModelScope.launch {
                 addMessage("Received: $text")// Prefix the message with "Received:" for clarity in the UI.
            }
        }

        // onFailure is called when an error occurs with the WebSocket connection.
        // This could be due to network issues, server errors, or unexpected disconnections.
        // It's crucial for handling robustness and potential reconnection logic in real apps.
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)// Call the superclass method.
            // Launch a coroutine to update the status and add an error message to the chat history.
            viewModelScope.launch {
                _connectionState.value = "Failed: ${t.message}" // Display the error message from the Throwable.
                  addMessage("--- Connection Failed: ${t.message} ---")// Add to chat history for debugging/user info.
            }
            this@WebsocketViewModel.webSocket?.close(1000, "Failure")
            this@WebsocketViewModel.webSocket =
                null// Clear the reference as the connection is no longer valid.
        }

        // onClosing is called when either the client or the server initiates a graceful closure of the WebSocket.
        // This happens before the connection is fully closed.
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            viewModelScope.launch {
                _connectionState.value =
                    "Closing ($code): $reason" // Inform about the closing process.
                 addMessage("--- Closing WebSocket ($code): $reason ---") // Add to chat history.
            }
        }
        // onClosed is called when the WebSocket connection has been completely closed.
        // This is the final state after a graceful closure or after a failure has been handled.
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            // Launch a coroutine to update the status to "Disconnected" and clear the WebSocket reference.
            viewModelScope.launch {
                _connectionState.value = "Disconnected ($code): $reason"// Final status after closure.
                 addMessage("---WebSocket Closed ($code): $reason ---")// Add to chat history.
            }
            this@WebsocketViewModel.webSocket =
                null// Clear the reference as the connection is no longer valid.
        }
    }

    // Function To Establish the webSocket Connection
    fun connectWebSocket() {
        // Only attempt to connect if there isn't an existing active connection.
        if (webSocket == null) {
            _connectionState.value = "Connecting..."
            // Build a new Request object targeting our WebSocket URL.

            val request = Request.Builder().url(webSocketUrl).build()
            // Create a new WebSocket connection using the OkHttpClient and our custom listener.
            // This initiates the HTTP handshake to upgrade to a WebSocket connection.
            webSocket = client.newWebSocket(request = request, SimpleWebSocketListener())
        } else {
            // If already connected, add a message to inform the user.
            viewModelScope.launch {
                // addMessage("Already connected.")
            }
        }
    }

    // Function SendMessage
    fun sendMessage(message: String) {
        if (webSocket != null) {
            // Send the message. The `send()` method returns true if the message was successfully
            // queued for sending, false otherwise (e.g., if the connection is not ready).
            val sent = webSocket?.send(message) ?: false
            if (sent) {
                // If the message was successfully queued, add it to our display list.
                viewModelScope.launch {
                      addMessage("Sent: $message") // Prefix with "Sent:" for clarity.
                }
            } else {
                // If sending failed (e.g., connection is in a bad state), inform the user.
                viewModelScope.launch {
                       addMessage("Failed to send message. Not connected or connection issue.")
                }
            }
        } else {
            // If no WebSocket connection is active, inform the user.
            viewModelScope.launch {
                    addMessage("Not connected to WebSocket.")
            }
        }
    }

    // Function to disconnected to webSocket
    fun disconnectedWebSocket(){
        // Check if a WebSocket connection exists.
        if(webSocket!=null){
            // Close the connection with a normal closure code (1000) and a reason.
            // Code 1000 signifies "Normal Closure" as per WebSocket protocol.
            webSocket?.close(1000,"User Disconnected")
            webSocket = null// Immediately clear the reference to indicate no active connection.
        }else{
            // If not connected, inform the user.
            viewModelScope.launch {
                addMessage("Not connected to WebSocket.")
            }
        }
    }

    // Private helper function to add a message string to the list of messages displayed in the UI.
    // It updates the _listOfMessages MutableStateFlow, which in turn triggers a recomposition in Compose,
    // causing the UI to update with the new message.
    private fun addMessage(message:String){
        // Update the value of _listOfMessages by creating a new list with all existing messages
        // plus the new `message`. This is important because StateFlow (and Compose)
        // detects changes by reference equality. Creating a new list ensures a change is detected.
        _listOfMessages.value = _listOfMessages.value .plus(message)
    }

    override fun onCleared() {
        super.onCleared()// Call the superclass method.
        disconnectedWebSocket()// Ensure the WebSocket connection is closed to release network resources.
        // Shut down OkHttp's internal thread pool. This is good practice to release resources
        // associated with the HTTP client when it's no longer needed.
        client.dispatcher.executorService.shutdown()
    }
}