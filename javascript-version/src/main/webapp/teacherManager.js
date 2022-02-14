{
  let pageTitle,
    modalModifyResult,
    modalBulkModifyResults,
    corsiElenco,
    iscrittiElenco,
    verbale,
    pageOrchestrator = new PageOrchestrator(); // main controller

  window.addEventListener(
    "load",
    () => {
      if (sessionStorage.getItem("teacher") == null) {
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
   * Manages the modal window to set a student's grade
   * @param  {} options - particular parameter used to enclose multiple parameters
   */
  function ModalModifyResult(options) {
    this.alert = options["alert"];
    this.content = options["content"];
    this.contentSet = options["contentSet"];
    this.result = options["result"];
    this.setResultForm = options["setResultForm"];
    this.close = options["close"];

    this.setResult = function (orchestrator) {
      this.setResultForm
        .querySelector("input[type='button']")
        .addEventListener("click", (event) => {
          var self = this,
            form = event.target.closest("form");
          makeCall("POST", "SetExamResult", form, function (req) {
            if (req.readyState == 4) {
              var message = req.responseText;
              if (req.status == 200) {
                orchestrator.refresh();
                iscrittiElenco.show(self.sessionId);
              } else if (req.status == 403) {
              } else {
                self.alert.textContent = message;
              }
            }
          });
        });
    };

    this.show = function (sessionId, studentId) {
      this.sessionId = sessionId;
      this.studentId = studentId;
      var self = this;
      makeCall(
        "GET",
        "SetExamResult?examSessionID=" + sessionId + "&studentId=" + studentId,
        null,
        function (request) {
          if (request.readyState == 4) {
            var message = request.responseText;
            if (request.status == 200) {
              self.update(JSON.parse(request.responseText));
            } else {
              self.alert.textContent = message;
            }
          }
        }
      );
    };

    this.update = function (sessionResult) {
      var surname,
        name,
        serialNumber,
        degreeCourse,
        self = this;

      surname = "Cognome: " + sessionResult.student.surname;
      name = "<br/>Nome: " + sessionResult.student.name;
      serialNumber = "<br/>Matricola: " + sessionResult.student.serialNumber;
      degreeCourse = "<br/>Corso: " + sessionResult.student.degreeCourse;
      this.contentSet.innerHTML = surname + name + serialNumber + degreeCourse;
      this.content.style.display = "block";

      this.setResultForm.querySelector(
        "input[name='setResultSessionID']"
      ).value = this.sessionId;
      this.setResultForm.querySelector(
        "input[name='setResultStudentID']"
      ).value = this.studentId;

      this.close.addEventListener(
        "click",
        () => {
          self.reset();
        },
        false
      );
      window.onclick = function (event) {
        if (event.target == self.content) {
          self.reset();
        }
      };
    };

    this.reset = function () {
      this.content.style.display = "none";
    };
  }

  /**
   * Manages the modal window to set grades for multiple students
   * @param  {} options - particular parameter used to enclose multiple parameters
   */
  function ModalBulkModifyResults(options) {
    this.alert = options["alert"];
    this.content = options["content"];
    this.bulkResultForm = options["bulkResultForm"];
    this.tableBody = options["tableBody"];
    this.genericSelectResult = options["genericSelectResult"];
    this.close = options["close"];

    this.setResults = function (orchestrator) {
      this.bulkResultForm
        .querySelector("input[type='button']")
        .addEventListener("click", (event) => {
          var self = this,
            examResults,
            examResult,
            student,
            raw;
          form = event.target.closest("form");
          examResults = [];
          raw = form.querySelectorAll("select");
          raw.forEach(function (item) {
            student = {
              serialNumber: item.id.replace("student_", ""),
            };
            examResult = {
              result: item.value,
              student: student,
            };
            examResults.push(examResult);
          });
          sendJSON(
            "SetBulkOfResult?examSessionID=" + self.sessionId,
            examResults,
            function (req) {
              if (req.readyState == 4) {
                var message = req.responseText;
                if (req.status == 200) {
                  orchestrator.refresh();
                  iscrittiElenco.show(self.sessionId);
                } else {
                  self.alert.textContent = message;
                }
              }
            }
          );
        });
    };

    this.show = function (sessionId) {
      this.sessionId = sessionId;
      var self = this;
      makeCall(
        "GET",
        "SetBulkOfResult?examSessionID=" + self.sessionId,
        null,
        function (request) {
          if (request.readyState == 4) {
            var message = request.responseText;
            if (request.status == 200) {
              self.update(JSON.parse(request.responseText));
            } else {
              self.alert.textContent = message;
            }
          }
        }
      );
    };

    this.update = function (parameter) {
      var row,
        serialNumber,
        surnameName,
        selectResult,
        students = parameter,
        length = students.length;
      this.tableBody.innerHTML = "";
      var self = this;
      for (let i = 0; i < length; i++) {
        row = document.createElement("tr");

        serialNumber = document.createElement("td");
        serialNumber.textContent = students[i].serialNumber;
        row.appendChild(serialNumber);

        surnameName = document.createElement("td");
        surnameName.textContent = students[i].surname + " " + students[i].name;
        row.appendChild(surnameName);

        selectResult = document.createElement("td");
        selectResult.innerHTML = this.genericSelectResult.innerHTML;
        selectResult.querySelector("select").id =
          "student_" + students[i].serialNumber;
        row.appendChild(selectResult);

        self.tableBody.appendChild(row);
      }
      this.close.addEventListener(
        "click",
        () => {
          self.reset();
        },
        false
      );
      window.onclick = function (event) {
        if (event.target == self.content) {
          self.reset();
        }
      };

      this.content.style.display = "block";
    };

    this.reset = function () {
      this.content.style.display = "none";
    };
  }
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
      makeCall("GET", "GetTeacherCourses", null, function (request) {
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
        examSessionList,
        warningBox,
        length = corsi.length;

      if (length === 0) {
        this.alert.textContent = "Nessun Corso, i tuoi dati non sono ancora stati inserti, contatta la segreteria";
      } else {
        pageTitle.update("Home");
        this.content.innerHTML = "";
        var self = this;

        for (let i = 0; i < length; i++) {
          courseName = document.createElement("h3");
          courseName.textContent = corsi[i].courseName;
          self.content.appendChild(courseName);

          examSessionList = corsi[i].examSessionList;

          if (examSessionList.length == 0) {
            warningBox = document.createElement("div");
            warningBox.classList.add("alert", "alert-warning", "no-print");
            warningBox.innerHTML = "Nessun appello registrato per questo corso";
            self.content.appendChild(warningBox);
          } else {
            const myList = document.createElement("ul");
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
                  iscrittiElenco.show(e.target.getAttribute("sessionId"));
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
   * Manages all those enrolled in an exam session
   * @param  {} options - particular parameter used to enclose multiple parameters
   */
  function IscrittiElenco(options) {
    this.alert = options["alert"];
    this.content = options["content"];
    this.contentbody = options["contentbody"];
    this.resultActionForm = options["resultActionForm"];

    // implements the function of the button that publish an exam session
    this.publishExam = function (orchestrator) {
      this.resultActionForm
        .querySelector("input[value='Pubblica']")
        .addEventListener("click", (e) => {
          var self = this,
            form = e.target.closest("form");
          makeCall("POST", "GetSubscribedStudents", form, function (req) {
            if (req.readyState == 4) {
              var message = req.responseText;
              if (req.status == 200) {
                orchestrator.refresh();
                iscrittiElenco.show(self.sessionId);
              } else {
                self.alert.textContent = message;
              }
            }
          });
        });
    };

    // implements the function of the button that verbalizes an exam session
    this.verbalizeExam = function (orchestrator) { 
      this.resultActionForm
        .querySelector("input[value='Verbalizza']")
        .addEventListener("click", (e) => {
          orchestrator.refresh();
          verbale.show(this.sessionId);
        });
    };

    this.show = function (sessionId) {
      var self = this;
      makeCall(
        "GET",
        "GetSubscribedStudents?examSessionID=" + sessionId,
        null,
        function (request) {
          if (request.readyState == 4) {
            var message = request.responseText;
            if (request.status == 200) {
              self.sessionId = sessionId;
              var iscritti = JSON.parse(request.responseText);
              self.update(iscritti);
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

    this.update = function (iscritti) {
      var row,
        serialNumber,
        surnameName,
        email,
        course,
        result,
        state,
        modifyBtn,
        length = iscritti.subscribers.length;
      pageTitle.update("Iscritti");
      if (length === 0) {
        this.alert.textContent = "Nessun Iscritto";
      } else {
        this.contentbody.innerHTML = "";
        var self = this;
        for (let i = 0; i < length; i++) {
          row = document.createElement("tr");

          serialNumber = document.createElement("td");
          serialNumber.textContent = iscritti.subscribers[i].serialNumber;
          row.appendChild(serialNumber);

          surnameName = document.createElement("td");
          surnameName.textContent =
            iscritti.subscribers[i].surname +
            " " +
            iscritti.subscribers[i].name;
          row.appendChild(surnameName);

          email = document.createElement("td");
          email.textContent = iscritti.subscribers[i].mail;
          row.appendChild(email);

          course = document.createElement("td");
          course.textContent = iscritti.subscribers[i].degreeCourse;
          row.appendChild(course);

          result = document.createElement("td");
          result.textContent = iscritti.results[i];
          row.appendChild(result);

          state = document.createElement("td");
          state.textContent = iscritti.states[i];
          row.appendChild(state);

          modifyBtn = document.createElement("td");
          btn = document.createElement("button");
          btn.classList.add("btn", "btn-dark", "no-print");
          btnText = document.createTextNode("Modifica");
          btn.appendChild(btnText);
          if (
            state.textContent !== "PUBBLICATO" &&
            state.textContent !== "VERBALIZZATO" &&
            state.textContent !== "RIFIUTATO"
          ) {
            btn.addEventListener(
              "click",
              () => {
                modalModifyResult.show(
                  this.sessionId,
                  iscritti.subscribers[i].serialNumber
                );
              },
              false
            );
          } else {
            btn.style.display = "none";
          }
          modifyBtn.appendChild(btn);
          row.appendChild(modifyBtn);
          self.contentbody.appendChild(row);
        }

        this.resultActionForm
          .querySelector("input[value='INSERIMENTO MULTIPLO']")
          .addEventListener("click", () => {
            modalBulkModifyResults.show(this.sessionId);
          });
        this.resultActionForm.querySelector("input[type = 'hidden']").value =
          this.sessionId;

        this.content.style.display = "block";
      }
    };

    this.reset = function () {
      this.content.style.display = "none";
    };
  }
  /**
   * Manages the creation of the vision and the possible printing of a report
   * @param  {} options - particular parameter used to enclose multiple parameters
   */
  function Verbale(options) {
    this.alert = options["alert"];
    this.content = options["content"];
    this.reportInfo = options["reportInfo"];
    this.contentbody = options["contentbody"];

    this.show = function (sessionId) {
      var self = this;
      makeCall(
        "GET",
        "Verbale?examSessionID=" + sessionId,
        null,
        function (request) {
          if (request.readyState == 4) {
            var message = request.responseText;
            if (request.status == 200) {
              self.sessionId = sessionId;
              var report = JSON.parse(request.responseText);
              self.update(report);
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

    this.update = function (report) {
      var row,
        name,
        surname,
        serialNumber,
        result,
        length = report.students.length;

      if (length === 0) {
        this.alert.textContent = "Verbale Vuoto";
      }
      pageTitle.update("Verbale");
      this.contentbody.innerHTML = "";
      var self = this;
      this.reportInfo.innerHTML =
        "Verbale numero: " +
        report.reportId +
        "<br/> Creato in Data: " +
        report.dateTime +
        "<br/>  Associato all'appello del: " +
        report.examSession.sessionDate;
      for (let i = 0; i < length; i++) {
        row = document.createElement("tr");

        name = document.createElement("td");
        name.textContent = report.students[i].name;
        row.appendChild(name);

        surname = document.createElement("td");
        surname.textContent = report.students[i].surname;
        row.appendChild(surname);

        serialNumber = document.createElement("td");
        serialNumber.textContent = report.students[i].serialNumber;
        row.appendChild(serialNumber);

        result = document.createElement("td");
        result.textContent = report.results[i];
        row.appendChild(result);
        self.contentbody.appendChild(row);
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

      iscrittiElenco = new IscrittiElenco({
        alert: alertContainer,
        content: document.getElementById("id_subscriberContainer"),
        contentbody: document.getElementById("id_iscrittiBody"),
        resultActionForm: document.getElementById("id_resultActionForm"),
      });

      modalModifyResult = new ModalModifyResult({
        alert: alertContainer,
        content: document.getElementById("id_modifyResultModalContainer"),
        contentSet: document.getElementById("id_contentSet"),
        result: document.getElementById("id_result"),
        setResultForm: document.getElementById("id_drawResultForm"),
        close: document.getElementById("id_closeResult"),
      });

      modalBulkModifyResults = new ModalBulkModifyResults({
        alert: alertContainer,
        content: document.getElementById("id_bulkModifyResultModalContainer"),
        bulkResultForm: document.getElementById("id_bulkModifyResultForm"),
        tableBody: document.getElementById("id_bulkModifyResultTbody"),
        genericSelectResult: document.getElementById("id_genericSelectResult"),
        close: document.getElementById("id_closeBulkModifyResult"),
      });

      verbale = new Verbale({
        alert: alertContainer,
        content: document.getElementById("id_reportContainer"),
        reportInfo: document.getElementById("id_reportInfo"),
        contentbody: document.getElementById("id_reportTbody"),
      });

      pageTitle = new PageTitle(document.getElementById("id_title"));

      iscrittiElenco.publishExam(this);
      iscrittiElenco.verbalizeExam(this);
      modalModifyResult.setResult(this);
      modalBulkModifyResults.setResults(this);

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
      corsiElenco.reset();
      iscrittiElenco.reset();
      verbale.reset();
      modalModifyResult.reset();
      modalBulkModifyResults.reset();
    };
  }
}
