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


exports.disputeNotify = functions.firestore.document('disputes/{disp}')
.onCreate((snap, context) => {
    const dispute = snap.data();
    
    const submitter = dispute.Submitter;
    const requestID = dispute.uniqueId;

    let token;

    return db.collection('requests').doc(requestID).get()
        .then(async docSnap => {
            let doc = docSnap.data();
            
            let borrower = doc.borrowerUID;
            let lender = doc.lenderUID;

            let personToNotify = (submitter === borrower)? borrower : lender;
            let otherPerson = (submitter === borrower)? doc.borrowerName: doc.lenderName;

            const payload = {
                notification: {
                    title: `Your transaction with ${otherPerson} is now under dispute.`,
                    body: `An email will be sent to you within 48 hours with additional information.`,
                },
        
                data: {
                    requestID: requestID,
                }
            };
            console.log(`Person to notify: ${personToNotify}`);
            let query = await db.collection("users").doc(personToNotify).get();
            token = query.data().notificationToken;
            return admin.messaging().sendToDevice(token,payload);
        })
        .then((response) => {
            console.log(`Dispute notification sent to: ${token}`);
            return 0;
        })
        .catch(error => console.log(error));
});

exports.progressTransaction = functions.firestore.document('requests/{req}')
    .onUpdate(async (change, context) => {
    // Retrieve the current and previous value
    const data = change.after.data();
    const previousData = change.before.data();

    //Setup variables
    const requestID = data.requestID;
     
    const borrowerName = data.borrowerName;
    const borrowerID = data.borrowerUID;

    const lenderName = data.lenderName;
    const lenderID = data.lenderUID;
      
    //Non status changes
    if (data.status === previousData.status){
        return null;
    }

    const payloadBorrower = {
        notification: {
            title: `${borrowerName}, your transaction has been updated.`,
            body: `Tap this notification to view details.`,
        },
    
        data: {
            requestID: requestID,
        }
    };

    const payloadLender = {
        notification: {
            title: `${lenderName}, your transaction has been updated.`,
            body: `Tap this notification to view details.`,
        },
    
        data: {
            requestID: requestID,
        }
    };

    let promises = [];

    if (lenderID.length > 0){
        let lenderQuery = await db.collection("users").doc(lenderID).get();
        const lenderToken = lenderQuery.data().notificationToken;
        promises.push(admin.messaging().sendToDevice(lenderToken,payloadLender));
        console.log(`Lender: ${lenderToken}`); 
    }
    
    let borrowerQuery = await db.collection("users").doc(borrowerID).get();   
    const borrowerToken = borrowerQuery.data().notificationToken;
    promises.push(admin.messaging().sendToDevice(borrowerToken,payloadBorrower));
    console.log(`Borrower: ${borrowerToken}`);

    return Promise.all(promises).then((resp)=> console.log("Successfully sent."));
});