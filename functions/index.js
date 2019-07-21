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
        //Add tokenOwners
        .then(querySnapshot => {                    
            querySnapshot.forEach(doc => {
                const attribs = doc.data();

                if (attribs.uuid !== borrowerID && !tokenOwners.includes(attribs.uuid)){
                    if (recommendations || attribs.itemName.toLowerCase() === itemName.toLowerCase()){
                        tokenOwners.push(attribs.uuid);
                    }
                }
            });
            return 0;
        })
        //
        .then(response => {
            let tokens = [];
            tokenOwners.forEach(owner => {
                tokens.push(new Promise((resolve,reject) => {
                    db.collection("users").doc(owner).get()
                    .then(documentSnapshot => resolve(documentSnapshot.data().notificationToken))
                    .catch(err => reject(err));
                    }));  
                }
            );
            return Promise.all(tokens).then((data) => {return data;});
        })

        .then(response => {
            if (response.length > 0){
                return admin.messaging().sendToDevice(response,payload)
                .then((resp) => {
                    console.log(`Successfully sent to ${response}`);
                    return 0;
                })
                .catch((err) => {
                    console.log(err);
                });
            }

            else{
                return 0;
            }
        })
});

/*
exports.disputeNotify = functions.firestore.document('disputes/{disp}')
.onCreate((snap, context) => {
    const dispute = snap.data();
    
    const submitter = dispute.Submitter;
    const requestID = dispute.uniqueId;

    let token;

    const payload = {
        notification: {
            title: "You are a potential lender!",
            body: `${borrowerName} is looking for a ${itemName}`,
        },

        data: {
            requestID: requestID,
        }
    };

    return db.collection('requests').doc(requestID).get()
        .then(docSnap => {
            let doc = docSnap.data();
            
            let borrower = doc.borrowerUID;
            let lender = doc.lenderUID;

            let personToNotify = (submitter === borrower)? borrower : lender;
            
                
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


exports.progressTransaction = functions.firestore.document('requests/{req}')
    .onUpdate((change, context) => {
      // Retrieve the current and previous value
      const data = change.after.data();
      const previousData = change.before.data();

      //Setup variables
      const requestID = data.id; 
      const itemName = data.itemName;

      const borrowerID = data.borrowerUID;
      const lenderID = data.lenderID;
      
      //Handle disputes first
      if (data.dispute !== previousData.dispute){
          
        const payload = {
            notification: {
                title: `Your request for ${itemName} is now under dispute.`,
                body: `${borrowerName} is looking for a ${itemName}`,
            },

            data: {
                requestID: requestID,
            }
      }

      if (data.status === previousData.status && data.dispute === previousData.dispute){
          return null;
      } 

      // Retrieve the current count of name changes
      let count = data.name_change_count;
      if (!count) {
        count = 0;
      }
    }
*/