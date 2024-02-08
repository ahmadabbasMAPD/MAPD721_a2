

package com.abbas.mycontacts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.provider.ContactsContract
import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.items

import androidx.core.content.ContextCompat

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment


import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context

import androidx.compose.material.ButtonDefaults
import androidx.compose.ui.text.style.TextAlign

import com.abbas.mycontacts.Contacts





import com.abbas.mycontacts.ui.theme.MycontactsTheme

data class Contact(val name: String, val number: String)

val randomContacts = List(10) {
    Contact(
        "Name ${it + 1}",
        "1234567890".substring(0 until it % 9 + 1) + (it * 1000000).toString()
    )
}





class MainActivity : ComponentActivity() {

        private val requestContactsPermissionLauncher =
               registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                        handlePermissionsResult(permissions)
                    }

        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContent {
                        MycontactsTheme {
                                checkAndRequestContactsPermission()
                                MainScreen()
            }
                    }
          }

        private fun checkAndRequestContactsPermission() {
                val permissionsToRequest = mutableMapOf<String, Boolean>()

                if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.READ_CONTACTS
                                    ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionsToRequest[Manifest.permission.READ_CONTACTS] = true
                    }

                if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.WRITE_CONTACTS
                                    ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionsToRequest[Manifest.permission.WRITE_CONTACTS] = true
                    }

                if (permissionsToRequest.isNotEmpty()) {
                        requestContactsPermissionLauncher.launch(permissionsToRequest.keys.toTypedArray())
                    }
          }

        private fun handlePermissionsResult(permissions: Map<String, Boolean>) {
                if (permissions[Manifest.permission.READ_CONTACTS] == true &&
                        permissions[Manifest.permission.WRITE_CONTACTS] == true
                    ) {
                        // Permissions are granted, you can proceed with fetching or adding contacts
                    } else {
                        // Permission is denied, handle accordingly (e.g., show a message or disable features)
                    }
        }
    }



@Composable
fun MainScreen() {
    val context = LocalContext.current
    val contacts = remember { mutableStateListOf<Contact>() }
    val name = remember { mutableStateOf("") }
    val number = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextField(value = name.value, onValueChange = { name.value = it }, label = { Text("Contact Name") })
            Spacer(modifier = Modifier.height(8.dp))
           // TextField(value = number.value, onValueChange = { number.value = it }, label = { Text("Contact No") })
            TextField(
                value = number.value,
                onValueChange = {
                    if (it.all { char -> char.isDigit() } && it.length <= 10) {
                        number.value = it
                    }
                },
                label = { Text("Contact No") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = { contacts.addAll(fetchContacts(context)) }, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Yellow)) {
                    Text("Load", color = Color.Black)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (name.value.isNotBlank() && number.value.isNotBlank()) {
                        addContact(context, name.value, number.value)
                        contacts.add(Contact(name.value, number.value))
                        errorMessage.value = ""
                    } else {
                        errorMessage.value = "Please fill in both fields before saving."
                    }
                }, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Yellow)) {
                    Text("Save", color = Color.Black)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (errorMessage.value.isNotBlank()) {
                Text(text = errorMessage.value, color = Color.Red)
            }
            Text("Contacts:", style = TextStyle( fontSize = 20.sp, fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Left )

            contacts.forEach { contact ->
                Text(text = "    ${contact.name}, ${contact.number}")
            }
        }
    }
}



fun addContact(context: Context, name: String, number: String) {
    val contentValues = ContentValues().apply {
        put(ContactsContract.RawContacts.ACCOUNT_TYPE, null as String?)
        put(ContactsContract.RawContacts.ACCOUNT_NAME, null as String?)
    }

    val rawContactUri = context.contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentValues)
    val rawContactId = ContentUris.parseId(rawContactUri!!)

    // Insert name
    contentValues.clear()
    contentValues.apply {
        put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
        put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
        put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
    }
    context.contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues)

    // Insert phone number
    contentValues.clear()
    contentValues.apply {
        put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
        put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
        put(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
        put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
    }
    context.contentResolver.insert(ContactsContract.Data.CONTENT_URI, contentValues)
}



//@Composable
//fun MainScreen() {
//    // ... existing code ...
//
//    Column(modifier = Modifier.padding(16.dp)) {
//        // ... existing UI code ...
//
//        TextField(
//            value = number.value,
//            onValueChange = {
//                if (it.all { char -> char.isDigit() } && it.length <= 10) {
//                    number.value = it
//                }
//            },
//            label = { Text("Contact No") },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//        )
//
//        // ... existing UI code ...
//    }
//}
//

fun fetchContacts(context: Context): List<Contact> {
    val contacts = mutableListOf<Contact>()

    val cursor = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        null
    )

    cursor?.let {
        while (it.moveToNext()) {
            val contactName = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val contactNumber = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            contacts.add(Contact(contactName, contactNumber))
        }
        it.close()
    }

    return contacts
}








@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainActivity()
}
