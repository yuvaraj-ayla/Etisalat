package com.aylanetworks.aylasdk.ams.action.params;

import com.google.gson.annotations.Expose;

/**
 *      Contains key / value pairs with information relevant to the action to be performed.
 *      For Datapoint action required parameter key is <code>‘datapoint’</code>.
 *      For e.g. <pre>
 *      "parameters": {
 *      "datapoint" :
 *      "DATAPOINT(dsn1,prop1) = 70"
 *      } </pre>
 *
 *      For Email action required parameter keys are
 *      email_to, email_subject &
 *      Email_body.
 *
 *      For e.g. <pre>
 *      "parameters": {
 *      "email_body": "Hi there!!",
 *      "email_to": [   "abc@xyz.com"
 *      ],
 *      "email_subject": "Device Updated Notification"
 *      } </pre>
 */

public class AylaActionParameters {
}
