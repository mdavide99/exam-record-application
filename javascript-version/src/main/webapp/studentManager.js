{
  let corsiElenco,
    pageTitle,
    resultPage,
    pageOrchestrator = new PageOrchestrator(); // main controller

  window.addEventListener(
    "load",
    () => {
      if (sessionStorage.getItem("student") == null) {
        window.location.href = "index.html";
      } else {
        pageOrchestrator.start(); // initialize the components
        pageOrchestrator.refresh();
        corsiElenco.show();
      } // display initial content
    },
    false
  );
  /**
   * Manages the page title
   * @param  {} _content - The title of the page
   */
  function PageTitle(_content) {
    this.content = _content;
    this.update = function (text) {
      this.content.innerHTML = text;
    };
    this.reset = function () {
      this.content.innerHTML = "";
    };
  }
  /**
   * Manages the list of courses in which a student is enrolled
   * @param  {} _alert - box for allert message
   * @param  {} _content - box that contains all the contents
   */
  function CorsiElenco(_alert, _content) {
    this.alert = _alert;
    this.content = _content;

    this.show = function () {
      var self = this;
      makeCall("GET", "GetStudentCourses", null, function (request) {
        if (request.readyState == 4) {
          var message = request.responseText;
          if (request.status == 200) {
            self.update(JSON.parse(request.responseText));
          } else {
            self.alert.textContent = message;
          }
        }
      });
    };

    this.update = function (corsi) {
      var courseName,
        sessionDate,
        anchor,
        linkText,
        length = corsi.length;

      if (length === 0) {
        this.alert.textContent =
          "Nessun Corso, i tuoi dati non sono ancora stati inserti, contatta la segreteria";
      } else {
        pageTitle.update("Home");
        this.content.innerHTML = "";
        var self = this;

        for (let i = 0; i < length; i++) {
          courseName = document.createElement("h3");
          courseName.textContent = corsi[i].courseName;
          self.content.appendChild(courseName);

          const myList = document.createElement("ul");
          const examSessionList = corsi[i].examSessionList;

          if (examSessionList.length == 0) {
            warningBox = document.createElement("div");
            warningBox.classList.add("alert", "alert-warning", "no-print");
            warningBox.innerHTML = "Nessun appello registrato per questo corso";
            self.content.appendChild(warningBox);
          } else {
            for (let j = 0; j < examSessionList.length; j++) {
              sessionDate = document.createElement("li");
              anchor = document.createElement("a");
              sessionDate.appendChild(anchor);
              linkText = document.createTextNode(
                examSessionList[j].sessionDate
              );
              anchor.appendChild(linkText);
              anchor.setAttribute("sessionId", examSessionList[j].sessionId); // set a sessionId HTML attribute
              anchor.addEventListener(
                "click",
                (e) => {
                  pageOrchestrator.refresh();
                  resultPage.show(e.target.getAttribute("sessionId"));
                },
                false
              );
              anchor.href = "#";
              myList.appendChild(sessionDate);
            }

            self.content.appendChild(myList);
          }
        }
        this.content.style.display = "block";
      }
    };

    this.reset = function () {
      this.content.style.display = "none";
    };
  }
  /**
   * Manages all possible grades of an exam
   * @param  {} options - particular parameter used to enclose multiple parameters
   */
  function ResultPage(options) {
    this.alert = options["alert"];
    this.content = options["content"];
    this.contentBody = options["contentBody"];
    this.btnPrint = options["btnPrint"];

    this.show = function (sessionId) {
      var self = this;
      this.sessionId = sessionId;
      makeCall(
        "GET",
        "GetExamResult?examSessionID=" + sessionId,
        null,
        function (request) {
          if (request.readyState == 4) {
            var message = request.responseText;
            if (request.status == 200) {
              self.update(JSON.parse(request.responseText));
            } else {
              self.alert.textContent = message;
              breakLine = document.createElement("br");
              refresh = document.createElement("button");
              refresh.classList.add("btn", "btn-light", "m-2");
              refresh.href = "#";
              refresh.addEventListener(
                "click",
                (e) => {
                  pageOrchestrator.refresh();
                  corsiElenco.show();
                },
                false
              );
              refresh.innerHTML = "Ricarica la pagina";
              self.alert.appendChild(breakLine);
              self.alert.appendChild(refresh);
            }
          }
        }
      );
    };

    this.update = function (parameter) {
      var sessionId,
        refuseState,
        message,
        btn,
        self = this;
      this.contentBody.innerHTML = "";
      if (
        parameter.state.toLowerCase() === "non inserito" ||
        parameter.state.toLowerCase() === "inserito"
      ) {
        message = document.createElement("p");
        message.textContent = "Voto non ancora definito";
        this.contentBody.appendChild(message);
      } else {
        this.contentBody.innerHTML =
          "Matricola: " +
          parameter.student.serialNumber +
          "<br/> Cognome e Nome: " +
          parameter.student.surname +
          " " +
          parameter.student.name +
          "<br/> Mail: " +
          parameter.student.mail +
          "<br/> Corso di Laurea: " +
          parameter.student.degreeCourse +
          "<br/> Valutazione: " +
          parameter.result +
          "<br/>";

        if (parameter.state.toLowerCase() === "verbalizzato") {
          message = document.createElement("h1");
          message.classList.add("text-primary");
          message.textContent = "Valutazione Verbalizzata";
          this.contentBody.appendChild(message);
        } else if (parameter.state.toLowerCase() === "rifiutato") {
          message = document.createElement("h1");
          message.classList.add("text-danger");
          message.textContent = "Valutazione Rifiutata";
          this.contentBody.appendChild(message);
        }

        form = document.createElement("form");
        sessionId = document.createElement("input");
        sessionId.type = "hidden";
        sessionId.value = this.sessionId;
        sessionId.name = "examSessionID";
        form.appendChild(sessionId);
        refuseState = document.createElement("input");
        refuseState.type = "hidden";
        refuseState.value = "true";
        refuseState.name = "refuse";
        form.appendChild(refuseState);

        btn = document.createElement("input");
        btn.type = "button";
        btn.classList.add("btn", "btn-danger", "no-print", "m-2");
        btn.value = "Rifiuta";
        if (parameter.state.toLowerCase() === "pubblicato") {
          btn.addEventListener(
            "click",
            (e) => {
              var form = e.target.closest("form");
              makeCall("POST", "GetExamResult", form, function (req) {
                if (req.readyState == 4) {
                  var message = req.responseText;
                  if (req.status == 200) {
                    pageOrchestrator.refresh();
                    resultPage.show(self.sessionId);
                  } else if (req.status == 403) {
                  } else {
                    self.alert.textContent = message;
                  }
                }
              });
            },
            false
          );
        } else {
          btn.style.display = "none";
        }
        form.appendChild(btn);
        this.contentBody.appendChild(form);
      }
      this.content.style.display = "block";
    };

    this.reset = function () {
      this.content.style.display = "none";
    };
  }

  /**
   * Manages all interactions within the page, without completely reloading the page
   */
  function PageOrchestrator() {
    var alertContainer = document.getElementById("id_alert");

    this.start = function () {
      corsiElenco = new CorsiElenco(
        alertContainer,
        document.getElementById("id_classContainer")
      );

      pageTitle = new PageTitle(document.getElementById("id_title"));

      resultPage = new ResultPage({
        alert: alertContainer,
        content: document.getElementById("id_resultContainer"),
        contentBody: document.getElementById("id_resultBody"),
        btnPrint: document.getElementById("id_printPage"),
      });

      //implements the function of the buttons backToHome
      document.querySelectorAll("button.backToHomeBtn").forEach((btn) => {
        btn.classList.add("btn", "btn-primary", "m-2");
        btn.innerHTML = "Ritorna alla home";
        btn.addEventListener(
          "click",
          () => {
            this.refresh();
            corsiElenco.show();
          },
          false
        );
      });

      //implements the function of the buttons printPage
      document.querySelectorAll("button.printPage").forEach((btn) => {
        btn.classList.add("btn", "btn-secondary", "m-2");
        btn.innerHTML = "Stampa";
        btn.addEventListener(
          "click",
          () => {
            window.print();
          },
          false
        );
      });

      //implements the function of the buttons logout
      document.querySelectorAll("button.logoutBtn").forEach((btn) => {
        btn.classList.add("btn", "btn-danger", "m-2");
        btn.innerHTML = "Logout";
        btn.addEventListener(
          "click",
          () => {
            window.sessionStorage.removeItem("teacher");
            makeCall("GET", "LogoutAction", null, function (request) {
              window.location.href = "index.html";
            });
          },
          false
        );
      });
    };

    this.refresh = function () {
      alertContainer.textContent = "";
      pageTitle.reset();
      resultPage.reset();
      corsiElenco.reset();
    };
  }
}
