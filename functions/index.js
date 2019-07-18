const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();
const db = admin.firestore();

exports.notifyLenders = functions.firestore.document('requests/{req}').onCreate((snap, context) => {
        const requestID = snap.id;
        const stuff = snap.data();

        const itemName = stuff.itemName;
        const itemType = stuff.itemType;
        const recommendations = stuff.recommendations;
        const borrowerID = stuff.borrowerUID;
        const borrowerName = stuff.borrowerName;
        
        let tokens = [];
        let tokenOwners = [];
        let promises = [];

        const payload = {
            notification: {
                title: "You are a potential lender!",
                body: `${borrowerName} is looking for a ${itemName}`,
            },

            data: {
                requestID: requestID,
            }
        };

        return db.collectionGroup('lendList').where('itemType', '==', itemType).get()
        .then(querySnapshot => {                    
            querySnapshot.forEach(doc => {
                const attribs = doc.data();

                if (attribs.uuid !== borrowerID && !tokens.includes(attribs.token)){
                    if (recommendations || attribs.itemName.toLowerCase() === itemName.toLowerCase()){
                        tokens.push(attribs.token);
                        tokenOwners.push(attribs.uuid);
                    }
                }
            });
                
            if (tokens.length > 0){
                promises.push(admin.messaging().sendToDevice(tokens,payload));
            }

            return Promise.resolve(promises);
        })
        .then((response) => {
            console.log(`Successfully sent to ${tokenOwners}`);
            return 0;
        })
        .catch(error => console.log(error));
});