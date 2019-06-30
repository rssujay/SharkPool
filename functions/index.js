// The Cloud Functions for Firebase SDK to create Cloud Functions and setup triggers.
const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();
const db = admin.firestore();

exports.notifyLenders = functions.firestore.document('requests/{req}').onCreate(async (snap, context) => {
        const stuff = snap.data();
        const itemType = stuff.itemType;
        const recommendations = stuff.recommendations;

        if (recommendations === true){
            db.collectionGroup('lendList').where('itemType', '==', itemType).get()
            .then(querySnapshot => {                    
                querySnapshot.forEach(doc => {
                    console.log(doc.id, ' => ', doc.data());
                });
                return console.log("Successfully sent");
            }).catch(error => {
                   return console.log(error);
                });
            }

        return console.log("No recommendations required");
});