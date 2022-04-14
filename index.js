var aws = require("aws-sdk");
const crypto = require("crypto");
aws.config.update({region:'us-east-1'})
var ses = new aws.SES({ region: "us-east-1" });
var docClient = new aws.DynamoDB.DocumentClient();
exports.handler = async function (event) {
  console.log(event.Records[0].Sns.Message);
  console.log("event" + event);
   // let message = event;
    //let json = JSON.parse(message);
    let email = event.Records[0].Sns.Message;
    console.log("email " + email);
    console.log("SNS index file updated");
    let token = crypto.randomBytes(16).toString("hex");
    const seconds = 5 * 60;
    const secondsInEpoch = Math.round(Date.now() / 1000);
    const expirationTime = secondsInEpoch + seconds;
    const currentTime = Math.round(Date.now() / 1000);
    console.log("next line " );
    //Creating a table for DynamoDB
var table = {
    TableName : "csye6225",
    Item:{
      "username" : email,
      "token" : token,
      "TimeToExist" :expirationTime
    }
  }
  console.log("Adding a new item...");
  console.log("email sent "+email);
  console.log("token sent "+token);
  //Putting an item to DynamoDB Table
docClient.put(table, function(err, data) {
    if (err) {
        console.error("Unable to add item. Error JSON:", JSON.stringify(err, null, 2));
    } else {
        console.log("Added item:", JSON.stringify(data, null, 2));
    }
});

console.log(email + " " +token + "Parameters set!!");
var params = {
  
  Destination: {
    ToAddresses: [email],
  },
  Message: {
    Body: {
      Html: {
        Charset: "UTF-8",
        Data:
        '<html><head>' +
        '<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />' +
        '<title>' + "test" + '</title>' +  
        '</head><body>' +
        'This is the link to update your password. It is valid for five minutes.' +
        '<br><br>' +
        "<a href=\"http://" + "demo.khismatrao.me" + "/v1/user/verifyUser/" + token + "/" + email + "\">" +
        "http://" + "demo.khismatrao.me" + "/v1/user/verifyUser/" + token + "/" + email + "</a>"
        +'</body></html>',
      },
    }, 
    Subject: {
        Charset: "UTF-8",
        Data: "CSYE 6225: Verify Email Address",
      },
    },
    Source: "sender@demo.khismatrao.me",
  };
  console.log("Email Send!!");
  
  return ses.sendEmail(params).promise()
  
};           