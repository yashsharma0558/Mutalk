package com.example.mutalk

import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    var currentUserName: String = ""
    var currentUserPhoneNo: String = ""
    private val currentUserContactList = mutableListOf<Contact>()

    // Method to add a contact to the list
    fun addContact(contact: Contact) {
        currentUserContactList.add(contact)
    }

    // Method to retrieve all contacts
    fun getAllContacts(): MutableList<Contact> {
        return currentUserContactList
    }
}
