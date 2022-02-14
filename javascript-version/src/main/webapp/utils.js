/**
 * AJAX call management
 */
function makeCall(method, url, formElement, cback, reset = true) {
  var req = new XMLHttpRequest();
  req.onreadystatechange = function () {
    cback(req);
  };
  req.open(method, url);
  if (formElement == null) {
    req.send();
  } else {
    req.send(new FormData(formElement));
  }
  if (formElement !== null && reset === true) {
    formElement.reset();
  }
}

/**
 * AJAX do POST with custom Json Body
 */
function sendJSON(url, body, cback) {
  var req = new XMLHttpRequest();
  req.onreadystatechange = function () {
    cback(req);
  };
  req.open("POST", url);
  req.setRequestHeader("Content-Type", "application/json");
  req.send(JSON.stringify(body));
}
