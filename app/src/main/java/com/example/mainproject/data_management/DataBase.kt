package com.example.mainproject.data_management

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.example.mainproject.search.ListItemModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class DataBase {
    companion object {
        val TAG = "AILIESEI FireStore - DataBase"
    }

    internal var db= FirebaseFirestore.getInstance()
    private val participantsCollectionName = "Participants"
    private val idManagementCollectionName = "IdManagement"

    // Following functions are used for participants management
    fun createParticipant(participantInfo: ParticipantInfo
    ): HashMap<String, Any> {
        return hashMapOf(
            "firstName" to participantInfo.firstName,
            "lastName" to participantInfo.lastName,
            "birthDay" to participantInfo.birthDay,
            "birthMonth" to participantInfo.birthMonth,
            "birthYear" to participantInfo.birthYear,
            "participantPhone" to participantInfo.participantPhone,
            "parentPhone" to participantInfo.parentPhone,
            "isCommuter" to participantInfo.isCommuter,
            "idLetter" to participantInfo.idLetter,
            "idValue" to participantInfo.idValue,
            "search" to participantInfo.nameSearch
        )
    }

    fun addParticipant(participantInfoMap: HashMap<String, Any>) {
        db.collection(participantsCollectionName)
            .add(participantInfoMap)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    fun deleteParticipant(documentReference: DocumentReference) {
        documentReference.delete()
            .addOnSuccessListener {
                // Document was successfully deleted.
                Log.d(TAG, "DocumentSnapshot successfully deleted!")
            }
            .addOnFailureListener { error ->
                // An error occurred while deleting the document.
                Log.w(TAG, "Error deleting document", error)
            }
    }

    private fun formatId(idLetter: String, idValue: Int): String {
        val idValueString = String.format("%03d", idValue)
        return "#$idLetter$idValueString"
    }

    // Gets a list of all participants
    fun getAllParticipants(callback: (ArrayList<ListItemModel>) -> Unit) {
        val modelList : ArrayList<ListItemModel> = arrayListOf()
        db.collection(participantsCollectionName)
            .get()
            .addOnCompleteListener { task ->
                for (document: DocumentSnapshot in task.result.documents) {
                    val fullName =
                        document.getString("lastName") + " " + document.getString("firstName")
                    val id = formatId(
                        document.getString("idLetter")!!, document.getLong("idValue")!!
                            .toInt()
                    )
                    modelList.add(ListItemModel(id, fullName))
                }
                callback(modelList)
            }
    }

    fun searchParticipants(s: String, callback: (ArrayList<ListItemModel>) -> Unit) {
        val modelList : ArrayList<ListItemModel> = arrayListOf()
        db.collection(participantsCollectionName).whereEqualTo("search", s)
            .get()
            .addOnCompleteListener { task ->
                for (document: DocumentSnapshot in task.result.documents) {
                    val fullName =
                        document.getString("lastName") + " " + document.getString("firstName")
                    val id = formatId(
                        document.getString("idLetter")!!, document.getLong("idValue")!!
                            .toInt()
                    )
                    modelList.add(ListItemModel(id, fullName))
                }
                callback(modelList)
            }
    }

    // Following functions are used for participants' Ids management

    // TODO: Make this following code better
    // TODO: idea: make text fields as string resources? or smth cus its  a mess

    private val firstId = Id("a", 1)
    private var currentId : Id? = null
    private var currentIdPath : String = "availableId"
    private var currentIdReference : DocumentReference? = null

    private fun createId(id : Id
    ): HashMap<String, Any> {
        return hashMapOf(
            "letter" to id.letter,
            "value" to id.value,
        )
    }

    private fun initId (idMap: HashMap<String, Any>) {
        currentIdReference = db.collection(idManagementCollectionName).document(currentIdPath)
        currentIdReference!!.set(idMap)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: $documentReference.id")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    private fun initWithFirstId (): Id {
        val idMap = createId(firstId)
        initId(idMap)
        currentId = firstId
        return firstId
    }

    private fun updateValue (newValue : Int) {
        val newValueMap = hashMapOf<String, Any>("value" to newValue)
        if (currentIdReference != null) {
            currentIdReference!!.update(newValueMap)
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot with ID: ${currentIdReference!!.id} was updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating document", e)
                }
        }
    }

    private fun updateId (idMap: HashMap<String, Any>) {
        if (currentIdReference != null) {
            currentIdReference!!.update(idMap)
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot with ID: ${currentIdReference!!.id} was updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating document", e)
                }
        }
    }

    fun getNextAvailableId(callback: (Id) -> Unit) {
        getCurrentIdReference {
            if (it) callback(currentId!!.getNextId())
            else callback(initWithFirstId())
        }
    }

    fun updateNextId () {
        var nextId: Id
        getNextAvailableId {
            nextId = it
            if (nextId != firstId)
                if (nextId.value <= 999)
                    updateValue(nextId.value)
                else updateId(createId(nextId))
            currentId = nextId
        }
    }

    private fun getCurrentId (callback: (Id?) -> Unit) {
        var id : Id? = null
        if (currentIdReference != null) {
            currentIdReference!!.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val letter = documentSnapshot.get("letter") as String
                        val value = (documentSnapshot.get("value") as Long).toInt()
                        Log.d(TAG, "get current id: $letter $value")
                        id = Id(letter, value)
                        currentId = id
                        callback(id!!)
                    }
                }
                .addOnFailureListener { callback(null) }
        }
    }

    private fun getCurrentIdReference (callback: (Boolean) -> Unit){
        db.collection(idManagementCollectionName)
            .document(currentIdPath)
            .get()
            .addOnSuccessListener {
                currentIdReference = db.collection(idManagementCollectionName).document(currentIdPath)
                getCurrentId{
                    currentId = it
                    callback(true)
                    Log.d(TAG, "getCurrentIdreference succeded")
                }
            }
            .addOnFailureListener {
                currentId = null
                currentIdReference = null
                Log.d(TAG, "getcurrentidreference failed")
                callback(false)
            }
    }
}