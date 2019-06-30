const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();
const db = admin.firestore();

exports.notifyLenders = functions.firestore.document('requests/{req}').onCreate(async (snap, context) => {
        const stuff = snap.data();
        const itemName = stuff.itemName;
        const itemType = stuff.itemType;
        const recommendations = stuff.recommendations;
        const borrowerID = stuff.borrowerUID;
        let tokens = [];

        if (recommendations === true){
            db.collectionGroup('lendList').where('itemType', '==', itemType).get()
            .then(querySnapshot => {                    
                querySnapshot.forEach(doc => {
                    const attribs = doc.data();

                    if (attribs.uuid !== borrowerID && !tokens.includes(attribs.token)){
                        return tokens.push(attribs.token);
                    }
                    return console.log(attribs.token);
                });
                
                const payload = {
                        notification: {
                            title: 'You are a potential lender!',
                            body: 'Someone is looking for a '.concat(itemName)
                    }
                };

                if (tokens.length > 0){
                    console.log("Successfully sent");
                    return admin.messaging().sendToDevice(tokens,payload);
                }
                return console.log(tokens);

            }).catch(error => {
                   return console.log(error);
                });
            }
});