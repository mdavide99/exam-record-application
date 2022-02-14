/**
 * Manages the login phase of both students and teachers
 */

(function() { 

  document.getElementById("loginbutton").addEventListener('click', (e) => {
    var form = e.target.closest("form");
    if (form.checkValidity()) {
      makeCall("POST", 'LoginAction', e.target.closest("form"),
        function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {
            var message = x.responseText;
            switch (x.status) {
              case 200:
				var id = message.split(":");
                if (message.includes("teacher:")) {
                  sessionStorage.setItem('teacher',id);
                  window.location.href = "Teacher.html";
                }
                else if (message.includes("student:")) {
                  sessionStorage.setItem('student',id);
                  window.location.href = "Student.html";
                }
                break;
              case 400: // bad request
                document.getElementById("errormessage").textContent = message;
                break;
              case 401: // unauthorized
                  document.getElementById("errormessage").textContent = message;
                  break;
              case 500: // server error
            	document.getElementById("errormessage").textContent = message;
                break;
            }
          }
        }
      );
    } else {
    	 form.reportValidity();
    }
  });

})();