/* Common functions */

var jFormErrorHandler = function(response) {
    unblockUI();
    jAlert(response.status + ": " + response.statusText);
}

var angularErrorHandler = function(response) {
    // TODO : better error handling
    unblockUI();
    motechAlert("error");
}

var alertHandler = function(msg) {
    return function() {
        unblockUI();
        motechAlert(msg);
    }
}

var alertHandlerWithCallback = function(msg, callback) {
    return function() {
        unblockUI();
        motechAlert(msg, "", callback);
    }
}

var dummyHandler = function() {}

function motechAlert(msg) {
    jAlert(jQuery.i18n.prop(msg), "");
}

function motechAlert(msg, title) {
    jAlert(jQuery.i18n.prop(msg), jQuery.i18n.prop(title));
}

function motechAlert(msg, title, callback) {
    jAlert(jQuery.i18n.prop(msg), jQuery.i18n.prop(title), callback);
}

/* Define "finished typing" as 5 second puase */
var typingTimer;
var doneTypingInterval = 5 * 1000;

function captureTyping(callback) {
    clearTimeout(typingTimer);
    typingTimer = setTimeout(callback, doneTypingInterval);
}

function blockUI() {
    $.blockUI({message : '<h1><img src="img/bigloader.gif" alt="loading" /></h1>'});
}

function unblockUI() {
    $.unblockUI();
}

