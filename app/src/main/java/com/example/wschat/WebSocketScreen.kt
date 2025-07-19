package com.example.wschat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun WebSocketScreen(viewModel: WebsocketViewModel = viewModel()) {

    // `collectAsState()` is a Compose extension function for StateFlow.
    // It collects the latest value emitted by the ViewModel's connectionState StateFlow
    // and represents it as a Compose `State` object.
    // The `by` keyword allows direct access to the `String` value without needing `.value`.
    // Any change in `connectionState` will trigger a recomposition of this Composable.
    val connectedState by viewModel.connectedState.collectAsState()

    // Similarly, collect the list of messages from the ViewModel's listOfMessages StateFlow.
    val listOfMessages by viewModel.listOFMessages.collectAsState()

    // `remember` stores a value across recompositions. `mutableStateOf` creates an observable state
    // for the TextField's input. When `messageInput` changes, Composables that read it will recompose.
    var messageInput by remember { mutableStateOf("") }

    // `rememberLazyListState()` creates and remembers the scroll state for our LazyColumn.
    // This allows us to programmatically control scrolling, e.g., to automatically scroll to the bottom.
    val listState = rememberLazyListState()

    // `LaunchedEffect` is a composable that runs a suspend function (like coroutines) when its key changes.
    // Here, the key is `listOfMessages.size`. Whenever the number of messages changes (i.e., a new message is added),
    // this effect will launch, and we'll scroll to the bottom of the list.
    LaunchedEffect(listOfMessages.size) {
        // Check if there are any messages to scroll to.
        if (listOfMessages.isNotEmpty()) {
            listState.animateScrollToItem(listOfMessages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff6a5bc2))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Display the current connection status.
        // `text` property takes the `connectionStatus` collected from the ViewModel.
        // `style` applies a predefined text style from MaterialTheme.
        // `modifier.padding(bottom = 8.dp)` adds space below this text.
        Text(
            text = "status: $connectedState",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xffe8e2ff),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {

            Button(
                onClick = { viewModel.connectWebSocket() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xfff2f285))
            ) { Text("Connect", fontWeight = FontWeight.Bold, color = Color.Black) }

            Spacer(modifier = Modifier.width(8.dp)) // Added back the Spacer width

            Button(
                onClick = { viewModel.disconnectedWebSocket() },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xfff2f285))
            ) { Text("Disconnected", fontWeight = FontWeight.Bold, color = Color.Black) }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFe8e8e8), RoundedCornerShape(8.dp))
                .padding(8.dp),
            state = listState

        ) {
            items(listOfMessages) { messages ->
                // Display each message as a Text composable.
                // Modifier.padding(vertical = 4.dp) adds vertical spacing between messages.
                // `style` applies a predefined text style from MaterialTheme.
//                Text(
//                    text = messages,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black,
//                    modifier = Modifier.padding(vertical = 4.dp), // Add vertical padding for readability.
//
//                )

                CustomTextComponent(text = messages)
            }
        }
        // Spacer for vertical space between the message area and the input field.
        Spacer(modifier = Modifier.height(16.dp)) // Add 16dp vertical space.

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // TextField for user input.
            // `value` is the current text in the field, bound to `messageInput` state.
            // `onValueChange` is a lambda that updates `messageInput` whenever the text changes.
            // `label` provides a hint text inside the TextField.
            // `modifier.weight(1f)` makes it take up most of the row's width.
            // `singleLine = true` ensures the input stays on one line.
            TextField(
                value = messageInput, // Current text value.
                onValueChange = { messageInput = it }, // Update state when text changes.
                shape = RoundedCornerShape(9.dp),
                placeholder = { Text("Enter Message") }, // Hint text.
                modifier = Modifier.weight(1f), // Take up most of the available width.
                singleLine = true, // Keep input on a single line.
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Black,
                    cursorColor = Color.Black,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    disabledLabelColor = Color.Black,
                    focusedPlaceholderColor = Color.Black,
                    unfocusedPlaceholderColor = Color.Black,
                )
            )
            // Spacer for horizontal space.
            Spacer(modifier = Modifier.width(8.dp)) // Add 8dp horizontal space.

            IconButton(onClick = {
                // Only send the message if the input field is not empty or just whitespace.
                if (messageInput.isNotBlank()) {
                    viewModel.sendMessage(message = messageInput)
                    messageInput = ""
                }
            }, enabled = connectedState == "Connected"
            ) {
               Icon(
                   imageVector = Icons.Default.Send,
                   contentDescription = "send",
                   modifier = Modifier.size(40.dp),
                   tint = Color.White
               )
            }

            // Send Button
//            Button(
//                onClick = {
//                    // Only send the message if the input field is not empty or just whitespace.
//                    if (messageInput.isNotBlank()) {
//                        viewModel.sendMessage(message = messageInput)
//                        messageInput = ""
//                    }
//                },
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xfff2f285)),
//                enabled = connectedState == "Connected" // Added back enabled state
//            ) {
//                Text(text = "SEND")
//            }
        }
    }
}


@Preview
@Composable
fun MyPreviewFive(){
    WebSocketScreen()
}